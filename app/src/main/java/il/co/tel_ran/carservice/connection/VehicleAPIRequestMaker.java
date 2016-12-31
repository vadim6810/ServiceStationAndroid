package il.co.tel_ran.carservice.connection;

import android.content.Context;
import android.util.Log;

import com.android.volley.Request;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import il.co.tel_ran.carservice.VehicleExtendedData;

/**
 * Created by maxim on 30-Dec-16.
 */

public class VehicleAPIRequestMaker extends RequestMaker {

    public static final String JSON_FIELD_MARK = "mark";
    public static final String JSON_FIELD_MODEL = "model";
    public static final String JSON_FIELD_MODELS = "models";
    public static final String JSON_FIELD_TITLE = "title";

    public VehicleAPIRequestMaker(OnDataRetrieveListener listener) {
        super(listener, DataResult.Type.VEHICLE_API);
    }

    @Override
    public void makeRequest(Context context, DataRequest dataRequest) {
        // Create the request
        Request request = makeStringRequest(dataRequest);

        // Add request to queue
        RequestQueueSingleton.getInstance(context).addToRequestQueue(request);
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
    protected void handleResponse(DataRequest request, JSONArray response) {
        OnDataRetrieveListener listener = getListener();
        if (listener != null) {
            if (response == null) {
                // Pass null message for the error message to be handled elsewhere.
                listener.onDataRetrieveFailed(request, getResultType(),
                        ServerResponseError.INVALID_PARAMETER, null);
            } else {
                VehicleExtendedData[] vehicleDataArray
                        = new VehicleExtendedData[response.length()];

                if (vehicleDataArray.length == 0) {
                    // Pass null message for the error message to be handled elsewhere.
                    listener.onDataRetrieveFailed(request, getResultType(),
                            ServerResponseError.VEHICLE_DB_EMPTY, null);
                    return;
                }

                try {
                    for (int i = 0; i < response.length(); i++) {
                        VehicleExtendedData vehicleData = parseVehicleDataFromJSONObject(
                                response.getJSONObject(i));
                        vehicleDataArray[i] = vehicleData;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                VehicleAPIDataResult result = new VehicleAPIDataResult(vehicleDataArray);

                listener.onDataRetrieveSuccess(request, result);
            }
        }
    }

    private VehicleExtendedData parseVehicleDataFromJSONObject(JSONObject jsonObject) {
        VehicleExtendedData vehicleData = new VehicleExtendedData();

        JSONObject modelJSONObject = jsonObject.optJSONObject(JSON_FIELD_MODEL);

        String vehicleMake = null;
        // Parse make
        if (modelJSONObject != null) {
            JSONObject vehicleMakeJSONObject = modelJSONObject.optJSONObject(JSON_FIELD_MARK);
            if (vehicleMakeJSONObject != null) {
                vehicleMake = vehicleMakeJSONObject.optString(JSON_FIELD_TITLE);
            }
        }

        vehicleData.setVehicleMake(vehicleMake);

        // Parse models
        if (modelJSONObject != null) {
            JSONArray modelsJSONArray = modelJSONObject.optJSONArray(JSON_FIELD_MODELS);
            if (modelsJSONArray != null) {
                ArrayList<String> carModels = new ArrayList<>();
                int modelCount = modelsJSONArray.length();
                for (int i = 0; i < modelCount; i++) {
                    String model = modelsJSONArray.optJSONObject(i).optString(JSON_FIELD_TITLE);
                    if (model != null) {
                        carModels.add(model);
                    }
                }

                vehicleData.setExtraModels(carModels);
            }
        }

        return vehicleData;
    }

    @Override
    protected void handleResponse(DataRequest request, JSONObject response) {
        // Not handled for this request maker.
    }
}
