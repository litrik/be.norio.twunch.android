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
import android.os.Bundle;
import android.view.MenuItem;
import android.view.Window;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesClient;
import com.google.android.gms.location.LocationClient;

import be.norio.twunch.android.data.DataManager;
import be.norio.twunch.android.otto.BusProvider;
import butterknife.ButterKnife;
import de.keyboardsurfer.android.widget.crouton.Crouton;

public abstract class BaseActivity extends Activity implements GooglePlayServicesClient.OnConnectionFailedListener, GooglePlayServicesClient.ConnectionCallbacks {

    private LocationClient mLocationClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

        if (!(this instanceof HomeActivity)) {
            getActionBar().setHomeButtonEnabled(true);
            getActionBar().setDisplayHomeAsUpEnabled(true);
        }

        mLocationClient = new LocationClient(this, this, this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mLocationClient.connect();
    }

    @Override
    protected void onStop() {
        mLocationClient.disconnect();
        super.onStop();
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
        if (mLocationClient.isConnected()) {
            DataManager.getInstance().updateLocation(mLocationClient.getLastLocation());
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Crouton.cancelAllCroutons();
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

    @Override
    public void onConnected(Bundle bundle) {
        DataManager.getInstance().updateLocation(mLocationClient.getLastLocation());
    }

    @Override
    public void onDisconnected() {
        // Do nothing
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // Do nothing
    }
}
