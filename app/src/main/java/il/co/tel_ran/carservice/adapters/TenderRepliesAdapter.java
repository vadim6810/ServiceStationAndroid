package il.co.tel_ran.carservice.adapters;

import android.content.Context;
import android.graphics.PorterDuff;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatRatingBar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageButton;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import il.co.tel_ran.carservice.R;
import il.co.tel_ran.carservice.ServiceStation;
import il.co.tel_ran.carservice.TenderReply;
import il.co.tel_ran.carservice.Utils;

/**
 * Created by maxim on 12-Nov-16.
 */

public class TenderRepliesAdapter
        extends RecyclerView.Adapter<TenderRepliesAdapter.ViewHolder> implements View.OnClickListener {

    private final int mDeleteButtonColor;

    private List<TenderReply> mTenderReplies = new ArrayList<>();
    private final String mTenderReplyTitle;

    private TenderReplyClickListener mListener;

    public interface TenderReplyClickListener {
        void onClickTenderReply(View view);
        void onDeleteTenderReply(View view);
    }

    public TenderRepliesAdapter(List<TenderReply> replies, Context context,
                                TenderReplyClickListener listener) {
        if (replies != null) {
            mTenderReplies = replies;
        }

        mTenderReplyTitle = context.getString(R.string.tender_reply_title);
        mDeleteButtonColor = ContextCompat.getColor(context, android.R.color.white);

        mListener = listener;
    }

    @Override
    public void onClick(View v) {
        View clickedParent = null;
        ViewParent parent = v.getParent();
        if (parent != null) {
            try {
                clickedParent = (View) parent;
            } catch (ClassCastException e) {
                e.printStackTrace();
            }
        }
        switch (v.getId()) {
            case R.id.tender_reply_details_layout:
                if (clickedParent != null)
                    mListener.onClickTenderReply(clickedParent);
                break;
            case R.id.remove_tender_reply_button:
                if (clickedParent != null)
                    mListener.onDeleteTenderReply(clickedParent);
                break;
        }
    }

    @Override
    public TenderRepliesAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context parentContext = parent.getContext();

        View tenderReplyLayout = LayoutInflater.from(parentContext)
                .inflate(R.layout.tender_reply_layout, parent, false);

        ViewHolder viewHolder = new ViewHolder(tenderReplyLayout);
        viewHolder.deleteReplyButton.setOnClickListener(this);

        View replyDetailsLayout = tenderReplyLayout.findViewById(R.id.tender_reply_details_layout);
        replyDetailsLayout.setOnClickListener(this);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(TenderRepliesAdapter.ViewHolder holder, int position) {
        TenderReply reply = mTenderReplies.get(position);

        if (reply != null) {
            ServiceStation replyingService = reply.getReplyingService();
            if (replyingService != null) {
                holder.replyTitleTextView.setText(String.format(Locale.getDefault(),
                        mTenderReplyTitle, replyingService.getName()));
                holder.deleteReplyButton.setColorFilter(mDeleteButtonColor, PorterDuff.Mode.SRC_ATOP);

                holder.messageTextView.setText(reply.getReplyMessage());
                holder.ratingBar.setRating(replyingService.getAvgRating());
                holder.ratingCountTextView.setText('(' + String.valueOf(replyingService.getSubmittedRatings()) + ')');
                holder.locationTextView.setText(replyingService.getLocation().getAddress());
            }
        }
    }

    @Override
    public int getItemCount() {
        return mTenderReplies.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView replyTitleTextView;
        private final ImageButton deleteReplyButton;

        private final AppCompatRatingBar ratingBar;
        private final TextView ratingCountTextView;
        private final TextView messageTextView;
        private final TextView locationTextView;

        public ViewHolder(View layout) {
            super(layout);

            replyTitleTextView = (TextView) layout.findViewById(R.id.reply_title_text_view);
            deleteReplyButton = (ImageButton) layout.findViewById(R.id.remove_tender_reply_button);

            ratingBar = (AppCompatRatingBar) layout.findViewById(R.id.service_rating_bar);
            ratingCountTextView = (TextView) layout.findViewById(R.id.rating_submit_count);
            messageTextView = (TextView) layout.findViewById(R.id.reply_message_text_view);
            locationTextView = (TextView) layout.findViewById(R.id.reply_location_text_view);
        }
    }

    public void addItem(TenderReply tenderReply) {
        addItem(tenderReply, true);
    }

    public void addItems(TenderReply... tenderReplies) {
        for (TenderReply reply : tenderReplies) {
            addItem(reply, false);
        }

        notifyDataSetChanged();
    }

    public void addItems(Collection<TenderReply> replyCollection) {
        for (TenderReply reply : replyCollection) {
            addItem(reply, false);
        }

        notifyDataSetChanged();
    }

    private void addItem(TenderReply tenderReply, boolean notify) {
        if (tenderReply != null) {
            mTenderReplies.add(tenderReply);

            if (notify) {
                notifyDataSetChanged();
            }
        }
    }

    public void removeItem(int position) {
        mTenderReplies.remove(position);

        notifyDataSetChanged();
    }

    public void removeAllItems() {
        mTenderReplies.clear();
    }

    public TenderReply getItem(int position) {
        if (position >= 0 && position < mTenderReplies.size()) {
            return mTenderReplies.get(position);
        }

        return null;
    }
}
