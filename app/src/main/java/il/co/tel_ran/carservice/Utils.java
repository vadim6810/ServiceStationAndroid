package il.co.tel_ran.carservice;

import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;

/**
 * Created by Max on 17/09/2016.
 */
public class Utils {

    public static void expandView(final View view) {
        // Use 1dp/ms
        expandView(view, (int)(view.getMeasuredHeight() /
                view.getContext().getResources().getDisplayMetrics().density));
    }
    public static void expandView(final View view, int duration) {
        view.measure(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        final int finalViewHeight = view.getMeasuredHeight();

        // Older versions of android (pre API 21) cancel animations for views with a height of 0.
        view.getLayoutParams().height = (Build.VERSION.SDK_INT < 21) ?  1 : 0;
        view.setVisibility(View.VISIBLE);

        Animation expandAnimation = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                view.getLayoutParams().height = interpolatedTime == 1
                        ? ViewGroup.LayoutParams.WRAP_CONTENT
                        : (int)(finalViewHeight * interpolatedTime);
                view.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        expandAnimation.setDuration(duration);
        view.startAnimation(expandAnimation);
    }

    public static void collapseView(final View view) {
        // Use 1dp/ms
        collapseView(view, (int)(view.getMeasuredHeight() /
                view.getContext().getResources().getDisplayMetrics().density));
    }

    public static void collapseView(final View view, int duration) {
        final int initialHeight = view.getMeasuredHeight();

        Animation collapseAnimation = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1){
                    view.setVisibility(View.GONE);
                } else {
                    view.getLayoutParams().height = initialHeight - (int)(initialHeight * interpolatedTime);
                    view.requestLayout();
                }
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        collapseAnimation.setDuration(duration);
        view.startAnimation(collapseAnimation);
    }
}
