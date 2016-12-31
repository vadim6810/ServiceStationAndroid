package il.co.tel_ran.carservice.connection;

import android.content.Context;

import com.android.volley.Request;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import il.co.tel_ran.carservice.ClientUser;
import il.co.tel_ran.carservice.Utils;
import il.co.tel_ran.carservice.VehicleData;

/**
 * Created by maxim on 31-Dec-16.
 */

public class ClientUserDataRequestMaker extends RequestMaker {

    public static final String JSON_FIELD_ID = "id";
    public static final String JSON_FIELD_NAME = "name";
    public static final String JSON_FIELD_CARS = "cars";
    public static final String JSON_FIELD_LOGO = "logo";
    public static final String JSON_FIELD_CREATE_DATETIME = "createdAt";
    public static final String JSON_FIELD_UPDATE_DATETIME = "updatedAt";

    public ClientUserDataRequestMaker(OnDataRetrieveListener listener) {
        super(listener, DataResult.Type.CLIENT_USER);
    }

    @Override
    public void makeRequest(Context context, DataRequest dataRequest) {
        Request request;

        // Make String requests for GET method.
        request = makeStringRequest(dataRequest);

        // Add request to queue
        RequestQueueSingleton.getInstance(context).addToRequestQueue(request);
    }

    @Override
    protected void handleResponse(DataRequest request, JSONArray response) {
        OnDataRetrieveListener listener = getListener();
        if (listener != null) {
            if (response == null) {
                // Pass null message for the error message to be handled elsewhere.
                listener.onDataRetrieveFailed(request, DataResult.Type.CLIENT_USER,
                        ServerResponseError.INVALID_PARAMETER, null);
            } else {
                try {
                    // We could get multiple users response, for now use only the first and ignore the rest.
                    ClientUser[] clientUsers = new ClientUser[response.length()];

                    if (clientUsers.length == 0) {
                        // Pass null message for the error message to be handled elsewhere.
                        listener.onDataRetrieveFailed(request, DataResult.Type.CLIENT_USER,
                                ServerResponseError.USER_NOT_FOUND, null);
                        return;
                    }

                    for (int i = 0; i < response.length(); i++) {
                        ClientUser user = parseClientFromJSONObject(response.getJSONObject(i));
                        clientUsers[i] = user;
                    }

                    ClientUserDataResult result = new ClientUserDataResult(clientUsers);

                    listener.onDataRetrieveSuccess(request, result);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    protected void handleResponse(DataRequest request, String response) {
        try {
            handleResponse(request, new JSONArray(response));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void handleResponse(DataRequest request, JSONObject response) {
        // Not handled in this request maker.
    }

    private ClientUser parseClientFromJSONObject(JSONObject jsonObject) {
        ClientUser clientUser = new ClientUser();

        long clientId = jsonObject.optLong(JSON_FIELD_ID, -1);
        clientUser.setClientId(clientId);

        String name = jsonObject.optString(JSON_FIELD_NAME, "");
        clientUser.setName(name);

        List<VehicleData> cars = new ArrayList<>();

        JSONArray carsJSONArray = jsonObject.optJSONArray(JSON_FIELD_CARS);
        if (carsJSONArray != null) {
            for (int j = 0; j < carsJSONArray.length(); j++) {
                String carString = carsJSONArray.optString(j);
                if (carString != null) {
                    String[] carProperties = carString.split(",");
                    if (carProperties.length != 4)
                        continue;

                    VehicleData vehicleData = new VehicleData();
                    vehicleData.setVehicleMake(carProperties[0]);
                    vehicleData.setVehicleModel(carProperties[1]);
                    vehicleData.setVehicleYear(Integer.parseInt(carProperties[2].trim()));
                    vehicleData.setVehicleModifications(carProperties[3]);

                    cars.add(vehicleData);
                }
            }
        }
        clientUser.setVehicles(cars);

        String logo = jsonObject.optString(JSON_FIELD_LOGO);
        clientUser.setLogo(logo);

        try {
            String createdAt = jsonObject.optString(JSON_FIELD_CREATE_DATETIME);
            if (createdAt != null) {
                clientUser.setCreationDate(Utils.parseDateTime(createdAt));
            }

            String updatedAt = jsonObject.optString(JSON_FIELD_UPDATE_DATETIME);
            if (updatedAt != null) {
                clientUser.setUpdateDate(Utils.parseDateTime(updatedAt));
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return clientUser;
    }
}
