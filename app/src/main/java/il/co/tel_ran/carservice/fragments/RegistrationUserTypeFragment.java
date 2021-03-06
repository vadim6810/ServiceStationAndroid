package il.co.tel_ran.carservice.fragments;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Intent;
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
import android.widget.Button;
import android.widget.TextView;

import il.co.tel_ran.carservice.R;
import il.co.tel_ran.carservice.UserType;
import il.co.tel_ran.carservice.Utils;
import il.co.tel_ran.carservice.activities.SignInActivity;
import il.co.tel_ran.carservice.activities.SignUpActivity;

/**
 * Created by Max on 12/10/2016.
 */

public class RegistrationUserTypeFragment extends Fragment
        implements View.OnClickListener {

    private UserType mUserType = UserType.NONE;

    private View mClientOptionLayout;
    private View mServiceProviderOptionLayout;

    private TextView mClientOptionTitleTextView;
    private TextView mClientOptionCaptionTextView;
    private TextView mProviderOptionTitleTextView;
    private TextView mProviderOptionCaptionTextView;

    private FloatingActionButton mNextStepFAB;

    private UserTypeChangeListener mListener;

    public interface UserTypeChangeListener {
        void onUserTypeChange(UserType previousType, UserType newType);
    }

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

        Button navigateToSignInButton = (Button) layout.findViewById(R.id.navgiate_to_sign_in_button);
        navigateToSignInButton.setOnClickListener(this);

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
                animateRegisterOptionsLayouts(mUserType, UserType.CLIENT,
                        android.R.color.background_light,
                        Utils.getThemeAccentColor(getActivity()), 300);

                if (mListener != null) {
                    mListener.onUserTypeChange(mUserType, UserType.CLIENT);
                }
                mUserType = UserType.CLIENT;

                // Show the FAB if it's hidden.
                // We keep it hidden to encourage the user to select a type, otherwise don't let the user proceed.
                if (mNextStepFAB.getVisibility() != View.VISIBLE) {
                    mNextStepFAB.show();
                }

                break;
            case R.id.service_provider_option_layout:
                // Animate background color and text color.
                animateRegisterOptionsLayouts(mUserType, UserType.MASTER,
                        android.R.color.background_light,
                        Utils.getThemeAccentColor(getActivity()), 300);

                if (mListener != null) {
                    mListener.onUserTypeChange(mUserType, UserType.MASTER);
                }
                mUserType = UserType.MASTER;

                // Show the FAB if it's hidden.
                // We keep it hidden to encourage the user to select a type, otherwise don't let the user proceed.
                if (mNextStepFAB.getVisibility() != View.VISIBLE) {
                    mNextStepFAB.show();
                }

                if (mListener != null) {
                    mUserType = UserType.MASTER;
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
            case R.id.navgiate_to_sign_in_button:
                Intent intent = new Intent(getContext(), SignInActivity.class);
                // Use clear FLAG_ACTIVITY_CLEAR_TOP to ensure we don't get stack overflow.
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
                break;
        }
    }

    public void setListener(UserTypeChangeListener listener) {
        mListener = listener;
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
                    case CLIENT:
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
                    case MASTER:
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
