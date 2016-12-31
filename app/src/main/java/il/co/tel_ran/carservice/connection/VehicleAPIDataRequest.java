package il.co.tel_ran.carservice.connection;

import com.android.volley.Request;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by maxim on 30-Dec-16.
 */

public class VehicleAPIDataRequest extends DataRequest {

    public VehicleAPIDataRequest() {
        super(Request.Method.GET, ServerConnection.VEHICLE_API_URL);
    }

    @Override
    public String getRequestParameters() {
        // No request parameters are required.
        return "";
    }

    @Override
    public JSONObject getRequestJSON() throws JSONException {
        // Not relevant to this request.
        return null;
    }

    @Override
    public String getRequestString() {
        // Not relevant to this request.
        return null;
    }
}
