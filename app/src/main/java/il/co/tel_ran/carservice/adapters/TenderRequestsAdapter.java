package il.co.tel_ran.carservice.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import il.co.tel_ran.carservice.MaxLengthTextWatcher;
import il.co.tel_ran.carservice.R;
import il.co.tel_ran.carservice.TenderRequest;
import il.co.tel_ran.carservice.Utils;

/**
 * Created by maxim on 12-Nov-16.
 */

public class TenderRequestsAdapter
        extends RecyclerView.Adapter<TenderRequestsAdapter.ViewHolder> {

    private static final int MAX_MESSAGE_LENGTH = 50;

    private final Context mContext;

    private List<TenderRequest> mTenderRequests = new ArrayList<>();

    private final String mTenderRequestString;

    private final String mDeadlineTextString;
    private final DateFormat mDateFormat;

    private TenderRequestClickListener mListener;

    private final int mExceedingTextLengthColor;
    private final String mEmptyMessageErrorText;
    private final String mMessageTooLongErrorText;
    private final String mMessageTheSameErrorText;

    private final String mConfirmationDialogMessage;

    private final String mUpdateString;

    public interface TenderRequestClickListener {
        void onSendReply(View view, String message, boolean isUpdate);
    }

    public TenderRequestsAdapter(List<TenderRequest> requests, Context context,
                                 TenderRequestClickListener listener) {
        if (requests != null) {
            mTenderRequests.clear();
            addItems(requests);
        }

        mContext = context;

        mListener = listener;

        mTenderRequestString = context.getString(R.string.tender_request_message);

        mDeadlineTextString = context.getString(R.string.deadline_with_date);
        mDateFormat = android.text.format.DateFormat.getDateFormat(context);

        mExceedingTextLengthColor = ContextCompat.getColor(context, R.color.colorSecondaryText);
        mEmptyMessageErrorText = context.getString(R.string.leave_message_empty_error);
        mMessageTooLongErrorText = context.getString(R.string.leave_message_too_long, MAX_MESSAGE_LENGTH);
        mMessageTheSameErrorText = context.getString(R.string.leave_message_not_different_error);

        mConfirmationDialogMessage = context.getString(R.string.reply_to_tender_request_dialog_message);

        mUpdateString = context.getString(R.string.update);
    }

    @Override
    public TenderRequestsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context parentContext = parent.getContext();

        View tenderRequestLayout = LayoutInflater.from(parentContext)
                .inflate(R.layout.provider_tender_request_layout, parent, false);

        ViewHolder holder = new ViewHolder(tenderRequestLayout);

        holder.messageEditText.addTextChangedListener(new MaxLengthTextWatcher(MAX_MESSAGE_LENGTH,
                holder.messageInputLayout, holder.messageEditText,
                mEmptyMessageErrorText,
                mMessageTooLongErrorText,
                mExceedingTextLengthColor));

        return holder;
    }

    @Override
    public void onBindViewHolder(final TenderRequestsAdapter.ViewHolder holder, final int position) {
        try {
            TenderRequest tenderRequest = mTenderRequests.get(position);

            // Set deadline text.
            int deadlineYear = tenderRequest.getDeadline(Calendar.YEAR);
            int deadlineMonth = tenderRequest.getDeadline(Calendar.MONTH);
            int deadlineDay = tenderRequest.getDeadline(Calendar.DAY_OF_MONTH);
            if (deadlineYear != 0) {
                holder.deadlineTextView.setVisibility(View.VISIBLE);
                holder.deadlineTextView.setText(String.format(
                        Locale.getDefault(),
                        mDeadlineTextString,
                        Utils.getFormattedDate(mDateFormat, deadlineYear, deadlineMonth, deadlineDay)));
            } else {
                holder.deadlineTextView.setVisibility(View.GONE);
            }

            // Set message text
            holder.messageTextView.setText(String.format(Locale.getDefault(), mTenderRequestString,
                    tenderRequest.getPrice(), tenderRequest.getVehicleData()));

            // Set location text
            holder.locationTextView.setText(tenderRequest.getLocation());

            holder.sendButton.setOnClickListener(new View.OnClickListener() {

                private String mPrevMessage = null;

                @Override
                public void onClick(final View v) {
                    if (mListener != null) {
                        final String replyMessage = holder.getReplyMessage();

                        // Make sure message is not empty
                        if (replyMessage.length() == 0) {
                            holder.messageInputLayout.setError(mEmptyMessageErrorText);
                            // Don't send the message.
                            return;
                        }

                        final boolean isFirstMessage = mPrevMessage == null;

                       if (!isFirstMessage && mPrevMessage.equals(replyMessage)) {
                           holder.messageInputLayout.setError(mMessageTheSameErrorText);
                           // Show error
                           return;
                       }
                        showMessageSendConfirmationDialog(replyMessage, new DialogInterface.OnClickListener() {
                                    // Positive button
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        boolean isUpdate = false;

                                        // Check if we had a previous message, if not then the user is send a new one.
                                        // Update the buttons to let the user know that the next time it will be an update.
                                        if (isFirstMessage) {
                                            // Update reply button and send button
                                            holder.sendButton.setText(mUpdateString);
                                            holder.replyButton.setText(mUpdateString);
                                        } else {
                                            isUpdate = true;
                                        }

                                        toggleReplyLayout(holder, false);

                                        mListener.onSendReply(v, replyMessage, isUpdate);

                                        mPrevMessage = replyMessage;
                                    }
                                }, null);

                                // TODO: Add check for message length.
                                // TODO: Add confirmation dialog.
                    }
                }
            });
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return mTenderRequests.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final TextView titleTextView;
        private final ImageButton removeRequestButton;
        private final TextView statusTextView;
        private final TextView deadlineTextView;
        private final TextView messageTextView;
        private final TextView locationTextView;

        private final View replyLayout;
        private final Button replyButton;
        private final View messageLayout;
        private final TextInputLayout messageInputLayout;
        private final EditText messageEditText;
        private final ImageButton collapseButton;
        private final Button sendButton;

        public ViewHolder(View layout) {
            super(layout);

            titleTextView = (TextView) layout.findViewById(R.id.request_title_text_view);
            // Set title to be more general
            titleTextView.setText(R.string.tender_request_title);
            removeRequestButton = (ImageButton) layout.findViewById(R.id.remove_tender_request_button);
            // Hide delete button.
            removeRequestButton.setVisibility(View.GONE);
            statusTextView = (TextView) layout.findViewById(R.id.request_status_text_view);
            // Hide status text - If status is anything but opened it's irrelevant to the provider.
            statusTextView.setVisibility(View.GONE);
            deadlineTextView = (TextView) layout.findViewById(R.id.request_deadline_text_view);
            messageTextView = (TextView) layout.findViewById(R.id.request_message_text_view);
            locationTextView = (TextView) layout.findViewById(R.id.request_location_text_view);

            replyLayout = layout.findViewById(R.id.reply_layout);
            replyButton = (Button) layout.findViewById(R.id.reply_button);
            replyButton.setOnClickListener(this);
            messageLayout = layout.findViewById(R.id.leave_message_layout);
            messageInputLayout = (TextInputLayout) layout.findViewById(R.id.leave_message_input_layout);
            messageEditText = (EditText) layout.findViewById(R.id.leave_message_edit_text);
            collapseButton = (ImageButton) layout.findViewById(R.id.collapse_image_button);
            collapseButton.setOnClickListener(this);
            sendButton = (Button) layout.findViewById(R.id.send_button);
        }

        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.reply_button:
                    toggleReplyLayout(this, true);
                    break;
                case R.id.collapse_image_button:
                    toggleReplyLayout(this, false);
                    break;
            }
        }

        public String getReplyMessage() {
            String reply = messageEditText.getText().toString();
            if (reply.length() > MAX_MESSAGE_LENGTH)
                return reply.substring(0, MAX_MESSAGE_LENGTH);

            return reply;
        }
    }

    public void addItem(TenderRequest tenderRequest) {
        addItem(tenderRequest, true);
    }

    public void addItems(TenderRequest... tenderRequests) {
        for (TenderRequest reply : tenderRequests) {
            addItem(reply, false);
        }

        notifyDataSetChanged();
    }

    public void addItems(Collection<TenderRequest> replyCollection) {
        for (TenderRequest reply : replyCollection) {
            addItem(reply, false);
        }

        notifyDataSetChanged();
    }

    private void addItem(TenderRequest tenderRequest, boolean notify) {
        if (!checkRequestOpened(tenderRequest))
            return;

        mTenderRequests.add(tenderRequest);

        if (notify) {
            notifyDataSetChanged();
        }
    }

    public void removeItem(int position) {
        mTenderRequests.remove(position);

        notifyDataSetChanged();
    }

    public void removeAllItems() {
        mTenderRequests.clear();
    }

    public TenderRequest getItem(int position) {
        if (position >= 0 && position < mTenderRequests.size()) {
            return mTenderRequests.get(position);
        }

        return null;
    }

    private boolean checkRequestOpened(TenderRequest request) {
        if (request != null && request.getStatus() == TenderRequest.Status.OPENED)
            return true;
        return false;
    }

    private static void toggleReplyLayout(ViewHolder holder, boolean toggle) {
        int EXPAND_COLLAPSE_DURATION = 350;
        if (toggle) {
            Utils.collapseView(holder.replyLayout, EXPAND_COLLAPSE_DURATION);
            Utils.expandView(holder.collapseButton, EXPAND_COLLAPSE_DURATION);
            Utils.expandView(holder.messageLayout, EXPAND_COLLAPSE_DURATION);
        } else {
            Utils.collapseView(holder.collapseButton, EXPAND_COLLAPSE_DURATION);
            Utils.collapseView(holder.messageLayout, EXPAND_COLLAPSE_DURATION);
            Utils.expandView(holder.replyLayout, EXPAND_COLLAPSE_DURATION);
        }
    }

    private void showMessageSendConfirmationDialog(String message,
                                                   DialogInterface.OnClickListener positiveButtonLister,
                                                   DialogInterface.OnClickListener negativeButtonListener) {
        new AlertDialog.Builder(mContext)
                .setTitle(R.string.reply_to_tender_request_dialog_title)
                .setMessage(String.format(Locale.getDefault(), mConfirmationDialogMessage, message))
                .setPositiveButton(R.string.send_title, positiveButtonLister)
                .setNegativeButton(R.string.button_cancel, negativeButtonListener).create().show();
    }
}
