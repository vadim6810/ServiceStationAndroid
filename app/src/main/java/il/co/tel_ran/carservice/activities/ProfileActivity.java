package il.co.tel_ran.carservice.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Places;

import org.json.JSONException;

import java.util.List;

import il.co.tel_ran.carservice.ClientUser;
import il.co.tel_ran.carservice.ProviderUser;
import il.co.tel_ran.carservice.R;
import il.co.tel_ran.carservice.ServerConnection;
import il.co.tel_ran.carservice.ServiceSearchQuery;
import il.co.tel_ran.carservice.ServiceSearchResult;
import il.co.tel_ran.carservice.ServiceStation;
import il.co.tel_ran.carservice.User;
import il.co.tel_ran.carservice.UserType;
import il.co.tel_ran.carservice.Utils;
import il.co.tel_ran.carservice.VehicleData;
import il.co.tel_ran.carservice.dialogs.ChangePasswordDialog;
import il.co.tel_ran.carservice.fragments.RegistrationServiceDetailsFragment;
import il.co.tel_ran.carservice.fragments.RegistrationVehicleDetailsFragment;

public class ProfileActivity extends AppCompatActivity
    implements View.OnClickListener, View.OnTouchListener,
        GoogleApiClient.OnConnectionFailedListener {

    private UserType mUserType = UserType.USER_CLIENT;

    private Menu mMenu;

    private User mUser = new User();
    private User mUserChanges;

    private boolean mIsEditing = false;

    private View mLayout;

    private EditText mNameEditText;
    private EditText mEmailAddressEditText;

    private View mVehicleDetailsLayout;
    private TextView mVehicleDetailsTextView;

    private Snackbar mChangesSnackbar;
    private boolean mUndoChanges;

    private View mServiceDetailsLayout;
    private RegistrationServiceDetailsFragment mServiceDetailsFragment;
    private long mServiceId;
    private GoogleApiClient mGoogleApiClient;

    private boolean mIsServiceLoading = false;

    private boolean mChangesMade;

    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Log.d("PMA" , "onBackPressed :: fragmentManager.getBackStackEntryCount() = " + fragmentManager.getBackStackEntryCount());
        if (fragmentManager.getBackStackEntryCount() == 0) {
            Log.d("PMA" , "onBackPressed :: any changes: " + mChangesMade);
            Intent intent = new Intent();
            intent.putExtra("any_changes", mChangesMade);
            setResult(RESULT_OK, intent);
            finish();
        }

        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_activity_menu, menu);

        mMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Navigate on back stack when pressing the back button.
                onBackPressed();
                break;
            case R.id.menu_item_edit:
                if (mIsServiceLoading) {
                    Toast.makeText(
                            ProfileActivity.this, R.string.loading_message, Toast.LENGTH_SHORT)
                            .show();
                } else {
                    // Allow user editing.
                    mIsEditing = !mIsEditing;
                    toggleEditing(mIsEditing);
                }

                break;
            default:
                super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void onClick(View v) {
    }

    public void updateVehicleDetails(View v) {
        showUpdateVehicleDetailsDialog();
    }

    public void changePassword(View v) {
        showChangePaswsordDialog();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (v.getId()) {
            case R.id.user_name_edit_text:
                // FALLTHROUGH
            case R.id.user_email_edit_text:
                if (!mIsEditing) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        Toast
                                .makeText(ProfileActivity.this, getString(
                                        R.string.enable_editing_required_message), Toast.LENGTH_SHORT)
                                .show();
                    }
                    return true;
                }
                break;
        }
        return false;
    }

    public void updateVehicleDetails(VehicleData newVehicleData) {
        if (newVehicleData != null && mUserType == UserType.USER_CLIENT) {
            ((ClientUser) mUser).setVehicleData(newVehicleData);
            updateFields(false);
        }
    }

    public void onServicesRetrievingStarted() {
        if (mServiceDetailsFragment != null) {
            mServiceDetailsFragment.toggleLoadingService(true);
        }

        mIsServiceLoading = true;
    }

    public void onServicesRetrieved(ServiceStation serviceStation) {
        mIsServiceLoading = false;

        if (serviceStation != null) {
            if (mServiceDetailsFragment != null) {
                mServiceDetailsFragment.toggleLoadingService(false);
                mServiceDetailsFragment.setFieldsFromService(serviceStation);
            }

            try {
                ((ProviderUser) mUser).setService(serviceStation);
                // Save as a copy
                ((ProviderUser) mUserChanges).setService(new ServiceStation(serviceStation));
            } catch (ClassCastException e) {
                e.printStackTrace();
            }
        } else {
            // TODO: Add text view to display if service failed to load.
        }
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_profile);

        setupGoogleApiClient();

        Bundle extras = getIntent().getExtras();

        boolean hasExtras = extras != null && !extras.isEmpty();
        if (hasExtras) {
            UserType userType = (UserType) extras.getSerializable("user_type");
            if (userType != null) {
                mUserType = userType;
            }

            // TODO: Get User object from extras.
        }

        mLayout = findViewById(R.id.activity_profile);

        mNameEditText = (EditText) findViewById(R.id.user_name_edit_text);
        mNameEditText.setOnTouchListener(this);
        mEmailAddressEditText = (EditText) findViewById(R.id.user_email_edit_text);
        mEmailAddressEditText.setOnTouchListener(this);

        mVehicleDetailsLayout = findViewById(R.id.vehicle_info_layout);
        mVehicleDetailsTextView = (TextView) findViewById(R.id.vehicle_details_text_view);

        mServiceDetailsLayout = findViewById(R.id.service_info_layout);

        setupServiceDetailsFragment();

        setupChangesSnackbar();

        // Mock details
        mUser.setName("Max");
        mUser.setEmail("maximglukhov@hotmail.com");

        // TODO: Remove mock user later.
        switch (mUserType) {
            case USER_SERVICE_PROVIDER:
                // Hide vehicle details layout since it's visible by default.
                mVehicleDetailsLayout.setVisibility(View.GONE);
                findViewById(R.id.update_button).setVisibility(View.GONE);
                // Show the service details layout since it's hidden by default.
                mServiceDetailsLayout.setVisibility(View.VISIBLE);

                if (hasExtras) {
                    mUser = (User) extras.getSerializable("user");
                }

                onServicesRetrievingStarted();
                ServerConnection.getMasterById(ProfileActivity.this, mUser.getId(),
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                if (response == null || response.isEmpty()) {
                                    // TODO: handle error by showing an error message

                                    // Exit this activity.
                                    finish();
                                }
                                try {
                                    ServiceStation serviceStation = ServerConnection
                                            .parseMastersFromResponse(response)[0];

                                    if (serviceStation != null) {
                                        mUser = new ProviderUser(mUser);

                                        onServicesRetrieved(serviceStation);
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        }, new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                // TODO: handle error by showing an error message

                                // Exit this activity.
                                finish();
                            }
                        });

                mUserChanges = new ProviderUser(mUser);

                if (mServiceDetailsFragment != null)
                    ((ProviderUser) mUserChanges).setService(mServiceDetailsFragment.getService());

                break;
            case USER_CLIENT:
                // FALLTHROUGH
            default:
                // Service details is hidden by default.

                if (hasExtras) {
                    mUser = (ClientUser) extras.getSerializable("user");
                }

                mUserChanges = new ClientUser(mUser);
                break;
        }

        updateFields(false);

        setupActionBar();
    }

    private void setupServiceDetailsFragment() {
        mServiceDetailsFragment = (RegistrationServiceDetailsFragment) getSupportFragmentManager()
                .findFragmentById(R.id.service_details_fragment);

        mServiceDetailsFragment.toggleFields(false);

        mServiceDetailsFragment.hideTitle(true);
        mServiceDetailsFragment.hideCaption(true);
    }

    private void toggleEditing(boolean toggle) {
        MenuItem editProfileMenuItem = mMenu.getItem(0);

        if (toggle) {
            Toast.makeText(ProfileActivity.this, getString(R.string.editing_enabled_messeage), Toast.LENGTH_SHORT).show();

            if (editProfileMenuItem != null) {
                editProfileMenuItem.setIcon(R.drawable.ic_check_white_24dp);
                editProfileMenuItem.setTitle(getString(R.string.done));
            }

            // Make sure the user can't undo changes while editing.
            if (mChangesSnackbar.isShownOrQueued()) {
                mChangesSnackbar.dismiss();
            }
        } else {
            mLayout.requestFocus();

            // Force the keyboard to hide to ensure no changes are made when editing is disabled.
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mLayout.getWindowToken(), 0);

            if (editProfileMenuItem != null) {
                editProfileMenuItem.setIcon(R.drawable.ic_edit_white_24dp);
                editProfileMenuItem.setTitle(getString(R.string.edit));
            }

            finishEditing();
        }

        if (mServiceDetailsFragment != null) {
            mServiceDetailsFragment.toggleFields(toggle);
        }
    }

    private void finishEditing() {
        // Save changes
        mUserChanges.setName(mNameEditText.getText().toString());
        mUserChanges.setEmail(mEmailAddressEditText.getText().toString());

        if (mUserType == UserType.USER_SERVICE_PROVIDER && mServiceDetailsFragment != null) {
            // Update Service in fragment
            mServiceDetailsFragment.updateServiceFromFields();

            try {
                // Get a copy of the service.
                ((ProviderUser) mUserChanges).setService(mServiceDetailsFragment.getService());
            } catch (ClassCastException e) {
                e.printStackTrace();
            }
        }

        // Check if any of the fields were changed
        if (changesMade()) {
            // Show the user a message notifying him about the changes, also giving him an option to undo the changes.
            mChangesSnackbar.show();
        }
    }

    private boolean changesMade() {
        return !mUserChanges.equals(mUser);
    }

    private void undoChanges() {
        Toast.makeText(ProfileActivity.this, getString(R.string.discarding_changes_message),
                Toast.LENGTH_SHORT).show();

        if (mUserType == UserType.USER_SERVICE_PROVIDER) {
            mUserChanges = new ProviderUser((ProviderUser) mUser);
        } else {
            mUserChanges = new ClientUser((ClientUser) mUser);
        }

        updateFields(true);
    }

    private void saveChanges() {
        mChangesMade = true;

        mUser.setName(mNameEditText.getText().toString());
        mUser.setEmail(mEmailAddressEditText.getText().toString());

        try {
            if (mUserType == UserType.USER_SERVICE_PROVIDER) {
                // Get a copy of the internal service
                ((ProviderUser) mUser).setService((mServiceDetailsFragment.getService()));
                mUserChanges = new ProviderUser((ProviderUser) mUser);
            } else {
                mUserChanges = new ClientUser((ClientUser) mUser);
            }
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
    }

    private void updateFields(boolean undo) {
        mNameEditText.setText(mUser.getName());
        mEmailAddressEditText.setText(mUser.getEmail());

        switch (mUserType) {
            case NONE:
                // FALLTHROUGH
            case USER_CLIENT:
                VehicleData vehicleData = ((ClientUser) mUser).getVehicleData();
                if (vehicleData != null) {
                    mVehicleDetailsTextView.setText(vehicleData.toString());
                }
                break;
            case USER_SERVICE_PROVIDER:
                if (mServiceDetailsFragment != null) {
                    try {
                        if (undo) {
                            mServiceDetailsFragment.setFieldsFromService(((ProviderUser) mUser).getService());
                        } else {
                            mServiceDetailsFragment.setFieldsFromService(((ProviderUser) mUserChanges).getService());
                        }
                    } catch (ClassCastException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    private void setupChangesSnackbar() {
        mChangesSnackbar = Snackbar
                .make(mLayout, getString(R.string.saving_changes_message), Snackbar.LENGTH_LONG)
                .setAction(getString(R.string.undo), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        undoChanges();
                        mUndoChanges = true;
                    }
                })
                .setCallback(new Snackbar.Callback() {

                    @Override
                    public void onDismissed(Snackbar snackbar, int event) {
                        // Check if the event was dismissed by anything but the action
                        switch (event) {
                            case DISMISS_EVENT_TIMEOUT:
                                // FALLTHROUGH
                            case DISMISS_EVENT_MANUAL:
                                // FALLTHROUGH
                            case DISMISS_EVENT_CONSECUTIVE:
                                // FALLTHROUGH
                            case DISMISS_EVENT_SWIPE:
                                // FALLTHROUGH
                            default:
                                // FALLTHROUGH
                                if (!mUndoChanges)
                                    saveChanges();

                                mUndoChanges = false;
                                break;
                        }
                    }
                });
    }

    private void setupActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.action_bar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                // Display the back key instead of home.
                actionBar.setDisplayHomeAsUpEnabled(true);
                // Enable the icon displaying.
                actionBar.setDisplayShowHomeEnabled(true);
                actionBar.setTitle(getString(R.string.profile_activity_title));
            }
        }
    }

    private void showChangePaswsordDialog() {
        ChangePasswordDialog changePasswordDialog = ChangePasswordDialog.getInstance();
        Utils.showDialogFragment(getSupportFragmentManager(), changePasswordDialog,
                "change_password_dialog");
    }

    private void showUpdateVehicleDetailsDialog() {
        VehicleData vehicleData = ((ClientUser) mUser).getVehicleData();
        RegistrationVehicleDetailsFragment vehicleDetailsFragment =
                RegistrationVehicleDetailsFragment.getInstance(vehicleData);
        Utils.showDialogFragment(getSupportFragmentManager(), vehicleDetailsFragment,
                "change_password_dialog");
    }

    private void setupGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();
    }
}
