package il.co.tel_ran.carservice;

import com.google.android.gms.location.places.Place;

import java.util.EnumSet;

/**
 * Created by maxim on 9/29/2016.
 */

public class ServiceSearchResult {

    private String mServiceName;
    private Place mLocation;
    private float mAvgRating; // Based on rating sum (each rating 0-5) divided by number of ratings submitted;
    private int mSubmittedRatings;
    private EnumSet<ServiceType> mAvailableServices = EnumSet.noneOf(ServiceType.class);
    private String mCityName;

    public ServiceSearchResult(String name, Place location, float avgRating, int submittedRatings,
                               EnumSet<ServiceType> availableServices, String cityName) {
        mServiceName        = name;
        mLocation           = location;
        mAvgRating          = avgRating;
        mSubmittedRatings   = submittedRatings;
        mAvailableServices  = availableServices;
        // TODO: Uncomment this when using real results.
        //mCityName           = Utils.parseCityNameFromAddress(location.getAddress());
    }

    public String getName() {
        return mServiceName;
    }

    public Place getLocation() {
        return mLocation;
    }

    public String getCityName() {
        return mCityName;
    }

    public float getAvgRating() {
        return mAvgRating;
    }

    public int getSubmittedRatings() {
        return mSubmittedRatings;
    }

    public EnumSet<ServiceType> getAvailableServices() {
        return mAvailableServices;
    }

    // TODO: Remove when using real results.
    public void setLocation(Place place) {
        mLocation = place;
    }

    public void setCityName(String cityName) {
        mCityName = cityName;
    }
}
