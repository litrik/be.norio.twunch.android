/**
 *	Copyright 2010-2012 Norio bvba
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

import java.util.List;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.BaseColumns;
import be.norio.twunch.android.BuildProperties;
import be.norio.twunch.android.R;
import be.norio.twunch.android.provider.TwunchContract.Twunches;
import be.norio.twunch.android.util.AnalyticsUtils;
import be.norio.twunch.android.util.TwunchItemizedOverlay;
import be.norio.twunch.android.util.TwunchOverlayItem;

import com.actionbarsherlock.app.SherlockMapActivity;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;

public class TwunchesMapActivity extends SherlockMapActivity {

	Cursor mCursor;

	MapView mapView;
	TwunchItemizedOverlay itemizedoverlay;
	MyLocationOverlay myLocationOverlay;

	private interface TwunchesQuery {
		int _TOKEN = 0x1;

		String[] PROJECTION = { BaseColumns._ID, Twunches.LATITUDE, Twunches.LONGITUDE };

		int _ID = 0;
		int LATITUDE = 1;
		int LONGITUDE = 2;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		AnalyticsUtils.trackPageView(AnalyticsUtils.Pages.TWUNCH_MAP);

		mapView = new MapView(this, BuildProperties.MAPS_KEY);
		mapView.setClickable(true);
		mapView.setBuiltInZoomControls(true);

		setContentView(mapView);

		mCursor = managedQuery(Twunches.buildFutureTwunchesUri(), TwunchesQuery.PROJECTION, null, null, null);

		List<Overlay> mapOverlays = mapView.getOverlays();
		Drawable drawable = this.getResources().getDrawable(R.drawable.marker);
		itemizedoverlay = new TwunchItemizedOverlay(drawable, this);
		while (mCursor.moveToNext()) {
			if (mCursor.getFloat(TwunchesQuery.LATITUDE) != 0 && mCursor.getFloat(TwunchesQuery.LONGITUDE) != 0) {
				System.out.println(mCursor.getFloat(TwunchesQuery.LATITUDE) + ":" + mCursor.getFloat(TwunchesQuery.LONGITUDE));
				GeoPoint point = new GeoPoint(new Double(mCursor.getFloat(TwunchesQuery.LATITUDE) * 1E6).intValue(), new Double(
						mCursor.getFloat(TwunchesQuery.LONGITUDE) * 1E6).intValue());
				TwunchOverlayItem overlayitem = new TwunchOverlayItem(point, mCursor.getInt(0));
				itemizedoverlay.addOverlay(overlayitem);
			}
		}
		mapOverlays.add(itemizedoverlay);
		myLocationOverlay = new MyLocationOverlay(this, mapView);
		mapOverlays.add(myLocationOverlay);
		mapView.getController().zoomToSpan(itemizedoverlay.getLatSpanE6(), itemizedoverlay.getLonSpanE6());
		mapView.getController().animateTo(itemizedoverlay.getCenter());

		getSupportActionBar().setHomeButtonEnabled(true);
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	@Override
	protected void onPause() {
		super.onPause();
		myLocationOverlay.disableMyLocation();
	}

	@Override
	protected void onResume() {
		super.onResume();
		myLocationOverlay.enableMyLocation();
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
		Intent intent = new Intent(this, TwunchListActivity.class);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		startActivity(intent);
	}
}