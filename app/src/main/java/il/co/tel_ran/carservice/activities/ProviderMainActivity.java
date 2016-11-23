package il.co.tel_ran.carservice.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Places;

import java.util.List;

import il.co.tel_ran.carservice.ProviderUser;
import il.co.tel_ran.carservice.R;
import il.co.tel_ran.carservice.ServerConnection;
import il.co.tel_ran.carservice.ServiceSearchQuery;
import il.co.tel_ran.carservice.ServiceSearchResult;
import il.co.tel_ran.carservice.ServiceStation;
import il.co.tel_ran.carservice.User;
import il.co.tel_ran.carservice.UserType;

public class ProviderMainActivity extends AppCompatActivity
        implements ServerConnection.OnServicesRetrievedListener,
        GoogleApiClient.OnConnectionFailedListener {

    private static final int REQUEST_CODE_PROFILE_CHANGED = 1;

    private ServerConnection mServerConnection;
    private GoogleApiClient mGoogleApiClient;
    private boolean mIsLoadingService;

    ProviderUser mUser;

    private ActionBar mActionBar;

    /*
     * ServerConnection.OnServicesRetrievedListener
     */

    @Override
    public void onServicesRetrievingStarted() {
        Log.d("PMA", "onServicesRetrievingStarted :: called.");
        mIsLoadingService = true;
        if (mActionBar != null) {
            mActionBar.setTitle(R.string.loading_progress_title);
        }
    }

    @Override
    public void onServicesRetrieved(List<ServiceSearchResult> searchResults) {
        mIsLoadingService = false;

        ServiceStation loadedService = null;
        for (ServiceSearchResult searchResult : searchResults) {
            loadedService = searchResult.getSerivce();
            // Check if this result is our service
            if (loadedService.getID() == mUser.getService().getID()) {
                Log.d("PMA", "onServicesRetrieved :: found service id match: loadedId=" + loadedService.getID() + " userServiceId=" + mUser.getService().getID());
                if (mActionBar != null) {
                    // Update action bar title to display service's name
                    mActionBar.setTitle(loadedService.getName());
                }
                mUser.setService(loadedService);

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
                if (mIsLoadingService) {
                    Toast.makeText(
                            ProviderMainActivity.this, R.string.loading_message, Toast.LENGTH_SHORT)
                            .show();

                    return true;
                }
                // TODO: Add check for user signed-in
                Intent intent = new Intent(ProviderMainActivity.this, ProfileActivity.class);
                // Since we are in ClientMainActivity the user type is a client.
                intent.putExtra("user_type", UserType.USER_SERVICE_PROVIDER);
                // Pass user's service id
                intent.putExtra("service_id", mUser.getService().getID());
                startActivityForResult(intent, REQUEST_CODE_PROFILE_CHANGED);
                return true;
        }
        return super.onOptionsItemSelected(item);
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

        setupActionBar();

        setupGoogleApiClient();

        mServerConnection = new ServerConnection();

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
        Toolbar toolbar = (Toolbar) findViewById(R.id.actionBar);
        setSupportActionBar(toolbar);

        mActionBar = getSupportActionBar();
        if (mActionBar != null) {
            mActionBar.setDisplayHomeAsUpEnabled(true);
            mActionBar.setHomeButtonEnabled(true);
            mActionBar.setTitle(getString(R.string.app_name));
        }
    }
}
