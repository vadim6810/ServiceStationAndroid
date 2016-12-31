package il.co.tel_ran.carservice;

import com.google.android.gms.common.api.GoogleApiClient;

import il.co.tel_ran.carservice.connection.ServerConnection;
import il.co.tel_ran.carservice.fragments.RecentServicesTabFragment;
import il.co.tel_ran.carservice.fragments.RequestServiceTabFragment;

/**
 * Created by Max on 09/10/2016.
 */

public class ClientActivityRetainedData extends RetainedData {

    private GoogleApiClient mGoogleApiClient;

    private ServerConnection mServerConnection;

    private RecentServicesTabFragment mRecentServicesTabFragment;
    private RequestServiceTabFragment mRequestServiceTabFragment;

    public ClientActivityRetainedData(GoogleApiClient googleApiClient,
                                      ServerConnection serverConnection,
                                      RecentServicesTabFragment recentServicesTabFragment,
                                      RequestServiceTabFragment requestServiceTabFragment) {
        mGoogleApiClient = googleApiClient;
        mServerConnection = serverConnection;
        mRecentServicesTabFragment = recentServicesTabFragment;
        mRequestServiceTabFragment = requestServiceTabFragment;
    }

    public GoogleApiClient getGoogleApiClient() {
        return mGoogleApiClient;
    }

    public ServerConnection getServerConnection() {
        return mServerConnection;
    }

    public RecentServicesTabFragment getRecentServicesTabFragment() {
        return mRecentServicesTabFragment;
    }

    public RequestServiceTabFragment getRequestServiceTabFragment() {
        return mRequestServiceTabFragment;
    }
}
