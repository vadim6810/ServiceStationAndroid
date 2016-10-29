package il.co.tel_ran.carservice.fragments;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatSpinner;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import il.co.tel_ran.carservice.ClientUser;
import il.co.tel_ran.carservice.R;
import il.co.tel_ran.carservice.Utils;
import il.co.tel_ran.carservice.VehicleData;
import il.co.tel_ran.carservice.activities.SignUpActivity;
import il.co.tel_ran.carservice.VehicleAPI;
import il.co.tel_ran.carservice.adapters.VehicleDataResultAdapter;

/**
 * Created by Max on 12/10/2016.
 */

public class RegistrationVehicleDetailsFragment extends RegistrationUserDetailsFragment
        implements AdapterView.OnItemSelectedListener, VehicleAPI.OnVehicleDataRetrieveListener {

    private ProgressBar mVehicleApiDataProgressBar;

    private View mVehicleDetailsLayout;
    private AppCompatSpinner mVehicleMakeSpinner;
    private AppCompatSpinner mVehicleModelSpinner;
    private AppCompatSpinner mEngineSpinner;
    private AppCompatSpinner mModelYearSpinner;
    private ProgressBar mVehicleMakeProgressBar;
    private ProgressBar mVehicleModelProgressBar;
    private ProgressBar mEngineProgressBar;

    private VehicleAPI mVehicleAPI;

    private ArrayAdapter<Integer> mModelYearsAdapter;

    private VehicleData mVehicleData;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        // Generate model years adapter.
        int minYear = 1980;
        int maxYear = 2017;
        Integer[] modelYears = new Integer[maxYear - minYear + 1];
        for (int j = 0, i = minYear; i <= maxYear; j++, i++) {
            modelYears[j] = i;
        }
        mModelYearsAdapter = new ArrayAdapter<>(getContext(),
                R.layout.support_simple_spinner_dropdown_item, modelYears);

        mVehicleAPI = new VehicleAPI(this);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_registration_step_vehicledetails, null);

        mVehicleDetailsLayout = layout.findViewById(R.id.vehicle_details_layout);

        mVehicleMakeSpinner = (AppCompatSpinner) layout.findViewById(R.id.vehicle_make_spinner);
        mVehicleMakeSpinner.setOnItemSelectedListener(this);
        mVehicleMakeProgressBar = (ProgressBar) layout.findViewById(R.id.vehicle_make_results_progress_bar);
        mVehicleModelSpinner = (AppCompatSpinner) layout.findViewById(R.id.vehicle_model_spinner);
        mVehicleModelSpinner.setOnItemSelectedListener(this);
        mVehicleModelProgressBar = (ProgressBar) layout.findViewById(R.id.vehicle_model_results_progress_bar);
        mModelYearSpinner = (AppCompatSpinner) layout.findViewById(R.id.vehicle_year_spinner);
        mModelYearSpinner.setAdapter(mModelYearsAdapter);
        mModelYearSpinner.setOnItemSelectedListener(this);
        mEngineSpinner = (AppCompatSpinner) layout.findViewById(R.id.engine_displacement_spinner);
        mEngineSpinner.setOnItemSelectedListener(this);
        mEngineProgressBar = (ProgressBar) layout.findViewById(R.id.vehicle_engine_displacement_results_progress_bar);

        // Load vehicle makes from Vehicle API.
        mVehicleAPI.getVehicleData(new VehicleAPI.Request(VehicleAPI.RequestType.MAKE,
                VehicleAPI.JSON_BASE_URL + VehicleAPI.JSON_MAKE_API));
        return layout;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        VehicleAPI.Result result = null;
        if (parent != null) {
            result = (VehicleAPI.Result) parent.getItemAtPosition(position);
        }

        VehicleAPI.RequestType requestType = null;
        if (parent != null) {
            switch (parent.getId()) {
                case R.id.vehicle_make_spinner:
                    // Different vehicle make was selected.
                    requestType = VehicleAPI.RequestType.MODEL;

                    if (result != null) {
                        mVehicleData.setVehicleMake(result.getResult());
                    }
                    break;
                case R.id.vehicle_model_spinner:
                    // Different vehicle model was selected.
                    requestType = VehicleAPI.RequestType.MODIFICATION;

                    if (result != null) {
                        mVehicleData.setVehicleModel(result.getResult());
                    }
                    break;
                case R.id.vehicle_year_spinner:
                    if (result != null) {
                        mVehicleData.setVehicleYear(Integer.parseInt(result.getResult()));
                    }
                    break;
                case R.id.engine_displacement_spinner:
                    if (result != null) {
                        mVehicleData.setVehicleModifications(result.getResult());
                    }
                    break;
            }
        }

        if (requestType != null) {
            // Get additional data for next spinner.
            if (result != null) {
                VehicleAPI.Request request = new VehicleAPI.Request(requestType,
                        VehicleAPI.JSON_BASE_URL + result.getExtraURL());
                mVehicleAPI.getVehicleData(request);
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    @Override
    public void onVehicleDataRetrievingStarted(VehicleAPI.RequestType requestType) {
        switch (requestType) {
            case MAKE:
                // Set all spinners visibility to gone until they receive their results.
                mVehicleMakeSpinner.setVisibility(View.GONE);
                // The only visible view would be the Make Spinner progress bar.
                mVehicleMakeProgressBar.setVisibility(View.VISIBLE);

                mVehicleModelSpinner.setVisibility(View.GONE);
                mVehicleModelProgressBar.setVisibility(View.GONE);
                mModelYearSpinner.setVisibility(View.GONE);
                mEngineSpinner.setVisibility(View.GONE);
                mEngineProgressBar.setVisibility(View.GONE);
                break;
            case MODEL:
                // Set model, year and modification spinners visibility to gone.
                mVehicleModelSpinner.setVisibility(View.GONE);
                // Set the model progress bar to visible.
                mVehicleModelProgressBar.setVisibility(View.VISIBLE);
                mModelYearSpinner.setVisibility(View.GONE);
                mEngineSpinner.setVisibility(View.GONE);
                mEngineProgressBar.setVisibility(View.GONE);
                break;
            case MODIFICATION:
                // Set the modification spinner visibility to gone.
                mEngineSpinner.setVisibility(View.GONE);
                // Set the modification progress bar to visible.
                mEngineProgressBar.setVisibility(View.VISIBLE);
                break;
        }
    }

    @Override
    public void onVehicleDataRetrieved(VehicleAPI.RequestType requestType,
                                       List<VehicleAPI.Result> results) {
        VehicleDataResultAdapter adapter = null;

        if (!results.isEmpty()) {
            // Convert the results ArrayList to an Array because we use an instance of ArrayAdapter.
            VehicleAPI.Result[] resultsArray = new VehicleAPI.Result[results.size()];
            results.toArray(resultsArray);
            // Apply the new results to an adapter.
            adapter = new VehicleDataResultAdapter(getContext(),
                    R.layout.support_simple_spinner_dropdown_item, resultsArray);
        }

        switch (requestType) {
            case MAKE:
                // Now that we received our results set the make spinner to visible again.
                // Everything else (including progress bars) is still at gone.
                // They will appear once they retrieve their individual results.
                mVehicleMakeSpinner.setVisibility(View.VISIBLE);
                mVehicleMakeProgressBar.setVisibility(View.GONE);

                mVehicleModelSpinner.setVisibility(View.GONE);
                mVehicleModelProgressBar.setVisibility(View.GONE);
                mEngineSpinner.setVisibility(View.GONE);
                mEngineProgressBar.setVisibility(View.GONE);

                // Set the adapter to the requesting spinner.
                if (adapter != null)
                    mVehicleMakeSpinner.setAdapter(adapter);
                break;
            case MODEL:
                // Set the spinner back to visible once we got the results.
                mVehicleModelSpinner.setVisibility(View.VISIBLE);
                mVehicleModelProgressBar.setVisibility(View.GONE);
                // Year spinner can be visible since it doesn't require any asynchronous work.
                mModelYearSpinner.setVisibility(View.VISIBLE);
                mEngineSpinner.setVisibility(View.GONE);
                mEngineProgressBar.setVisibility(View.GONE);

                // Set the adapter to the requesting spinner.
                if (adapter != null)
                    mVehicleModelSpinner.setAdapter(adapter);
                break;
            case MODIFICATION:
                // Set the modification spinner back to visible.
                mEngineSpinner.setVisibility(View.VISIBLE);
                mEngineProgressBar.setVisibility(View.GONE);

                // Set the adapter to the requesting spinner.
                if (adapter != null)
                    mEngineSpinner.setAdapter(adapter);
                break;
        }
    }

    @Override
    public boolean isNextStepEnabled() {
        return true;
    }

    public VehicleData getVehicleData() {
        return mVehicleData;
    }
}
