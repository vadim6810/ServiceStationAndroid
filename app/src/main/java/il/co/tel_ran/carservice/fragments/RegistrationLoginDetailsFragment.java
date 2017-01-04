package il.co.tel_ran.carservice.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import java.util.regex.Pattern;

import il.co.tel_ran.carservice.R;
import il.co.tel_ran.carservice.User;
import il.co.tel_ran.carservice.Utils;
import il.co.tel_ran.carservice.activities.SignUpActivity;
import il.co.tel_ran.carservice.dialogs.ChangePasswordDialog;

/**
 * Created by Max on 12/10/2016.
 */

public class RegistrationLoginDetailsFragment extends RegistrationPageFragment
        implements TextView.OnEditorActionListener, View.OnClickListener,
        ChangePasswordDialog.OnPasswordChangeListener {

    private View mLayout;

    private EditText mEmailEditText;
    private TextInputLayout mEmailInputLayout;

    private View mPasswordLayout;
    private EditText mPasswordEditText;
    private TextInputLayout mPasswordInputLayout;

    private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
    private static Pattern mEmailPattern = Pattern.compile(EMAIL_PATTERN);

    private User mUser;

    private boolean mIsEditing;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);

        Bundle extras = getArguments();
        if (extras != null && !extras.isEmpty()) {
            mUser = (User) extras.getSerializable("user");

            mIsEditing = true;
        } else {
            mUser = new User();
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mLayout = inflater.inflate(R.layout.fragment_registration_step_logininfo, null);

        mEmailEditText = (EditText) mLayout.findViewById(R.id.user_email_edit_text);
        mEmailEditText.setOnEditorActionListener(this);
        mEmailInputLayout = (TextInputLayout) mLayout.findViewById(R.id.user_email_input_layout);

        mEmailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mUser.setEmail(s.toString());
            }
        });

        if (mIsEditing) {
            setupPasswordLayout();
        }

        updateFieldsFromUser();

        return mLayout;
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        switch (v.getId()) {
            case R.id.user_email_edit_text:
                if (actionId == EditorInfo.IME_ACTION_DONE && !isEmailValid()) {
                    return true;
                }
                SignUpActivity activity = (SignUpActivity) getActivity();
                if (activity != null) {
                    activity.requestPageChange(false);
                }
                break;
        }
        return false;
    }

    /*
     * View.OnClickListener
     */

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.change_password_button:
                showChangePasswordDialog();
                break;
        }
    }

    @Override
    public boolean isNextStepEnabled() {
        // We don't call the methods in the return statement since it's a logical AND.
        // This guarantees that both methods are called.
        boolean isEmailValid = isEmailValid();

        return isEmailValid;
    }

    /*
     * ChangePasswordDialog.OnPasswordChangeListener
     */

    @Override
    public void onPasswordChanged(String newPassword) {
        if (mIsEditing) {
            mUser.setPassword(newPassword);
            updateFieldsFromUser();
        }
    }

    public User getUser() {
        return mUser;
    }

    public void setUser(User user) {
        mUser = user;
    }

    public void updateFieldsFromUser() {
        if (mUser != null) {
            if (mEmailEditText != null) {
                String email = mUser.getEmail();
                if (email != null && !email.isEmpty()) {
                    mEmailEditText.setText(email);
                }
            }

            if (mPasswordEditText != null) {
                // TODO: when updating to better protected password use a different way to display the password.
                String password = mUser.getPassword();
                if (password != null && !password.isEmpty()) {
                    String asteriskedPassword = Utils.generateAsteriskString(password.length());

                    mPasswordEditText.setText(asteriskedPassword);
                }
            }
        }
    }

    public void enablePasswordEdit(boolean enable) {
        if (mPasswordLayout == null) {
            setupPasswordLayout();
        }

        if (enable) {
            mPasswordLayout.setVisibility(View.VISIBLE);
        } else {
            mPasswordLayout.setVisibility(View.GONE);
        }

        mIsEditing = enable;
    }

    private void setupPasswordLayout() {
        mPasswordLayout = mLayout.findViewById(R.id.password_layout);
        mPasswordLayout.setVisibility(View.VISIBLE);

        mPasswordEditText = (EditText) mLayout.findViewById(R.id.user_password_edit_text);
        mPasswordInputLayout = (TextInputLayout) mLayout.findViewById(
                R.id.user_password_input_layout);

        mLayout.findViewById(R.id.change_password_button).setOnClickListener(this);
    }

    private boolean isEmailValid() {
        boolean isValid =  (mEmailEditText != null
                && mEmailPattern.matcher(mEmailEditText.getText().toString()).matches());

        if (!isValid) {
            mEmailInputLayout.setError(getString(R.string.user_email_error_message));
        } else {
            mEmailInputLayout.setError(null);
        }

        return isValid;
    }

    private void showChangePasswordDialog() {
        ChangePasswordDialog changePasswordDialog = ChangePasswordDialog.getInstance(this,
                mUser.getPassword());
        Utils.showDialogFragment(getFragmentManager(), changePasswordDialog,
                "change_password_dialog");
    }
}
