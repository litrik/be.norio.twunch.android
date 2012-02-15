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

import android.app.Application;
import be.norio.twunch.android.util.PrefsUtils;

/**
 * 
 */
public class TwunchApplication extends Application {

	private static boolean isInDevMode = true;

	public static String getTrackerId() {
		return isInDevMode ? "UA-1839065-14" : "UA-1839065-15";
	}

	public static String getMapsKey() {
		return isInDevMode ? "0im5xQjfO1W_jbSxXP79PDw_m5fCCruNE-rtiow" : "0im5xQjfO1W_YsqVbJtjY6M_I8pYmdza3gkoe5Q";
	}

	public static final String LOG_TAG = "Twunch";

	@Override
	public void onCreate() {
		super.onCreate();
		PrefsUtils.initialize(this);
	}

}
