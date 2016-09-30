package il.co.tel_ran.carservice;

import com.google.android.gms.location.places.Place;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

/**
 * Created by maxim on 9/29/2016.
 */

public class ServiceSearchQuery {

    private List<Place> mLocations;
    private EnumSet<ServiceType> mAvailableServices;

    public ServiceSearchQuery() {
        this(new ArrayList<Place>(), EnumSet.noneOf(ServiceType.class));
    }

    public ServiceSearchQuery(List<Place> locations) {
        this(locations, EnumSet.noneOf(ServiceType.class));
    }

    public ServiceSearchQuery(EnumSet<ServiceType> serviceTypes) {
        this(new ArrayList<Place>(), serviceTypes);
    }

    public ServiceSearchQuery(List<Place> locations, EnumSet<ServiceType> serviceTypes) {
        mLocations          = locations;
        mAvailableServices  = serviceTypes;
    }

    public void addLocation(Place place) {
        mLocations.add(place);
    }

    public void addLocations(List<Place> locations) {
        mLocations.addAll(locations);
    }

    public void removeLocation(Place place) {
        if (mLocations.contains(place)) {
            mLocations.remove(place);
        }
    }

    public List<Place> getLocations() {
        return mLocations;
    }

    public void toggleServiceType(ServiceType type, boolean toggle) {
        boolean contains = mAvailableServices.contains(type);
        if (toggle && !contains) {
            mAvailableServices.add(type);
        } else if (!toggle && contains){
            mAvailableServices.remove(type);
        }
    }

    public EnumSet<ServiceType> getAvailableServices() {
        return mAvailableServices;
    }
}
