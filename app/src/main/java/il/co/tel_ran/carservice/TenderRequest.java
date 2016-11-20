package il.co.tel_ran.carservice;

import java.io.Serializable;
import java.util.Calendar;

/**
 * Created by maxim on 12-Nov-16.
 */

public class TenderRequest implements Serializable {

    public enum Status implements Serializable {
        CLOSED,
        OPENED,
        RESOLVED
    }

    private String mLocation;
    private String mLocationPlaceID;

    private String mServices;

    private VehicleData mVehicleData;

    private long mSubmitTimestamp = -1;
    private long mUpdateTimestamp = -1;

    private int mDeadlineYear;
    private int mDeadlineMonth;
    private int mDeadlineDay;

    private Status mStatus =  Status.OPENED;

    public TenderRequest() {

    }

    public TenderRequest(String place, String placeID, String services, VehicleData vehicleData) {
        mLocation        = place;
        mLocationPlaceID = placeID;
        mServices        = services;
        mVehicleData     = vehicleData;
    }

    public void setLocation(String place) {
        mLocation = place;
    }

    public String getLocation() {
        return mLocation;
    }

    public void setPlaceID(String id) {
        mLocationPlaceID = id;
    }

    public String getPlaceID() {
        return mLocationPlaceID;
    }

    public void setServices(String services) {
        mServices = services;
    }

    public String getServices() {
        return mServices;
    }

    public void setVehicleData(VehicleData vehicleData) {
        mVehicleData = vehicleData;
    }

    public VehicleData getVehicleData() {
        return mVehicleData;
    }

    public void setSubmitTimestamp(long time) {
        mSubmitTimestamp = time;
    }

    public long getSubmitTimeStamp() {
        return mSubmitTimestamp;
    }

    public void setUpdateTimestamp(long time) {
        mUpdateTimestamp = time;
    }

    public long getUpdateTimestamp() {
        return mUpdateTimestamp;
    }

    public void setDeadlineDate(int day, int month, int year) {
        mDeadlineYear = year;
        mDeadlineMonth = month;
        mDeadlineDay = day;
    }

    /**
     * Get deadline date.
     * @param type refers to type of date component.
     *             Calendar.YEAR - year. Calendar.MONTH - month. Calendar.DAY_OF_MONTH - day.
     * @return the component
     */
    public int getDeadline(int type) {
        switch (type) {
            case Calendar.YEAR:
                return mDeadlineYear;
            case Calendar.MONTH:
                return mDeadlineMonth;
            case Calendar.DAY_OF_MONTH:
                return mDeadlineDay;
        }

        return -1;
    }

    public void setStatus(Status status) {
        mStatus = status;
    }

    public Status getStatus() {
        return mStatus;
    }
}
