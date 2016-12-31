package il.co.tel_ran.carservice.connection;

import android.content.Context;
import android.os.AsyncTask;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import il.co.tel_ran.carservice.InboxMessage;
import il.co.tel_ran.carservice.ServiceType;
import il.co.tel_ran.carservice.TenderReply;
import il.co.tel_ran.carservice.TenderRequest;
import il.co.tel_ran.carservice.Utils;
import il.co.tel_ran.carservice.VehicleData;

/**
 * Created by maxim on 9/29/2016.
 */

public class ServerConnection {

    public static final int MAX_TIMEOUT_MS = 30000;
    
    private GetTenderRequestsTask mGetTenderRequestsTask;

    private GetProviderInboxMessagesTask mGetProviderInboxMessagesTask;

    public static final String ROOT_URL = "https://secure-citadel-93919.herokuapp.com";
//    public static final String ROOT_URL = "http://10.0.2.2:3001";
    public static final String MASTERS_URL = ROOT_URL + "/api/masters";
    public static final String CLIENTS_URL = ROOT_URL + "/api/clients";
    public static final String COMMENTS_URL = ROOT_URL + "/api/comments";
    public static final String VEHICLE_API_URL = ROOT_URL + "/api/carmodelsapi";
    public static final String MESSAGES_URL = ROOT_URL + "/sendmessages";

    public static final String AUTHENTICATE_URL = ROOT_URL + "/api/authentic";

    private static final String RESPOND_SERVICES_URL = "https://jsonblob.com/api/jsonBlob/58313c83e4b0a828bd27ae5d";

    private static final String REQUEST_TENDER_URL = "https://jsonblob.com/api/jsonBlob/8d29361b-b3cb-11e6-871b-771d67f790ab";

    private static final String PROVIDER_INBOX_MESSAGES_URL = "https://jsonblob.com/api/jsonBlob/dd1044ea-b726-11e6-871b-7523749b294d";

    private static final String POST_GET_MECHANICS_BY_ALL = "/getmechanicsbyall";
    private static final String POST_GET_MOUNTING_BY_ALL = "/getmountingbyall";
    private static final String POST_GET_CAR_WASH_BY_ALL = "/getcarwashbyall";
    private static final String POST_GET_TOW_TRUCK_BY_ALL = "/gettowtruckbyall";

    private static final String POST_COMMENTS = "/comments";
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
        cancelGetTenderRequestsTask();
        cancelGetProviderInboxMessagesTask();
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

    public static<T> void doHttpGet(Context context, String rootUrl, T parameter,
                                    Response.Listener<String> responseListener,
                                    Response.ErrorListener errorListener) {
        RequestQueue queue = Volley.newRequestQueue(context);

        String url = rootUrl + parameter.toString();

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                responseListener, errorListener);
        // Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    public static String getPostUrlForServiceType(ServiceType serviceType) {
        switch (serviceType) {
            case CAR_WASH:
                return ROOT_URL + POST_GET_CAR_WASH_BY_ALL;
            case TOWING:
                return ROOT_URL + POST_GET_TOW_TRUCK_BY_ALL;
            case TYRE_REPAIR:
                return ROOT_URL + POST_GET_MOUNTING_BY_ALL;
            case AUTO_SERVICE:
                return ROOT_URL + POST_GET_MECHANICS_BY_ALL;
        }

        return null;
    }

    public static String getPostUrlForComments() {
        return ROOT_URL + POST_COMMENTS;
    }
}
