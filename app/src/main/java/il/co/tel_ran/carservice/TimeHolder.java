package il.co.tel_ran.carservice;

/**
 * Created by maxim on 10/29/2016.
 */

import java.util.Locale;

/**
 * Simple class to keep hour & minute.
 */
public class TimeHolder {

    private int mHour;
    private int mMinute;

    public TimeHolder() {

    }

    public TimeHolder(int hour, int minute) {
        mHour = hour;
        mMinute = minute;
    }

    public void setHour(int hour) {
        mHour = hour;
    }

    public int getHour() {
        return mHour;
    }

    public void setMinute(int minute) {
        mMinute = minute;
    }

    public int getMinute() {
        return mMinute;
    }

    @Override
    public String toString() {
        return Integer.toString(mHour) + ':' + String.format(Locale.getDefault(), "%02d", mMinute);
    }

    /**
     *
     * @param compareTime
     * @return 0 - equal time, 1 - this time is later, 2 - compared time is later.
     */
    public int compare(TimeHolder compareTime) {

        if (compareTime.getHour() > this.getHour()) {
            return 2;
        }

        if (compareTime.getHour() == this.getHour()) {
            if (compareTime.getMinute() > this.getMinute()) {
                return 2;
            } else if (compareTime.getMinute() == this.getMinute()) {
                return 0;
            } else {
                return 1;
            }
        }

        return 1;
    }

    public static TimeHolder parseTime(String time) {
        String[] timePart = time.split(":");
        int hour = Integer.parseInt(timePart[0]);
        int minute = Integer.parseInt(timePart[1]);

        return new TimeHolder(hour, minute);
    }
}
