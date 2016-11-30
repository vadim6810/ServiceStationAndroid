package il.co.tel_ran.carservice.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.Locale;

import il.co.tel_ran.carservice.InboxMessage;
import il.co.tel_ran.carservice.R;
import il.co.tel_ran.carservice.Utils;

/**
 * Created by Max on 29/11/2016.
 */

public class InboxMessagesAdapter extends RecyclerView.Adapter<InboxMessagesAdapter.ViewHolder> implements View.OnClickListener {

    public interface InboxMessageClickListener {
        void onClickMessage(View v);
    }

    private ArrayList<InboxMessage> mMessages = new ArrayList<>();

    private final String mMessageTimePrefixYesterday;

    private InboxMessageClickListener mListener;

    public InboxMessagesAdapter(Collection<InboxMessage> messages, Context context,
                                InboxMessageClickListener listener) {
        if (messages != null) {
            mMessages.clear();
            mMessages.addAll(messages);
        }

        mMessageTimePrefixYesterday = context.getString(R.string.message_time_format_prefix_yesterday) + ' ';

        mListener = listener;
    }

    @Override
    public InboxMessagesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context parentContext = parent.getContext();

        View layout = LayoutInflater.from(parentContext)
                .inflate(R.layout.inbox_message_layout, parent, false);

        layout.setOnClickListener(this);

        return new ViewHolder(layout);
    }

    @Override
    public void onBindViewHolder(InboxMessagesAdapter.ViewHolder holder, int position) {
        InboxMessage inboxMessage = mMessages.get(position);

        if (inboxMessage != null) {
            String message = inboxMessage.getTitle();

            if (message != null) {
                holder.messageTextView.setText(message);
            }

            Date submitDate = new Date(inboxMessage.getTimestamp());
            Calendar now = Calendar.getInstance();
            Calendar submitTime = Calendar.getInstance();
            submitTime.setTime(submitDate);

            String dateString = "";
            SimpleDateFormat dateFormat;
            if (now.get(Calendar.YEAR) != submitTime.get(Calendar.YEAR)) {
                dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
            } else {
                long daysDifference = Utils.getDaysDifference(now, submitTime);
                if (daysDifference == 0) {
                    // Format as hour
                    dateFormat = new SimpleDateFormat("kk:mm", Locale.getDefault());
                } else if (daysDifference == 1) {
                    // Format as yesterday at specific time
                    dateString = mMessageTimePrefixYesterday;
                    dateFormat = new SimpleDateFormat("kk:mm", Locale.getDefault());
                } else if (daysDifference < 7) {
                    // Format as day in week
                    dateFormat = new SimpleDateFormat("E, kk:mm", Locale.getDefault());
                } else {
                    // Format as date
                    dateFormat = new SimpleDateFormat("MMM dd", Locale.getDefault());
                }
            }

            dateString = dateString + dateFormat.format(submitDate);
            holder.submitTimeTextView.setText(dateString);
        }

    }

    @Override
    public int getItemCount() {
        return mMessages.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView messageTextView;
        private final TextView submitTimeTextView;

        public ViewHolder(View itemView) {
            super(itemView);

            messageTextView = (TextView) itemView.findViewById(R.id.message_text_view);
            submitTimeTextView = (TextView) itemView.findViewById(R.id.message_submit_date_text_view);
        }
    }

    /*
     * View.OnClickListener
     */

    @Override
    public void onClick(View v) {
        // Respond to message click
        if (mListener != null) {
            mListener.onClickMessage(v);
        }
    }

    public InboxMessage getItem(int itemPos) {
        return mMessages.get(itemPos);
    }
}
