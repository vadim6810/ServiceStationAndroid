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

    private GetProviderInboxMessagesTask mGetProviderInboxMessagesTask;

    public static final String ROOT_URL = "https://secure-citadel-93919.herokuapp.com";
//    public static final String ROOT_URL = "http://10.0.2.2:3001";
    public static final String MASTERS_URL = ROOT_URL + "/api/masters";
    public static final String CLIENTS_URL = ROOT_URL + "/api/clients";
    public static final String COMMENTS_URL = ROOT_URL + "/api/comments";
    public static final String VEHICLE_API_URL = ROOT_URL + "/api/carmodelsapi";
    public static final String MESSAGES_URL = ROOT_URL + "/sendmessages";
    public static final String TENDERS_URL = ROOT_URL + "/api/tenders";
    public static final String AUTHENTICATE_URL = ROOT_URL + "/api/authentic";

    private static final String PROVIDER_INBOX_MESSAGES_URL = "https://jsonblob.com/api/jsonBlob/dd1044ea-b726-11e6-871b-7523749b294d";

    private static final String POST_GET_MECHANICS_BY_ALL = "/getmechanicsbyall";
    private static final String POST_GET_MOUNTING_BY_ALL = "/getmountingbyall";
    private static final String POST_GET_CAR_WASH_BY_ALL = "/getcarwashbyall";
    private static final String POST_GET_TOW_TRUCK_BY_ALL = "/gettowtruckbyall";

    private static final String POST_COMMENTS = "/comments";
    
    public interface OnTenderRequestsRetrievedListener {
        void onTenderRequestRetrievingStarted();
        void onTenderRequestRetrieved(List<TenderRequest> tenderRequests);
    }

    public interface OnProviderInboxMessagesRetrievedListener {
        void onProviderInboxMessagesRetrievingStarted();
        void onProviderInboxMessagesRetrieved(List<InboxMessage> inboxMessages);
    }

    // TODO: when back-end available add id parameter to fetch for specific service
    public void getProviderInboxMessages(OnProviderInboxMessagesRetrievedListener listener) {
        cancelGetProviderInboxMessagesTask();
        mGetProviderInboxMessagesTask = new GetProviderInboxMessagesTask(listener);
        mGetProviderInboxMessagesTask.execute();
    }

    public void cancelGetProviderInboxMessagesTask() {
        if (mGetProviderInboxMessagesTask != null && mGetProviderInboxMessagesTask.getStatus()
                == AsyncTask.Status.RUNNING)
            mGetProviderInboxMessagesTask.cancel(true);
    }

    public void cancelAllTasks() {
        cancelGetProviderInboxMessagesTask();
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
