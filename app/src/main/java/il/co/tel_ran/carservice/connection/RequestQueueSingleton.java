package il.co.tel_ran.carservice.connection;

/**
 * Created by maxim on 24-Dec-16.
 */

import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

/**
 * Singleton class to that provides RequestQueue functionality.
 */
public class RequestQueueSingleton {

    private static RequestQueueSingleton mInstance;

    private final Context mContext;

    private RequestQueue mRequestQueue;

    private RequestQueueSingleton(Context context) {
        mContext = context;

        mRequestQueue = getRequestQueue();
    }

    public static synchronized RequestQueueSingleton getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new RequestQueueSingleton(context);
        }
        return mInstance;
    }

    public RequestQueue getRequestQueue() {
        if (mRequestQueue == null) {
            // getApplicationContext() is key, it keeps you from leaking the
            // Activity or BroadcastReceiver if someone passes one in.
            mRequestQueue = Volley.newRequestQueue(mContext.getApplicationContext());
        }
        return mRequestQueue;
    }

    public <T> void addToRequestQueue(Request<T> req) {
        getRequestQueue().add(req);
    }

    public void cancelRequests(Object tag) {
        getRequestQueue().cancelAll(tag);
    }
}
