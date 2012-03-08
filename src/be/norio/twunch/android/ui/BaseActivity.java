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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.webkit.WebView;
import be.norio.twunch.android.BuildProperties;
import be.norio.twunch.android.R;
import be.norio.twunch.android.util.AnalyticsUtils;
import be.norio.twunch.android.util.PrefsUtils;
import be.norio.twunch.android.util.Util;
import be.norio.twunch.android.util.ViewServer;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.google.android.apps.iosched.util.GoogleAnalyticsSessionManager;

public abstract class BaseActivity extends SherlockFragmentActivity {

	private static final String TAG = BaseActivity.class.getSimpleName();
	private final static boolean LOGV = true;

	protected final String GA_VAR_KLANTID = "KLANTID";

	private final int DIALOG_WHATS_NEW = 56479952;
	private final int DIALOG_ABOUT = 3267613;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		GoogleAnalyticsSessionManager.getInstance(getApplication()).incrementActivityCount();
		if (!(this instanceof TwunchListActivity)) {
			getSupportActionBar().setHomeButtonEnabled(true);
		}
		ViewServer.get(getApplicationContext()).addWindow(this);

		showWhatsNew();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		GoogleAnalyticsTracker.getInstance().dispatch();
		GoogleAnalyticsSessionManager.getInstance().decrementActivityCount();
		ViewServer.get(getApplicationContext()).removeWindow(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		ViewServer.get(getApplicationContext()).setFocusedWindow(this);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.activity_base, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			goHome();
			return true;
		case R.id.menuAbout:
			showDialog(DIALOG_ABOUT);
			break;
		case R.id.menuWhatsNew:
			showDialog(DIALOG_WHATS_NEW);
			break;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	protected void goHome() {
		Intent intent = new Intent(this, TwunchListActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}

	private void showWhatsNew() {
		final int currentVersion = Integer.parseInt(BuildProperties.VERSION_CODE);
		if (currentVersion > PrefsUtils.getLastRunVersion()) {
			if (LOGV)
				Log.v(TAG, "This is the first time we run version " + BuildProperties.VERSION_CODE);
			showDialog(DIALOG_WHATS_NEW);
		}
		PrefsUtils.setLastRunVersion(currentVersion);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		Dialog dialog;
		switch (id) {
		case DIALOG_WHATS_NEW:
			dialog = createHtmlDialog(getString(R.string.whats_new), R.raw.whats_new, AnalyticsUtils.Pages.WHATS_NEW);
			break;
		case DIALOG_ABOUT:
			dialog = createHtmlDialog(getString(R.string.about, BuildProperties.VERSION_NAME), R.raw.about, AnalyticsUtils.Pages.ABOUT);
			break;
		default:
			dialog = null;
		}
		return dialog;
	}

	private Dialog createHtmlDialog(String title, int contentResourceId, String pageName) {
		AnalyticsUtils.trackPageView(pageName);
		WebView webView = new WebView(this);
		webView.loadDataWithBaseURL(null, Util.readTextFromResource(this, contentResourceId), "text/html", "utf-8", null);
		return new AlertDialog.Builder(this).setTitle(title).setView(webView).setPositiveButton(android.R.string.ok, null).create();
	}
}