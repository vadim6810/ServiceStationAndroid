package il.co.tel_ran.carservice;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.AttributeSet;
import android.view.MenuItem;
import android.view.MotionEvent;

import java.util.Locale;

public class SignUpActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        isRTL = Utils.isLocaleRTL(Locale.getDefault());

        mPager = (SignUpViewPager) findViewById(R.id.registration_viewpager);
        ScreenSlidePagerAdapter pagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        mPager.setAdapter(pagerAdapter);
        // This makes sure page fragments don't get unloaded when not visible.
        mPager.setOffscreenPageLimit(NUM_PAGES - 1);
        mPager.addOnPageChangeListener(this);
        // Set the page to user type selection page (RTL is handled by reversing the item)
        mPager.setCurrentItem(getReversedAdapterItem(PAGE_USER_TYPE), true);

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

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        // Disable back button to prevent confusion about the use of this button.
        // The only page in which back button will be displayed is the initial setup page (user type)
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(getReversedAdapterItem(position) == PAGE_USER_TYPE);
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

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

    private static class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {

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
                    return new RegistrationUserTypeFragment();
                case PAGE_LOGIN_DETAILS:
                    return new RegistrationLoginDetailsFragment();
                case PAGE_USER_DETAILS:
                    return new RegistrationUserDetailsFragment();
            }

            return null;
        }

        @Override
        public int getCount() {
            return NUM_PAGES;
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
}