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

package be.norio.twunch.android.provider;

import android.net.Uri;
import android.provider.BaseColumns;

import be.norio.twunch.android.BuildConfig;

public class TwunchContract {

	interface TwunchesColumns {
		String ID = "id";
		String ADDED = "added";
		String SYNCED = "synced";
		String TITLE = "title";
		String ADDRESS = "address";
		String NOTE = "note";
		String DATE = "date";
		String LINK = "link";
		String LATITUDE = "latitude";
		String LONGITUDE = "longitude";
		String PARTICIPANTS = "participants";
		String NUMPARTICIPANTS = "numparticipants";
		String NEW = "new";
		String CLOSED = "closed";
		String DISTANCE = "distance";
	}

	public static final String CONTENT_AUTHORITY = BuildConfig.PROVIDER_AUTHORITY;

	private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

	private static final String PATH_TWUNCHES = "twunches";
	private static final String PATH_FUTURE = "future";

	public static class Twunches implements TwunchesColumns, BaseColumns {
		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_TWUNCHES).build();

		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.twunch.twunch";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.twunch.twunch";

		public static final String SORT_DATE = TwunchesColumns.DATE + "," + TwunchesColumns.NUMPARTICIPANTS + " DESC";
		public static final String SORT_DISTANCE = TwunchesColumns.DISTANCE + "," + TwunchesColumns.NUMPARTICIPANTS + " DESC";

		public interface Query {
			int _TOKEN = 0x1;

			String[] PROJECTION = { BaseColumns._ID, Twunches.TITLE, Twunches.ADDRESS, Twunches.DATE, Twunches.NUMPARTICIPANTS,
					Twunches.NEW, Twunches.DISTANCE, Twunches.LATITUDE, Twunches.LONGITUDE, Twunches.TITLE, Twunches.NOTE,
					Twunches.PARTICIPANTS, Twunches.CLOSED, Twunches.LINK };

			int _ID = 0;
			int NAME = 1;
			int ADDRESS = 2;
			int DATE = 3;
			int NUMPARTICIPANTS = 4;
			int NEW = 5;
			int DISTANCE = 6;
			int LATITUDE = 7;
			int LONGITUDE = 8;
			int TITLE = 9;
			int NOTE = 10;
			int PARTICIPANTS = 11;
			int CLOSED = 12;
			int LINK = 13;
		}

		public static Uri buildTwunchUri(String twunchId) {
			return CONTENT_URI.buildUpon().appendPath(twunchId).build();
		}

		public static Uri buildFutureTwunchesUri() {
			return CONTENT_URI.buildUpon().appendPath(PATH_FUTURE).build();
		}

		public static String getTwunchId(Uri uri) {
			return uri.getPathSegments().get(1);
		}
	}

	private TwunchContract() {
	}
}
