package il.co.tel_ran.carservice.fragments;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.AppCompatCheckBox;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;

import il.co.tel_ran.carservice.R;
import il.co.tel_ran.carservice.TimeHolder;
import il.co.tel_ran.carservice.Utils;
import il.co.tel_ran.carservice.dialogs.TimePickerDialogFragment;

import static android.app.Activity.RESULT_CANCELED;
import static android.app.Activity.RESULT_OK;

/**
 * Created by maxim on 10/24/2016.
 */

public class RegistrationServiceDetailsFragment extends RegistrationUserDetailsFragment
        implements View.OnClickListener, TimePickerDialog.OnTimeSetListener {

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

    private EditText mServiceNameEditText;

    private EditText mPhonenumberEditText;

    private final static int[] SERVICE_CHECKBOX_IDS = {
            R.id.service_checkbox_car_wash,
            R.id.service_checkbox_tuning,
            R.id.service_checkbox_tyre_repair,
            R.id.service_checkbox_air_cond
    };
    private AppCompatCheckBox[] mServicesCheckBoxes = new AppCompatCheckBox[SERVICE_CHECKBOX_IDS.length];

    private final static int[] VEHICLE_TYPE_CHECKBOX_IDS = {
            R.id.service_vehicle_type_private,
            R.id.service_vehicle_type_truck,
            R.id.service_vehicle_type_bus,
            R.id.service_vehicle_type_motorcycles
    };
    private AppCompatCheckBox[] mVehicleTypeCheckBoxes = new AppCompatCheckBox[VEHICLE_TYPE_CHECKBOX_IDS.length];

    private EditText mDirectorNameEditText;
    private EditText mDirectorPhonenumberEditText;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_registration_step_servicedetails, null);

        mServiceNameEditText = (EditText) layout.findViewById(R.id.service_name_edit_text);

        mAddressSearchButton = (Button) layout.findViewById(R.id.search_address_button);
        mAddressSearchButton.setOnClickListener(this);

        mPhonenumberEditText = (EditText) layout.findViewById(R.id.service_phonenumber_edit_text);

        for (int i = 0; i < mServicesCheckBoxes.length; i++) {
            mServicesCheckBoxes[i] = (AppCompatCheckBox) layout
                    .findViewById(SERVICE_CHECKBOX_IDS[i]);
        }

        for (int i = 0; i< mVehicleTypeCheckBoxes.length; i++) {
            mVehicleTypeCheckBoxes[i] = (AppCompatCheckBox) layout
                    .findViewById(VEHICLE_TYPE_CHECKBOX_IDS[i]);
        }

        mDirectorNameEditText = (EditText) layout.findViewById(R.id.director_name_edit_text);

        mDirectorPhonenumberEditText = (EditText) layout.findViewById(R.id.director_phonenumber_edit_text);

        mOpeningTimeButton = (Button) layout.findViewById(R.id.set_start_hour_button);
        mOpeningTimeButton.setOnClickListener(this);

        mClosingTimeButton = (Button) layout.findViewById(R.id.set_end_hour_button);
        mClosingTimeButton.setOnClickListener(this);

        mBrowsePhotoButton = (Button) layout.findViewById(R.id.browse_photo_button);
        mBrowsePhotoButton.setOnClickListener(this);
        mRemovePhotoButton = (Button) layout.findViewById(R.id.remove_photo_button);
        mRemovePhotoButton.setOnClickListener(this);

        mServicePhotoImageView = (ImageView) layout.findViewById(R.id.service_photo);
        return layout;
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
        for (int i = 0; i< mVehicleTypeCheckBoxes.length; i++) {
            mVehicleTypeCheckBoxes[i].setEnabled(toggle);
        }
        mDirectorNameEditText.setEnabled(toggle);
        mDirectorPhonenumberEditText.setEnabled(toggle);
        mBrowsePhotoButton.setEnabled(toggle);
        mRemovePhotoButton.setEnabled(toggle);
    }
}
