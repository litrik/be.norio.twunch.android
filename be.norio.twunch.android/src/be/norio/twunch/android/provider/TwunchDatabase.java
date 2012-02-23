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

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import android.util.Log;
import be.norio.twunch.android.provider.TwunchContract.TwunchesColumns;

public class TwunchDatabase extends SQLiteOpenHelper {
	private static final String TAG = TwunchDatabase.class.getSimpleName();

	private static final String DATABASE_NAME = "twunches.db";

	private static final int VER_LAUNCH = 2;

	private static final int DATABASE_VERSION = VER_LAUNCH;

	interface Tables {
		String TWUNCHES = "twunches";
	}

	public TwunchDatabase(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + Tables.TWUNCHES + " (" + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ TwunchesColumns.ID + " VARCHAR(255) UNIQUE," + TwunchesColumns.ADDED + " INTEGER," + TwunchesColumns.SYNCED
				+ " INTEGER," + TwunchesColumns.NEW + " INTEGER," + TwunchesColumns.TITLE + " VARCHAR(255)," + TwunchesColumns.ADDRESS
				+ " VARCHAR(255)," + TwunchesColumns.NOTE + " VARCHAR(1024)," + TwunchesColumns.PARTICIPANTS + " VARCHAR(2048),"
				+ TwunchesColumns.NUMPARTICIPANTS + " INTEGER," + TwunchesColumns.DATE + " INTEGER," + TwunchesColumns.LINK
				+ " VARCHAR(255)," + TwunchesColumns.CLOSED + " INTEGER," + TwunchesColumns.LATITUDE + " DECIMAL(10,7),"
				+ TwunchesColumns.LONGITUDE + " DECIMAL(10,7)" + ");");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		if (oldVersion != DATABASE_VERSION) {
			Log.w(TAG, "Destroying old data during upgrade");

			db.execSQL("DROP TABLE IF EXISTS " + Tables.TWUNCHES);

			onCreate(db);
		}
	}
}
