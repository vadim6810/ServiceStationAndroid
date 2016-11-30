package il.co.tel_ran.carservice.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatRatingBar;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;

import java.util.Locale;

import il.co.tel_ran.carservice.LoadPlacePhotoTask;
import il.co.tel_ran.carservice.ProviderUser;
import il.co.tel_ran.carservice.R;
import il.co.tel_ran.carservice.ServerConnection;
import il.co.tel_ran.carservice.ServiceSearchQuery;
import il.co.tel_ran.carservice.ServiceSearchResult;
import il.co.tel_ran.carservice.ServiceStation;
import il.co.tel_ran.carservice.User;
import il.co.tel_ran.carservice.UserType;
import il.co.tel_ran.carservice.fragments.ProviderInboxFragment;
import il.co.tel_ran.carservice.fragments.RefreshingFragment;
import il.co.tel_ran.carservice.fragments.TenderRequestsFragment;

public class ProviderMainActivity extends AppCompatActivity
        implements ServerConnection.OnServicesRetrievedListener,
        GoogleApiClient.OnConnectionFailedListener, NavigationView.OnNavigationItemSelectedListener,
        SwipeRefreshLayout.OnRefreshListener, RefreshingFragment.RefreshingFragmentListener {

    private static final int REQUEST_CODE_PROFILE_CHANGED = 1;

    private static final int DRAWER_MENU_ITEM_PROFILE_INDEX = 0;
    private static final int DRAWER_MENU_ITEM_INBOX_INDEX = 1;
    private static final int DRAWER_MENU_ITEM_ITENDER_REQUESTS_INDEX = 2;

    private ServerConnection mServerConnection;
    private GoogleApiClient mGoogleApiClient;
    private boolean mIsLoadingService;

    ProviderUser mUser;

    private Toolbar mToolbar;
    private ActionBar mActionBar;

    private SwipeRefreshLayout mSwipeRefreshLayout;

    private DrawerLayout mDrawerLayout;
    private AppCompatRatingBar mDrawerServiceRatingBar;
    private TextView mDrawerRatingCountTextView;

    private int mSelectedDrawerItem;
    private ImageView mDrawerServicePhotoImageView;
    private TextView mDrawerServiceNameTextView;
    private NavigationView mNavigationView;
    private TextView mInboxItemTitleTextView;

    private TenderRequestsFragment mTenderRequestsFragment;
    private ProviderInboxFragment mInboxMessagesFragment;

    /*
     * ServerConnection.OnServicesRetrievedListener
     */

    @Override
    public void onServicesRetrievingStarted() {

        mIsLoadingService = true;
        if (mActionBar != null) {
            mActionBar.setTitle(R.string.loading_progress_title);
        }

        if (mDrawerServiceNameTextView != null) {
            mDrawerServiceNameTextView.setText(R.string.loading_progress_title);
        }
    }

    @Override
    public void onServicesRetrieved(ServiceSearchResult searchResult) {
        mIsLoadingService = false;

        ServiceStation loadedService = null;
        for (ServiceStation service : searchResult.getSerivces()) {
            loadedService = service;
            // Check if this result is our service
            if (loadedService.getID() == mUser.getService().getID()) {
                mUser.setService(loadedService);
                updateLayout();
                break;
            }
        }

        if (loadedService == null) {
            // TODO: handle error, display error messasge.
        }
    }

    /*
     * GoogleApiClient.OnConnectionFailedListener
     */

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    /*
     * NavigationView.OnNavigationItemSelectedListener
     */

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Close opened drawer.
        mDrawerLayout.closeDrawers();

        Fragment replaceFragment = null;
        switch (item.getItemId()) {
            case R.id.drawer_menu_item_profile:
                startProfileActivity();

                // Return false because we don't want to display the item as selected item.
                // That's because we are launching a different activity rather than updating a fragment.
                return false;
            case R.id.drawer_menu_item_inbox:
                if (mInboxMessagesFragment == null) {
                    mInboxMessagesFragment = new ProviderInboxFragment();
                    mInboxMessagesFragment.setOnRefreshEndListener(this);
                }
                replaceFragment = mInboxMessagesFragment;
                break;
            case R.id.drawer_menu_item_tender_requests:
                if (mTenderRequestsFragment == null) {
                    mTenderRequestsFragment = new TenderRequestsFragment();
                    mTenderRequestsFragment.setOnRefreshEndListener(this);
                }
                replaceFragment = mTenderRequestsFragment;
                break;
            default:
                return false;
        }

        mSelectedDrawerItem = item.getItemId();

        if (replaceFragment != null) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            // Replace the current fragment with one corresponding to the selected item.
            fragmentTransaction.replace(R.id.frame, replaceFragment);
            fragmentTransaction.commit();
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                return true;
            case R.id.menu_item_profile:
                startProfileActivity();
                return true;
            case R.id.menu_item_refresh:
                // Toggle refresh indicator
                mSwipeRefreshLayout.setRefreshing(true);
                // Start refreshing
                onRefresh();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public ServerConnection getServerConnection() {
        return mServerConnection;
    }

    /*
     * SwipeRefreshLayout.OnRefreshListener
     */

    @Override
    public void onRefresh() {
        switch (mSelectedDrawerItem) {
            case R.id.drawer_menu_item_inbox:
                break;
            case R.id.drawer_menu_item_tender_requests:
                if (mTenderRequestsFragment != null) {
                    mTenderRequestsFragment.onRefreshStart();
                }
                break;
        }
    }

    /*
     * RefreshingFragment.RefreshingFragmentListener
     */

    @Override
    public void onRefreshEnd() {
        mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_PROFILE_CHANGED:
                if (resultCode == RESULT_OK) {
                    // Check if user made any changes in ProfileActivity.
                    Bundle extras = data.getExtras();
                    if (extras != null && !extras.isEmpty()) {
                        boolean anyChanges = extras.getBoolean("any_changes");
                        if (anyChanges) {
                            // TODO: Reload user when back-end is available
                            // Reload service.
                            loadUserService();
                        }
                    }
                    // TODO: check for any changes extra. if there are any changes, update user & service.
                }
                break;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provider_main);

        setupServerConnection();

        setupGoogleApiClient();

        setupActionBar();

        setupDrawerLayout();
        setupDrawerHeader();

        setupRefreshLayout();

        Bundle extras = getIntent().getExtras();
        if (extras != null && !extras.isEmpty()) {
            int serviceId = extras.getInt("service_id");

            User user = (User) extras.get("user");
            if (user != null) {
                mUser = new ProviderUser(user);

                ServiceStation userService = new ServiceStation();
                userService.setID(serviceId);

                mUser.setService(userService);

                loadUserService();
            }
        } else {
            // Show some error

            // Exit this activity.
            finish();
        }
    }

    private void setupRefreshLayout() {
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(this);
    }

    private void setupServerConnection() {
        mServerConnection = new ServerConnection();
    }

    private void setupGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();
    }

    private void loadUserService() {
        mServerConnection.findServices(new ServiceSearchQuery(), mGoogleApiClient, this);
    }

    private void setupActionBar() {
        mToolbar = (Toolbar) findViewById(R.id.actionBar);
        setSupportActionBar(mToolbar);

        mActionBar = getSupportActionBar();
        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(true);
            mActionBar.setHomeButtonEnabled(true);
            mActionBar.setTitle(getString(R.string.app_name));
        }
    }

    private void setupDrawerLayout() {
        mDrawerLayout = (DrawerLayout) findViewById(R.id.activity_provider_drawer_layout);

        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(
                ProviderMainActivity.this, mDrawerLayout, mToolbar,
                R.string.open_drawer, R.string.close_drawer);

        mDrawerLayout.addDrawerListener(actionBarDrawerToggle);
        // Make navigation drawer icon sync animation with the navigation drawer.
        actionBarDrawerToggle.syncState();

        mNavigationView = (NavigationView) findViewById(R.id.provider_navigation_view);
        mNavigationView.setNavigationItemSelectedListener(this);

        // Set the default page to show current tender requests
        MenuItem tenderRequestsMenuItem = mNavigationView.getMenu()
                .findItem(R.id.drawer_menu_item_tender_requests);
        this.onNavigationItemSelected(tenderRequestsMenuItem);
        mNavigationView.setCheckedItem(R.id.drawer_menu_item_tender_requests);

        View inboxItemLayout = mNavigationView.getMenu().getItem(DRAWER_MENU_ITEM_INBOX_INDEX)
                .getActionView();
        mInboxItemTitleTextView = (TextView) inboxItemLayout.findViewById(R.id.notification_text_view);
        mInboxItemTitleTextView.setText("0");
    }

    private void setupDrawerHeader() {
        View headerView = mNavigationView.getHeaderView(0);
        mDrawerServicePhotoImageView = (ImageView) headerView.findViewById(R.id.service_details_photo);
        mDrawerServiceNameTextView = (TextView) headerView.findViewById(R.id.service_name_text_view);
        mDrawerServiceRatingBar = (AppCompatRatingBar) headerView.findViewById(R.id.service_rating_bar);
        mDrawerRatingCountTextView = (TextView) headerView.findViewById(R.id.rating_submit_count_text_view);
    }

    private void updateLayout() {
        if (mUser == null)
            return;

        ServiceStation userService = mUser.getService();
        if (userService == null)
            return;

        if (mActionBar != null) {
            // Update action bar title to display service's name
            mActionBar.setTitle(userService.getName());
        }

        Place location = userService.getLocation();
        if (mDrawerServicePhotoImageView != null && location != null) {
            mDrawerServicePhotoImageView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
            // Update service photo in drawer layout
            // Get photo for this Google Maps address to display.
            new LoadPlacePhotoTask(mGoogleApiClient,
                    mDrawerServicePhotoImageView.getMeasuredWidth(),
                    mDrawerServicePhotoImageView.getMeasuredHeight()) {

                @Override
                protected void onPostExecute(Bitmap bitmapPhoto) {
                    if (bitmapPhoto != null) {
                        // Photo has been loaded, display it.
                        mDrawerServicePhotoImageView.setImageBitmap(bitmapPhoto);

                    }
                }
            }.execute(location.getId());
        }

        if (mDrawerServiceNameTextView != null) {
            // Update service name title in drawer layout
            mDrawerServiceNameTextView.setText(userService.getName());
        }

        if (mDrawerServiceRatingBar != null) {
            // Update rating stars.
            mDrawerServiceRatingBar.setRating(userService.getAvgRating());
        }

        if (mDrawerRatingCountTextView != null) {
            // Update rating submitted count.
            mDrawerRatingCountTextView.setText(String.format(Locale.getDefault(),
                    "(%d)", userService.getSubmittedRatings()));
        }
    }

    private void startProfileActivity() {
        if (mIsLoadingService) {
            Toast.makeText(
                    ProviderMainActivity.this, R.string.loading_message, Toast.LENGTH_SHORT)
                    .show();

            return;
        }
        // TODO: Add check for user signed-in
        Intent intent = new Intent(ProviderMainActivity.this, ProfileActivity.class);
        // Since we are in ClientMainActivity the user type is a client.
        intent.putExtra("user_type", UserType.USER_SERVICE_PROVIDER);
        // Pass user's service id
        intent.putExtra("service_id", mUser.getService().getID());
        startActivityForResult(intent, REQUEST_CODE_PROFILE_CHANGED);
    }
}
