package il.co.tel_ran.carservice.fragments;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;

import il.co.tel_ran.carservice.RetainedData;

/**
 * Keeps activity's data on configuration change.
 * Refer to: https://developer.android.com/guide/topics/resources/runtime-changes.html
 * for more information.
 */
public class RetainedFragment extends Fragment {

    public static final String CLIENT_MAIN_ACTIVITY_RETAINED_FRAGMENT_TAG = "clientMainActivityData";

    private RetainedData mDataObject;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    public void setData(RetainedData dataObject) {
        mDataObject = dataObject;
    }

    public RetainedData getData() {
        return mDataObject;
    }
}
