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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;

import be.norio.twunch.android.BuildConfig;
import be.norio.twunch.android.R;
import be.norio.twunch.android.otto.BusProvider;
import be.norio.twunch.android.util.AnalyticsUtils;
import be.norio.twunch.android.util.PrefsUtils;
import be.norio.twunch.android.util.Util;
import be.norio.twunch.android.util.ViewServer;
import butterknife.ButterKnife;

public abstract class BaseActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        // FIXME
		// GoogleAnalyticsSessionManager.getInstance(getApplication()).incrementActivityCount();
		if (!(this instanceof HomeActivity)) {
			getActionBar().setHomeButtonEnabled(true);
			getActionBar().setDisplayHomeAsUpEnabled(true);
		}
		ViewServer.get(getApplicationContext()).addWindow(this);


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
    public void onContentChanged() {
        super.onContentChanged();
        ButterKnife.inject(this);
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			goHome();
			return true;
		default:
			break;
		}
		return super.onOptionsItemSelected(item);
	}

	protected void goHome() {
		finish();
	}


}
