package il.co.tel_ran.carservice.connection;

import com.android.volley.Request;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by maxim on 24-Dec-16.
 */

public class ReviewRequest extends DataRequest {

    public static final String JSON_FIELD_REVIEW_ID = "id";
    public static final String JSON_FIELD_REVIEW_COMMENT = "comment";
    public static final String JSON_FIELD_REVIEW_USERNAME = "userName";
    public static final String JSON_FIELD_REVIEW_USERID = "userId";
    public static final String JSON_FIELD_REVIEW_DATE = "date";
    public static final String JSON_FIELD_REVIEW_MASTERID = "masterId";
    public static final String JSON_FIELD_REVIEW_MASTERNAME = "masterName";
    public static final String JSON_FIELD_REVIEW_RATING = "rate";
    public static final String JSON_FIELD_REVIEW_CREATEDAT = "createdAt";
    public static final String JSON_FIELD_REVIEW_UPDATEDAT = "updatedAt";

    private long mId = -1;

    public ReviewRequest() {
        super(Request.Method.GET, ServerConnection.COMMENTS_URL);
    }

    public ReviewRequest(long id) {
        super(Request.Method.POST, ServerConnection.getPostUrlForComments());

        mId = id;
    }

    public void setId(long id) {
        mId = id;
    }

    public long getId() {
        return mId;
    }

    @Override
    public String getRequestParameters() {
        // Currently we don't require any request parameters.
        return "";
    }

    @Override
    public JSONObject getRequestJSON() throws JSONException {
        // Check if the request method is correct.
        if (getRequestMethod() == Request.Method.POST) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put(JSON_FIELD_REVIEW_ID, mId);
                return jsonObject;
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        // Return null if request method is wrong or we got an exception.
        return null;
    }

    @Override
    public String getRequestString() {
        return null;
    }
}
