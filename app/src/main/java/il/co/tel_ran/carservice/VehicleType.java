package il.co.tel_ran.carservice;

import il.co.tel_ran.carservice.connection.ServiceStationRequestMaker;

/**
 * Created by Max on 21/11/2016.
 */

public enum VehicleType {
    PRIVATE,
    MOTORCYCLE,
    BUS,
    TRUCK;

    public static String getFieldForType(VehicleType vehicleType) {
        if (vehicleType == null)
            return null;

        switch (vehicleType) {
            case PRIVATE:
                return ServiceStationRequestMaker.JSON_FIELD_SERVICE_TYPE_PASSCAR;
            case MOTORCYCLE:
                return ServiceStationRequestMaker.JSON_FIELD_SERVICE_TYPE_BICYCLE;
            case BUS:
                return ServiceStationRequestMaker.JSON_FIELD_SERVICE_TYPE_BUS;
            case TRUCK:
                return ServiceStationRequestMaker.JSON_FIELD_SERVICE_TYPE_LORRY;
        }

        return null;
    }
}
