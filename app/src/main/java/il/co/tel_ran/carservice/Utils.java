package il.co.tel_ran.carservice;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;

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

    public static Intent buildPlaceAutoCompleteIntent(Activity activity)
            throws GooglePlayServicesNotAvailableException, GooglePlayServicesRepairableException {
        // Use MODE_OVERLAY for a transparent background.
        // Filters will only allow cities in Israel to appear.
        return new PlaceAutocomplete.IntentBuilder(
                PlaceAutocomplete.MODE_OVERLAY)
                .setFilter(new AutocompleteFilter.Builder()
                        .setCountry("IL")
                        .setTypeFilter(AutocompleteFilter.TYPE_FILTER_CITIES)
                        .build())
                .build(activity);
    }

    public static int measureChildrenWidth(ViewGroup view) {
        int measuredChildrenWidth = 0;
        for (int i = 0, j = view.getChildCount(); i < j; i++) {
            measuredChildrenWidth += view.getChildAt(i).getMeasuredWidth();
        }

        return measuredChildrenWidth;
    }

    public static String parseCityNameFromAddress(CharSequence address) {
        boolean isRTL = Utils.isRTL((String) address);
        // Google Places API return human-readable address with types separated by commas.
        String[] addressParts = ((String) address).split(",");
        switch (addressParts.length) {
            case 2:
                // City Name, Country (Opposite for RTL)
                return addressParts[0].trim();
            case 3:
                // Street Name, City Name, Country (Opposite for RTL)
                // FALLTHROUGH
            case 4:
                // Street Name, City Name, Postcode, Country (Opposite for RTL)
                return addressParts[isRTL ? 2 : 1].trim();
        }

        return "";
    }

    public static boolean isRTL (String string) {
        if (string.isEmpty()) {
            return false;
        }
        char c = string.charAt(0);
        return c >= 0x590 && c <= 0x6ff;
    }
}
