/**
 *	Copyright 2012-2013 Norio bvba
 *
 *	This program is free software: you can redistribute it and/or modify
 *	it under the terms of the GNU General Public License as published by
 *	the Free Software Foundation, either version 3 of the License, or
 *	(at your option) any later version.
 *
 *	This program is distributed in the hope that it will be useful,
 *	but WITHOUT ANY WARRANTY; without even the implied warranty of
 *	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *	GNU General Public License for more details.
 *
 *	You should have received a copy of the GNU General Public License
 *	along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package be.norio.twunch.android.ui;

import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.support.v7.app.ActionBar;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.squareup.otto.Subscribe;

import java.util.Date;

import be.norio.twunch.android.R;
import be.norio.twunch.android.TwunchApplication;
import be.norio.twunch.android.data.DataManager;
import be.norio.twunch.android.otto.OnTwunchClickedEvent;
import be.norio.twunch.android.ui.fragment.TwunchListFragment;
import be.norio.twunch.android.ui.fragment.TwunchMapFragment;
import be.norio.twunch.android.util.AnalyticsUtils;
import be.norio.twunch.android.util.PrefsUtils;
import be.norio.twunch.android.util.TwitterUtils;

public class HomeActivity extends BaseActivity implements ActionBar.TabListener, OnPageChangeListener {

    private final static String[] PAGES = new String[]{AnalyticsUtils.Pages.TWUNCH_LIST_DATE,
            AnalyticsUtils.Pages.TWUNCH_LIST_DISTANCE, AnalyticsUtils.Pages.TWUNCH_MAP};

    MenuItem refreshMenuItem;

    LocationManager locationManager;
    LocationListener locationListener;

    private ViewPager mViewPager;
    private MyAdapter mMyAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_twunch_list);
        mViewPager = (ViewPager) findViewById(R.id.home_pager);
        mMyAdapter = new MyAdapter(getSupportFragmentManager(),
                GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS);
        mViewPager.setAdapter(mMyAdapter);
        mViewPager.setOffscreenPageLimit(2);
        mViewPager.setOnPageChangeListener(this);

        getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

        final ActionBar bar = getSupportActionBar();
        bar.addTab(bar.newTab().setText(R.string.tab_date).setTabListener(this), false);
        bar.addTab(bar.newTab().setText(R.string.tab_distance).setTabListener(this), false);
        bar.addTab(bar.newTab().setText(R.string.menu_map).setTabListener(this), false);

        bar.setSelectedNavigationItem(PrefsUtils.getLastTab());

        // Acquire a reference to the system Location Manager
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);

        // Define a listener that responds to location updates
        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                if (location != null) {
                    // FIXME
                    // new UpdateDistancesTask().execute(location);
                }
            }

            public void onStatusChanged(String provider, int status, Bundle extras) {
                // Do nothing
            }

            public void onProviderEnabled(String provider) {
                // Do nothing
            }

            public void onProviderDisabled(String provider) {
                // Do nothing
            }
        };

        if (PrefsUtils.getTwitterToken() == null) {
            TwitterUtils.getToken();
        }

    }

    public static class MyAdapter extends FragmentPagerAdapter {

        final private boolean mIsGooglePlayServicesAvailable;

        public MyAdapter(FragmentManager fm, boolean b) {
            super(fm);
            mIsGooglePlayServicesAvailable = b;
        }

        @Override
        public int getCount() {
            return mIsGooglePlayServicesAvailable ? 3 : 2;
        }

        @Override
        public Fragment getItem(int position) {

            if (position == 0 || position == 1) {
                return TwunchListFragment.newInstance();
            } else {
                return new TwunchMapFragment();
            }
        }
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
        int position = tab.getPosition();
        PrefsUtils.setLastTab(position);
        if (mViewPager.getCurrentItem() != position) {
            mViewPager.setCurrentItem(position, true);
            AnalyticsUtils.trackPageView(PAGES[position]);
        }
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {
        // Do nothing
    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {
        // Do nothing
    }

    @Override
    public void onPageScrollStateChanged(int arg0) {
        // Do nothing
    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {
        // Do nothing
    }

    @Override
    public void onPageSelected(int position) {
        getSupportActionBar().setSelectedNavigationItem(position);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.fragment_twunch_list, menu);
        refreshMenuItem = menu.findItem(R.id.menuRefresh);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menuRefresh:
                refreshTwunches(true);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void refreshTwunches(boolean force) {
        long lastSync = PrefsUtils.getLastUpdate();
        long now = (new Date()).getTime();
        if (!force && lastSync != 0 && (now - lastSync < DateUtils.DAY_IN_MILLIS)) {
            Log.d(TwunchApplication.LOG_TAG, "Not refreshing twunches");
            return;
        }
        if (refreshMenuItem != null) {
        }
        Log.d(TwunchApplication.LOG_TAG, "Refreshing twunches");
        DataManager.getInstance().loadTwunches();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshTwunches(false);
        // Start listening for location updates
        String provider = locationManager.getBestProvider(new Criteria(), true);
        if (provider != null) {
            locationManager.requestLocationUpdates(provider, 300000, 500, locationListener);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        locationManager.removeUpdates(locationListener);
    }

    @Subscribe
    public void onTwunchClicked(OnTwunchClickedEvent event) {
        TwunchDetailsActivity.start(this, event.getTwunch().getId());
    }

}
