package il.co.tel_ran.carservice.activities;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import java.util.Locale;

import il.co.tel_ran.carservice.ClientUser;
import il.co.tel_ran.carservice.ProviderUser;
import il.co.tel_ran.carservice.R;
import il.co.tel_ran.carservice.User;
import il.co.tel_ran.carservice.UserType;
import il.co.tel_ran.carservice.Utils;
import il.co.tel_ran.carservice.VehicleData;
import il.co.tel_ran.carservice.fragments.RegistrationLoginDetailsFragment;
import il.co.tel_ran.carservice.fragments.RegistrationPageFragment;
import il.co.tel_ran.carservice.fragments.RegistrationServiceDetailsFragment;
import il.co.tel_ran.carservice.fragments.RegistrationUserDetailsFragment;
import il.co.tel_ran.carservice.fragments.RegistrationUserTypeFragment;
import il.co.tel_ran.carservice.fragments.RegistrationVehicleDetailsFragment;

public class SignUpActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener, View.OnClickListener {

    // Number of pages in ViewPager.
    // Each step has it's own page.
    private static final int NUM_PAGES = 3;

    // User type selection page
    public static final int PAGE_USER_TYPE = 0;
    // User login details page
    public static final int PAGE_LOGIN_DETAILS = 1;
    // User type details (client - vehicle info, etc. provider - service info, etc.)
    public static final int PAGE_USER_DETAILS = 2;

    // Check if the current device configuration is RTL
    public static boolean isRTL;

    private SignUpViewPager mPager;
    private ScreenSlidePagerAdapter mPagerAdapter;

    private View mNavigationLayout;
    private Button mPreviousPageButton;
    private Button mNextPageButton;

    private boolean mIsGoogleApiClientConnected;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        isRTL = Utils.isLocaleRTL(Locale.getDefault());

        mIsGoogleApiClientConnected = getIntent().getExtras().getBoolean("GAPI_CLIENT_CONNECTED");

        setupPageNavigation();
        setupViewPager();

        setupActionBar();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // The action bar home key is set to back key.
            // Returns to previous activity.
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onBackPressed() {
        int currentPagerItem = mPager.getCurrentItem();
        // If current page is the initial setup page (User Type) return to previous Activity.
        // Otherwise simply go back one page.
        if (getReversedAdapterItem(currentPagerItem) == PAGE_USER_TYPE)
            super.onBackPressed();
        else
            requestViewPagerPage(getReversedAdapterItem(currentPagerItem) - 1);
    }

    /**
     * Sets ViewPager's current item to desired position.
     * Automatically converts it to RTL format if required.
     * @param position Position of page in ViewPager
     */
    public void requestViewPagerPage(int position) {
        if (position >= 0 && position < NUM_PAGES) {
            // Reverse LTR to RTL position if required.
            mPager.setCurrentItem(getReversedAdapterItem(position), true);
        }
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        int reversedPosition = getReversedAdapterItem(position);

        ActionBar actionBar = getSupportActionBar();

        switch (reversedPosition) {
            case PAGE_USER_TYPE:
                if (actionBar != null) {
                    // Disable back button to prevent confusion about the use of this button.
                    // The only page in which back button will be displayed is the initial setup page (user type)
                    actionBar.setDisplayHomeAsUpEnabled(true);
                }

                mNavigationLayout.setVisibility(View.GONE);
                break;
            case PAGE_LOGIN_DETAILS:
                if (actionBar != null) {
                    actionBar.setDisplayHomeAsUpEnabled(false);
                }

                mNavigationLayout.setVisibility(View.VISIBLE);

                if (mPager.getOffscreenPageLimit() != (NUM_PAGES - 1)) {
                    mPager.setOffscreenPageLimit(NUM_PAGES - 1);
                } else {
                    mNextPageButton.setText(getString(R.string.next_button));
                }

                changeNavigationButtonDrawableVisibility(mNextPageButton, true);

                break;
            case PAGE_USER_DETAILS:
                mNavigationLayout.setVisibility(View.VISIBLE);

                // Update the button's text to "finish".
                mNextPageButton.setText(getString(R.string.finish_button));

                changeNavigationButtonDrawableVisibility(mNextPageButton, false);
                break;
            default:
                break;
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    /*
     * View.OnClickListener
     */

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.page_previous_step:
                requestPageChange(true);
                break;
            case R.id.page_next_step:
                requestPageChange(false);
                break;
        }
    }

