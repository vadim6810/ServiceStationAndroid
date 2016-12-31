package il.co.tel_ran.carservice.fragments;

import android.app.Activity;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Place;

import java.util.ArrayList;
import java.util.HashSet;

import il.co.tel_ran.carservice.DividerItemDecoration;
import il.co.tel_ran.carservice.LoadPlaceTask;
import il.co.tel_ran.carservice.R;
import il.co.tel_ran.carservice.ServiceReview;
import il.co.tel_ran.carservice.connection.ServiceStationDataRequest;
import il.co.tel_ran.carservice.ServiceStation;
import il.co.tel_ran.carservice.ServiceType;
import il.co.tel_ran.carservice.Utils;
import il.co.tel_ran.carservice.activities.ClientMainActivity;
import il.co.tel_ran.carservice.adapters.ReviewsAdapter;
import il.co.tel_ran.carservice.connection.DataRequest;
import il.co.tel_ran.carservice.connection.DataResult;
import il.co.tel_ran.carservice.connection.RequestMaker;
import il.co.tel_ran.carservice.connection.ReviewRequest;
import il.co.tel_ran.carservice.connection.ReviewRequestMaker;
import il.co.tel_ran.carservice.connection.ServerResponseError;
import il.co.tel_ran.carservice.connection.ServiceStationRequestMaker;
import il.co.tel_ran.carservice.dialogs.ServiceDetailsDialog;

/**
 * Created by maxim on 20-Dec-16.
 */

