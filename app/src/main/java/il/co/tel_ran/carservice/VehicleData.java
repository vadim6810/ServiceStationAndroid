package il.co.tel_ran.carservice;

import java.io.Serializable;

/**
 * Created by maxim on 10/23/2016.
 */

public class VehicleData implements Serializable {
    private String mVehicleMake;
    private String mVehicleModel;
    private String mVehicleModifications;
    private int mVehicleYear = -1;

    public VehicleData(VehicleData otherVehicle) {
        setVehicleMake(otherVehicle.getVehicleMake());
        setVehicleModel(otherVehicle.getVehicleModel());
        setVehicleModifications(otherVehicle.getVehicleModifications());
        setVehicleYear(otherVehicle.getVehicleYear());
    }

    public VehicleData() {
    }

    public void setVehicleMake(String make) {
        mVehicleMake = make;
    }

    public String getVehicleMake() {
        return mVehicleMake;
    }

    public void setVehicleModel(String model) {
        mVehicleModel = model;
    }

    public String getVehicleModel() {
        return mVehicleModel;
    }

    public void setVehicleModifications(String modifications) {
        mVehicleModifications = modifications;
    }

    public String getVehicleModifications() {
        return mVehicleModifications;
    }

    public void setVehicleYear(int year) {
        mVehicleYear = year;
    }

    public int getVehicleYear() {
        return mVehicleYear;
    }

    public boolean equals(VehicleData otherVehicleData) {
        if (!mVehicleMake.equals(otherVehicleData.getVehicleMake()))
            return false;

        if (!mVehicleModel.equals(otherVehicleData.getVehicleModel()))
            return false;

        if (!mVehicleModifications.equals(otherVehicleData.getVehicleModifications()))
            return false;

        if (mVehicleYear != otherVehicleData.getVehicleYear())
            return false;

        return true;
    }

    @Override
    public String toString() {
        return mVehicleMake + ' ' + mVehicleModel + ' ' + Integer.toString(mVehicleYear) + ' ' + mVehicleModifications;
    }

    public String toPersistedString() {
        return mVehicleMake + ", " + mVehicleModel + ", " + Integer.toString(mVehicleYear) + ", " + mVehicleModifications;
    }

    public static VehicleData parseVehicleData(String vehicleDataString) {
        VehicleData parsedVehicleData = new VehicleData();

        if (vehicleDataString != null && !vehicleDataString.isEmpty()) {
            String[] vehicleProperties = vehicleDataString.split(",");
            if (vehicleProperties.length != 4)
                return null;

            parsedVehicleData.setVehicleMake(vehicleProperties[0]);
            parsedVehicleData.setVehicleModel(vehicleProperties[1]);
            parsedVehicleData.setVehicleYear(Integer.parseInt(vehicleProperties[2].trim()));
            parsedVehicleData.setVehicleModifications(vehicleProperties[3]);
        }

        return parsedVehicleData;
    }
}
