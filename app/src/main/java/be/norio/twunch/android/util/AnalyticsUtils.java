/**
 *	Copyright 2012 Norio bvba
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

package be.norio.twunch.android.util;

import android.text.TextUtils;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Logger;
import com.google.android.gms.analytics.Tracker;

import be.norio.twunch.android.BuildConfig;
import be.norio.twunch.android.R;

public class AnalyticsUtils {

    private static Tracker sTracker;

    static Tracker getTracker() {
        if (sTracker == null) {
            sTracker = GoogleAnalytics.getInstance(PrefsUtils.getContext()).newTracker(R.xml.analytics);
            if(BuildConfig.DEBUG) {
                GoogleAnalytics.getInstance(PrefsUtils.getContext()).getLogger().setLogLevel(Logger.LogLevel.VERBOSE);
            }
        }
        return sTracker;
    }

    public interface Pages {
        public static final String HOME = "Home";
        public static final String DETAILS = "Details";
        public static final String MAP = "Map";
        public static final String ABOUT = "About";
        public static final String WHATS_NEW = "WhatsNew";
        public static final String SETTINGS = "Settings";
    }

    public interface EventCategories {
        public static final String DETAILS = "Details";
    }

    public interface EventActions {
        public static final String SHOW_MAP = "ShowMap";
        public static final String ADD_TO_CALENDAR = "AddToCalendar";
        public static final String SHARE = "Share";
        public static final String REGISTER = "Register";
    }

    public static void trackPageView(String page) {
        if (TextUtils.isEmpty(page)) {
            return;
        }
        try {
            Tracker tracker = getTracker();
            tracker.setScreenName(page);
            tracker.send(new HitBuilders.AppViewBuilder().build());
            if(BuildConfig.DEBUG) {
                GoogleAnalytics.getInstance(PrefsUtils.getContext()).dispatchLocalHits();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void trackEvent(String category, String action, String label, long value) {
        try {
            Tracker tracker = getTracker();
            tracker.send(new HitBuilders.EventBuilder()
                    .setCategory(category)
                    .setAction(action)
                    .setLabel(label)
                    .setValue(value)
                    .build());
            if(BuildConfig.DEBUG) {
                GoogleAnalytics.getInstance(PrefsUtils.getContext()).dispatchLocalHits();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
