package il.co.tel_ran.carservice.connection;

import org.json.JSONObject;

/**
 * Created by maxim on 30-Dec-16.
 */

public class NewUserDataResult extends DataResult<JSONObject> {

    public NewUserDataResult(JSONObject[] jsonObjects) {
        super(Type.NEW_USER, jsonObjects);
    }
}
