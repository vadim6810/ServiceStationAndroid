package il.co.tel_ran.carservice.connection;

import android.content.Context;

import com.android.volley.Request;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.Date;

import il.co.tel_ran.carservice.ServiceReview;
import il.co.tel_ran.carservice.Utils;

/**
 * Created by maxim on 24-Dec-16.
 */

public class ReviewRequestMaker extends RequestMaker {

    public ReviewRequestMaker(OnDataRetrieveListener listener) {
        super(listener, DataResult.Type.REVIEW);
    }

    @Override
    public void makeRequest(Context context, DataRequest dataRequest) {
        Request request;
        if (dataRequest.getRequestMethod() == Request.Method.POST) {
            // Make JSON requests for POST method.
            request = makeJSONArrayRequest(dataRequest);
        } else {
            // Make String requests for GET method.
            request = makeStringRequest(dataRequest);
        }

        // Add request to queue
        RequestQueueSingleton.getInstance(context).addToRequestQueue(request);
    }

    @Override
    protected void handleResponse(DataRequest dataRequest, String response) {
        try {
            handleResponse(dataRequest, new JSONArray(response));
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void handleResponse(DataRequest dataRequest, JSONArray response) {
        OnDataRetrieveListener listener = getListener();
        if (listener != null) {
            if (response == null) {
                // Pass null message for the error message to be handled elsewhere.
                listener.onDataRetrieveFailed(dataRequest, getResultType(),
                        ServerResponseError.INVALID_PARAMETER, null);
            } else {
                // We could get multiple service response
                ServiceReview[] reviews
                        = new ServiceReview[response.length()];

                if (reviews.length == 0) {
                    // Pass null message for the error message to be handled elsewhere.
                    listener.onDataRetrieveFailed(dataRequest, getResultType(),
                            ServerResponseError.SERVICE_NOT_FOUND, null);
                    return;
                }

                try {
                    for (int i = 0; i < response.length(); i++) {
                        ServiceReview review = parseReviewFromJSONObject(
                                response.getJSONObject(i));
                        reviews[i] = review;
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                ReviewDataResult result = new ReviewDataResult(reviews);

                listener.onDataRetrieveSuccess(dataRequest, result);
            }
        }
    }

    @Override
    protected void handleResponse(DataRequest request, JSONObject response) {
        // Not used in this request maker.
    }

    private ServiceReview parseReviewFromJSONObject(JSONObject jsonObject) {
        try {
            long id = jsonObject.getLong(ReviewRequest.JSON_FIELD_REVIEW_ID);

            String comment = jsonObject.getString(ReviewRequest.JSON_FIELD_REVIEW_COMMENT);

            String userName = jsonObject.getString(ReviewRequest.JSON_FIELD_REVIEW_USERNAME);
            long userId = Long.valueOf(jsonObject.getString(ReviewRequest.JSON_FIELD_REVIEW_USERID));

            String dateString = jsonObject.getString(ReviewRequest.JSON_FIELD_REVIEW_DATE);
            Date date = Utils.parseDateTime(dateString);

            long masterId = Long.valueOf(jsonObject.getString(ReviewRequest.JSON_FIELD_REVIEW_MASTERID));
            String masterName = jsonObject.getString(ReviewRequest.JSON_FIELD_REVIEW_MASTERNAME);

            float rating = 0.0f;
            if (!jsonObject.isNull(ReviewRequest.JSON_FIELD_REVIEW_RATING)) {
                rating = (float) jsonObject.getDouble(ReviewRequest.JSON_FIELD_REVIEW_RATING);
            }

            String createdAtString = jsonObject.getString(ReviewRequest.JSON_FIELD_REVIEW_CREATEDAT);
            Date createdAt = Utils.parseDateTime(createdAtString);

            String updatedAtString = jsonObject.getString(ReviewRequest.JSON_FIELD_REVIEW_UPDATEDAT);
            Date updatedAt = Utils.parseDateTime(updatedAtString);

            return new ServiceReview.Builder()
                    .setId(id)
                    .setComment(comment)
                    .setReviewerName(userName)
                    .setReviewerId(userId)
                    .setDate(date)
                    .setServiceName(masterName)
                    .setServiceId(masterId)
                    .setRating(rating)
                    .setCreatedAt(createdAt)
                    .setUpdatedAt(updatedAt)
                    .build();
        } catch (JSONException | ParseException e) {
            e.printStackTrace();
        }

        return null;
    }
}
