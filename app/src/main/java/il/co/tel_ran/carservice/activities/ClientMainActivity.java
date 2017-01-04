package il.co.tel_ran.carservice.activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.hotmail.maximglukhov.arrangedlayout.ArrangedLayout;
import com.hotmail.maximglukhov.chipview.ChipView;

import java.util.ArrayList;
import java.util.Collection;

import il.co.tel_ran.carservice.ClientUser;
import il.co.tel_ran.carservice.R;
import il.co.tel_ran.carservice.TenderRequest;
import il.co.tel_ran.carservice.connection.ServiceStationDataRequest;
import il.co.tel_ran.carservice.ServiceSubWorkType;
import il.co.tel_ran.carservice.ServiceType;
import il.co.tel_ran.carservice.ServiceWorkType;
import il.co.tel_ran.carservice.UserType;
import il.co.tel_ran.carservice.Utils;
import il.co.tel_ran.carservice.VehicleType;
import il.co.tel_ran.carservice.connection.ClientUserDataRequest;
import il.co.tel_ran.carservice.connection.ClientUserDataRequestMaker;
import il.co.tel_ran.carservice.connection.DataRequest;
import il.co.tel_ran.carservice.connection.DataResult;
import il.co.tel_ran.carservice.connection.RequestMaker;
import il.co.tel_ran.carservice.connection.ServerResponseError;
import il.co.tel_ran.carservice.fragments.RefreshingFragment;
import il.co.tel_ran.carservice.fragments.RequestServiceTabFragment;
import il.co.tel_ran.carservice.fragments.ReviewsFragment;
import il.co.tel_ran.carservice.fragments.ServicesListFragment;
import il.co.tel_ran.carservice.fragments.VehicleMakesFragment;
import il.co.tel_ran.carservice.fragments.WorkTypesFragment;