public class ReviewsFragment extends RefreshingFragment
        implements AdapterView.OnItemSelectedListener, ReviewsAdapter.OnReviewClickListener,
        RequestMaker.OnDataRetrieveListener {

    private View mMainLayout;
    private AppCompatSpinner mServiceTypeSpinner;
    private TextView mNoReviewsTextView;
    private ProgressBar mReviewsRetrieveProgressBar;
    private RecyclerView mReviewsRecyclerView;

    private ServiceType mServiceType;

    private GoogleApiClient mGoogleApiClient;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mMainLayout == null) {
            mMainLayout = inflater.inflate(R.layout.fragment_reviews, container, false);

            setupSpinner();
            mReviewsRetrieveProgressBar = (ProgressBar) mMainLayout.findViewById(
                    R.id.reviews_progressbar);
            mNoReviewsTextView = (TextView) mMainLayout.findViewById(
                    R.id.no_reviews_text_view);

            setupRecyclerView();

            setupGoogleApiClient();
        }

        return mMainLayout;
    }

    /*
     * AdapterView.OnItemSelectedListener (Spinner)
     */

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        ServiceType selectedServiceType;
        switch (position) {
            case 0:
                // null indicates all
                selectedServiceType = null;
                break;
            case 1:
                selectedServiceType = ServiceType.AUTO_SERVICE;
                break;
            case 2:
                selectedServiceType = ServiceType.TYRE_REPAIR;
                break;
            case 3:
                selectedServiceType = ServiceType.CAR_WASH;
                break;
            case 4:
                selectedServiceType = ServiceType.TOWING;
                break;
            default:
                // null indicates all
                selectedServiceType = null;
                break;
        }

        loadReviews(selectedServiceType);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    /*
     * RequestMaker.OnDataRetrieveListener
     */

    @Override
    public void onDataRetrieveSuccess(DataRequest request, DataResult result) {
        final ReviewsAdapter adapter = (ReviewsAdapter) mReviewsRecyclerView.getAdapter();
        if (result.getDataType() == DataResult.Type.REVIEW) {
            processReviewsRetrieve((ServiceReview[]) result.getData(), adapter);
        } else if (result.getDataType() == DataResult.Type.SERVICE_STATION) {
            ServiceStation[] serviceStations = (ServiceStation[]) result.getData();

            boolean isFilterReviews = false;

            Bundle extras = request.getExtras();
            if (extras != null && !extras.isEmpty()) {
                isFilterReviews = extras.getBoolean("filter_services");
            }

            processServiceRetrieve(serviceStations, isFilterReviews);
        }

        onRefreshEnd();
    }

    @Override
    public void onDataRetrieveFailed(DataRequest request, DataResult.Type resultType,
                                     ServerResponseError error, @Nullable String message) {
        toggleProgressBar(false);
        mNoReviewsTextView.setVisibility(View.VISIBLE);

        // TODO: handle error

        onRefreshEnd();
    }

    /*
     * RefreshingFragment
     */

    @Override
    public void onRefreshStart() {
        super.onRefreshStart();

        loadReviews(mServiceType);
    }

    /*
     * ReviewsAdapter.OnReviewClickListener
     */

    @Override
    public void onClickReview(View view) {
        Log.d("RF", "onClickReview:: called.");
        // TODO: Add check for user signed in.

        // Find the position in the adapter for this view.
        int itemPos = mReviewsRecyclerView.getChildAdapterPosition(view);
        Log.d("RF", "onClickReview:: itemPos=" + itemPos);
        ReviewsAdapter adapter = (ReviewsAdapter) mReviewsRecyclerView
                .getAdapter();
        Log.d("RF", "onClickReview:: adapterItems=" + adapter.getItemCount());
        // Get the review object for this position.
        final ServiceReview review = adapter.getItem(itemPos);

        if (review != null) {
            ServiceStationRequestMaker requestMaker = new ServiceStationRequestMaker(this);
            requestMaker.makeRequest(getContext(), new ServiceStationDataRequest(review.getServiceId()));
        }
    }

    private void setupRecyclerView() {
        mReviewsRecyclerView = (RecyclerView) mMainLayout.findViewById(
                R.id.reviews_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mReviewsRecyclerView.setLayoutManager(layoutManager);

        // Load divider for our recycler view
        Drawable dividerDrawable = ContextCompat
                .getDrawable(getContext(),R.drawable.recycler_view_divider);
        // Load padding
        Resources res = getResources();
        int itemSpacing = res.getDimensionPixelSize(R.dimen.item_spacing);
        int[] padding = {itemSpacing, itemSpacing, itemSpacing, itemSpacing};
        // Apply divider
        RecyclerView.ItemDecoration dividerItemDecoration = new DividerItemDecoration(
                dividerDrawable, padding);
        mReviewsRecyclerView.addItemDecoration(dividerItemDecoration);

        ReviewsAdapter reviewsAdapter = new ReviewsAdapter(new ArrayList<ServiceReview>(),
                getContext(), this);
        mReviewsRecyclerView.setAdapter(reviewsAdapter);
    }

    private void setupSpinner() {
        mServiceTypeSpinner = (AppCompatSpinner) mMainLayout.findViewById(
                R.id.review_service_type_spinner);
        mServiceTypeSpinner.setOnItemSelectedListener(this);

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getContext(),
                R.layout.support_simple_spinner_dropdown_item,
                getResources().getStringArray(R.array.service_types));

        mServiceTypeSpinner.setAdapter(arrayAdapter);
    }

    private void setupGoogleApiClient() {
        Activity activity = getActivity();
        if (activity != null) {
            try {
                ClientMainActivity clientMainActivity = (ClientMainActivity) activity;
                mGoogleApiClient = clientMainActivity.getGoogleApiClient();
            } catch (ClassCastException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadReviews() {
        loadReviews(null);
    }

    private void loadReviews(final ServiceType serviceType) {
        ReviewsAdapter adapter = (ReviewsAdapter) mReviewsRecyclerView
                .getAdapter();

        // Check if we are showing any reviews currently.
        if (adapter.getItemCount() == 0) {
            mNoReviewsTextView.setVisibility(View.GONE);
        } else {
            adapter.removeAllItems();
        }

        toggleProgressBar(true);

        mServiceType = serviceType;

        ReviewRequest reviewRequest = new ReviewRequest();

        // Get all reviews
//        ReviewData.get(getContext(), this);
        ReviewRequestMaker requestMaker = new ReviewRequestMaker(this);
        requestMaker.makeRequest(getContext(), reviewRequest);
    }

    private void toggleProgressBar(boolean toggle) {
        if (toggle) {
            mReviewsRecyclerView.setVisibility(View.GONE);
            mReviewsRetrieveProgressBar.setVisibility(View.VISIBLE);
        } else {
            mReviewsRetrieveProgressBar.setVisibility(View.GONE);
            mReviewsRecyclerView.setVisibility(View.VISIBLE);
        }
    }

    private void processServiceRetrieve(final ServiceStation[] serviceStations,
                                        boolean isForFilter) {
        final ReviewsAdapter adapter = (ReviewsAdapter) mReviewsRecyclerView.getAdapter();
        if (serviceStations == null || serviceStations.length == 0) {
            // TODO: handle error
        } else {
            if (!isForFilter) {
                final ServiceStation serviceStation = serviceStations[0];

                new LoadPlaceTask(mGoogleApiClient) {
                    @Override
                    protected void onPostExecute(Place[] places) {
                        if (places != null && places.length != 0) {
                            serviceStation.setLocation(places[0]);

                            ServiceDetailsDialog serviceDetailsDialog = ServiceDetailsDialog
                                    .getInstance(serviceStation, ReviewsFragment.this);
                            Utils.showDialogFragment(getFragmentManager(), serviceDetailsDialog,
                                    "service_details_dialog");
                        }
                    }
                }.execute(serviceStation.getPlaceId());
            } else {
                boolean reviewsFound = true;

                // Check for matching master ids
                HashSet<Long> matchingMasterIds = new HashSet<>();
                // Add all service id's matching the required service type to an array list.
                for (ServiceStation serviceStation : serviceStations) {
                    // If this service supports this service type add it to the collection.
                    if (serviceStation.getAvailableServices().contains(mServiceType)) {
                        matchingMasterIds.add(serviceStation.getID());
                    }
                }

                // Check if we found any matching services.
                if (matchingMasterIds.isEmpty()) {
                    reviewsFound = false;
                } else {
                    // Look for reviews referring to those services.
                    ArrayList<ServiceReview> filteredReviews = new ArrayList<>();
                    for (ServiceReview review : adapter.getItems()) {
                        long masterId = review.getServiceId();
                        // Add this review to the collection
                        if (matchingMasterIds.contains(masterId)) {
                            filteredReviews.add(review);
                        }
                    }

                    // Check if we found any matching services
                    if (filteredReviews.isEmpty()) {
                        reviewsFound = false;
                    } else {
                        // Clear previous results
                        adapter.removeAllItems();
                        // Add filtered results
                        adapter.addItems(filteredReviews);
                    }
                }

                toggleProgressBar(false);

                if (!reviewsFound) {
                    mNoReviewsTextView.setVisibility(View.VISIBLE);
                }
            }
        }
    }

    private void processReviewsRetrieve(final ServiceReview[] reviews,
                                        final ReviewsAdapter adapter) {
        // Clear all previous items
        adapter.removeAllItems();

        if (reviews.length == 0) {
            toggleProgressBar(false);
            mNoReviewsTextView.setVisibility(View.VISIBLE);
        } else {
            // Add current found items.
            adapter.addItems(reviews);
            if (mServiceType != null) {
                // Get all services - then filter the ones matching our service type
                ServiceStationDataRequest searchRequest = new ServiceStationDataRequest();

                Bundle extras = new Bundle();
                extras.putBoolean("filter_services", true);
                searchRequest.putExtras(extras);

                ServiceStationRequestMaker requestMaker = new ServiceStationRequestMaker(this);
                requestMaker.makeRequest(getContext(), searchRequest);
            } else {
                toggleProgressBar(false);
            }
        }
    }
}
