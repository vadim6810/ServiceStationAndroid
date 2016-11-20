package il.co.tel_ran.carservice;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Transformation;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;

/**
 * Created by Max on 17/09/2016.
 */
public class Utils {

    public static final AutocompleteFilter PLACE_FILTER_CITY = new AutocompleteFilter.Builder()
            .setCountry("IL")
            .setTypeFilter(AutocompleteFilter.TYPE_FILTER_CITIES)
            .build();

    public static final AutocompleteFilter PLACE_FILTER_ADDRESS = new AutocompleteFilter.Builder()
            .setCountry("IL")
            .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS
                    | AutocompleteFilter.TYPE_FILTER_CITIES)
            .build();

    public enum Padding {
        LEFT,
        RIGHT,
        TOP,
        BOTTOM
    }

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

    public static Intent buildPlaceAutoCompleteIntent(Activity activity, AutocompleteFilter filter)
            throws GooglePlayServicesNotAvailableException, GooglePlayServicesRepairableException {
        // Use MODE_OVERLAY for a transparent background.
        // Filters will only allow cities in Israel to appear.
        return new PlaceAutocomplete.IntentBuilder(
                PlaceAutocomplete.MODE_OVERLAY)
                .setFilter(filter)
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

    public static boolean isLocaleRTL(Locale locale) {
        // Check if a specific locate is RTL.
        final int directionality = Character.getDirectionality(locale.getDisplayName().charAt(0));
        return directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT ||
                directionality == Character.DIRECTIONALITY_RIGHT_TO_LEFT_ARABIC;
    }

    public static void setSpecificPadding(View view, Padding padding, int amount) {
        int left = view.getPaddingLeft();
        int right = view.getPaddingRight();
        int top = view.getPaddingTop();
        int bottom = view.getPaddingBottom();

        switch (padding) {
            case LEFT:
                view.setPadding(amount, top, right, bottom);
                break;
            case TOP:
                view.setPadding(left, amount, right, bottom);
                break;
            case RIGHT:
                view.setPadding(left, top, amount, bottom);
                break;
            case BOTTOM:
                view.setPadding(left, top, right, amount);
                break;
        }
    }

    public static int getThemeAccentColor(Context context) {
        int colorAttr;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            colorAttr = android.R.attr.colorAccent;
        } else {
            // Get colorAccent defined for AppCompat
            colorAttr = context.getResources().getIdentifier("colorAccent", "attr", context.getPackageName());
        }
        TypedValue outValue = new TypedValue();
        context.getTheme().resolveAttribute(colorAttr, outValue, true);
        return outValue.data;
    }

    public static void showDialogFragment(FragmentManager fragmentManager,
                                          DialogFragment dialogFragment, String tag) {
        if (fragmentManager != null && dialogFragment != null) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            Fragment previousDialog = fragmentManager.findFragmentByTag(tag);
            if (previousDialog != null) {
                fragmentTransaction.remove(previousDialog);
            }
            fragmentTransaction.addToBackStack(null);
            dialogFragment.show(fragmentTransaction, tag);
        }
    }

    public static List<String> parseServiceTypes(Context context, EnumSet<ServiceType> serviceTypes) {
        List<String> serviceTypeStrings = new ArrayList<>();
        for (ServiceType serviceType : serviceTypes) {
            switch (serviceType) {
                case SERVICE_TYPE_CAR_WASH:
                    serviceTypeStrings.add(context.getString(R.string.required_service_car_wash));
                    break;
                case SERVICE_TYPE_TUNING:
                    serviceTypeStrings.add(context.getString(R.string.required_service_tuning));
                    break;
                case SERVICE_TYPE_TYRE_REPAIR:
                    serviceTypeStrings.add(context.getString(R.string.required_service_tyre_repair));
                    break;
                case SERVICE_TYPE_AC_REPAIR_REFILL:
                    serviceTypeStrings.add(context.getString(R.string.required_service_air_cond_refill));
                    break;
            }
        }

        return serviceTypeStrings;
    }

    public static String toSentenceCase(String string) {
        String lower = string.toLowerCase();
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }

    public static String getFormattedDate(Context context, int year, int month, int day) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(year, month, day);
        DateFormat dateFormat = android.text.format.DateFormat
                .getDateFormat(context);

        return dateFormat.format(calendar.getTime());
    }
}
