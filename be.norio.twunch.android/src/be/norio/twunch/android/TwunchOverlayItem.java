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

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;

public class TwunchOverlayItem extends OverlayItem {

	private final int twunchId;

	public TwunchOverlayItem(GeoPoint point, int id) {
		super(point, "", "");
		twunchId = id;
	}

	/**
	 * @return the twunchId
	 */
	public int getTwunchId() {
		return twunchId;
	}

}
