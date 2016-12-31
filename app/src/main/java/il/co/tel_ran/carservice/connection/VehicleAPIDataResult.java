package il.co.tel_ran.carservice.connection;

import il.co.tel_ran.carservice.VehicleExtendedData;

/**
 * Created by maxim on 30-Dec-16.
 */

public class VehicleAPIDataResult extends DataResult<VehicleExtendedData> {

    public VehicleAPIDataResult(VehicleExtendedData[] data) {
        super(Type.VEHICLE_API, data);
    }
}
