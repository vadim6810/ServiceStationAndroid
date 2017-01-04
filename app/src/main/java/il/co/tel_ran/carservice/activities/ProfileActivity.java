package il.co.tel_ran.carservice.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;
import com.hotmail.maximglukhov.arrangedlayout.ArrangedLayout;
import com.hotmail.maximglukhov.chipview.ChipView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import il.co.tel_ran.carservice.ClientUser;
import il.co.tel_ran.carservice.LoadPlaceTask;
import il.co.tel_ran.carservice.ProviderUser;
import il.co.tel_ran.carservice.R;
import il.co.tel_ran.carservice.ServiceStation;
import il.co.tel_ran.carservice.ServiceType;
import il.co.tel_ran.carservice.TimeHolder;
import il.co.tel_ran.carservice.User;
import il.co.tel_ran.carservice.UserType;
import il.co.tel_ran.carservice.Utils;
import il.co.tel_ran.carservice.VehicleData;
import il.co.tel_ran.carservice.VehicleType;
import il.co.tel_ran.carservice.fragments.RegistrationServiceDetailsFragment;
import il.co.tel_ran.carservice.fragments.VehicleMakesFragment;
import il.co.tel_ran.carservice.fragments.WorkTypesFragment;

public class ProfileActivity extends AppCompatActivity
    implements GoogleApiClient.OnConnectionFailedListener {

    public static final int REQUEST_CODE_CLIENT_DETAILS_UPDATED = 1;
    public static final int REQUEST_CODE_LOGIN_INFO_UPDATED = 2;
    public static final int REQUEST_CODE_SERVICE_DETAILS_UPDATED = 3;

    private UserType mUserType = UserType.CLIENT;

    private User mUser = new User();

    private View mLayout;

    private EditText mEmailAddressEditText;
    private EditText mPasswordEditText;

    private EditText mNameEditText;
    private View mClientPersonalInfoLayout;
    private ArrangedLayout mVehicleDetailsArrangedLayout;

    private Snackbar mChangesSnackbar;
    private boolean mUndoChanges;

    private View mServiceDetailsLayout;
    private RegistrationServiceDetailsFragment mServiceDetailsFragment;
    private long mServiceId;
    private ServiceStation mService;
    private GoogleApiClient mGoogleApiClient;

    private boolean mIsServiceLoading = false;

    private boolean mChangesMade;
    private EditText mServiceNameEditText;
    private ArrangedLayout mServiceTypesArrangedLayout;
    private EditText mServiceAddressEditText;
    private EditText mServicePhonenumberEditText;
    private EditText mActiveTimeEditText;
    private ArrangedLayout mVehicleTypesArrangedLayout;
    private EditText mManagerNameEditText;
    private EditText mManagerPhonenumberEditText;
    private EditText mDirectorNameEditText;

    private int mItemSpacing;

    private String mServiceTypeAutoServiceString;
    private String mServiceTypeTyreRepairString;
    private String mServiceTypeCarWashString;
    private String mServiceTypeTowingString;

    private String mVehicleTypePrivateString;
    private String mVehicleTypeBusString;
    private String mVehicleTypeTruckString;
    private String mVehicleTypeMotorcycleString;

    @Override
    public void onBackPressed() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        Log.d("PMA" , "onBackPressed :: fragmentManager.getBackStackEntryCount() = " + fragmentManager.getBackStackEntryCount());
        if (fragmentManager.getBackStackEntryCount() == 0) {
            Log.d("PMA" , "onBackPressed :: any changes: " + mChangesMade);
            Intent intent = new Intent();
            intent.putExtra("any_changes", mChangesMade);
            intent.putExtra("user", mUser);
            intent.putExtra("service", mService);
            setResult(RESULT_OK, intent);
            finish();
        }

        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Navigate on back stack when pressing the back button.
                onBackPressed();
                break;
            default:
                super.onOptionsItemSelected(item);
        }
        return true;
    }

    public void editClientPersonalInfo(View v) {
        startEditClientPersonalInfoActivity();
    }

    public void editLogin(View view) {
        startEditLoginInfoActivity();
    }

    public void editServiceInfo(View view) {
        if (mIsServiceLoading) {
            Toast.makeText(ProfileActivity.this, R.string.loading_message, Toast.LENGTH_SHORT)
                    .show();
        } else {
            startEditServiceInfoActivity();
        }
    }

    public void showWorkTypes(View view) {
        showWorkTypesSelectionDialog();
    }

    public void showVehicleMakes(View view) {
        showVehicleMakesSelectionDialog();
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        // TODO: handle error
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CODE_CLIENT_DETAILS_UPDATED:
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        ClientUser clientUserChanges = (ClientUser) data
                                .getSerializableExtra("edited_user");
                        handleClientUserChanges(clientUserChanges);
                    }
                } else if (resultCode == EditProfileInfoActivity.RESULT_ERROR) {
                    // TODO: handle error
                }
                break;
            case REQUEST_CODE_LOGIN_INFO_UPDATED:
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        User userChanges;
                        if (mUserType == UserType.CLIENT) {
                            userChanges = (ClientUser) data
                                    .getSerializableExtra("edited_user");
                        } else {
                            userChanges = (ProviderUser) data
                                    .getSerializableExtra("edited_user");
                        }
                        handleLoginInfoChanges(userChanges);
                    }
                } else if (resultCode == EditProfileInfoActivity.RESULT_ERROR) {
                    // TODO: handle error
                }
                break;
            case REQUEST_CODE_SERVICE_DETAILS_UPDATED:
                if (resultCode == RESULT_OK) {
                    if (data != null) {
                        ServiceStation serviceStation = (ServiceStation) data
                                .getSerializableExtra("edited_service");
                        handleServiceDetailsChanges(serviceStation);
                    }
                } else if (resultCode == EditProfileInfoActivity.RESULT_ERROR) {
                    // TODO: handle error
                }
                break;
        }
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

            mUser = (User) extras.getSerializable("user");

            mService = (ServiceStation) extras.getSerializable("service");
        }

        mLayout = findViewById(R.id.activity_profile);

        setupLoginInfoLayout();

        mClientPersonalInfoLayout = findViewById(R.id.client_personal_info_layout);

        loadResources();

        // TODO: Remove mock user later.
        switch (mUserType) {
            case MASTER:
                // Hide vehicle details layout since it's visible by default.
                mClientPersonalInfoLayout.setVisibility(View.GONE);

                setupServiceInfoLayout();

                loadServiceResources();

                // Show the service details layout since it's hidden by default.
                mServiceDetailsLayout.setVisibility(View.VISIBLE);

                getGooglePlaceForService();

                updateServiceInfoFields();

                break;
            case CLIENT:
                // FALLTHROUGH
            default:
                // Service details is hidden by default.

                setupClientPersonalInfoLayout();

                updatePersonalInfoFields();
                break;
        }

        updateLoginInfoFields();

        setupActionBar();
    }

    private void loadResources() {
        mItemSpacing = getResources().getDimensionPixelSize(R.dimen.item_spacing);
    }

    private void loadServiceResources() {
        mServiceTypeTowingString = getString(R.string.towing_title);
        mServiceTypeAutoServiceString = getString(R.string.auto_service_title);
        mServiceTypeCarWashString = getString(R.string.car_wash_title);
        mServiceTypeTyreRepairString = getString(R.string.tyre_repair_title);

        mVehicleTypePrivateString = getString(R.string.vehicle_type_private);
        mVehicleTypeBusString = getString(R.string.vehicle_type_bus);
        mVehicleTypeMotorcycleString = getString(R.string.vehicle_type_motorcycle);
        mVehicleTypeTruckString = getString(R.string.vehicle_type_truck);
    }

    private void setupLoginInfoLayout() {
        mEmailAddressEditText = (EditText) findViewById(R.id.user_email_edit_text);
        mPasswordEditText = (EditText) findViewById(R.id.user_password_edit_text);
    }

    private void setupClientPersonalInfoLayout() {
        mNameEditText = (EditText) findViewById(R.id.user_name_edit_text);
        mVehicleDetailsArrangedLayout = (ArrangedLayout) findViewById(
                R.id.vehicles_arranged_layout);
    }

    private void setupServiceInfoLayout() {
        mServiceDetailsLayout = findViewById(R.id.service_info_layout);

        mServiceNameEditText = (EditText) findViewById(R.id.service_name_edit_text);

        mServiceTypesArrangedLayout = (ArrangedLayout) findViewById(
                R.id.service_types_arranged_layout);

        mServiceAddressEditText = (EditText) findViewById(R.id.address_edit_text);

        mServicePhonenumberEditText = (EditText) findViewById(R.id.phonenumber_edit_text);

        mActiveTimeEditText = (EditText) findViewById(R.id.active_time_edit_text);

        mVehicleTypesArrangedLayout = (ArrangedLayout) findViewById(
                R.id.vehicle_types_arranged_layout);

        mManagerNameEditText = (EditText) findViewById(R.id.manager_name_edit_text);
        mManagerPhonenumberEditText = (EditText) findViewById(R.id.manager_phonenumber_edit_text);

        mDirectorNameEditText = (EditText) findViewById(R.id.director_name_edit_text);
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

    private void startEditClientPersonalInfoActivity() {
        Intent intent = new Intent(ProfileActivity.this, EditClientDetailsActivity.class);
        // Pass user object
        intent.putExtra("user", mUser);
        intent.putExtra("user_type", mUserType);
        startActivityForResult(intent, REQUEST_CODE_CLIENT_DETAILS_UPDATED);
    }

    private void startEditServiceInfoActivity() {
        Intent intent = new Intent(ProfileActivity.this, EditServiceDetailsActivity.class);
        // Pass user object
        intent.putExtra("user", mUser);
        intent.putExtra("user_type", mUserType);
        // Pass service object
        intent.putExtra("service", mService);
        startActivityForResult(intent, REQUEST_CODE_SERVICE_DETAILS_UPDATED);
    }

    private void startEditLoginInfoActivity() {
        Intent intent = new Intent(ProfileActivity.this, EditUserLoginActivity.class);
        // Pass user object
        intent.putExtra("user", mUser);
        intent.putExtra("user_type", mUserType);
        startActivityForResult(intent, REQUEST_CODE_LOGIN_INFO_UPDATED);
    }

    private void setupGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();
    }

    private void getGooglePlaceForService() {
        if (mService != null && mGoogleApiClient != null) {
            final String placeId = mService.getPlaceId();
            if (placeId != null && !placeId.isEmpty()) {
                new LoadPlaceTask(mGoogleApiClient) {
                    @Override
                    protected void onPreExecute() {
                        mIsServiceLoading = true;
                    }

                    @Override
                    protected void onPostExecute(Place[] places) {
                        mIsServiceLoading = false;

                        if (places != null && places.length > 0) {
                            mService.setLocation(places[0]);

                            updateServiceAddress();
                        } else {
                            // TODO: handle error.
                        }
                    }
                }.execute(placeId);
            }
        }
    }

    private void handleClientUserChanges(ClientUser clientUserChanges) {
        if (!mUser.equals(clientUserChanges)) {
            // User has made changes.
            mChangesMade = true;

            mUser = clientUserChanges;

            updatePersonalInfoFields();
        }
    }

    private void handleLoginInfoChanges(User userChanges) {
        if (!mUser.equals(userChanges)) {
            // User has made changes (authentication).
            mChangesMade = true;

            mUser = userChanges;

            updateLoginInfoFields();
        }
    }

    private void handleServiceDetailsChanges(ServiceStation serviceStation) {
        // We must handle location (Place) respectively since it is transient and is not passed with extras.
        // The solution would be to compare place ids - if they are different that means there is a change.
        String newPlaceId = serviceStation.getPlaceId();
        String currentPlaceId = mService.getPlaceId();

        boolean isPlaceIdEquals = currentPlaceId.equals(newPlaceId);

        // Compare place ids first
        if (isPlaceIdEquals) {
            // If they are the same simply assign the current Place object to the new service.
            // This is important because otherwise it will interfere with equals method.
            serviceStation.setLocation(mService.getLocation());
        } else {
            // Update current service
            mService = serviceStation;

            // Update place object.
            getGooglePlaceForService();

            // Update fields
            updateServiceInfoFields();

            mChangesMade = true;
        }

        // Now compare other fields, knowing that Place object being transient does not bother equals method.
        if (isPlaceIdEquals && !mService.equals(serviceStation)) {
            // Update current service
            mService = serviceStation;

            // Update fields
            updateServiceInfoFields();

            mChangesMade = true;
        }
    }

    private void updateLoginInfoFields() {
        updateEmailAddress();
        updatePassword();
    }

    private void updatePersonalInfoFields() {
        updateClientVehiclesArrangedLayout();
        updateClientName();
    }

    private void updateServiceInfoFields() {
        if (mService != null) {
            updateServiceName();
            updateServicesArrangedLayout();
            updateServiceAddress();
            updateServicePhonenumber();
            updateServiceActiveTime();
            updateVehicleTypesArrangedLayout();
            updateManagerName();
            updateManagerPhonenumber();
            updateDirectorName();
        }
    }

    private void updateEmailAddress() {
        String emailAddress = mUser.getEmail();
        if (emailAddress != null) {
            mEmailAddressEditText.setText(emailAddress);
        } else {
            mEmailAddressEditText.setText("");
        }
    }

    private void updatePassword() {
        String password = mUser.getPassword();
        if (password != null) {
            String asteriskedPassword = Utils.generateAsteriskString(password.length());

            mPasswordEditText.setText(asteriskedPassword);
        } else {
            mPasswordEditText.setText("");
        }
    }

    private void updateClientName() {
        String name = ((ClientUser) mUser).getName();
        if (name != null) {
            mNameEditText.setText(name);
        } else {
            mNameEditText.setText("");
        }
    }

    private void updateClientVehiclesArrangedLayout() {
        mVehicleDetailsArrangedLayout.removeAllViews();

        List<VehicleData> vehicles = ((ClientUser) mUser).getVehicles();
        if (vehicles != null && !vehicles.isEmpty()) {

            for (VehicleData vehicleData : vehicles) {
                // Create a ChipView for every vehicle
                ChipView vehicleStringChipView = new ChipView(ProfileActivity.this);
                ViewCompat.setPaddingRelative(vehicleStringChipView, mItemSpacing, 0, 0,
                        mItemSpacing);
                // Make sure the chip is not deletable, it will be handled on another fragment.
                vehicleStringChipView.setDeletable(false);
                vehicleStringChipView.setText(vehicleData.toString());

                mVehicleDetailsArrangedLayout.addView(vehicleStringChipView);
            }
        }
    }

    public void updateServiceName() {
        String serviceName = mService.getName();
        if (serviceName != null && !serviceName.isEmpty()) {
            mServiceNameEditText.setText(serviceName);
        }
    }

    public void updateServicesArrangedLayout() {
        EnumSet<ServiceType> availableServices = mService.getAvailableServices();

        mServiceTypesArrangedLayout.removeAllViews();
        if (availableServices != null && !availableServices.isEmpty()) {
            for (ServiceType serviceType : availableServices) {
                ChipView serviceTypeChipView = new ChipView(ProfileActivity.this);
                serviceTypeChipView.setDeletable(false);

                switch (serviceType) {
                    case CAR_WASH:
                        serviceTypeChipView.setText(mServiceTypeCarWashString);
                        break;
                    case TOWING:
                        serviceTypeChipView.setText(mServiceTypeTowingString);
                        break;
                    case TYRE_REPAIR:
                        serviceTypeChipView.setText(mServiceTypeTyreRepairString);
                        break;
                    case AUTO_SERVICE:
                        serviceTypeChipView.setText(mServiceTypeAutoServiceString);
                        break;
                }

                serviceTypeChipView.setPadding(mItemSpacing, 0, mItemSpacing, 0);
                mServiceTypesArrangedLayout.addView(serviceTypeChipView);
            }
        }
    }

    public void updateServiceAddress() {
        Place location = mService.getLocation();
        if (location != null) {
            mServiceAddressEditText.setText(location.getAddress());
        }
    }

    public void updateServicePhonenumber() {
        String phonenumber = mService.getPhonenumber();
        if (phonenumber != null && !phonenumber.isEmpty()) {
            mServicePhonenumberEditText.setText(phonenumber);
        }
    }

    public void updateServiceActiveTime() {
        TimeHolder openingTime = mService.getOpeningTime();
        TimeHolder closingTime = mService.getClosingTime();

        if (openingTime != null && closingTime != null) {
            mActiveTimeEditText.setText(openingTime.toString() + " - " + closingTime.toString());
        }
    }

    public void updateVehicleTypesArrangedLayout() {
        EnumSet<VehicleType> vehicleTypes = mService.getVehicleTypes();

        mVehicleTypesArrangedLayout.removeAllViews();
        if (vehicleTypes != null && !vehicleTypes.isEmpty()) {
            for (VehicleType vehicleType : vehicleTypes) {
                ChipView vehicleTypeChipView = new ChipView(ProfileActivity.this);
                vehicleTypeChipView.setDeletable(false);

                switch (vehicleType) {
                    case PRIVATE:
                        vehicleTypeChipView.setText(mVehicleTypePrivateString);
                        break;
                    case MOTORCYCLE:
                        vehicleTypeChipView.setText(mVehicleTypeMotorcycleString);
                        break;
                    case BUS:
                        vehicleTypeChipView.setText(mVehicleTypeBusString);
                        break;
                    case TRUCK:
                        vehicleTypeChipView.setText(mVehicleTypeTruckString);
                        break;
                }

                vehicleTypeChipView.setPadding(mItemSpacing, mItemSpacing, mItemSpacing, 0);
                mVehicleTypesArrangedLayout.addView(vehicleTypeChipView);
            }
        }
    }

    public void updateManagerName() {
        String managerName = mService.getManagerName();
        if (managerName != null && !managerName.isEmpty()) {
            mManagerNameEditText.setText(managerName);
        }
    }

    public void updateManagerPhonenumber() {
        String managerPhonenumber = mService.getManagerPhonenumber();
        if (managerPhonenumber != null && !managerPhonenumber.isEmpty()) {
            mManagerPhonenumberEditText.setText(managerPhonenumber);
        }
    }

    public void updateDirectorName() {
        String directorName = mService.getDirectorName();
        if (directorName != null && !directorName.isEmpty()) {
            mDirectorNameEditText.setText(directorName);
        }
    }

    private void showWorkTypesSelectionDialog() {
        WorkTypesFragment workTypesFragment = WorkTypesFragment.getInstance(false,
                mService.getSubWorkTypes());
        Utils.showDialogFragment(getSupportFragmentManager(), workTypesFragment,
                "work_type_fragment");
    }

    private void showVehicleMakesSelectionDialog() {
        ArrayList<String> servicesCarMakes = new ArrayList<>();

        String[] servicedCarMakesArr = mService.getServicedCarMakes();
        if (servicedCarMakesArr != null && servicedCarMakesArr.length > 0) {
            Collections.addAll(servicesCarMakes, mService.getServicedCarMakes());
        }

        VehicleMakesFragment vehicleMakesFragment = VehicleMakesFragment.getInstance(
                servicesCarMakes, false);
        Utils.showDialogFragment(getSupportFragmentManager(),
                vehicleMakesFragment, "vehicle_make_fragment");
    }
}
