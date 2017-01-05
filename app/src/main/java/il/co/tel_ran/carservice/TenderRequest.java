package il.co.tel_ran.carservice;

import com.google.android.gms.location.places.Place;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
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

    private long mId;
    private long mIdUser;

    private String mSender;

    private String mLocation;
    private String mLocationPlaceID;
    private transient Place mLocationPlace;

    private float mPrice;

    private VehicleData mVehicleData;
    private EnumSet<VehicleType> mVehicleTypes;

    private ArrayList<ServiceWorkType> mWorkTypes = new ArrayList<>();
    private ArrayList<ServiceSubWorkType> mSubWorkTypes = new ArrayList<>();

    private String mMessage;

    private Date mCreatedAtDate;
    private Date mUpdatedAtDate;

    private Date mDeadlineDate;

    private Status mStatus =  Status.OPENED;

    public TenderRequest() {

    }

    private TenderRequest(long id, long idUser, String location, String placeID,
                          VehicleData vehicleData, float price, EnumSet<VehicleType> vehicleTypes,
                          ArrayList<ServiceWorkType> workTypes,
                          ArrayList<ServiceSubWorkType> subWorkTypes, String message,
                          Date createdAtDate, Date updatedAtDate, Date deadlineDate, Status status,
                          String sender, Place place) {
        mId              = id;
        mIdUser          = idUser;
        mLocation        = location;
        mLocationPlaceID = placeID;
        mVehicleData     = vehicleData;
        mPrice           = price;
        mVehicleTypes    = vehicleTypes;
        mWorkTypes       = workTypes;
        mSubWorkTypes    = subWorkTypes;
        mMessage         = message;
        mCreatedAtDate   = createdAtDate;
        mUpdatedAtDate   = updatedAtDate;
        mDeadlineDate    = deadlineDate;
        mStatus          = status;
        mSender          = sender;
        mLocationPlace   = place;
    }

    public static class Builder {

        private long id;
        private long idUser;
        private String location;
        private String locationPlaceId;
        private Place place;
        private float price;
        private VehicleData vehicleData;
        private EnumSet<VehicleType> vehicleTypes;
        private ArrayList<ServiceWorkType> serviceWorkTypes = new ArrayList<>();
        private ArrayList<ServiceSubWorkType> serviceSubWorkTypes = new ArrayList<>();
        private String message;
        private Date createdAtDate;
        private Date updatedAtDate;
        private Date deadlineDate;
        private String sender;
        private Status status;

        public Builder setId(long id){
            this.id = id;
            return this;
        }

        public Builder setIdUser(long idUser){
            this.idUser = idUser;
            return this;
        }

        public Builder setLocation(String location) {
            this.location = location;
            return this;
        }

        public Builder setPlace(Place place) {
            this.place = place;
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

        public Builder setCreatedAtDate(Date createdAtDate) {
            this.createdAtDate = createdAtDate;
            return this;
        }

        public Builder setUpdatedAtDate(Date updatedAtDate) {
            this.updatedAtDate = updatedAtDate;
            return this;
        }

        public Builder setDeadlineDate(Date date) {
            deadlineDate = date;
            return this;
        }

        public Builder setStatus(Status status) {
            this.status = status;
            return this;
        }

        public Builder setSender(String sender) {
            this.sender = sender;
            return this;
        }

        public TenderRequest build() {
            return new TenderRequest(id, idUser, location, locationPlaceId, vehicleData, price,
                    vehicleTypes, serviceWorkTypes, serviceSubWorkTypes, message, createdAtDate,
                    updatedAtDate, deadlineDate, status, sender, place);
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

    public void setPlace(Place place) {
        mLocationPlace = place;
    }

    public Place getPlace() {
        return mLocationPlace;
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

    public void setCreatedAtDate(Date createdAtDate) {
        mCreatedAtDate = createdAtDate;
    }

    public Date getCreatedAtDate() {
        return mCreatedAtDate;
    }

    public void setUpdatedAtDate(Date updatedAtDate) {
        mUpdatedAtDate = updatedAtDate;
    }

    public Date getUpdatedAtDate() {
        return mUpdatedAtDate;
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

    public void setDeadlineDate(Date date) {
        mDeadlineDate = date;
    }

    public Date getDeadlineDate() {
        return mDeadlineDate;
    }

    public void setStatus(Status status) {
        mStatus = status;
    }

    public Status getStatus() {
        return mStatus;
    }

    public void setSender(String sender) {
        mSender = sender;
    }

    public String getSender() {
        return mSender;
    }

    public void setId(long id) {
        mId = id;
    }

    public long getId() {
        return mId;
    }

    public void setIdUser(long iduser) {
        mIdUser = iduser;
    }

    public long getIdUser() {
        return mIdUser;
    }
}
