package il.co.tel_ran.carservice.connection;

/**
 * Created by maxim on 24-Dec-16.
 */

import android.os.Bundle;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Defines what and how to request from back-end
 */
public abstract class DataRequest {

    private int mRequestMethod;
    private String mUrl;

    private Bundle mExtras;

    public DataRequest(int requestMethod, String url) {
        mRequestMethod  = requestMethod;
        mUrl = url;
    }

    public void setRequestMethod(int method) {
        mRequestMethod = method;
    }

    public int getRequestMethod() {
        return mRequestMethod;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    public String getUrl() {
        return mUrl;
    }

    /**
     * Define how should this request should handle as a GET.
     */
    public abstract String getRequestParameters();

    /**
     * Define the request JSON to send.
     * @return {@link JSONObject} containing request parameters.
     */
    public abstract JSONObject getRequestJSON() throws JSONException;

    /**
     * Define the request String to send.
     * @return {@link String} containing request parameters.
     */
    public abstract String getRequestString();

    public void putExtras(Bundle extras) {
        mExtras = extras;
    }

    public Bundle getExtras() {
        return mExtras;
    }
}
