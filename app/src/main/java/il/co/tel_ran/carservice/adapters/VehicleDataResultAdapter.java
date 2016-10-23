package il.co.tel_ran.carservice.adapters;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import il.co.tel_ran.carservice.VehicleAPI;

/**
 * Created by maxim on 10/23/2016.
 */

public class VehicleDataResultAdapter extends ArrayAdapter<VehicleAPI.Result> {

    private ArrayList<VehicleAPI.Result> mResults;

    public VehicleDataResultAdapter(Context context, int resource, VehicleAPI.Result[] results) {
        super(context, resource, results);

        mResults = new ArrayList<>();

        for (VehicleAPI.Result result : results) {
            mResults.add(result);
        }
    }

    @Nullable
    @Override
    public VehicleAPI.Result getItem(int position) {
        return mResults.get(position);
    }

    @Override
    public void add(VehicleAPI.Result result) {
        mResults.add(result);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);

        // Set the spinner item text to the relevant result.
        if (view instanceof TextView) {
            TextView textView = (TextView) view;
            textView.setText(mResults.get(position).getResult());
        }

        return view;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View view = super.getView(position, convertView, parent);

        // Set the drop down item text to the relevant result.
        if (view instanceof TextView) {
            TextView textView = (TextView) view;
            textView.setText(mResults.get(position).getResult());
        }

        return view;
    }

    @Override
    public int getCount() {
        return mResults.size();
    }
}
