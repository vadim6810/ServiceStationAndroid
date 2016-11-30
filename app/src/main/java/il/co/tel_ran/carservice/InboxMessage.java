package il.co.tel_ran.carservice;

/**
 * Created by Max on 29/11/2016.
 */

public class InboxMessage {

    // Expand this enum as required.
    public enum Source {
        USER
    }

    private long mId;

    private String mTitle;
    private String mMessage;

    private long mSubmitTimestamp;

    private long mSourceId;
    private Source mSourceType;

    public InboxMessage() {

    }

    public InboxMessage(long id, String title, String message, long timeStamp, long sourceId, Source source) {
        setId(id);
        setTitle(title);
        setMessage(message);
        setTimesamp(timeStamp);
        setSourceId(sourceId);
        setSourceType(source);
    }

    public InboxMessage(InboxMessage otherMessage) {
        if (otherMessage != null) {
            setId(otherMessage.getId());
            setTitle(otherMessage.getTitle());
            setMessage(otherMessage.getMessage());
            setTimesamp(otherMessage.getTimestamp());
            setSourceId(otherMessage.getSourceId());
            setSourceType(otherMessage.getSourceType());
        }
    }

    public boolean equals(InboxMessage otherMessage) {
        if (otherMessage == null)
            return false;

        if (mId != otherMessage.getId())
            return false;

        if (mTitle == null)
            return false;

        String otherMessageString = otherMessage.getTitle();
        if (mTitle != null) {
            if (otherMessageString == null)
                return false;
            if (!mTitle.equals(otherMessageString))
                return false;
        }

        if (mSubmitTimestamp != otherMessage.getTimestamp())
            return false;

        if (mSourceId != otherMessage.getSourceId())
            return false;

        Source sourceType = otherMessage.getSourceType();
        if (mSourceType != null) {
            if (sourceType == null)
                return false;
            if (!mSourceType.equals(sourceType))
                return false;
        }

        return true;
    }

    public void setId(long id) {
        mId = id;
    }

    public long getId() {
        return mId;
    }

    public void setTitle(String message) {
        mTitle = message;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setMessage(String message) {
        mMessage = message;
    }

    public String getMessage() {
        return mMessage;
    }

    public void setTimesamp(long timeStamp) {
        mSubmitTimestamp = timeStamp;
    }

    public long getTimestamp() {
        return mSubmitTimestamp;
    }

    public void setSourceId(long id) {
        mSourceId = id;
    }

    public long getSourceId() {
        return mSourceId;
    }

    public void setSourceType(Source sourceType) {
        mSourceType = sourceType;
    }

    public Source getSourceType() {
        return mSourceType;
    }
}
