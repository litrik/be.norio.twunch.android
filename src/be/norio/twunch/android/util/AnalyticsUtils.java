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

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class AnalyticsUtils {

	public interface Pages {
		public static final String TWUNCH_LIST_DATE = "TwunchListByDate";
		public static final String TWUNCH_LIST_DISTANCE = "TwunchListByDistance";
		public static final String TWUNCH_DETAILS = "TwunchDetails";
		public static final String TWUNCH_MAP = "TwunchMap";
		public static final String ABOUT = "About";
		public static final String WHATS_NEW = "WhatsNew";
	}

	public interface EventCategories {
		public static final String TWUNCH_DETAILS = "TwunchDetails";
	}

	public interface EventActions {
		public static final String SHOW_MAP = "ShowMap";
		public static final String SHOW_DIRECTIONS = "ShowDirections";
		public static final String ADD_TO_CALENDAR = "AddToCalendar";
		public static final String SHARE = "Share";
		public static final String REGISTER = "Register";
	}

	public static void trackPageView(String page) {
		if (TextUtils.isEmpty(page)) {
			return;
		}
		try {
			GoogleAnalyticsTracker.getInstance().trackPageView(page);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
