package il.co.tel_ran.carservice.connection;

import android.content.Context;

import com.android.volley.Request;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by maxim on 30-Dec-16.
 */

public class NewAuthenticationRequestMaker extends RequestMaker {

    private static final String JSON_FIELD_PASS = "pass";

    public NewAuthenticationRequestMaker(OnDataRetrieveListener listener) {
        super(listener, DataResult.Type.NEW_AUTHENTICATION);
    }

    @Override
    public void makeRequest(Context context, DataRequest dataRequest) {
        Request request;

        // Make JSON requests for POST method.
        request = makeJSONObjectRequest(dataRequest);

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
                    resultJSON.put(UserAuthentication.JSON_FIELD_EMAIL,
                            response.getString(UserAuthentication.JSON_FIELD_EMAIL));
                    resultJSON.put(JSON_FIELD_PASS,
                            response.getString(UserAuthentication.JSON_FIELD_PASSWORD));

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                JSONObject[] resultsArray = {resultJSON};
                NewAuthenticationDataResult result = new NewAuthenticationDataResult(resultsArray);

                listener.onDataRetrieveSuccess(request, result);
            }
        }
    }

    @Override
    protected void handleResponse(DataRequest request, String response) {
        // Not used in this request maker.
    }

    @Override
    protected void handleResponse(DataRequest request, JSONArray response) {
        // Not used in this request maker.
    }
}
