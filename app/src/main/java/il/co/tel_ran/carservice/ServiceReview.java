package il.co.tel_ran.carservice;

import java.util.Date;

/**
 * Created by maxim on 20-Dec-16.
 */

public class ServiceReview {

    private final long mId;

    private final long mServiceId;
    private final String mServiceName;

    private final long mReviewerId;
    private final String mReviewerName;

    private final float mRating;

    private final String mComment;

    private final Date mDate;
    private final Date mCreatedAt;
    private final Date mUpdatedAt;

    public long getId() {
        return mId;
    }

    public long getServiceId() {
        return mServiceId;
    }

    public String getServiceName() {
        return mServiceName;
    }

    public long getReviewerId() {
        return mReviewerId;
    }

    public String getReviewerName() {
        return mReviewerName;
    }

    public float getRating() {
        return mRating;
    }

    public String getComment() {
        return mComment;
    }

    public Date getDate() {
        return mDate;
    }

    public Date getCreateDate() {
        return mCreatedAt;
    }

    public Date getUpdatedDate() {
        return mUpdatedAt;
    }

    private ServiceReview(long id, String serviceName, long serviceId, String reviewerName,
                          long reviewerId, float rating, String comment, Date date, Date createdAt,
                          Date updatedAt) {
        mId = id;
        mServiceName    = serviceName;
        mServiceId      = serviceId;
        mReviewerName   = reviewerName;
        mReviewerId     = reviewerId;
        mRating         = rating;
        mComment        = comment;
        mDate           = date;
        mCreatedAt      = createdAt;
        mUpdatedAt      = updatedAt;
    }

    public static class Builder {

        private long id;
        private String serviceName;
        private long serviceId;
        private String reviewerName;
        private long reviewerId;
        private float rating;
        private String comment;
        private Date date;
        private Date createdAt;
        private Date updatedAt;

        public ServiceReview.Builder setId(long id) {
            this.id = id;
            return this;
        }

        public ServiceReview.Builder setServiceName(String name) {
            this.serviceName = name;
            return this;
        }

        public ServiceReview.Builder setServiceId(long masterId) {
            this.serviceId = masterId;
            return this;
        }

        public ServiceReview.Builder setReviewerName(String name) {
            this.reviewerName = name;
            return this;
        }

        public ServiceReview.Builder setReviewerId(long userId) {
            this.reviewerId = userId;
            return this;
        }

        public ServiceReview.Builder setRating(float rating) {
            this.rating = rating;
            return this;
        }

        public ServiceReview.Builder setComment(String comment) {
            this.comment = comment;
            return this;
        }

        public ServiceReview.Builder setDate(Date date) {
            this.date = date;
            return this;
        }

        public ServiceReview.Builder setCreatedAt(Date createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public ServiceReview.Builder setUpdatedAt(Date updatedAt) {
            this.updatedAt = updatedAt;
            return this;
        }

        public ServiceReview build() {
            return new ServiceReview(id, serviceName, serviceId, reviewerName, reviewerId, rating,
                    comment, date, createdAt, updatedAt);
        }
    }
}
