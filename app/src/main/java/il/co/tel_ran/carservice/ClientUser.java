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
        setId(user.getId());
    }

    public ClientUser(ClientUser user) {
        this((User) user);

        setVehicleData(user.getVehicleData());
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

    @Override
    public boolean equals(User otherUser) {
        boolean isSuperEquals = super.equals(otherUser);

        if (!isSuperEquals)
            return false;

        if (!(otherUser instanceof ClientUser)) {
            return false;
        }

        VehicleData otherVehicleData = ((ClientUser) otherUser).getVehicleData();
        if (!mVehicleData.equals(otherVehicleData))
            return false;

        return true;
    }
}
