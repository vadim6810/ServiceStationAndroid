package il.co.tel_ran.carservice.dialogs;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import il.co.tel_ran.carservice.R;
import il.co.tel_ran.carservice.ServiceSearchResult;

/**
 * Created by Max on 16/10/2016.
 */

public class ServiceLeaveMessageDialog extends DialogFragment
        implements DialogInterface.OnClickListener {

    private static final int MAX_MESSAGE_LENGTH = 100; // Max message characters.

    private static ServiceSearchResult mSearchResult;
    private static LeaveMessageDialogListener mListener;

    private TextInputLayout mLeaveMessageInputLayout;
    private EditText mLeaveMessageEditText;

    public interface LeaveMessageDialogListener {
        void onMessageSubmitted(CharSequence message, ServiceSearchResult searchResult);
    }

    public static ServiceLeaveMessageDialog getInstance(ServiceSearchResult result,
                                                        LeaveMessageDialogListener listener) {
        ServiceLeaveMessageDialog leaveMessageDialog = new ServiceLeaveMessageDialog();

        mSearchResult = result;

        mListener = listener;

        return leaveMessageDialog;
    }

    public View onCreateDialogView(LayoutInflater inflater, @Nullable ViewGroup container,
                                   @Nullable Bundle savedInstanceState) {
        // Inflate the dialog layout.
        View layout = inflater.inflate(R.layout.service_leave_message_layout, null);

        mLeaveMessageInputLayout = (TextInputLayout) layout.findViewById(
                R.id.leave_message_input_layout);

        mLeaveMessageEditText = (EditText) layout.findViewById(
                R.id.leave_message_edit_text);
        // Add TextWatcher to validate user's input.
        mLeaveMessageEditText.addTextChangedListener(new MessageTextWatcher());

        return layout;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = onCreateDialogView(getActivity().getLayoutInflater(), null, null);
        onViewCreated(view, null);

        // Build the AlertDialog.
        // We are using AlertDialog to add native support for buttons.
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.leave_message_dialog_title))
                .setView(view)
                .setPositiveButton(R.string.button_submit, this)
                .setNeutralButton(R.string.button_cancel, this);
        return builder.create();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.TitledDialogFragment);
    }

    @Override
    public void onStart() {
        super.onStart();

        final AlertDialog dialog = (AlertDialog) getDialog();
        if (dialog != null) {
            // Overriding dialog's button listener gives us the control over dismissing the dialog.
            dialog.getButton(DialogInterface.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (isMessageValid(mLeaveMessageEditText.getText())) {
                        // Only dismiss the dialog with submit option if the message has the appropriate length.
                        dismiss();
                    } else {
                        // If the error is not displayed yet, display it so the user get's feedback on why he can't proceed.
                        if (mLeaveMessageInputLayout.getError() == null) {
                            if (mLeaveMessageEditText.length() == 0) {
                                mLeaveMessageInputLayout.setError(getString(
                                        R.string.leave_message_empty_error));
                            } else {
                                mLeaveMessageInputLayout.setError(getString(
                                        R.string.leave_message_too_long, MAX_MESSAGE_LENGTH));
                            }
                        }
                    }
                }
            });
        }
    }

    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance()) {
            getDialog().setDismissMessage(null);
        }
        super.onDestroyView();
    }

    // Handle button type click here.
    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                if (mLeaveMessageEditText != null) {
                    CharSequence message = mLeaveMessageEditText.getText();
                    if (mListener != null && isMessageValid(message)) {
                        mListener.onMessageSubmitted(message, mSearchResult);
                    }
                }
                break;
            case DialogInterface.BUTTON_NEUTRAL:
                break;
        }
    }

    private boolean isMessageValid(CharSequence message) {
        int length = message.length();
        return length > 0 && length <= MAX_MESSAGE_LENGTH;
    }

    private class MessageTextWatcher implements TextWatcher {
        private int mLastLength;

        private boolean mSpanSet = false;
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
        }

        @Override
        public void afterTextChanged(Editable s) {
            int length = s.length();

            // Make sure we don't get stuck in an infinite loop.
            if (mLastLength == length)
                return;
            mLastLength = length;

            Context context = getContext();

            if (length == 0) {
                // User has to type at least something
                mLeaveMessageInputLayout.setError(context.getString(R.string.leave_message_empty_error));
            }

            // Notify the user that he has exceeded the allowed character count.
            if (length > MAX_MESSAGE_LENGTH) {
                // Apply span if we haven't already.
                if (!mSpanSet) {
                    SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder(
                            s);
                    // Color the exceeding characters to let the user know which characters are exceeding.
                    spannableStringBuilder.setSpan(new ForegroundColorSpan(ContextCompat.getColor(
                            context, R.color.colorSecondaryText)),
                            MAX_MESSAGE_LENGTH, length,
                            Spanned.SPAN_EXCLUSIVE_INCLUSIVE);
                    // Apply the spanned text.
                    mLeaveMessageEditText.setText(spannableStringBuilder);
                    // Move the cursor back to the end of the text.
                    mLeaveMessageEditText.setSelection(length);
                    mSpanSet = true;
                }
                // Show the error message.
                if (mLeaveMessageInputLayout.getError() == null) {
                    mLeaveMessageInputLayout.setError(context.getString(R.string.leave_message_too_long,
                            MAX_MESSAGE_LENGTH));
                }
            } else {
                // Make sure we don't accidentally remove the previous error message if the length is still at 0.
                if (length > 0)
                    mLeaveMessageInputLayout.setError(null);

                // Remove spans because the length is no longer exceeding the maximum amount of allowed characters.
                if (mSpanSet) {
                    ForegroundColorSpan[] spans = s.getSpans(0, length,
                            ForegroundColorSpan.class);
                    if (spans.length > 0)
                        s.removeSpan(spans[0]);
                }
                mSpanSet = false;
            }
        }
    }
}
