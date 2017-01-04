package il.co.tel_ran.carservice.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import il.co.tel_ran.carservice.ClientUser;
import il.co.tel_ran.carservice.ProviderUser;
import il.co.tel_ran.carservice.R;
import il.co.tel_ran.carservice.User;
import il.co.tel_ran.carservice.UserType;

/**
 * Created by maxim on 03-Jan-17.
 */

public abstract class EditProfileInfoActivity extends AppCompatActivity
    implements DialogInterface.OnClickListener {

    public static final int RESULT_ERROR = 0;

    protected final String LOG_TAG = getClass().getSimpleName();

    protected ProgressBar mProgressBar;

    protected User mUser;
    protected UserType mUserType;

    private int mLayoutResId;
    private final int mTitleResId;

    public EditProfileInfoActivity(int layoutResId, int titleResId) {
        mLayoutResId = layoutResId;
        mTitleResId = titleResId;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_update_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_done:
                if (isUserChanged()) {
                    showFinishConfirmationAlertDialog();
                } else {
                    Toast.makeText(EditProfileInfoActivity.this, R.string.no_changes_made_message,
                            Toast.LENGTH_SHORT)
                            .show();
                }
                return true;
            case android.R.id.home:
                super.onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /*
     * DialogInterface.OnClickListener
     */

    @Override
    public abstract void onClick(DialogInterface dialog, int which);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(mLayoutResId);

        Intent intent = getIntent();
        if (intent == null) {
            finishWithError("onCreate :: activity started with null intent.");
        } else {
            Bundle extras = intent.getExtras();
            if (extras != null && !extras.isEmpty()) {
                mUserType = (UserType) extras.getSerializable("user_type");
                if (mUserType == null) {
                    finishWithError("onCreate :: UserType extra object is null.");
                }

                if (mUserType == UserType.CLIENT) {
                    mUser = (ClientUser) extras.getSerializable("user");
                } else {
                    mUser = (ProviderUser) extras.getSerializable("user");
                }

                if (mUser == null) {
                    finishWithError("onCreate :: User extra object is null.");
                }
            } else {
                finishWithError("onCreate :: activity started with null or empty extras bundle.");
            }
        }

        mProgressBar = (ProgressBar) findViewById(R.id.saving_progressbar);

        setupActionBar();
    }

    protected abstract boolean isUserChanged();

    protected abstract void saveUserChanges();

    protected boolean compareUsers(User firstUser, User otherUser) {
        return firstUser.equals(otherUser);
    }

    protected void toggleProgressBar(boolean toggle, Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();

        if (toggle) {
            fragmentTransaction.hide(fragment);
            mProgressBar.setVisibility(View.VISIBLE);
        } else {
            fragmentTransaction.show(fragment);
            mProgressBar.setVisibility(View.GONE);
        }
        fragmentTransaction.commit();
    }

    protected void finishWithError(String message) {
        Log.e(LOG_TAG, message);

        Intent intent = new Intent();
        intent.putExtra("error_message", message);

        setResult(RESULT_ERROR, intent);
        finish();
    }

    protected void finishEditing(User user, boolean isUserChanged) {
        Intent intent = new Intent();
        intent.putExtra("edited_user", user);
        intent.putExtra("is_user_changed", isUserChanged);

        setResult(RESULT_OK, intent);
        finish();
    }

    private void setupActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.action_bar);

        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setTitle(mTitleResId);
        }
    }

    private void showFinishConfirmationAlertDialog() {
        AlertDialog alertDialog = new AlertDialog.Builder(EditProfileInfoActivity.this)
                .setTitle(R.string.finish_editing_title)
                .setMessage(R.string.keep_changes_confirmation_message)
                .setPositiveButton(R.string.finish_button, this)
                .setNegativeButton(R.string.button_cancel, null)
                .create();

        alertDialog.show();
    }
}
