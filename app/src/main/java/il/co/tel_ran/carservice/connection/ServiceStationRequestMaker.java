package il.co.tel_ran.carservice.connection;

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import il.co.tel_ran.carservice.ServiceStation;
import il.co.tel_ran.carservice.ServiceSubWorkType;
import il.co.tel_ran.carservice.ServiceType;
import il.co.tel_ran.carservice.ServiceWorkType;
import il.co.tel_ran.carservice.ServiceWorkTypeCategory;
import il.co.tel_ran.carservice.TimeHolder;
import il.co.tel_ran.carservice.VehicleType;

/**
 * Created by maxim on 24-Dec-16.
 */

public class ServiceStationRequestMaker extends RequestMaker {

    public static final String JSON_FIELD_SERVICE_ID = "id";
    public static final String JSON_FIELD_SERVICE_NAME = "companyName";
    public static final String JSON_FIELD_SERVICE_TYPE_MECHANICS = "mechanics";
    public static final String JSON_FIELD_SERVICE_TYPE_MOUNTING = "mounting";
    public static final String JSON_FIELD_SERVICE_TYPE_CAR_WASH = "carWash";
    public static final String JSON_FIELD_SERVICE_TYPE_TOW_TRUCK = "towTruck";
    public static final String JSON_FIELD_SERVICE_TELEPHONE = "companyTelephone";
    public static final String JSON_FIELD_SERVICE_START_TIME = "startTime";
    public static final String JSON_FIELD_SERVICE_LAST_TIME = "lastTime";
    public static final String JSON_FIELD_SERVICE_MANAGER_NAME = "managerName";
    public static final String JSON_FIELD_SERVICE_MANAGER_TELEPHONE = "managerTelephone";
    public static final String JSON_FIELD_SERVICE_DIRECTOR_NAME = "directorName";
    public static final String JSON_FIELD_SERVICE_CARS = "cars";
    public static final String JSON_FIELD_SERVICE_CHOSEN_PLACE = "chosenPlace";
    public static final String JSON_FIELD_SERVICE_PLACE_ID = "PlaceId";
    public static final String JSON_FIELD_SERVICE_RATE = "rate";
    public static final String JSON_FIELD_SERVICE_RATEALL = "rateAll";
    public static final String JSON_FIELD_SERVICE_AMOUNT_COMMENTS = "amountComments";
    public static final String JSON_FIELD_SERVICE_CATEGORIES = "categories";
    public static final String JSON_FIELD_SERVICE_TYPE_BUS = "bus";
    public static final String JSON_FIELD_SERVICE_TYPE_PASSCAR = "passCar";
    public static final String JSON_FIELD_SERVICE_TYPE_BICYCLE = "bicycle";
    public static final String JSON_FIELD_SERVICE_TYPE_LORRY = "lorry";
    public static final String JSON_FIELD_SERVICE_LOCATION_LAT = "Latitude";
    public static final String JSON_FIELD_SERVICE_LOCATION_LONG = "Longitude";
    public static final String JSON_FIELD_SERVICE_LOCATION_ID = "PlaceId";
    public static final String JSON_FIELD_SERVICE_LOCATION_ADDRESS = "FormattedAddress";
    public static final String JSON_FIELD_SERVICES = "services";
    public static final String JSON_FIELD_WORK_TYPE = "vid";

    // Used for parsing service work type strings.
    private ArrayList<ServiceWorkTypeCategory> mWorkTypeCategories;

    public ServiceStationRequestMaker(OnDataRetrieveListener listener) {
        super(listener, DataResult.Type.SERVICE_STATION);
    }

    @Override
    public void makeRequest(Context context, DataRequest dataRequest) {
        Request request;
        if (dataRequest.getRequestMethod() == Request.Method.POST) {
            // Make JSON requests for POST method.
            request = makeJSONArrayRequest(dataRequest);
        } else {
            // Make String requests for GET method.
            request = makeStringRequest(dataRequest);
        }

        // Add request to queue
        RequestQueueSingleton.getInstance(context).addToRequestQueue(request);

        mWorkTypeCategories = ServiceWorkTypeCategory.generateWorkTypeCategories(context);
    }

