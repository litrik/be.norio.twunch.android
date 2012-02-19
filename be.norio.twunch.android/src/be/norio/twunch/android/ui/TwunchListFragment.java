package be.norio.twunch.android.ui;

import java.util.Date;

import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Typeface;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.provider.BaseColumns;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.CursorAdapter;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import be.norio.twunch.android.R;
import be.norio.twunch.android.TwunchActivity;
import be.norio.twunch.android.TwunchApplication;
import be.norio.twunch.android.TwunchesMapActivity;
import be.norio.twunch.android.provider.TwunchContract.Twunches;
import be.norio.twunch.android.service.SyncService;
import be.norio.twunch.android.util.PrefsUtils;
import be.norio.twunch.android.util.Util;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.apps.iosched.util.DetachableResultReceiver;
import com.google.android.apps.iosched.util.NotifyingAsyncQueryHandler;
import com.google.android.apps.iosched.util.NotifyingAsyncQueryHandler.AsyncQueryListener;

public class TwunchListFragment extends ListFragment implements AsyncQueryListener {

	private Cursor mCursor;
	private CursorAdapter mAdapter;

	private NotifyingAsyncQueryHandler mHandler;
	private DetachableResultReceiver resultReceiver;

	LocationManager locationManager;
	LocationListener locationListener;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setHasOptionsMenu(true);

		mHandler = new NotifyingAsyncQueryHandler(getActivity().getContentResolver(), this);

		// Acquire a reference to the system Location Manager
		locationManager = (LocationManager) getActivity().getSystemService(Context.LOCATION_SERVICE);

