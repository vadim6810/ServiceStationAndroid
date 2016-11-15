package il.co.tel_ran.carservice;

import com.google.android.gms.location.places.Place;

import java.util.EnumSet;

/**
 * Created by maxim on 13-Nov-16.
 */

public class ServiceStation {

    private String mServiceName;
    private Place mLocation;
    private float mAvgRating; // Based on rating sum (each rating 0-5) divided by number of ratings submitted;
    private int mSubmittedRatings;
    private EnumSet<ServiceType> mAvailableServices = EnumSet.noneOf(ServiceType.class);
    private String mCityName;
    private String mPhonenumber;
    private String mEmail;

    private long mID; // Service ID number

    public ServiceStation(String name, Place location, float avgRating, int submittedRatings,
                          EnumSet<ServiceType> availableServices, String cityName,
                          String phoneNumber, String email) {
        mServiceName        = name;
        mLocation           = location;
        mAvgRating          = avgRating;
        mSubmittedRatings   = submittedRatings;
        mAvailableServices  = availableServices;
        mPhonenumber        = phoneNumber;
        mEmail              = email;
        mCityName           = cityName;
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

    public String getPhonenumber() {
        return mPhonenumber;
    }

    public String getEmail() {
        return mEmail;
    }

    public void setID(long id) {
        mID = id;
    }

    public long getID() {
        return mID;
    }
}
