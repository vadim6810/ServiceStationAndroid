package il.co.tel_ran.carservice;

import java.io.Serializable;

/**
 * Created by maxim on 12-Nov-16.
 */

public class TenderRequest implements Serializable {

    private String mLocation;
    private String mLocationPlaceID;

    private String mServices;

    private VehicleData mVehicleData;

    public TenderRequest() {

    }

    public TenderRequest(String place, String placeID, String services, VehicleData vehicleData) {
        mLocation        = place;
        mLocationPlaceID = placeID;
        mServices        = services;
        mVehicleData     = vehicleData;
    }

    public void setLocation(String place) {
        mLocation = place;
    }

    public String getLocation() {
        return mLocation;
    }

    public void setPlaceID(String id) {
        mLocationPlaceID = id;
    }

    public String getPlaceID() {
        return mLocationPlaceID;
    }

    public void setServices(String services) {
        mServices = services;
    }

    public String getServices() {
        return mServices;
    }

    public void setVehicleData(VehicleData vehicleData) {
        mVehicleData = vehicleData;
    }

    public VehicleData getVehicleData() {
        return mVehicleData;
    }
}
