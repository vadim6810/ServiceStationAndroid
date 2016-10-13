package il.co.tel_ran.carservice;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by Max on 12/10/2016.
 */

public class RegistrationUserTypeFragment extends Fragment implements View.OnClickListener {

    private UserType mUserType = UserType.NONE;

    private View mClientOptionLayout;
    private View mServiceProviderOptionLayout;

    private TextView mClientOptionTitleTextView;
    private TextView mClientOptionCaptionTextView;
    private TextView mProviderOptionTitleTextView;
    private TextView mProviderOptionCaptionTextView;

    private FloatingActionButton mNextStepFAB;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup layout = (ViewGroup) inflater.inflate(
                R.layout.fragment_registration_step_usertype, container, false);

        mClientOptionLayout = layout.findViewById(R.id.client_option_layout);
        mClientOptionLayout.setOnClickListener(this);
        mClientOptionTitleTextView = (TextView) layout.findViewById(R.id.client_option_title_text_view);
        mClientOptionCaptionTextView = (TextView) layout.findViewById(R.id.client_option_caption_text);

        mServiceProviderOptionLayout = layout.findViewById(R.id.service_provider_option_layout);
        mServiceProviderOptionLayout.setOnClickListener(this);
        mProviderOptionTitleTextView = (TextView) layout.findViewById(R.id.provider_option_title_text_view);
        mProviderOptionCaptionTextView = (TextView) layout.findViewById(R.id.provider_option_caption_text);

        mNextStepFAB = (FloatingActionButton) layout.findViewById(R.id.next_step_fab);
        mNextStepFAB.setOnClickListener(this);

        // Mirrored icon for RTL languages (autoMirrored attribute doesn't work even for SDK 21)
        if (SignUpActivity.isRTL) {
            mNextStepFAB.setImageResource(R.drawable.ic_arrow_back_white_24dp);
        }

        return layout;
    }

    @Override
    public void onClick(final View v) {
        switch (v.getId()) {
            case R.id.client_option_layout:
                // Animate background color and text color.
                animateRegisterOptionsLayouts(mUserType, UserType.USER_CLIENT,
                        android.R.color.background_light,
                        Utils.getThemeAccentColor(getActivity()), 300);
                mUserType = UserType.USER_CLIENT;

                // Show the FAB if it's hidden.
                // We keep it hidden to encourage the user to select a type, otherwise don't let the user proceed.
                if (mNextStepFAB.getVisibility() != View.VISIBLE) {
                    mNextStepFAB.show();
                }
                break;
            case R.id.service_provider_option_layout:
                // Animate background color and text color.
                animateRegisterOptionsLayouts(mUserType, UserType.USER_SERVICE_PROVIDER,
                        android.R.color.background_light,
                        Utils.getThemeAccentColor(getActivity()), 300);
                mUserType = UserType.USER_SERVICE_PROVIDER;

                // Show the FAB if it's hidden.
                // We keep it hidden to encourage the user to select a type, otherwise don't let the user proceed.
                if (mNextStepFAB.getVisibility() != View.VISIBLE) {
                    mNextStepFAB.show();
                }

                break;
            case R.id.next_step_fab:
                try {
                    SignUpActivity containerActivity = (SignUpActivity) getActivity();
                    // Advance to login details page.
                    containerActivity.requestViewPagerPage(SignUpActivity.PAGE_LOGIN_DETAILS);
                } catch (ClassCastException e) {
                    e.printStackTrace();
                }
                break;
        }
    }

    private ValueAnimator animateRegisterOptionsLayouts(final UserType oldType,
                                                        final UserType newType,
                                                        final int startColor, final int endColor,
                                                        int duration) {
        // Do nothing if the user hasn't changed their type.
        if (oldType == newType)
            return null;

        final Drawable clientBackground = mClientOptionLayout.getBackground();
        final Drawable providerBackground = mServiceProviderOptionLayout.getBackground();

        final ArgbEvaluator colorEvaluator = new ArgbEvaluator();
        final int unselectedTextColor = ContextCompat.getColor(getActivity(), R.color.colorPrimaryText);
        final int selectedTextColor = ContextCompat.getColor(getActivity(), android.R.color.white);

        ValueAnimator animator = new ValueAnimator();
        animator.setIntValues(startColor, endColor);
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                float fraction = valueAnimator.getAnimatedFraction();
                switch (newType) {
                    case USER_CLIENT:
                        // Apply color to Client registration option
                        clientBackground.setColorFilter((Integer)colorEvaluator.evaluate(
                                fraction, startColor, endColor), PorterDuff.Mode.SRC);

                        // Apply new text colors
                        mClientOptionTitleTextView.setTextColor((Integer)colorEvaluator.evaluate(
                                fraction, unselectedTextColor, selectedTextColor));
                        mClientOptionCaptionTextView.setTextColor((Integer)colorEvaluator.evaluate(
                                fraction, unselectedTextColor, selectedTextColor));

                        if (oldType != UserType.NONE) {
                            // Remove color from Provider registration option
                            providerBackground.setColorFilter((Integer) colorEvaluator.evaluate(
                                    fraction, endColor, startColor),
                                    PorterDuff.Mode.SRC);
                            // Retrieve original text color
                            mProviderOptionTitleTextView.setTextColor((Integer)colorEvaluator.evaluate(
                                    fraction, selectedTextColor, unselectedTextColor));
                            mProviderOptionCaptionTextView.setTextColor((Integer)colorEvaluator.evaluate(
                                    fraction, selectedTextColor, unselectedTextColor));
                        }

                        // Clear color filters to retrieve original background
                        if (fraction == 1.0f) {
                            providerBackground.clearColorFilter();
                        }
                        break;
                    case USER_SERVICE_PROVIDER:
                        // Apply color to Client registration option
                        providerBackground.setColorFilter((Integer)colorEvaluator.evaluate(
                                fraction, startColor, endColor), PorterDuff.Mode.SRC);

                        // Change text colors
                        mProviderOptionTitleTextView.setTextColor((Integer)colorEvaluator.evaluate(
                                fraction, unselectedTextColor, selectedTextColor));
                        mProviderOptionCaptionTextView.setTextColor((Integer)colorEvaluator.evaluate(
                                fraction, unselectedTextColor, selectedTextColor));

                        if (oldType != UserType.NONE) {
                            // Remove color from Provider registration option
                            clientBackground.setColorFilter((Integer)colorEvaluator.evaluate(
                                    fraction, endColor, startColor),
                                    PorterDuff.Mode.SRC);
                            // Retrieve original text color
                            mClientOptionTitleTextView.setTextColor((Integer)colorEvaluator.evaluate(
                                    fraction, selectedTextColor, unselectedTextColor));
                            mClientOptionCaptionTextView.setTextColor((Integer)colorEvaluator.evaluate(
                                    fraction, selectedTextColor, unselectedTextColor));
                        }

                        // Clear color filters to retrieve original background
                        if (fraction == 1.0f) {
                            clientBackground.clearColorFilter();
                        }
                        break;
                }
            }
        });

        animator.setDuration(duration);
        animator.start();

        return animator;
    }
}
