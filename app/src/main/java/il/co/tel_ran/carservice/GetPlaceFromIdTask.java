package il.co.tel_ran.carservice;

import android.os.AsyncTask;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;

/**
 * Created by maxim on 12-Dec-16.
 */

public class GetPlaceFromIdTask extends AsyncTask<String, Void, Place> {

    private GoogleApiClient mGoogleApiClient;

    public GetPlaceFromIdTask(GoogleApiClient client) {
        mGoogleApiClient = client;
    }

    @Override
    protected Place doInBackground(String... params) {
        if (params.length != 1) {
            return null;
        }

        // Convert ID to place
        PendingResult<PlaceBuffer> places_buffer = Places.GeoDataApi
                .getPlaceById(mGoogleApiClient, params[0]);
        PlaceBuffer placeBuffer = places_buffer.await();
        // Object must be frozen if we want to use it after the buffer is released.
        Place place = placeBuffer.get(0).freeze();
        placeBuffer.release();

        return place;
    }
}
