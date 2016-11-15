package il.co.tel_ran.carservice.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Place;

import java.util.ArrayList;
import java.util.List;

import il.co.tel_ran.carservice.ClientUser;
import il.co.tel_ran.carservice.R;
import il.co.tel_ran.carservice.ServerConnection;
import il.co.tel_ran.carservice.ServiceSearchResult;
import il.co.tel_ran.carservice.ServiceStation;
import il.co.tel_ran.carservice.ServiceType;
import il.co.tel_ran.carservice.TenderReply;
import il.co.tel_ran.carservice.TenderRequest;
import il.co.tel_ran.carservice.Utils;
import il.co.tel_ran.carservice.activities.ClientMainActivity;
import il.co.tel_ran.carservice.activities.PostTenderActivity;
import il.co.tel_ran.carservice.adapters.TenderRepliesAdapter;
import il.co.tel_ran.carservice.dialogs.ServiceDetailsDialog;

/**
 * Created by Max on 16/09/2016.
 */
public class RequestServiceTabFragment extends Fragment
        implements View.OnClickListener, TenderRepliesAdapter.TenderReplyClickListener,
        ServerConnection.OnTenderRepliesRetrievedListener,
        ServiceDetailsDialog.ServiceDetailsDialogListener {

    private View mMainLayout;

    private RecyclerView mTenderRepliesRecyclerView;

    private FloatingActionButton mPostTenderFAB;

    private ClientUser mUser;

    private View mTenderRequestLayout;
    private View mTenderRequestCard;
    private View mTenderRequestDetailsLayout;
    private TextView mRequestMessageTextView;
    private TextView mRequestLocationTextView;

    private TenderRequest mTenderRequest;

    private ServerConnection mServerConnection;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        mServerConnection = new ServerConnection();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mMainLayout == null) {
            mMainLayout = inflater.inflate(R.layout.fragment_tab_request_service, container, false);

            mTenderRequestCard = mMainLayout.findViewById(R.id.active_tender_request_card);
            mTenderRequestLayout = mMainLayout.findViewById(R.id.active_tender_request_layout);
            mTenderRequestDetailsLayout = mMainLayout.findViewById(R.id.active_tender_request_details_layout);
            mTenderRequestDetailsLayout.setOnClickListener(this);

            mPostTenderFAB = (FloatingActionButton) mMainLayout
                    .findViewById(R.id.post_tender_request_fab);
            mPostTenderFAB.setOnClickListener(this);

            mRequestMessageTextView = (TextView) mMainLayout.findViewById(R.id.request_message_text_view);
            mRequestLocationTextView = (TextView) mMainLayout.findViewById(R.id.request_location_text_view);

            setupRecyclerView();

            setupRemoveRequestButton();

            mTenderRequest = new TenderRequest();
        }

        return mMainLayout;
    }

    /*
     * View.OnClickListener
     */

    @Override
    public void onClick(View v) {
        Activity containerActivity = getActivity();
        ClientMainActivity clientMainActivity = null;
        if (containerActivity != null) {
            try {
                clientMainActivity = (ClientMainActivity) containerActivity;
            } catch (ClassCastException e) {
                e.printStackTrace();
            }
        }
        switch (v.getId()) {
            case R.id.post_tender_request_fab:
                // FALLTHROUGH
            case R.id.active_tender_request_details_layout:
                if (mUser != null && mUser.getVehicleData() != null) {
                    if (mTenderRequest != null) {
                        mTenderRequest.setVehicleData(mUser.getVehicleData());
                    }
                    Intent intent = new Intent(getContext(), PostTenderActivity.class);
                    intent.putExtra("tender_request", mTenderRequest);

                    if (clientMainActivity != null) {
                        clientMainActivity.startActivityForResult(intent,
                                ClientMainActivity.REQUEST_CODE_POST_TENDER);
                    }
                }
                break;
            case R.id.remove_tender_request_button:
                // TODO: Add confirmation
                // TODO: Add undo snackbar

                // Hide the layout.
                mTenderRequestCard.setVisibility(View.GONE);

                onTenderRepliesRetrievingStarted();

                // Show the FAB
                mPostTenderFAB.show();
                break;
        }
    }

    public void onTenderRequestUpdate(TenderRequest request) {
        if (request != null) {
            mTenderRequest = request;

            // Update the message.
            mRequestMessageTextView.setText(getString(
                    R.string.tender_request_message, request.getServices(),
                    request.getVehicleData().toString()));

            // Update the location.
            if (request.getLocation() != null)
                mRequestLocationTextView.setText(request.getLocation());

            // Hide the fab if visible.
            mPostTenderFAB.setVisibility(View.GONE);

            // Make sure the layout is visible.
            mTenderRequestCard.setVisibility(View.VISIBLE);

            getTenderReplies();
        }
    }

    public void setUserData(ClientUser userData) {
        mUser = userData;
    }

    @Override
    public void onTenderRepliesRetrievingStarted() {
        Log.d("RSTF", "onTenderRepliesRetrievingStarted :: called.");
        TenderRepliesAdapter repliesAdapter = (TenderRepliesAdapter) mTenderRepliesRecyclerView
                .getAdapter();

        repliesAdapter.removeAllItems();
    }

    @Override
    public void onTenderRepliesRetrieved(List<TenderReply> tenderReplies) {
        Log.d("RSTF", "onTenderRepliesRetrieved :: called.");
        Log.d("RSTF", "onTenderRepliesRetrieved :: tenderReplies: " + tenderReplies);

        if (tenderReplies != null && !tenderReplies.isEmpty()) {
            TenderRepliesAdapter repliesAdapter = (TenderRepliesAdapter) mTenderRepliesRecyclerView
                    .getAdapter();

            repliesAdapter.addItems(tenderReplies);
        }
    }

    @Override
    public void onClickTenderReply(View view) {
        TenderRepliesAdapter repliesAdapter = (TenderRepliesAdapter) mTenderRepliesRecyclerView
                .getAdapter();

        // Find the position in the adapter for this view.
        int itemPos = mTenderRepliesRecyclerView.getChildAdapterPosition((View) view.getParent());
        // Get the result object for this position.
        final TenderReply tenderReply = repliesAdapter.getItem(itemPos);
        ServiceStation serviceStation = tenderReply.getReplyingService();

        List<String> serviceTypeStrings = Utils.parseServiceTypes(getContext(),
                serviceStation.getAvailableServices());
        String servicesText = "";
        for (String serviceString : serviceTypeStrings) {
            servicesText += serviceString + ", ";
        }

        // Remove last comma.
        servicesText = servicesText.substring(0, servicesText.length() - 2);

        ServiceDetailsDialog serviceDetailsDialog = ServiceDetailsDialog.getInstance(
                servicesText, new ServiceSearchResult(serviceStation), this);
        Utils.showDialogFragment(getFragmentManager(), serviceDetailsDialog,
                "service_details_dialog");
    }

    @Override
    public void onDeleteTenderReply(View view) {
        TenderRepliesAdapter repliesAdapter = (TenderRepliesAdapter) mTenderRepliesRecyclerView
                .getAdapter();

        // Find the position in the adapter for this view.
        // We call .getParent on the parent of the original clicked view - this is because of a deeper hierachy.
        int itemPos = mTenderRepliesRecyclerView.getChildAdapterPosition((View) (view.getParent().getParent()));
        repliesAdapter.removeItem(itemPos);
    }

    private void setupRecyclerView() {
        mTenderRepliesRecyclerView = (RecyclerView) mMainLayout.findViewById(
                R.id.tender_replies_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mTenderRepliesRecyclerView.setLayoutManager(layoutManager);

        TenderRepliesAdapter tenderRepliesAdapter = new TenderRepliesAdapter(
                new ArrayList<TenderReply>(), getContext(), this);
        mTenderRepliesRecyclerView.setAdapter(tenderRepliesAdapter);
    }

    private void setupRemoveRequestButton() {
        ImageButton removeButton = (ImageButton) mMainLayout.findViewById(
                R.id.remove_tender_request_button);

        // Set the color to match the title
        removeButton.setColorFilter(ContextCompat.getColor(getContext(), android.R.color.white),
                PorterDuff.Mode.SRC_ATOP);

        removeButton.setOnClickListener(this);
    }

    private void getTenderReplies() {
        Log.d("RSTF", "getTenderReplies :: called.");
        if (mServerConnection != null) {
            Log.d("RSTF", "getTenderReplies :: mServerConnection not null.");
            Activity containerActivity = getActivity();
            if (containerActivity != null) {
                Log.d("RSTF", "getTenderReplies :: containerActivity not null.");
                try {
                    // Try to cast to ClientMainActivity
                    ClientMainActivity clientMainActivity = (ClientMainActivity) containerActivity;

                    // Get Google api client
                    GoogleApiClient googleApiClient = clientMainActivity.getGoogleApiClient();
                    Log.d("RSTF", "getTenderReplies :: googleApiClient: " + googleApiClient);
                    if (googleApiClient != null) {
                        Log.d("RSTF", "getTenderReplies :: googleApiClient not null and connected.");
                        mServerConnection.getTenderReplies(googleApiClient, this);
                    }
                } catch (ClassCastException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void onItemClick(DialogFragment dialogFragment, ServiceDetailsDialog.ITEM_TYPE itemType,
                            ServiceSearchResult result, View view) {

    }
}
