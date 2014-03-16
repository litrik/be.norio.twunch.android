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

import android.text.format.DateUtils;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.squareup.otto.Subscribe;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import be.norio.twunch.android.R;
import be.norio.twunch.android.data.DataManager;
import be.norio.twunch.android.data.model.Twunch;
import be.norio.twunch.android.otto.BusProvider;
import be.norio.twunch.android.otto.TwunchClickedEvent;
import be.norio.twunch.android.otto.TwunchesAvailableEvent;
import hugo.weaving.DebugLog;

public class MapFragment extends com.google.android.gms.maps.MapFragment implements OnInfoWindowClickListener {

    Map<Marker, Twunch> mMarkers = new HashMap<Marker, Twunch>();
    private GoogleMap mMap;

    @Override
    public void onPause() {
        super.onPause();
        BusProvider.getInstance().unregister(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        BusProvider.getInstance().register(this);
        setUpMapIfNeeded();
    }

    private void setUpMapIfNeeded() {
        if (mMap == null) {
            mMap = getMap();
            if (mMap != null) {
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(50.85, 4.35), 6));
                mMap.setMyLocationEnabled(true);
                mMap.setOnInfoWindowClickListener(this);
                showMarkers();
            }
        } else {
            showMarkers();
        }
    }

    public void showMarkers() {
        final List<Twunch> twunches = DataManager.getInstance().getTwunches();
        mMap.clear();
        mMarkers.clear();
        if (twunches.size() == 0) {
            return;
        }
        final LatLngBounds.Builder builder = LatLngBounds.builder();
        for (int i = 0; i < twunches.size(); i++) {
            Twunch twunch = twunches.get(i);

            final double lat = twunch.getLatitude();
            final double lon = twunch.getLongitude();
            if (lat != 0 && lon != 0) {
                final LatLng latLng = new LatLng(lat, lon);
                final Marker marker = mMap.addMarker(new MarkerOptions()
                        .position(latLng)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker))
                        .title(twunch.getTitle())
                        .snippet(
                                String.format(getString(R.string.date), DateUtils.formatDateTime(getActivity(),
                                        twunch.getDate(), DateUtils.FORMAT_SHOW_WEEKDAY
                                                | DateUtils.FORMAT_SHOW_DATE
                                ), DateUtils.formatDateTime(getActivity(),
                                        twunch.getDate(), DateUtils.FORMAT_SHOW_TIME))
                        ));
                mMarkers.put(marker, twunch);
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
    public void onInfoWindowClick(Marker marker) {
        BusProvider.getInstance().post(new TwunchClickedEvent(mMarkers.get(marker)));
    }

    @Subscribe
    @DebugLog
    public void onTwunchesAvailableEvent(TwunchesAvailableEvent event) {
        setUpMapIfNeeded();
    }
}