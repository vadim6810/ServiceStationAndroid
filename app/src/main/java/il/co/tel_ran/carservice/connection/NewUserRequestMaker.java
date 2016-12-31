package il.co.tel_ran.carservice.connection;

import android.content.Context;

import com.android.volley.Request;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import il.co.tel_ran.carservice.User;
import il.co.tel_ran.carservice.Utils;

/**
 * Created by maxim on 30-Dec-16.
 */

public class NewUserRequestMaker extends RequestMaker {

    public NewUserRequestMaker(OnDataRetrieveListener listener) {
        super(listener, DataResult.Type.NEW_USER);
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
                    int idUser = response.getInt(UserAuthentication.JSON_FIELD_ID);

                    User user;
                    String role;
                    if (request instanceof NewClientUserRequest) {
                        user = ((NewClientUserRequest) request)
                                .getClientUser();

                        role = UserAuthentication.JSON_FIELD_TYPE_CLIENT;
                    } else {
                        // Create master user
                        user = ((NewProviderUserRequest) request)
                                .getProviderUser();

                        role = UserAuthentication.JSON_FIELD_TYPE_MASTER;
                    }

                    String password = Utils.generateRandomString(8);

                    resultJSON.put(UserAuthentication.JSON_FIELD_IDUSER, idUser);
                    resultJSON.put(UserAuthentication.JSON_FIELD_EMAIL, user.getEmail());
                    resultJSON.put(UserAuthentication.JSON_FIELD_PASSWORD, password);
                    resultJSON.put(UserAuthentication.JSON_FIELD_TYPE, role);

                } catch (JSONException e) {
                    e.printStackTrace();
                }

                JSONObject[] resultsArray = {resultJSON};
                NewUserDataResult result = new NewUserDataResult(resultsArray);

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
