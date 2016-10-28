package il.co.tel_ran.carservice.fragments;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.Locale;
import java.util.regex.Pattern;

import il.co.tel_ran.carservice.R;
import il.co.tel_ran.carservice.Utils;
import il.co.tel_ran.carservice.activities.SignUpActivity;

/**
 * Created by Max on 12/10/2016.
 */

public class RegistrationLoginDetailsFragment extends RegistrationPageFragment
        implements TextView.OnEditorActionListener {

    private EditText mNameEditText;
    private TextInputLayout mNameInputLayout;

    private EditText mEmailEditText;
    private TextInputLayout mEmailInputLayout;

    private static final String EMAIL_PATTERN = "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@" + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";
    private static Pattern mEmailPattern = Pattern.compile(EMAIL_PATTERN);

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mLayout = inflater.inflate(R.layout.fragment_registration_step_logininfo, null);

        mNameEditText = (EditText) mLayout.findViewById(R.id.user_name_edit_text);
        mNameEditText.setOnEditorActionListener(this);
        mNameInputLayout = (TextInputLayout) mLayout.findViewById(R.id.user_name_input_layout);
        mEmailEditText = (EditText) mLayout.findViewById(R.id.user_email_edit_text);
        mEmailEditText.setOnEditorActionListener(this);
        mEmailInputLayout = (TextInputLayout) mLayout.findViewById(R.id.user_email_input_layout);

        mNameEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                isNameValid();
            }
        });
        return mLayout;
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        switch (v.getId()) {
            case R.id.user_name_edit_text:
                if (actionId == EditorInfo.IME_ACTION_NEXT && !isNameValid()) {
                    return true;
                }
                break;
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

    @Override
    public boolean isNextStepEnabled() {
        // We don't call the methods in the return statement since it's a logical AND.
        // This guarantees that both methods are called.
        boolean isNameValid = isNameValid();
        boolean isEmailValid = isEmailValid();

        return isNameValid && isEmailValid;
    }

    private boolean isNameValid() {
        boolean isValid = (mNameEditText != null && !(mNameEditText.getText().toString().isEmpty()));

        if (!isValid) {
            mNameInputLayout.setError(getString(R.string.user_name_error_message));
        } else {
            mNameInputLayout.setError(null);
        }

        return isValid;
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
}
