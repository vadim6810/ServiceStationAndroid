package il.co.tel_ran.carservice;

import android.graphics.Bitmap;
import android.os.AsyncTask;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.places.PlacePhotoMetadata;
import com.google.android.gms.location.places.PlacePhotoMetadataBuffer;
import com.google.android.gms.location.places.PlacePhotoMetadataResult;
import com.google.android.gms.location.places.Places;

/**
 * Created by maxim on 9/30/2016.
 */

public class LoadPlacePhotoTask
        extends AsyncTask<String, Void, Bitmap> {

    private GoogleApiClient mGoogleApiClient;

    private int mWidth;
    private int mHeight;

    public LoadPlacePhotoTask(GoogleApiClient client, int imageWidth, int imageHeight) {
        mGoogleApiClient = client;
        mWidth           = imageWidth;
        mHeight          = imageHeight;
    }

    @Override
    protected Bitmap doInBackground(String... params) {
        if (params.length != 1) {
            return null;
        }

        final String placeId = params[0];
        Bitmap bitmapPhoto = null;

        PlacePhotoMetadataResult result = Places.GeoDataApi
                .getPlacePhotos(mGoogleApiClient, placeId).await();

        if (result.getStatus().isSuccess()) {
            PlacePhotoMetadataBuffer photoMetadataBuffer = result.getPhotoMetadata();
            if (photoMetadataBuffer.getCount() > 0 && !isCancelled()) {
                // Get the first bitmap and its attributions.
                PlacePhotoMetadata photo = photoMetadataBuffer.get(0);
                CharSequence attribution = photo.getAttributions();
                // Load a scaled bitmap for this photo.
                bitmapPhoto = photo.getScaledPhoto(mGoogleApiClient, mWidth, mHeight).await()
                        .getBitmap();
            }
            // Release the PlacePhotoMetadataBuffer.
            photoMetadataBuffer.release();
        }
        return bitmapPhoto;
    }
}
