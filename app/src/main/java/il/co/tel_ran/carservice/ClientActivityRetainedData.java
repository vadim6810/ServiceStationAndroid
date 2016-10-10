package il.co.tel_ran.carservice;

import com.google.android.gms.common.api.GoogleApiClient;

/**
 * Created by Max on 09/10/2016.
 */

public class ClientActivityRetainedData extends RetainedData {

    private GoogleApiClient mGoogleApiClient;

    private ServerConnection mServerConnection;

    private RecentServicesTabFragment mRecentServicesTabFragment;
    private SearchServiceTabFragment mSearchServiceTabFragment;
    private RequestServiceTabFragment mRequestServiceTabFragment;

    public ClientActivityRetainedData(GoogleApiClient googleApiClient,
                                      ServerConnection serverConnection,
                                      RecentServicesTabFragment recentServicesTabFragment,
                                      SearchServiceTabFragment searchServiceTabFragment,
                                      RequestServiceTabFragment requestServiceTabFragment) {
        mGoogleApiClient = googleApiClient;
        mServerConnection = serverConnection;
        mRecentServicesTabFragment = recentServicesTabFragment;
        mSearchServiceTabFragment = searchServiceTabFragment;
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

    public SearchServiceTabFragment getSearchServiceTabFragment() {
        return mSearchServiceTabFragment;
    }

    public RequestServiceTabFragment getRequestServiceTabFragment() {
        return mRequestServiceTabFragment;
    }
}
