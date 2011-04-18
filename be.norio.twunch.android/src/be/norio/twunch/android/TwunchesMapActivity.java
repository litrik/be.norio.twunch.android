/**
 *	Copyright 2010-2011 Norio bvba
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

package be.norio.twunch.android;

import greendroid.app.GDMapActivity;
import greendroid.widget.ActionBarItem;
import greendroid.widget.ActionBarItem.Type;

import java.util.List;

import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.BaseColumns;

import com.cyrilmottier.android.greendroid.R;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import com.google.android.maps.Overlay;

public class TwunchesMapActivity extends GDMapActivity {

	MapView mapView;
	TwunchItemizedOverlay itemizedoverlay;
	MyLocationOverlay myLocationOverlay;

	private static String[] columns = new String[] { BaseColumns._ID, TwunchManager.COLUMN_LATITUDE,
			TwunchManager.COLUMN_LONGITUDE };
	private static final int COLUMN_DISPLAY_LATITUDE = 1;
	private static final int COLUMN_DISPLAY_LONGITUDE = 2;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		GoogleAnalyticsTracker.getInstance().start(TwunchApplication.getTrackerId(), 60, this);
		GoogleAnalyticsTracker.getInstance().trackPageView("TwunchesMap");

		mapView = new MapView(this, TwunchApplication.getMapsKey());
		mapView.setClickable(true);
		mapView.setBuiltInZoomControls(true);

		setActionBarContentView(mapView);
		addActionBarItem(Type.LocateMyself);

		DatabaseHelper dbHelper = new DatabaseHelper(this);
		Cursor cursor = dbHelper.getReadableDatabase().query(TwunchManager.TABLE_NAME, columns, null, null, null, null,
				TwunchManager.COLUMN_DATE + "," + TwunchManager.COLUMN_NUMPARTICIPANTS + " DESC");
		startManagingCursor(cursor);

		List<Overlay> mapOverlays = mapView.getOverlays();
		Drawable drawable = this.getResources().getDrawable(R.drawable.marker);
		itemizedoverlay = new TwunchItemizedOverlay(drawable, this);
		while (cursor.moveToNext()) {
			GeoPoint point = new GeoPoint(new Double(cursor.getFloat(COLUMN_DISPLAY_LATITUDE) * 1E6).intValue(), new Double(
					cursor.getFloat(COLUMN_DISPLAY_LONGITUDE) * 1E6).intValue());
			TwunchOverlayItem overlayitem = new TwunchOverlayItem(point, cursor.getInt(0));
			itemizedoverlay.addOverlay(overlayitem);
		}
		mapOverlays.add(itemizedoverlay);
		myLocationOverlay = new MyLocationOverlay(this, mapView);
		mapOverlays.add(myLocationOverlay);
		dbHelper.close();
		mapView.getController().zoomToSpan(itemizedoverlay.getLatSpanE6(), itemizedoverlay.getLonSpanE6());
		mapView.getController().animateTo(itemizedoverlay.getCenter());
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		GoogleAnalyticsTracker.getInstance().dispatch();
		GoogleAnalyticsTracker.getInstance().stop();
	}

	@Override
	public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {
		if (position == 0) {
			LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
			String lp = lm.getBestProvider(new Criteria(), true);
			if (lp != null) {
				Location lastKnownLocation = lm.getLastKnownLocation(lp);
				if (lastKnownLocation != null) {
					mapView.getController().animateTo(
							new GeoPoint(new Double(lastKnownLocation.getLatitude() * 1E6).intValue(), new Double(lastKnownLocation
									.getLongitude() * 1E6).intValue()));
					mapView.getController().setZoom(11);
					return true;
				}
			}
		}
		return false;
	}

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.google.android.maps.MapActivity#onPause()
	 */
	@Override
	protected void onPause() {
		super.onPause();
		myLocationOverlay.disableMyLocation();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.google.android.maps.MapActivity#onResume()
	 */
	@Override
	protected void onResume() {
		super.onResume();
		myLocationOverlay.enableMyLocation();
	}

}