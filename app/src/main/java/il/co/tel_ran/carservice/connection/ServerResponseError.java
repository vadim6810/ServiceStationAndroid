package il.co.tel_ran.carservice.connection;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.ParseError;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;

/**
 * Created by maxim on 16-Dec-16.
 */

public enum ServerResponseError {
    TIMEOUT,
    NO_CONNECTION,
    AUTH_FAILURE,
    SERVER,
    NETWORK,
    PARSE,
    INCORRECT_EMAIL,
    INCORRECT_PASSWORD,
    ROLE,
    INVALID_PARAMETER,
    USER_NOT_FOUND,
    SERVICE_NOT_FOUND,
    INVALID_ID, REVIEW_NOT_FOUND, VEHICLE_DB_EMPTY;

    public static ServerResponseError getErrorFromVolley(VolleyError volleyError) {
        if (volleyError instanceof TimeoutError) {
            return ServerResponseError.TIMEOUT;
        } else if (volleyError instanceof NoConnectionError) {
            return ServerResponseError.NO_CONNECTION;
        } else if (volleyError instanceof AuthFailureError) {
            return ServerResponseError.AUTH_FAILURE;
        } else if (volleyError instanceof ServerError) {
            return ServerResponseError.SERVER;
        } else if (volleyError instanceof NetworkError) {
            return ServerResponseError.NETWORK;
        } else if (volleyError instanceof ParseError) {
            return ServerResponseError.PARSE;
        }

        return null;
    }
}
