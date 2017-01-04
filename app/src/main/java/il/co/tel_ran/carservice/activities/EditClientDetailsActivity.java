package il.co.tel_ran.carservice.activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import com.android.volley.Request;

import il.co.tel_ran.carservice.ClientUser;
import il.co.tel_ran.carservice.R;
import il.co.tel_ran.carservice.connection.DataRequest;
import il.co.tel_ran.carservice.connection.DataResult;
import il.co.tel_ran.carservice.connection.NewClientUserRequest;
import il.co.tel_ran.carservice.connection.NewUserRequestMaker;
import il.co.tel_ran.carservice.connection.RequestMaker;
import il.co.tel_ran.carservice.connection.ServerResponseError;
import il.co.tel_ran.carservice.fragments.RegistrationClientDetailsFragment;

public class EditClientDetailsActivity extends EditProfileInfoActivity
        implements RequestMaker.OnDataRetrieveListener {

    private RegistrationClientDetailsFragment mClientDetailsFragment;

    public EditClientDetailsActivity() {
        super(R.layout.activity_update_client_details,
                R.string.personal_info_title);
    }

    @Override
    public void onDataRetrieveSuccess(DataRequest dataRequest, DataResult result) {
        if (result.getDataType() == DataResult.Type.NEW_USER) {
            finishEditing(mClientDetailsFragment.getUser(), true);
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

        mClientDetailsFragment
                = (RegistrationClientDetailsFragment) getSupportFragmentManager()
                .findFragmentById(R.id.update_client_details_fragment);

        // Make a copy of the user to make sure we don't edit the one we received from extras.
        // This will help with changes confirmation.
        // Use casting to make sure we use the right copy constructor.
        mClientDetailsFragment.setUser(new ClientUser((ClientUser) mUser));
        // Update fields from the user we passed over.
        mClientDetailsFragment.updateFieldsFromUser();
    }

    @Override
    protected boolean isUserChanged() {
        return !compareUsers(mClientDetailsFragment.getUser(), mUser);
    }

    @Override
    protected void saveUserChanges() {
        toggleProgressBar(true, mClientDetailsFragment);

        NewClientUserRequest request = new NewClientUserRequest(mClientDetailsFragment.getUser());
        // Update request method.
        request.setRequestMethod(Request.Method.PUT);
        // Send the request
        new NewUserRequestMaker(this).makeRequest(EditClientDetailsActivity.this, request);
    }
}
