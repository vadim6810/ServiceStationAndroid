package il.co.tel_ran.carservice;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.hotmail.maximglukhov.chipview.ChipView;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

/**
 * Created by Max on 16/09/2016.
 */
public class SearchServiceTabFragment extends Fragment
    implements View.OnClickListener, ChipView.OnChipDeleteClickListener,
        ServerConnection.OnServicesRetrievedListener,
        ServiceSearchResultAdapter.ServiceSearchResultClickListener,
        ServiceDetailsDialog.ServiceDetailsDialogListener {

    private final static int[] SERVICE_CHECKBOX_IDS = {
            R.id.service_checkbox_car_wash,
            R.id.service_checkbox_tuning,
            R.id.service_checkbox_tyre_repair,
            R.id.service_checkbox_air_cond
    };

    private static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;

    private ScrollView searchFieldsLayout;
    private LinearLayout findServiceLayout;
    private Button expandFieldsButton;

    // Holds all programmatically created layouts to keep ChipView objects.
    private List<LinearLayout> chipsContainerLayouts = new ArrayList<>();
    // Holds all locations specified by the user (retrieved from Google Places API).
    // Used to query locations with required services.
    private List<Place> locations = new ArrayList<>();
    private Button searchLocationButton;

    private AppCompatCheckBox[] servicesCheckBoxes = new AppCompatCheckBox[SERVICE_CHECKBOX_IDS.length];

    private RecyclerView searchResultsRecyclerView;
    private ProgressBar resultsProgressBar;

    private View mainLayout;

    private ServiceDetailsDialog serviceDetailsDialog;

    private TextView mNoServicesTextView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (mainLayout == null) {
            mainLayout = inflater.inflate(R.layout.fragment_tab_search_service, container, false);

            searchFieldsLayout = (ScrollView) mainLayout.findViewById(R.id.find_service_expandable_layout);
            findServiceLayout = (LinearLayout) mainLayout.findViewById(R.id.find_service_layout);

            expandFieldsButton = (Button) mainLayout.findViewById(R.id.expand_search_fields_button);
            expandFieldsButton.setOnClickListener(this);
            AppCompatImageButton collapseFieldsButton = (AppCompatImageButton) mainLayout.findViewById(R.id.collapse_search_fields_button);
            collapseFieldsButton.setOnClickListener(this);

            Button findServicesButton = (Button) mainLayout.findViewById(R.id.find_services_button);
            findServicesButton.setOnClickListener(this);

            Button clearFieldsButton = (Button) mainLayout.findViewById(R.id.clear_search_fields_button);
            clearFieldsButton.setOnClickListener(this);

            searchLocationButton = (Button) mainLayout.findViewById(R.id.search_city_button);
            searchLocationButton.setOnClickListener(this);

            mNoServicesTextView = (TextView) mainLayout.findViewById(R.id.no_services_text_view);

            for (int i = 0; i < servicesCheckBoxes.length; i++) {
                servicesCheckBoxes[i] = (AppCompatCheckBox) mainLayout.findViewById(SERVICE_CHECKBOX_IDS[i]);
            }

            setupResultsRecyclerView();

            resultsProgressBar = (ProgressBar) mainLayout.findViewById(R.id.search_results_progress_bar);
        } else {

            if (((ClientMainActivity) getActivity()).isUserControlLayoutVisible()) {
                int bottom = findServiceLayout.getPaddingBottom();
                int configLayoutHeight = (int) getResources().getDimension(
                        R.dimen.snackbar_single_line_height);

                Configuration newConfig = getResources().getConfiguration();
                if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    Utils.setSpecificPadding(findServiceLayout, Utils.Padding.BOTTOM, bottom +
                            configLayoutHeight);
                } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT) {
                    Utils.setSpecificPadding(findServiceLayout, Utils.Padding.BOTTOM, bottom -
                            configLayoutHeight);
                }
            }
        }

        return mainLayout;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PLACE_AUTOCOMPLETE_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    Place place = PlaceAutocomplete.getPlace(getActivity(), data);
                    if (!locations.contains(place)) {
                        addPlaceChip(place);
                    }
                } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                    Status status = PlaceAutocomplete.getStatus(getActivity(), data);
                    // TODO: Handle error
                } else if (resultCode == RESULT_CANCELED) {

                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onClick(View view) {
        int EXPAND_COLLAPSE_DURATION = 350;
        ClientMainActivity containerActivity = (ClientMainActivity) getActivity();

        switch (view.getId()) {
            case R.id.expand_search_fields_button:
                Utils.collapseView(expandFieldsButton, EXPAND_COLLAPSE_DURATION);
                Utils.expandView(searchFieldsLayout, EXPAND_COLLAPSE_DURATION);
                break;
            case R.id.collapse_search_fields_button:
                Utils.collapseView(searchFieldsLayout, EXPAND_COLLAPSE_DURATION);
                Utils.expandView(expandFieldsButton, EXPAND_COLLAPSE_DURATION);
                break;
            case R.id.clear_search_fields_button:
                for (AppCompatCheckBox serviceCheckbox : servicesCheckBoxes) {
                    serviceCheckbox.setChecked(false);
                }

                for (LinearLayout layout : chipsContainerLayouts) {
                    findServiceLayout.removeView(layout);
                }
                chipsContainerLayouts.clear();
                locations.clear();

                break;
            case R.id.search_city_button:
                GoogleApiClient googleApiClient = containerActivity.getGoogleApiClient();

                if (googleApiClient != null && googleApiClient.isConnected()) {
                    try {
                        // Build a Place autocomplete activity to search locations.
                        Intent placeAutoCompleteIntent = Utils.buildPlaceAutoCompleteIntent(
                                containerActivity);
                        startActivityForResult(placeAutoCompleteIntent,
                                PLACE_AUTOCOMPLETE_REQUEST_CODE);
                    } catch (GooglePlayServicesRepairableException
                            | GooglePlayServicesNotAvailableException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case R.id.find_services_button:
                Utils.collapseView(searchFieldsLayout, EXPAND_COLLAPSE_DURATION);
                Utils.expandView(expandFieldsButton, EXPAND_COLLAPSE_DURATION);

                ServiceSearchQuery searchQuery = new ServiceSearchQuery(locations);
                for (int i = 0; i < SERVICE_CHECKBOX_IDS.length; i++) {
                    // This is a new created object with empty service types (all toggled off).
                    // Therefore only add the checked ones.
                    if (!servicesCheckBoxes[i].isChecked())
                        continue;

                    switch (SERVICE_CHECKBOX_IDS[i]) {
                        case R.id.service_checkbox_air_cond:
                            searchQuery.toggleServiceType(
                                    ServiceType.SERVICE_TYPE_AC_REPAIR_REFILL, true);
                            break;
                        case R.id.service_checkbox_car_wash:
                            searchQuery.toggleServiceType(
                                    ServiceType.SERVICE_TYPE_CAR_WASH, true);
                            break;
                        case R.id.service_checkbox_tuning:
                            searchQuery.toggleServiceType(
                                    ServiceType.SERVICE_TYPE_TUNING, true);
                            break;
                        case R.id.service_checkbox_tyre_repair:
                            searchQuery.toggleServiceType(
                                    ServiceType.SERVICE_TYPE_TYRE_REPAIR, true);
                            break;
                    }
                }

                ServerConnection connection = containerActivity.getServerConnection();
                if (connection != null) {
                    connection.findServices(searchQuery, containerActivity.getGoogleApiClient(),
                            this);
                }
                break;
        }
    }

    @Override
    public void onItemClick(DialogFragment dialogFragment, ServiceDetailsDialog.ITEM_TYPE itemType,
                            ServiceSearchResult result, View view) {
        switch (itemType) {
            case ITEM_FAB:
                // Open Google Maps with navigation directions.
                Uri gmmIntentUri = Uri.parse(
                        "google.navigation:q=" + result.getLocation().getAddress());
                Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                mapIntent.setPackage("com.google.android.apps.maps");
                startActivity(mapIntent);
                break;
            case ITEM_CONTACT_DETAILS:
                showContactDetailsDialog(result);
                break;
            case ITEM_DISMISS:
                dialogFragment.dismiss();
                break;
        }
    }

    @Override
    public void onServicesRetrievingStarted() {
        ServiceSearchResultAdapter adapter = (ServiceSearchResultAdapter) searchResultsRecyclerView
                .getAdapter();

        adapter.removeAllItems();

        mNoServicesTextView.setVisibility(View.GONE);

        resultsProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public void onServicesRetrieved(List<ServiceSearchResult> searchResults) {
        resultsProgressBar.setVisibility(View.GONE);

        if (searchResults != null && !searchResults.isEmpty()) {
            ServiceSearchResultAdapter adapter = (ServiceSearchResultAdapter) searchResultsRecyclerView
                    .getAdapter();

            adapter.addItems(searchResults);
        } else {
            mNoServicesTextView.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onClickSearchResult(View view) {
        // TODO: Add check for user signed in.

        // Find the position in the adapter for this view.
        int itemPos = searchResultsRecyclerView.getChildAdapterPosition(view);
        ServiceSearchResultAdapter adapter = (ServiceSearchResultAdapter) searchResultsRecyclerView
                .getAdapter();
        // Get the result object for this position.
        final ServiceSearchResult searchResult = adapter.getItem(itemPos);

        // Get the services text from the search result view (from the adapter).
        // This is done because the EnumSet<ServiceType> types were already parsed to string.
        // It is easier to simply extract it from the TextView.
        CharSequence servicesText = ((TextView)view
                .findViewById(R.id.result_available_services_text_view)).getText();

        showServiceDetailsDialog(searchResult, servicesText);
    }

    @Override
    public void onChipDelete(ChipView view) {
        for (LinearLayout layout : chipsContainerLayouts) {
            int index = layout.indexOfChild(view);
            if (index != -1) {
                Place place = (Place) view.getTag(R.id.tag_chip_place);
                locations.remove(place);

                layout.removeViewAt(index);
                if (layout.getChildCount() == 0) {
                    chipsContainerLayouts.remove(layout);
                    findServiceLayout.removeView(layout);
                } else {
                    rearrangePlaceChips(layout);
                }
                break;
            }
        }
    }

     /*
     Called when a chip is deleted, rearranges ChipView views to ensure no space is wasted.
     The idea is to move the first ChipView (if exists) of the next layout to the end
     of the current layout.

     This is a recursive function because it is possible for number of layouts to be stacked, so
     every next layout has to go through the same process.
     */

    private void rearrangePlaceChips(LinearLayout layout) {
        int layoutIndex = chipsContainerLayouts.indexOf(layout);
        // Check if this is NOT the last container (there's nothing left to rearrange).
        if (layoutIndex < (chipsContainerLayouts.size() - 1)) {
            int childrenWidth = Utils.measureChildrenWidth(layout);
            LinearLayout nextLayout = chipsContainerLayouts.get(layoutIndex + 1);
            ChipView nextChild = (ChipView) nextLayout.getChildAt(0);
            // Check if we got enough space to fit the next ChipView in the current layout.
            if (childrenWidth + nextChild.getMeasuredWidth() < layout.getMeasuredWidth()) {
                nextLayout.removeView(nextChild);
                layout.addView(nextChild);

                // If the next layout doesn't have any children left remove it.
                if (nextLayout.getChildCount() == 0) {
                    chipsContainerLayouts.remove(nextLayout);
                    findServiceLayout.removeView(nextLayout);
                } else {
                    // Call this function once again for the next layout.
                    rearrangePlaceChips(nextLayout);
                }
            }
        }
    }

    private void addPlaceChip(Place place) {
        locations.add(place);

        Context context = getContext();

        // Create a chip for the specified Place.
        ChipView locationChip = new ChipView(context);
        locationChip.setDeletable(true);
        locationChip.setText(place.getName());
        locationChip.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        locationChip.addOnChipDeleteClickListener(this);
        locationChip.setTag(R.id.tag_chip_place, place);

        int padding = (int) getResources().getDimension(R.dimen.item_spacing);
        // Use setPaddingRelative to support RTL layout for lower APIs (<17).
        ViewCompat.setPaddingRelative(locationChip, 0, padding, padding, 0);

        // lastContainer represents the last layout containing ChipView views.
        LinearLayout lastContainer = null;
        // Determine whether we need a new container layout:
        // If there are currently no containers.
        // If the width of the last container + width of ChipView is too much to fit on the screen.
        boolean isNewContainerRequired = false;
        if (chipsContainerLayouts.isEmpty()) {
            // Currently no containers, create a new one as the first container.
            isNewContainerRequired = true;
        } else {
            lastContainer = chipsContainerLayouts.get(chipsContainerLayouts.size() - 1);

            // Check if children's width is exceeding container layout width.
            // Make sure padding is NOT considered in the calculation.
            int measuredChildrenWidth = Utils.measureChildrenWidth(lastContainer);
            if (measuredChildrenWidth + locationChip.getMeasuredWidth()
                    > (lastContainer.getWidth() - padding)) {
                isNewContainerRequired = true;
            }
        }

        if (isNewContainerRequired) {
            // Create a new LinearLayout in horizontal orientation to layout the ChipView views.
            LinearLayout containerLayout = new LinearLayout(context);
            containerLayout.setOrientation(LinearLayout.HORIZONTAL);
            containerLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT));
            // Add the chip.
            containerLayout.addView(locationChip);
            // Add padding from the top for some spacing.
            ViewCompat.setPaddingRelative(containerLayout, 0, 0, padding, 0);

            // Add the new container layout to ArrayList.
            chipsContainerLayouts.add(containerLayout);

            // Check where to insert the new layout:
            // If there are no containers insert it right after the search button.
            // Otherwise insert it after the last container.
            int insertIndex = findServiceLayout.indexOfChild(searchLocationButton);
            if (lastContainer != null) {
                insertIndex = findServiceLayout.indexOfChild(lastContainer);
            }
            findServiceLayout.addView(containerLayout, ++insertIndex);
        } else {
            // If we don't need to create a new container layout add it to the current layout.
            lastContainer.addView(locationChip);
        }
    }

    private void setupResultsRecyclerView() {
        searchResultsRecyclerView = (RecyclerView) mainLayout.findViewById(
                R.id.search_results_recycler_view);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        searchResultsRecyclerView.setLayoutManager(layoutManager);
        ServiceSearchResultAdapter searchResultAdapter = new ServiceSearchResultAdapter(
                new ArrayList<ServiceSearchResult>(), getContext(), this);
        searchResultsRecyclerView.setAdapter(searchResultAdapter);
    }

    private void showServiceDetailsDialog(ServiceSearchResult searchResult,
                                          CharSequence servicesText) {
        serviceDetailsDialog = ServiceDetailsDialog.getInstance(servicesText, searchResult,
                this);
        showServiceDetailsDialog();
    }

    private void showServiceDetailsDialog() {
        if (serviceDetailsDialog != null) {
            FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
            Fragment previousDialog = getFragmentManager().findFragmentByTag("service_details_dialog");
            if (previousDialog != null) {
                fragmentTransaction.remove(previousDialog);
            }
            fragmentTransaction.addToBackStack(null);
            serviceDetailsDialog.show(fragmentTransaction, "service_details_dialog");
        }
    }

    private void showContactDetailsDialog(ServiceSearchResult searchResult) {
        ServiceContactDetailsDialog contactDetailsDialog = ServiceContactDetailsDialog.getInstance(
                searchResult.getPhonenumber(), searchResult.getEmail());
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        Fragment previousDialog = getFragmentManager().findFragmentByTag("contact_details_dialog");
        if (previousDialog != null) {
            fragmentTransaction.remove(previousDialog);
        }
        fragmentTransaction.addToBackStack(null);
        contactDetailsDialog.show(fragmentTransaction, "contact_details_dialog");
    }
}
