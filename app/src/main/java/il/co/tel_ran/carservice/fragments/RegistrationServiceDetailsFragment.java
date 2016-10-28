package il.co.tel_ran.carservice.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import il.co.tel_ran.carservice.R;

/**
 * Created by maxim on 10/24/2016.
 */

public class RegistrationServiceDetailsFragment extends RegistrationUserDetailsFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_registration_step_servicedetails, null);
        return layout;
    }

    @Override
    public boolean isNextStepEnabled() {
        return true;
    }
}
