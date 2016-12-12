package il.co.tel_ran.carservice;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;

/**
 * Created by maxim on 10/28/2016.
 */

public class User implements Persisted, Serializable {

    protected String mName;
    protected String mEmail;

    protected long mId;

    public User() {

    }

    public User(User copyUser) {
        setName(copyUser.getName());
        setEmail(copyUser.getEmail());
        setId(copyUser.getId());
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

    public void setId(long id) {
        mId = id;
    }

    public long getId() {
        return mId;
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

    public boolean equals(User otherUser) {
        if (!mName.equals(otherUser.getName()))
            return false;

        if (!mEmail.equals(otherUser.getEmail()))
            return false;

        return true;
    }
}
