package il.co.tel_ran.carservice.connection;

import android.content.Context;

import com.android.volley.Request;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.EnumSet;

import il.co.tel_ran.carservice.ServiceSubWorkType;
import il.co.tel_ran.carservice.ServiceWorkType;
import il.co.tel_ran.carservice.ServiceWorkTypeCategory;
import il.co.tel_ran.carservice.TenderRequest;
import il.co.tel_ran.carservice.Utils;
import il.co.tel_ran.carservice.VehicleData;
import il.co.tel_ran.carservice.VehicleType;

/**
 * Created by maxim on 05-Jan-17.
 */

public class TenderRequestMaker extends RequestMaker {

    public static final String JSON_FIELD_ID = "id";
    public static final String JSON_FIELD_IDUSER = "idUser";
    public static final String JSON_FIELD_USER_NAME = "userName";
    public static final String JSON_FIELD_CHOSEN_PLACE = "chosenPlace";
    public static final String JSON_FIELD_LATITUDE = "Latitude";
    public static final String JSON_FIELD_LONGITUDE = "Longitude";
    public static final String JSON_FIELD_FORMATTED_ADDRESS = "FormattedAddress";
    public static final String JSON_FIELD_PLACE_ID = "PlaceId";
    public static final String JSON_FIELD_BICYCLE = "bicycle";
    public static final String JSON_FIELD_PASSCAR = "passCar";
    public static final String JSON_FIELD_LORRY = "lorry";
    public static final String JSON_FIELD_BUS = "bus";
    public static final String JSON_FIELD_MOPED = "moped";
    public static final String JSON_FIELD_CAR = "car";
    public static final String JSON_FIELD_SERVICE = "service";
    public static final String JSON_FIELD_SUM = "sum";
    public static final String JSON_FIELD_DATE = "date";
    public static final String JSON_FIELD_COMMENT = "comment";
    public static final String JSON_FIELD_CREATED_AT = "createdAt";
    public static final String JSON_FIELD_UPDATED_AT = "updatedAt";

    // Used for parsing service work type strings.
    private ArrayList<ServiceWorkTypeCategory> mWorkTypeCategories;

    public TenderRequestMaker(OnDataRetrieveListener listener) {
        super(listener, DataResult.Type.TENDER_REQUEST);
    }

    @Override
    public void makeRequest(Context context, DataRequest dataRequest) {
        Request request = null;

        // Build the request.
        if (dataRequest.getRequestMethod() == Request.Method.GET) {
            request = makeStringRequest(dataRequest);
        }

        if (request != null) {
            // Send the request
            RequestQueueSingleton.getInstance(context).addToRequestQueue(request);

            mWorkTypeCategories = ServiceWorkTypeCategory.generateWorkTypeCategories(context);
        }
    }

