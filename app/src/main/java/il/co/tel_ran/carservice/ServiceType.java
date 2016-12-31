package il.co.tel_ran.carservice;

import il.co.tel_ran.carservice.connection.ServiceStationRequestMaker;

/**
 * Created by maxim on 9/29/2016.
 */

public enum ServiceType {
    CAR_WASH,
    TOWING,
    TYRE_REPAIR,
    AUTO_SERVICE;

    public static String getFieldForType(ServiceType serviceType) {
        if (serviceType == null)
            return null;

        switch (serviceType) {
            case CAR_WASH:
                return ServiceStationRequestMaker.JSON_FIELD_SERVICE_TYPE_CAR_WASH;
            case TOWING:
                return ServiceStationRequestMaker.JSON_FIELD_SERVICE_TYPE_TOW_TRUCK;
            case TYRE_REPAIR:
                return ServiceStationRequestMaker.JSON_FIELD_SERVICE_TYPE_MOUNTING;
            case AUTO_SERVICE:
                return ServiceStationRequestMaker.JSON_FIELD_SERVICE_TYPE_MECHANICS;
        }

        return null;
    }
}
