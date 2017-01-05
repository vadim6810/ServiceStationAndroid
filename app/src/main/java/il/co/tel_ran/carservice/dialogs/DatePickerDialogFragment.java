package il.co.tel_ran.carservice.dialogs;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;

import java.util.Calendar;
import java.util.Date;

/**
 * Created by Max on 17/11/2016.
 */

public class DatePickerDialogFragment extends DialogFragment {

    DatePickerDialog.OnDateSetListener mListener;

    public void setOnDateSelectedListener(DatePickerDialog.OnDateSetListener listener) {
        mListener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        int year, month, dayOfMonth;
        final Calendar calendar = Calendar.getInstance();

        Bundle args = getArguments();
        if (args != null) {
            Date date = (Date) args.getSerializable("date");
            calendar.setTime(date);
        }

        year = calendar.get(Calendar.YEAR);
        month = calendar.get(Calendar.MONTH);
        dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);

        return new DatePickerDialog(getContext(), mListener, year, month, dayOfMonth);
    }
}
