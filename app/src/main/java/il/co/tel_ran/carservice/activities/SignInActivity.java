package il.co.tel_ran.carservice.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.util.regex.Pattern;

import il.co.tel_ran.carservice.ClientUser;
import il.co.tel_ran.carservice.ProviderUser;
import il.co.tel_ran.carservice.R;
import il.co.tel_ran.carservice.UserType;
import il.co.tel_ran.carservice.Utils;
import il.co.tel_ran.carservice.User;
import il.co.tel_ran.carservice.connection.AuthenticationRequestMaker;
import il.co.tel_ran.carservice.connection.DataRequest;
import il.co.tel_ran.carservice.connection.DataResult;
import il.co.tel_ran.carservice.connection.AuthenticationRequest;
import il.co.tel_ran.carservice.connection.RequestMaker;
import il.co.tel_ran.carservice.connection.ServerResponseError;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;

public class SignInActivity extends AppCompatActivity implements TextView.OnEditorActionListener,
        RequestMaker.OnDataRetrieveListener {

    private final String LOG_TAG = getClass().getSimpleName();

    private EditText mEmailEditText;
    private TextInputLayout mEmailInputLayout;

    private EditText mPasswordEditText;
    private TextInputLayout mPasswordInputLayout;

    private FloatingActionButton mLoginFAB;
    private ProgressBar mConnectionProgressBar;

    private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
    private static Pattern mEmailPattern = Pattern.compile(EMAIL_PATTERN);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        // TODO: Add text watchers to edittext fields

        mEmailEditText = (EditText) findViewById(R.id.login_email_edit_text);
        mEmailEditText.setOnEditorActionListener(this);
        mEmailInputLayout = (TextInputLayout) findViewById(R.id.login_email_input_layout);

        mPasswordEditText = (EditText) findViewById(R.id.login_password_edit_text);
        mPasswordEditText.setOnEditorActionListener(this);
        mPasswordInputLayout = (TextInputLayout) findViewById(R.id.login_password_input_layout);

        mLoginFAB = (FloatingActionButton) findViewById(R.id.login_fab);
        mConnectionProgressBar = (ProgressBar) findViewById(R.id.connection_progressbar);

        setupActionBar();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                super.onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        switch (v.getId()) {
            case R.id.login_email_edit_text:
                if (isEmailValid(mEmailEditText.getText())) {
                    mEmailInputLayout.setError(null);
                } else {
                    mEmailInputLayout.setError(getString(R.string.user_email_error_message));
                    return true;
                }
                break;
            case R.id.login_password_edit_text:
                if (isPasswordValid(mPasswordEditText.getText())) {
                    mPasswordInputLayout.setError(null);
                    authenticateUser();
                } else {
                    mPasswordInputLayout.setError(getString(R.string.user_password_error_message));
                    return true;
                }
                break;
        }
        return false;
    }

    public void navigateToSignUp(View view) {
        Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
        // Use clear FLAG_ACTIVITY_CLEAR_TOP to ensure we don't get stack overflow.
        intent.addFlags(FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
    }

    public void onSignInClick(View view) {
        authenticateUser();
    }

    /*
     * RequestMaker.OnDataRetrieveListener
     */

    @Override
    public void onDataRetrieveSuccess(DataRequest dataRequest, DataResult result) {
        switch (result.getDataType()) {
            case AUTHENTICATION:
                JSONObject[] resultJSONs = (JSONObject[]) result.getData();

                if (resultJSONs != null && resultJSONs.length > 0) {
                    JSONObject resultJSON = resultJSONs[0];
                    handleAuthentication(resultJSON);
                } else {
                    // TODO: handle error
                    Log.e(LOG_TAG, "onDataRetrieveSuccess :: failed to retrieve authentication results");
                    finish();
                }
                break;
        }

        // Hide the progress bar
        toggleConenctionProgressBar(false);
    }

    @Override
    public void onDataRetrieveFailed(DataRequest dataRequest, DataResult.Type resultType,
                                     ServerResponseError error, @Nullable String message) {
        // Hide the progress bar
        toggleConenctionProgressBar(false);

        switch (error) {
            case TIMEOUT:
                break;
            case NO_CONNECTION:
                break;
            case AUTH_FAILURE:
                break;
            case SERVER:
                break;
            case NETWORK:
                break;
            case PARSE:
                break;
            case INCORRECT_EMAIL:
                if (resultType == DataResult.Type.AUTHENTICATION) {
                    mEmailInputLayout.setError(
                            getString(R.string.user_incorrect_email_error_message));
                }
                break;
            case INCORRECT_PASSWORD:
                break;
            case ROLE:
                break;
            case INVALID_PARAMETER:
                break;
            case USER_NOT_FOUND:
                break;
            case SERVICE_NOT_FOUND:
                break;
            case INVALID_ID:
                break;
            case REVIEW_NOT_FOUND:
                break;
            case VEHICLE_DB_EMPTY:
                break;
        }
    }

    private void authenticateUser() {
        final String email = mEmailEditText.getText().toString();
        if (isEmailValid(email)) {
            mEmailInputLayout.setError(null);
        } else {
            mEmailInputLayout.setError(getString(R.string.user_email_error_message));
            return;
        }

        final String password = mPasswordEditText.getText().toString();
        if (isPasswordValid(password)) {
            mPasswordInputLayout.setError(null);
        } else {
            mPasswordInputLayout.setError(getString(R.string.user_password_error_message));
            return;
        }

        // Show the progress bar.
        toggleConenctionProgressBar(true);

        AuthenticationRequest request = new AuthenticationRequest(email);
        new AuthenticationRequestMaker(this).makeRequest(SignInActivity.this, request);
    }

    private void setupActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.actionBar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeButtonEnabled(true);
            actionBar.setTitle(getString(R.string.button_sign_in));
        }
    }

    private boolean isEmailValid(CharSequence email) {
        return mEmailPattern.matcher(email).matches();
    }

    private boolean isPasswordValid(CharSequence password) {
        // TODO: check requirements for a valid password.
        // NOTE: passwords are sent to user via-email.
//        return password.length() > 7;
        if (password.length() == 0)
            return false;
        // Temporary return true until password requirements are defined.
        return true;
    }

    private void toggleConenctionProgressBar(boolean toggle) {
        if (toggle) {
            mLoginFAB.setVisibility(View.GONE);
            mConnectionProgressBar.setVisibility(View.VISIBLE);
        } else {
            mLoginFAB.setVisibility(View.VISIBLE);
            mConnectionProgressBar.setVisibility(View.GONE);

        }
    }

    private void handleAuthentication(JSONObject resultJSON) {
        String inputPassword = mPasswordEditText.getText().toString();
        String comparePassword = resultJSON.optString(
                AuthenticationRequestMaker.JSON_FIELD_PASSWORD);

        String role = resultJSON.optString(AuthenticationRequestMaker.JSON_FIELD_TYPE);

        boolean authenticationSuccess = checkPassword(inputPassword, comparePassword);
        if (authenticationSuccess) {
            if (role.equals(AuthenticationRequestMaker.JSON_FIELD_TYPE_MASTER)) {
                // User is authenticated as master
                onAuthenticationSuccess(UserType.MASTER, resultJSON);
            } else if (role.equals(AuthenticationRequestMaker.JSON_FIELD_TYPE_CLIENT)) {
                // User is authenticated as client
                onAuthenticationSuccess(UserType.CLIENT, resultJSON);
            } else {
                // TODO: handle error
                Log.e(LOG_TAG, "onDataRetrieveSuccess :: failed to identify user role.");
                finish();
            }
        } else {
            mPasswordInputLayout.setError(getString(R.string.user_password_error_message));
        }
    }

    private boolean checkPassword(String inputPassword, String comparePassword) {
        return inputPassword.equals(comparePassword);
    }

    public void onAuthenticationSuccess(UserType userType, JSONObject authenticationJSON) {
        User user = new User();

        long id = 0;
        long idUser = 0;
        String email = "";
        String password = "";
        try {
            // Refers to id in authentication JSON
            id = Long.parseLong(authenticationJSON.getString(AuthenticationRequestMaker.JSON_FIELD_ID));
            email = authenticationJSON.getString(AuthenticationRequestMaker.JSON_FIELD_EMAIL);
            password = authenticationJSON.getString(AuthenticationRequestMaker.JSON_FIELD_PASSWORD);

            idUser = Long.parseLong(authenticationJSON.getString(
                    AuthenticationRequestMaker.JSON_FIELD_IDUSER));

            String createdAt = authenticationJSON.getString(
                    AuthenticationRequestMaker.JSON_FIELD_CREATE_DATETIME);
            String updatedAt = authenticationJSON.getString(
                    AuthenticationRequestMaker.JSON_FIELD_UPDATE_DATETIME);

            try {
                user.setCreationDate(Utils.parseDateTime(updatedAt));
                user.setUpdateDate(Utils.parseDateTime(createdAt));
            } catch (ParseException e) {
                e.printStackTrace();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        user.setId(id);
        user.setEmail(email);
        user.setPassword(password);

        Intent intent;
        switch (userType) {
            case CLIENT:
                user = new ClientUser(user);

                ClientUser userAsClient = (ClientUser) user;
                userAsClient.setClientId(idUser);

                // Create a result intent for ClientMainActivity.
                intent = new Intent();
                intent.putExtra("user", user);

                // Set result to OK.
                setResult(RESULT_OK, intent);

                // Finish this activity
                finish();
                break;
            case MASTER:
                // Change the user type to Provider
                user = new ProviderUser(user);

                ((ProviderUser) user).setMasterId(idUser);

                // Create new intent for ProviderMainActivity
                intent = new Intent(SignInActivity.this, ProviderMainActivity.class);
                intent.putExtra("user", user);

                // Clear the back-stack so user can't go back to Sign In form.
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);

                // Finish this activity.
                finish();
                break;
        }
    }
}
