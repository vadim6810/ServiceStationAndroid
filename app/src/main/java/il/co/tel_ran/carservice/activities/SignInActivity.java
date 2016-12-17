package il.co.tel_ran.carservice.activities;

import android.content.Intent;
import android.os.Bundle;
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

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Pattern;

import il.co.tel_ran.carservice.ClientUser;
import il.co.tel_ran.carservice.ProviderUser;
import il.co.tel_ran.carservice.R;
import il.co.tel_ran.carservice.connection.ServerConnection;
import il.co.tel_ran.carservice.User;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;

public class SignInActivity extends AppCompatActivity implements TextView.OnEditorActionListener {

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
        // Temporary: switch to provider main activity
//        Intent providerIntent = new Intent(SignInActivity.this, ProviderMainActivity.class);

        // When sign in is complete, we should retrieve user information from back-end.
        // In this case we would end up with an instance of ProviderUser, which should contain
        // service ID.
        // For now we will use a mock user id to load user's service.
        /*providerIntent.putExtra("service_id", 1);

        User mockUser = new User();
        mockUser.setName("Maxim Glukhov");
        mockUser.setEmail("maximglukhov@hotmail.com");

        providerIntent.putExtra("user", mockUser);

        startActivity(providerIntent);*/

        authenticateUser();

        // TODO: Sign in - send data to back-end and wait for answer.
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

        ServerConnection.authenticateUser(SignInActivity.this, email, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {
                // Check if email is registered
                if (response == null || response.isEmpty()) {
                    mEmailInputLayout.setError(getString(R.string.user_incorrect_email_error_message));
                } else {
                    try {
                        JSONArray responseJSONArray = new JSONArray(response);
                        JSONObject userJSONObject = responseJSONArray.getJSONObject(0);

                        // Validate password
                        if (validatePassword(password, userJSONObject.getString("password"))) {
                            // Authenticated successfully.
                            User user = new User();

                            long id = Long.parseLong(userJSONObject.getString("idUser"));

                            user.setId(id);
                            user.setEmail(email);

                            String userType = userJSONObject.getString("role");
                            if (userType.equals("master")) {
                                // Change the user type to Provider
                                user = new ProviderUser(user);

                                // Create new intent for ProviderMainActivity
                                Intent intent = new Intent(SignInActivity.this, ProviderMainActivity.class);
                                intent.putExtra("user", user);

                                // Clear the back-stack so user can't go back to Sign In form.
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);

                                // Finish this activity.
                                finish();
                            } else {
                                user = new ClientUser(user);

                                // Create a result intent for ClientMainActivity.
                                Intent intent = new Intent();
                                intent.putExtra("user", user);

                                // Set result to OK.
                                setResult(RESULT_OK, intent);

                                // Finish this activity
                                finish();
                            }
                        } else {
                            mPasswordInputLayout.setError(getString(R.string.user_password_error_message));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            private boolean validatePassword(String inputPassword, String comparePassword) {
                return inputPassword.equals(comparePassword);
            }

        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("SIA", "onResponse::error" + error.getMessage());
            }
        });
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
}
