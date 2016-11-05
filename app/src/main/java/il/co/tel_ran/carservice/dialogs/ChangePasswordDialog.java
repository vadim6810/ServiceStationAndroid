package il.co.tel_ran.carservice.dialogs;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import il.co.tel_ran.carservice.R;
import il.co.tel_ran.carservice.Utils;

/**
 * Created by Max on 10/10/2016.
 */

public class ChangePasswordDialog extends DialogFragment implements View.OnClickListener {

    private String mPhonenumber;
    private String mEmail;
    private TextInputLayout mCurrentPasswordInputLayout;
    private TextInputLayout mNewPasswordInputLayout;
    private TextInputLayout mConfirmPasswordInputLayout;

    public static ChangePasswordDialog getInstance() {
        return new ChangePasswordDialog();
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
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.fragment_change_password, null);

        mCurrentPasswordInputLayout = (TextInputLayout) layout.findViewById(
                R.id.current_password_input_layout);
        mNewPasswordInputLayout = (TextInputLayout) layout.findViewById(
                R.id.new_password_input_layout);
        mConfirmPasswordInputLayout = (TextInputLayout) layout.findViewById(
                R.id.confirm_password_input_layout);

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
                // TODO: check if current password is valid.
                // TODO: check if new password matches the correct regex.
                // TODO: check if confirm password is correct.
                break;
        }
    }
}
