/**
 *	Copyright 2012 Norio bvba
 *
 *	Based on https://github.com/inazaruk/examples/blob/master/MapFragmentExample/src/com/inazaruk/example/LocalActivityManagerFragment.java
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

import android.app.LocalActivityManager;
import android.os.Bundle;

import com.actionbarsherlock.app.SherlockFragment;

@SuppressWarnings("deprecation")
public class LocalActivityManagerFragment extends SherlockFragment {

	private static final String KEY_STATE_BUNDLE = "localActivityManagerState";

	private LocalActivityManager mLocalActivityManager;

	protected LocalActivityManager getLocalActivityManager() {
		return mLocalActivityManager;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle state = null;
		if (savedInstanceState != null) {
			state = savedInstanceState.getBundle(KEY_STATE_BUNDLE);
		}

		mLocalActivityManager = new LocalActivityManager(getActivity(), true);
		mLocalActivityManager.dispatchCreate(state);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putBundle(KEY_STATE_BUNDLE, mLocalActivityManager.saveInstanceState());
	}

	@Override
	public void onResume() {
		super.onResume();
		mLocalActivityManager.dispatchResume();
	}

	@Override
	public void onPause() {
		super.onPause();
		mLocalActivityManager.dispatchPause(getActivity().isFinishing());
	}

	@Override
	public void onStop() {
		super.onStop();
		mLocalActivityManager.dispatchStop();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mLocalActivityManager.dispatchDestroy(getActivity().isFinishing());
	}
}