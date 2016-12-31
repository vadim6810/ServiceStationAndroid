package il.co.tel_ran.carservice;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Date;

import il.co.tel_ran.carservice.connection.UserAuthentication;

/**
 * Created by maxim on 10/28/2016.
 */

public class User implements Persisted, Serializable {

    private long mId;

    private String mEmail;
    private String mPassword;

    private Date mCreationDate;
    private Date mUpdateDate;

    public User() {

    }

    public User(User copyUser) {
        setEmail(copyUser.getEmail());
        setPassword(copyUser.getPassword());
        setId(copyUser.getId());
        setCreationDate(copyUser.getCreationDate());
        setUpdateDate(copyUser.getUpdateDate());
    }

    public User(String email, String password, long id, Date creationDate, Date updateDate) {
        setEmail(email);
        setPassword(password);
        setId(id);
        setCreationDate(creationDate);
        setUpdateDate(updateDate);
    }

    public void setEmail(String email) {
        mEmail = email;
    }

    public String getEmail() {
        return mEmail;
    }

    public void setPassword(String password) {
        mPassword = password;
    }

    public String getPassword() {
        return mPassword;
    }

    public void setId(long id) {
        mId = id;
    }

    public long getId() {
        return mId;
    }

    public void setCreationDate(Date date) {
        mCreationDate = date;
    }

    public Date getCreationDate() {
        return mCreationDate;
    }

    public void setUpdateDate(Date date) {
        mUpdateDate = date;
    }

    public Date getUpdateDate() {
        return mUpdateDate;
    }

    @Override
    public JSONObject persistData() {
        JSONObject data = new JSONObject();

        try {
            data.put(UserAuthentication.JSON_FIELD_EMAIL, mEmail);
            data.put(UserAuthentication.JSON_FIELD_PASSWORD, mPassword);
            data.put(UserAuthentication.JSON_FIELD_CREATE_DATETIME,
                    Utils.convertDateToDateTime(mCreationDate));
            data.put(UserAuthentication.JSON_FIELD_UPDATE_DATETIME,
                    Utils.convertDateToDateTime(mUpdateDate));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return data;
    }

    public boolean equals(User otherUser) {
        if (mId != otherUser.getId())
            return false;

        if (!mEmail.equals(otherUser.getEmail()))
            return false;

        if (!mPassword.equals(otherUser.getPassword()))
            return false;

        if (!mCreationDate.equals(otherUser.getCreationDate()))
            return false;

        if (!mUpdateDate.equals(otherUser.getUpdateDate()))
            return false;

        return true;
    }
}
