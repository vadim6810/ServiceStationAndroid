package il.co.tel_ran.carservice.connection;

import com.android.volley.Request;

import org.json.JSONException;
import org.json.JSONObject;

import il.co.tel_ran.carservice.ClientUser;
import il.co.tel_ran.carservice.User;

/**
 * Created by maxim on 30-Dec-16.
 */

public class AuthenticationRequest extends DataRequest {

    private final JSONObject mRequestJSON;
    private final String mEmail;
    private final User mUser;

    // Used for POST method
    public AuthenticationRequest(JSONObject requestJSON) {
        this(Request.Method.POST, ServerConnection.AUTHENTICATE_URL, requestJSON, null, null);
    }

    // Used for GET method
    public AuthenticationRequest(String email) {
        this(Request.Method.GET, ServerConnection.AUTHENTICATE_URL, null, email, null);
    }

    // Used for PUT method.
    public AuthenticationRequest(User user) {
        this(Request.Method.PUT, ServerConnection.AUTHENTICATE_URL, null, null, user);
    }

    private AuthenticationRequest(int requestMethod, String url,
                                  JSONObject requestJSON, String email, User user) {
        super(requestMethod, url);

        mRequestJSON = requestJSON;
        mEmail = email;
        mUser = user;
    }

    @Override
    public String getRequestParameters() {
        String parameters = "";

        switch (getRequestMethod()) {
            case Request.Method.GET:
                if (mEmail != null && !mEmail.isEmpty()) {
                    // Add email parameter
                    parameters += '?' + AuthenticationRequestMaker.JSON_FIELD_EMAIL + '=' + mEmail;
                }
                break;
            case Request.Method.PUT:
                if (mUser != null) {
                    long authenticationId = mUser.getId();
                    if (authenticationId > -1) {
                        // Add authentication id parameter
                        parameters += +'/' + Long.toString(mUser.getId());
                    }
                }
                break;
        }

        return parameters;
    }

    @Override
    public JSONObject getRequestJSON() throws JSONException {
        if (getRequestMethod() == Request.Method.PUT && mRequestJSON == null) {
            JSONObject requestJSON = new JSONObject();

            requestJSON.put(AuthenticationRequestMaker.JSON_FIELD_EMAIL, mUser.getEmail());
            requestJSON.put(AuthenticationRequestMaker.JSON_FIELD_PASSWORD, mUser.getPassword());
            if (mUser instanceof ClientUser) {
                requestJSON.put(AuthenticationRequestMaker.JSON_FIELD_TYPE,
                        AuthenticationRequestMaker.JSON_FIELD_TYPE_CLIENT);
            } else {
                requestJSON.put(AuthenticationRequestMaker.JSON_FIELD_TYPE,
                        AuthenticationRequestMaker.JSON_FIELD_TYPE_MASTER);
            }

            return requestJSON;
        }

        return mRequestJSON;
    }

    @Override
    public String getRequestString() {
        // Not used for this request.
        return null;
    }
}
