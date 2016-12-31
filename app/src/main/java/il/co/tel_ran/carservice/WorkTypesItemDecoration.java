package il.co.tel_ran.carservice;

import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import il.co.tel_ran.carservice.adapters.WorkTypesAdapter;

/**
 * Created by maxim on 20-Dec-16.
 */

public class WorkTypesItemDecoration extends RecyclerView.ItemDecoration {
    private int[] mMargins = {0, 0, 0, 0};

    public WorkTypesItemDecoration(int[] margins) {
        mMargins = margins;
        for (int i = 0; i < margins.length; i++) {
            mMargins[i] = margins[i];
        }
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                               RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);

        if (parent.getChildAdapterPosition(view) == 0) {
            return;
        }

        int position = parent.getChildAdapterPosition(view);
        WorkTypesAdapter adapter = (WorkTypesAdapter) parent.getAdapter();
        int viewType = adapter.getItemViewType(position);
        switch (viewType) {
            case WorkTypesAdapter.VIEW_TYPE_ITEM_MULTI_CHOICE:
                // FALLTHROUGH
            case WorkTypesAdapter.VIEW_TYPE_ITEM_SINGLE_CHOICE:
                outRect.left = mMargins[0];
                outRect.top = mMargins[1];
                outRect.right = mMargins[2];
                outRect.bottom = mMargins[3];
               break;
        }
    }
}
