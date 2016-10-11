package il.co.tel_ran.carservice;

import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Locale;

/**
 * Created by Max on 10/10/2016.
 */

public class ServiceDetailsDialog extends DialogFragment implements View.OnClickListener {

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
        ITEM_TYPE itemType = null;
        switch (v.getId()) {
            case R.id.open_map_fab:
                itemType = ITEM_TYPE.ITEM_FAB;
                break;
            case R.id.contact_details_image_view:
                itemType = ITEM_TYPE.ITEM_CONTACT_DETAILS;
                break;
            case R.id.leave_message_image_view:
                itemType = ITEM_TYPE.ITEM_LEAVE_MESSAGE;
                break;
            case R.id.leave_rating_image_view:
                itemType = ITEM_TYPE.ITEM_LEAVE_RATING;
                break;
            case R.id.dismiss_service_details:
                itemType = ITEM_TYPE.ITEM_DISMISS;
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

        // ImageView for service's photo (based on Google Maps address).
        final ImageView placeImageView = (ImageView) layout
                .findViewById(R.id.service_details_photo);
        // Measuring is required so we can pass width & height to Google API to retrieve
        // scaled photo.
        placeImageView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);

        // Set service's name
        final TextView serviceNameTextView = (TextView) layout
                .findViewById(R.id.service_details_name);
        serviceNameTextView.setText(mSearchResult.getName());

        // Set service's address (from Google Maps).
        final TextView serviceAddressTextView = (TextView) layout
                .findViewById(R.id.service_details_address);
        serviceAddressTextView.setText(mSearchResult.getLocation().getAddress());

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
                Locale.getDefault(), "%.2f", mSearchResult.getAvgRating())
                + " (" + Integer.toString(mSearchResult.getSubmittedRatings()) + ')');

        // Rating stars ImageView IDs
        final int[] ratingStarIds = {
                R.id.rating_star_1,
                R.id.rating_star_2,
                R.id.rating_star_3,
                R.id.rating_star_4,
                R.id.rating_star_5,
        };
        final ImageView[] ratingStars = new ImageView[ratingStarIds.length];
        for (int i = 0; i < ratingStarIds.length; i++) {
            ratingStars[i] = (ImageView) layout.findViewById(ratingStarIds[i]);
        /*
        Set the source according to the difference between avg rating and index.
        It is necessary to "fill" all stars up to the average rating (0.0f-5.0f).

        As long as the difference in every iteration is bigger than 1, it's a full star.
        If the difference is negative the star is empty.
        If the difference is a fraction (>0 & <1) it's half a star.
         */
            float diff = mSearchResult.getAvgRating() - i;
            if (diff >= 1) {
                ratingStars[i].setImageResource(R.mipmap.ic_star_full);
            } else if (diff <= 0) {
                ratingStars[i].setImageResource(R.mipmap.ic_star_empty);
            } else {
                ratingStars[i].setImageResource(R.mipmap.ic_star_half);
            }
        }

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
        }.execute(mSearchResult.getLocation().getId());

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
}
