/**
 *	Copyright 2012-2013 Norio bvba
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
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;

import com.squareup.otto.Subscribe;

import be.norio.twunch.android.BuildConfig;
import be.norio.twunch.android.R;
import be.norio.twunch.android.data.DataManager;
import be.norio.twunch.android.otto.NetworkStatusUpdatedEvent;
import be.norio.twunch.android.otto.TwunchClickedEvent;
import be.norio.twunch.android.ui.fragment.HtmlDialogFragment;
import be.norio.twunch.android.util.AnalyticsUtils;
import be.norio.twunch.android.util.PrefsUtils;
import be.norio.twunch.android.util.Util;

public class HomeActivity extends BaseActivity {

    private final int DIALOG_WHATS_NEW = 56479952;
    private final int DIALOG_ABOUT = 3267613;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);

        final int currentVersion = BuildConfig.VERSION_CODE;
        if (currentVersion > PrefsUtils.getLastRunVersion()) {
            // FIXME
            // showWhatsNew();
        }
        PrefsUtils.setLastRunVersion(currentVersion);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.activity_home, menu);
        menu.findItem(R.id.action_map).setVisible(findViewById(R.id.fragment_map) == null);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_map:
                MapActivity.start(this);
                break;
            case R.id.action_settings:
                SettingsActivity.start(this);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        DataManager.getInstance().loadTwunches(false);
    }

    @Subscribe
    public void onTwunchClicked(TwunchClickedEvent event) {
        DetailsActivity.start(this, event.getTwunch().getId());
    }

    @Subscribe
    public void onNetworkStatusUpdated(NetworkStatusUpdatedEvent event) {
        setProgressBarIndeterminateVisibility(event.getOustandingNetworkCalls() > 0);
    }

}
