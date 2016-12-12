package il.co.tel_ran.carservice;

import com.google.android.gms.location.places.Place;

import java.util.Arrays;
import java.util.EnumSet;

/**
 * Created by maxim on 13-Nov-16.
 */

public class ServiceStation {

    private long mID; // Service ID number

    private String mServiceName;

    private String mPlaceId;
    private Place mLocation;
    private String mCityName;

    private float mAvgRating; // Based on rating sum (each rating 0-5) divided by number of ratings submitted;
    private int mSubmittedRatings;

    private EnumSet<ServiceType> mAvailableServices = EnumSet.noneOf(ServiceType.class);

    private String mPhonenumber;
    private String mEmail;

    private TimeHolder mOpeningTime;
    private TimeHolder mClosingTime;

    private EnumSet<VehicleType> mVehicleTypes = EnumSet.noneOf(VehicleType.class);

    private String mDirectorName;
    private String mDirectorPhonenumber;

    private String mManagerName;

    private String[] mServicedCarMakes;

    public ServiceStation() {

    }

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

    // Copy constructor
    public ServiceStation(ServiceStation otherService) {
        if (otherService == null)
            return;
        mID = otherService.getID();
        mServiceName = otherService.getName();
        mPlaceId = otherService.getPlaceId();
        mLocation = otherService.getLocation();
        mCityName = otherService.getCityName();
        mAvgRating = otherService.getAvgRating();
        mSubmittedRatings = otherService.getSubmittedRatings();
        mAvailableServices = EnumSet.copyOf(otherService.getAvailableServices());
        mPhonenumber = otherService.getPhonenumber();
        mEmail = otherService.getEmail();
        mOpeningTime = otherService.getOpeningTime();
        mClosingTime = otherService.getClosingTime();
        mVehicleTypes = EnumSet.copyOf(otherService.getVehicleTypes());
        mDirectorName = otherService.getDirectorName();
        mDirectorPhonenumber = otherService.getDirectorPhonenumber();
        mServicedCarMakes = otherService.getServicedCarMakes();
        mManagerName = otherService.getManagerName();
    }

    public void setName(String name) {
        mServiceName = name;
    }

    public String getName() {
        return mServiceName;
    }

    public void setLocation(Place place) {
        mLocation = place;
    }

    public Place getLocation() {
        return mLocation;
    }

    public void setCityName(String name) {
        mCityName = name;
    }

    public String getCityName() {
        return mCityName;
    }

    public void setAvgRating(float rating) {
        mAvgRating = rating;
    }

    public float getAvgRating() {
        return mAvgRating;
    }

    public void setSubmittedRatings(int ratings) {
        mSubmittedRatings = ratings;
    }

    public int getSubmittedRatings() {
        return mSubmittedRatings;
    }

    public void toggleService(ServiceType serviceType, boolean toggle) {
        if (toggle) {
            if (!mAvailableServices.contains(serviceType)) {
                mAvailableServices.add(serviceType);
            }
        } else {
            if (mAvailableServices.contains(serviceType)) {
                mAvailableServices.remove(serviceType);
            }
        }
    }

    public EnumSet<ServiceType> getAvailableServices() {
        return mAvailableServices;
    }

    public void setPhonenumber(String phonenumber) {
        mPhonenumber = phonenumber;
    }

    public String getPhonenumber() {
        return mPhonenumber;
    }

    public void setEmail(String email) {
        mEmail = email;
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

    public void setOpeningTime(TimeHolder time) {
        mOpeningTime = time;
    }

    public void setOpeningTime(int hour, int minute) {
        mOpeningTime = new TimeHolder(hour, minute);
    }

    public TimeHolder getOpeningTime() {
        return mOpeningTime;
    }

    public void setClosingTime(TimeHolder time) {
        mClosingTime = time;
    }

    public void setClosingTime(int hour, int minute) {
        mClosingTime = new TimeHolder(hour, minute);
    }

    public TimeHolder getClosingTime() {
        return mClosingTime;
    }

    public void toggleVehicleType(VehicleType vehicleType, boolean toggle) {
        if (toggle) {
            if (!mVehicleTypes.contains(vehicleType)) {
                mVehicleTypes.add(vehicleType);
            }
        } else {
            if (mVehicleTypes.contains(vehicleType)) {
                mVehicleTypes.remove(vehicleType);
            }
        }
    }

    public EnumSet<VehicleType> getVehicleTypes() {
        return mVehicleTypes;
    }

    public void setDirectorName(String name) {
        mDirectorName = name;
    }

    public String getDirectorName() {
        return mDirectorName;
    }

    public void setDirectorPhonenumber(String phonenumber) {
        mDirectorPhonenumber = phonenumber;
    }

    public String getDirectorPhonenumber() {
        return mDirectorPhonenumber;
    }

    public void setServicedCarMakes(String[] carMakes) {
        mServicedCarMakes = carMakes;
    }

    public String[] getServicedCarMakes() {
        return mServicedCarMakes;
    }

    public void setManagerName(String name) {
        mManagerName = name;
    }

    public String getManagerName() {
        return mManagerName;
    }

    public void setPlaceId(String placeId) {
        mPlaceId = placeId;
    }

    public String getPlaceId() {
        return mPlaceId;
    }

    public boolean equals(ServiceStation otherService) {
        if (otherService == null)
            return false;

        if (mServiceName != null && !mServiceName.equals(otherService.getName()))
            return false;

        if (mID != otherService.getID())
            return false;

        if (mLocation != null && !mLocation.equals(otherService.getLocation()))
            return false;

        if (mPlaceId != null && mPlaceId.equals(otherService.getPlaceId()))
            return false;

        if (mCityName != null && !mCityName.equals(otherService.getCityName()))
            return false;

        if (mAvgRating != otherService.getAvgRating())
            return false;

        if (mSubmittedRatings != otherService.getSubmittedRatings())
            return false;

        if (mAvailableServices != null && !mAvailableServices.equals(otherService.getAvailableServices()))
            return false;

        if (mPhonenumber != null && !mPhonenumber.equals(otherService.getPhonenumber()))
            return false;

        if (mEmail != null && !mEmail.equals(otherService.getEmail()))
            return false;

        if (mOpeningTime != null && mOpeningTime.compare(otherService.getOpeningTime()) != 0)
            return false;

        if (mClosingTime != null && mClosingTime.compare(otherService.getClosingTime()) != 0)
            return false;

        if (mVehicleTypes != null && !mVehicleTypes.equals(otherService.getVehicleTypes()))
            return false;

        if (mDirectorName != null && !mDirectorName.equals(otherService.getDirectorName()))
            return false;

        if (mDirectorPhonenumber != null && !mDirectorPhonenumber.equals(otherService.getDirectorPhonenumber()))
            return false;

        if (mServicedCarMakes != null && !Arrays.equals(mServicedCarMakes, otherService.getServicedCarMakes()))
            return false;

        if (mManagerName != null && !mManagerName.equals(otherService.getManagerName()))
            return false;

        return true;
    }
}
