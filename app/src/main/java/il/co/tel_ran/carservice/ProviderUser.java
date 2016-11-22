package il.co.tel_ran.carservice;

/**
 * Created by maxim on 10/28/2016.
 */

public class ProviderUser extends User {

    private ServiceStation mService;

    public ProviderUser() {

    }

    public ProviderUser(User user) {
        setName(user.getName());
        setEmail(user.getEmail());
    }

    public ProviderUser(ProviderUser providerUser) {
        setName(providerUser.getName());
        setEmail(providerUser.getEmail());
        setService(new ServiceStation(providerUser.getService()));
    }

    @Override
    public boolean equals(User otherUser) {
        boolean isSuperEquals = super.equals(otherUser);

        if (!isSuperEquals)
            return false;

        if (!(otherUser instanceof ProviderUser)) {
            return false;
        }

        if (mService != ((ProviderUser) otherUser).getService())
            return false;

        if (mService != null && !mService.equals(((ProviderUser) otherUser).getService()))
            return false;

        return true;
    }

    public void setService(ServiceStation serviceStation) {
        mService = serviceStation;
    }

    public ServiceStation getService() {
        return mService;
    }
}
