package il.co.tel_ran.carservice;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by maxim on 10/28/2016.
 */

public class User implements Persisted {

    protected String mName;
    protected String mEmail;

    public User() {

    }

    public User(String name, String email) {
        setName(name);
        setEmail(email);
    }

    public void setName(String name) {
        mName = name;
    }

    public String getName() {
        return mName;
    }

    public void setEmail(String email) {
        mEmail = email;
    }

    public String getEmail() {
        return mEmail;
    }

    @Override
    public JSONObject persistData() {
        JSONObject data = new JSONObject();

        try {
            data.put("name", mName);
            data.put("email", mEmail);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return data;
    }
}
