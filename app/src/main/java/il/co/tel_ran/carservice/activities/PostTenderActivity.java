package il.co.tel_ran.carservice.activities;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.hotmail.maximglukhov.arrangedlayout.ArrangedLayout;
import com.hotmail.maximglukhov.chipview.ChipView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;

import il.co.tel_ran.carservice.ClientUser;
import il.co.tel_ran.carservice.R;
import il.co.tel_ran.carservice.ServiceSubWorkType;
import il.co.tel_ran.carservice.ServiceWorkType;
import il.co.tel_ran.carservice.TenderRequest;
import il.co.tel_ran.carservice.Utils;
import il.co.tel_ran.carservice.VehicleData;
import il.co.tel_ran.carservice.VehicleType;
import il.co.tel_ran.carservice.dialogs.DatePickerDialogFragment;
import il.co.tel_ran.carservice.fragments.WorkTypesFragment;

public class PostTenderActivity extends AppCompatActivity implements View.OnClickListener,
        GoogleApiClient.OnConnectionFailedListener, DatePickerDialog.OnDateSetListener,
        CompoundButton.OnCheckedChangeListener, WorkTypesFragment.SelectWorkTypesDialogListener,
        ChipView.OnChipDeleteClickListener, AdapterView.OnItemSelectedListener {

    private static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 2;

    private ScrollView mLayout;

    private TenderRequest mTenderRequest;

    private Button mSearchLocationButton;
    private ImageView mCurrentLocationImageView;
    private ProgressBar mAcquireLocationProgressbar;

    private EditText mPriceEditText;

    private EditText mMessageEditText;

    private GoogleApiClient mGoogleApiClient;

    private TextView mThirdPartyAttributionsTextView;

    private Button mSetDeadlineButton;

    private RadioButton mTenderResolvedStatusRadioButton;
    private RadioButton mTenderClosedStatusRadioButton;
    private RadioButton mTenderOpenedStatusRadioButton;

    private ArrangedLayout mWorkTypesArrangedLayout;

    private final static int[] VEHICLE_TYPE_CHECKBOX_IDS = {
            R.id.service_vehicle_type_private,
            R.id.service_vehicle_type_truck,
            R.id.service_vehicle_type_bus,
            R.id.service_vehicle_type_motorcycles
    };
    private AppCompatCheckBox[] mVehicleTypeCheckBoxes
            = new AppCompatCheckBox[VEHICLE_TYPE_CHECKBOX_IDS.length];

    private ClientUser mClientUser;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /*
     * View.OnClickListener
     */

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.search_address_button:
                try {
                    // Build a place search intent with address filter.
                    Intent addressIntent = Utils.buildPlaceAutoCompleteIntent(
                            PostTenderActivity.this,
                            Utils.PLACE_FILTER_ADDRESS);
                    // Start the overlay activity with a unique request code to identify later.
                    startActivityForResult(addressIntent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
                } catch (GooglePlayServicesNotAvailableException
                        | GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.current_location_image_view:
                if (mGoogleApiClient != null) {
                    if (mGoogleApiClient.isConnected()) {
                        // Check if the application has permission to device's location.
                        if (!requestLocationServicesPermission())
                            return;

                        // Show the progressbar for some feedback.
                        mCurrentLocationImageView.setVisibility(View.GONE);
                        mAcquireLocationProgressbar.setVisibility(View.VISIBLE);
                        // Get the current place.
                        PendingResult<PlaceLikelihoodBuffer> result = Places.PlaceDetectionApi
                                .getCurrentPlace(mGoogleApiClient, null);
                        result.setResultCallback(new ResultCallback<PlaceLikelihoodBuffer>() {
                            @Override
                            public void onResult(@NonNull PlaceLikelihoodBuffer likelyPlaces) {
                                // Google's Places API requires displaying attributions.
                                final CharSequence thirdPartyAttributions =
                                        likelyPlaces.getAttributions();
                                if (thirdPartyAttributions != null && thirdPartyAttributions.length() > 0) {
                                    mThirdPartyAttributionsTextView.setText(thirdPartyAttributions);
                                    mThirdPartyAttributionsTextView.setVisibility(View.VISIBLE);
                                } else {
                                    mThirdPartyAttributionsTextView.setVisibility(View.GONE);
                                }

                                Place likelyPlace = null;
                                float highestLikelihood = -1.0f;
                                for (PlaceLikelihood placeLikelihood : likelyPlaces) {
                                    // Search for the highest likelihood of current location
                                    float likelihood = placeLikelihood.getLikelihood();
                                    if (likelihood > highestLikelihood) {
                                        likelyPlace = placeLikelihood.getPlace();
                                        highestLikelihood = likelihood;
                                    }
                                }

                                mCurrentLocationImageView.setVisibility(View.VISIBLE);
                                mAcquireLocationProgressbar.setVisibility(View.GONE);

                                if (likelyPlace != null) {
                                    mTenderRequest.setLocation(likelyPlace.getAddress().toString());
                                    mTenderRequest.setPlaceID(likelyPlace.getId());
                                    // Update the text to the highest likelihood place.
                                    mSearchLocationButton.setText(likelyPlace.getAddress());
                                } else {
                                    Toast.makeText(PostTenderActivity.this, R.string.failed_location_search,
                                            Toast.LENGTH_SHORT)
                                            .show();
                                }

                                // Release the buffer.
                                likelyPlaces.release();
                            }
                        });
                    }
                }
            case R.id.show_work_types_button:
                showWorkTypesSelectionDialog();
                break;
        }
    }

    /*
     * WorkTypesFragment.SelectWorkTypesDialogListener
     */

    @Override
    public void onWorkTypeSelected(ServiceWorkType[] workTypes, ServiceSubWorkType[] subWorkTypes) {
        ArrayList<ServiceWorkType> serviceWorkTypes = mTenderRequest.getWorkTypes();
        ArrayList<ServiceSubWorkType> serviceSubWorkTypes = mTenderRequest.getSubWorkTypes();

        serviceWorkTypes.clear();
        serviceSubWorkTypes.clear();

        Collections.addAll(serviceWorkTypes, workTypes);
        Collections.addAll(serviceSubWorkTypes, subWorkTypes);

        updateWorkTypesArrangedLayout();
    }

    /*
     * ChipView.OnChipDeleteClickListener
     */

    @Override
    public void onChipDelete(ChipView chipView) {
        Object tag = chipView.getTag(R.id.tag_chip_sub_work_type);
        if (tag != null) {
            ServiceSubWorkType subWorkType = (ServiceSubWorkType) tag;

            ArrayList<ServiceSubWorkType> subWorkTypes = mTenderRequest.getSubWorkTypes();
            if (subWorkTypes != null) {
                subWorkTypes.remove(subWorkType);

                int arrangedLayoutChildCount = mWorkTypesArrangedLayout.getChildCount();
                for (int i = 0; i < arrangedLayoutChildCount; i++) {
                    if (mWorkTypesArrangedLayout.getChildAt(i).equals(chipView)) {
                        mWorkTypesArrangedLayout.removeView(chipView);
                        break;
                    }
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        switch (requestCode) {
            case LOCATION_PERMISSION_REQUEST_CODE:
                for (int i = 0; i < permissions.length; i++) {
                    String permission = permissions[i];
                    int grantResult = grantResults[i];

                    if (permission.equals(Manifest.permission.ACCESS_FINE_LOCATION)) {
                        // The user has given the application the permission to access device location.
                        // Simulate location press once again (this time it should have the correct permission).
                        if (grantResult == PackageManager.PERMISSION_GRANTED) {
                            onClick(mCurrentLocationImageView);
                        } else {
                            // Notify the user that the application require location services to get current location.
                            Snackbar.make(mLayout, R.string.location_permission_denied, Snackbar.LENGTH_LONG)
                                    // For convenience, add an action to try to enable the permission once again.
                                    .setAction(getString(R.string.enable), new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            // Request the permission once again.
                                            requestLocationServicesPermission();
                                        }
                                    })
                                    .show();
                        }
                    }
                }
                break;
        }
    }

    /*
     * GoogleApiClient.OnConnectionFailedListener
     */

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    /*
     * AdapterView.OnItemSelectedListener
     */

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        String vehicleString = (String) parent.getItemAtPosition(position);

        if (vehicleString != null && !vehicleString.isEmpty()) {
            mTenderRequest.setVehicleData(VehicleData.parseVehicleData(vehicleString));
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    public void postRequest(View view) {
        Intent intent = new Intent();

        if (mTenderRequest != null) {
            if (!checkFields()) {
                Toast.makeText(
                        PostTenderActivity.this, R.string.incomplete_fields_message, Toast.LENGTH_SHORT)
                        .show();
                return;
            }
            mTenderRequest.setPrice(Float.valueOf(mPriceEditText.getText().toString()));

            Date currentDate = Calendar.getInstance().getTime();

            if (mTenderRequest.getCreatedAtDate() == null) {
                // Posting for the first time
                mTenderRequest.setCreatedAtDate(currentDate);
                mTenderRequest.setUpdatedAtDate(currentDate);
            } else {
                // Updating the request.
                mTenderRequest.setUpdatedAtDate(currentDate);
            }

            intent.putExtra("tender_request", mTenderRequest);
        } else {
            Toast.makeText(
                    PostTenderActivity.this, R.string.incomplete_fields_message, Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        setResult(RESULT_OK, intent);
        finish();
    }

    public void setDeadline(View view) {
        showDatePickerDialog();
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        if (mTenderRequest != null) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(year, month, dayOfMonth);

            mTenderRequest.setDeadlineDate(calendar.getTime());

            // Picked date is earlier or current date
            if (calendar.compareTo(Calendar.getInstance()) == -1) {
                Toast.makeText(PostTenderActivity.this, R.string.date_too_early_message,
                        Toast.LENGTH_SHORT)
                        .show();
            } else {
                mSetDeadlineButton.setText(Utils.getFormattedDate(
                        PostTenderActivity.this, year, month, dayOfMonth));
            }
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (isChecked) {
            if (buttonView.getId() == mTenderClosedStatusRadioButton.getId()) {
                mTenderRequest.setStatus(TenderRequest.Status.CLOSED);
            } else if (buttonView.getId() == mTenderOpenedStatusRadioButton.getId()) {
                mTenderRequest.setStatus(TenderRequest.Status.OPENED);
            } else if (buttonView.getId() == mTenderResolvedStatusRadioButton.getId()) {
                mTenderRequest.setStatus(TenderRequest.Status.RESOLVED);
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_tender);

        Bundle extras = getIntent().getExtras();
        if (extras != null && !extras.isEmpty()) {
            mTenderRequest = (TenderRequest) extras.getSerializable("tender_request");
            if (mTenderRequest == null) {
                mTenderRequest = new TenderRequest();
            }
            mClientUser = (ClientUser) extras.getSerializable("user");
        } else {
            // TODO: handle error
            finish();
        }

        mLayout = (ScrollView) findViewById(R.id.activity_post_tender);

        mAcquireLocationProgressbar = (ProgressBar) findViewById(R.id.acquire_location_progress_bar);

        mPriceEditText = (EditText) findViewById(R.id.tender_price_edit_text);

        float price = mTenderRequest.getPrice();
        mPriceEditText.setText(String.format(Locale.getDefault(), "%.2f", price));

        mMessageEditText = (EditText) findViewById(R.id.tender_message_edit_text);
        String message = mTenderRequest.getMessage();
        if (message != null && !message.isEmpty()) {
            mMessageEditText.setText(message);
        }

        mThirdPartyAttributionsTextView = (TextView) findViewById(R.id.third_party_attributions_text_view);

        findViewById(R.id.show_work_types_button).setOnClickListener(this);
        mWorkTypesArrangedLayout = (ArrangedLayout) findViewById(R.id.services_arranged_layout);
        updateWorkTypesArrangedLayout();

        setupVehicleTypeCheckboxes();

        setupUserVehiclesSpinner(mTenderRequest.getVehicleData());

        setupStatusRadioGroup();

        setupLocationSearchButton();

        setupDeadlineButton();

        setupActionBar();

        setupGoogleApiClient();
    }

    private void updateWorkTypesArrangedLayout() {
        ArrayList<ServiceSubWorkType> subWorkTypes = mTenderRequest.getSubWorkTypes();
        if (subWorkTypes == null || subWorkTypes.isEmpty()) {
            mWorkTypesArrangedLayout.removeAllViews();
        } else {
            // Remove current views (ChipView).
            mWorkTypesArrangedLayout.removeAllViews();

            int itemSpacing = getResources().getDimensionPixelSize(R.dimen.item_spacing);

            for (ServiceSubWorkType subWorkType : subWorkTypes) {
                // Create a ChipView for every sub work type
                ChipView subWorkTypeChip = new ChipView(PostTenderActivity.this);
                ViewCompat.setPaddingRelative(subWorkTypeChip, itemSpacing, 0, 0,
                        itemSpacing);
                subWorkTypeChip.setDeletable(true);
                subWorkTypeChip.setText(subWorkType.toString());
                subWorkTypeChip.addOnChipDeleteClickListener(this);
                subWorkTypeChip.setTag(R.id.tag_chip_sub_work_type, subWorkType);

                mWorkTypesArrangedLayout.addView(subWorkTypeChip);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case PLACE_AUTOCOMPLETE_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    Place place = PlaceAutocomplete.getPlace(PostTenderActivity.this, data);
                    mTenderRequest.setLocation(place.getAddress().toString());
                    mTenderRequest.setPlaceID(place.getId());
                    mSearchLocationButton.setText(place.getAddress());
                } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                    Status status = PlaceAutocomplete.getStatus(PostTenderActivity.this, data);
                    // TODO: Handle error
                } else if (resultCode == RESULT_CANCELED) {

                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void setupVehicleTypeCheckboxes() {
        for (int i = 0; i < mVehicleTypeCheckBoxes.length; i++) {
            mVehicleTypeCheckBoxes[i] = (AppCompatCheckBox) mLayout
                    .findViewById(VEHICLE_TYPE_CHECKBOX_IDS[i]);
        }

        EnumSet<VehicleType> vehicleTypes = mTenderRequest.getVehicleTypes();
        if (vehicleTypes != null && !vehicleTypes.isEmpty()) {
            mVehicleTypeCheckBoxes[0].setChecked(vehicleTypes.contains(VehicleType.PRIVATE));
            mVehicleTypeCheckBoxes[1].setChecked(vehicleTypes.contains(VehicleType.TRUCK));
            mVehicleTypeCheckBoxes[2].setChecked(vehicleTypes.contains(VehicleType.BUS));
            mVehicleTypeCheckBoxes[3].setChecked(vehicleTypes.contains(VehicleType.MOTORCYCLE));
        }
    }

    private void setupUserVehiclesSpinner(VehicleData vehicleData) {
        List<String> vehicleStrings = new ArrayList<>();
        for (VehicleData vehicle : mClientUser.getVehicles()) {
            vehicleStrings.add(vehicle.toString());
        }

        AppCompatSpinner vehiclesSpinner = (AppCompatSpinner) mLayout
                .findViewById(R.id.user_vehicles_spinner);
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(PostTenderActivity.this,
                android.R.layout.simple_spinner_item, vehicleStrings);
        vehiclesSpinner.setOnItemSelectedListener(this);

        vehiclesSpinner.setAdapter(arrayAdapter);

        if (vehicleData != null) {
            int index = vehicleStrings.indexOf(vehicleData.toString());
            if (index != -1) {
                vehiclesSpinner.setSelection(index);
            }
        }
    }

    private void setupLocationSearchButton() {
        mSearchLocationButton = (Button) findViewById(R.id.search_address_button);
        Drawable[] drawables = mSearchLocationButton.getCompoundDrawables();
        // Look for the search icon (could be from the left if it's RTL or from the right if its LTR)
        // The loop just jumps between index 0 (left) and index 2 (right)
        for (int i = 0; i < 3; i += 2) {
            Drawable searchIcon = drawables[i];
            if (searchIcon != null) {
                // Set the color to match text color (accent color)
                searchIcon.setColorFilter(Utils.getThemeAccentColor(PostTenderActivity.this),
                        PorterDuff.Mode.SRC_ATOP);

                // If we found a non-null drawable it's the only icon we require, don't look for anymore.
                break;
            }
        }
        mSearchLocationButton.setOnClickListener(this);

        mCurrentLocationImageView = (ImageView) findViewById(
                R.id.current_location_image_view);
        mCurrentLocationImageView.setColorFilter(Utils.getThemeAccentColor(PostTenderActivity.this),
                PorterDuff.Mode.SRC_ATOP);
        mCurrentLocationImageView.setOnClickListener(this);

        // Check if we are updating an existing tender request.
        String location = mTenderRequest.getLocation();
        if (location != null && !location.isEmpty()) {
            mSearchLocationButton.setText(location);
        }
    }

    private void setupDeadlineButton() {
        mSetDeadlineButton = (Button) findViewById(R.id.set_deadline_button);

        Date deadlineDate = mTenderRequest.getDeadlineDate();
        if (mTenderRequest != null && deadlineDate != null) {
            DateFormat dateFormat = android.text.format.DateFormat
                    .getDateFormat(PostTenderActivity.this);

            String formattedDate = dateFormat.format(deadlineDate);
            mSetDeadlineButton.setText(formattedDate);
        }
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
                actionBar.setTitle(getString(R.string.post_tender_activity_title));
            }
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

    private boolean requestLocationServicesPermission() {
        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // If the application does not have the permission, ask the user if he's willing to grant it.
            String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
            ActivityCompat.requestPermissions(PostTenderActivity.this,
                    permissions, LOCATION_PERMISSION_REQUEST_CODE);
            return false;
        }
        return true;
    }

    private void showDatePickerDialog() {
        DatePickerDialogFragment datePickerFragment = new DatePickerDialogFragment();

        Bundle bundle = new Bundle();
        Date deadlineDate = mTenderRequest.getDeadlineDate();
        if (mTenderRequest != null && deadlineDate != null) {
            bundle.putSerializable("date", deadlineDate);
            datePickerFragment.setArguments(bundle);
        }

        datePickerFragment.setOnDateSelectedListener(this);
        datePickerFragment.show(getSupportFragmentManager(),
                "deadline_date_picker");
    }

    private void setupStatusRadioGroup() {
        mTenderOpenedStatusRadioButton = (RadioButton) findViewById(
                R.id.tender_status_opened_radio_button);
        mTenderOpenedStatusRadioButton.setOnCheckedChangeListener(this);
        mTenderClosedStatusRadioButton = (RadioButton) findViewById(
                R.id.tender_status_closed_radio_button);
        mTenderClosedStatusRadioButton.setOnCheckedChangeListener(this);
        mTenderResolvedStatusRadioButton = (RadioButton) findViewById(
                R.id.tender_status_resolved_radio_button);
        mTenderResolvedStatusRadioButton.setOnCheckedChangeListener(this);

        if (mTenderRequest != null) {
            updateRadioGroup();
        }
    }

    private void updateRadioGroup() {
        switch (mTenderRequest.getStatus()) {
            case OPENED:
                mTenderOpenedStatusRadioButton.setChecked(true);
                break;
            case CLOSED:
                mTenderClosedStatusRadioButton.setChecked(true);
                break;
            case RESOLVED:
                mTenderResolvedStatusRadioButton.setChecked(true);
                break;
        }
    }

    private boolean checkFields() {
        boolean isLocationSet = mTenderRequest.getLocation() != null
                && !mTenderRequest.getLocation().isEmpty();
        boolean isServicesTextField = !mPriceEditText.getText().toString().trim().isEmpty();
        boolean isDeadlineSet = mTenderRequest.getDeadlineDate() != null;

        if (isLocationSet && isServicesTextField && isDeadlineSet)
            return true;

        return false;
    }

    private void showWorkTypesSelectionDialog() {
        WorkTypesFragment workTypesFragment = WorkTypesFragment.getInstance(false,
                mTenderRequest.getSubWorkTypes());
        workTypesFragment.setOnWorkTypesSelectedListener(this);
        Utils.showDialogFragment(getSupportFragmentManager(), workTypesFragment,
                "work_type_fragment");
    }


}
