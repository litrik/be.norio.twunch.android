package be.norio.twunch.android.util;

import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;

public class Util {
	public static Float getDistanceToTwunch(Context context, double lat, double lon) {
		if (lat == 0 && lon == 0) {
			return null;
		}
		LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		String p = locationManager.getBestProvider(new Criteria(), true);
		if (p != null && p.length() > 0) {
			Location location = locationManager.getLastKnownLocation(p);
			if (location != null) {
				float[] distance = new float[1];
				Location.distanceBetween(location.getLatitude(), location.getLongitude(), lat, lon, distance);
				return (distance[0] / 1000);
			}
		}
		return null;
	}
}
