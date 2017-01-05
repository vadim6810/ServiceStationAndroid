package il.co.tel_ran.carservice.fragments;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Place;

import java.util.ArrayList;
import java.util.List;

import il.co.tel_ran.carservice.ClientUser;
import il.co.tel_ran.carservice.LoadPlaceTask;
import il.co.tel_ran.carservice.R;
import il.co.tel_ran.carservice.TenderRequest;
import il.co.tel_ran.carservice.UserType;
import il.co.tel_ran.carservice.activities.ClientMainActivity;
import il.co.tel_ran.carservice.activities.PostTenderActivity;
import il.co.tel_ran.carservice.adapters.TenderRequestsAdapter;
import il.co.tel_ran.carservice.connection.DataRequest;
import il.co.tel_ran.carservice.connection.DataResult;
import il.co.tel_ran.carservice.connection.RequestMaker;
import il.co.tel_ran.carservice.connection.ServerResponseError;
import il.co.tel_ran.carservice.connection.TenderRequestDataRequest;
import il.co.tel_ran.carservice.connection.TenderRequestMaker;

/**
 * Created by Max on 16/09/2016.
 */
public class RequestServiceTabFragment extends RefreshingFragment
        implements View.OnClickListener, TenderRequestsAdapter.TenderRequestClickListener,
        RequestMaker.OnDataRetrieveListener {

    private View mMainLayout;

    private RecyclerView mTenderRequestsRecyclerView;

    private ProgressBar mProgressbar;

    private TextView mNoRequestsTextView;

    private ClientUser mUser;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mMainLayout == null) {
            mMainLayout = inflater.inflate(R.layout.fragment_tab_request_service, container, false);

            mProgressbar = (ProgressBar) mMainLayout.findViewById(R.id.progress_bar);

            mNoRequestsTextView = (TextView) mMainLayout.findViewById(
                    R.id.no_tender_requests_text_view);

            FloatingActionButton postTenderFAB = (FloatingActionButton) mMainLayout
                    .findViewById(R.id.post_tender_request_fab);
            postTenderFAB.setOnClickListener(this);

            setupRecyclerView();

            toggleProgressBar(true);

            loadTenderRequests();
        }

        return mMainLayout;
    }

    public void setUserData(ClientUser userData) {
        mUser = userData;
    }

    /*
     * RequestMaker.OnDataRetrieveListener
     */

    @Override
    public void onDataRetrieveSuccess(DataRequest dataRequest, DataResult result) {
        boolean anyResults = false;
        if (result.getDataType() == DataResult.Type.TENDER_REQUEST) {
            TenderRequest[] requests = (TenderRequest[]) result.getData();
            if (requests != null && requests.length > 0) {

                TenderRequestsAdapter adapter = (TenderRequestsAdapter) mTenderRequestsRecyclerView
                        .getAdapter();

                adapter.removeAllItems();
                adapter.addItems(requests);

                anyResults = true;

                loadGooglePlaceObjects();

            } else {
                // TODO: handle error
            }
        }

        toggleProgressBar(false);

        if (anyResults)
            mNoRequestsTextView.setVisibility(View.GONE);

        onRefreshEnd();
    }

    @Override
    public void onDataRetrieveFailed(DataRequest dataRequest, DataResult.Type resultType,
                                     ServerResponseError error, @Nullable String message) {
        // TODO: handle error.

        TenderRequestsAdapter adapter = (TenderRequestsAdapter) mTenderRequestsRecyclerView
                .getAdapter();

        adapter.removeAllItems();

        toggleProgressBar(false);

        onRefreshEnd();
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
                if (mUser != null) {
                    Intent intent = new Intent(getContext(), PostTenderActivity.class);
                    intent.putExtra("user", mUser);

                    if (clientMainActivity != null) {
                        clientMainActivity.startActivityForResult(intent,
                                ClientMainActivity.REQUEST_CODE_POST_TENDER);
                    }
                }
                break;
        }
    }

    /*
     * TenderRequestsAdapter.TenderRequestClickListener
     */

    @Override
    public void onClickRequest(View v) {
    }

    @Override
    public void onSendReply(View view, String message, boolean isUpdate) {

    }

    /*
     * RefreshingFragment.onRefreshStart
     */

    @Override
    public void onRefreshStart() {
        super.onRefreshStart();

        loadTenderRequests();
    }

    private void setupRecyclerView() {
        mTenderRequestsRecyclerView = (RecyclerView) mMainLayout.findViewById(
                R.id.tender_requests_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mTenderRequestsRecyclerView.setLayoutManager(layoutManager);

        TenderRequestsAdapter tenderRequestsAdapter = new TenderRequestsAdapter(
                new ArrayList<TenderRequest>(), getContext(), this, UserType.CLIENT);
        mTenderRequestsRecyclerView.setAdapter(tenderRequestsAdapter);
    }

    private void toggleProgressBar(boolean toggle) {
        if (toggle) {
            mTenderRequestsRecyclerView.setVisibility(View.GONE);
            mNoRequestsTextView.setVisibility(View.GONE);
            mProgressbar.setVisibility(View.VISIBLE);
        } else {
            mTenderRequestsRecyclerView.setVisibility(View.VISIBLE);
            mNoRequestsTextView.setVisibility(View.VISIBLE);
            mProgressbar.setVisibility(View.GONE);

        }
    }

    private void loadTenderRequests() {
        // Make the request
        TenderRequestDataRequest request = new TenderRequestDataRequest();

        // Send the request
        new TenderRequestMaker(this).makeRequest(getContext(), request);
    }

    private void loadGooglePlaceObjects() {
        Activity containerActivity = getActivity();
        if (containerActivity != null) {
            try {
                // Get Google API client from container activity.
                ClientMainActivity clientMainActivity = (ClientMainActivity) containerActivity;
                GoogleApiClient googleApiClient = clientMainActivity.getGoogleApiClient();

                if (googleApiClient != null) {
                    TenderRequestsAdapter adapter
                            = (TenderRequestsAdapter) mTenderRequestsRecyclerView.getAdapter();
                    final List<TenderRequest> tenderRequests = adapter.getAllItems();

                    // Check if we have any items to display
                    if (tenderRequests != null && !tenderRequests.isEmpty()) {
                        final int tenderRequestsCount = tenderRequests.size();
                        final String[] placeIds = new String[tenderRequestsCount];

                        // We use iteration by index to match Place objects to placeIds
                        for (int i = 0; i < tenderRequestsCount; i++) {
                            placeIds[i] = tenderRequests.get(0).getPlaceID();
                        }

                        new LoadPlaceTask(googleApiClient) {
                            @Override
                            protected void onPostExecute(Place[] places) {
                                if (places != null) {
                                    for (int i = 0; i < places.length; i++) {
                                        Place place = places[i];
                                        if (place != null) {
                                            // Make sure we stay in index bound - this is possible if user has refreshed the recycler view
                                            // and we got different results (hence different size as-well).
                                            try {
                                                TenderRequest request = tenderRequests.get(i);
                                                if (request != null) {
                                                    String requestPlaceId = request.getPlaceID();
                                                    // Compare place Ids to make sure we don't assign the wrong Place object to this request.
                                                    // This is possible if user has refreshed and we got a different set of results.
                                                    if (requestPlaceId.equals(place.getId())) {
                                                        request.setPlace(place);
                                                    }
                                                }
                                            } catch (IndexOutOfBoundsException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }
                                }
                            }
                        }.execute(placeIds);
                    }
                }
            } catch (ClassCastException e) {
                e.printStackTrace();
            }
        }
    }
}
