package il.co.tel_ran.carservice;

import android.os.AsyncTask;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by maxim on 9/29/2016.
 */

public class ServerConnection {

    private FindServicesTask mFindServicesTask;

    private static final String SERVICES_URL = "https://api.myjson.com/bins/2dkp8";

    public interface OnServicesRetrievedListener {
        void onServicesRetrievingStarted();
        void onServicesRetrieved(List<ServiceSearchResult> searchResults);
    }

    public void findServices(ServiceSearchQuery searchQuery, GoogleApiClient googleApiClient,
                             OnServicesRetrievedListener listener) {
        cancelServiceSearchTask();
        mFindServicesTask = new FindServicesTask(googleApiClient, listener);
        mFindServicesTask.execute(searchQuery);
    }

    public void cancelServiceSearchTask() {
        if (mFindServicesTask != null && mFindServicesTask.getStatus() == AsyncTask.Status.RUNNING)
            mFindServicesTask.cancel(true);
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
            HttpHandler httpHandler = new HttpHandler();
            // Get JSON file from server
            String jsonFile = httpHandler.makeServiceCall(SERVICES_URL);

            List<ServiceSearchResult> searchResults = new ArrayList<>();
            try {
                JSONObject servicesJSONObject = new JSONObject(jsonFile);
                JSONArray servicesJSONArray = servicesJSONObject.getJSONArray("services");

                if (servicesJSONArray != null) {
                    for (int i = 0; i < servicesJSONArray.length(); i++) {
                        // Periodically check if the task was canceled.
                        if (isCancelled())
                            break;

                        JSONObject object = servicesJSONArray.getJSONObject(i);

                        // Get service data.
                        String name = object.getString("serviceName");
                        float avgRating = (float) object.getDouble("avgRating");
                        int submittedRating = object.getInt("subRating");
                        int availableServices = object.getInt("availServices");
                        String placeID = object.getString("addressPlaceId");
                        String phoneNumber = object.getString("phoneNumber");
                        String email = object.getString("email");

                        // Convert ID to place
                        PendingResult<PlaceBuffer> places_buffer = Places.GeoDataApi
                                .getPlaceById(mGoogleApiClient, placeID);
                        PlaceBuffer placeBuffer = places_buffer.await();
                        // Object must be frozen if we want to use it after the buffer is released.
                        Place place = placeBuffer.get(0).freeze();
                        placeBuffer.release();

                        // Parse the city name from given address (to hide information from non-registered users)
                        String cityName = Utils.parseCityNameFromAddress(
                                place.getAddress());

                        ServiceStation resultServiceStation = new ServiceStation(name, place, avgRating, submittedRating,
                                ServiceType.decode(availableServices),
                                cityName, phoneNumber, email);
                        ServiceSearchResult searchResult = new ServiceSearchResult(resultServiceStation);

                        searchResults.add(searchResult);
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return filterServices(params[0], searchResults);
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
            ServiceStation serviceStation = searchResult.getSerivce();
            boolean containsService = false;

            if (searchQuery.getAvailableServices().isEmpty()) {
                // Show all services.
                containsService = true;
            } else {
                for (ServiceType type : searchQuery.getAvailableServices()) {
                    if (serviceStation.getAvailableServices().contains(type)) {
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
                    if (serviceStation.getCityName().equals(location.getName())) {
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

    /*// TODO: Remove when switching to real results.
    private List<SampleRawResult> getSampleRawResults() {
        List<SampleRawResult> sampleRawResults = new ArrayList<>();

        sampleRawResults.add(new SampleRawResult("Sample ServiceStation 1", null,
                randFloat(5.0f, 0.5f), randInt(100, 1),
                EnumSet.of(ServiceType.SERVICE_TYPE_TUNING, ServiceType.SERVICE_TYPE_TYRE_REPAIR),
                "ChIJ73TZ6KRMHRURfT9T61-w7QY", "0847164955", "sampleservice1@gmail.com"));
        sampleRawResults.add(new SampleRawResult("Sample ServiceStation 2", null,
                randFloat(5.0f, 0.5f), randInt(100, 1),
                EnumSet.of(ServiceType.SERVICE_TYPE_TYRE_REPAIR),
                "ChIJnSco919LHRURg2ZruWtsDmg", "0847648955", "sampleservice2@gmail.com"));
        sampleRawResults.add(new SampleRawResult("Sample ServiceStation 3", null,
                randFloat(5.0f, 0.5f), randInt(100, 1),
                EnumSet.of(ServiceType.SERVICE_TYPE_AC_REPAIR_REFILL,
                        ServiceType.SERVICE_TYPE_TUNING),
                "ChIJzfEJQkCjAhURl70lo2y4SxA", "0847163335", "sampleservice3@gmail.com"));
        sampleRawResults.add(new SampleRawResult("Sample ServiceStation 4", null,
                randFloat(5.0f, 0.5f), randInt(100, 1),
                EnumSet.of(ServiceType.SERVICE_TYPE_AC_REPAIR_REFILL,
                        ServiceType.SERVICE_TYPE_TYRE_REPAIR),
                "ChIJrbITsnBLHRURxOktiI37Yn4", "083794955", "sampleservice4@gmail.com"));
        sampleRawResults.add(new SampleRawResult("Sample ServiceStation 5", null,
                randFloat(5.0f, 0.5f), randInt(100, 1),
                EnumSet.of(ServiceType.SERVICE_TYPE_TYRE_REPAIR), "ChIJpxhWFYNAHRURmP31dRn87zA",
                "0847122455", "sampleservice5@gmail.com"));
        sampleRawResults.add(new SampleRawResult("Sample ServiceStation 6", null,
                randFloat(5.0f, 0.5f), randInt(100, 1),
                EnumSet.of(ServiceType.SERVICE_TYPE_CAR_WASH, ServiceType.SERVICE_TYPE_TUNING),
                "ChIJmckm3fVLHRUR0DtEhUiQGhA", "0847197555", "sampleservice7@gmail.com"));
        sampleRawResults.add(new SampleRawResult("Sample ServiceStation 7", null,
                randFloat(5.0f, 0.5f), randInt(100, 1),
                EnumSet.of(ServiceType.SERVICE_TYPE_AC_REPAIR_REFILL,
                        ServiceType.SERVICE_TYPE_CAR_WASH, ServiceType.SERVICE_TYPE_TUNING,
                        ServiceType.SERVICE_TYPE_TYRE_REPAIR),
                "ChIJSRHA_51LHRUR15zBsFayP_U", "0847962155", "sampleservice7@gmail.com"));

        return sampleRawResults;
    }

    // TODO: Remove when switching to real results.
    private int randInt(int max, int min) {
        return new Random().nextInt((max - min) + 1) + min;
    }

    // TODO: Remove when switching to real results.
    private float randFloat(float max, float min) {
        return new Random().nextFloat() * (max - min) + min;
    }

    // TODO: Remove when switching to real results.
    private class SampleRawResult extends ServiceSearchResult {

        private String mPlaceId;

        public SampleRawResult(String name, Place address, float avgRating, int submittedRatings,
                               EnumSet<ServiceType> availableServices, String placeId,
                               String phonenumber, String email) {
            super(name, address, avgRating, submittedRatings, availableServices, null,
                    phonenumber, email);
            mPlaceId = placeId;
        }

        public String getPlaceId() {
            return mPlaceId;
        }
    }*/
}
