package il.co.tel_ran.carservice.activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;

import il.co.tel_ran.carservice.ClientUser;
import il.co.tel_ran.carservice.ProviderUser;
import il.co.tel_ran.carservice.R;
import il.co.tel_ran.carservice.User;
import il.co.tel_ran.carservice.UserType;
import il.co.tel_ran.carservice.connection.AuthenticationRequest;
import il.co.tel_ran.carservice.connection.AuthenticationRequestMaker;
import il.co.tel_ran.carservice.connection.DataRequest;
import il.co.tel_ran.carservice.connection.DataResult;
import il.co.tel_ran.carservice.connection.RequestMaker;
import il.co.tel_ran.carservice.connection.ServerResponseError;
import il.co.tel_ran.carservice.fragments.RegistrationLoginDetailsFragment;

public class EditUserLoginActivity extends EditProfileInfoActivity
        implements RequestMaker.OnDataRetrieveListener {

    private RegistrationLoginDetailsFragment mRegistrationLoginDetailsFragment;

    public EditUserLoginActivity() {
        super(R.layout.activity_update_user_login,
                R.string.login_title);
    }

    @Override
    public void onDataRetrieveSuccess(DataRequest dataRequest, DataResult result) {
        if (result.getDataType() == DataResult.Type.AUTHENTICATION) {
            finishEditing(mRegistrationLoginDetailsFragment.getUser(), true);
        }
    }

    @Override
    public void onDataRetrieveFailed(DataRequest dataRequest, DataResult.Type resultType,
                                     ServerResponseError error, @Nullable String message) {
        finishWithError("onDataRetrieveFailed :: resultType=" + resultType
                + " error=" + error.toString() + " message=" + message);
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if (which == DialogInterface.BUTTON_POSITIVE) {
            saveUserChanges();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mRegistrationLoginDetailsFragment
                = (RegistrationLoginDetailsFragment) getSupportFragmentManager()
                .findFragmentById(R.id.update_user_login_fragment);

        // Make a copy of the user to make sure we don't edit the one we received from extras.
        // This will help with changes confirmation.
        User user;
        if (mUserType == UserType.MASTER) {
            user = new ProviderUser((ProviderUser) mUser);
        } else {
            user = new ClientUser((ClientUser) mUser);
        }
        mRegistrationLoginDetailsFragment.setUser(user);
        // Enable password layout because we are navigated from ProfileActivity.
        mRegistrationLoginDetailsFragment.enablePasswordEdit(true);
        // Update fields from the user we passed over.
        mRegistrationLoginDetailsFragment.updateFieldsFromUser();
    }

    @Override
    protected boolean isUserChanged() {
        return !compareUsers(mRegistrationLoginDetailsFragment.getUser(), mUser);
    }

    @Override
    protected void saveUserChanges() {
        toggleProgressBar(true, mRegistrationLoginDetailsFragment);

        AuthenticationRequest request = new AuthenticationRequest(
                mRegistrationLoginDetailsFragment.getUser());

        // Send the request
        new AuthenticationRequestMaker(this).makeRequest(EditUserLoginActivity.this, request);
    }
}
