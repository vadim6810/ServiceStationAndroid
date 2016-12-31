package il.co.tel_ran.carservice.connection;

import com.android.volley.Request;
import com.google.android.gms.location.places.Place;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

import il.co.tel_ran.carservice.ServiceSubWorkType;
import il.co.tel_ran.carservice.ServiceType;
import il.co.tel_ran.carservice.ServiceWorkType;
import il.co.tel_ran.carservice.VehicleType;

/**
 * Created by maxim on 9/29/2016.
 */

public class ServiceStationDataRequest extends DataRequest {

    private long mId = -1;

    private ArrayList<String> mCarMakes;

    private Place mLocation;

    private ServiceType mServiceType;

    private VehicleType mVehicleType;

    private ArrayList<ServiceWorkType> mWorkTypes;

    private ArrayList<ServiceSubWorkType> mSubWorkTypes;

    /**
     * Only used when the request method is GET and all we need is to get a service by its Id.
     */
    public ServiceStationDataRequest(long id) {
        super(Request.Method.GET, ServerConnection.MASTERS_URL);

        mId = id;
    }

    /**
     * Only used when the request method is GET and we need to get all services.
     */
    public ServiceStationDataRequest() {
        super(Request.Method.GET, ServerConnection.MASTERS_URL);
    }

    /**
     * Only used when the request method is POST and we request by specific paramters.
     * @param carMakes ArrayList containing car makes the service supports.
     * @param place Place object defining the location of this service.
     * @param serviceType The type of this service.
     * @param vehicleType Vehicle types serviced.
     * @param workTypes Work types this service supports.
     */
    private ServiceStationDataRequest(ArrayList<String> carMakes, Place place,
                                      ServiceType serviceType, VehicleType vehicleType,
                                      ArrayList<ServiceWorkType> workTypes,
                                      ArrayList<ServiceSubWorkType> subWorkTypes) {
        super(Request.Method.POST, ServerConnection.getPostUrlForServiceType(serviceType));

        mCarMakes     = carMakes;
        mLocation     = place;
        mServiceType  = serviceType;
        mVehicleType  = vehicleType;
        mWorkTypes    = workTypes;
        mSubWorkTypes = subWorkTypes;
    }

    @Override
    public String getUrl() {
        // URL has to be updated according to the service type.
        if (getRequestMethod() == Request.Method.POST) {
            return ServerConnection.getPostUrlForServiceType(mServiceType);
        }
        return super.getUrl();
    }

    @Override
    public String getRequestParameters() {
        // Check if the request method is correct.
        if (getRequestMethod() == Request.Method.GET) {
            String parameters = "";
            // Check if we need to add any parameters (or get everything)
            if (mId >= 0) {
                // Add ID parameter
                parameters += '?' + ServiceStationRequestMaker.JSON_FIELD_SERVICE_ID + '=' + mId;
            }

            return parameters;
        } else {
            // Return null because this is the wrong request method.
            return null;
        }
    }

