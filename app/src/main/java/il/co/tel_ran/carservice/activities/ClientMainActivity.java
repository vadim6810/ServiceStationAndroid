package il.co.tel_ran.carservice.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Places;

import il.co.tel_ran.carservice.ClientActivityRetainedData;
import il.co.tel_ran.carservice.ClientUser;
import il.co.tel_ran.carservice.R;
import il.co.tel_ran.carservice.ServerConnection;
import il.co.tel_ran.carservice.TenderRequest;
import il.co.tel_ran.carservice.UserType;
import il.co.tel_ran.carservice.Utils;
import il.co.tel_ran.carservice.VehicleData;
import il.co.tel_ran.carservice.fragments.RecentServicesTabFragment;
import il.co.tel_ran.carservice.fragments.RequestServiceTabFragment;
import il.co.tel_ran.carservice.fragments.RetainedFragment;
import il.co.tel_ran.carservice.fragments.SearchServiceTabFragment;

public class ClientMainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    public static final int REQUEST_CODE_POST_TENDER = 1;

    public static final String SHARED_PREFS_RECENT_SERVICES = "recent_services";

    private GoogleApiClient mGoogleApiClient;

    private ServerConnection mServerConnection;

    private View mUserAccountControlLayout;

    private static final int TAB_FRAGMENT_RECENT_SERVICES_INDEX = 0;
    private RecentServicesTabFragment mRecentServicesTabFragment;
    private static final int TAB_FRAGMENT_SEARCH_SERVICES_INDEX = 1;
    private SearchServiceTabFragment mSearchServiceTabFragment;
    private static final int TAB_FRAGMENT_REQUEST_SERVICES_INDEX = 2;
    private RequestServiceTabFragment mRequestServiceTabFragment;

    private Toolbar mToolbar;
    private int mToolbarScrollFlags;

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // Handle connection results.
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.client_main_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_profile:
                // TODO: Add check for user signed-in
                Intent intent = new Intent(ClientMainActivity.this, ProfileActivity.class);
                // Since we are in ClientMainActivity the user type is a client.
                intent.putExtra("user_type", UserType.USER_CLIENT);
                startActivity(intent);
                break;
            case R.id.menu_item_about:
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    public void showSignUpForm(View view) {
        Intent intent = new Intent(ClientMainActivity.this, SignUpActivity.class);
        intent.putExtra("GAPI_CLIENT_CONNECTED",mGoogleApiClient.isConnected());
        startActivity(intent);
    }

    public void showSignInForm(View view) {
        Intent intent = new Intent(ClientMainActivity.this, SignInActivity.class);
        startActivity(intent);
    }

    public GoogleApiClient getGoogleApiClient() {
        return mGoogleApiClient;
    }

    public ServerConnection getServerConnection() {
        return mServerConnection;
    }

    public boolean isUserControlLayoutVisible() {
        return mUserAccountControlLayout.getVisibility() == View.VISIBLE;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_main);

        mUserAccountControlLayout = findViewById(R.id.user_control_layout);

        // find the retained fragment on activity restarts
        FragmentManager fm = getSupportFragmentManager();
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
        }

        setupGoogleApiClient();

        setupActionBar();
        setupTabLayout();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mServerConnection != null) {
            mServerConnection.cancelAllTasks();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_POST_TENDER:
                if (resultCode == RESULT_OK) {
                    if (mRequestServiceTabFragment != null) {
                        // Get data from PostTenderActivity
                        TenderRequest tenderRequest = (TenderRequest) data
                                .getSerializableExtra("tender_request");
                        mRequestServiceTabFragment.onTenderRequestUpdate(tenderRequest);
                    }
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void setupGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();
    }

    private void setupServerConnection() {
        mServerConnection = new ServerConnection();
    }

    private void setupActionBar() {
        mToolbar = (Toolbar) findViewById(R.id.actionBar);
        mToolbarScrollFlags = ((AppBarLayout.LayoutParams) mToolbar.getLayoutParams())
                .getScrollFlags();

        setSupportActionBar(mToolbar);
    }

    private void setupTabLayout() {
        ViewPager tabsViewPager = (ViewPager) findViewById(R.id.tabs_viewpager);
        tabsViewPager.setAdapter(new TabsPagerAdapter(getSupportFragmentManager()));

        if (mUserAccountControlLayout.getVisibility() == View.VISIBLE) {
            int paddingBottom = tabsViewPager.getPaddingBottom();

            mUserAccountControlLayout.measure(View.MeasureSpec.UNSPECIFIED,
                    View.MeasureSpec.UNSPECIFIED);

            // Adding this much padding reduces the height of the ViewPager so there is
            // no interference between the ViewPager and the Sign In/Up layout.
            int addedPadding = (int) (mUserAccountControlLayout.getMeasuredHeight()
                    + getResources().getDimension(R.dimen.activity_horizontal_margin));

            Utils.setSpecificPadding(tabsViewPager, Utils.Padding.BOTTOM,
                    paddingBottom + addedPadding);
        }

        final TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(tabsViewPager);
        tabLayout.addOnTabSelectedListener(
                new TabLayout.ViewPagerOnTabSelectedListener(tabsViewPager) {
                    @Override
                    public void onTabSelected(TabLayout.Tab tab) {
                        super.onTabSelected(tab);

                        AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) mToolbar
                                .getLayoutParams();
                        // Check if the tab is "Request ServiceStation" which requires toolbar scrolling to be disabled under certain conditions.
                        if (tab.getPosition() == 2) {
                            // Clear scroll flags - makes scrolling disabled.
                            params.setScrollFlags(0);
                        } else {
                            // Restore original scroll flags
                            params.setScrollFlags(mToolbarScrollFlags);

                            // Recent services
                            if (tab.getPosition() == 0) {
                                mRecentServicesTabFragment.reloadRecentServices();
                            }
                        }

                        // Make sure our layout params are taken into consideration
                        mToolbar.requestLayout();
                    }
        });
    }

    private class TabsPagerAdapter extends FragmentStatePagerAdapter {

        private static final int TAB_COUNT = 3;

        private String[] tabTitles = new String[TAB_COUNT];

        public TabsPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);

            tabTitles[0] = getString(R.string.tab_title_recent_services);
            tabTitles[1] = getString(R.string.tab_title_find_services);
            tabTitles[2] = getString(R.string.tab_title_request_service);
        }

        @Override
        public Fragment getItem(int position) {
            createTabFragment(position);
            switch (position) {
                case 0:
                    return mRecentServicesTabFragment;
                case 1:
                    return mSearchServiceTabFragment;
                case 2:
                    return mRequestServiceTabFragment;
            }
            return null;
        }

        @Override
        public int getCount() {
            return TAB_COUNT;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabTitles[position];
        }
    }

    private boolean createTabFragment(int tabIndex) {
        switch (tabIndex) {
            case TAB_FRAGMENT_RECENT_SERVICES_INDEX:
                if (mRecentServicesTabFragment == null) {
                    mRecentServicesTabFragment = new RecentServicesTabFragment();
                    return true;
                }
            case TAB_FRAGMENT_SEARCH_SERVICES_INDEX:
                if (mSearchServiceTabFragment == null) {
                    mSearchServiceTabFragment = new SearchServiceTabFragment();
                    return true;
                }
            case TAB_FRAGMENT_REQUEST_SERVICES_INDEX:
                if (mRequestServiceTabFragment == null) {
                    mRequestServiceTabFragment = new RequestServiceTabFragment();
                    // TODO: pass user data once user logs-in
                    ClientUser mockUser = new ClientUser();
                    mockUser.setName("Maxim Glukhov");
                    mockUser.setEmail("maximglukhov@hotmail.com");

                    VehicleData mockVehicle = new VehicleData();
                    mockVehicle.setVehicleMake("Audi");
                    mockVehicle.setVehicleModel("R8 Coupe");
                    mockVehicle.setVehicleYear(2016);
                    mockVehicle.setVehicleModifications("5.2 V10 FSI (560 Hp) GT");
                    mockUser.setVehicleData(mockVehicle);

                    mRequestServiceTabFragment.setUserData(mockUser);
                    return true;
                }
        }

        return false;
    }
}
