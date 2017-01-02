package il.co.tel_ran.carservice;

import com.google.android.gms.location.places.Place;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.EnumSet;

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

    private float mPrice;

    private VehicleData mVehicleData;
    private EnumSet<VehicleType> mVehicleTypes;

    private ArrayList<ServiceWorkType> mWorkTypes = new ArrayList<>();
    private ArrayList<ServiceSubWorkType> mSubWorkTypes = new ArrayList<>();

    private String mMessage;

    private long mSubmitTimestamp = -1;
    private long mUpdateTimestamp = -1;

    private int mDeadlineYear;
    private int mDeadlineMonth;
    private int mDeadlineDay;

    private Status mStatus =  Status.OPENED;

    public TenderRequest() {

    }

    private TenderRequest(String place, String placeID, VehicleData vehicleData, float price,
                          EnumSet<VehicleType> vehicleTypes, ArrayList<ServiceWorkType> workTypes,
                          ArrayList<ServiceSubWorkType> subWorkTypes, String message,
                          long submitTimeStamp, long updateTimeStamp, int deadlineYear,
                          int deadlineMonth, int deadlineDay, Status status) {
        mLocation        = place;
        mLocationPlaceID = placeID;
        mVehicleData     = vehicleData;
        mPrice           = price;
        mVehicleTypes    = vehicleTypes;
        mWorkTypes       = workTypes;
        mSubWorkTypes    = subWorkTypes;
        mMessage         = message;
        mSubmitTimestamp = submitTimeStamp;
        mUpdateTimestamp = updateTimeStamp;
        mDeadlineYear    = deadlineYear;
        mDeadlineMonth   = deadlineMonth;
        mDeadlineDay     = deadlineDay;
        mStatus          = status;
    }

    public static class Builder {

        private String location;
        private String locationPlaceId;
        private float price;
        private VehicleData vehicleData;
        private EnumSet<VehicleType> vehicleTypes;
        private ArrayList<ServiceWorkType> serviceWorkTypes = new ArrayList<>();
        private ArrayList<ServiceSubWorkType> serviceSubWorkTypes = new ArrayList<>();
        private String message;
        private long submitTimeStamp;
        private long updateTimeStamp;
        private int deadlineYear;
        private int deadlineMonth;
        private int deadlineDay;
        private Status status;

       public Builder setLocation(String location) {
           this.location = location;
           return this;
       }

        public Builder setLocationPlaceId(String locationPlaceId) {
            this.locationPlaceId = locationPlaceId;
            return this;
        }

        public Builder setPrice(float price) {
            this.price = price;
            return this;
        }

        public Builder setVehicleData(VehicleData vehicleData) {
            this.vehicleData = vehicleData;
            return this;
        }

        public Builder setVehicleTypes(EnumSet<VehicleType> vehicleTypes) {
            this.vehicleTypes = vehicleTypes;
            return this;
        }

        public Builder setWorkTypes(ArrayList<ServiceWorkType> workTypes) {
            this.serviceWorkTypes = workTypes;
            return this;
        }

        public Builder setSubWorkTypes(ArrayList<ServiceSubWorkType> subWorkTypes) {
            this.serviceSubWorkTypes = subWorkTypes;
            return this;
        }

        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }

        public Builder setSubmitTimeStamp(long submitTimeStamp) {
            this.submitTimeStamp = submitTimeStamp;
            return this;
        }

        public Builder setUpdateTimeStamp(long updateTimeStamp) {
            this.updateTimeStamp = updateTimeStamp;
            return this;
        }

        public Builder setDeadlineYear(int deadlineYear) {
            this.deadlineYear = deadlineYear;
            return this;
        }

        public Builder setDeadlineMonth(int deadlineMonth) {
            this.deadlineMonth = deadlineMonth;
            return this;
        }

        public Builder setDeadlineDay(int deadlineDay) {
            this.deadlineDay = deadlineDay;
            return this;
        }

        public Builder setStatus(Status status) {
            this.status = status;
            return this;
        }

        public TenderRequest build() {
            return new TenderRequest(location, locationPlaceId, vehicleData, price, vehicleTypes,
                    serviceWorkTypes, serviceSubWorkTypes, message, submitTimeStamp,
                    updateTimeStamp, deadlineYear, deadlineMonth, deadlineDay, status);
        }
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

    public void setPrice(float price) {
        mPrice = price;
    }

    public float getPrice() {
        return mPrice;
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

    public void setVehicleTypes(EnumSet<VehicleType> vehicleTypes) {
        mVehicleTypes = vehicleTypes;
    }

    public EnumSet<VehicleType> getVehicleTypes() {
        return mVehicleTypes;
    }

    public void setWorkTypes(ArrayList<ServiceWorkType> workTypes) {
        mWorkTypes = workTypes;
    }

    public ArrayList<ServiceSubWorkType> getSubWorkTypes() {
        return mSubWorkTypes;
    }

    public ArrayList<ServiceWorkType> getWorkTypes() {
        return mWorkTypes;
    }

    public void setMessage(String message) {
        mMessage = message;
    }

    public String getMessage() {
        return mMessage;
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
