package il.co.tel_ran.carservice;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.v7.widget.RecyclerView;
import android.view.View;

/**
 * Created by maxim on 20-Dec-16.
 */

public class DividerItemDecoration extends RecyclerView.ItemDecoration {

    private Drawable mDivider;

    private int[] mPadding = {0, 0, 0, 0};

    public DividerItemDecoration(Drawable divider, int[] padding) {
        mDivider = divider;

        mPadding = padding;
        for (int i = 0; i < padding.length; i++) {
            mPadding[i] = padding[i];
        }
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                               RecyclerView.State state) {
        super.getItemOffsets(outRect, view, parent, state);

        if (parent.getChildAdapterPosition(view) == 0) {
            return;
        }

        outRect.left = mPadding[0] + mDivider.getIntrinsicWidth();
        outRect.top = mPadding[1] + mDivider.getIntrinsicHeight();
        outRect.right = mPadding[2] + mDivider.getIntrinsicWidth();
        outRect.bottom = mPadding[3] + mDivider.getIntrinsicHeight();
    }

    @Override
    public void onDrawOver(Canvas canvas, RecyclerView parent, RecyclerView.State state) {
        int dividerLeft = parent.getPaddingLeft();
        int dividerRight = parent.getWidth() - parent.getPaddingRight();

        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount - 1; i++) {
            View child = parent.getChildAt(i);

            RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

            int dividerTop = child.getBottom() + params.bottomMargin;
            int dividerBottom = dividerTop + mDivider.getIntrinsicHeight();

            mDivider.setBounds(dividerLeft, dividerTop,
                    dividerRight, dividerBottom);
            mDivider.draw(canvas);
        }
    }
}
