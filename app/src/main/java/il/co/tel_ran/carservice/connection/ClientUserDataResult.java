package il.co.tel_ran.carservice.connection;

import il.co.tel_ran.carservice.ClientUser;

/**
 * Created by maxim on 31-Dec-16.
 */

public class ClientUserDataResult extends DataResult<ClientUser> {

    public ClientUserDataResult(ClientUser[] data) {
        super(Type.CLIENT_USER, data);
    }
}
