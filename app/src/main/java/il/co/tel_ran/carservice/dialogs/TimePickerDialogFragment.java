package il.co.tel_ran.carservice.dialogs;

import android.app.Dialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;

import java.util.Calendar;

/**
 * Created by maxim on 10/29/2016.
 */

public class TimePickerDialogFragment extends DialogFragment {

    TimePickerDialog.OnTimeSetListener mListener;

    public void setOnTimeListener(TimePickerDialog.OnTimeSetListener listener) {
        mListener = listener;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int hour, minute;
        Bundle args = getArguments();
        if (args == null) {
            // Use the current time as the default values for the picker
            final Calendar calendar = Calendar.getInstance();
            hour = calendar.get(Calendar.HOUR_OF_DAY);
            minute = calendar.get(Calendar.MINUTE);
        } else {
            hour = args.getInt("hour");
            minute = args.getInt("minute");
        }

        if (mListener == null)
            return null;

        // Create a new instance of TimePickerDialog and return it
        return new TimePickerDialog(getActivity(), mListener, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
    }
}
