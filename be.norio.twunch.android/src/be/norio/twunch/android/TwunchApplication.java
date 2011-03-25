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

import greendroid.app.GDApplication;

/**
 * 
 */
public class TwunchApplication extends GDApplication {

	private static boolean isInDevMode = true;

	public static String getTrackerId() {
		return isInDevMode ? "UA-1839065-14" : "UA-1839065-15";
	}

	public static final String LOG_TAG = "Twunch";

	@Override
	public Class<?> getHomeActivityClass() {
		return TwunchesActivity.class;
	}

}
