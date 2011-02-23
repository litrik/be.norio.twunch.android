/**
 *	Copyright 2010 Norio bvba
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

import java.util.List;

import be.norio.twunch.android.core.Twunch;
import be.norio.twunch.android.core.TwunchParser;

/**
 * 
 */
public class TwunchApplication extends GDApplication {

	// public static final String TRACKER_ID = "UA-1839065-15"; // PRD
	public static final String TRACKER_ID = "UA-1839065-14"; // DEV

	private List<Twunch> twunches = null;

	public void loadTwunches() throws Exception {
		TwunchParser tp = new TwunchParser("http://twunch.be/events.xml?when=future");
		twunches = tp.parse();
	}

	public List<Twunch> getTwunchList() {
		return twunches;
	}

	public boolean isTwunchListCurrent() {
		return twunches != null;
	}

	@Override
	public Class<?> getHomeActivityClass() {
		return TwunchesActivity.class;
	}

}
