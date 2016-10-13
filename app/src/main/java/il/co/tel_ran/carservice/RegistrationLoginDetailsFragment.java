package il.co.tel_ran.carservice;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

/**
 * Created by Max on 12/10/2016.
 */

public class RegistrationLoginDetailsFragment extends Fragment implements View.OnClickListener {

    private EditText mNameEditText;
    private EditText mEmailEditText;
    private Button mNextStepButton;
    private Button mPreviousStepButton;

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
        mPreviousStepButton = (Button) mLayout.findViewById(R.id.logininfo_previous_step);

        // SDK > 21 supports autoMirrored attribute, this code is for backwards compatibility.
        if (Build.VERSION.SDK_INT < 21) {
            if (SignUpActivity.isRTL) {
                // Only check for RTL languages since by default it's configured for LTR languages.
                Drawable navigateLeft = ContextCompat.getDrawable(getContext(), R.drawable.ic_navigate_before_accent_24dp);
                Drawable navigateRight = ContextCompat.getDrawable(getContext(), R.drawable.ic_navigate_next_accent_24dp);
                mNextStepButton.setCompoundDrawablesWithIntrinsicBounds(navigateLeft, null, null, null);
                mPreviousStepButton.setCompoundDrawablesWithIntrinsicBounds(null, null, navigateRight, null);
            }
        }

        mNameEditText = (EditText) mLayout.findViewById(R.id.user_name_edit_text);
        mEmailEditText = (EditText) mLayout.findViewById(R.id.user_email_edit_text);

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
        mPreviousStepButton.setOnClickListener(this);
        return mLayout;
    }

    private void checkNextStepEnabled() {
        boolean isNameValid = !mNameEditText.getText().toString().isEmpty();
        // TODO: Add regex check for email.
        boolean isEmailValid = true;

        mNextStepButton.setEnabled(isEmailValid && isNameValid);
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
                if (containerActivity != null) {
                    // Advance one page.
                    containerActivity.requestViewPagerPage(SignUpActivity.PAGE_USER_DETAILS);
                }
                break;
        }
    }
}