    @Override
    public JSONObject getRequestJSON() throws JSONException {
        // Check if the request method is correct.
        if (getRequestMethod() == Request.Method.POST) {
            String serviceTypeStr = ServiceType.getFieldForType(mServiceType);
            // Service type must be specified
            if (serviceTypeStr == null)
                return null;

            String vehicleTypeStr = VehicleType.getFieldForType(mVehicleType);
            // Vehicle type must be specified
            if (vehicleTypeStr == null)
                return null;

            JSONObject jsonObject = new JSONObject();
            jsonObject.put(ServiceStationRequestMaker.JSON_FIELD_SERVICE_CATEGORIES, vehicleTypeStr);
            jsonObject.put(ServiceStationRequestMaker.JSON_FIELD_SERVICE_CARS, new JSONArray(mCarMakes));

            // Currently, filtering on back-end accepts only one type of work.
            String workTypeStr = "";
            String subWorkTypeStr = "";

            if (mWorkTypes != null && !mWorkTypes.isEmpty()
                    && mSubWorkTypes != null && !mSubWorkTypes.isEmpty()) {
                workTypeStr = ServiceWorkType.getFieldForType(mWorkTypes.get(0));
                subWorkTypeStr = ServiceSubWorkType.getFieldForType(mSubWorkTypes.get(0));
            }

            jsonObject.put(ServiceStationRequestMaker.JSON_FIELD_WORK_TYPE, workTypeStr);
            jsonObject.put(ServiceStationRequestMaker.JSON_FIELD_SERVICES, subWorkTypeStr);

            JSONObject chosenPlace = new JSONObject();

            Place location = mLocation;
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
                // Set default location as Israel.
                chosenPlace.put(ServiceStationRequestMaker.JSON_FIELD_SERVICE_LOCATION_LAT,
                        31.046051);
                chosenPlace.put(ServiceStationRequestMaker.JSON_FIELD_SERVICE_LOCATION_LONG,
                        34.85161199999993f);
                chosenPlace.put(ServiceStationRequestMaker.JSON_FIELD_SERVICE_LOCATION_ADDRESS,
                        "Israel");
                chosenPlace.put(ServiceStationRequestMaker.JSON_FIELD_SERVICE_LOCATION_ID,
                        "ChIJi8mnMiRJABURuiw1EyBCa2o");
            }
            jsonObject.put(ServiceStationRequestMaker.JSON_FIELD_SERVICE_CHOSEN_PLACE, chosenPlace);

            jsonObject.put(serviceTypeStr, true);

            return jsonObject;
        } else {
            // Return null because this is the wrong request method.
            return null;
        }
    }

    @Override
    public String getRequestString() {
        // Return null since this request does not send String requests.
        return null;
    }

    public void setId(long id) {
        mId = id;
    }

    public long getId() {
        return mId;
    }

    public void setCarMakes(ArrayList<String> carMakes) {
        mCarMakes = carMakes;
    }

    public ArrayList<String> getCarMakes() {
        return mCarMakes;
    }

    public void setLocation(Place location) {
        mLocation = location;
    }

    public Place getLocation() {
        return mLocation;
    }

    public void setServiceType(ServiceType serviceType) {
        mServiceType = serviceType;
    }

    public ServiceType getServiceType() {
        return mServiceType;
    }

    public void setWorkTypes(ArrayList<ServiceWorkType> workTypes) {
        mWorkTypes = workTypes;
    }

    public ArrayList<ServiceWorkType> getWorkTypes() {
        return mWorkTypes;
    }

    public void setSubWorkTypes(ArrayList<ServiceSubWorkType> subWorkTypes) {
        mSubWorkTypes = subWorkTypes;
    }

    public ArrayList<ServiceSubWorkType> getSubWorkTypes() {
        return mSubWorkTypes;
    }

    public void setVehicleType(VehicleType vehicleType) {
        mVehicleType = vehicleType;
    }

    public VehicleType getVehicleType() {
        return mVehicleType;
    }

    public static class Builder {

        private ArrayList<String> carMakes;
        private Place place;
        private ServiceType serviceType;
        private VehicleType vehicleType;
        private ArrayList<ServiceWorkType> workTypes;
        private ArrayList<ServiceSubWorkType> subWorkTypes;

        public ServiceStationDataRequest.Builder setCarMakes(ArrayList<String> carMakes) {
            this.carMakes = carMakes;
            return this;
        }

        public ServiceStationDataRequest.Builder setPlace(Place location) {
            this.place = location;
            return this;
        }

        public ServiceStationDataRequest.Builder setServiceType(ServiceType serviceType) {
            this.serviceType = serviceType;
            return this;
        }

        public ServiceStationDataRequest.Builder setVehicleType(VehicleType vehicleType) {
            this.vehicleType = vehicleType;
            return this;
        }

        public ServiceStationDataRequest.Builder setWorkTypes(ArrayList<ServiceWorkType> workType) {
            this.workTypes = workType;
            return this;
        }

        public ServiceStationDataRequest.Builder setSubWorkTypes(
                ArrayList<ServiceSubWorkType> subWorkTypes) {
            this.subWorkTypes = subWorkTypes;
            return this;
        }

        public ServiceStationDataRequest build() {
            return new ServiceStationDataRequest(carMakes, place, serviceType, vehicleType, workTypes,
                    subWorkTypes);
        }
    }
}
