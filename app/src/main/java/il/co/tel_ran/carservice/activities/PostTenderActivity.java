package il.co.tel_ran.carservice.activities;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ScrollingView;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
import com.google.android.gms.location.places.PlaceFilter;
import com.google.android.gms.location.places.PlaceLikelihood;
import com.google.android.gms.location.places.PlaceLikelihoodBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;

import il.co.tel_ran.carservice.R;
import il.co.tel_ran.carservice.TenderRequest;
import il.co.tel_ran.carservice.Utils;
import il.co.tel_ran.carservice.VehicleData;

public class PostTenderActivity extends AppCompatActivity implements View.OnClickListener, GoogleApiClient.OnConnectionFailedListener {

    private static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 2;

    private ScrollView mLayout;

    private TenderRequest mTenderRequest;

    private Button mSearchLocationButton;
    private ImageView mCurrentLocationImageView;
    private ProgressBar mAcquireLocationProgressbar;

    private EditText mServicesEditText;

    private GoogleApiClient mGoogleApiClient;

    private TextView mThirdPartyAttributionsTextView;

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
                break;
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

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

    }

    public void postRequest(View view) {
        Intent intent = new Intent();

        if (mTenderRequest != null) {
            mTenderRequest.setServices(mServicesEditText.getText().toString());

            intent.putExtra("tender_request", mTenderRequest);
        }

        setResult(RESULT_OK, intent);
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_tender);

        Bundle extras = getIntent().getExtras();
        if (extras != null && !extras.isEmpty()) {
            mTenderRequest = (TenderRequest) extras.getSerializable("tender_request");
        } else {
            mTenderRequest = new TenderRequest();
        }

        mLayout = (ScrollView) findViewById(R.id.activity_post_tender);

        TextView vehicleDetailsTextView = (TextView) findViewById(R.id.vehicle_details_text_view);

        // Check if we are updating an existing tender request.
        VehicleData vehicleData = mTenderRequest.getVehicleData();
        if (vehicleData != null) {
            vehicleDetailsTextView.setText(vehicleData.toString());
        }

        mAcquireLocationProgressbar = (ProgressBar) findViewById(R.id.acquire_location_progress_bar);

        mServicesEditText = (EditText) findViewById(R.id.tender_services_edit_text);
        // Check if we are updating an existing tender request.
        String message = mTenderRequest.getServices();
        if (message != null && !message.isEmpty()) {
            mServicesEditText.setText(message);
        }

        mThirdPartyAttributionsTextView = (TextView) findViewById(R.id.third_party_attributions_text_view);

        setupLocationSearchButton();
        // Check if we are updating an existing tender request.
        String location = mTenderRequest.getLocation();
        if (location != null && !location.isEmpty()) {
            mSearchLocationButton.setText(location);
        }

        setupActionBar();

        setupGoogleApiClient();
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
}
