package il.co.tel_ran.carservice.connection;

import android.util.Log;

import com.android.volley.Request;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import il.co.tel_ran.carservice.ClientUser;
import il.co.tel_ran.carservice.VehicleData;

/**
 * Created by maxim on 30-Dec-16.
 */

public class NewClientUserRequest extends DataRequest {

    private final ClientUser mClientUser;

    public NewClientUserRequest(ClientUser clientUser) {
        super(Request.Method.POST, ServerConnection.CLIENTS_URL);

        mClientUser = clientUser;
    }

    @Override
    public String getRequestParameters() {
        // Currently we don't require any request parameters.
        return "";
    }

    @Override
    public JSONObject getRequestJSON() throws JSONException {
        JSONObject requestJSON = new JSONObject();

        requestJSON.put("name", mClientUser.getName());

        ArrayList<String> vehicleStrings = new ArrayList<>();

        List<VehicleData> vehicles = mClientUser.getVehicles();
        if (vehicles != null && !vehicles.isEmpty()) {
            for (VehicleData vehicleData : vehicles) {
                vehicleStrings.add(vehicleData.toPersistedString());
            }
        }

        requestJSON.put("cars" , new JSONArray(vehicleStrings));

        return requestJSON;
    }

    @Override
    public String getRequestString() {
        // Not used for this request.
        return null;
    }

    public ClientUser getClientUser() {
        return mClientUser;
    }
}
