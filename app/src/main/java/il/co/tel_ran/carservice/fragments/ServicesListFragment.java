package il.co.tel_ran.carservice.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Place;

import java.util.ArrayList;

import il.co.tel_ran.carservice.LoadPlaceTask;
import il.co.tel_ran.carservice.R;
import il.co.tel_ran.carservice.connection.ServiceStationDataRequest;
import il.co.tel_ran.carservice.ServiceStation;
import il.co.tel_ran.carservice.ServiceType;
import il.co.tel_ran.carservice.Utils;
import il.co.tel_ran.carservice.VehicleType;
import il.co.tel_ran.carservice.activities.ClientMainActivity;
import il.co.tel_ran.carservice.adapters.ServiceSearchResultAdapter;
import il.co.tel_ran.carservice.connection.DataRequest;
import il.co.tel_ran.carservice.connection.DataResult;
import il.co.tel_ran.carservice.connection.RequestMaker;
import il.co.tel_ran.carservice.connection.ServerResponseError;
import il.co.tel_ran.carservice.connection.ServiceStationRequestMaker;
import il.co.tel_ran.carservice.dialogs.ServiceDetailsDialog;

/**
 * Created by Max on 24/11/2016.
 */

public class ServicesListFragment extends RefreshingFragment
    implements ServiceSearchResultAdapter.ServiceSearchResultClickListener,
        GoogleApiClient.OnConnectionFailedListener, RequestMaker.OnDataRetrieveListener {

    private View mMainLayout;

    private ProgressBar mServicesRetrieveProgressBar;
    private TextView mNoServicesTextView;

    private RecyclerView mServicesRecyclerView;

    private ServiceStationDataRequest mServiceStationDataRequest;

    private GoogleApiClient mGoogleApiClient;

    private boolean mIsLoadingServices;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        Bundle extras = getArguments();
        if (extras != null && !extras.isEmpty()) {
            ServiceType serviceType = (ServiceType) extras.get("service_type");
            VehicleType vehicleType = (VehicleType) extras.get("vehicle_type");

            mServiceStationDataRequest = new ServiceStationDataRequest.Builder()
                    .setServiceType(serviceType)
                    .setVehicleType(vehicleType)
                    .build();
        }

        setupGoogleApiClient();
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

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mMainLayout == null) {
            mMainLayout = inflater.inflate(R.layout.fragment_services_list, container, false);

            mServicesRetrieveProgressBar = (ProgressBar) mMainLayout.findViewById(R.id.services_retrieve_progress_bar);
            mNoServicesTextView = (TextView) mMainLayout.findViewById(R.id.no_services_text_view);

            setupRecyclerView();
        }

        loadServices();

        return mMainLayout;
    }

    private void setupRecyclerView() {
        mServicesRecyclerView = (RecyclerView) mMainLayout.findViewById(R.id.services_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mServicesRecyclerView.setLayoutManager(layoutManager);

        ServiceSearchResultAdapter tenderRequestsAdapter = new ServiceSearchResultAdapter(
                new ArrayList<ServiceStation>(), getContext(), this, false);
        mServicesRecyclerView.setAdapter(tenderRequestsAdapter);
    }

    public void setServiceType(ServiceType serviceType) {
        setServiceType(serviceType, true);
    }

    public void setServiceType(ServiceType serviceType, boolean reload) {
        if (mServiceStationDataRequest != null && serviceType != null) {
            mServiceStationDataRequest.setServiceType(serviceType);
            if (reload) {
                onRefreshStart();
            }
        }
    }

    public void setSearchRequest(ServiceStationDataRequest searchRequest) {
        if (searchRequest != null) {
            if (searchRequest.getVehicleType() != null && searchRequest.getServiceType() != null) {
                mServiceStationDataRequest = searchRequest;
            }
        }
    }

    public void loadServices() {
        loadServices(mServiceStationDataRequest);
    }

    public void loadServices(ServiceStationDataRequest searchQuery) {
        if (searchQuery != null && mServicesRecyclerView != null) {
            ServiceSearchResultAdapter adapter = (ServiceSearchResultAdapter) mServicesRecyclerView
                    .getAdapter();

            // Check if we are showing any results currently.
            if (adapter.getItemCount() == 0) {
                mNoServicesTextView.setVisibility(View.GONE);
            } else {
                adapter.removeAllItems();
            }

            toggleProgressBar(true);

            mIsLoadingServices = true;
            ServiceStationRequestMaker requestMaker = new ServiceStationRequestMaker(this);
            requestMaker.makeRequest(getContext(), searchQuery);
        }
    }

    /*
     * RequestMaker.OnDataRetrieveListener
     */

    @Override
    public void onDataRetrieveSuccess(DataRequest dataRequest, DataResult result) {
        if (result.getDataType() == DataResult.Type.SERVICE_STATION) {
            final ServiceStation[] serviceStations = (ServiceStation[]) result.getData();

            String[] placeIds = new String[serviceStations.length];
            for (int i = 0; i < serviceStations.length; i++) {
                placeIds[i] = serviceStations[i].getPlaceId();
            }

            toggleProgressBar(false);

            final ServiceSearchResultAdapter adapter =
                    (ServiceSearchResultAdapter) mServicesRecyclerView.getAdapter();
            adapter.removeAllItems();
            if (serviceStations.length != 0) {
                adapter.addItems(serviceStations);
            } else {
                mNoServicesTextView.setVisibility(View.VISIBLE);
            }

            new LoadPlaceTask(mGoogleApiClient) {
                @Override
                protected void onPostExecute(Place[] places) {
                    if (places != null && places.length >= 0) {

                        for (int j = 0; j < places.length; j++) {
                            // Retrieve place from place id
                            serviceStations[j].setLocation(places[j]);
                            // Check if formatted address is correct
                            if (serviceStations[j].getCityName() == null) {
                                serviceStations[j].setCityName(
                                        Utils.parseCityNameFromAddress(places[j].getAddress()));
                            }
                        }
                    }

                    adapter.notifyDataSetChanged();

                    mIsLoadingServices = false;
                }
            }.execute(placeIds);

            onRefreshEnd();
        }
    }

    @Override
    public void onDataRetrieveFailed(DataRequest dataRequest, DataResult.Type resultType,
                                     ServerResponseError error, @Nullable String message) {
        if (resultType == DataResult.Type.SERVICE_STATION) {
            mIsLoadingServices = false;

            final ServiceSearchResultAdapter adapter =
                    (ServiceSearchResultAdapter) mServicesRecyclerView.getAdapter();
            adapter.removeAllItems();

            toggleProgressBar(false);

            mNoServicesTextView.setVisibility(View.VISIBLE);
        }
    // TODO: handle errors

        onRefreshEnd();
    }

    /*
     * RefreshingFragment
     */

    @Override
    public void onRefreshStart() {
        super.onRefreshStart();

        loadServices();
    }

    /*
     * ServiceSearchResultAdapter.ServiceSearchResultClickListener
     */

    @Override
    public void onClickSearchResult(View view) {
        if (mIsLoadingServices) {
            Toast.makeText(getContext(), R.string.data_still_loading_message, Toast.LENGTH_SHORT)
                    .show();
        } else {
            // TODO: Add check for user signed in.

            // Find the position in the adapter for this view.
            int itemPos = mServicesRecyclerView.getChildAdapterPosition(view);
            ServiceSearchResultAdapter adapter = (ServiceSearchResultAdapter) mServicesRecyclerView
                    .getAdapter();
            // Get the result object for this position.
            final ServiceStation serviceStation = adapter.getItem(itemPos);

            ServiceDetailsDialog serviceDetailsDialog = ServiceDetailsDialog.getInstance(
                    serviceStation, ServicesListFragment.this);
            Utils.showDialogFragment(getFragmentManager(), serviceDetailsDialog,
                    "service_details_dialog");
        }
    }

    @Override
    public void onClickDeleteResult(View view) {
        // Irrelevant for this fragment.
    }

    /*
     * GoogleApiClient.OnConnectionFailedListener
     */

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // TODO: handle errors
    }

    private void toggleProgressBar(boolean toggle) {
        mServicesRetrieveProgressBar.setVisibility(toggle ? View.VISIBLE : View.GONE);
    }
}
