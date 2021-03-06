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

package be.norio.twunch.android;

import android.app.Application;

import com.crashlytics.android.Crashlytics;

import be.norio.twunch.android.data.DataManager;
import be.norio.twunch.android.util.AvatarManager;
import be.norio.twunch.android.util.PrefsUtils;

/**
 * 
 */
public class TwunchApplication extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
        Crashlytics.start(this);
		PrefsUtils.initialize(this);
        DataManager.getInstance();
        AvatarManager.initialize(this);
	}

}