    @Override
    protected void handleResponse(DataRequest request, String response) {
        if (response == null || response.isEmpty()) {
            OnDataRetrieveListener listener = getListener();
            if (listener != null) {
                listener.onDataRetrieveFailed(request, getResultType(),
                        ServerResponseError.INVALID_PARAMETER, null);
            }
        } else {
            try {
                handleResponse(request, new JSONArray(response));
            } catch (JSONException e) {
                e.printStackTrace();
            }
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
                TenderRequest[] tenderRequests
                        = new TenderRequest[response.length()];

                if (tenderRequests.length == 0) {
                    // Pass null message for the error message to be handled elsewhere.
                    listener.onDataRetrieveFailed(request, getResultType(),
                            ServerResponseError.TENDER_REQUEST_NOT_FOUND, null);
                    return;
                }

                try {
                    for (int i = 0; i < response.length(); i++) {
                        TenderRequest tenderRequest = parseTenderRequestFromJSONObject(
                                response.getJSONObject(i));
                        tenderRequests[i] = tenderRequest;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                TenderRequestDataResult result = new TenderRequestDataResult(tenderRequests);

                listener.onDataRetrieveSuccess(request, result);
            }
        }
    }

    @Override
    protected void handleResponse(DataRequest request, JSONObject response) {
        // Not handled in this request maker.
    }

    private TenderRequest parseTenderRequestFromJSONObject(JSONObject jsonObject) {
        TenderRequest request;

        long id = jsonObject.optLong(JSON_FIELD_ID, -1);
        long idUser = jsonObject.optLong(JSON_FIELD_IDUSER, -1);
        String userName = jsonObject.optString(JSON_FIELD_USER_NAME, "");

        String placeId = "";
        String location = "";
        JSONObject chosenPlace = jsonObject.optJSONObject(JSON_FIELD_CHOSEN_PLACE);
        if (chosenPlace != null) {
            placeId = chosenPlace.optString(JSON_FIELD_PLACE_ID, "");
            location = chosenPlace.optString(JSON_FIELD_FORMATTED_ADDRESS, "");
        }

        EnumSet<VehicleType> vehicleTypes = EnumSet.noneOf(VehicleType.class);
        if (jsonObject.optBoolean(JSON_FIELD_PASSCAR)) {
            vehicleTypes.add(VehicleType.PRIVATE);
        }
        if (jsonObject.optBoolean(JSON_FIELD_BICYCLE)) {
            vehicleTypes.add(VehicleType.MOTORCYCLE);
        }
        if (jsonObject.optBoolean(JSON_FIELD_LORRY)) {
            vehicleTypes.add(VehicleType.TRUCK);
        }
        if (jsonObject.optBoolean(JSON_FIELD_BUS)) {
            vehicleTypes.add(VehicleType.BUS);
        }

        String vehicleDataString = jsonObject.optString(JSON_FIELD_CAR, "");
        VehicleData vehicleData = VehicleData.parseVehicleData(vehicleDataString);

        ArrayList<ServiceSubWorkType> subWorkTypes = new ArrayList<>();

        JSONArray servicesJSONArray = jsonObject.optJSONArray(JSON_FIELD_SERVICE);
        if (servicesJSONArray != null) {
            // Iterate through the array.
            int length = servicesJSONArray.length();
            for (int i = 0; i < length; i++) {
                // Get a field representing a sub work type.
                String field = servicesJSONArray.optString(i);
                if (field != null && !field.isEmpty()) {
                    // Parse field to a sub work type object.
                    ServiceSubWorkType subWorkType = ServiceSubWorkType.getTypeFromField(field);
                    if (subWorkType != null) {
                        // Look through all of our categories
                        for (ServiceWorkTypeCategory workTypeCategory : mWorkTypeCategories) {
                            // Get all sub work types for this category
                            ArrayList<ServiceSubWorkType> allSubWorkTypes = workTypeCategory
                                    .getSubWorkTypes();
                            // Check if this sub work type is valid
                            if (allSubWorkTypes.contains(subWorkType)) {
                                // Add the sub work type from all categories - this ensure correct naming.
                                subWorkTypes.add(allSubWorkTypes
                                        .get(allSubWorkTypes.indexOf(subWorkType)));
                            }
                        }
                    }
                }
            }
        }

        float price = 0.0f;

        String priceString = jsonObject.optString(JSON_FIELD_SUM, "0.0");
        try {
            price = Float.parseFloat(priceString);
        } catch (NumberFormatException ignored) {}

        String message = jsonObject.optString(JSON_FIELD_COMMENT, "");

        String deadlineDateTime = jsonObject.optString(JSON_FIELD_DATE, "1970-1-1T00:00:00.000Z");
        String createdAtDateTime = jsonObject.optString(JSON_FIELD_CREATED_AT,
                "1970-1-1T00:00:00.000Z");
        String updatedAtDateTime = jsonObject.optString(JSON_FIELD_UPDATED_AT,
                "1970-1-1T00:00:00.000Z");

        Date deadlineDate = null;
        Date createdAtDate = null;
        Date updatedAtDate = null;
        try {
            deadlineDate = Utils.parseDateTime(deadlineDateTime);
            createdAtDate = Utils.parseDateTime(createdAtDateTime);
            updatedAtDate = Utils.parseDateTime(updatedAtDateTime);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        request = new TenderRequest.Builder()
                .setId(id)
                .setIdUser(idUser)
                .setSender(userName)
                .setLocationPlaceId(placeId)
                .setVehicleTypes(vehicleTypes)
                .setVehicleData(vehicleData)
                .setSubWorkTypes(subWorkTypes)
                .setPrice(price)
                .setMessage(message)
                .setCreatedAtDate(createdAtDate)
                .setUpdatedAtDate(updatedAtDate)
                .setDeadlineDate(deadlineDate)
                .setLocation(location)
                .build();

        return request;
    }
}
