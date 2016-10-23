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

public class RegistrationLoginDetailsFragment extends Fragment implements View.OnClickListener, TextView.OnEditorActionListener {

    private EditText mNameEditText;
    private TextInputLayout mNameInputLayout;

    private EditText mEmailEditText;
    private TextInputLayout mEmailInputLayout;

    private Button mNextStepButton;
    private boolean mIsNextStepButtonEnabled;

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

        mNextStepButton = (Button) mLayout.findViewById(R.id.logininfo_next_step);
        Button previousStepButton = (Button) mLayout.findViewById(R.id.logininfo_previous_step);

        // Arrow direction and placement relative to the button should be in the opposite direction.
        // RTL - to the left
        // LTR - to the right
        Drawable navigateLeft = ContextCompat.getDrawable(getContext(), R.drawable.ic_navigate_before_accent_24dp);
        Drawable navigateRight = ContextCompat.getDrawable(getContext(), R.drawable.ic_navigate_next_accent_24dp);
        if (Utils.isLocaleRTL(Locale.getDefault())) {
            previousStepButton.setCompoundDrawablesWithIntrinsicBounds(null, null, navigateRight, null);
            mNextStepButton.setCompoundDrawablesWithIntrinsicBounds(navigateLeft, null, null, null);
        } else {
            previousStepButton.setCompoundDrawablesWithIntrinsicBounds(navigateLeft, null, null, null);
            mNextStepButton.setCompoundDrawablesWithIntrinsicBounds(null, null, navigateRight, null);
        }

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
                checkNextStepEnabled();
                if (!isNameValid()) {
                    mNameInputLayout.setError(getString(R.string.user_name_error_message));
                } else {
                    mNameInputLayout.setError(null);
                }
            }
        });
        mEmailEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                checkNextStepEnabled();
            }
        });

        mNextStepButton.setOnClickListener(this);
        previousStepButton.setOnClickListener(this);
        return mLayout;
    }

    private void checkNextStepEnabled() {
        // Make sure input name is not empty and e-mail address is valid.
        mIsNextStepButtonEnabled = isEmailValid() && isNameValid();
    }

    private boolean isNameValid() {
        return !mNameEditText.getText().toString().isEmpty();
    }

    private boolean isEmailValid() {
        return mEmailPattern.matcher(mEmailEditText.getText().toString()).matches();
    }

    @Override
    public void onClick(View v) {
        SignUpActivity containerActivity = null;
        try {
            containerActivity = (SignUpActivity) getActivity();
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
        switch (v.getId()) {
            case R.id.logininfo_previous_step:
                if (containerActivity != null) {
                    // Go back one page.
                    containerActivity.requestViewPagerPage(SignUpActivity.PAGE_USER_TYPE);
                }
                break;
            case R.id.logininfo_next_step:
                if (mIsNextStepButtonEnabled) {
                    mEmailInputLayout.setError(null);
                    mNameInputLayout.setError(null);

                    if (containerActivity != null) {
                        // Advance one page.
                        containerActivity.requestViewPagerPage(SignUpActivity.PAGE_USER_DETAILS);
                    }
                } else {
                    if (!isNameValid()) {
                        mNameInputLayout.setError(getString(R.string.user_name_error_message));
                    }
                    if (!isEmailValid()) {
                        mEmailInputLayout.setError(getString(R.string.user_email_error_message));
                    }
                }
                break;
        }
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        switch (v.getId()) {
            case R.id.user_name_edit_text:
                if (actionId == EditorInfo.IME_ACTION_NEXT && !isNameValid()) {
                        mNameInputLayout.setError(getString(R.string.user_name_error_message));
                    return true;
                }
                break;
            case R.id.user_email_edit_text:
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    onClick(mNextStepButton);
                }
                return true;
        }
        return false;
    }
}
