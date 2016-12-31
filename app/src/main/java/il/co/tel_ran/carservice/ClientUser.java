package il.co.tel_ran.carservice;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import il.co.tel_ran.carservice.connection.ClientUserDataRequestMaker;

/**
 * Created by maxim on 10/28/2016.
 */

public class ClientUser extends User {

    public ClientUser() {

    }

    public ClientUser(User user) {
        super(user);
    }

    public ClientUser(ClientUser user) {
        super(user);

        setClientId(user.getClientId());
        setLogo(user.getLogo());
        setName(user.getName());
        setVehicles(user.getVehicles());
    }

    public void setClientId(long id) {
        mClientId = id;
    }

    public long getClientId() {
        return mClientId;
    }

    public void setLogo(String logo) {
        mLogo = logo ;
    }

    public String getLogo() {
        return mLogo;
    }

    public void setName(String name) {
        mName = name;
    }

    public String getName() {
        return mName;
    }

    public void setVehicles(Collection<VehicleData> vehicles) {
        mVehicles.clear();
        mVehicles.addAll(vehicles);
    }

    public List<VehicleData> getVehicles() {
        return mVehicles;
    }

    @Override
    public JSONObject persistData() {
        // Make sure parent class handles it's own data first.
        JSONObject data = super.persistData();

        try {
            if (mVehicles != null && !mVehicles.isEmpty()) {
                String[] cars = new String[mVehicles.size()];
                for (int i = 0; i < mVehicles.size(); i++) {
                    cars[i] = mVehicles.get(i).toPersistedString();
                }

                data.put(ClientUserDataRequestMaker.JSON_FIELD_CARS, cars);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return data;
    }

    @Override
    public boolean equals(User otherUser) {
        boolean isSuperEquals = super.equals(otherUser);

        if (!isSuperEquals)
            return false;

        if (!(otherUser instanceof ClientUser)) {
            return false;
        }

        ClientUser clientUser = (ClientUser) otherUser;

        if (mClientId != clientUser.getClientId())
            return false;

        if (!mName.equals(clientUser.getName()))
            return false;

        if (!mVehicles.equals(clientUser.getVehicles()))
            return false;

        if (!mLogo.equals(clientUser.getLogo()))
            return false;

        return true;
    }

    private long mClientId;

    private String mName;

    private List<VehicleData> mVehicles = new ArrayList<>();

    private String mLogo;
}
