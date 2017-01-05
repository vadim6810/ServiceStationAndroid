package il.co.tel_ran.carservice.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
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
import java.util.Date;
import java.util.List;
import java.util.Locale;

import il.co.tel_ran.carservice.MaxLengthTextWatcher;
import il.co.tel_ran.carservice.R;
import il.co.tel_ran.carservice.ServiceSubWorkType;
import il.co.tel_ran.carservice.TenderRequest;
import il.co.tel_ran.carservice.UserType;
import il.co.tel_ran.carservice.Utils;
import il.co.tel_ran.carservice.VehicleData;

/**
 * Created by maxim on 12-Nov-16.
 */

public class TenderRequestsAdapter
        extends RecyclerView.Adapter<TenderRequestsAdapter.ViewHolder>
        implements View.OnClickListener {

    private static final int VIEW_TYPE_MASTER = 1;
    private static final int VIEW_TYPE_CLIENT = 2;

    private static final int MAX_MESSAGE_LENGTH = 50;

    private final Context mContext;

    private final String mRequiredWorkTypesString;

    private final String mTenderSenderString;

    private final String mDeadlineTextString;
    private final DateFormat mDateFormat;

    private List<TenderRequest> mTenderRequests = new ArrayList<>();

    private TenderRequestClickListener mListener;

    private final int mExceedingTextLengthColor;
    private final String mEmptyMessageErrorText;
    private final String mMessageTooLongErrorText;
    private final String mMessageTheSameErrorText;

    private final String mConfirmationDialogMessage;

    private final String mUpdateString;

    private final int mViewType;

    @Override
    public void onClick(View v) {
        if (mListener != null) {
            mListener.onClickRequest(v);
        }
    }

    public interface TenderRequestClickListener {
        void onClickRequest(View v);
        void onSendReply(View view, String message, boolean isUpdate);
    }

    public TenderRequestsAdapter(List<TenderRequest> requests, Context context,
                                 TenderRequestClickListener listener) {
        this(requests, context, listener, UserType.MASTER);
    }

    public TenderRequestsAdapter(List<TenderRequest> requests, Context context,
                                 TenderRequestClickListener listener, UserType userType) {
        if (requests != null) {
            mTenderRequests.clear();
            addItems(requests);
        }

        mContext = context;

        mListener = listener;

        mRequiredWorkTypesString = context.getString(R.string.tender_required_work_Types);

        mTenderSenderString = context.getString(R.string.tender_sender);

        mDeadlineTextString = context.getString(R.string.tender_deadline);
        mDateFormat = android.text.format.DateFormat.getDateFormat(context);

        mExceedingTextLengthColor = ContextCompat.getColor(context, R.color.colorSecondaryText);
        mEmptyMessageErrorText = context.getString(R.string.leave_message_empty_error);
        mMessageTooLongErrorText = context.getString(R.string.leave_message_too_long, MAX_MESSAGE_LENGTH);
        mMessageTheSameErrorText = context.getString(R.string.leave_message_not_different_error);

        mConfirmationDialogMessage = context.getString(R.string.reply_to_tender_request_dialog_message);

        mUpdateString = context.getString(R.string.update);

        switch (userType) {
            case NONE:
                // FALLTHROUGH
            default:
                // FALLTHROUGH
            case CLIENT:
                mViewType = VIEW_TYPE_CLIENT;
                break;
            case MASTER:
                mViewType = VIEW_TYPE_MASTER;
                break;
        }
    }

    @Override
    public TenderRequestsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context parentContext = parent.getContext();

        ViewHolder holder;

        View layout;
        switch (viewType) {
            case VIEW_TYPE_MASTER:
                layout = LayoutInflater.from(parentContext)
                        .inflate(R.layout.provider_tender_request_layout, parent, false);

                holder = new ViewHolder(layout, viewType);

                holder.messageEditText.addTextChangedListener(new MaxLengthTextWatcher(
                        MAX_MESSAGE_LENGTH,
                        holder.messageInputLayout, holder.messageEditText,
                        mEmptyMessageErrorText,
                        mMessageTooLongErrorText,
                        mExceedingTextLengthColor));

                break;
            case VIEW_TYPE_CLIENT:
                // FALLTHROUGH:
                default:
                    layout = LayoutInflater.from(parentContext)
                        .inflate(R.layout.client_tender_request_layout, parent, false);

                holder = new ViewHolder(layout, viewType);
                break;
        }

        layout.findViewById(R.id.request_details_layout).setOnClickListener(this);

        return holder;
    }

    @Override
    public void onBindViewHolder(final TenderRequestsAdapter.ViewHolder holder, final int position) {
        try {
            TenderRequest tenderRequest = mTenderRequests.get(position);

            VehicleData vehicleData = tenderRequest.getVehicleData();
            if (vehicleData != null) {
                holder.titleTextView.setText(vehicleData.toString());
            }

            float price = tenderRequest.getPrice();
            holder.priceTextView.setText(String.format(Locale.getDefault(), "%.2f₪", price));

            String workTypes = getSubWorkTypesString(tenderRequest.getSubWorkTypes());
            holder.requiredWorkTypesTextView.setText(String.format(Locale.getDefault(),
                    mRequiredWorkTypesString, workTypes));

            // Set deadline text.
            Date deadlineDate = tenderRequest.getDeadlineDate();
            if (deadlineDate != null) {
                String formattedDate = mDateFormat.format(deadlineDate);

                holder.deadlineTextView.setVisibility(View.VISIBLE);
                holder.deadlineTextView.setText(String.format(Locale.getDefault(),
                        mDeadlineTextString, formattedDate));
            } else {
                holder.deadlineTextView.setVisibility(View.GONE);
            }

            String message = tenderRequest.getMessage();
            if (message != null) {
                // Set message text
                holder.messageTextView.setText(message);
            } else {
                holder.messageTextView.setVisibility(View.GONE);
            }

            // Set location text
            holder.locationTextView.setText(tenderRequest.getLocation());

            String sender = tenderRequest.getSender();
            if (sender != null) {
                holder.senderTextView.setText(String.format(Locale.getDefault(),
                        mTenderSenderString, sender));
            } else {
                holder.senderTextView.setVisibility(View.GONE);
            }

            if (mViewType == VIEW_TYPE_MASTER) {
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
            }
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return mTenderRequests.size();
    }

    @Override
    public int getItemViewType(int position) {
        return mViewType;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        private final TextView titleTextView;
        private final ImageButton removeRequestButton;
        private final TextView priceTextView;

        private final TextView requiredWorkTypesTextView;
        private final TextView deadlineTextView;
        private final TextView messageTextView;
        private final TextView locationTextView;
        private final TextView senderTextView;

        private View replyLayout;
        private Button replyButton;
        private View messageLayout;
        private TextInputLayout messageInputLayout;
        private EditText messageEditText;
        private ImageButton collapseButton;
        private Button sendButton;

        public ViewHolder(View layout, int viewType) {
            super(layout);

            titleTextView = (TextView) layout.findViewById(R.id.request_title_text_view);
            // Set title to be more general
            titleTextView.setText(R.string.tender_request_title);
            removeRequestButton = (ImageButton) layout.findViewById(
                    R.id.remove_tender_request_button);
            // Hide delete button.
            removeRequestButton.setVisibility(View.GONE);

            priceTextView = (TextView) layout.findViewById(R.id.request_price_text_view);

            requiredWorkTypesTextView = (TextView) layout.findViewById(
                    R.id.request_work_types_text_view);
            deadlineTextView = (TextView) layout.findViewById(R.id.request_deadline_text_view);
            messageTextView = (TextView) layout.findViewById(R.id.request_message_text_view);
            locationTextView = (TextView) layout.findViewById(R.id.request_location_text_view);
            senderTextView = (TextView) layout.findViewById(R.id.request_sender_text_view);

            if (viewType == VIEW_TYPE_MASTER) {
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
        /*if (!checkRequestOpened(tenderRequest))
            return;*/

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

    private String getSubWorkTypesString(ArrayList<ServiceSubWorkType> subWorkTypes) {
        String subWorkTypesString = "";
        if (subWorkTypes != null) {
            for (ServiceSubWorkType subWorkType : subWorkTypes) {
                subWorkTypesString += subWorkType.toString() + ", ";
            }
        }

        subWorkTypesString = subWorkTypesString.substring(0, subWorkTypesString.length() - 2);

        return subWorkTypesString;
    }
}
