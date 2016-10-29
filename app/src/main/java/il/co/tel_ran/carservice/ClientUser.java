package il.co.tel_ran.carservice;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by maxim on 10/28/2016.
 */

public class ClientUser extends User {

    private VehicleData mVehicleData;

    public ClientUser() {

    }

    public ClientUser(User user) {
        setName(user.getName());
        setEmail(user.getEmail());
    }

    public ClientUser(VehicleData vehicleData) {
        setVehicleData(vehicleData);
    }

    public void setVehicleData(VehicleData vehicleData) {
        mVehicleData = vehicleData;
    }

    public VehicleData getVehicleData() {
        return mVehicleData;
    }

    @Override
    public JSONObject persistData() {
        // Make sure parent class handles it's own data first.
        JSONObject data = super.persistData();

        try {
            if (mVehicleData != null) {
                data.put("vehicleMake", mVehicleData.getVehicleMake());
                data.put("vehicleModel", mVehicleData.getVehicleModel());
                data.put("vehicleYear", mVehicleData.getVehicleYear());
                data.put("vehicleEngine", mVehicleData.getVehicleModifications());
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return  data;
    }
}
