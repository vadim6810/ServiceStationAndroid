package il.co.tel_ran.carservice.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatSpinner;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import il.co.tel_ran.carservice.ClientUser;
import il.co.tel_ran.carservice.R;
import il.co.tel_ran.carservice.Utils;
import il.co.tel_ran.carservice.VehicleData;
import il.co.tel_ran.carservice.activities.ProfileActivity;
import il.co.tel_ran.carservice.activities.SignUpActivity;
import il.co.tel_ran.carservice.VehicleAPI;
import il.co.tel_ran.carservice.adapters.VehicleDataResultAdapter;

/**
 * Created by Max on 12/10/2016.
 */

public class RegistrationVehicleDetailsFragment extends RegistrationUserDetailsFragment
        implements AdapterView.OnItemSelectedListener, VehicleAPI.OnVehicleDataRetrieveListener,
        View.OnClickListener {

    private AppCompatSpinner mVehicleMakeSpinner;
    private AppCompatSpinner mVehicleModelSpinner;
    private AppCompatSpinner mEngineSpinner;
    private AppCompatSpinner mModelYearSpinner;
    private ProgressBar mVehicleMakeProgressBar;
    private ProgressBar mVehicleModelProgressBar;
    private ProgressBar mEngineProgressBar;

    private VehicleAPI mVehicleAPI;

    private ArrayAdapter<Integer> mModelYearsAdapter;

    private VehicleData mVehicleData = new VehicleData();

    private boolean mIsDialog;
    private boolean mIsUserDataLoaded;

    private Button mUpdateDetailsButton;

    // This is intened for use only when using this fragment as a dialog.
    public static RegistrationVehicleDetailsFragment getInstance(VehicleData vehicleData) {
        RegistrationVehicleDetailsFragment registrationVehicleDetailsFragment
                = new RegistrationVehicleDetailsFragment();

        Bundle args = new Bundle();
        args.putString("make", vehicleData.getVehicleMake());
        args.putString("model", vehicleData.getVehicleModel());
        args.putInt("year", vehicleData.getVehicleYear());
        args.putString("modifications", vehicleData.getVehicleModifications());
        args.putBoolean("as_dialog", true);
        registrationVehicleDetailsFragment.setArguments(args);

        return registrationVehicleDetailsFragment;
    }

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

        Bundle extras = getArguments();
        if (extras != null && !extras.isEmpty()) {
            mVehicleData.setVehicleMake(extras.getString("make"));
            mVehicleData.setVehicleModel(extras.getString("model"));
            mVehicleData.setVehicleYear(extras.getInt("year"));
            mVehicleData.setVehicleModifications(extras.getString("modifications"));

            mIsDialog = extras.getBoolean("as_dialog");
        }
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);

        if (mVehicleAPI != null) {
            mVehicleAPI.cancelRunningTasks();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_registration_step_vehicledetails, null);

        TextView mCaptionTextView = (TextView) layout.findViewById(
                R.id.vehicle_details_setup_caption_text_view);

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

        mUpdateDetailsButton = (Button) layout.findViewById(R.id.update_button);

        if (mIsDialog) {
            // Hide the caption when displaying dialog. This ensures only the title shows.
            mCaptionTextView.setVisibility(View.GONE);

            // Show the update button for dialog layout.
            mUpdateDetailsButton.setVisibility(View.VISIBLE);
            mUpdateDetailsButton.setOnClickListener(this);

            // Check if we have any vehicle data.
            // Vehicle make is the very basic for vehicle data, without other information is irrelevant.
            if (mVehicleData != null && !mVehicleData.getVehicleMake().isEmpty()) {
                mIsUserDataLoaded = false;
                // Disable spinners until user data is loaded.
                toggleSpinners(false);
            } else {
                mIsUserDataLoaded = true;
            }
        } else {
            mUpdateDetailsButton = null;
        }

        // Load vehicle makes from Vehicle API.
        mVehicleAPI.getVehicleData(new VehicleAPI.Request(VehicleAPI.RequestType.MAKE,
                VehicleAPI.JSON_BASE_URL + VehicleAPI.JSON_MAKE_API));
        return layout;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        VehicleAPI.Result result = null;

        VehicleAPI.RequestType requestType = null;
        if (parent != null) {
            switch (parent.getId()) {
                case R.id.vehicle_make_spinner:
                    // Different vehicle make was selected.
                    requestType = VehicleAPI.RequestType.MODEL;

                    result = (VehicleAPI.Result) parent.getItemAtPosition(position);
                    if (result != null) {
                        mVehicleData.setVehicleMake(result.getResult());
                    }
                    break;
                case R.id.vehicle_model_spinner:
                    // Different vehicle model was selected.
                    requestType = VehicleAPI.RequestType.MODIFICATION;

                    result = (VehicleAPI.Result) parent.getItemAtPosition(position);
                    if (result != null) {
                        mVehicleData.setVehicleModel(result.getResult());
                    }
                    break;
                case R.id.vehicle_year_spinner:
                    mVehicleData.setVehicleYear((Integer) parent.getItemAtPosition(position));
                    break;
                case R.id.engine_displacement_spinner:
                    result = (VehicleAPI.Result) parent.getItemAtPosition(position);
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

        String userVehicleData = null;
        Spinner updatedSpinner = null;

        switch (requestType) {
            case MAKE:
                updatedSpinner = mVehicleMakeSpinner;

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

                // Check if loading user's data is still required.
                if (!mIsUserDataLoaded && mVehicleData != null) {
                    userVehicleData = mVehicleData.getVehicleMake();
                }

                break;
            case MODEL:
                updatedSpinner = mVehicleModelSpinner;

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

                // Check if loading user's data is still required.
                if (!mIsUserDataLoaded && mVehicleData != null) {
                    userVehicleData = mVehicleData.getVehicleModel();

                    int modelYear = mVehicleData.getVehicleYear();
                    if (modelYear != -1) {
                        mModelYearSpinner.setSelection(mModelYearsAdapter
                                .getPosition(modelYear));
                    }
                }
                break;
            case MODIFICATION:
                updatedSpinner = mEngineSpinner;

                // Set the modification spinner back to visible.
                mEngineSpinner.setVisibility(View.VISIBLE);
                mEngineProgressBar.setVisibility(View.GONE);

                // Set the adapter to the requesting spinner.
                if (adapter != null)
                    mEngineSpinner.setAdapter(adapter);

                // Check if loading user's data is still required.
                if (!mIsUserDataLoaded && mVehicleData != null) {
                    userVehicleData = mVehicleData.getVehicleModifications();
                }

                // All user data is loaded at this point anyway.
                mIsUserDataLoaded = true;
                toggleSpinners(true);
                break;
        }

        if (userVehicleData != null && adapter != null) {
            // Get the position for this result (make/model/modification)
            int position = adapter.getPosition(userVehicleData);
            if (position != -1) {
                // Select the spinner (automatically starts the tasks as it calls spinner selection)
                updatedSpinner.setSelection(position);
            } else {
                // If there's any error on the way enable all spinners.
                mIsUserDataLoaded = true;
                toggleSpinners(true);
            }
        } else {
            // If there's any error on the way enable all spinners.
            mIsUserDataLoaded = true;
            toggleSpinners(true);
        }
    }

    @Override
    public boolean isNextStepEnabled() {
        return true;
    }

    public VehicleData getVehicleData() {
        return mVehicleData;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.update_button:
                FragmentActivity containerActivity = getActivity();
                if (containerActivity != null) {
                    try {
                        if (mVehicleAPI != null) {
                            List<VehicleAPI.GetVehicleDataTask> tasks = mVehicleAPI
                                    .getRunningTasks();

                            // Make sure user is not trying to update while we have running tasks.
                            if (tasks.isEmpty()) {
                                ProfileActivity profileActivity = (ProfileActivity) containerActivity;
                                // Pass the new information to profile activity.
                                profileActivity.updateVehicleDetails(getVehicleData());
                                // Dismiss the dialog
                                dismiss();
                            } else {
                                Toast.makeText(containerActivity,
                                        getString(R.string.data_still_loading_message),
                                        Toast.LENGTH_SHORT)
                                        .show();
                            }
                        }
                    } catch (ClassCastException e) {
                        e.printStackTrace();
                    }
                }
                break;
        }
    }

    private void toggleSpinners(boolean toggle) {
        mVehicleMakeSpinner.setEnabled(toggle);
        mVehicleModelSpinner.setEnabled(toggle);
        mModelYearSpinner.setEnabled(toggle);
        mEngineSpinner.setEnabled(toggle);
    }
}
