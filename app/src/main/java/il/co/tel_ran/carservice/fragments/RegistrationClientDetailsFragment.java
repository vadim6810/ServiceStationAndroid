package il.co.tel_ran.carservice.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.AppCompatSpinner;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.hotmail.maximglukhov.arrangedlayout.ArrangedLayout;
import com.hotmail.maximglukhov.chipview.ChipView;

import java.util.ArrayList;
import java.util.List;

import il.co.tel_ran.carservice.ClientUser;
import il.co.tel_ran.carservice.R;
import il.co.tel_ran.carservice.User;
import il.co.tel_ran.carservice.Utils;
import il.co.tel_ran.carservice.VehicleData;
import il.co.tel_ran.carservice.VehicleExtendedData;

/**
 * Created by maxim on 30-Dec-16.
 */

public class RegistrationClientDetailsFragment extends RegistrationUserDetailsFragment
        implements View.OnClickListener, VehicleMakesFragment.SelectVehicleMakesDialogListener,
        AdapterView.OnItemSelectedListener, ChipView.OnChipDeleteClickListener {

    private View mLayout;

    private TextInputEditText mNameInputEditText;

    private ArrangedLayout mVehiclesArrangedLayout;

//    private View mVehicleDetailsLayout;
    private Button mShowVehicleMakesButton;
    private AppCompatSpinner mModelSpinner;

    private TextInputLayout mYearTextInputLayout;
    private TextInputEditText mYearInputEditText;

    private TextInputLayout mEngineDisplacementTextInputLayout;
    private TextInputEditText mEngineInputEditText;

    private View mAddVehicleLayout;
    private Button mAddVehiclesButton;
    private ImageButton mCollapseAddVehicleButton;

    private ArrayList<VehicleExtendedData> mVehicleAPIData;

    private ClientUser mUser = new ClientUser();

    private VehicleData mCurrentEditedVehicleData;

    public ClientUser getUser() {
        return mUser;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        Bundle extras = getArguments();
        if (extras != null && !extras.isEmpty()) {
            mUser = (ClientUser) extras.getSerializable("user");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mLayout = inflater.inflate(R.layout.fragment_registration_step_clientdetails, null);

        mNameInputEditText = (TextInputEditText) mLayout.findViewById(R.id.name_input_edit_text);
        mNameInputEditText.addTextChangedListener(new NameTextWatcher());

        mVehiclesArrangedLayout = (ArrangedLayout) mLayout.findViewById(
                R.id.vehicles_arranged_layout);

        setupVehicleDetailsLayout();

        updateFieldsFromUser();

        return mLayout;
    }

    public void updateFieldsFromUser() {
        updateFieldsFromUser(mUser);
    }

    public void updateFieldsFromUser(ClientUser user) {
        if (user != null) {
            String name = user.getName();
            if (name != null && !name.isEmpty()) {
                mNameInputEditText.setText(name);
            }

            List<VehicleData> vehicles = user.getVehicles();
            if (vehicles != null && !vehicles.isEmpty()) {
                for (VehicleData currentVehicle : vehicles) {
                    mCurrentEditedVehicleData = currentVehicle;
                    addNewUserVehicleChipView();
                }

                mCurrentEditedVehicleData = null;
            }
        }
    }

    @Override
    public boolean isNextStepEnabled() {
        return true;
    }

    /*
     * View.OnClickListener
     */

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.add_new_vehicle_button:
                if (mCurrentEditedVehicleData == null) {
                    mCurrentEditedVehicleData = new VehicleData();
                }

                toggleAddVehicleLayout(true);
                break;
            case R.id.collapse_add_vehicle_layout_button:
                toggleAddVehicleLayout(false);
                // Release the resources.
                mVehicleAPIData = null;
                mCurrentEditedVehicleData = null;
                break;
            case R.id.show_vehicle_makes_button:
                ArrayList<String> vehicleMakes = new ArrayList<>();
                if (mCurrentEditedVehicleData != null) {
                    vehicleMakes.add(mCurrentEditedVehicleData.getVehicleMake());
                }
                showVehicleMakesDialog(vehicleMakes);
                break;
            case R.id.finish_add_vehicle_button:
                if (checkSufficientFieldsForVehicle()) {
                    toggleAddVehicleLayout(false);

                    addNewUserVehicle();

                    addNewUserVehicleChipView();

                    clearAddVehicleFields();

                    mCurrentEditedVehicleData = null;
                }
                break;
        }
    }

    /*
     * VehicleMakesFragment.SelectVehicleMakesDialogListener
     */

    @Override
    public void onVehicleMakesSelected(ArrayList<String> vehicleMakes) {
        if (vehicleMakes != null && !vehicleMakes.isEmpty()) {
            String vehicleMakeString = vehicleMakes.get(0);
            mShowVehicleMakesButton.setText(vehicleMakeString);

            mCurrentEditedVehicleData.setVehicleMake(vehicleMakeString);

            if (mVehicleAPIData != null) {
                ArrayList<String> vehicleModels = new ArrayList<>();
                for (VehicleExtendedData vehicleExtendedData : mVehicleAPIData) {
                    if (vehicleExtendedData.getVehicleMake().equals(vehicleMakeString)) {
                        vehicleModels = vehicleExtendedData.getExtraModels();
                        break;
                    }
                }

                if (vehicleModels != null) {
                    ArrayAdapter adapter = new ArrayAdapter<>(getContext(),
                            R.layout.support_simple_spinner_dropdown_item, vehicleModels);
                    mModelSpinner.setAdapter(adapter);
                    mModelSpinner.setEnabled(true);
                }
            }
        }
    }

    /*
     * AdapterView.OnItemSelectedListener
     */

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        ArrayAdapter<String> arrayAdapter = (ArrayAdapter<String>) parent.getAdapter();
        if (arrayAdapter != null) {
            String model = arrayAdapter.getItem(position);
            mCurrentEditedVehicleData.setVehicleModel(model);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    /*
     * ChipView.OnChipDeleteClickListener
     */

    @Override
    public void onChipDelete(ChipView chipView) {
        Object tag = chipView.getTag(R.id.tag_chip_car_make);
        if (tag != null) {
            VehicleData vehicleData = (VehicleData) tag;
            if (mUser != null) {
                List<VehicleData> userVehicles = mUser.getVehicles();
                userVehicles.remove(vehicleData);

                mVehiclesArrangedLayout.removeView(chipView);
            }
        }
    }

    public void setUser(ClientUser user) {
        mUser = user;
    }

    private void clearAddVehicleFields() {
        mShowVehicleMakesButton.setText(R.string.tap_to_view_vehicle_makes_button);

        mModelSpinner.setEnabled(false);
        mModelSpinner.setAdapter(null);

        mYearInputEditText.setText(null);
        mYearTextInputLayout.setError(null);

        mEngineInputEditText.setText(null);
        mEngineDisplacementTextInputLayout.setError(null);
    }

    private void addNewUserVehicle() {
        // Add a copy of the current edited vehicle data.
        mUser.getVehicles().add(new VehicleData(mCurrentEditedVehicleData));
    }

    private void addNewUserVehicleChipView() {
        ChipView carMakeChip = new ChipView(getContext());

        int itemSpacing = getResources().getDimensionPixelSize(R.dimen.item_spacing);
        ViewCompat.setPaddingRelative(carMakeChip, itemSpacing, 0, 0, itemSpacing);

        carMakeChip.setDeletable(true);
        carMakeChip.setText(mCurrentEditedVehicleData.toString());
        carMakeChip.addOnChipDeleteClickListener(this);
        carMakeChip.setTag(R.id.tag_chip_car_make, mCurrentEditedVehicleData);

        mVehiclesArrangedLayout.addView(carMakeChip);
    }

    private boolean checkSufficientFieldsForVehicle() {
        if (mCurrentEditedVehicleData == null) {
            mCurrentEditedVehicleData = new VehicleData();
            return false;
        }

        String vehicleMake = mCurrentEditedVehicleData.getVehicleMake();
        if (vehicleMake == null || vehicleMake.isEmpty()) {
            Snackbar.make(mAddVehicleLayout, R.string.vehicle_make_not_set_error_message,
                    Snackbar.LENGTH_LONG).setAction(R.string.tap_to_set,
                    new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showVehicleMakesDialog();
                }
            })
                    .show();
            return false;
        }

        String vehicleModel = mCurrentEditedVehicleData.getVehicleModel();
        if (vehicleModel == null || vehicleModel.isEmpty()) {
            Toast.makeText(getContext(), R.string.vehicle_model_not_set_error_message,
                    Toast.LENGTH_SHORT)
                    .show();
            return false;
        }

        int year = mCurrentEditedVehicleData.getVehicleYear();
        // TODO: update to some threshold year
        if (!isValidYear(year)) {
            mYearTextInputLayout.setError(getString(R.string.invalid_year_error_message));
            return false;
        }

        String modifications = mCurrentEditedVehicleData.getVehicleModifications();
        if (modifications == null || modifications.isEmpty() ||
                !isValidEngineDisplacement(Float.valueOf(modifications))) {
            mEngineDisplacementTextInputLayout.setError(
                    getString(R.string.invalid_engine_displacement_error_message));
            return false;
        }

        return true;
    }


    private void setupVehicleDetailsLayout() {
        mAddVehicleLayout = mLayout.findViewById(R.id.add_vehicle_layout);

        mCollapseAddVehicleButton = (ImageButton) mLayout.findViewById(
                R.id.collapse_add_vehicle_layout_button);
        mCollapseAddVehicleButton.setOnClickListener(this);

        mAddVehiclesButton = (Button) mLayout.findViewById(R.id.add_new_vehicle_button);
        mAddVehiclesButton.setOnClickListener(this);

        mShowVehicleMakesButton = (Button) mLayout.findViewById(R.id.show_vehicle_makes_button);
        mShowVehicleMakesButton.setOnClickListener(this);

        mModelSpinner = (AppCompatSpinner) mLayout.findViewById(R.id.vehicle_model_spinner);
        mModelSpinner.setEnabled(false);
        mModelSpinner.setOnItemSelectedListener(this);

        mYearTextInputLayout = (TextInputLayout) mLayout.findViewById(
                R.id.vehicle_year_text_input_layout);
        mYearInputEditText = (TextInputEditText) mLayout.findViewById(
                R.id.year_input_edit_text);
        mYearInputEditText.addTextChangedListener(new VehicleYearTextWatcher());

        mEngineDisplacementTextInputLayout = (TextInputLayout) mLayout.findViewById(
                R.id.vehicle_engine_displacement_text_input_layout);
        mEngineInputEditText = (TextInputEditText) mLayout.findViewById(
                R.id.engine_input_edit_text);
        mEngineInputEditText.addTextChangedListener(new VehicleEngineDisplacementTextWatcher());

        mLayout.findViewById(R.id.finish_add_vehicle_button).setOnClickListener(this);
    }

    private void toggleAddVehicleLayout(boolean toggle) {
        int EXPAND_COLLAPSE_DURATION = 250;
        if (toggle) {
            Utils.collapseView(mAddVehiclesButton, EXPAND_COLLAPSE_DURATION);
            Utils.expandView(mAddVehicleLayout, EXPAND_COLLAPSE_DURATION);
        } else {
            Utils.collapseView(mAddVehicleLayout, EXPAND_COLLAPSE_DURATION);
            Utils.expandView(mAddVehiclesButton, EXPAND_COLLAPSE_DURATION);
        }
    }

    private void showVehicleMakesDialog() {
        showVehicleMakesDialog(null);
    }

    private void showVehicleMakesDialog(ArrayList<String> vehicleMakes) {
        VehicleMakesFragment vehicleMakesFragment = VehicleMakesFragment
                .getInstance(vehicleMakes, true);
        vehicleMakesFragment.setOnVehicleMakesSelectedListener(this);
        Utils.showDialogFragment(getFragmentManager(),
                vehicleMakesFragment, "vehicle_make_fragment");

        mVehicleAPIData = vehicleMakesFragment.getVehicleData();
    }

    public static RegistrationClientDetailsFragment getInstance(User mUser) {
        RegistrationClientDetailsFragment registrationClientDetailsFragment
                = new RegistrationClientDetailsFragment();

        Bundle args = new Bundle();
        args.putSerializable("user", mUser);
        registrationClientDetailsFragment.setArguments(args);

        return registrationClientDetailsFragment;
    }

    private class VehicleYearTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.length() > 0) {
                int year = Integer.valueOf(s.toString());
                if (!isValidYear(year)) {
                    mYearTextInputLayout.setError(getString(R.string.invalid_year_error_message));
                } else {
                    mYearTextInputLayout.setError(null);
                }

                if (mCurrentEditedVehicleData != null) {
                    mCurrentEditedVehicleData.setVehicleYear(Integer.valueOf(s.toString()));
                }
            }
        }
    }

    private boolean isValidYear(int year) {
        return year > 0;
    }

    private class VehicleEngineDisplacementTextWatcher implements TextWatcher {

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.length() > 0) {
                float value = Float.valueOf(s.toString());
                if (!isValidEngineDisplacement(value)) {
                    mEngineDisplacementTextInputLayout.setError(
                            getString(R.string.invalid_year_error_message));
                } else {
                    mEngineDisplacementTextInputLayout.setError(null);
                }

                if (mCurrentEditedVehicleData != null) {
                    mCurrentEditedVehicleData.setVehicleModifications(s.toString());
                }
            }
        }
    }

    private boolean isValidEngineDisplacement(float value) {
        return value > 0.0f;
    }

    private class NameTextWatcher implements TextWatcher {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

        }

        @Override
        public void afterTextChanged(Editable s) {
            if (s.length() > 0) {
                if (mUser != null) {
                    mUser.setName(s.toString());
                }
            }
        }
    }
}
