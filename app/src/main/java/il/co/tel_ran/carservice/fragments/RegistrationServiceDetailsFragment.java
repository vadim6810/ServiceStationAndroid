package il.co.tel_ran.carservice.fragments;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.AppCompatCheckBox;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;

import java.util.ArrayList;
import java.util.EnumSet;

import il.co.tel_ran.carservice.R;
import il.co.tel_ran.carservice.ServiceStation;
import il.co.tel_ran.carservice.ServiceSubWorkType;
import il.co.tel_ran.carservice.ServiceType;
import il.co.tel_ran.carservice.ServiceWorkType;
import il.co.tel_ran.carservice.TimeHolder;
import il.co.tel_ran.carservice.Utils;
import il.co.tel_ran.carservice.VehicleType;
import il.co.tel_ran.carservice.dialogs.TimePickerDialogFragment;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

/**
 * Created by maxim on 10/24/2016.
 */

public class RegistrationServiceDetailsFragment extends RegistrationUserDetailsFragment
        implements View.OnClickListener, TimePickerDialog.OnTimeSetListener,
        WorkTypesFragment.SelectWorkTypesDialogListener,
        VehicleMakesFragment.SelectVehicleMakesDialogListener {

    private static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 1;
    private static final int SELECT_PHOTO_FROM_GALLERY_REQUEST_CODE = 2;

    private Button mAddressSearchButton;

    private TimeHolder mOpeningTime;
    private Button mOpeningTimeButton;
    private TimeHolder mClosingTime;
    private Button mClosingTimeButton;

    private Button mBrowsePhotoButton;
    private ImageView mServicePhotoImageView;
    private Button mRemovePhotoButton;

    private TextInputLayout mServiceNameInputLayout;
    private EditText mServiceNameEditText;

    private TextInputLayout mPhonenumberInputLayout;
    private EditText mPhonenumberEditText;

    private ServiceStation mService = new ServiceStation();

    private final static int[] SERVICE_CHECKBOX_IDS = {
            R.id.service_checkbox_car_wash,
            R.id.service_checkbox_towing,
            R.id.service_checkbox_tyre_repair,
            R.id.service_checkbox_auto_service
    };
    private AppCompatCheckBox[] mServicesCheckBoxes = new AppCompatCheckBox[SERVICE_CHECKBOX_IDS.length];
    private Button mShowWorkTypesButton;

    private final static int[] VEHICLE_TYPE_CHECKBOX_IDS = {
            R.id.service_vehicle_type_private,
            R.id.service_vehicle_type_truck,
            R.id.service_vehicle_type_bus,
            R.id.service_vehicle_type_motorcycles
    };
    private AppCompatCheckBox[] mVehicleTypeCheckBoxes = new AppCompatCheckBox[VEHICLE_TYPE_CHECKBOX_IDS.length];
    private Button mShowVehicleMakesButton;

    private TextInputLayout mDirectorNameInputLayout;
    private EditText mDirectorNameEditText;

    private TextInputLayout mManagerNameInputLayout;
    private EditText mManagerNameEditText;

    private TextInputLayout mManagerPhonenumberInputLayout;
    private EditText mManagerPhonenumberEditText;

    private TextView mTitle;
    private TextView mCaption;

    private View mServiceDetailsLayout;
    private View mServiceLoadingLayout;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_registration_step_servicedetails, null);

        mTitle = (TextView) layout.findViewById(R.id.service_details_title);
        mCaption = (TextView) layout.findViewById(R.id.service_details_caption);

        mServiceDetailsLayout = layout.findViewById(R.id.service_details_layout);

        mServiceNameInputLayout = (TextInputLayout) layout.findViewById(
                R.id.service_name_input_layout);
        mServiceNameEditText = (EditText) layout.findViewById(R.id.service_name_edit_text);

        mAddressSearchButton = (Button) layout.findViewById(R.id.search_address_button);
        mAddressSearchButton.setOnClickListener(this);

        mPhonenumberInputLayout = (TextInputLayout) layout.findViewById(
                R.id.service_phonenumber_input_layout);
        mPhonenumberEditText = (EditText) layout.findViewById(R.id.service_phonenumber_edit_text);

        for (int i = 0; i < mServicesCheckBoxes.length; i++) {
            mServicesCheckBoxes[i] = (AppCompatCheckBox) layout
                    .findViewById(SERVICE_CHECKBOX_IDS[i]);
        }

        mShowWorkTypesButton = (Button) layout.findViewById(R.id.show_work_types_button);
        mShowWorkTypesButton.setOnClickListener(this);

        for (int i = 0; i< mVehicleTypeCheckBoxes.length; i++) {
            mVehicleTypeCheckBoxes[i] = (AppCompatCheckBox) layout
                    .findViewById(VEHICLE_TYPE_CHECKBOX_IDS[i]);
        }

        mShowVehicleMakesButton = (Button ) layout.findViewById(R.id.show_vehicle_makes_button);
        mShowVehicleMakesButton.setOnClickListener(this);

        mDirectorNameInputLayout = (TextInputLayout) layout.findViewById(
                R.id.director_name_input_layout);
        mDirectorNameEditText = (EditText) layout.findViewById(R.id.director_name_edit_text);

        mManagerNameInputLayout = (TextInputLayout) layout.findViewById(
                R.id.manager_name_input_layout);
        mManagerNameEditText = (EditText) layout.findViewById(R.id.manager_name_edit_text);

        mManagerPhonenumberInputLayout = (TextInputLayout) layout.findViewById(
                R.id.manager_phonenumber_input_layout);
        mManagerPhonenumberEditText = (EditText) layout.findViewById(
                R.id.manager_phonenumber_edit_text);

        mOpeningTimeButton = (Button) layout.findViewById(R.id.set_start_hour_button);
        mOpeningTimeButton.setOnClickListener(this);

        mClosingTimeButton = (Button) layout.findViewById(R.id.set_end_hour_button);
        mClosingTimeButton.setOnClickListener(this);

        mBrowsePhotoButton = (Button) layout.findViewById(R.id.browse_photo_button);
        mBrowsePhotoButton.setOnClickListener(this);
        mRemovePhotoButton = (Button) layout.findViewById(R.id.remove_photo_button);
        mRemovePhotoButton.setOnClickListener(this);

        mServicePhotoImageView = (ImageView) layout.findViewById(R.id.service_photo);

        mServiceLoadingLayout = layout.findViewById(R.id.loading_service_layout);

        return layout;
    }

    @Override
    public boolean isNextStepEnabled() {
        return checkFieldsReady();
    }

    private boolean checkFieldsReady() {
        boolean isValidName = isValidServiceName();

        if (!isValidName) {
            mServiceNameInputLayout.setError(getString(R.string.invalid_name_error_message));
            return false;
        } else {
            mServiceNameInputLayout.setError(null);
        }

        boolean isValidAddress = isValidAddress();

        if (!isValidAddress) {
            Toast.makeText(getContext(), R.string.address_field_empty_error_message,
                    Toast.LENGTH_LONG).show();
            return false;
        }

        boolean isValidPhonenumber = isValidPhonenumber();

        if (!isValidPhonenumber) {
            mPhonenumberInputLayout.setError(
                    getString(R.string.invalid_phone_number_error_message));
            return false;
        } else {
            mPhonenumberInputLayout.setError(null);

        }

        boolean isValidActiveTime = isValidActiveTime();

        if (!isValidActiveTime) {
            Toast.makeText(getContext(), R.string.invalid_active_time_error_message,
                    Toast.LENGTH_LONG).show();
            return false;
        }

        boolean isValidServices = isValidServiceTypes();

        if (!isValidServices) {
            Toast.makeText(getContext(), R.string.empty_service_types_error_message,
                    Toast.LENGTH_LONG).show();
            return false;
        }

        boolean isValidWorkTypes = isValidWorkTypes();

        if (!isValidWorkTypes) {
            Toast.makeText(getContext(), R.string.empty_work_types_error_message,
                    Toast.LENGTH_LONG).show();
            return false;
        }

        boolean isValidVehicleTypes = isValidVehicleTypes();

        if (!isValidVehicleTypes) {
            Toast.makeText(getContext(), R.string.empty_vehicle_types_error_message,
                    Toast.LENGTH_LONG).show();
            return false;
        }

        boolean isValidVehicleMakes = isValidVehicleMakes();

        if (!isValidVehicleMakes) {
            Toast.makeText(getContext(), R.string.empty_vehicle_makes_error_message,
                    Toast.LENGTH_LONG).show();
            return false;
        }


        boolean isValidDirectorName = isValidDirectorName();

        if (!isValidDirectorName) {
            mDirectorNameInputLayout.setError(
                    getString(R.string.invalid_name_error_message));
            return false;
        } else {
            mDirectorNameInputLayout.setError(null);
        }

        boolean isValidManagerName = isValidManagerName();

        if (!isValidManagerName) {
            mManagerNameInputLayout.setError(
                    getString(R.string.invalid_name_error_message));
            return false;
        } else {
            mManagerNameInputLayout.setError(null);
        }

        boolean isValidManagerPhonenumber = isValidManagerPhonenumber();

        if (!isValidManagerPhonenumber) {
            mManagerPhonenumberInputLayout.setError(
                    getString(R.string.invalid_phone_number_error_message));
            return false;
        } else {
            mManagerPhonenumberInputLayout.setError(null);
        }

        return true;
    }

    /*
     * View.OnClickListener
     */

    @Override
    public void onClick(View v) {
        Activity containerActivity = getActivity();
        switch (v.getId()) {
            case R.id.search_address_button:
                try {
                    // Build a place search intent with address filter.
                    Intent addressIntent = Utils.buildPlaceAutoCompleteIntent(containerActivity,
                            Utils.PLACE_FILTER_ADDRESS);
                    // Start the overlay activity with a unique request code to ideintify later.
                    startActivityForResult(addressIntent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
                } catch (GooglePlayServicesNotAvailableException
                        | GooglePlayServicesRepairableException e) {
                    e.printStackTrace();
                }
                break;
            case R.id.set_start_hour_button:
                showTimePickerDialog(true);
                break;
            case R.id.set_end_hour_button:
                if (mOpeningTime == null) {
                    Toast.makeText(getContext(),
                            R.string.open_time_not_set_message, Toast.LENGTH_SHORT).show();
                } else {
                    showTimePickerDialog(false);
                }
                break;
            case R.id.browse_photo_button:
                Intent browseGalleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                browseGalleryIntent.setType("image/*");
                startActivityForResult(browseGalleryIntent, SELECT_PHOTO_FROM_GALLERY_REQUEST_CODE);
                break;
            case R.id.remove_photo_button:
                // Release the image resource to avoid unnecessary memory usage.
                mServicePhotoImageView.setImageBitmap(null);
                // Hide the image and button.
                mServicePhotoImageView.setVisibility(View.GONE);
                mRemovePhotoButton.setVisibility(View.GONE);
                break;
            case R.id.show_work_types_button:
                showWorkTypesSelectionDialog();
                break;
            case R.id.show_vehicle_makes_button:
                showVehicleMakesSelectionDialog();
                break;
        }
    }

    /*
     * WorkTypesFragment.SelectWorkTypesDialogListener
     */

    @Override
    public void onWorkTypeSelected(ServiceWorkType[] workTypes, ServiceSubWorkType[] subWorkTypes) {
        ArrayList<ServiceWorkType> selectedWorkTypes = mService.getWorkTypes();
        ArrayList<ServiceSubWorkType> selectedSubWorkTypes = mService.getSubWorkTypes();

        selectedWorkTypes.clear();
        for (ServiceWorkType workType : workTypes) {
            selectedWorkTypes.add(workType);
        }

        selectedSubWorkTypes.clear();
        for (ServiceSubWorkType subWorkType : subWorkTypes) {
            selectedSubWorkTypes.add(subWorkType);
        }
    }

    /*
     * VehicleMakesFragment.SelectVehicleMakesDialogListener
     */

    @Override
    public void onVehicleMakesSelected(ArrayList<String> vehicleMakes) {
        if (vehicleMakes != null && !vehicleMakes.isEmpty()) {
            String[] vehicleMakesArr = new String[vehicleMakes.size()];
            vehicleMakes.toArray(vehicleMakesArr);

            mService.setServicedCarMakes(vehicleMakesArr);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            // User has selected a Place.
            case PLACE_AUTOCOMPLETE_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    Activity containerActivity = getActivity();
                    // Get the place
                    Place place = PlaceAutocomplete.getPlace(containerActivity, data);
                    // Change the text to the address
                    mAddressSearchButton.setText(place.getAddress());
                    // Change the text color to accent color to be more intuitive.
                    // Clicking on the button again will up the place search again.
                    mAddressSearchButton.setTextColor(Utils.getThemeAccentColor(containerActivity));

                    mService.setLocation(place);
                    mService.setCityName(Utils.parseCityNameFromAddress(place.getAddress()));
                } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                    Status status = PlaceAutocomplete.getStatus(getActivity(), data);
                    // TODO: Handle error
                } else if (resultCode == RESULT_CANCELED) {

                }
                break;
            case SELECT_PHOTO_FROM_GALLERY_REQUEST_CODE:
                if (resultCode == RESULT_OK) {
                    final Bundle extras = data.getExtras();
                    if (extras != null) {
                        // Set the selected image.
                        mServicePhotoImageView.setImageURI(data.getData());
                        // Show the image
                        mServicePhotoImageView.setVisibility(View.VISIBLE);
                        // Show remove button to clear the image.
                        mRemovePhotoButton.setVisibility(View.VISIBLE);
                    }
                } else if (resultCode == PlaceAutocomplete.RESULT_ERROR) {
                    // TODO: Handle error
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /*
     * TimePickerDialogFragment.onTimeSet
     */

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
        if (fragmentManager.findFragmentByTag(
                Integer.toString(R.id.set_start_hour_button)) != null) {
            // Set start time as the fragment has a tag with start hour id.
            if (mOpeningTime == null) {
                mOpeningTime = new TimeHolder(hourOfDay, minute);
            } else {
                mOpeningTime.setHour(hourOfDay);
                mOpeningTime.setMinute(minute);
            }

            mService.setOpeningTime(mOpeningTime);

            mOpeningTimeButton.setText(mOpeningTime.toString());
        } else {
            // Set closing time since it doesn't have start hour id.

            if (mOpeningTime != null) {

                TimeHolder tmpTime = new TimeHolder(hourOfDay, minute);
                if (tmpTime.compare(mOpeningTime) != 1) {
                    // Close time is not later than opening time (equal / earlier)
                    Toast.makeText(getContext(),
                            R.string.open_time_earlier_equal_message, Toast.LENGTH_SHORT).show();
                } else {
                    mClosingTime = tmpTime;
                    mClosingTimeButton.setText(mClosingTime.toString());

                    mService.setClosingTime(mClosingTime);
                }
            }
        }
    }

    public void showTimePickerDialog(boolean isStartTime) {
        TimePickerDialogFragment timePickerFragment = new TimePickerDialogFragment();

        int identifier;
        boolean setArguments = false;

        Bundle bundle = new Bundle();
        if (isStartTime) {
            identifier = R.id.set_start_hour_button;

            if (mOpeningTime != null) {
                setArguments = true;
                bundle.putInt("hour", mOpeningTime.getHour());
                bundle.putInt("minute", mOpeningTime.getMinute());
            }
        } else {
            identifier = R.id.set_end_hour_button;

            if (mClosingTime != null) {
                setArguments = true;
                bundle.putInt("hour", mClosingTime.getHour());
                bundle.putInt("minute", mClosingTime.getMinute());
            }
        }

        if (setArguments)
            timePickerFragment.setArguments(bundle);

        timePickerFragment.setOnTimeListener(this);
        timePickerFragment.show(getActivity().getSupportFragmentManager(),
                Integer.toString(identifier));
    }

    // Used for ProfileActivity - limit user's option to make any changes.
    public void toggleFields(boolean toggle) {
        mServiceNameEditText.setEnabled(toggle);
        mAddressSearchButton.setEnabled(toggle);
        mPhonenumberEditText.setEnabled(toggle);
        mOpeningTimeButton.setEnabled(toggle);
        mClosingTimeButton.setEnabled(toggle);
        for (int i = 0; i < mServicesCheckBoxes.length; i++) {
            mServicesCheckBoxes[i].setEnabled(toggle);
        }
        mShowWorkTypesButton.setEnabled(toggle);
        for (int i = 0; i< mVehicleTypeCheckBoxes.length; i++) {
            mVehicleTypeCheckBoxes[i].setEnabled(toggle);
        }
        mShowVehicleMakesButton.setEnabled(toggle);
        mDirectorNameEditText.setEnabled(toggle);
        mManagerNameEditText.setEnabled(toggle);
        mManagerPhonenumberEditText.setEnabled(toggle);
        mBrowsePhotoButton.setEnabled(toggle);
        mRemovePhotoButton.setEnabled(toggle);
    }

    public void hideTitle(boolean hide) {
        mTitle.setVisibility(hide ? View.GONE : View.VISIBLE);
    }

    public void hideCaption(boolean hide) {
        mCaption.setVisibility(hide ? View.GONE : View.VISIBLE);
    }

    public void toggleLoadingService(boolean toggle) {
        if (toggle) {
            mServiceDetailsLayout.setVisibility(View.GONE);
            mServiceLoadingLayout.setVisibility(View.VISIBLE);
        } else {
            mServiceDetailsLayout.setVisibility(View.VISIBLE);
            mServiceLoadingLayout.setVisibility(View.GONE);

        }
    }

    public void setService(ServiceStation service) {
        mService = service;
    }

    public void setFieldsFromService() {
        setFieldsFromService(mService);
    }

    public void setFieldsFromService(ServiceStation service) {
        if (service != null) {
            String name = service.getName();
            if (name != null)
                mServiceNameEditText.setText(service.getName());
            else
                mServiceNameEditText.setText("");

            String cityName = service.getCityName();
            if (cityName != null)
                mAddressSearchButton.setText(service.getCityName());
            else
                mAddressSearchButton.setText(getString(R.string.search_location_button_hint));

            String phonenumber = service.getPhonenumber();
            if (phonenumber != null)
                mPhonenumberEditText.setText(service.getPhonenumber());
            else
                mPhonenumberEditText.setText("");
        /*
            Missing:
            mServicePhotoImageView
        */

            EnumSet<ServiceType> serviceTypes = service.getAvailableServices();
            if (serviceTypes != null) {
                for (ServiceType serviceType : ServiceType.values()) {
                    boolean contains = serviceTypes.contains(serviceType);
                    switch (serviceType) {
                        case CAR_WASH:
                            mServicesCheckBoxes[0].setChecked(contains);
                            break;
                        case TOWING:
                            mServicesCheckBoxes[1].setChecked(contains);
                            break;
                        case TYRE_REPAIR:
                            mServicesCheckBoxes[2].setChecked(contains);
                            break;
                        case AUTO_SERVICE:
                            mServicesCheckBoxes[3].setChecked(contains);
                            break;
                    }
                }
            }

            TimeHolder openingTime = service.getOpeningTime();
            if (openingTime != null)
                mOpeningTimeButton.setText(openingTime.toString());
            else
                mOpeningTimeButton.setText(getString(R.string.set_opening_time));

            TimeHolder closingTime = service.getClosingTime();
            if (closingTime != null)
                mClosingTimeButton.setText(closingTime.toString());
            else
                mClosingTimeButton.setText(getString(R.string.set_closing_time));

            EnumSet<VehicleType> vehicleTypes = service.getVehicleTypes();
            if (vehicleTypes != null) {
                for (VehicleType vehicleType : VehicleType.values()) {
                    boolean contains = vehicleTypes.contains(vehicleType);
                    switch (vehicleType) {
                        case PRIVATE:
                            mVehicleTypeCheckBoxes[0].setChecked(contains);
                            break;
                        case TRUCK:
                            mVehicleTypeCheckBoxes[1].setChecked(contains);
                            break;
                        case BUS:
                            mVehicleTypeCheckBoxes[2].setChecked(contains);
                            break;
                        case MOTORCYCLE:
                            mVehicleTypeCheckBoxes[3].setChecked(contains);
                            break;
                    }
                }
            }

            String directorName = service.getDirectorName();
            if (directorName != null)
                mDirectorNameEditText.setText(directorName);
            else
                mDirectorNameEditText.setText("");

            String managerPhonenumber = service.getManagerPhonenumber();
            if (managerPhonenumber != null)
                mManagerPhonenumberEditText.setText(managerPhonenumber);
            else
                mManagerPhonenumberEditText.setText("");

            String managerName = service.getManagerName();
            if (managerName != null)
                mManagerNameEditText.setText(managerName);
            else
                mManagerNameEditText.setText("");

            // TODO: add code for image loading
        }
    }

    public void updateServiceFromFields() {
        mService.setName(mServiceNameEditText.getText().toString());
        mService.setPhonenumber(mPhonenumberEditText.getText().toString());
        mService.setOpeningTime(mOpeningTime);
        mService.setClosingTime(mClosingTime);

        mService.toggleService(ServiceType.CAR_WASH, mServicesCheckBoxes[0].isChecked());
        mService.toggleService(ServiceType.TOWING, mServicesCheckBoxes[1].isChecked());
        mService.toggleService(ServiceType.TYRE_REPAIR, mServicesCheckBoxes[2].isChecked());
        mService.toggleService(ServiceType.AUTO_SERVICE, mServicesCheckBoxes[3].isChecked());

        mService.toggleVehicleType(VehicleType.PRIVATE, mVehicleTypeCheckBoxes[0].isChecked());
        mService.toggleVehicleType(VehicleType.TRUCK, mVehicleTypeCheckBoxes[1].isChecked());
        mService.toggleVehicleType(VehicleType.BUS, mVehicleTypeCheckBoxes[2].isChecked());
        mService.toggleVehicleType(VehicleType.MOTORCYCLE, mVehicleTypeCheckBoxes[3].isChecked());

        mService.setDirectorName(mDirectorNameEditText.getText().toString());

        mService.setManagerName(mManagerNameEditText.getText().toString());
        mService.setManagerPhonenumber(mManagerPhonenumberEditText.getText().toString());
    }

    public ServiceStation getService() {
        return new ServiceStation(mService);
    }

    private void showWorkTypesSelectionDialog() {
        WorkTypesFragment workTypesFragment = WorkTypesFragment.getInstance(false,
                mService.getSubWorkTypes());
        workTypesFragment.setOnWorkTypesSelectedListener(this);
        Utils.showDialogFragment(getFragmentManager(), workTypesFragment,
                "work_type_fragment");
    }

    private void showVehicleMakesSelectionDialog() {
        ArrayList<String> servicesCarMakes = new ArrayList<>();

        String[] servicedCarMakesArr = mService.getServicedCarMakes();
        if (servicedCarMakesArr != null && servicedCarMakesArr.length > 0) {
            for (String carMake : mService.getServicedCarMakes()) {
                servicesCarMakes.add(carMake);
            }
        }

        VehicleMakesFragment vehicleMakesFragment = VehicleMakesFragment.getInstance(
                servicesCarMakes, false);
        vehicleMakesFragment.setOnVehicleMakesSelectedListener(this);
        Utils.showDialogFragment(getFragmentManager(),
                vehicleMakesFragment, "vehicle_make_fragment");
    }

    private boolean isValidServiceName() {
        return !mServiceNameEditText.getText().toString().isEmpty();
    }

    private boolean isValidAddress() {
        return mService.getLocation() != null;
    }

    private boolean isValidPhonenumber() {
        return !mPhonenumberEditText.getText().toString().isEmpty();
    }

    private boolean isValidActiveTime() {
        return (mService.getOpeningTime() != null && mService.getClosingTime() != null);
    }

    private boolean isValidServiceTypes() {
        for (int i = 0; i < SERVICE_CHECKBOX_IDS.length; i++) {
            if (mServicesCheckBoxes[i].isChecked()) {
                return true;
            }
        }

        return false;
    }

    private boolean isValidWorkTypes() {
        return (mService.getSubWorkTypes() != null && !mService.getSubWorkTypes().isEmpty());
    }

    private boolean isValidVehicleTypes() {
        for (int i = 0; i < VEHICLE_TYPE_CHECKBOX_IDS.length; i++) {
            if (mVehicleTypeCheckBoxes[i].isChecked()) {
                return true;
            }
        }

        return false;
    }

    private boolean isValidVehicleMakes() {
        return (mService.getServicedCarMakes() != null
                && mService.getServicedCarMakes().length > 0);
    }

    private boolean isValidDirectorName() {
        return !mDirectorNameEditText.getText().toString().isEmpty();
    }

    private boolean isValidManagerName() {
        return !mManagerNameEditText.getText().toString().isEmpty();
    }

    private boolean isValidManagerPhonenumber() {
        return !mManagerPhonenumberEditText.getText().toString().isEmpty();
    }
}
