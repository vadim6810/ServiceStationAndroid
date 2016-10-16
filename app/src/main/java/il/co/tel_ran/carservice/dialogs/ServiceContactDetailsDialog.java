package il.co.tel_ran.carservice.dialogs;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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

public class ServiceContactDetailsDialog extends DialogFragment implements View.OnClickListener {

    private String mPhonenumber;
    private String mEmail;

    public static ServiceContactDetailsDialog getInstance(String phoneNumber,
                                                          String email) {
        ServiceContactDetailsDialog contactDetailsDialog = new ServiceContactDetailsDialog();

        Bundle args = new Bundle();
        args.putString("phone_number", phoneNumber);
        args.putString("email", email);
        contactDetailsDialog.setArguments(args);

        return contactDetailsDialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.setTitle(getString(R.string.service_contat_details_dialog_title));
        return dialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.TitledDialogFragment);

        Bundle args = getArguments();
        if (args != null && !args.isEmpty()) {
            mPhonenumber = args.getString("phone_number");
            mEmail = args.getString("email");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.service_contact_details_layout, null);

        int accentColor = Utils.getThemeAccentColor(getContext());

        ImageButton dialButton = (ImageButton) layout.findViewById(R.id.dial_button);
        dialButton.setOnClickListener(this);
        dialButton.setColorFilter(accentColor, PorterDuff.Mode.SRC_ATOP);

        TextView phoneNumberTextView = (TextView) layout.findViewById(R.id.phone_number_text_view);
        phoneNumberTextView.setText(mPhonenumber);

        ImageButton emailButton = (ImageButton) layout.findViewById(R.id.email_button);
        emailButton.setOnClickListener(this);
        emailButton.setColorFilter(accentColor, PorterDuff.Mode.SRC_ATOP);

        TextView emailTextView = (TextView) layout.findViewById(R.id.email_text_view);
        emailTextView.setText(mEmail);

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
        Intent intent = null;
        switch (v.getId()) {
            case R.id.dial_button:
                intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + mPhonenumber));
                startActivity(intent);
                break;
            case R.id.email_button:
                intent = new Intent(Intent.ACTION_SENDTO);
                intent.setData(Uri.parse("mailto:" + mEmail));
                intent.putExtra(Intent.EXTRA_SUBJECT, "Service Request");
                break;
        }

        if (intent != null && intent.resolveActivity(getActivity().getPackageManager()) != null) {
            startActivity(intent);
        }
    }
}
