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

package be.norio.twunch.android.ui.fragment;

import java.util.HashMap;
import java.util.Map;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.format.DateUtils;
import be.norio.twunch.android.R;
import be.norio.twunch.android.otto.BusProvider;
import be.norio.twunch.android.otto.OnTwunchClickedEvent;
import be.norio.twunch.android.provider.TwunchContract.Twunches;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class TwunchMapFragment extends SupportMapFragment implements LoaderManager.LoaderCallbacks<Cursor>,
		OnInfoWindowClickListener {

	Map<Marker, Uri> mMarkers = new HashMap<Marker, Uri>();
	private GoogleMap mMap;

	@Override
	public void onResume() {
		super.onResume();
		setUpMapIfNeeded();
	}

	private void setUpMapIfNeeded() {
		if (mMap == null) {
			mMap = getMap();
			if (mMap != null) {
				mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(50.85, 4.35), 6));
				mMap.setMyLocationEnabled(true);
				mMap.setOnInfoWindowClickListener(this);
				getLoaderManager().restartLoader(Twunches.Query._TOKEN, getArguments(), this);
			}
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(getActivity(), Twunches.buildFutureTwunchesUri(), Twunches.Query.PROJECTION, null, null,
				Twunches.SORT_DATE);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		mMap.clear();
		mMarkers.clear();
		if (cursor.getCount() == 0) {
			return;
		}
		final LatLngBounds.Builder builder = LatLngBounds.builder();
		while (cursor.moveToNext()) {
			final float lat = cursor.getFloat(Twunches.Query.LATITUDE);
			final float lon = cursor.getFloat(Twunches.Query.LONGITUDE);
			if (lat != 0 && lon != 0) {
				final LatLng latLng = new LatLng(lat, lon);
				final Marker marker = mMap.addMarker(new MarkerOptions()
						.position(latLng)
						.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker))
						.title(cursor.getString(Twunches.Query.NAME))
						.snippet(
								String.format(getString(R.string.date), DateUtils.formatDateTime(getActivity(),
										cursor.getLong(Twunches.Query.DATE), DateUtils.FORMAT_SHOW_WEEKDAY
												| DateUtils.FORMAT_SHOW_DATE), DateUtils.formatDateTime(getActivity(),
										cursor.getLong(Twunches.Query.DATE), DateUtils.FORMAT_SHOW_TIME))));
				mMarkers.put(marker, Twunches.buildTwunchUri(Integer.toString(cursor.getInt(Twunches.Query._ID))));
				builder.include(latLng);
			}
		}
		// http://stackoverflow.com/questions/14428766/at-what-time-in-the-application-lifecycle-can-should-you-use-layout-measurements
		getView().post(new Runnable() {

			@Override
			public void run() {
				CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(builder.build(),
						getResources().getDimensionPixelSize(R.dimen.map_padding));
				mMap.animateCamera(cu);
			}
		});

	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		mMap.clear();
	}

	@Override
	public void onInfoWindowClick(Marker marker) {
		BusProvider.getInstance().post(new OnTwunchClickedEvent(mMarkers.get(marker)));
	}
}