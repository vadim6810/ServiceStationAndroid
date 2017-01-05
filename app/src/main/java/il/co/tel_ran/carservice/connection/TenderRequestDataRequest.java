package il.co.tel_ran.carservice.connection;

import com.android.volley.Request;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by maxim on 05-Jan-17.
 */

public class TenderRequestDataRequest extends DataRequest {

    public TenderRequestDataRequest() {
        super(Request.Method.GET, ServerConnection.TENDERS_URL);
    }

    @Override
    public String getRequestParameters() {
        return "";
    }

    @Override
    public JSONObject getRequestJSON() throws JSONException {
        // Not used for this request.
        return null;
    }

    @Override
    public String getRequestString() {
        // Not used for this request.
        return null;
    }
}