		// Define a listener that responds to location updates
		locationListener = new LocationListener() {
			public void onLocationChanged(Location location) {
				mCursor.requery();
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

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		reloadFromArguments(getArguments());
	}

	private final ContentObserver mChangesObserver = new ContentObserver(new Handler()) {
		@Override
		public void onChange(boolean selfChange) {
			if (mCursor != null) {
				mCursor.requery();
			}
		}
	};

	private interface TwunchesQuery {
		int _TOKEN = 0x1;

		String[] PROJECTION = { BaseColumns._ID, Twunches.TITLE, Twunches.ADDRESS, Twunches.DATE, Twunches.NUMPARTICIPANTS,
				Twunches.LATITUDE, Twunches.LONGITUDE, Twunches.NEW };

		int _ID = 0;
		int NAME = 1;
		int ADDRESS = 2;
		int DATE = 3;
		int NUMPARTICIPANTS = 4;
		int LATITUDE = 5;
		int LONGITUDE = 6;
		int NEW = 7;
	}

	public void reloadFromArguments(Bundle arguments) {
		if (mCursor != null) {
			getActivity().stopManagingCursor(mCursor);
			mCursor = null;
		}

		setListAdapter(null);

		mHandler.cancelOperation(TwunchesQuery._TOKEN);

		// Load new arguments
		// final Intent intent =
		// BaseActivity.fragmentArgumentsToIntent(arguments);
		// final Uri uri = intent.getData();
		// if (uri == null) {
		// return;
		// }

		mAdapter = new TwunchAdapter(getActivity());
		setListAdapter(mAdapter);

		mHandler.startQuery(TwunchesQuery._TOKEN, null, Twunches.CONTENT_URI, TwunchesQuery.PROJECTION, null, null,
				Twunches.DEFAULT_SORT);
	}

	@Override
	public void onQueryComplete(int token, Object cookie, Cursor cursor) {
		if (getActivity() == null) {
			return;
		}

		if (token == TwunchesQuery._TOKEN) {
			onDepotsQueryComplete(cursor);
		} else {
			cursor.close();
		}
	}

	private void onDepotsQueryComplete(Cursor cursor) {
		if (mCursor != null) {
			getActivity().stopManagingCursor(mCursor);
			mCursor = null;
		}

		mCursor = cursor;
		getActivity().startManagingCursor(mCursor);
		mAdapter.changeCursor(mCursor);
	}

	class TwunchAdapter extends CursorAdapter {

		public TwunchAdapter(Context context) {
			super(context, null);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {
			// Title
			((TextView) view.findViewById(R.id.twunchTitle)).setText(cursor.getString(TwunchesQuery.NAME));
			// Address
			((TextView) view.findViewById(R.id.twunchAddress)).setText(cursor.getString(TwunchesQuery.ADDRESS));
			((TextView) view.findViewById(R.id.twunchAddress)).setTypeface(null, cursor.getInt(TwunchesQuery.NEW) == 1 ? Typeface.BOLD
					: Typeface.NORMAL);
			// Distance
			Float distance = Util.getDistanceToTwunch(view.getContext(), cursor.getFloat(TwunchesQuery.LATITUDE),
					cursor.getFloat(TwunchesQuery.LONGITUDE));
			((TextView) view.findViewById(R.id.twunchDistance)).setText(String.format(view.getContext().getString(R.string.distance),
					distance));
			view.findViewById(R.id.twunchDistance).setVisibility(distance == null ? View.INVISIBLE : View.VISIBLE);
			// Date
			((TextView) view.findViewById(R.id.twunchDate)).setText(String.format(
					view.getContext().getString(R.string.date),
					DateUtils.formatDateTime(view.getContext(), cursor.getLong(TwunchesQuery.DATE), DateUtils.FORMAT_SHOW_WEEKDAY
							| DateUtils.FORMAT_SHOW_DATE),
					DateUtils.formatDateTime(view.getContext(), cursor.getLong(TwunchesQuery.DATE), DateUtils.FORMAT_SHOW_TIME)));
			((TextView) view.findViewById(R.id.twunchDate)).setTypeface(null, cursor.getInt(TwunchesQuery.NEW) == 1 ? Typeface.BOLD
					: Typeface.NORMAL);
			// Days
			final long msInDay = 86400000;
			int days = (int) (cursor.getLong(TwunchesQuery.DATE) / msInDay - new Date().getTime() / msInDay);
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
	public void onResume() {
		super.onResume();
		refreshTwunches(false);
		// Start listening for location updates
		String provider = locationManager.getBestProvider(new Criteria(), true);
		if (provider != null) {
			locationManager.requestLocationUpdates(provider, 300000, 500, locationListener);
		}
		getActivity().getContentResolver().registerContentObserver(Twunches.CONTENT_URI, true, mChangesObserver);
		if (mCursor != null) {
			mCursor.requery();
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		getActivity().getContentResolver().unregisterContentObserver(mChangesObserver);
		locationManager.removeUpdates(locationListener);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		Intent intent = new Intent();
		intent.setComponent(new ComponentName(v.getContext(), TwunchActivity.class));
		intent.putExtra(TwunchActivity.PARAMETER_ID, ((Cursor) mAdapter.getItem(position)).getInt(TwunchesQuery._ID));
		startActivity(intent);
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
		Intent intent = new Intent(getActivity(), SyncService.class);
		intent.putExtra(SyncService.EXTRA_STATUS_RECEIVER, resultReceiver);
		getActivity().startService(intent);
	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.fragment_twunchlist, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menuRefresh:
			refreshTwunches(true);
			return true;
		case R.id.menuMap:
			startActivity(new Intent(getActivity(), TwunchesMapActivity.class));
			return true;
		}
		return false;
	}

	private class SyncResultReceiver implements DetachableResultReceiver.Receiver {

		@Override
		public void onReceiveResult(int resultCode, Bundle resultData) {
			switch (resultCode) {
			case SyncService.STATUS_RUNNING: {
				break;
			}
			case SyncService.STATUS_FINISHED: {
				mCursor.requery();
				// FIXME
				// ((LoaderActionBarItem)
				// getActionBar().getItem(0)).setLoading(false);
				Toast.makeText(getActivity(), getString(R.string.download_done), Toast.LENGTH_SHORT).show();
				break;
			}
			case SyncService.STATUS_ERROR: {
				// FIXME
				// ((LoaderActionBarItem)
				// getActionBar().getItem(0)).setLoading(false);
				AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
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
