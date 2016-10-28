package il.co.tel_ran.carservice;

import java.util.EnumSet;

/**
 * Created by maxim on 9/29/2016.
 */

public enum ServiceType {
    SERVICE_TYPE_CAR_WASH,
    SERVICE_TYPE_TUNING,
    SERVICE_TYPE_TYRE_REPAIR,
    SERVICE_TYPE_AC_REPAIR_REFILL;

    public static EnumSet<ServiceType> decode(int value) {
        EnumSet<ServiceType> result = EnumSet.noneOf(ServiceType.class);
        // Iterate through all available services.
        for (ServiceType serviceType : ServiceType.values()) {
            // Get value for the service type.
            int compare = 1 << serviceType.ordinal();
            // Check if given value contains the service type value using bitwise and.
            if ((value & compare) == compare) {
                result.add(serviceType);
            }
        }

        return result;
    }

    public static int encode(EnumSet<ServiceType> set) {
        int value = 0;
        for (ServiceType serviceType : set) {
            // Get value for the service type.
            int compare = 1 << serviceType.ordinal();
            // Add the value to the total sum.
            value |= compare;
        }

        return value;
    }
}
