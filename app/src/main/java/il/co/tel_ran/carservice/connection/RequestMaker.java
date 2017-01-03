package il.co.tel_ran.carservice.connection;

import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import il.co.tel_ran.carservice.CustomJsonArrayRequest;

/**
 * Created by maxim on 24-Dec-16.
 */

public abstract class RequestMaker {

    private final DataResult.Type mResultType;

    private OnDataRetrieveListener mListener;

    public interface OnDataRetrieveListener{
        void onDataRetrieveSuccess(DataRequest dataRequest, DataResult result);
        void onDataRetrieveFailed(DataRequest dataRequest, DataResult.Type resultType,
                                  ServerResponseError error, @Nullable String message);
    }

    public RequestMaker(OnDataRetrieveListener listener, DataResult.Type type) {
        mListener = listener;
        mResultType = type;
    }

    public abstract void makeRequest(Context context, DataRequest dataRequest);

    public OnDataRetrieveListener getListener() {
        return mListener;
    }

    public DataResult.Type getResultType() {
        return mResultType;
    }

    protected abstract void handleResponse(DataRequest request, String response);

    protected abstract void handleResponse(DataRequest request, JSONArray response);

    protected abstract void handleResponse(DataRequest request, JSONObject response);

    protected void handleError(DataResult.Type resultType, DataRequest request, VolleyError error) {
        if (mListener != null) {
            mListener.onDataRetrieveFailed(request, mResultType,
                    ServerResponseError.getErrorFromVolley(error), error.getLocalizedMessage());
        }
    }

    protected Request makeStringRequest(final DataRequest dataRequest) {
        // Build the request
        return new StringRequest(
                dataRequest.getRequestMethod(),
                dataRequest.getUrl() + getParameters(dataRequest),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        handleResponse(dataRequest, response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                handleError(mResultType, dataRequest, error);
            }
        });
    }

    protected Request makeJSONArrayRequest(final DataRequest dataRequest) {
        // Build the request
        try {
            return new CustomJsonArrayRequest(
                    dataRequest.getRequestMethod(),
                    dataRequest.getUrl() + getParameters(dataRequest),
                    dataRequest.getRequestJSON(),
                    new Response.Listener<JSONArray>() {
                        @Override
                        public void onResponse(JSONArray response) {
                            handleResponse(dataRequest, response);
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    handleError(mResultType, dataRequest, error);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    protected Request makeJSONObjectRequest(final DataRequest dataRequest) {
        // Build the request
        try {
            return new JsonObjectRequest(
                    dataRequest.getRequestMethod(),
                    dataRequest.getUrl() + getParameters(dataRequest),
                    dataRequest.getRequestJSON(),
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            handleResponse(dataRequest, response);
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    handleError(mResultType, dataRequest, error);
                }
            });
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return null;
    }

    private String getParameters(DataRequest dataRequest) {
        // Get parameters
        String params = dataRequest.getRequestParameters();
        if (params == null) {
            params = dataRequest.getRequestString();
            if (params == null) {
                params = "";
            }
        }

        return params;
    }
}
