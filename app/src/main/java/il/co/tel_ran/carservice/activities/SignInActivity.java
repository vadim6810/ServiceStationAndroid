package il.co.tel_ran.carservice.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import java.util.regex.Pattern;

import il.co.tel_ran.carservice.R;
import il.co.tel_ran.carservice.User;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;

public class SignInActivity extends AppCompatActivity implements TextView.OnEditorActionListener {

    private EditText mEmailEditText;
    private TextInputLayout mEmailInputLayout;

    private EditText mPasswordEditText;
    private TextInputLayout mPasswordInputLayout;

    private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
    private static Pattern mEmailPattern = Pattern.compile(EMAIL_PATTERN);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);

        mEmailEditText = (EditText) findViewById(R.id.login_email_edit_text);
        mEmailEditText.setOnEditorActionListener(this);
        mEmailInputLayout = (TextInputLayout) findViewById(R.id.login_email_input_layout);

        mPasswordEditText = (EditText) findViewById(R.id.login_password_edit_text);
        mPasswordEditText.setOnEditorActionListener(this);
        mPasswordInputLayout = (TextInputLayout) findViewById(R.id.login_password_input_layout);

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

    public void signIn(View view) {
        // Temporary: switch to provider main activity
        Intent providerIntent = new Intent(SignInActivity.this, ProviderMainActivity.class);

        // When sign in is complete, we should retrieve user information from back-end.
        // In this case we would end up with an instance of ProviderUser, which should contain
        // service ID.
        // For now we will use a mock user id to load user's service.
        providerIntent.putExtra("service_id", 1);

        User mockUser = new User();
        mockUser.setName("Maxim Glukhov");
        mockUser.setEmail("maximglukhov@hotmail.com");

        providerIntent.putExtra("user", mockUser);

        startActivity(providerIntent);

        /*if (isEmailValid(mEmailEditText.getText())) {
            mEmailInputLayout.setError(null);
        } else {
            mEmailInputLayout.setError(getString(R.string.user_email_error_message));
        }
        if (isPasswordValid(mPasswordEditText.getText())) {
            mPasswordInputLayout.setError(null);
        } else {
            mPasswordInputLayout.setError(getString(R.string.user_password_error_message));
        }*/

        // TODO: Sign in - send data to back-end and wait for answer.
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
        return password.length() > 7;
    }
}
