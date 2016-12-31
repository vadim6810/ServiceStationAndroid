package il.co.tel_ran.carservice;

import android.os.AsyncTask;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;

import java.util.concurrent.TimeUnit;

/**
 * Created by maxim on 9/30/2016.
 */

public class LoadPlaceTask
        extends AsyncTask<String, Void, Place[]> {

    private GoogleApiClient mGoogleApiClient;

    public LoadPlaceTask(GoogleApiClient client) {
        mGoogleApiClient = client;
    }

    @Override
    protected Place[] doInBackground(String... params) {
        if (params.length == 0) {
            return null;
        }

        Place[] places = new Place[params.length];

        // Convert Id to place
        PendingResult<PlaceBuffer> places_buffer = Places.GeoDataApi
                .getPlaceById(mGoogleApiClient, params);
        Log.d("LPT", "waiting");
        PlaceBuffer placeBuffer = places_buffer.await(10, TimeUnit.SECONDS);
        Log.d("LPT", "done");
        // Object must be frozen if we want to use it after the buffer is released.
        for (int i = 0; i < params.length; i++) {
            places[i] = placeBuffer.get(i).freeze();
        }
        placeBuffer.release();

        Log.d("LPT", "exiting");
        return places;
    }
}
