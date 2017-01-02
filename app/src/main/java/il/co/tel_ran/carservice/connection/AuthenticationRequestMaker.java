package il.co.tel_ran.carservice.connection;

import android.content.Context;
import android.os.Bundle;

import com.android.volley.Request;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by maxim on 30-Dec-16.
 */

public class AuthenticationRequestMaker extends RequestMaker {

    public static final String JSON_FIELD_ID = "id";
    public static final String JSON_FIELD_IDUSER = "idUser";
    public static final String JSON_FIELD_EMAIL = "email";
    public static final String JSON_FIELD_PASSWORD = "password";
    public static final String JSON_FIELD_TYPE = "role";
    public static final String JSON_FIELD_TYPE_MASTER = "master";
    public static final String JSON_FIELD_TYPE_CLIENT = "client";
    public static final String JSON_FIELD_CREATE_DATETIME = "createdAt";
    public static final String JSON_FIELD_UPDATE_DATETIME = "updatedAt";
    public static final String JSON_FIELD_PASS = "pass";

    public AuthenticationRequestMaker(OnDataRetrieveListener listener) {
        super(listener, DataResult.Type.AUTHENTICATION);
    }

    @Override
    public void makeRequest(Context context, DataRequest dataRequest) {
        Request request;

        switch (dataRequest.getRequestMethod()) {
            case Request.Method.POST:
                // FALLTHROUGH
            case Request.Method.PUT:
                // Make JSON requests for POST and PUT methods.
                request = makeJSONObjectRequest(dataRequest);
                break;
            case Request.Method.GET:
                // FALLTHROUGH
            default:
                request = makeStringRequest(dataRequest);
                break;
        }

        // Add request to queue
        RequestQueueSingleton.getInstance(context).addToRequestQueue(request);
    }

    @Override
    protected void handleResponse(DataRequest request, JSONObject response) {
        OnDataRetrieveListener listener = getListener();
        if (listener != null) {
            if (response == null) {
                // Pass null message for the error message to be handled elsewhere.
                listener.onDataRetrieveFailed(request, getResultType(),
                        ServerResponseError.INVALID_PARAMETER, null);
            } else {

                JSONObject resultJSON = new JSONObject();
                try {
                    resultJSON.put(AuthenticationRequestMaker.JSON_FIELD_EMAIL,
                            response.getString(AuthenticationRequestMaker.JSON_FIELD_EMAIL));
                    resultJSON.put(JSON_FIELD_PASS,
                            response.getString(AuthenticationRequestMaker.JSON_FIELD_PASSWORD));

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                JSONObject[] resultsArray = {resultJSON};
                AuthenticationDataResult result = new AuthenticationDataResult(resultsArray);

                Bundle extras = new Bundle();
                extras.putString("user_type",
                        response.optString(AuthenticationRequestMaker.JSON_FIELD_TYPE));

                listener.onDataRetrieveSuccess(request, result);
            }
        }
    }

    @Override
    protected void handleResponse(DataRequest request, String response) {
        if (response == null || response.isEmpty()) {
            OnDataRetrieveListener listener = getListener();
            if (listener != null) {
                // Pass null message for the error message to be handled elsewhere.
                listener.onDataRetrieveFailed(request, getResultType(),
                        ServerResponseError.INCORRECT_EMAIL, null);
            }
        } else {
            try {
                handleResponse(request, new JSONArray(response).getJSONObject(0));
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void handleResponse(DataRequest request, JSONArray response) {
        // Not used in this request maker.
    }
}
