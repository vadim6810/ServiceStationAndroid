package il.co.tel_ran.carservice.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import il.co.tel_ran.carservice.R;
import il.co.tel_ran.carservice.fragments.RecentServicesTabFragment;
import il.co.tel_ran.carservice.fragments.RequestServiceTabFragment;
import il.co.tel_ran.carservice.fragments.RetainedFragment;
import il.co.tel_ran.carservice.fragments.SearchServiceTabFragment;
import il.co.tel_ran.carservice.ServerConnection;
import il.co.tel_ran.carservice.Utils;

public class ClientMainActivity extends AppCompatActivity implements GoogleApiClient.OnConnectionFailedListener {

    private GoogleApiClient mGoogleApiClient;

    private ServerConnection mServerConnection;

    private View userAccountControlLayout;

    private static final int TAB_FRAGMENT_RECENT_SERVICES_INDEX = 0;
    private RecentServicesTabFragment recentServicesTabFragment;
    private static final int TAB_FRAGMENT_SEARCH_SERVICES_INDEX = 1;
    private SearchServiceTabFragment searchServiceTabFragment;
    private static final int TAB_FRAGMENT_REQUEST_SERVICES_INDEX = 2;
    private RequestServiceTabFragment requestServiceTabFragment;

    private RetainedFragment dataFragment;
    private TabLayout tabLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_main);

        userAccountControlLayout = findViewById(R.id.user_control_layout);

        // find the retained fragment on activity restarts
        FragmentManager fm = getSupportFragmentManager();
        dataFragment = (RetainedFragment) fm.findFragmentByTag(
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
                    mServerConnection, recentServicesTabFragment, searchServiceTabFragment,
                    requestServiceTabFragment);

            dataFragment.setData(data);
        } else {
            ClientActivityRetainedData data = (ClientActivityRetainedData) dataFragment.getData();
            if (data != null) {
                mServerConnection = data.getServerConnection();
                recentServicesTabFragment = data.getRecentServicesTabFragment();
                searchServiceTabFragment = data.getSearchServiceTabFragment();
                requestServiceTabFragment = data.getRequestServiceTabFragment();
            }
        }

        setupGoogleApiClient();

        setupActionBar();
        setupTabLayout();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mServerConnection.cancelAllTasks();
    }

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
        startActivity(intent);
    }

    public void showSignInForm(View view) {
    }

    public GoogleApiClient getGoogleApiClient() {
        return mGoogleApiClient;
    }

    public ServerConnection getServerConnection() {
        return mServerConnection;
    }

    public boolean isUserControlLayoutVisible() {
        return userAccountControlLayout.getVisibility() == View.VISIBLE;
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
        Toolbar actionBar = (Toolbar) findViewById(R.id.actionBar);
        setSupportActionBar(actionBar);
    }

    private void setupTabLayout() {
        ViewPager tabsViewPager = (ViewPager) findViewById(R.id.tabs_viewpager);
        tabsViewPager.setAdapter(new TabsPagerAdapter(getSupportFragmentManager()));

        if (userAccountControlLayout.getVisibility() == View.VISIBLE) {
            int paddingBottom = tabsViewPager.getPaddingBottom();

            userAccountControlLayout.measure(View.MeasureSpec.UNSPECIFIED,
                    View.MeasureSpec.UNSPECIFIED);

            // Adding this much padding reduces the height of the ViewPager so there is
            // no interference between the ViewPager and the Sign In/Up layout.
            int addedPadding = (int) (userAccountControlLayout.getMeasuredHeight()
                    + getResources().getDimension(R.dimen.activity_horizontal_margin));

            Utils.setSpecificPadding(tabsViewPager, Utils.Padding.BOTTOM,
                    paddingBottom + addedPadding);
        }

        tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(tabsViewPager);
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
                    return recentServicesTabFragment;
                case 1:
                    return searchServiceTabFragment;
                case 2:
                    return requestServiceTabFragment;
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
                if (recentServicesTabFragment == null) {
                    recentServicesTabFragment = new RecentServicesTabFragment();
                    return true;
                }
            case TAB_FRAGMENT_SEARCH_SERVICES_INDEX:
                if (searchServiceTabFragment == null) {
                    searchServiceTabFragment = new SearchServiceTabFragment();
                    return true;
                }
            case TAB_FRAGMENT_REQUEST_SERVICES_INDEX:
                if (requestServiceTabFragment == null) {
                    requestServiceTabFragment = new RequestServiceTabFragment();
                    return true;
                }
        }

        return false;
    }
}
