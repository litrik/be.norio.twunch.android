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

package be.norio.twunch.android.ui;

import android.os.Bundle;
import be.norio.twunch.android.ui.fragment.TwunchDetailsFragment;
import be.norio.twunch.android.util.FragmentUtils;

public class TwunchDetailsActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		if (getSupportFragmentManager().findFragmentById(android.R.id.content) == null) {
			TwunchDetailsFragment fragment = new TwunchDetailsFragment();
			fragment.setArguments(FragmentUtils.intentToFragmentArguments(getIntent()));
			getSupportFragmentManager().beginTransaction().add(android.R.id.content, fragment).commit();
		}
	}

}
