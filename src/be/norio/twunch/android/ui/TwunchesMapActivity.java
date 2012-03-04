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
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;

public class TwunchesMapActivity extends SherlockMapActivity {

	Cursor cursor;

	MapView mapView;
	TwunchItemizedOverlay itemizedoverlay;
	MyLocationOverlay myLocationOverlay;

	private static String[] columns = new String[] { BaseColumns._ID, Twunches.LATITUDE, Twunches.LONGITUDE };
	private static final int COLUMN_DISPLAY_LATITUDE = 1;
	private static final int COLUMN_DISPLAY_LONGITUDE = 2;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		AnalyticsUtils.trackPageView(AnalyticsUtils.Pages.TWUNCH_MAP);

		mapView = new MapView(this, BuildProperties.MAPS_KEY);
		mapView.setClickable(true);
		mapView.setBuiltInZoomControls(true);

		setContentView(mapView);

		cursor = getContentResolver().query(Twunches.buildFutureTwunchesUri(), columns, null, null, null);
		startManagingCursor(cursor);

		List<Overlay> mapOverlays = mapView.getOverlays();
		Drawable drawable = this.getResources().getDrawable(R.drawable.marker);
		itemizedoverlay = new TwunchItemizedOverlay(drawable, this);
		while (cursor.moveToNext()) {
			if (cursor.getFloat(COLUMN_DISPLAY_LATITUDE) != 0 && cursor.getFloat(COLUMN_DISPLAY_LONGITUDE) != 0) {
				System.out.println(cursor.getFloat(COLUMN_DISPLAY_LATITUDE) + ":" + cursor.getFloat(COLUMN_DISPLAY_LONGITUDE));
				GeoPoint point = new GeoPoint(new Double(cursor.getFloat(COLUMN_DISPLAY_LATITUDE) * 1E6).intValue(), new Double(
						cursor.getFloat(COLUMN_DISPLAY_LONGITUDE) * 1E6).intValue());
				TwunchOverlayItem overlayitem = new TwunchOverlayItem(point, cursor.getInt(0));
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

}