package il.co.tel_ran.carservice.connection;

import android.content.Context;
import android.support.annotation.Nullable;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import il.co.tel_ran.carservice.UserType;

/**
 * Created by maxim on 14-Dec-16.
 */

public class UserAuthentication {

    public static final String JSON_FIELD_ID = "id";
    public static final String JSON_FIELD_IDUSER = "idUser";
    public static final String JSON_FIELD_EMAIL = "email";
    public static final String JSON_FIELD_PASSWORD = "password";
    public static final String JSON_FIELD_TYPE = "role";
    public static final String JSON_FIELD_TYPE_MASTER = "master";
    public static final String JSON_FIELD_TYPE_CLIENT = "client";
    public static final String JSON_FIELD_CREATE_DATETIME = "createdAt";
    public static final String JSON_FIELD_UPDATE_DATETIME = "updatedAt";

    public interface OnAuthenticationResponseListener {
        void onAuthenticationSuccess(UserType userType, JSONObject authenticationJSON);
        void onAuthenticationFailed(ServerResponseError error, @Nullable String message);
    }

    public static void authenticate(final Context context, final String email, final String password,
                                    final OnAuthenticationResponseListener listener) {
        // TODO: update to match back-end communication model.
        // Send HTTP GET with user's email
        ServerConnection.doHttpGet(context,
                ServerConnection.AUTHENTICATE_URL + '?' + JSON_FIELD_EMAIL + '=', email,
                new Response.Listener<String>() {
            // Handle response
            @Override
            public void onResponse(String response) {
                // Make sure we have a listener to pass the result to.
                if (listener != null) {
                    // Empty (or null) response means the email was not found and is incorrect.
                    if (response == null || response.isEmpty()) {
                        // Pass null message for the error message to be handled elsewhere.
                        listener.onAuthenticationFailed(ServerResponseError.INCORRECT_EMAIL, null);
                    } else {
                        try {
                            JSONArray responseJSONArray = new JSONArray(response);
                            JSONObject userJSONObject = responseJSONArray.getJSONObject(0);

                            // Check if password is correct.
                            if (validatePassword(password, userJSONObject.getString(JSON_FIELD_PASSWORD))) {
                                String userType = userJSONObject.getString(JSON_FIELD_TYPE);
                                if (userType.equals(JSON_FIELD_TYPE_MASTER)) {
                                    // User is authenticated as master
                                    listener.onAuthenticationSuccess(UserType.MASTER,
                                            userJSONObject);
                                } else if (userType.equals(JSON_FIELD_TYPE_CLIENT)) {
                                    // User is authenticated as client
                                    listener.onAuthenticationSuccess(UserType.CLIENT,
                                            userJSONObject);
                                } else {
                                    // Show error if we can't identify the type of this user.
                                    // Pass null message for the error message to be handled elsewhere.
                                    listener.onAuthenticationFailed(
                                            ServerResponseError.ROLE, null);
                                }
                            } else {
                                // Pass null message for the error message to be handled elsewhere.
                                listener.onAuthenticationFailed(
                                        ServerResponseError.INCORRECT_PASSWORD, null);

                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if (listener != null) {
                    listener.onAuthenticationFailed(ServerResponseError.getErrorFromVolley(error),
                            error.getLocalizedMessage());
                }
            }
        });
    }

    private static boolean validatePassword(String inputPassword, String comparePassword) {
        return inputPassword.equals(comparePassword);
    }
}
