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

package be.norio.twunch.android.ui;

import java.util.Date;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;
import be.norio.twunch.android.R;
import be.norio.twunch.android.TwunchApplication;
import be.norio.twunch.android.provider.TwunchContract.Twunches;
import be.norio.twunch.android.service.SyncService;
import be.norio.twunch.android.util.PrefsUtils;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.ActionBar.Tab;
import com.actionbarsherlock.app.ActionBar.TabListener;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.apps.iosched.util.DetachableResultReceiver;

public class TwunchListActivity extends BaseActivity implements TabListener {

	TwunchListFragment[] mFragments = new TwunchListFragment[2];
	String[] mSort = new String[] { Twunches.SORT_DATE, Twunches.SORT_DISTANCE };

	MenuItem refreshMenuItem;

	private DetachableResultReceiver resultReceiver;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getSupportActionBar().setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		final ActionBar bar = getSupportActionBar();
		bar.addTab(bar.newTab().setText("By Date").setTabListener(this));
		bar.addTab(bar.newTab().setText("By Distance").setTabListener(this));

		resultReceiver = new DetachableResultReceiver(new Handler());
		resultReceiver.setReceiver(new SyncResultReceiver());
	}

	@Override
	public void onTabSelected(Tab tab) {
		int pos = tab.getPosition();
		if (mFragments[pos] == null) {
			mFragments[pos] = new TwunchListFragment();
			Bundle args = new Bundle();
			args.putString(TwunchListFragment.EXTRA_SORT, mSort[pos]);
			mFragments[pos].setArguments(args);
		} else {
			//
		}
		getSupportFragmentManager().beginTransaction().replace(android.R.id.content, mFragments[pos]).commit();
	}

	@Override
	public void onTabUnselected(Tab tab) {
		// int pos = tab.getPosition();
		// getSupportFragmentManager().beginTransaction().detach(mFragments[pos]);
	}

	@Override
	public void onTabReselected(Tab tab) {
		// Do nothing
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		super.onCreateOptionsMenu(menu);
		getSupportMenuInflater().inflate(R.menu.fragment_twunchlist, menu);
		refreshMenuItem = menu.findItem(R.id.menuRefresh);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menuRefresh:
			refreshTwunches(true);
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
		if (refreshMenuItem != null) {
			refreshMenuItem.setActionView(R.layout.actionbar_indeterminate_progress);
			((AnimationDrawable) ((ImageView) refreshMenuItem.getActionView().findViewById(R.id.refreshing)).getDrawable()).start();
		}
		Log.d(TwunchApplication.LOG_TAG, "Refreshing twunches");
		Intent intent = new Intent(this, SyncService.class);
		intent.putExtra(SyncService.EXTRA_STATUS_RECEIVER, resultReceiver);
		startService(intent);
	}

	private class SyncResultReceiver implements DetachableResultReceiver.Receiver {

		@Override
		public void onReceiveResult(int resultCode, Bundle resultData) {
			switch (resultCode) {
			case SyncService.STATUS_RUNNING: {
				break;
			}
			case SyncService.STATUS_FINISHED: {
				((AnimationDrawable) ((ImageView) refreshMenuItem.getActionView().findViewById(R.id.refreshing)).getDrawable()).stop();
				refreshMenuItem.setActionView(null);
				Toast.makeText(TwunchListActivity.this, getString(R.string.download_done), Toast.LENGTH_SHORT).show();
				break;
			}
			case SyncService.STATUS_ERROR: {
				((AnimationDrawable) ((ImageView) refreshMenuItem.getActionView().findViewById(R.id.refreshing)).getDrawable()).stop();
				refreshMenuItem.setActionView(null);
				AlertDialog.Builder builder = new AlertDialog.Builder(TwunchListActivity.this);
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

	@Override
	protected void onResume() {
		super.onResume();
		refreshTwunches(false);
	}

}
