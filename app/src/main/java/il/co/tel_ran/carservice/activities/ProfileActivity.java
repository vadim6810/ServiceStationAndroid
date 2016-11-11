package il.co.tel_ran.carservice.activities;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import il.co.tel_ran.carservice.ClientUser;
import il.co.tel_ran.carservice.ProviderUser;
import il.co.tel_ran.carservice.R;
import il.co.tel_ran.carservice.User;
import il.co.tel_ran.carservice.UserType;
import il.co.tel_ran.carservice.Utils;
import il.co.tel_ran.carservice.VehicleData;
import il.co.tel_ran.carservice.dialogs.ChangePasswordDialog;
import il.co.tel_ran.carservice.fragments.RegistrationVehicleDetailsFragment;

public class ProfileActivity extends AppCompatActivity
    implements View.OnClickListener, View.OnTouchListener {

    private UserType mUserType = UserType.USER_CLIENT;

    private Menu mMenu;

    private User mUser;
    private User mUserChanges;

    private boolean mIsEditing = false;

    private View mLayout;

    private EditText mNameEditText;
    private EditText mEmailAddressEditText;

    private View mVehicleDetailsLayout;
    private TextView mVehicleDetailsTextView;

    private Snackbar mChangesSnackbar;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_activity_menu, menu);

        mMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                // Navigate on back stack when pressing the back button.
                super.onBackPressed();
                break;
            case R.id.menu_item_edit:
                // Allow user editing.
                mIsEditing = !mIsEditing;
                toggleEditing(mIsEditing);
                break;
            default:
                super.onOptionsItemSelected(item);
        }
        return true;
    }

    @Override
    public void onClick(View v) {
    }

    public void updateVehicleDetails(View v) {
        showUpdateVehicleDetailsDialog();
    }

    public void changePassword(View v) {
        showChangePaswsordDialog();
    }

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (v.getId()) {
            case R.id.user_name_edit_text:
                // FALLTHROUGH
            case R.id.user_email_edit_text:
                if (!mIsEditing) {
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        Toast
                                .makeText(ProfileActivity.this, getString(
                                        R.string.enable_editing_required_message), Toast.LENGTH_SHORT)
                                .show();
                    }
                    return true;
                }
                break;
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_profile);

        Bundle extras = getIntent().getExtras();
        if (extras != null && !extras.isEmpty()) {
            UserType userType = (UserType) extras.getSerializable("user_type");
            if (userType != null) {
                mUserType = userType;
            }

            // TODO: Get User object from extras.
        }

        mLayout = findViewById(R.id.activity_profile);

        mNameEditText = (EditText) findViewById(R.id.user_name_edit_text);
        mNameEditText.setOnTouchListener(this);
        mEmailAddressEditText = (EditText) findViewById(R.id.user_email_edit_text);
        mEmailAddressEditText.setOnTouchListener(this);

        mVehicleDetailsLayout = findViewById(R.id.vehicle_details_layout);
        mVehicleDetailsTextView = (TextView) findViewById(R.id.vehicle_details_text_view);

        setupChangesSnackbar();

        // TODO: Remove mock user later.
        switch (mUserType) {
            case USER_SERVICE_PROVIDER:
                // Hide vehicle details layout since it's irrelevant.
                mVehicleDetailsLayout.setVisibility(View.GONE);

                mUser = new ProviderUser();
                break;
            case USER_CLIENT:
                // FALLTHROUGH
            default:
                // Mock details
                ClientUser clientUser = new ClientUser();

                VehicleData mockVehicleData = new VehicleData();
                mockVehicleData.setVehicleMake("Audi");
                mockVehicleData.setVehicleModel("R8 Coupe");
                mockVehicleData.setVehicleYear(2016);
                mockVehicleData.setVehicleModifications("5.2 V10 FSI (560 Hp) GT");

                clientUser.setVehicleData(mockVehicleData);
                mUser = clientUser;
                    break;
        }

        // Mock details
        mUser.setName("Max");
        mUser.setEmail("maximglukhov@hotmail.com");

        updateFields();

        // Make a copy of the user.
        mUserChanges = new User(mUser);

        setupActionBar();
    }

    private void toggleEditing(boolean toggle) {
        MenuItem editProfileMenuItem = mMenu.getItem(0);

        if (toggle) {
            Toast.makeText(ProfileActivity.this, getString(R.string.editing_enabled_messeage), Toast.LENGTH_SHORT).show();

            if (editProfileMenuItem != null) {
                editProfileMenuItem.setIcon(R.drawable.ic_check_white_24dp);
                editProfileMenuItem.setTitle(getString(R.string.done));
            }

            // Make sure the user can't undo changes while editing.
            if (mChangesSnackbar.isShownOrQueued()) {
                mChangesSnackbar.dismiss();
            }
        } else {
            mLayout.requestFocus();

            // Force the keyboard to hide to ensure no changes are made when editing is disabled.
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(mLayout.getWindowToken(), 0);

            if (editProfileMenuItem != null) {
                editProfileMenuItem.setIcon(R.drawable.ic_edit_white_24dp);
                editProfileMenuItem.setTitle(getString(R.string.edit));
            }

            finishEditing();
        }
    }

    private void finishEditing() {
        // Save changes
        mUserChanges.setName(mNameEditText.getText().toString());
        mUserChanges.setEmail(mEmailAddressEditText.getText().toString());

        // Check if any of the fields were changed
        if (changesMade()) {
            // Show the user a message notifying him about the changes, also giving him an option to undo the changes.
            mChangesSnackbar.show();
        }
    }

    private boolean changesMade() {
        return !mUserChanges.equals(mUser);
    }

    private void undoChanges() {
        Toast.makeText(ProfileActivity.this, getString(R.string.discarding_changes_message),
                Toast.LENGTH_SHORT).show();

        mUserChanges = new User(mUser);

        updateFields();
    }

    private void saveChanges() {
        mUserChanges.setName(mNameEditText.getText().toString());
        mUserChanges.setEmail(mEmailAddressEditText.getText().toString());

        mUser = new User(mUserChanges);
    }

    private void updateFields() {
        mNameEditText.setText(mUser.getName());
        mEmailAddressEditText.setText(mUser.getEmail());

        switch (mUserType) {
            case NONE:
                // FALLTHROUGH
            case USER_CLIENT:
                mVehicleDetailsTextView.setText(((ClientUser) mUser).getVehicleData().toString());
                break;
            case USER_SERVICE_PROVIDER:
                break;
        }
    }

    private void setupChangesSnackbar() {
        mChangesSnackbar = Snackbar
                .make(mLayout, getString(R.string.saving_changes_message), Snackbar.LENGTH_LONG)
                .setAction(getString(R.string.undo), new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        undoChanges();
                    }
                })
                .setCallback(new Snackbar.Callback() {
                    @Override
                    public void onDismissed(Snackbar snackbar, int event) {
                        // Check if the event was dismissed by anything but the action
                        switch (event) {
                            case DISMISS_EVENT_TIMEOUT:
                                // FALLTHROUGH
                            case DISMISS_EVENT_MANUAL:
                                // FALLTHROUGH
                            case DISMISS_EVENT_CONSECUTIVE:
                                // FALLTHROUGH
                            case DISMISS_EVENT_SWIPE:
                                // FALLTHROUGH
                            default:
                                // FALLTHROUGH
                                saveChanges();
                                break;
                        }
                    }
                });
    }

    private void setupActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.action_bar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                // Display the back key instead of home.
                actionBar.setDisplayHomeAsUpEnabled(true);
                // Enable the icon displaying.
                actionBar.setDisplayShowHomeEnabled(true);
                actionBar.setTitle(getString(R.string.profile_activity_title));
            }
        }
    }

    private void showChangePaswsordDialog() {
        ChangePasswordDialog changePasswordDialog = ChangePasswordDialog.getInstance();
        Utils.showDialogFragment(getSupportFragmentManager(), changePasswordDialog,
                "change_password_dialog");
    }

    private void showUpdateVehicleDetailsDialog() {
        VehicleData vehicleData = ((ClientUser) mUser).getVehicleData();
        RegistrationVehicleDetailsFragment vehicleDetailsFragment =
                RegistrationVehicleDetailsFragment.getInstance(vehicleData);
        Utils.showDialogFragment(getSupportFragmentManager(), vehicleDetailsFragment,
                "change_password_dialog");
    }
}
