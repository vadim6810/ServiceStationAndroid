package il.co.tel_ran.carservice.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.android.volley.Request;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.Places;

import il.co.tel_ran.carservice.LoadPlaceTask;
import il.co.tel_ran.carservice.ProviderUser;
import il.co.tel_ran.carservice.R;
import il.co.tel_ran.carservice.ServiceStation;
import il.co.tel_ran.carservice.connection.DataRequest;
import il.co.tel_ran.carservice.connection.DataResult;
import il.co.tel_ran.carservice.connection.NewProviderUserRequest;
import il.co.tel_ran.carservice.connection.NewUserRequestMaker;
import il.co.tel_ran.carservice.connection.RequestMaker;
import il.co.tel_ran.carservice.connection.ServerResponseError;
import il.co.tel_ran.carservice.fragments.RegistrationServiceDetailsFragment;

public class EditServiceDetailsActivity extends EditProfileInfoActivity
        implements RequestMaker.OnDataRetrieveListener, GoogleApiClient.OnConnectionFailedListener {

    private RegistrationServiceDetailsFragment mServiceDetailsFragment;

    private GoogleApiClient mGoogleApiClient;

    private ServiceStation mService;

    public EditServiceDetailsActivity() {
        super(R.layout.activity_update_service_details,
                R.string.service_details_title);
    }

    @Override
    public void onDataRetrieveSuccess(DataRequest dataRequest, DataResult result) {
        if (result.getDataType() == DataResult.Type.NEW_USER) {
            finishEditing(mServiceDetailsFragment.getService(), true);
        }
    }

    @Override
    public void onDataRetrieveFailed(DataRequest dataRequest, DataResult.Type resultType,
                                     ServerResponseError error, @Nullable String message) {
        finishWithError("onDataRetrieveFailed :: resultType=" + resultType
                + " error=" + error.toString() + " message=" + message);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            saveUserChanges();
        }
    }

    /*
     * GoogleApiClient.OnConnectionFailedListener
     */

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        finishWithError("onConnectionFailed :: Google API client connection failed.");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mServiceDetailsFragment
                = (RegistrationServiceDetailsFragment) getSupportFragmentManager()
                .findFragmentById(R.id.update_service_details_fragment);

        // Disable editing fields until we load Google Place object.
        mServiceDetailsFragment.toggleFields(false);

        setupGoogleApiClient();

        // intent and extras null is handled by super.
        mService = (ServiceStation) getIntent().getExtras().getSerializable("service");
        if (mService == null) {
            finishWithError("onCreate :: Service extra is null.");
        }

        // Make a copy of the service to make sure we don't edit the one we received from extras.
        // This will help with changes confirmation.
        mServiceDetailsFragment.setService(new ServiceStation(mService));
        // NOTE: we don't call fields update method because we are about to retrieve additional information
        // which will require updating fields again anyway.

        // We need to retrieve Place object for this service since it is transient and does not pass with extras.
        getGooglePlaceForServices(false);
    }

    @Override
    protected boolean isUserChanged() {
        // Make sure we retrieve all fields' data first.
        mServiceDetailsFragment.updateServiceFromFields();
        return !compareServices(mServiceDetailsFragment.getService(), mService);
    }

    @Override
    protected void saveUserChanges() {
        toggleProgressBar(true, mServiceDetailsFragment);

        // Build the request
        NewProviderUserRequest request = new NewProviderUserRequest((ProviderUser) mUser,
                mServiceDetailsFragment.getService());
        // Update request method.
        request.setRequestMethod(Request.Method.PUT);
        // Send the request.
        new NewUserRequestMaker(this).makeRequest(getApplicationContext(), request);
    }

    private boolean compareServices(ServiceStation service, ServiceStation compareService) {
        return service.equals(compareService);
    }

    private void finishEditing(ServiceStation serviceStation, boolean isChanged) {
        Intent intent = new Intent();
        intent.putExtra("edited_user", mUser);
        intent.putExtra("is_user_changed", false); // User object is not changed in this activity.
        intent.putExtra("edited_service", serviceStation);
        intent.putExtra("is_service_changed", isChanged);

        setResult(RESULT_OK, intent);
        finish();
    }

    private void setupGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient
                .Builder(this)
                .addApi(Places.GEO_DATA_API)
                .addApi(Places.PLACE_DETECTION_API)
                .enableAutoManage(this, this)
                .build();
    }

    private void getGooglePlaceForServices(final boolean disableFieldsOnPreExecute) {
        final ServiceStation serviceFromFragment = mServiceDetailsFragment.getService();
        if (mService != null && serviceFromFragment != null &&  mGoogleApiClient != null) {
            final String[] placeIds = new String[2];
            placeIds[0] = serviceFromFragment.getPlaceId();
            placeIds[1] = mService.getPlaceId();

            final String placeId = mService.getPlaceId();
            if (placeIds[0] != null && !placeIds[0].isEmpty()
                    && placeIds[1] != null && !placeIds[1].isEmpty()) {
                new LoadPlaceTask(mGoogleApiClient) {
                    @Override
                    protected void onPreExecute() {
                        if (disableFieldsOnPreExecute)
                            mServiceDetailsFragment.toggleFields(false);

                        toggleProgressBar(true, mServiceDetailsFragment);
                    }

                    @Override
                    protected void onPostExecute(Place[] places) {
                        if (places != null && places.length > 0) {
                            serviceFromFragment.setLocation(places[0]);
                            mService.setLocation(places[1]);
                        } else {
                            finishWithError("getGooglePlaceForServices :: onPostExecute :: places is null or empty.");
                        }

                        mServiceDetailsFragment.setFieldsFromService();

                        // Allow editing since we are done loading
                        mServiceDetailsFragment.toggleFields(true);

                        toggleProgressBar(false, mServiceDetailsFragment);
                    }
                }.execute(placeIds);
            }
        }
    }
}
