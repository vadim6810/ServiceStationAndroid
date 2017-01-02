package il.co.tel_ran.carservice.connection;

/**
 * Created by maxim on 24-Dec-16.
 */

/**
 * Defines the result from back-end. Holds the data retrieved (after being parsed).
 * Could also hold additional meta-data
 */
public class DataResult<T> {

    public enum Type {
        SERVICE_STATION,
        REVIEW,
        NEW_USER,
        AUTHENTICATION,
        VEHICLE_API,
        CLIENT_USER
    }

    private T[] mData;

    private Type mDataType;

    public DataResult(Type dataType, T[] data) {
        mData = data;
        mDataType = dataType;
    }

    public T[] getData() {
        return mData;
    }

    public DataResult.Type getDataType() {
        return mDataType;
    }
}
