/**
 *	Copyright 2012 Norio bvba
 *
 *	Based on https://github.com/inazaruk/examples/blob/master/MapFragmentExample/src/com/inazaruk/example/MyMapFragment.java
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

import be.norio.twunch.android.ui.TwunchesMapActivity;
import be.norio.twunch.android.ui.fragment.map.ActivityHostFragment;
import android.app.Activity;

public class TwunchesMapFragment extends ActivityHostFragment {

	@Override
	protected Class<? extends Activity> getActivityClass() {
		return TwunchesMapActivity.class;
	}
}