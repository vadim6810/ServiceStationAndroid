package il.co.tel_ran.carservice.adapters;

import android.content.Context;
import android.support.v7.widget.AppCompatRatingBar;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import il.co.tel_ran.carservice.R;
import il.co.tel_ran.carservice.ServiceReview;

/**
 * Created by maxim on 20-Dec-16.
 */

public class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ViewHolder> implements View.OnClickListener {

    private final String mReviewTitle;

    private final OnReviewClickListener mListener;

    private List<ServiceReview> mReviews = new ArrayList<>();

    public interface OnReviewClickListener {
        void onClickReview(View view);
    }

    public ReviewsAdapter(List<ServiceReview> reviews, Context context,
                          OnReviewClickListener listener) {
        mReviews = reviews;

        mReviewTitle = context.getString(R.string.review_title);

        mListener = listener;
    }

    @Override
    public ReviewsAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context parentContext = parent.getContext();

        View reviewLayout = LayoutInflater.from(parentContext)
                .inflate(R.layout.review_search_result_layout, parent, false);

        reviewLayout.setOnClickListener(this);

        return new ViewHolder(reviewLayout);
    }

    @Override
    public void onBindViewHolder(ReviewsAdapter.ViewHolder holder, int position) {
        ServiceReview review = mReviews.get(position);

        // Set title
        holder.titleTextView.setText(String.format(Locale.getDefault(), mReviewTitle,
                review.getReviewerName(), review.getServiceName()));

        // Set rating
        holder.ratingBar.setRating(review.getRating());
        // Set rating date
        Date updatedDate = review.getUpdatedDate();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        holder.dateTextView.setText(dateFormat.format(updatedDate.getTime()));

        // Set comment
        holder.commentsTextView.setText(review.getComment());
    }

    @Override
    public int getItemCount() {
        return mReviews.size();
    }

    /*
     * View.OnClickListener
     */

    @Override
    public void onClick(View v) {
        if (mListener != null) {
            mListener.onClickReview(v);
        }
    }

    public void removeAllItems() {
        mReviews.clear();
    }


    public void addItems(ServiceReview[] reviews) {
        for (ServiceReview review : reviews) {
            mReviews.add(review);
        }
    }

    public void addItems(ArrayList<ServiceReview> filteredReviews) {
        for (ServiceReview review : filteredReviews) {
            mReviews.add(review);
        }
    }

    public ServiceReview getItem(int itemPos) {
        if (itemPos < 0 || itemPos > mReviews.size()) {
            return null;
        }

        return mReviews.get(itemPos);
    }

    public ServiceReview[] getItems() {
        ServiceReview[] reviews = new ServiceReview[mReviews.size()];
        mReviews.toArray(reviews);
        return reviews;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView titleTextView;
        private final TextView dateTextView;
        private final TextView commentsTextView;
        private final AppCompatRatingBar ratingBar;

        public ViewHolder(View layout) {
            super(layout);

            titleTextView    = (TextView) layout.findViewById(R.id.review_title_text_view);
            dateTextView     = (TextView) layout.findViewById(R.id.review_date_text_view);
            commentsTextView = (TextView) layout.findViewById(R.id.review_comment_text_view);
            ratingBar        = (AppCompatRatingBar) layout.findViewById(R.id.rating_bar);
        }
    }
}
