package il.co.tel_ran.carservice;

/**
 * Created by maxim on 9/29/2016.
 */

/**
 * This class holds a ServiceStation as a result, could as well hold meta-data for other uses.
 */
public class ServiceSearchResult {

    private ServiceStation mServiceStation;

    public ServiceSearchResult() {

    }

    public ServiceSearchResult(ServiceStation serviceStation) {
        mServiceStation = serviceStation;
    }

    public void setService(ServiceStation serviceStation) {
        mServiceStation = serviceStation;
    }

    public ServiceStation getSerivce() {
        return mServiceStation;
    }
}