public class ClientMainActivity extends AppCompatActivity
        implements GoogleApiClient.OnConnectionFailedListener, SwipeRefreshLayout.OnRefreshListener,
        RefreshingFragment.RefreshingFragmentListener,
        NavigationView.OnNavigationItemSelectedListener,
        View.OnClickListener, CompoundButton.OnCheckedChangeListener,
        WorkTypesFragment.SelectWorkTypesDialogListener, ChipView.OnChipDeleteClickListener,
        VehicleMakesFragment.SelectVehicleMakesDialogListener, RequestMaker.OnDataRetrieveListener {

    public static final int REQUEST_CODE_POST_TENDER = 1;
    public static final int REQUEST_CODE_SIGN_IN = 2;
    public static final int REQUEST_CODE_PROFILE_CHANGED = 3;
    public static final int REQUEST_CODE_PLACE_AUTOCOMPLETE = 4;

    private static final int NAVIGATION_MENU_ITEM_HOME_INDEX = 0;
    private static final int NAVIGATION_MENU_ITEM_AUTO_SERVICE_INDEX = 1;
    private static final int NAVIGATION_MENU_ITEM_TYRE_REPAIR_INDEX = 2;
    private static final int NAVIGATION_MENU_ITEM_CAR_WASH_INDEX = 3;
    private static final int NAVIGATION_MENU_ITEM_TOWING_INDEX = 4;
    private static final int NAVIGATION_MENU_ITEM_REVIEWS_INDEX = 5;
    private static final int NAVIGATION_MENU_ITEM_TENDER_INDEX = 6;
    private static final int NAVIGATION_MENU_ITEM_SIGN_IN_INDEX = 7;
    private static final int NAVIGATION_MENU_ITEM_SIGN_UP_INDEX = 8;
    private static final int NAVIGATION_MENU_ITEM_PROFILE_INDEX = 9;
    private static final int NAVIGATION_MENU_ITEM_SIGN_OUT_INDEX = 10;
    private static final int NAVIGATION_MENU_ITEM_ABOUT_INDEX = 11;

    public static final String SHARED_PREFS_RECENT_SERVICES = "recent_services";

    private GoogleApiClient mGoogleApiClient;

    private View mUserAccountControlLayout;

    private Menu mMenu;

    private Toolbar mToolbar;
    private int mToolbarScrollFlags;

    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private TextView mDrawerClientNameTextView;
    private Menu mNavigationViewMenu;
    private int mSelectedDrawerItem;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private View mFilterLayout;
    private Button mExpandSearchFieldsButton;
    private View mSearchFieldsLayout;
    private Button mSearchCityButton;
    private RadioButton mPrivateVehicleTypeRadioButton;
    private RadioButton mMotorcycleVehicleTypeRadioButton;
    private RadioButton mBusVehicleTypeRadioButton;
    private RadioButton mTruckVehicleTypeRadioButton;
    private ArrangedLayout mServicesArrangedLayout;
    private ArrangedLayout mVehicleMakesArrangedLayout;

    private ClientUser mUser;
    private boolean mIsSignedIn;
    private boolean mIsLoadingUser;

    private RequestServiceTabFragment mTenderRequestsFragment;
    private ServicesListFragment mServicesListFragment;

    private ServiceStationDataRequest mServiceStationDataRequest;

    private ReviewsFragment mReviewsFragment;

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // Handle connection results.
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_menu, menu);

        mMenu = menu;

        MenuItem signOutItem = menu.getItem(0);
        signOutItem.getIcon().setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        signOutItem.setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_profile:
                startProfileActivity();
                break;
            case R.id.menu_item_about:
                break;
            case R.id.menu_item_refresh:
                // Show refreshing animation.
                mSwipeRefreshLayout.setRefreshing(true);
                // Start refreshing.
                onRefresh();
                break;
            case R.id.menu_item_signout:
                signOut();
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    public void onClickSignUp(View view) {
        startSignUpActivity();
    }

    public void onClickSignIn(View view) {
        startSignInActivity();
    }

    public GoogleApiClient getGoogleApiClient() {
        if (mGoogleApiClient != null && !mGoogleApiClient.isConnected())
            mGoogleApiClient.reconnect();

        return mGoogleApiClient;
    }

    public boolean isUserControlLayoutVisible() {
        return mUserAccountControlLayout.getVisibility() == View.VISIBLE;
    }

    @Override
    public void onRefresh() {
        RefreshingFragment refreshingFragment = null;
        switch (mSelectedDrawerItem) {
            case R.id.drawer_menu_item_auto_service:
            case R.id.drawer_menu_item_tyre_repair:
            case R.id.drawer_menu_item_car_wash:
            case R.id.drawer_menu_item_towing:
                refreshingFragment = mServicesListFragment;
                break;
            case R.id.drawer_menu_item_reviews:
                refreshingFragment = mReviewsFragment;
                break;
            default:
                // If we haven't defined refreshing for this drawer item cancel the refreshing.
                onRefreshEnd();
        }

        if (refreshingFragment != null) {
            refreshingFragment.onRefreshStart();
        } else {
            onRefreshEnd();
        }
    }

    /*
     * RefreshingFragment.RefreshingFragmentListener
     */

    @Override
    public void onRefreshEnd() {
        mSwipeRefreshLayout.setRefreshing(false);
    }

    /*
     * OnNavigationItemSelectedListener.onNavigationItemSelected
     */

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Close opened drawer.
        mDrawerLayout.closeDrawers();

        ServiceType serviceType = null;
        Fragment replaceFragment = null;
        ActionBar actionBar = getSupportActionBar();
        switch (item.getItemId()) {
            case R.id.drawer_menu_item_home:
                if (actionBar != null)
                    actionBar.setTitle(R.string.home_title);
                break;
            case R.id.drawer_menu_item_auto_service:
                serviceType = ServiceType.AUTO_SERVICE;

                if (actionBar != null)
                    actionBar.setTitle(R.string.auto_service_title);
                break;
            case R.id.drawer_menu_item_tyre_repair:
                serviceType = ServiceType.TYRE_REPAIR;

                if (actionBar != null)
                    actionBar.setTitle(R.string.tyre_repair_title);
                break;
            case R.id.drawer_menu_item_car_wash:
                serviceType = ServiceType.CAR_WASH;

                if (actionBar != null)
                    actionBar.setTitle(R.string.car_wash_title);
                break;
            case R.id.drawer_menu_item_towing:
                serviceType = ServiceType.TOWING;

                if (actionBar != null)
                    actionBar.setTitle(R.string.towing_title);
                break;
            case R.id.drawer_menu_item_reviews:
                if (actionBar != null)
                    actionBar.setTitle(R.string.reviews_title);

                if (mReviewsFragment == null) {
                    mReviewsFragment = new ReviewsFragment();
                    mReviewsFragment.setOnRefreshEndListener(this);
                }
                replaceFragment = mReviewsFragment;
                break;
            case R.id.drawer_menu_item_requests:
                // TODO: add check for user being signed in.
                if (mTenderRequestsFragment == null) {
                    mTenderRequestsFragment = new RequestServiceTabFragment();
                    mTenderRequestsFragment.setOnRefreshEndListener(this);
                }
                mTenderRequestsFragment.setUserData(mUser);
                replaceFragment = mTenderRequestsFragment;

                if (actionBar != null)
                    actionBar.setTitle(R.string.tender_requests_title);
                break;
            case R.id.drawer_menu_item_sign_in:
                // Show Sign In activity
                startSignInActivity();
                break;
            case R.id.drawer_menu_item_sign_up:
                // Show Sign Up activity
                startSignUpActivity();
                break;
            case R.id.drawer_menu_item_sign_out:
                // Handle user sign out
                signOut();
                break;
            case R.id.drawer_menu_item_profile:
                // Show Profile activity.
                startProfileActivity();

                // Return false because we don't want to display the item as selected item.
                // That's because we are launching a different activity rather than updating a fragment.
                return false;
            case R.id.drawer_menu_item_about:
                // TODO: show about dialog
                break;
            default:
                return false;
        }

        if (serviceType != null) {
            mServiceStationDataRequest.setServiceType(serviceType);

            mFilterLayout.setVisibility(View.VISIBLE);
            if (mServicesListFragment == null) {
                mServicesListFragment = new ServicesListFragment();

                Bundle args = new Bundle();
                args.putSerializable("service_type", mServiceStationDataRequest.getServiceType());
                args.putSerializable("vehicle_type", mServiceStationDataRequest.getVehicleType());
                mServicesListFragment.setArguments(args);

                mServicesListFragment.setOnRefreshEndListener(this);
            }

            mServicesListFragment.setServiceType(serviceType);
            mServicesListFragment.loadServices();

            replaceFragment = mServicesListFragment;

            if (mSelectedDrawerItem == NAVIGATION_MENU_ITEM_AUTO_SERVICE_INDEX ||
                    mSelectedDrawerItem == NAVIGATION_MENU_ITEM_TOWING_INDEX ||
                    mSelectedDrawerItem == NAVIGATION_MENU_ITEM_CAR_WASH_INDEX ||
                    mSelectedDrawerItem == NAVIGATION_MENU_ITEM_TYRE_REPAIR_INDEX) {
                // Set replace fragment to null to avoid reloading the same fragment.
                replaceFragment = null;
            }
        } else {
            mFilterLayout.setVisibility(View.GONE);
        }

        mSelectedDrawerItem = item.getItemId();

        if (replaceFragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            // Replace the current fragment with one corresponding to the selected item.
            fragmentTransaction.replace(R.id.frame, replaceFragment);
            fragmentTransaction.commit();
        } else {
        }
        return true;
    }

    /*
     * RequestMaker.OnDataRetrieveListener
     */

    @Override
    public void onDataRetrieveSuccess(DataRequest dataRequest, DataResult result) {
        if (result.getDataType() == DataResult.Type.CLIENT_USER) {
            ClientUser clientUserResults[] = (ClientUser[]) result.getData();

            if (clientUserResults != null && clientUserResults.length > 0) {
                // Only use the first result.
                ClientUser user = clientUserResults[0];

                mUser.setLogo(user.getLogo());
                mUser.setName(user.getName());
                mUser.setClientId(user.getClientId());
                mUser.setVehicles(user.getVehicles());
                mUser.setCreationDate(user.getCreationDate());
                mUser.setUpdateDate(user.getUpdateDate());

                onClientLoaded();
            }
        }
    }

    @Override
    public void onDataRetrieveFailed(DataRequest dataRequest, DataResult.Type resultType,
                                     ServerResponseError error, @Nullable String message) {
        mIsLoadingUser = false;
        // TODO: handle error
    }

    /*
     * View.OnClickListener
     */

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.search_city_button:
                if (mGoogleApiClient != null && mGoogleApiClient.isConnected()) {
                    try {
                        // Build a Place autocomplete activity to search locations.
                        Intent placeAutoCompleteIntent = Utils.buildPlaceAutoCompleteIntent(
                                ClientMainActivity.this, Utils.PLACE_FILTER_CITY);
                        startActivityForResult(placeAutoCompleteIntent,
                                REQUEST_CODE_PLACE_AUTOCOMPLETE);
                    } catch (GooglePlayServicesRepairableException
                            | GooglePlayServicesNotAvailableException e) {
                        e.printStackTrace();
                    }
                }
                break;
            case R.id.show_work_types_button:
                WorkTypesFragment workTypesFragment = WorkTypesFragment.getInstance(false,
                        mServiceStationDataRequest.getSubWorkTypes());
                workTypesFragment.setOnWorkTypesSelectedListener(this);
                Utils.showDialogFragment(getSupportFragmentManager(), workTypesFragment,
                        "work_type_fragment");
                break;
            case R.id.show_vehicle_makes_button:
                VehicleMakesFragment vehicleMakesFragment = VehicleMakesFragment.getInstance(
                        mServiceStationDataRequest.getCarMakes());
                vehicleMakesFragment.setOnVehicleMakesSelectedListener(this);
                Utils.showDialogFragment(getSupportFragmentManager(),
                        vehicleMakesFragment, "vehicle_make_fragment");
                break;
            case R.id.find_services_button:
                if (mServicesListFragment != null) {
                    toggleSearchFieldsLayout(false);
                    mServicesListFragment.setSearchRequest(mServiceStationDataRequest);
                    mServicesListFragment.onRefreshStart();
                }
                break;
            case R.id.collapse_search_fields_button:
                toggleSearchFieldsLayout(false);
                break;
            case R.id.clear_search_fields_button:
                if (mServiceStationDataRequest != null) {
                    mServiceStationDataRequest.setLocation(null);
                    mServiceStationDataRequest.setCarMakes(null);
                    mServiceStationDataRequest.setWorkTypes(null);
                    mServiceStationDataRequest.setSubWorkTypes(null);

                    updateFilterServicesLayout();
                }
                break;
        }
    }

    /*
     * CompoundButton.OnCheckedChangeListener
     */

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        // Only update when the (radio)button is checked - this callback is called for unchecking too
        // which gets triggered when checking another button in the radio group.
        if (isChecked) {
            VehicleType newVehicleType = null;
            switch (buttonView.getId()) {
                case R.id.car_type_private_radio_button:
                    newVehicleType = VehicleType.PRIVATE;
                    break;
                case R.id.car_type_motorcycle_radio_button:
                    newVehicleType = VehicleType.MOTORCYCLE;
                    break;
                case R.id.car_type_bus_radio_button:
                    newVehicleType = VehicleType.BUS;
                    break;
                case R.id.car_type_truck_radio_button:
                    newVehicleType = VehicleType.TRUCK;
                    break;
            }

            if (newVehicleType != null && mServiceStationDataRequest != null) {
                mServiceStationDataRequest.setVehicleType(newVehicleType);
                updateFilterServicesLayout();
            }
        }
    }

    /*
     * WorkTypesFragment.SelectWorkTypesDialogListener
     */

    @Override
    public void onWorkTypeSelected(ServiceWorkType[] workTypes, ServiceSubWorkType[] subWorkTypes) {
        if (mServiceStationDataRequest != null && workTypes.length > 0 && subWorkTypes.length > 0) {
            ArrayList<ServiceWorkType> workTypeArrayList = new ArrayList<>();
            ArrayList<ServiceSubWorkType> subWorkTypeArrayList = new ArrayList<>();

            for (ServiceWorkType workType : workTypes) {
                workTypeArrayList.add(workType);
                Log.d("CMA", "onWorkTypeSelected :: workType="+workType);
            }

            for (ServiceSubWorkType subWorkType : subWorkTypes) {
                subWorkTypeArrayList.add(subWorkType);
                Log.d("CMA", "onWorkTypeSelected :: subWorkTypes="+subWorkType);
            }

            mServiceStationDataRequest.setWorkTypes(workTypeArrayList);
            mServiceStationDataRequest.setSubWorkTypes(subWorkTypeArrayList);

            if (mServicesListFragment != null) {
                mServicesListFragment.setSearchRequest(mServiceStationDataRequest);
            }

            updateFilterServicesLayout();
        }
    }

    /*
     * VehicleMakesFragment.SelectVehicleMakesDialogListener
     */

    @Override
    public void onVehicleMakesSelected(ArrayList<String> vehicleMakes) {
        if (mServiceStationDataRequest != null) {
            mServiceStationDataRequest.setCarMakes(vehicleMakes);

            updateFilterServicesLayout();
        }
    }

    /*
     * ChipView.OnChipDeleteClickListener
     */

    @Override
    public void onChipDelete(ChipView chipView) {
        Object tag = chipView.getTag(R.id.tag_chip_sub_work_type);
        try {
            if (tag != null) {
                handleChipDelete(mServiceStationDataRequest.getSubWorkTypes(), (ServiceSubWorkType) tag);
            } else {
                tag = chipView.getTag(R.id.tag_chip_car_make);

                if (tag != null) {
                    handleChipDelete(mServiceStationDataRequest.getCarMakes(), (String) tag);
                }
            }

            updateFilterServicesLayout();
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
    }

    public void expandSearchFieldsLayout(View view) {
        toggleSearchFieldsLayout(true);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_main);

        mUserAccountControlLayout = findViewById(R.id.user_control_layout);

        mExpandSearchFieldsButton = (Button) findViewById(R.id.expand_search_fields_button);
        mFilterLayout = findViewById(R.id.search_fields_layout);

        mServicesArrangedLayout = (ArrangedLayout) findViewById(R.id.services_arranged_layout);

        mVehicleMakesArrangedLayout = (ArrangedLayout) findViewById(R.id.vehicle_makes_arranged_layout);

        setupSearchFieldsLayout();

        // find the retained fragment on activity restarts
        /*FragmentManager fm = getSupportFragmentManager();
        RetainedFragment dataFragment = (RetainedFragment) fm.findFragmentByTag(
                RetainedFragment.CLIENT_MAIN_ACTIVITY_RETAINED_FRAGMENT_TAG);

        // create the fragment and data the first time
        if (dataFragment == null) {
            // add the fragment
            dataFragment = new RetainedFragment();
            fm.beginTransaction().add(dataFragment,
                    RetainedFragment.CLIENT_MAIN_ACTIVITY_RETAINED_FRAGMENT_TAG).commit();

            // Initialize data
            setupServerConnection();
            createTabFragment(TAB_FRAGMENT_RECENT_SERVICES_INDEX);
            createTabFragment(TAB_FRAGMENT_SEARCH_SERVICES_INDEX);
            createTabFragment(TAB_FRAGMENT_REQUEST_SERVICES_INDEX);

            ClientActivityRetainedData data = new ClientActivityRetainedData(mGoogleApiClient,
                    mServerConnection, mRecentServicesTabFragment, mSearchServiceTabFragment,
                    mRequestServiceTabFragment);

            dataFragment.setData(data);
        } else {
            ClientActivityRetainedData data = (ClientActivityRetainedData) dataFragment.getData();
            if (data != null) {
                mServerConnection = data.getServerConnection();
                mRecentServicesTabFragment = data.getRecentServicesTabFragment();
                mSearchServiceTabFragment = data.getSearchServiceTabFragment();
                mRequestServiceTabFragment = data.getRequestServiceTabFragment();
            }
        }*/

        setupServiceSearchQuery();

        setupGoogleApiClient();

        setupActionBar();
        setupDrawerLayout();
        setupDrawerHeader();

        setupRefreshLayout();

        updateFilterServicesLayout();
    }


    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_POST_TENDER:
                if (resultCode == RESULT_OK) {
                    if (mTenderRequestsFragment != null) {
                        // Get data from PostTenderActivity
                        TenderRequest tenderRequest = (TenderRequest) data
                                .getSerializableExtra("tender_request");
                        // Update tender fragment
                        mTenderRequestsFragment.onTenderRequestUpdate(tenderRequest);
                    }
                }
                break;
            case REQUEST_CODE_SIGN_IN:
                if (resultCode == RESULT_OK) {
                    mUser = (ClientUser) data.getSerializableExtra("user");
                    onSignIn();
                }
                break;
            case REQUEST_CODE_PLACE_AUTOCOMPLETE:
                if (resultCode == RESULT_OK) {
                    Place place = PlaceAutocomplete.getPlace(ClientMainActivity.this, data);
                    if (mServiceStationDataRequest != null) {
                        mServiceStationDataRequest.setLocation(place);
                    }
                    updateFilterServicesLayout();
                } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                    Status status = PlaceAutocomplete.getStatus(ClientMainActivity.this, data);
                    // TODO: Handle error
                } else if (resultCode == RESULT_CANCELED) {

                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void setupSearchFieldsLayout() {
        mSearchFieldsLayout = findViewById(R.id.filter_services_layout);
        mSearchFieldsLayout.findViewById(R.id.collapse_search_fields_button)
                .setOnClickListener(this);

        mSearchCityButton = (Button) mSearchFieldsLayout.findViewById(R.id.search_city_button);
        mSearchCityButton.setOnClickListener(this);

        mSearchFieldsLayout.findViewById(R.id.show_work_types_button).setOnClickListener(this);

        mPrivateVehicleTypeRadioButton = (RadioButton) mSearchFieldsLayout.findViewById(
                R.id.car_type_private_radio_button);
        mPrivateVehicleTypeRadioButton.setOnCheckedChangeListener(this);
        mMotorcycleVehicleTypeRadioButton = (RadioButton) mSearchFieldsLayout.findViewById(
                R.id.car_type_motorcycle_radio_button);
        mMotorcycleVehicleTypeRadioButton.setOnCheckedChangeListener(this);
        mBusVehicleTypeRadioButton = (RadioButton) mSearchFieldsLayout.findViewById(
                R.id.car_type_bus_radio_button);
        mBusVehicleTypeRadioButton.setOnCheckedChangeListener(this);
        mTruckVehicleTypeRadioButton = (RadioButton) mSearchFieldsLayout.findViewById(
                R.id.car_type_truck_radio_button);
        mTruckVehicleTypeRadioButton.setOnCheckedChangeListener(this);

        mSearchFieldsLayout.findViewById(R.id.show_vehicle_makes_button).setOnClickListener(this);

        mSearchFieldsLayout.findViewById(R.id.find_services_button).setOnClickListener(this);
        mSearchFieldsLayout.findViewById(R.id.clear_search_fields_button).setOnClickListener(this);
    }

    private void setupServiceSearchQuery() {
        mServiceStationDataRequest = new ServiceStationDataRequest.Builder()
                .setVehicleType(VehicleType.PRIVATE)
                .setServiceType(ServiceType.AUTO_SERVICE)
                .build();
    }

    private void setupDrawerLayout() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.activity_client_drawer_layout);

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(
                ClientMainActivity.this, mDrawerLayout, mToolbar,
                R.string.open_drawer, R.string.close_drawer);

        mDrawerLayout.addDrawerListener(actionBarDrawerToggle);
        // Make navigation drawer icon sync animation with the navigation drawer.
        actionBarDrawerToggle.syncState();

        mNavigationView = (NavigationView) findViewById(R.id.provider_navigation_view);
        mNavigationView.setNavigationItemSelectedListener(this);

        mNavigationViewMenu = mNavigationView.getMenu();

        // Set the default page to show home
        this.onNavigationItemSelected(mNavigationViewMenu.getItem(NAVIGATION_MENU_ITEM_HOME_INDEX));
        mNavigationView.setCheckedItem(R.id.drawer_menu_item_home);

        toggleExclusiveDrawerMenuItems(false);
    }

    private void setupDrawerHeader() {
        View headerView = mNavigationView.getHeaderView(0);
        mDrawerClientNameTextView = (TextView) headerView.findViewById(R.id.client_name_text_view);
    }

    private void updateFilterServicesLayout() {
        if (mServiceStationDataRequest != null && mSearchFieldsLayout != null) {
            Place location = mServiceStationDataRequest.getLocation();
            if (location != null) {
                mSearchCityButton.setText(location.getName());
            } else {
                // Restore the text
                mSearchCityButton.setText(getString(R.string.search_location_button_hint));
            }

            switch (mServiceStationDataRequest.getVehicleType()) {
                case PRIVATE:
                    mPrivateVehicleTypeRadioButton.setChecked(true);
                    break;
                case MOTORCYCLE:
                    mMotorcycleVehicleTypeRadioButton.setChecked(true);
                    break;
                case BUS:
                    mBusVehicleTypeRadioButton.setChecked(true);
                    break;
                case TRUCK:
                    mTruckVehicleTypeRadioButton.setChecked(true);
                    break;
            }

            int itemSpacing = getResources().getDimensionPixelSize(R.dimen.item_spacing);

            // Remove current views (ChipView).
            mServicesArrangedLayout.removeAllViews();
            ArrayList<ServiceSubWorkType> subWorkTypes = mServiceStationDataRequest.getSubWorkTypes();
            if (subWorkTypes != null && !subWorkTypes.isEmpty()) {
                // Create a ChipView for every sub work type
                for (ServiceSubWorkType subWorkType : subWorkTypes) {
                    ChipView subWorkTypeChip = new ChipView(ClientMainActivity.this);
                    ViewCompat.setPaddingRelative(subWorkTypeChip, itemSpacing, 0, 0, itemSpacing);
                    subWorkTypeChip.setDeletable(true);
                    // TODO: make a method to get string for (sub)work types.
                    subWorkTypeChip.setText(subWorkType.toString());
                    subWorkTypeChip.addOnChipDeleteClickListener(this);
                    subWorkTypeChip.setTag(R.id.tag_chip_sub_work_type, subWorkType);

                    mServicesArrangedLayout.addView(subWorkTypeChip);
                }
            }

            // Remove current views (ChipView).
            mVehicleMakesArrangedLayout.removeAllViews();
            ArrayList<String> carMakes = mServiceStationDataRequest.getCarMakes();
            if (carMakes != null && !carMakes.isEmpty()) {
                // Create a ChipView for every vehicle make
                for (String carMake : carMakes) {
                    ChipView carMakeChip = new ChipView(ClientMainActivity.this);
                    ViewCompat.setPaddingRelative(carMakeChip, itemSpacing, 0, 0, itemSpacing);
                    carMakeChip.setDeletable(true);
                    carMakeChip.setText(carMake);
                    carMakeChip.addOnChipDeleteClickListener(this);
                    carMakeChip.setTag(R.id.tag_chip_car_make, carMake);

                    mVehicleMakesArrangedLayout.addView(carMakeChip);
                }
            }
        }
    }

    private void startSignUpActivity() {
        Intent intent = new Intent(ClientMainActivity.this, SignUpActivity.class);
        intent.putExtra("GAPI_CLIENT_CONNECTED", mGoogleApiClient.isConnected());
        startActivity(intent);
    }

    private void startSignInActivity() {
        Intent intent = new Intent(ClientMainActivity.this, SignInActivity.class);
        startActivityForResult(intent, REQUEST_CODE_SIGN_IN);
    }

    private void onSignIn() {
        mIsSignedIn = true;

        // Set sign out action enabled
        mMenu.getItem(0).setVisible(true);

        mUserAccountControlLayout.setVisibility(View.GONE);

        loadClient();

        toggleExclusiveDrawerMenuItems(true);
    }

    private void signOut() {
        onSignOut();
        toggleExclusiveDrawerMenuItems(false);
    }

    private void onSignOut() {
        mIsSignedIn = false;

        // Set sign out action enabled
        mMenu.getItem(0).setVisible(false);

        mUserAccountControlLayout.setVisibility(View.VISIBLE);

        mUser = null;

        // TODO: remove recent services, clear request service.
        toggleExclusiveDrawerMenuItems(false);
    }

    private void loadClient() {
        mIsLoadingUser = true;

        ClientUserDataRequest request = new ClientUserDataRequest(mUser.getClientId());
        new ClientUserDataRequestMaker(this).makeRequest(ClientMainActivity.this, request);
    }

    private void onClientLoaded() {
        mIsLoadingUser = false;
        toggleExclusiveDrawerMenuItems(true);

        mDrawerClientNameTextView.setText(mUser.getName());
    }

    private void setupRefreshLayout() {
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(this);
    }

    private void setupGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();
    }

    private void setupActionBar() {
        mToolbar = (Toolbar) findViewById(R.id.actionBar);
        mToolbarScrollFlags = ((AppBarLayout.LayoutParams) mToolbar.getLayoutParams())
                .getScrollFlags();

        setSupportActionBar(mToolbar);
    }

    private void toggleSearchFieldsLayout(boolean toggle) {
        int EXPAND_COLLAPSE_DURATION = 350;
        if (toggle) {
            Utils.collapseView(mExpandSearchFieldsButton, EXPAND_COLLAPSE_DURATION);
            Utils.expandView(mSearchFieldsLayout, EXPAND_COLLAPSE_DURATION);
        } else {
            Utils.collapseView(mSearchFieldsLayout, EXPAND_COLLAPSE_DURATION);
            Utils.expandView(mExpandSearchFieldsButton, EXPAND_COLLAPSE_DURATION);
        }
    }

    /*
     * SwipeRefreshLayout.OnRefreshListener
     */

    private void startProfileActivity() {
        if (isUserSignedIn()) {
            if (mIsLoadingUser) {
                Toast.makeText(ClientMainActivity.this, R.string.loading_message,
                        Toast.LENGTH_LONG).show();
                return;
            }
            Intent intent = new Intent(ClientMainActivity.this, ProfileActivity.class);
            // Since we are in ClientMainActivity the user type is a client.
            intent.putExtra("user_type", UserType.CLIENT);
            // Pass user object
            intent.putExtra("user", mUser);
            startActivityForResult(intent, REQUEST_CODE_PROFILE_CHANGED);
        }
    }

    private boolean isUserSignedIn() {
        return mIsSignedIn && mUser != null;
    }

    /**
     * Toggles menu items which are available for signed-in users only.
     */
    private void toggleExclusiveDrawerMenuItems(boolean toggle) {
        mNavigationViewMenu.getItem(NAVIGATION_MENU_ITEM_TENDER_INDEX).setVisible(toggle);
        mNavigationViewMenu.getItem(NAVIGATION_MENU_ITEM_SIGN_IN_INDEX).setVisible(!toggle);
        mNavigationViewMenu.getItem(NAVIGATION_MENU_ITEM_SIGN_UP_INDEX).setVisible(!toggle);
        mNavigationViewMenu.getItem(NAVIGATION_MENU_ITEM_SIGN_OUT_INDEX).setVisible(toggle);
        mNavigationViewMenu.getItem(NAVIGATION_MENU_ITEM_PROFILE_INDEX).setVisible(toggle);
    }

    private <T> void handleChipDelete(Collection<T> collection, T removal) {
        if (collection != null && !collection.isEmpty()) {
            collection.remove(removal);
        }
    }
}
