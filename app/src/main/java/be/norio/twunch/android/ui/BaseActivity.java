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
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

import be.norio.twunch.android.BuildConfig;
import be.norio.twunch.android.R;
import be.norio.twunch.android.otto.BusProvider;
import be.norio.twunch.android.util.AnalyticsUtils;
import be.norio.twunch.android.util.PrefsUtils;
import be.norio.twunch.android.util.Util;
import be.norio.twunch.android.util.ViewServer;
import butterknife.ButterKnife;

public abstract class BaseActivity extends ActionBarActivity {

	private static final String TAG = BaseActivity.class.getSimpleName();
	private final static boolean LOGV = true;

	private final int DIALOG_WHATS_NEW = 56479952;
	private final int DIALOG_ABOUT = 3267613;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        // FIXME
		// GoogleAnalyticsSessionManager.getInstance(getApplication()).incrementActivityCount();
		if (!(this instanceof HomeActivity)) {
			getSupportActionBar().setHomeButtonEnabled(true);
			getSupportActionBar().setDisplayHomeAsUpEnabled(true);
		}
		ViewServer.get(getApplicationContext()).addWindow(this);

		showWhatsNew();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
        // FIXME
		//GoogleAnalyticsTracker.getInstance().dispatch();
        // GoogleAnalyticsSessionManager.getInstance().decrementActivityCount();
		ViewServer.get(getApplicationContext()).removeWindow(this);
	}

	@Override
	protected void onPause() {
		super.onPause();
		BusProvider.getInstance().unregister(this);
	}

	@Override
	protected void onResume() {
		super.onResume();
		BusProvider.getInstance().register(this);
		ViewServer.get(getApplicationContext()).setFocusedWindow(this);
	}

    @Override
    public void onSupportContentChanged() {
        super.onSupportContentChanged();
        ButterKnife.inject(this);
    }

    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_base, menu);
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
		finish();
	}

	private void showWhatsNew() {
		final int currentVersion = BuildConfig.VERSION_CODE;
		if (currentVersion > PrefsUtils.getLastRunVersion()) {
			if (LOGV)
				Log.v(TAG, "This is the first time we run version " + BuildConfig.VERSION_CODE);
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
			dialog = createHtmlDialog(getString(R.string.about, BuildConfig.VERSION_NAME), R.raw.about,
					AnalyticsUtils.Pages.ABOUT);
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
		return new AlertDialog.Builder(this).setTitle(title).setView(webView).setPositiveButton(android.R.string.ok, null)
				.setNeutralButton(R.string.rate, new OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));
					}
				}).create();
	}
}
