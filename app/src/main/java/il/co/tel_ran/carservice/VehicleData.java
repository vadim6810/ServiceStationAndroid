package il.co.tel_ran.carservice;

/**
 * Created by maxim on 10/23/2016.
 */

public class VehicleData {
    private String mVehicleMake;
    private String mVehicleModel;
    private String mVehicleModifications;
    private int mVehicleYear;

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
}
