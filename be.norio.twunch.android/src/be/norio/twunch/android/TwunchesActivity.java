/**
 *	Copyright 2010-2011 Norio bvba
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

import java.util.Date;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Typeface;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.BaseColumns;
import android.support.v4.app.FragmentActivity;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import be.norio.twunch.android.provider.TwunchContract.Twunches;
import be.norio.twunch.android.service.SyncService;
import be.norio.twunch.android.util.PrefsUtils;
import be.norio.twunch.android.util.Util;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.google.android.apps.iosched.util.DetachableResultReceiver;

public class TwunchesActivity extends FragmentActivity {

	ListView mListView;
	Cursor cursor;

	LocationManager locationManager;
	LocationListener locationListener;

	private DetachableResultReceiver resultReceiver;

	private static String[] columns = new String[] { BaseColumns._ID, Twunches.TITLE, Twunches.ADDRESS, Twunches.DATE,
			Twunches.NUMPARTICIPANTS, Twunches.LATITUDE, Twunches.LONGITUDE, Twunches.NEW };
	private static final int COLUMN_DISPLAY_TITLE = 1;
	private static final int COLUMN_DISPLAY_ADDRESS = 2;
	private static final int COLUMN_DISPLAY_DATE = 3;
	private static final int COLUMN_DISPLAY_NUMPARTICIPANTS = 4;
	private static final int COLUMN_DISPLAY_LATITUDE = 5;
	private static final int COLUMN_DISPLAY_LONGITUDE = 6;
	private static final int COLUMN_DISPLAY_NEW = 7;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		GoogleAnalyticsTracker.getInstance().start(BuildProperties.GA_TRACKING, 60, this);
		GoogleAnalyticsTracker.getInstance().trackPageView("Twunches");

		setContentView(R.layout.twunch_list);

		mListView = (ListView) findViewById(R.id.twunchesList);
		mListView.setEmptyView(findViewById(R.id.noTwunches));

		cursor = getContentResolver().query(Twunches.CONTENT_URI, columns, null, null, Twunches.DEFAULT_SORT);
		startManagingCursor(cursor);
		mListView.setAdapter(new TwunchCursorAdapter(this, cursor));
		mListView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView l, View v, int position, long id) {
				Intent intent = new Intent();
				intent.setComponent(new ComponentName(v.getContext(), TwunchActivity.class));
				intent.putExtra(TwunchActivity.PARAMETER_ID, ((Cursor) l.getAdapter().getItem(position)).getInt(0));
				startActivity(intent);
			}
		});

		// Acquire a reference to the system Location Manager
		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

		// Define a listener that responds to location updates
		locationListener = new LocationListener() {
			public void onLocationChanged(Location location) {
				cursor.requery();
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

		resultReceiver = new DetachableResultReceiver(new Handler());
		resultReceiver.setReceiver(new SyncResultReceiver());
	}

	class TwunchCursorAdapter extends CursorAdapter {

		public TwunchCursorAdapter(Context context, Cursor c) {
			super(context, c);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			// Title
			((TextView) view.findViewById(R.id.twunchTitle)).setText(cursor.getString(COLUMN_DISPLAY_TITLE));
			// Address
			((TextView) view.findViewById(R.id.twunchAddress)).setText(cursor.getString(COLUMN_DISPLAY_ADDRESS));
			((TextView) view.findViewById(R.id.twunchAddress)).setTypeface(null,
					cursor.getInt(COLUMN_DISPLAY_NEW) == 1 ? Typeface.BOLD : Typeface.NORMAL);
			// Distance
			Float distance = Util.getDistanceToTwunch(view.getContext(), cursor.getFloat(COLUMN_DISPLAY_LATITUDE),
					cursor.getFloat(COLUMN_DISPLAY_LONGITUDE));
			((TextView) view.findViewById(R.id.twunchDistance)).setText(String.format(view.getContext().getString(R.string.distance),
					distance));
			view.findViewById(R.id.twunchDistance).setVisibility(distance == null ? View.INVISIBLE : View.VISIBLE);
			// Date
			((TextView) view.findViewById(R.id.twunchDate)).setText(String.format(
					view.getContext().getString(R.string.date),
					DateUtils.formatDateTime(view.getContext(), cursor.getLong(COLUMN_DISPLAY_DATE), DateUtils.FORMAT_SHOW_WEEKDAY
							| DateUtils.FORMAT_SHOW_DATE),
					DateUtils.formatDateTime(view.getContext(), cursor.getLong(COLUMN_DISPLAY_DATE), DateUtils.FORMAT_SHOW_TIME)));
			((TextView) view.findViewById(R.id.twunchDate)).setTypeface(null, cursor.getInt(COLUMN_DISPLAY_NEW) == 1 ? Typeface.BOLD
					: Typeface.NORMAL);
			// Days
			final long msInDay = 86400000;
			int days = (int) (cursor.getLong(COLUMN_DISPLAY_DATE) / msInDay - new Date().getTime() / msInDay);
			((TextView) view.findViewById(R.id.twunchDays)).setText(days == 0 ? getString(R.string.today) : String.format(
					getResources().getQuantityString(R.plurals.days_to_twunch, days), days));
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			final LayoutInflater inflater = LayoutInflater.from(context);
			return inflater.inflate(R.layout.twunch_list_item, parent, false);
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		GoogleAnalyticsTracker.getInstance().dispatch();
		GoogleAnalyticsTracker.getInstance().stop();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.twunches, menu);
		return super.onCreateOptionsMenu(menu);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menuAbout:
			startActivity(new Intent(this, AboutActivity.class));
			return true;
		case R.id.menuRefresh:
			refreshTwunches(true);
			return true;
		case R.id.menuMarkRead:
			// FIXME
			// TwunchManager.getInstance().setAllTwunchesRead(this);
			cursor.requery();
			return true;
		case R.id.menuMap:
			startActivity(new Intent(this, TwunchesMapActivity.class));
			return true;
		}
		return false;
	}

	public void refreshTwunches(boolean force) {
		long lastSync = PrefsUtils.getLastUpdate();
		long now = (new Date()).getTime();
		long oneDay = 1000 * 60 * 60 * 24;
		if (!force && lastSync != 0 && (now - lastSync < oneDay)) {
			Log.d(TwunchApplication.LOG_TAG, "Not refreshing twunches");
			return;
		}
		// FIXME
		// ((LoaderActionBarItem) getActionBar().getItem(0)).setLoading(true);
		Log.d(TwunchApplication.LOG_TAG, "Refreshing twunches");
		Intent intent = new Intent(getApplicationContext(), SyncService.class);
		intent.putExtra(SyncService.EXTRA_STATUS_RECEIVER, resultReceiver);
		startService(intent);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onPause()
	 */
	@Override
	protected void onPause() {
		super.onPause();
		// Stop listening for location updates
		locationManager.removeUpdates(locationListener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onResume()
	 */
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

	private class SyncResultReceiver implements DetachableResultReceiver.Receiver {

		@Override
		public void onReceiveResult(int resultCode, Bundle resultData) {
			switch (resultCode) {
			case SyncService.STATUS_RUNNING: {
				break;
			}
			case SyncService.STATUS_FINISHED: {
				cursor.requery();
				// FIXME
				// ((LoaderActionBarItem)
				// getActionBar().getItem(0)).setLoading(false);
				Toast.makeText(getApplicationContext(), getString(R.string.download_done), Toast.LENGTH_SHORT).show();
				break;
			}
			case SyncService.STATUS_ERROR: {
				// FIXME
				// ((LoaderActionBarItem)
				// getActionBar().getItem(0)).setLoading(false);
				AlertDialog.Builder builder = new AlertDialog.Builder(TwunchesActivity.this);
				builder.setMessage(R.string.download_error);
				builder.setCancelable(false);
				builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// Do nothing
					}
				});
				builder.create().show();
				break;
			}
			}

		}
	}

}