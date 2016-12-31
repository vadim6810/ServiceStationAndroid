package il.co.tel_ran.carservice.connection;

import com.android.volley.Request;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by maxim on 30-Dec-16.
 */

public class NewAuthenticationRequest extends DataRequest {

    private final JSONObject mRequestJSON;

    public NewAuthenticationRequest(JSONObject requestJSON) {
        super(Request.Method.POST, ServerConnection.AUTHENTICATE_URL);
        mRequestJSON = requestJSON;
    }

    @Override
    public String getRequestParameters() {
        // Currently we don't require any request parameters.
        return "";
    }

    @Override
    public JSONObject getRequestJSON() throws JSONException {
        return mRequestJSON;
    }

    @Override
    public String getRequestString() {
        // Not used for this request.
        return null;
    }
}
