package il.co.tel_ran.carservice.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import il.co.tel_ran.carservice.R;
import il.co.tel_ran.carservice.ServerConnection;
import il.co.tel_ran.carservice.ServiceSearchQuery;
import il.co.tel_ran.carservice.ServiceSearchResult;
import il.co.tel_ran.carservice.ServiceStation;
import il.co.tel_ran.carservice.Utils;
import il.co.tel_ran.carservice.activities.ClientMainActivity;
import il.co.tel_ran.carservice.adapters.ServiceSearchResultAdapter;
import il.co.tel_ran.carservice.dialogs.ServiceDetailsDialog;

/**
 * Created by Max on 16/09/2016.
 */
public class RecentServicesTabFragment extends Fragment
        implements ServerConnection.OnServicesRetrievedListener, ServiceSearchResultAdapter.ServiceSearchResultClickListener {

    private Set<Long> mServiceIds = new HashSet<>();

    private View mMainLayout;
    private TextView mNoServicesTextView;
    private RecyclerView mSearchResultsRecyclerView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        Context context = getContext();
        SharedPreferences sharedPreferences = context
                .getSharedPreferences(ClientMainActivity.SHARED_PREFS_RECENT_SERVICES,
                        Context.MODE_PRIVATE);

        Set<String> currentServices = sharedPreferences
                .getStringSet("service_set", new HashSet<String>());
        for (String serviceIdString : currentServices) {
            mServiceIds.add(Long.parseLong(serviceIdString));
        }

        loadServices();

        Log.d("RSTF", "onCreate :: called");
        Log.d("RSTF", "onCreate :: currentServices: " + currentServices.toString());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mMainLayout == null) {
            mMainLayout = inflater.inflate(R.layout.fragment_tab_recent_services, container, false);

            mNoServicesTextView = (TextView) mMainLayout.findViewById(R.id.no_services_text_view);

            setupRecyclerView();
        }

        reloadRecentServices();

        return mMainLayout;
    }

    public void reloadRecentServices() {
        Log.d("RSTF", "reloadRecentServices :: called");
        Context context = getContext();
        SharedPreferences sharedPreferences = context
                .getSharedPreferences(ClientMainActivity.SHARED_PREFS_RECENT_SERVICES,
                        Context.MODE_PRIVATE);

        // Get current services.
        Set<String> currentServices = sharedPreferences
                .getStringSet("service_set", new HashSet<String>());

        Log.d("RSTF", "reloadRecentServices :: currentServices: " + currentServices.toString());

        // Different sizes, reload anyway.
        if (currentServices.size() != mServiceIds.size()) {
            Log.d("RSTF", "reloadRecentServices :: different sizes");

            updateServiceIds(currentServices);
            loadServices();
            return;
        }

        for (long serviceId : mServiceIds) {
            // Id's do not match, reload services.
            if (!currentServices.contains(Long.toString(serviceId))) {
                Log.d("RSTF", "reloadRecentServices :: found new id");
                updateServiceIds(currentServices);
                loadServices();
                return;
            }
        }
    }

    @Override
    public void onServicesRetrievingStarted() {
        Log.d("RSTF", "onServicesRetrievingStarted :: called");
    }

    @Override
    public void onServicesRetrieved(ServiceSearchResult searchResult) {
        Log.d("RSTF", "onServicesRetrieved :: called");
        ServiceSearchResultAdapter adapter = (ServiceSearchResultAdapter) mSearchResultsRecyclerView
                .getAdapter();
        // Clear previous results
        adapter.removeAllItems();

        boolean anyServices = false;
        for (ServiceStation service : searchResult.getSerivces()) {
            // Check if this result is one of user's recent services
            if (mServiceIds.contains(service.getID())) {
                anyServices = true;
                // Add to recycler view
                adapter.addItem(service);
            }
        }

        if (!anyServices) {
            mNoServicesTextView.setVisibility(View.VISIBLE);
        } else {
            mNoServicesTextView.setVisibility(View.GONE);
        }
    }

    @Override
    public void onClickSearchResult(View view) {
        // Find the position in the adapter for this view.
        int itemPos = mSearchResultsRecyclerView.getChildAdapterPosition(view);
        ServiceSearchResultAdapter adapter = (ServiceSearchResultAdapter) mSearchResultsRecyclerView
                .getAdapter();
        // Get the result object for this position.
        final ServiceStation service = adapter.getItem(itemPos);

        // Get the services text from the search result view (from the adapter).
        // This is done because the EnumSet<ServiceType> types were already parsed to string.
        // It is easier to simply extract it from the TextView.
        CharSequence servicesText = ((TextView)view
                .findViewById(R.id.result_available_services_text_view)).getText();

        showServiceDetailsDialog(service, servicesText);
    }

    @Override
    public void onClickDeleteResult(View view) {
        // Find the position in the adapter for this view.
        int itemPos = mSearchResultsRecyclerView.getChildAdapterPosition((View) view.getParent());
        ServiceSearchResultAdapter adapter = (ServiceSearchResultAdapter) mSearchResultsRecyclerView
                .getAdapter();
        // Get the result object for this position.
        final ServiceStation service = adapter.getItem(itemPos);

        // Remove this service from saved recent services set.
        mServiceIds.remove(service.getID());
        saveServices();

        // Remove the view from the RecyclerView
        adapter.removeItem(itemPos);
    }

    private void loadServices() {
        Activity containerActivity = getActivity();
        if (containerActivity != null) {
            try {
                ClientMainActivity clientMainActivity = (ClientMainActivity) containerActivity;
                ServerConnection connection = clientMainActivity.getServerConnection();
                // Retrieve services from server
                if (connection != null) {
                    connection.findServices(new ServiceSearchQuery(),
                            clientMainActivity.getGoogleApiClient(),
                            this);
                }
            } catch (ClassCastException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveServices() {
        Context context = getContext();
        SharedPreferences sharedPreferences = context
                .getSharedPreferences(ClientMainActivity.SHARED_PREFS_RECENT_SERVICES,
                        Context.MODE_PRIVATE);

        // Get current services.
        Set<String> currentServices = sharedPreferences
                .getStringSet("service_set", new HashSet<String>());

        // Clear current services
        currentServices.clear();

        // Save new ones
        for (long serviceId : mServiceIds) {
            currentServices.add(String.valueOf(serviceId));
        }

        sharedPreferences.edit().putStringSet("service_set", currentServices).apply();
    }

    private void updateServiceIds(Set<String> serviceIdsStringSet) {
        mServiceIds.clear();

        for (String IdStr : serviceIdsStringSet) {
            mServiceIds.add(Long.parseLong(IdStr));
        }
    }

    private void showServiceDetailsDialog(ServiceStation service,
                                          CharSequence servicesText) {
        ServiceDetailsDialog serviceDetailsDialog = ServiceDetailsDialog.getInstance(servicesText, service,
                this);
        Utils.showDialogFragment(getFragmentManager(), serviceDetailsDialog,
                "service_details_dialog");
    }

    private void setupRecyclerView() {
        mSearchResultsRecyclerView = (RecyclerView) mMainLayout.findViewById(
                R.id.services_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        mSearchResultsRecyclerView.setLayoutManager(layoutManager);
        ServiceSearchResultAdapter searchResultAdapter = new ServiceSearchResultAdapter(
                new ArrayList<ServiceStation>(), getContext(), this, true);
        mSearchResultsRecyclerView.setAdapter(searchResultAdapter);

    }
}
