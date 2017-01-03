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
        String parameters = "";

        if (getRequestMethod() == Request.Method.PUT) {
            if (mClientUser != null) {
                parameters += '/' + Long.toString(mClientUser.getClientId());
            }
        }

        return parameters;
    }

    @Override
    public JSONObject getRequestJSON() throws JSONException {
        JSONObject requestJSON = new JSONObject();

        requestJSON.put(ClientUserDataRequestMaker.JSON_FIELD_NAME, mClientUser.getName());

        ArrayList<String> vehicleStrings = new ArrayList<>();

        List<VehicleData> vehicles = mClientUser.getVehicles();
        if (vehicles != null && !vehicles.isEmpty()) {
            for (VehicleData vehicleData : vehicles) {
                vehicleStrings.add(vehicleData.toPersistedString());
            }
        }

        requestJSON.put(ClientUserDataRequestMaker.JSON_FIELD_CARS, new JSONArray(vehicleStrings));

        requestJSON.put(ClientUserDataRequestMaker.JSON_FIELD_LOGO, mClientUser.getLogo());

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
