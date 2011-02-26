/**
 *	Copyright 2011 Norio bvba
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

import java.util.List;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import be.norio.twunch.android.core.Twunch;
import be.norio.twunch.android.core.TwunchParser;

public class TwunchManager {

	protected TwunchManager() {
		// Do nothing
	}

	static private TwunchManager instance = null;

	static public TwunchManager getInstance() {
		if (null == instance) {
			instance = new TwunchManager();
		}
		return instance;
	}

	private List<Twunch> twunches = null;

	public void loadTwunches() throws Exception {
		// TODO: Prevent multiple simultaneous downloads
		TwunchParser tp = new TwunchParser("http://twunch.be/events.xml?when=future");
		twunches = tp.parse();
	}

	public List<Twunch> getTwunchList() {
		return twunches;
	}

	public boolean isTwunchListCurrent() {
		return twunches != null;
	}

	public String getDistanceToTwunch(Context context, Twunch twunch) {
		if (twunch.hasLatLon()) {
			LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
			String p = locationManager.getBestProvider(new Criteria(), true);
			if (p != null && p.length() > 0) {
				Location location = locationManager.getLastKnownLocation(p);
				if (location != null) {
					float[] distance = new float[1];
					Location.distanceBetween(location.getLatitude(), location.getLongitude(), twunch.getLatitude(),
							twunch.getLongitude(), distance);
					return String.format(context.getString(R.string.distance), distance[0] / 1000);
				}
			}
		}
		return null;
	}

}