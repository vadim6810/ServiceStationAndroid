package il.co.tel_ran.carservice.connection;

import org.json.JSONObject;

/**
 * Created by maxim on 30-Dec-16.
 */

public class NewAuthenticationDataResult extends DataResult<JSONObject> {
    public NewAuthenticationDataResult(JSONObject[] data) {
        super(Type.NEW_AUTHENTICATION, data);
    }
}
