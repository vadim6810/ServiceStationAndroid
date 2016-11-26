package il.co.tel_ran.carservice.fragments;

import android.support.v4.app.Fragment;

/**
 * Created by maxim on 26-Nov-16.
 */

public class RefreshingFragment extends Fragment {

    private RefreshingFragmentListener mRefreshListener;

    private boolean mIsRefreshing;

    public interface RefreshingFragmentListener {
        public void onRefreshEnd();
    }

    public void onRefreshStart() {
        mIsRefreshing = true;
    }

    public void onRefreshEnd() {
        if (mIsRefreshing && mRefreshListener != null) {
            mRefreshListener.onRefreshEnd();
        }

        mIsRefreshing = false;
    }

    public void setOnRefreshEndListener(RefreshingFragmentListener listener) {
        mRefreshListener = listener;
    }
}
