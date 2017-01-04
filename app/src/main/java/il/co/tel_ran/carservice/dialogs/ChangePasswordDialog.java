package il.co.tel_ran.carservice.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import il.co.tel_ran.carservice.R;

/**
 * Created by Max on 10/10/2016.
 */

public class ChangePasswordDialog extends DialogFragment implements View.OnClickListener {

    private final String LOG_TAG = getClass().getSimpleName();

    private TextInputLayout mCurrentPasswordInputLayout;
    private EditText mCurrentPasswordEditText;

    private TextInputLayout mNewPasswordInputLayout;
    private EditText mNewPasswordEditText;

    private TextInputLayout mConfirmPasswordInputLayout;
    private EditText mConfirmPasswordEditText;

    private String mPassword;

    private OnPasswordChangeListener mListener;

    public interface OnPasswordChangeListener {
        void onPasswordChanged(String newPassword);
    }

    public static ChangePasswordDialog getInstance(OnPasswordChangeListener listener,
                                                   String password) {
        ChangePasswordDialog changePasswordDialog = new ChangePasswordDialog();

        Bundle args = new Bundle();
        args.putString("password", password);
        changePasswordDialog.setArguments(args);

        changePasswordDialog.setListener(listener);

        return changePasswordDialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setTitle(getString(R.string.change_password_button));
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.TitledDialogFragment);

        Bundle arguments = getArguments();
        if (arguments != null && !arguments.isEmpty()) {
            mPassword = arguments.getString("password");
            if (mPassword == null) {
                Log.e(LOG_TAG, "onCreate :: invalid password argument.");
                dismiss();
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_change_password, null);

        mCurrentPasswordInputLayout = (TextInputLayout) layout.findViewById(
                R.id.current_password_input_layout);
        mCurrentPasswordEditText = (EditText) layout.findViewById(R.id.current_password_edit_text);

        mNewPasswordInputLayout = (TextInputLayout) layout.findViewById(
                R.id.new_password_input_layout);
        mNewPasswordEditText = (EditText) layout.findViewById(R.id.new_password_edit_text);

        mConfirmPasswordInputLayout = (TextInputLayout) layout.findViewById(
                R.id.confirm_password_input_layout);
        mConfirmPasswordEditText = (EditText) layout.findViewById(R.id.confirm_password_edit_text);

        layout.findViewById(R.id.change_password_button).setOnClickListener(this);

        return layout;
    }

    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance()) {
            getDialog().setDismissMessage(null);
        }
        super.onDestroyView();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.change_password_button:
                boolean currentPasswordValid = checkPasswordFieldValid(mCurrentPasswordInputLayout,
                        mCurrentPasswordEditText);

                if (currentPasswordValid) {
                    boolean currentPasswordCorrect = checkPasswordFieldCorrect(
                            mCurrentPasswordInputLayout, mCurrentPasswordEditText, mPassword);

                    if (currentPasswordCorrect) {
                        boolean newPasswordValid = checkPasswordFieldValid(mNewPasswordInputLayout,
                                mNewPasswordEditText);

                        if (newPasswordValid) {
                            String newPassword = mNewPasswordEditText.getText().toString();
                            if (checkPasswordFieldCorrect(mConfirmPasswordInputLayout,
                                    mConfirmPasswordEditText, newPassword)) {
                                onPasswordChangeSuccess(newPassword);
                                dismiss();
                            }
                        }
                    }
                }
                break;
        }
    }

    public void setListener(OnPasswordChangeListener listener) {
        mListener = listener;
    }

    private void onPasswordChangeSuccess(String newPassword) {
        if (mListener != null) {
            mListener.onPasswordChanged(newPassword);
        }
    }

    private boolean checkPasswordFieldValid(TextInputLayout inputLayout, EditText editText) {
        String inputPassword = editText.getText().toString();

        if (!isValidPassword(inputPassword)) {
            inputLayout.setError(getString(
                    R.string.invalid_password_error_message));
            return false;
        }

        inputLayout.setError(null);

        return true;
    }

    private boolean checkPasswordFieldCorrect(TextInputLayout inputLayout, EditText editText,
                                              String comparePassword) {
        String inputPassword = editText.getText().toString();

        if (!arePasswordsEqual(inputPassword, comparePassword)) {
            inputLayout.setError(getString(
                    R.string.user_password_error_message));
            return false;
        }

        inputLayout.setError(null);

        return true;
    }

    private boolean isValidPassword(String password) {
        // TODO: update password requirements
        if (password.length() > 0)
            return true;

        return false;
    }

    private boolean arePasswordsEqual(String firstPassword, String secondPassword) {
        return firstPassword.equals(secondPassword);
    }
}
