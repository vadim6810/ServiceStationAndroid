package il.co.tel_ran.carservice.connection;

import com.android.volley.Request;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by maxim on 31-Dec-16.
 */

public class ClientUserDataRequest extends DataRequest {

    private final long mId;

    public ClientUserDataRequest() {
        this(-1); // Allows us to get all users
    }

    public ClientUserDataRequest(long id) {
        super(Request.Method.GET, ServerConnection.CLIENTS_URL);

        mId = id;
    }

    @Override
    public String getRequestParameters() {
        // Check if the request method is correct.
        if (getRequestMethod() == Request.Method.GET) {
            String parameters = "";
            // Check if we need to add any parameters (or get everything)
            if (mId >= 0) {
                // Add ID parameter
                parameters += '?' + ClientUserDataRequestMaker.JSON_FIELD_ID + '=' + mId;
            }

            return parameters;
        } else {
            // Return null because this is the wrong request method.
            return null;
        }
    }

    @Override
    public JSONObject getRequestJSON() throws JSONException {
        // Not used for this request type
        return null;
    }

    @Override
    public String getRequestString() {
        // Not used for this request type
        return null;
    }
}
