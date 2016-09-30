package il.co.tel_ran.carservice;

import android.os.AsyncTask;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Random;

/**
 * Created by maxim on 9/29/2016.
 */

public class ServerConnection {

    private FindServicesTask findServicesTask;

    public interface OnServicesRetrievedListener {
        void onServicesRetrievingStarted();
        void onServicesRetrieved(List<ServiceSearchResult> searchResults);
    }

    public void findServices(ServiceSearchQuery searchQuery, GoogleApiClient googleApiClient,
                             OnServicesRetrievedListener listener) {
        cancelServiceSearchTask();
        findServicesTask = new FindServicesTask(googleApiClient, listener);
        findServicesTask.execute(searchQuery);
    }

    public void cancelServiceSearchTask() {
        if (findServicesTask != null && findServicesTask.getStatus() == AsyncTask.Status.RUNNING)
            findServicesTask.cancel(true);
    }

    public void cancelAllTasks() {
        cancelServiceSearchTask();
    }

    private class FindServicesTask extends AsyncTask<ServiceSearchQuery, Integer, List<ServiceSearchResult>> {

        private GoogleApiClient mGoogleApiClient;
        private OnServicesRetrievedListener mListener;

        public FindServicesTask(GoogleApiClient googleApiClient,
                                OnServicesRetrievedListener listener) {
            mGoogleApiClient    = googleApiClient;
            mListener           = listener;
        }

        @Override
        protected List<ServiceSearchResult> doInBackground(ServiceSearchQuery... params) {

            // TODO: Remove when switching to real results.
            List<SampleRawResult> sampleRawResults = getSampleRawResults();

            String[] placeIds = new String[sampleRawResults.size()];
            for (int i = 0; i < sampleRawResults.size(); i ++) {
                if (isCancelled())
                    return new ArrayList<>();

                placeIds[i] = sampleRawResults.get(i).getPlaceId();
            }

            // Convert ID to place
            PendingResult<PlaceBuffer> places_buffer = Places.GeoDataApi
                    .getPlaceById(mGoogleApiClient, placeIds);
            PlaceBuffer places = places_buffer.await();

            if (isCancelled())
                return new ArrayList<>();

            List<ServiceSearchResult> results = new ArrayList<>();
            results.addAll(sampleRawResults);

            for (int i = 0; i < results.size(); i ++) {
                if (isCancelled())
                    return new ArrayList<>();

                ServiceSearchResult searchResult = results.get(i);
                // Place objects retrieved from place buffer must be frozen.
                // Refer to: https://developers.google.com/places/android-api/buffers
                searchResult.setLocation(places.get(i).freeze());
                searchResult.setCityName(Utils.parseCityNameFromAddress(
                        searchResult.getLocation().getAddress()));
            }

            places.release();
            return filterServices(params[0], results);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mListener.onServicesRetrievingStarted();
        }

        @Override
        protected void onPostExecute(List<ServiceSearchResult> serviceSearchResults) {
            super.onPostExecute(serviceSearchResults);
            mListener.onServicesRetrieved(serviceSearchResults);
        }
    }

    // NOTE: the following methods/classes are for sample and demonstration purpose only.
    // Will be removed once we are connected to backend.

    // TODO: Remove when switching to real results.

    // Filtering should be done on back-end.
    private List<ServiceSearchResult> filterServices(ServiceSearchQuery searchQuery,
                                                     List<ServiceSearchResult> searchResults) {
        List<ServiceSearchResult> filteredResults = new ArrayList<>();

        for (ServiceSearchResult searchResult : searchResults) {
            boolean containsService = false;

            if (searchQuery.getAvailableServices().isEmpty()) {
                // Show all services.
                containsService = true;
            } else {
                for (ServiceType type : searchQuery.getAvailableServices()) {
                    if (searchResult.getAvailableServices().contains(type)) {
                        containsService = true;
                        break;
                    }
                }
            }

            if (!containsService)
                continue;

            boolean containsLocation = false;

            if (searchQuery.getLocations().isEmpty()) {
                // Show in all cities.
                containsLocation = true;
            } else {
                for (Place location : searchQuery.getLocations()) {
                    // Comparison is done by city because that's how the user filters it.
                    if (searchResult.getCityName().equals(location.getName())) {
                        containsLocation = true;
                        break;
                    }
                }
            }

            if (!containsLocation)
                continue;

            filteredResults.add(searchResult);
        }

        return filteredResults;
    }

    // TODO: Remove when switching to real results.
    private List<SampleRawResult> getSampleRawResults() {
        List<SampleRawResult> sampleRawResults = new ArrayList<>();

        Random random = new Random();

        sampleRawResults.add(new SampleRawResult("Sample Service 1", null,
                random.nextFloat(), random.nextInt(),
                EnumSet.of(ServiceType.SERVICE_TYPE_AC_REPAIR_REFILL),
                "ChIJs5RXm2tmAhUR8KVchevUuh0"));
        sampleRawResults.add(new SampleRawResult("Sample Service 2", null,
                random.nextFloat(), random.nextInt(),
                EnumSet.of(ServiceType.SERVICE_TYPE_CAR_WASH),
                "ChIJU_6mLAFGAhUR0RKnIKBwupY"));
        sampleRawResults.add(new SampleRawResult("Sample Service 3", null,
                random.nextFloat(), random.nextInt(),
                EnumSet.of(ServiceType.SERVICE_TYPE_TUNING, ServiceType.SERVICE_TYPE_TYRE_REPAIR),
                "ChIJAa6V6yE_AhURjyBESg85Ogk"));
        sampleRawResults.add(new SampleRawResult("Sample Service 4", null,
                random.nextFloat(), random.nextInt(),
                EnumSet.of(ServiceType.SERVICE_TYPE_AC_REPAIR_REFILL,
                        ServiceType.SERVICE_TYPE_TYRE_REPAIR),
                "ChIJzXfCp-S2AhURqjtizUhdRe4"));
        sampleRawResults.add(new SampleRawResult("Sample Service 5", null,
                random.nextFloat(), random.nextInt(),
                EnumSet.of(ServiceType.SERVICE_TYPE_CAR_WASH), "ChIJS_zBNNbXAhURy-FuRT5ib9k"));
        sampleRawResults.add(new SampleRawResult("Sample Service 6", null,
                random.nextFloat(), random.nextInt(),
                EnumSet.of(ServiceType.SERVICE_TYPE_CAR_WASH, ServiceType.SERVICE_TYPE_TUNING),
                "ChIJC-qoAAIAAxURl8EhejtlMgI"));
        sampleRawResults.add(new SampleRawResult("Sample Service 7", null,
                random.nextFloat(), random.nextInt(),
                EnumSet.of(ServiceType.SERVICE_TYPE_AC_REPAIR_REFILL,
                        ServiceType.SERVICE_TYPE_CAR_WASH, ServiceType.SERVICE_TYPE_TUNING,
                        ServiceType.SERVICE_TYPE_TYRE_REPAIR),
                "ChIJ4UgSuMNlAhURSA3oVGl0VIM"));

        return sampleRawResults;
    }

    // TODO: Remove when switching to real results.
    private class SampleRawResult extends ServiceSearchResult {

        private String mPlaceId;

        public SampleRawResult(String name, Place address, float avgRating, int submittedRatings,
                               EnumSet<ServiceType> availableServices, String placeId) {
            super(name, address, avgRating, submittedRatings, availableServices, null);
            mPlaceId = placeId;
        }

        public String getPlaceId() {
            return mPlaceId;
        }
    }
}
