package il.co.tel_ran.carservice;

/**
 * Created by maxim on 9/29/2016.
 */

import java.util.ArrayList;
import java.util.List;

/**
 * This class holds a ServiceStation as a result, could as well hold meta-data for other uses.
 */
public class ServiceSearchResult {

    private List<ServiceStation> mServiceStations = new ArrayList<>();

    public ServiceSearchResult() {

    }

    public ServiceSearchResult(List<ServiceStation> serviceStations) {
        if (serviceStations != null) {
            mServiceStations.addAll(serviceStations);
        }
    }

    public void addService(ServiceStation serviceStation) {
        if (serviceStation != null) {
            mServiceStations.add(serviceStation);
        }
    }

    public List<ServiceStation> getSerivces() {
        return mServiceStations;
    }
}