    // This is public so ViewPager's fragments can request page changes under their own terms
    // (such as ime action buttons)
    public void requestPageChange(boolean backwards) {
        int currentItem = mPager.getCurrentItem();
        int reversedCurrentItem = getReversedAdapterItem(currentItem);

        try {
            // Get the current page fragment.
            RegistrationPageFragment pageFragment = (RegistrationPageFragment) mPagerAdapter
                    .getItem(reversedCurrentItem);
            if (!backwards) {
                // If the user tries to go forward, check if the current fragment page is allowing it.
                if (pageFragment.isNextStepEnabled()) {
                    // Request the next page.
                    requestViewPagerPage(currentItem + 1);

                    // Finish registration.
                    if (reversedCurrentItem == PAGE_USER_DETAILS) {
                        RegistrationLoginDetailsFragment loginDetailsFragment =
                                (RegistrationLoginDetailsFragment) mPagerAdapter.getItem(
                                        PAGE_LOGIN_DETAILS);

                        // Get login details, they are stored in a User object.
                        User loginDetails = loginDetailsFragment.getUser();

                        // New created user.
                        User newUser;

                        UserType userType = mPagerAdapter.getUserType();
                        switch (userType) {
                            case CLIENT:
                                RegistrationVehicleDetailsFragment vehicleDetailsFragment
                                        = (RegistrationVehicleDetailsFragment) pageFragment;
                                VehicleData data = vehicleDetailsFragment.getVehicleData();
                                newUser = new ClientUser(loginDetails);
                                ((ClientUser) newUser).setVehicleData(data);
                                break;
                            case MASTER:
                                RegistrationServiceDetailsFragment serviceDetailsFragment
                                        = (RegistrationServiceDetailsFragment) pageFragment;
                                newUser = new ProviderUser(loginDetails);
                                // TODO: add service details for this user.
                                break;
                        }
                    }
                }
            } else {
                // Go backwards one page.
                requestViewPagerPage(currentItem - 1);
            }
        } catch (ClassCastException e) {
            e.printStackTrace();
        }
    }

    public boolean isGoogleApiClientConnected() {
        return mIsGoogleApiClientConnected;
    }

    public static class SignUpViewPager extends ViewPager {

        public SignUpViewPager(Context context) {
            super(context);
        }

        public SignUpViewPager(Context context, AttributeSet attrs) {
            super(context, attrs);
        }

        @Override
        public boolean onInterceptTouchEvent(MotionEvent event) {
            // Never allow swiping to switch between pages
            return false;
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            // Never allow swiping to switch between pages
            return false;
        }
    }

    private static class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter
            implements RegistrationUserTypeFragment.UserTypeChangeListener {

        private UserType mUserType = UserType.CLIENT;
        private boolean mIsUserTypeChanged = true;

        private RegistrationUserTypeFragment mUserTypeFragment;
        private RegistrationLoginDetailsFragment mLoginDetailsFragment;
        private RegistrationUserDetailsFragment mUserDetailsFragment;

        public ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            /*
            Convert position for RTL format if required (RTL Locale).
            This is required to make the ViewPager swipe from right to left when using
            RTL Locale
            */
            switch (getReversedAdapterItem(position)) {
                case PAGE_USER_TYPE:
                    if (mUserTypeFragment == null) {
                        mUserTypeFragment =
                                new RegistrationUserTypeFragment();
                        // Listen to user-type selections.
                        mUserTypeFragment.setListener(this);
                    }
                    return mUserTypeFragment;
                case PAGE_LOGIN_DETAILS:
                    if (mLoginDetailsFragment == null) {
                        mLoginDetailsFragment = new RegistrationLoginDetailsFragment();
                    }
                    return mLoginDetailsFragment;
                case PAGE_USER_DETAILS:
                    // Load the user-type-specific details fragment according to user type.
                    // This field changes every time the user selects a different type.
                    if (mUserDetailsFragment == null || mIsUserTypeChanged) {
                        switch (mUserType) {
                            case MASTER:
                                mUserDetailsFragment = new RegistrationServiceDetailsFragment();
                                break;
                            default:
                                mUserDetailsFragment = new RegistrationVehicleDetailsFragment();
                                break;
                        }
                    }
                    mIsUserTypeChanged = false;

                    return mUserDetailsFragment;
            }
            return null;
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
        }

