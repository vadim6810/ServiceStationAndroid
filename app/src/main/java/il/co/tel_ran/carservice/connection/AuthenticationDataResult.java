package il.co.tel_ran.carservice.connection;

import org.json.JSONObject;

/**
 * Created by maxim on 30-Dec-16.
 */

public class AuthenticationDataResult extends DataResult<JSONObject> {
    public AuthenticationDataResult(JSONObject[] data) {
        super(Type.AUTHENTICATION, data);
    }
}
