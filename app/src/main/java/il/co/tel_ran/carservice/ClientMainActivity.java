package il.co.tel_ran.carservice;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

public class ClientMainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_client_main);

        setupActionBar();
        setupTabLayout();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.client_main_activity_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_profile:
                break;
            case R.id.menu_item_about:
                break;
            default:
                return super.onOptionsItemSelected(item);
        }

        return true;
    }

    public void showSignUpForm(View view) {
    }

    public void showSignInForm(View view) {
    }

    private void setupActionBar() {
        Toolbar actionBar = (Toolbar) findViewById(R.id.actionBar);
        setSupportActionBar(actionBar);
    }

    private void setupTabLayout() {
        ViewPager tabsViewPager = (ViewPager) findViewById(R.id.tabs_viewpager);
        tabsViewPager.setAdapter(new TabsPagerAdapter(getSupportFragmentManager()));

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(tabsViewPager);
    }

    private class TabsPagerAdapter extends FragmentStatePagerAdapter {

        private static final int TAB_COUNT = 3;

        private String[] tabTitles = new String[TAB_COUNT];

        public TabsPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager);

            tabTitles[0] = getString(R.string.tab_title_recent_services);
            tabTitles[1] = getString(R.string.tab_title_find_services);
            tabTitles[2] = getString(R.string.tab_title_request_service);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    // Recent services
                    return new RecentServicesTabFragment();
                case 1:
                    // Search for service
                    return new SearchServiceTabFragment();
                case 2:
                    // Request service
                    return new RequestServiceTabFragment();
            }
            return null;
        }

        @Override
        public int getCount() {
            return TAB_COUNT;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return tabTitles[position];
        }
    }
}