    @Override
    protected void handleError(DataResult.Type resultType, DataRequest request, VolleyError error) {
        super.handleError(resultType, request, error);
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
                // We could get multiple service response
                ServiceStation[] serviceStations
                        = new ServiceStation[response.length()];

                if (serviceStations.length == 0) {
                    // Pass null message for the error message to be handled elsewhere.
                    listener.onDataRetrieveFailed(request, getResultType(),
                            ServerResponseError.SERVICE_NOT_FOUND, null);
                    return;
                }

                try {
                    for (int i = 0; i < response.length(); i++) {
                        ServiceStation serviceStation = parseMasterFromJSONObject(
                                response.getJSONObject(i));
                        serviceStations[i] = serviceStation;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                ServiceStationDataResult result = new ServiceStationDataResult(serviceStations);

                listener.onDataRetrieveSuccess(request, result);
            }
        }
    }

    @Override
    protected void handleResponse(DataRequest request, JSONObject response) {
        // Not used in this request maker.
    }

    private ServiceStation parseMasterFromJSONObject(JSONObject mastersJSONObject)
            throws JSONException {
        ServiceStation serviceStation = new ServiceStation();

        long id = mastersJSONObject.getLong(JSON_FIELD_SERVICE_ID);
        serviceStation.setID(id);

        String name = mastersJSONObject.getString(JSON_FIELD_SERVICE_NAME);
        serviceStation.setName(name);

        // TODO: update service types when back-end is updated.
        boolean mechanics = !mastersJSONObject.isNull(JSON_FIELD_SERVICE_TYPE_MECHANICS)
                && mastersJSONObject.getBoolean(JSON_FIELD_SERVICE_TYPE_MECHANICS);
        serviceStation.toggleService(ServiceType.AUTO_SERVICE, mechanics);

        boolean mounting = !mastersJSONObject.isNull(JSON_FIELD_SERVICE_TYPE_MOUNTING)
                && mastersJSONObject.getBoolean(JSON_FIELD_SERVICE_TYPE_MOUNTING);
        serviceStation.toggleService(ServiceType.TYRE_REPAIR, mounting);

        boolean carWash = !mastersJSONObject.isNull(JSON_FIELD_SERVICE_TYPE_CAR_WASH)
                && mastersJSONObject.getBoolean(JSON_FIELD_SERVICE_TYPE_CAR_WASH);
        serviceStation.toggleService(ServiceType.CAR_WASH, carWash);

        boolean towTruck = !mastersJSONObject.isNull(JSON_FIELD_SERVICE_TYPE_TOW_TRUCK)
                && mastersJSONObject.getBoolean(JSON_FIELD_SERVICE_TYPE_TOW_TRUCK);
        serviceStation.toggleService(ServiceType.TOWING, towTruck);

        String companyPhonenumber = mastersJSONObject.getString(JSON_FIELD_SERVICE_TELEPHONE);
        serviceStation.setPhonenumber(companyPhonenumber);

        String openTime = mastersJSONObject.getString(JSON_FIELD_SERVICE_START_TIME);
        serviceStation.setOpeningTime(TimeHolder.parseTime(openTime));

        String closeTime = mastersJSONObject.getString(JSON_FIELD_SERVICE_LAST_TIME);
        serviceStation.setClosingTime(TimeHolder.parseTime(closeTime));

        String managerName = mastersJSONObject.getString(JSON_FIELD_SERVICE_MANAGER_NAME);
        serviceStation.setManagerName(managerName);

        String managerPhonenumber = mastersJSONObject.getString(
                JSON_FIELD_SERVICE_MANAGER_TELEPHONE);
        serviceStation.setManagerPhonenumber(managerPhonenumber);

        String directorName = mastersJSONObject.getString(JSON_FIELD_SERVICE_DIRECTOR_NAME);
        serviceStation.setDirectorName(directorName);

        String[] servicedCarMakesArray = new String[0];
        if (!mastersJSONObject.isNull(JSON_FIELD_SERVICE_CARS)) {
            JSONArray servicedCarMakes = mastersJSONObject.getJSONArray(JSON_FIELD_SERVICE_CARS);
            servicedCarMakesArray = new String[servicedCarMakes.length()];
            for (int j = 0; j < servicedCarMakes.length(); j++) {
                servicedCarMakesArray[j] = servicedCarMakes.getString(j);
            }
        }
        serviceStation.setServicedCarMakes(servicedCarMakesArray);

        JSONObject chosenPlace = mastersJSONObject.getJSONObject(JSON_FIELD_SERVICE_CHOSEN_PLACE);
        String placeId = chosenPlace.getString(JSON_FIELD_SERVICE_PLACE_ID);
        serviceStation.setPlaceId(placeId);

        float avgRating = 0.0f;
        if (!mastersJSONObject.isNull(JSON_FIELD_SERVICE_RATE)) {
            avgRating = (float) mastersJSONObject.getDouble(JSON_FIELD_SERVICE_RATE);
        }
        serviceStation.setAvgRating(avgRating);

        int totalRatings = 0;
        if (!mastersJSONObject.isNull(JSON_FIELD_SERVICE_AMOUNT_COMMENTS)) {
            totalRatings = Integer.valueOf(mastersJSONObject.getString(
                    JSON_FIELD_SERVICE_AMOUNT_COMMENTS));
        }
        serviceStation.setSubmittedRatings(totalRatings);

        JSONObject carTypes = mastersJSONObject.getJSONObject(JSON_FIELD_SERVICE_CATEGORIES);
        serviceStation.toggleVehicleType(VehicleType.BUS,
                carTypes.has(JSON_FIELD_SERVICE_TYPE_BUS)
                        && carTypes.getBoolean(JSON_FIELD_SERVICE_TYPE_BUS));
        serviceStation.toggleVehicleType(VehicleType.PRIVATE,
                carTypes.has(JSON_FIELD_SERVICE_TYPE_PASSCAR)
                        && carTypes.getBoolean(JSON_FIELD_SERVICE_TYPE_PASSCAR));
        serviceStation.toggleVehicleType(VehicleType.MOTORCYCLE,
                carTypes.has(JSON_FIELD_SERVICE_TYPE_BICYCLE)
                        && carTypes.getBoolean(JSON_FIELD_SERVICE_TYPE_BICYCLE));
        serviceStation.toggleVehicleType(VehicleType.TRUCK,
                carTypes.has(JSON_FIELD_SERVICE_TYPE_LORRY)
                        && carTypes.getBoolean(JSON_FIELD_SERVICE_TYPE_LORRY));

        if (mastersJSONObject.has(JSON_FIELD_SERVICE_LOCATION_ADDRESS))
            serviceStation.setCityName(
                    mastersJSONObject.getString(JSON_FIELD_SERVICE_LOCATION_ADDRESS));

        JSONObject workTypesJSONObject = mastersJSONObject.getJSONObject(JSON_FIELD_SERVICES);
        ArrayList<ServiceWorkType> workTypes = new ArrayList<>();
        ArrayList<ServiceSubWorkType> subWorkTypes = new ArrayList<>();

        for (ServiceWorkTypeCategory workTypeCategory : mWorkTypeCategories) {
            ServiceWorkType workType = workTypeCategory.getServiceWorkType();
            // Get field name for this work type.
            String workTypeFieldName = ServiceWorkType.getFieldForType(workType);
            // Check if this JSON object has a field corresponding to the work type.
            if (workTypesJSONObject.has(workTypeFieldName)) {
                // Keep this work type.
                workTypes.add(workType);
                // Get a JSON object containing sub work types.
                JSONObject subWorkTypesJSONObject = workTypesJSONObject.getJSONObject(
                        workTypeFieldName);
                for (ServiceSubWorkType subWorkType : workTypeCategory.getSubWorkTypes()) {
                    // Get field name for this sub work type.
                    String subWorkTypeFieldName = ServiceSubWorkType.getFieldForType(subWorkType);
                    // Check which sub work types are in this JSON object.
                    if (subWorkTypesJSONObject.has(subWorkTypeFieldName)) {
                        // Keep this sub work type.
                        subWorkTypes.add(subWorkType);
                    }
                }
            }
        }

        serviceStation.setWorkTypes(workTypes);
        serviceStation.setSubWorkTypes(subWorkTypes);

        return serviceStation;
    }
}
