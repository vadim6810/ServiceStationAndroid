package il.co.tel_ran.carservice;

import java.io.Serializable;

/**
 * Created by maxim on 10/28/2016.
 */

public class ProviderUser extends User implements Serializable {

    public ProviderUser() {

    }

    public ProviderUser(long id) {
        mMasterId = id;
    }

    public ProviderUser(User user) {
        super(user);
    }

    public ProviderUser(ProviderUser providerUser) {
        super(providerUser);

        setMasterId(providerUser.getMasterId());
    }

    @Override
    public boolean equals(User otherUser) {
        boolean isSuperEquals = super.equals(otherUser);

        if (!isSuperEquals)
            return false;

        if (!(otherUser instanceof ProviderUser)) {
            return false;
        }

        if (mMasterId != ((ProviderUser) otherUser).getMasterId())
            return false;

        return true;
    }

    public void setMasterId(long id) {
        mMasterId = id;
    }

    public long getMasterId() {
        return mMasterId;
    }

    private long mMasterId;

    public void setService(ServiceStation serviceStation) {
    }

    public ServiceStation getService() {
        return null;
    }
}
