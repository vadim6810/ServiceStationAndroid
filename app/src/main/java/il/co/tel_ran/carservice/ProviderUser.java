package il.co.tel_ran.carservice;

/**
 * Created by maxim on 10/28/2016.
 */

public class ProviderUser extends User {

    public ProviderUser() {

    }

    public ProviderUser(User user) {
        setName(user.getName());
        setEmail(user.getEmail());
    }
}
