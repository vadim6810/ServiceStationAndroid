package il.co.tel_ran.carservice;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
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

    private GetTenderRepliesTask mGetTenderRepliesTask;
    
    private GetTenderRequestsTask mGetTenderRequestsTask;

    private GetProviderInboxMessagesTask mGetProviderInboxMessagesTask;

//    private static final String MASTERS_URL = "https://api.myjson.com/bins/2545c";
    private static final String MASTERS_URL = "https://secure-citadel-93919.herokuapp.com/api/masters";
    private static final String CLIENTS_URL = "https://secure-citadel-93919.herokuapp.com/api/clients";

    private static final String AUTHENTICATE_URL ="https://secure-citadel-93919.herokuapp.com/api/authentic";

//    private static final String RESPOND_SERVICES_URL = "https://api.myjson.com/bins/5apnk";
    private static final String RESPOND_SERVICES_URL = "https://jsonblob.com/api/jsonBlob/58313c83e4b0a828bd27ae5d";

    private static final String REQUEST_TENDER_URL = "https://jsonblob.com/api/jsonBlob/8d29361b-b3cb-11e6-871b-771d67f790ab";

    private static final String PROVIDER_INBOX_MESSAGES_URL = "https://jsonblob.com/api/jsonBlob/dd1044ea-b726-11e6-871b-7523749b294d";

    public interface OnServicesRetrievedListener {
        void onServicesRetrievingStarted();
        void onServicesRetrieved(ServiceSearchResult searchResult);
    }

    public interface OnTenderRepliesRetrievedListener {
        void onTenderRepliesRetrievingStarted();
        void onTenderRepliesRetrieved(List<TenderReply> tenderReplies);
    }
    
    public interface OnTenderRequestsRetrievedListener {
        void onTenderRequestRetrievingStarted();
        void onTenderRequestRetrieved(List<TenderRequest> tenderRequests);
    }

    public interface OnProviderInboxMessagesRetrievedListener {
        void onProviderInboxMessagesRetrievingStarted();
        void onProviderInboxMessagesRetrieved(List<InboxMessage> inboxMessages);
    }


    public void findServices(ServiceSearchQuery searchQuery, GoogleApiClient googleApiClient,
                             OnServicesRetrievedListener listener) {
        cancelServiceSearchTask();
        mFindServicesTask = new FindServicesTask(googleApiClient, listener);
        mFindServicesTask.execute(searchQuery);
    }

    // TODO: When using real back-end the id for whom should be specified here.
    public void getTenderReplies(final GoogleApiClient googleApiClient,
                                 final OnTenderRepliesRetrievedListener listener) {
        if (googleApiClient != null) {
            cancelGetTenderRepliesTask();

            final ServiceSearchQuery searchQuery = new ServiceSearchQuery();
            findServices(searchQuery, googleApiClient, new OnServicesRetrievedListener() {
                @Override
                public void onServicesRetrievingStarted() {

                }

                @Override
                public void onServicesRetrieved(ServiceSearchResult searchResult) {
                    List<ServiceStation> services = searchResult.getSerivces();
                    ServiceStation[] servicesArray = new ServiceStation[services.size()];
                    for (int i = 0; i < services.size(); i++) {
                        servicesArray[i] = services.get(i);
                    }

                    mGetTenderRepliesTask = new GetTenderRepliesTask(listener);
                    mGetTenderRepliesTask.execute(servicesArray);
                }
            });
        }
    }

    // TODO: when back-end available add id/user parameter to fetch existing requests for users
    // TODO: when back-end available add service id parameter to update layout & communication for providers.
    public void getTenderRequests(OnTenderRequestsRetrievedListener listener) {
        cancelGetTenderRequestsTask();
        mGetTenderRequestsTask = new GetTenderRequestsTask(listener);
        mGetTenderRequestsTask.execute();
    }

    // TODO: when back-end available add id parameter to fetch for specific service
    public void getProviderInboxMessages(OnProviderInboxMessagesRetrievedListener listener) {
        cancelGetProviderInboxMessagesTask();
        mGetProviderInboxMessagesTask = new GetProviderInboxMessagesTask(listener);
        mGetProviderInboxMessagesTask.execute();
    }

    public void cancelServiceSearchTask() {
        if (mFindServicesTask != null && mFindServicesTask.getStatus() == AsyncTask.Status.RUNNING)
            mFindServicesTask.cancel(true);
    }

    public void cancelGetTenderRepliesTask() {
        if (mGetTenderRepliesTask != null && mGetTenderRepliesTask.getStatus()
                == AsyncTask.Status.RUNNING)
            mGetTenderRepliesTask.cancel(true);
    }
    
    public void cancelGetTenderRequestsTask() {
        if (mGetTenderRequestsTask != null && mGetTenderRequestsTask.getStatus()
                == AsyncTask.Status.RUNNING)
            mGetTenderRequestsTask.cancel(true);
    }

    public void cancelGetProviderInboxMessagesTask() {
        if (mGetProviderInboxMessagesTask != null && mGetProviderInboxMessagesTask.getStatus()
                == AsyncTask.Status.RUNNING)
            mGetProviderInboxMessagesTask.cancel(true);
    }

    public void cancelAllTasks() {
        cancelServiceSearchTask();
        cancelGetTenderRepliesTask();
        cancelGetTenderRequestsTask();
        cancelGetProviderInboxMessagesTask();
    }

    private class FindServicesTask extends AsyncTask<ServiceSearchQuery, Integer, ServiceSearchResult> {

        private GoogleApiClient mGoogleApiClient;
        private OnServicesRetrievedListener mListener;

        public FindServicesTask(GoogleApiClient googleApiClient,
                                OnServicesRetrievedListener listener) {
            mGoogleApiClient    = googleApiClient;
            mListener           = listener;
        }

        @Override
        protected ServiceSearchResult doInBackground(ServiceSearchQuery... params) {
            ServiceSearchResult searchResult = new ServiceSearchResult();

            String jsonFile = Utils.getJSONFileFromHttp(MASTERS_URL);
            if (jsonFile == null || jsonFile.isEmpty()) {
                // TODO: set error message in SearchResult
                return searchResult;
            }

            try {
                JSONArray servicesJSONArray = new JSONArray(jsonFile);
                for (int i = 0; i < servicesJSONArray.length(); i++) {
                    // Periodically check if task was canceled.
                    if (isCancelled())
                        break;

                    JSONObject serviceJSONObject = servicesJSONArray.getJSONObject(i);

                    ServiceStation serviceStation = parseMasterFromJSONObject(serviceJSONObject);

                    JSONObject chosenPlace = serviceJSONObject.getJSONObject("chosenPlace");
                    // Convert ID to place
                    PendingResult<PlaceBuffer> places_buffer = Places.GeoDataApi
                            .getPlaceById(mGoogleApiClient, serviceStation.getPlaceId());
                    PlaceBuffer placeBuffer = places_buffer.await();
                    // Object must be frozen if we want to use it after the buffer is released.
                    Place place = placeBuffer.get(0).freeze();
                    placeBuffer.release();

                    serviceStation.setLocation(place);
                    serviceStation.setCityName(Utils.parseCityNameFromAddress(
                            place.getAddress()));
                    searchResult.addService(serviceStation);
                }
            } catch (JSONException e) {
                e.printStackTrace();
                // TODO: set error message in SearchResult
            }

            return filterServices(params[0], searchResult);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mListener.onServicesRetrievingStarted();
        }

        @Override
        protected void onPostExecute(ServiceSearchResult serviceSearchResult) {
            super.onPostExecute(serviceSearchResult);
            mListener.onServicesRetrieved(serviceSearchResult);
        }
    }

    private class GetTenderRepliesTask extends AsyncTask<ServiceStation, Void,
            List<TenderReply>> {

        private final OnTenderRepliesRetrievedListener mListener;

        public GetTenderRepliesTask(OnTenderRepliesRetrievedListener listener) {
            mListener = listener;
        }

        @Override
        protected List<TenderReply> doInBackground(ServiceStation... params) {
            List<TenderReply> tenderReplies = new ArrayList<>();
            try {
                JSONArray repliesJSONArray = Utils.getJSONArrayFromHttp(
                        RESPOND_SERVICES_URL, "respond_services");

                if (repliesJSONArray != null) {
                    for (int i = 0; i < repliesJSONArray.length(); i++) {
                        // Periodically check if the task was canceled.
                        if (isCancelled())
                            break;

                        JSONObject object = repliesJSONArray.getJSONObject(i);

                        // Get reply data.
                        String message = object.getString("message");
                        long serviceID = object.getLong("service_id");

                        for (ServiceStation station : params) {
                            // Search for service with the correct ID
                            if (station.getID() == serviceID) {
                                // Add it to the result array list.
                                tenderReplies.add(new TenderReply(station, message));
                                break;
                            }
                        }
                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return tenderReplies;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (mListener != null)
                mListener.onTenderRepliesRetrievingStarted();
        }

        @Override
        protected void onPostExecute(List<TenderReply> tenderReplies) {
            super.onPostExecute(tenderReplies);
            if (mListener != null)
                mListener.onTenderRepliesRetrieved(tenderReplies);
        }
    }
    
    private class GetTenderRequestsTask extends AsyncTask<Void, Void, List<TenderRequest>> {
        
        private OnTenderRequestsRetrievedListener mListener;
        
        public GetTenderRequestsTask(OnTenderRequestsRetrievedListener listener) {
            mListener = listener;
        }

        @Override
        protected List<TenderRequest> doInBackground(Void... params) {
            List<TenderRequest> tenderRequests = new ArrayList<>();
            try {
                JSONArray requestsJSONArray = Utils.getJSONArrayFromHttp(
                        REQUEST_TENDER_URL, "requests");

                if (requestsJSONArray != null) {
                    for (int i = 0; i < requestsJSONArray.length(); i++) {
                        // Periodically check if the task was canceled.
                        if (isCancelled())
                            break;

                        JSONObject object = requestsJSONArray.getJSONObject(i);

                        // Get request data.
                        long id = object.getLong("id");
                        int statusInt = object.getInt("status");
                        String location = object.getString("location");
                        String placeId = object.getString("placeId");
                        String services = object.getString("services");
                        String vehicleMake = object.getString("vehicleMake");
                        String vehicleModel = object.getString("vehicleModel");
                        int vehicleYear = object.getInt("vehicleYear");
                        String vehicleModifications = object.getString("vehicleModifications");
                        long submitTimeStamp = object.getLong("submitTimeStamp");
                        long updateTimeStamp = object.getLong("updateTimeStamp");
                        int deadlineYear = object.getInt("deadlineYear");
                        int deadlineMonth = object.getInt("deadlineMonth");
                        int deadlineDay = object.getInt("deadlineDay");

                        VehicleData vehicleData = new VehicleData();
                        vehicleData.setVehicleMake(vehicleMake);
                        vehicleData.setVehicleModel(vehicleModel);
                        vehicleData.setVehicleYear(vehicleYear);
                        vehicleData.setVehicleModifications(vehicleModifications);

                        TenderRequest request = new TenderRequest();
                        request.setLocation(location);
                        request.setPlaceID(placeId);
                        request.setVehicleData(vehicleData);
                        request.setServices(services);
                        request.setDeadlineDate(deadlineDay, deadlineMonth, deadlineYear);
                        request.setStatus(statusInt == 0 ? TenderRequest.Status.CLOSED
                                : statusInt == 1 ? TenderRequest.Status.OPENED : TenderRequest.Status.RESOLVED);
                        request.setSubmitTimestamp(submitTimeStamp);
                        request.setUpdateTimestamp(updateTimeStamp);

                        tenderRequests.add(request);

                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return tenderRequests;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (mListener != null)
                mListener.onTenderRequestRetrievingStarted();
        }

        @Override
        protected void onPostExecute(List<TenderRequest> tenderRequests) {
            super.onPostExecute(tenderRequests);
            if (mListener != null)
                mListener.onTenderRequestRetrieved(tenderRequests);
        }
    }

    private class GetProviderInboxMessagesTask extends AsyncTask<Void, Void, List<InboxMessage>> {

        private OnProviderInboxMessagesRetrievedListener mListener;

        public GetProviderInboxMessagesTask(OnProviderInboxMessagesRetrievedListener listener) {
            mListener = listener;
        }

        @Override
        protected List<InboxMessage> doInBackground(Void... params) {
            List<InboxMessage> inboxMessages = new ArrayList<>();
            try {
                JSONArray requestsJSONArray = Utils.getJSONArrayFromHttp(
                        PROVIDER_INBOX_MESSAGES_URL, "provider_inbox_messages");

                if (requestsJSONArray != null) {
                    for (int i = 0; i < requestsJSONArray.length(); i++) {
                        // Periodically check if the task was canceled.
                        if (isCancelled())
                            break;

                        JSONObject object = requestsJSONArray.getJSONObject(i);

                        // Get inbox message data
                        long id = object.getLong("id");
                        int sourceTypeInt = object.getInt("sourceType");
                        long sourceId = object.getLong("sourceId");
                        String message = object.getString("message");
                        long timeStamp = object.getLong("submitTimestamp");

                        // TODO: when back-end available retrieve user from source id to match the title.
                        InboxMessage retrievedInboxMessage = new InboxMessage(id,
                                "Message from user", message, timeStamp, sourceId,
                                InboxMessage.Source.USER);

                        inboxMessages.add(retrievedInboxMessage);

                    }
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return inboxMessages;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (mListener != null)
                mListener.onProviderInboxMessagesRetrievingStarted();
        }

        @Override
        protected void onPostExecute(List<InboxMessage> inboxMessages) {
            super.onPostExecute(inboxMessages);
            if (mListener != null)
                mListener.onProviderInboxMessagesRetrieved(inboxMessages);
        }
    }

    // NOTE: the following methods/classes are for sample and demonstration purpose only.
    // Will be removed once we are connected to backend.

    // TODO: Remove when switching to real results.

    // Filtering should be done on back-end.
    private ServiceSearchResult filterServices(ServiceSearchQuery searchQuery,
                                                     ServiceSearchResult searchResult) {
        ServiceSearchResult filteredResult = new ServiceSearchResult();

        for (ServiceStation serviceStation : searchResult.getSerivces()) {
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

            filteredResult.addService(serviceStation);
        }

        return filteredResult;
    }

    public static void authenticateUser(Context context, String email,
                                        Response.Listener<String> responseListener,
                                        Response.ErrorListener errorListener) {
        doHttpGet(context, AUTHENTICATE_URL + "?email=", email, responseListener, errorListener);
    }

    public static void getMasterById(Context context, long id,
                                     Response.Listener<String> responseListener,
                                     Response.ErrorListener errorListener) {
        doHttpGet(context, MASTERS_URL + "?id=", id, responseListener, errorListener);
    }

    public static void getClientById(Context context, long id,
                                     Response.Listener<String> responseListener,
                                     Response.ErrorListener errorListener) {
        doHttpGet(context, CLIENTS_URL + "?id=", id, responseListener, errorListener);
    }

    public static ClientUser[] parseClientsFromResponse(String response) throws JSONException {
        JSONArray clientsJSONArray = new JSONArray(response);

        ClientUser[] users = new ClientUser[clientsJSONArray.length()];
        for (int i = 0; i < clientsJSONArray.length(); i++) {
            ClientUser user = parseClientFromJSONObject(clientsJSONArray.getJSONObject(i));
            users[i] = user;
        }

        return users;
    }

    private static ClientUser parseClientFromJSONObject(JSONObject mastersJSONObject) throws JSONException {
        long id = mastersJSONObject.getLong("id");
        String name = mastersJSONObject.getString("name");

        VehicleData[] cars = null;
        if (!mastersJSONObject.isNull("cars")) {
            JSONArray carsJSONArray = mastersJSONObject.getJSONArray("cars");
            cars = new VehicleData[carsJSONArray.length()];
            for (int j = 0; j < carsJSONArray.length(); j++) {
                Log.d("ServerConnection", "parseClientFromJSONObject:: string=" + carsJSONArray.getString(j));
                String[] carProperties = carsJSONArray.getString(j).split(",");

                cars[j] = new VehicleData();
                cars[j].setVehicleMake(carProperties[0]);
                cars[j].setVehicleModel(carProperties[1]);
                cars[j].setVehicleYear(Integer.parseInt(carProperties[2].trim()));
                cars[j].setVehicleModifications(carProperties[3]);
            }
        }

        ClientUser clientUser = new ClientUser();

        clientUser.setName(name);
        clientUser.setId(id);
        if (cars != null) {
            clientUser.setVehicleData(cars[0]); // For now support just one car.
        }

        return clientUser;
    }

    public static ServiceStation[] parseMastersFromResponse(String response) throws JSONException {
        JSONArray servicesJSONArray = new JSONArray(response);

        ServiceStation[] serviceStations = new ServiceStation[servicesJSONArray.length()];
        for (int i = 0; i < servicesJSONArray.length(); i++) {
            ServiceStation serviceStation = parseMasterFromJSONObject(servicesJSONArray.getJSONObject(i));
            serviceStations[i] = serviceStation;
        }

        return serviceStations;
    }

    private static ServiceStation parseMasterFromJSONObject(JSONObject mastersJSONObject) throws JSONException {
        long id = mastersJSONObject.getLong("id");
        String name = mastersJSONObject.getString("companyName");

        // TODO: update service types when back-end is updated.
        boolean mechanics = !mastersJSONObject.isNull("mechanics") && mastersJSONObject.getBoolean("mechanics");
        boolean mounting = !mastersJSONObject.isNull("mounting") && mastersJSONObject.getBoolean("mounting");
        boolean carWash = !mastersJSONObject.isNull("carWash") && mastersJSONObject.getBoolean("carWash");
        boolean towTruck = !mastersJSONObject.isNull("towTruck") && mastersJSONObject.getBoolean("towTruck");

        String companyPhonenumber = mastersJSONObject.getString("companyTelephone");
        String openTime = mastersJSONObject.getString("startTime");
        String closeTime = mastersJSONObject.getString("lastTime");
        String managerName = mastersJSONObject.getString("managerName");
        String managerPhonenumber = mastersJSONObject.getString("managerTelephone");
        String directorName = mastersJSONObject.getString("directorName");

        String[] servicedCarMakesArray = new String[0];
        if (!mastersJSONObject.isNull("cars")) {
            JSONArray servicedCarMakes = mastersJSONObject.getJSONArray("cars");
            servicedCarMakesArray = new String[servicedCarMakes.length()];
            for (int j = 0; j < servicedCarMakes.length(); j++) {
                servicedCarMakesArray[j] = servicedCarMakes.getString(j);
            }
        }

        JSONObject chosenPlace = mastersJSONObject.getJSONObject("chosenPlace");
        String placeId = chosenPlace.getString("PlaceId");

        float avgRating = (float) mastersJSONObject.getInt("rate");

        JSONObject carTypes = mastersJSONObject.getJSONObject("categories");

        ServiceStation serviceStation = new ServiceStation();
        serviceStation.setName(name);
        serviceStation.setID(id);
        serviceStation.setPlaceId(placeId);

        if (mastersJSONObject.has("FormattedAddress"))
            serviceStation.setCityName(mastersJSONObject.getString("FormattedAddress"));

        serviceStation.setOpeningTime(TimeHolder.parseTime(openTime));
        serviceStation.setClosingTime(TimeHolder.parseTime(closeTime));
        serviceStation.setAvgRating(avgRating);
        serviceStation.setDirectorName(directorName);
        serviceStation.setDirectorPhonenumber(managerPhonenumber);
        serviceStation.setManagerName(managerName);
        serviceStation.setPhonenumber(companyPhonenumber);
        serviceStation.setServicedCarMakes(servicedCarMakesArray);
        serviceStation.setSubmittedRatings(0); // TODO: update when back-end is updated

        serviceStation.toggleService(ServiceType.SERVICE_TYPE_CAR_WASH, carWash);
        serviceStation.toggleService(ServiceType.SERVICE_TYPE_AC_REPAIR_REFILL, mechanics); // TODO: Update to proper value when back-end is updated
        serviceStation.toggleService(ServiceType.SERVICE_TYPE_TUNING, towTruck); // TODO: Update to proper value when back-end is updated
        serviceStation.toggleService(ServiceType.SERVICE_TYPE_TYRE_REPAIR, mounting); // TODO: Update to proper value when back-end is updated

        serviceStation.toggleVehicleType(VehicleType.BUS, carTypes.has("bus") && carTypes.getBoolean("bus"));
        serviceStation.toggleVehicleType(VehicleType.PRIVATE, carTypes.has("passCar") && carTypes.getBoolean("passCar"));
        serviceStation.toggleVehicleType(VehicleType.MOTORCYCLE, carTypes.has("bicycle") && carTypes.getBoolean("bicycle"));
        serviceStation.toggleVehicleType(VehicleType.TRUCK, carTypes.has("lorry") && carTypes.getBoolean("lorry"));

        return serviceStation;
    }

    private static<T> void doHttpGet(Context context, String rootUrl, T parameter,
                                    Response.Listener<String> responseListener,
                                    Response.ErrorListener errorListener) {
        RequestQueue queue = Volley.newRequestQueue(context);

        String url = rootUrl + parameter.toString();
        Log.d("ServerConnection", "doHttpGet:: url=" + url);

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                responseListener, errorListener);
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
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
