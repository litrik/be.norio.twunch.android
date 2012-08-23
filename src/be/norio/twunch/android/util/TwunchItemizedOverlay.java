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

package be.norio.twunch.android.util;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import be.norio.twunch.android.ui.TwunchDetailsActivity;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.readystatesoftware.mapviewballoons.BalloonItemizedOverlay;

public class TwunchItemizedOverlay extends BalloonItemizedOverlay<TwunchOverlayItem> {

	private final ArrayList<TwunchOverlayItem> items = new ArrayList<TwunchOverlayItem>();
	private final Context context;

	public TwunchItemizedOverlay(MapView mapView, Drawable defaultMarker, Context context) {
		super(boundCenter(defaultMarker), mapView);
		this.context = context;
	}

	@Override
	protected TwunchOverlayItem createItem(int i) {
		return items.get(i);
	}

	public void addOverlay(TwunchOverlayItem overlay) {
		items.add(overlay);
		populate();
	}

	@Override
	public int size() {
		return items.size();
	}

	@Override
	protected boolean onBalloonTap(int index, TwunchOverlayItem item) {
		Intent intent = new Intent(context, TwunchDetailsActivity.class);
		intent.setData(item.getUri());
		context.startActivity(intent);
		return true;
	}

	@Override
	public GeoPoint getCenter() {
		int minLat = Integer.MAX_VALUE;
		int maxLat = Integer.MIN_VALUE;
		int minLon = Integer.MAX_VALUE;
		int maxLon = Integer.MIN_VALUE;
		for (TwunchOverlayItem item : items) {
			int lat = item.getPoint().getLatitudeE6();
			int lon = item.getPoint().getLongitudeE6();
			maxLat = Math.max(lat, maxLat);
			minLat = Math.min(lat, minLat);
			maxLon = Math.max(lon, maxLon);
			minLon = Math.min(lon, minLon);
		}
		return new GeoPoint((maxLat + minLat) / 2, (maxLon + minLon) / 2);
	}

}
