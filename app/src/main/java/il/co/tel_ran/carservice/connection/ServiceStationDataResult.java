package il.co.tel_ran.carservice.connection;

import il.co.tel_ran.carservice.ServiceStation;

/**
 * Created by maxim on 24-Dec-16.
 */

public class ServiceStationDataResult extends DataResult<ServiceStation> {

    public ServiceStationDataResult(ServiceStation[] data) {
        super(Type.SERVICE_STATION, data);
    }
}
