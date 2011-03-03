/**
 *	Copyright 2011 Norio bvba
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

package be.norio.twunch.android;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

class DatabaseHelper extends SQLiteOpenHelper {

	private static final String DATABASE_NAME = "twunches.db";
	private static final int DATABASE_VERSION = 1;

	DatabaseHelper(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL("CREATE TABLE " + TwunchManager.TABLE_NAME + " (" + BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
				+ TwunchManager.COLUMN_ID + " VARCHAR(255) UNIQUE," + TwunchManager.COLUMN_ADDED + " INTEGER,"
				+ TwunchManager.COLUMN_TITLE + " VARCHAR(255)," + TwunchManager.COLUMN_ADDRESS + " VARCHAR(255),"
				+ TwunchManager.COLUMN_NOTE + " VARCHAR(1024)," + TwunchManager.COLUMN_PARTICIPANTS + " VARCHAR(2048),"
				+ TwunchManager.COLUMN_NUMPARTICIPANTS + " INTEGER," + TwunchManager.COLUMN_DATE + " INTEGER,"
				+ TwunchManager.COLUMN_LINK + " VARCHAR(255)," + TwunchManager.COLUMN_LATITUDE + " DECIMAL(10,7),"
				+ TwunchManager.COLUMN_LONGITUDE + " DECIMAL(10,7)" + ");");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + TwunchManager.TABLE_NAME);
		onCreate(db);
	}

}