        @Override
        public int getItemPosition(Object object) {
            // getItemPosition will be called after notifyDataSetChanged, giving us an opportunity
            // to make changes to fragments by returning POSITION_NONE.

            // If the object is a vehicle-details fragment or service-details fragment (both inherit from the same base)
            // then return POSITION_NONE, forcing the view pager to reload the fragment for that position.
            if (object instanceof RegistrationUserDetailsFragment) {
                return POSITION_NONE;
            }

            // Let super handle the rest of the cases.
            return super.getItemPosition(object);
        }

        @Override
        public void onUserTypeChange(UserType previousType, UserType newType) {
            if (previousType != newType && newType != mUserType) {
                // Reload fragment based on the updated user type.
                mUserType = newType;
                mIsUserTypeChanged = true;
                notifyDataSetChanged();
            }
        }

        public UserType getUserType() {
            return mUserType;
        }
    }

    /**
     * Checks if position in LTR needs to be converted to RTL format according to current Locale.
     * @param position - Desired position in LTR format
     * @return Position of the same item in RTL format if current Locale is RTL, otherwise returns
     * the same position.
     */
    private static int getReversedAdapterItem(int position) {
        if (isRTL)
            return Math.abs(position - (NUM_PAGES - 1));
        return position;
    }

    private void setupPageNavigation() {
        mNavigationLayout = findViewById(R.id.page_navigation_layout);
        mPreviousPageButton = (Button) findViewById(R.id.page_previous_step);
        mPreviousPageButton.setOnClickListener(this);
        mNextPageButton = (Button) findViewById(R.id.page_next_step);
        mNextPageButton.setOnClickListener(this);

        // Arrow direction and placement relative to the button should be in the opposite direction.
        // RTL - to the left
        // LTR - to the right
        Drawable navigateLeftIcon = ContextCompat.getDrawable(SignUpActivity.this,
                R.drawable.ic_navigate_before_accent_24dp);
        Drawable navigateRightIcon = ContextCompat.getDrawable(SignUpActivity.this,
                R.drawable.ic_navigate_next_accent_24dp);
        if (Utils.isLocaleRTL(Locale.getDefault())) {
            mPreviousPageButton.setCompoundDrawablesWithIntrinsicBounds(
                    null, null, navigateRightIcon, null);
            mNextPageButton.setCompoundDrawablesWithIntrinsicBounds(
                    navigateLeftIcon, null, null, null);
        } else {
            mPreviousPageButton.setCompoundDrawablesWithIntrinsicBounds(
                    navigateLeftIcon, null, null, null);
            mNextPageButton.setCompoundDrawablesWithIntrinsicBounds(
                    null, null, navigateRightIcon, null);
        }
    }

    private void setupActionBar() {
        Toolbar toolbar = (Toolbar) findViewById(R.id.sign_up_toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Display the home key as back key.
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            // Hide the title.
            actionBar.setDisplayShowTitleEnabled(false);
        }
    }

    private void setupViewPager() {
        mPager = (SignUpViewPager) findViewById(R.id.registration_viewpager);
        mPagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPagerAdapter);
        // Only load one additional page at start.
        // This results in displaying only user type and login details pages, keeping
        // user-type-specific details unloaded, until we choose one.
        mPager.setOffscreenPageLimit(1);
        mPager.addOnPageChangeListener(this);
        // Set the page to user type selection page (RTL is handled by reversing the item)
        mPager.setCurrentItem(getReversedAdapterItem(PAGE_USER_TYPE), true);
    }

    private void changeNavigationButtonDrawableVisibility(Button button, boolean visible) {
        if (button != null) {
            Drawable[] drawables = button.getCompoundDrawables();

            // Look for the navigation icon (could be from the left if it's RTL or from the right if its LTR)
            // The loop just jumps between index 0 (left) and index 2 (right)
            for (int i = 0; i < 3; i += 2) {
                Drawable navigationIcon = drawables[i];
                if (navigationIcon != null) {
                    // Hide the navigation icon for finish button.
                    // Alpha controls the transparency, therefore 0 - invisible, 255 - visible.
                    navigationIcon.setAlpha(visible ? 255 : 0);

                    // If we found a non-null drawable it's the only navigation icon we require, don't look for anymore.
                    break;
                }
            }
        }
    }
}