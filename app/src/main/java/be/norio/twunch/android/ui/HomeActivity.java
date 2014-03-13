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

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;

import com.google.android.gms.common.AccountPicker;
import com.squareup.otto.Subscribe;

import be.norio.twunch.android.BuildConfig;
import be.norio.twunch.android.R;
import be.norio.twunch.android.data.DataManager;
import be.norio.twunch.android.otto.NetworkStatusUpdatedEvent;
import be.norio.twunch.android.otto.TwunchClickedEvent;
import be.norio.twunch.android.otto.TwunchesFailedEvent;
import be.norio.twunch.android.util.AnalyticsUtils;
import be.norio.twunch.android.util.PrefsUtils;
import de.keyboardsurfer.android.widget.crouton.Crouton;
import de.keyboardsurfer.android.widget.crouton.Style;

public class HomeActivity extends BaseActivity {

    // The account name
    public static final String ACCOUNT = "Twunch";
    // Sync interval constants
    public static final long HOUR_IN_SECONDS = 3600;
    Account mAccount;

    /**
     * Create a new dummy account for the sync adapter
     *
     * @param context The application context
     */
    public static Account CreateSyncAccount(Context context) {
        // Create the account type and default account
        Account newAccount = new Account(ACCOUNT, BuildConfig.PACKAGE_NAME);
        // Get an instance of the Android account manager
        AccountManager accountManager = (AccountManager) context.getSystemService(ACCOUNT_SERVICE);
        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
        if (accountManager.addAccountExplicitly(newAccount, null, null)) {
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call context.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */
            // Turn on periodic syncing
            ContentResolver.setIsSyncable(newAccount, BuildConfig.PACKAGE_NAME, 1);
            ContentResolver.setSyncAutomatically(newAccount, BuildConfig.PACKAGE_NAME, true);
            ContentResolver.addPeriodicSync(newAccount, BuildConfig.PACKAGE_NAME, Bundle.EMPTY, HOUR_IN_SECONDS);
        } else {
            /*
             * The account exists or some other error occurred. Log this, report it,
             * or handle it internally.
             */
        }
        return newAccount;
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_home);

        final int currentVersion = BuildConfig.VERSION_CODE;
        if (currentVersion > PrefsUtils.getLastRunVersion()) {
            // TODO
            // showWhatsNew();
        }
        PrefsUtils.setLastRunVersion(currentVersion);

        AnalyticsUtils.trackPageView(AnalyticsUtils.Pages.HOME);

        // Create the dummy account
        mAccount = CreateSyncAccount(this);

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

    @Subscribe
    public void onTwunchClicked(TwunchClickedEvent event) {
        DetailsActivity.start(this, event.getTwunch().getId());
    }

    @Subscribe
    public void onNetworkStatusUpdated(NetworkStatusUpdatedEvent event) {
        setProgressBarIndeterminateVisibility(event.getOustandingNetworkCalls() > 0);
    }

    @Subscribe
    public void onTwunchesFailed(TwunchesFailedEvent event) {
        Crouton.makeText(this, R.string.download_error, Style.ALERT).show();
    }

}
