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

package be.norio.twunch.android.provider;

import android.net.Uri;
import android.provider.BaseColumns;

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
	}

	public static final String CONTENT_AUTHORITY = "be.norio.twunch.android";

	private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

	private static final String PATH_TWUNCHES = "twunches";
	private static final String PATH_FUTURE = "future";

	public static class Twunches implements TwunchesColumns, BaseColumns {
		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_TWUNCHES).build();

		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.twunch.twunch";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.twunch.twunch";

		public static final String SORT_DATE = TwunchesColumns.DATE + "," + TwunchesColumns.NUMPARTICIPANTS + " DESC";
		public static final String SORT_DISTANCE = TwunchesColumns.DATE + " DESC," + TwunchesColumns.NUMPARTICIPANTS + " DESC";

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
