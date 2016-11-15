package il.co.tel_ran.carservice.dialogs;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.AppCompatRatingBar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Locale;

import il.co.tel_ran.carservice.LoadPlacePhotoTask;
import il.co.tel_ran.carservice.R;
import il.co.tel_ran.carservice.ServiceStation;
import il.co.tel_ran.carservice.ServiceSearchResult;
import il.co.tel_ran.carservice.Utils;
import il.co.tel_ran.carservice.activities.ClientMainActivity;

/**
 * Created by Max on 10/10/2016.
 */

public class ServiceDetailsDialog extends DialogFragment implements View.OnClickListener,
        ServiceSubmitRatingDialog.SubmitRatingDialogListener,
        ServiceLeaveMessageDialog.LeaveMessageDialogListener
{

    private ServiceSearchResult mSearchResult;
    private CharSequence mServicesText;

    private Fragment mCallingFragment;

    public enum ITEM_TYPE {
        ITEM_FAB,
        ITEM_CONTACT_DETAILS,
        ITEM_LEAVE_MESSAGE,
        ITEM_LEAVE_RATING,
        ITEM_DISMISS
    }

    public interface ServiceDetailsDialogListener {
        void onItemClick(DialogFragment dialogFragment, ITEM_TYPE itemType,
                         ServiceSearchResult result, View view);
    }

    public static ServiceDetailsDialog getInstance(CharSequence servicesText,
                                                   ServiceSearchResult serviceSearchResult) {
        return getInstance(servicesText, serviceSearchResult, null);
    }

    public static ServiceDetailsDialog getInstance(CharSequence servicesText,
                                                   ServiceSearchResult serviceSearchResult,
                                                   @Nullable Fragment callingFragment) {
        ServiceDetailsDialog serviceDetailsDialog = new ServiceDetailsDialog();

        Bundle args = new Bundle();
        args.putCharSequence("services_text", servicesText);
        serviceDetailsDialog.setArguments(args);

        serviceDetailsDialog.setSearchResult(serviceSearchResult);
        serviceDetailsDialog.setCallingFragment(callingFragment);

        return serviceDetailsDialog;
    }

    @Override
    public void onClick(View v) {
        boolean dismiss = false;

        ITEM_TYPE itemType = null;
        switch (v.getId()) {
            case R.id.open_map_fab:
                itemType = ITEM_TYPE.ITEM_FAB;
                // Open Google Maps with navigation directions.
                Uri gmmIntentUri = Uri.parse(
                        "google.navigation:q=" + mSearchResult.getSerivce().getLocation().getAddress());
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
                break;
            case R.id.contact_details_image_view:
                itemType = ITEM_TYPE.ITEM_CONTACT_DETAILS;
                showContactDetailsDialog();
                break;
            case R.id.leave_message_image_view:
                itemType = ITEM_TYPE.ITEM_LEAVE_MESSAGE;
                showLeaveMessageDialog();
                break;
            case R.id.leave_rating_image_view:
                itemType = ITEM_TYPE.ITEM_LEAVE_RATING;
                showSubmitRatingDialog();
                break;
            case R.id.dismiss_service_details:
                itemType = ITEM_TYPE.ITEM_DISMISS;
                dismiss = true;
                break;
        }

        try {
            if (itemType != null && mCallingFragment != null) {
                // This dialog was instantiated within a fragment
                ServiceDetailsDialogListener fragment =
                        (ServiceDetailsDialogListener) mCallingFragment;
                fragment.onItemClick(this, itemType, mSearchResult, v);
            } else {
                // This dialog was instantiated within an activity
                ServiceDetailsDialogListener activity = (ServiceDetailsDialogListener) getActivity();
                activity.onItemClick(this, itemType, mSearchResult, v);
            }
        } catch (ClassCastException e) {
            e.printStackTrace();
        }

        if (dismiss)
            dismiss();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        Bundle args = getArguments();
        if (args != null && !args.isEmpty()) {
            mServicesText = getArguments().getCharSequence("services_text");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.service_details_card_layout, null);

        ServiceStation serviceStation = mSearchResult.getSerivce();

        // ImageView for service's photo (based on Google Maps address).
        final ImageView placeImageView = (ImageView) layout
                .findViewById(R.id.service_details_photo);
        // Measuring is required so we can pass width & height to Google API to retrieve
        // scaled photo.
        placeImageView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);

        // Set service's name
        final TextView serviceNameTextView = (TextView) layout
                .findViewById(R.id.service_details_name);
        serviceNameTextView.setText(serviceStation.getName());

        // Set service's address (from Google Maps).
        final TextView serviceAddressTextView = (TextView) layout
                .findViewById(R.id.service_details_address);
        serviceAddressTextView.setText(serviceStation.getLocation().getAddress());

        // Get the text from the search result view (from the adapter).
        // This is done because the EnumSet<ServiceType> types were already parsed to string.
        // It is easier to simply extract it from the TextView.
        final TextView serviceTypesTextView = (TextView) layout
                .findViewById(R.id.service_details_services);
        serviceTypesTextView.setText(
                getString(R.string.service_details_available_services, mServicesText));

        // Set avg rating & submitted rating count.
        final TextView ratingSubmitTextView = (TextView) layout
                .findViewById(R.id.rating_submit_count);
        ratingSubmitTextView.setText(String.format(
                Locale.getDefault(), "%.2f", serviceStation.getAvgRating())
                + " (" + Integer.toString(serviceStation.getSubmittedRatings()) + ')');

        final AppCompatRatingBar ratingBar = (AppCompatRatingBar) layout
                .findViewById(R.id.service_rating_bar);
        ratingBar.setRating(serviceStation.getAvgRating());

        // Get photo for this Google Maps address to display.
        new LoadPlacePhotoTask(((ClientMainActivity) getActivity()).getGoogleApiClient(),
                placeImageView.getMeasuredWidth(), placeImageView.getMeasuredHeight()) {

            @Override
            protected void onPostExecute(Bitmap bitmapPhoto) {
                if (bitmapPhoto != null) {
                    // Photo has been loaded, display it.
                    placeImageView.setImageBitmap(bitmapPhoto);

                }
            }
        }.execute(serviceStation.getLocation().getId());

        final FloatingActionButton openMapFAB = (FloatingActionButton) layout
                .findViewById(R.id.open_map_fab);
        openMapFAB.setOnClickListener(this);

        final ImageView contactDetails = (ImageView) layout.findViewById(
                R.id.contact_details_image_view);
        contactDetails.setOnClickListener(this);

        final ImageView leaveMessage = (ImageView) layout.findViewById(
                R.id.leave_message_image_view);
        leaveMessage.setOnClickListener(this);

        final ImageView leaveRating = (ImageView) layout.findViewById(
                R.id.leave_rating_image_view);
        leaveRating.setOnClickListener(this);

        final Button dismiss = (Button) layout.findViewById(R.id.dismiss_service_details);
        dismiss.setOnClickListener(this);
        return layout;
    }

    public void setSearchResult(ServiceSearchResult result) {
        mSearchResult = result;
    }

    public void setCallingFragment(Fragment fragment) {
        mCallingFragment = fragment;
    }

    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance()) {
            getDialog().setDismissMessage(null);
        }
        super.onDestroyView();
    }

    @Override
    public void onRatingSubmitted(float rating, ServiceSearchResult searchResult) {
        // TODO: send new rating to back-end (update if exists, add if not)
        // TODO: update the search result rating card (after back-end receives update)
    }

    @Override
    public void onMessageSubmitted(CharSequence message, ServiceSearchResult searchResult) {
        // TODO: send submitted message to back-end
    }

    private void showContactDetailsDialog() {
        ServiceStation serviceStation = mSearchResult.getSerivce();
        ServiceContactDetailsDialog contactDetailsDialog = ServiceContactDetailsDialog.getInstance(
                serviceStation.getPhonenumber(), serviceStation.getEmail());
        Utils.showDialogFragment(getFragmentManager(), contactDetailsDialog,
                "contact_details_dialog");
    }

    private void showSubmitRatingDialog() {
        // TODO: check if user has already submitted rating before, and use it as the current rating.
        ServiceSubmitRatingDialog submitRatingDialog = ServiceSubmitRatingDialog.getInstance(0.0f,
                this, mSearchResult);
        Utils.showDialogFragment(getFragmentManager(), submitRatingDialog,
                "submit_rating_dialog");
    }

    private void showLeaveMessageDialog() {
        ServiceLeaveMessageDialog submitRatingDialog = ServiceLeaveMessageDialog.getInstance(mSearchResult,
                this);
        Utils.showDialogFragment(getFragmentManager(), submitRatingDialog,
                "leave_message_dialog");
    }
}
