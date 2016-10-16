package il.co.tel_ran.carservice;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.widget.RatingBar;

/**
 * Created by Max on 16/10/2016.
 */

public class ServiceSubmitRatingDialog extends DialogFragment implements
        RatingBar.OnRatingBarChangeListener, DialogInterface.OnClickListener {

    private static SubmitRatingDialogListener mListener;

    private static ServiceSearchResult mSearchResult;

    private float mRating;

    public interface SubmitRatingDialogListener {
        void onRatingSubmitted(float rating, ServiceSearchResult searchResult);
    }

    public static ServiceSubmitRatingDialog getInstance(float rating, SubmitRatingDialogListener
            listener, ServiceSearchResult result) {
        ServiceSubmitRatingDialog submitRatingDialog = new ServiceSubmitRatingDialog();

        Bundle args = new Bundle();
        args.putFloat("rating", rating);
        submitRatingDialog.setArguments(args);

        mListener = listener;
        mSearchResult = result;

        return submitRatingDialog;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        setStyle(DialogFragment.STYLE_NORMAL, R.style.TitledDialogFragment);

        Bundle args = getArguments();
        if (args != null && !args.isEmpty()) {
            mRating = args.getFloat("rating");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.submit_rating_dialog_title))
                .setView(R.layout.service_submit_rating_layout)
                .setPositiveButton(R.string.button_submit, this)
                .setNeutralButton(R.string.button_cancel, this);

        return builder.create();
    }

    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance()) {
            getDialog().setDismissMessage(null);
        }
        super.onDestroyView();
    }

    @Override
    public void onRatingChanged(RatingBar ratingBar, float rating, boolean fromUser) {
        mRating = rating;
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            case DialogInterface.BUTTON_POSITIVE:
                mListener.onRatingSubmitted(mRating, mSearchResult);
                break;
        }
    }
}
