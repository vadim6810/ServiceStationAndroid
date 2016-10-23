package il.co.tel_ran.carservice;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import il.co.tel_ran.carservice.HttpHandler;

/**
 * Created by maxim on 10/23/2016.
 */

public class VehicleAPI {

    public static final String JSON_BASE_URL =  "http://casco.cmios.ru/api";
    public static final String JSON_MAKE_API = "/cars/";

    private OnVehicleDataRetrieveListener mListener;

    // This map keeps track of the current running tasks.
    // We allow one task per request type (there could be a need to retrieve all three parallel).
    private Map<RequestType, GetVehicleDataTask> mTasksMap;

    public VehicleAPI(OnVehicleDataRetrieveListener listener) {
        mListener = listener;

        mTasksMap = new HashMap<>();
    }

    public void getVehicleData(Request request) {
        GetVehicleDataTask newTask = new GetVehicleDataTask(request.getType());

        // If there is a previous value for this key it will be returned when applying new value.
        // We get the previous task and apply the new one for the same key.
        GetVehicleDataTask previousTask = mTasksMap.put(request.getType(), newTask);
        // If we did have a previous task, make sure to cancel it.
        if (previousTask != null && previousTask.getStatus() == AsyncTask.Status.RUNNING) {
            previousTask.cancel(true);
        }

        // Start the async task.
        newTask.execute(request);
    }

    public interface OnVehicleDataRetrieveListener {
        void onVehicleDataRetrievingStarted(RequestType requestType);
        void onVehicleDataRetrieved(RequestType requestType, List<Result> results);
    }

    public class GetVehicleDataTask extends AsyncTask<Request, Void, List<Result>> {

        private RequestType mRequestType;

        public GetVehicleDataTask(RequestType type) {
            mRequestType = type;
        }

        @Override
        protected List<Result> doInBackground(Request... params) {

            // First (and only) parameter is the request.
            Request request = params[0];
            String requestURL = request.getURL();
            mRequestType = request.getType();

            List<Result> results = new ArrayList<>();
            try {
                JSONArray jsonArray = null;
                JSONObject jsonObject;
                switch (mRequestType) {
                    case MAKE:
                        // Get JSON Array of vehicle makes.
                        jsonArray = new JSONArray(parseURL(requestURL));
                        break;
                    case MODEL:
                        // Get JSON Object which contains models array.
                        jsonObject = new JSONObject(parseURL(requestURL));
                        jsonArray = jsonObject.getJSONArray("models");
                        break;
                    case MODIFICATION:
                        // Get JSON Object which contains modifications (engine dispalcement) array.
                        jsonObject = new JSONObject(parseURL(requestURL));
                        jsonArray = jsonObject.getJSONArray("modifications");
                        break;
                }

                String extraURL = "";
                for (int i = 0; i < jsonArray.length(); i++) {
                    // Periodically check if the task was canceled.
                    if (isCancelled())
                        break;

                    JSONObject object = jsonArray.getJSONObject(i);
                    // Get "title" which is a string representation of the data we require (make/model/modification)
                    String title = object.optString("title");

                    // Modification request doesn't contain url.
                    if (request.getType() != RequestType.MODIFICATION) {
                        // Get "url" which is a url for additional data for this make/model.
                        extraURL = object.getString("url");
                    }

                    results.add(new Result(extraURL, title));
                }
            } catch (JSONException e) {
                e.printStackTrace();
                // TODO: handle errors.
            }

            return results;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Invoke the callback on the implementing interface informing retrieving has started.
            mListener.onVehicleDataRetrievingStarted(mRequestType);
        }

        @Override
        protected void onPostExecute(List<Result> results) {
            super.onPostExecute(results);
            // Invoke the callback on the implementing interface informing retrieving has ended.
            mListener.onVehicleDataRetrieved(mRequestType, results);
        }

        private String parseURL(String url) {
            // Parse vehicle data api url to get a JSON input.
            HttpHandler httpHandler = new HttpHandler();
            return httpHandler.makeServiceCall(url);
        }
    }

    public static class Result {
        private String mExtraURL;
        private String mResult;

        public Result(String url, String result) {
            mExtraURL    = url;
            mResult      = result;
        }

        // Extra URL for additional information about this make/model.
        public String getExtraURL() {
            return mExtraURL;
        }

        // String describing the results (make name, model name or engine displacement).
        public String getResult() {
            return mResult;
        }
    }

    public static class Request {
        RequestType mRequestType;
        String mURL;

        public Request(RequestType requestType, String url) {
            mRequestType = requestType;
            mURL = url;
        }

        public RequestType getType() {
            return mRequestType;
        }

        public String getURL() {
            return mURL;
        }
    }

    public enum RequestType {
        MAKE,
        MODEL,
        MODIFICATION
    }
}
