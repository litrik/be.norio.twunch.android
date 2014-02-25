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

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.Fields;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.Tracker;

public class AnalyticsUtils {

	public interface Pages {
		public static final String HOME = "Home";
		public static final String DETAILS = "Details";
		public static final String MAP = "Map";
		public static final String ABOUT = "About";
		public static final String WHATS_NEW = "WhatsNew";
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
            Tracker tracker = EasyTracker.getInstance(PrefsUtils.getContext());
            tracker.send(MapBuilder.createAppView().set(Fields.SCREEN_NAME, page).build());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static void trackEvent(String category, String action, String label, long value) {
		try {
            Tracker tracker = EasyTracker.getInstance(PrefsUtils.getContext());
            tracker.send(MapBuilder.createEvent(category, action,label, value).build());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
