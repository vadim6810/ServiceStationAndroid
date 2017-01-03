package il.co.tel_ran.carservice.connection;

import com.android.volley.Request;
import com.google.android.gms.location.places.Place;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import il.co.tel_ran.carservice.ProviderUser;
import il.co.tel_ran.carservice.ServiceStation;
import il.co.tel_ran.carservice.ServiceSubWorkType;
import il.co.tel_ran.carservice.ServiceType;
import il.co.tel_ran.carservice.ServiceWorkType;
import il.co.tel_ran.carservice.VehicleData;
import il.co.tel_ran.carservice.VehicleType;

/**
 * Created by maxim on 30-Dec-16.
 */

public class NewProviderUserRequest extends DataRequest {

    private final ServiceStation mServiceStation;
    private final ProviderUser mProviderUser;

    public NewProviderUserRequest(ProviderUser providerUser, ServiceStation serviceStation) {
        super(Request.Method.POST, ServerConnection.MASTERS_URL);

        mProviderUser = providerUser;
        mServiceStation = serviceStation;
    }

    @Override
    public String getRequestParameters() {
        // Currently we don't require any request parameters.
        return "";
    }

    @Override
    public JSONObject getRequestJSON() throws JSONException {
        JSONObject requestJSON = new JSONObject();

        requestJSON.put(ServiceStationRequestMaker.JSON_FIELD_SERVICE_NAME,
                mServiceStation.getName());

        EnumSet<ServiceType> availableServices = mServiceStation.getAvailableServices();
        requestJSON.put(ServiceStationRequestMaker.JSON_FIELD_SERVICE_TYPE_MECHANICS,
                availableServices.contains(ServiceType.AUTO_SERVICE));
        requestJSON.put(ServiceStationRequestMaker.JSON_FIELD_SERVICE_TYPE_MOUNTING,
                availableServices.contains(ServiceType.TYRE_REPAIR));
        requestJSON.put(ServiceStationRequestMaker.JSON_FIELD_SERVICE_TYPE_CAR_WASH,
                availableServices.contains(ServiceType.CAR_WASH));
        requestJSON.put(ServiceStationRequestMaker.JSON_FIELD_SERVICE_TYPE_TOW_TRUCK,
                availableServices.contains(ServiceType.TOWING));

        JSONObject chosenPlace = new JSONObject();
        Place location = mServiceStation.getLocation();
        if (location != null) {
            chosenPlace.put(ServiceStationRequestMaker.JSON_FIELD_SERVICE_LOCATION_LAT,
                    location.getLatLng().latitude);
            chosenPlace.put(ServiceStationRequestMaker.JSON_FIELD_SERVICE_LOCATION_LONG,
                    location.getLatLng().longitude);
            chosenPlace.put(ServiceStationRequestMaker.JSON_FIELD_SERVICE_LOCATION_ADDRESS,
                    location.getAddress());
            chosenPlace.put(ServiceStationRequestMaker.JSON_FIELD_SERVICE_LOCATION_ID,
                    location.getId());
        } else {
            chosenPlace.put(ServiceStationRequestMaker.JSON_FIELD_SERVICE_LOCATION_LAT,
                    31.046051);
            chosenPlace.put(ServiceStationRequestMaker.JSON_FIELD_SERVICE_LOCATION_LONG,
                    34.85161199999993f);
            chosenPlace.put(ServiceStationRequestMaker.JSON_FIELD_SERVICE_LOCATION_ADDRESS,
                    "Israel");
            chosenPlace.put(ServiceStationRequestMaker.JSON_FIELD_SERVICE_LOCATION_ID,
                    "ChIJi8mnMiRJABURuiw1EyBCa2o");
        }
        requestJSON.put(ServiceStationRequestMaker.JSON_FIELD_SERVICE_CHOSEN_PLACE, chosenPlace);

        requestJSON.put(ServiceStationRequestMaker.JSON_FIELD_SERVICE_TELEPHONE,
                mServiceStation.getPhonenumber());

        requestJSON.put(ServiceStationRequestMaker.JSON_FIELD_SERVICE_START_TIME,
                mServiceStation.getOpeningTime().toString());
        requestJSON.put(ServiceStationRequestMaker.JSON_FIELD_SERVICE_LAST_TIME,
                mServiceStation.getClosingTime().toString());

        requestJSON.put(ServiceStationRequestMaker.JSON_FIELD_SERVICE_MANAGER_NAME,
                mServiceStation.getManagerName());
        requestJSON.put(ServiceStationRequestMaker.JSON_FIELD_SERVICE_MANAGER_TELEPHONE,
                mServiceStation.getManagerPhonenumber());

        requestJSON.put(ServiceStationRequestMaker.JSON_FIELD_SERVICE_DIRECTOR_NAME,
                mServiceStation.getDirectorName());

        JSONObject workTypesJSON = new JSONObject();
        JSONObject subWorkTypesJSON;

        ArrayList<ServiceWorkType> serviceWorkTypes = mServiceStation.getWorkTypes();
        ArrayList<ServiceSubWorkType> serviceSubWorkTypes = mServiceStation.getSubWorkTypes();
        // Iterate through all available work types
        for (ServiceWorkType workType : serviceWorkTypes) {
            subWorkTypesJSON = new JSONObject();
            // Iterate through all available sub work types
            for (ServiceSubWorkType subWorkType : serviceSubWorkTypes) {
                // Check if parents match
                if (subWorkType.getParentWorkType() == workType) {
                    // Set this sub work type as true.
                    subWorkTypesJSON.put(ServiceSubWorkType.getFieldForType(subWorkType), true);
                }
            }

            // Add this work type JSON
            workTypesJSON.put(ServiceWorkType.getFieldForType(workType), subWorkTypesJSON);
        }
        requestJSON.put(ServiceStationRequestMaker.JSON_FIELD_SERVICES, workTypesJSON);

        // Convert array of car makes (strings) to ArrayList
        ArrayList<String> servicedCarMakeStrings = new ArrayList<>();

        String[] servicedCarMakes = mServiceStation.getServicedCarMakes();
        if (servicedCarMakes.length > 0) {
            for (String carMake : servicedCarMakes) {
                servicedCarMakeStrings.add(carMake);
            }
        }
        requestJSON.put(ServiceStationRequestMaker.JSON_FIELD_SERVICE_CARS,
                new JSONArray(servicedCarMakeStrings));

        JSONObject categories = new JSONObject();
        for (VehicleType vehicleType : mServiceStation.getVehicleTypes()) {
            categories.put(VehicleType.getFieldForType(vehicleType), true);
        }
        requestJSON.put(ServiceStationRequestMaker.JSON_FIELD_SERVICE_CATEGORIES, categories);

        return requestJSON;
    }

    @Override
    public String getRequestString() {
        // Not used for this request.
        return null;
    }

    public ProviderUser getProviderUser() {
        return mProviderUser;
    }
}
