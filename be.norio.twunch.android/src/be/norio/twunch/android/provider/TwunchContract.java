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

	public static class Twunches implements TwunchesColumns, BaseColumns {
		public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_TWUNCHES).build();

		public static final String CONTENT_TYPE = "vnd.android.cursor.dir/vnd.twunch.twunch";
		public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/vnd.twunch.twunch";

		public static final String DEFAULT_SORT = TwunchesColumns.DATE + "," + TwunchesColumns.NUMPARTICIPANTS + " DESC";

		public static Uri buildTwunchUri(String twunchId) {
			return CONTENT_URI.buildUpon().appendPath(twunchId).build();
		}

		public static String getTwunchId(Uri uri) {
			return uri.getPathSegments().get(1);
		}
	}

	private TwunchContract() {
	}
}
