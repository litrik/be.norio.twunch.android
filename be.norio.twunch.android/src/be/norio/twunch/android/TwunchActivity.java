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

import greendroid.app.GDActivity;
import greendroid.widget.ActionBarItem;
import greendroid.widget.ActionBarItem.Type;

import java.util.regex.Pattern;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.text.util.Linkify;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import be.norio.twunch.android.core.Twunch;

import com.cyrilmottier.android.greendroid.R;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

public class TwunchActivity extends GDActivity {

	public static String PARAMETER_INDEX = "index";

	private final static int MENU_MAP = 0;
	private final static int MENU_REGISTER = 1;
	private final static int MENU_SHARE = 2;

	private Twunch twunch;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		GoogleAnalyticsTracker.getInstance().trackPageView("Twunch");
		setActionBarContentView(R.layout.twunch);
		addActionBarItem(Type.Add);
		addActionBarItem(Type.Share);
		addActionBarItem(Type.Locate);

		twunch = TwunchManager.getInstance().getTwunchList().get(getIntent().getIntExtra(PARAMETER_INDEX, 0));
		((TextView) findViewById(R.id.twunchTitle)).setText(twunch.getTitle());
		((TextView) findViewById(R.id.twunchDate)).setText(String.format(getString(R.string.date),
				DateUtils.formatDateTime(this, twunch.getDate().getTime(), DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_SHOW_DATE),
				DateUtils.formatDateTime(this, twunch.getDate().getTime(), DateUtils.FORMAT_SHOW_TIME)));
		((TextView) findViewById(R.id.twunchNumberParticipants)).setText(String.format(
				getResources().getQuantityString(R.plurals.numberOfParticipants, twunch.getNumberOfParticipants()),
				twunch.getNumberOfParticipants()));
		TextView noteView = ((TextView) findViewById(R.id.twunchNote));
		if (twunch.getNote().length() > 0) {
			noteView.setText(twunch.getNote());
			noteView.setVisibility(View.VISIBLE);
		} else {
			noteView.setVisibility(View.GONE);
		}
		StringBuffer address = new StringBuffer();
		String distance = TwunchManager.getInstance().getDistanceToTwunch(this, twunch);
		if (distance != null) {
			address.append(distance);
			address.append(" - ");
		}
		address.append(twunch.getAddress());
		((TextView) findViewById(R.id.twunchAddress)).setText(address);
		TextView participantsView = ((TextView) findViewById(R.id.twunchParticipants));
		participantsView.setText(twunch.getParticipants());
		Linkify.addLinks(participantsView, Pattern.compile("@([A-Za-z0-9-_]+)"), "http://twitter.com/");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreateOptionsMenu(android.view.Menu)
	 */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_REGISTER, 0, R.string.button_register).setIcon(R.drawable.ic_menu_add);
		menu.add(0, MENU_SHARE, 0, R.string.menu_share).setIcon(R.drawable.ic_menu_share);
		menu.add(0, MENU_MAP, 0, R.string.button_map).setIcon(R.drawable.ic_menu_mapmode);
		return super.onCreateOptionsMenu(menu);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onOptionsItemSelected(android.view.MenuItem)
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case MENU_MAP:
			doMap();
			return true;
		case MENU_REGISTER:
			doRegister();
			return true;
		case MENU_SHARE:
			doShare();
			return true;
		}
		return false;
	}

	/**
	 * Show the location of this Twunch on a map.
	 */
	private void doMap() {
		final Intent myIntent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?q="
				+ twunch.getLatitude() + "," + twunch.getLongitude()));
		startActivity(myIntent);
	}

	/**
	 * Register for this Twunch.
	 */
	private void doRegister() {
		final Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_TEXT, String.format(getString(R.string.register_text), twunch.getTitle(), twunch.getLink()));
		startActivity(Intent.createChooser(intent, getString(R.string.register_title)));
	}

	/**
	 * Share information about this Twunch.
	 */
	private void doShare() {
		final Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.putExtra(
				Intent.EXTRA_TEXT,
				String.format(
						getString(R.string.share_text),
						twunch.getTitle(),
						DateUtils.formatDateTime(this, twunch.getDate().getTime(), DateUtils.FORMAT_SHOW_WEEKDAY
								| DateUtils.FORMAT_SHOW_DATE),
						DateUtils.formatDateTime(this, twunch.getDate().getTime(), DateUtils.FORMAT_SHOW_TIME), twunch.getLink()));
		intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_subject));
		startActivity(Intent.createChooser(intent, getString(R.string.share_title)));
	}

	@Override
	public boolean onHandleActionBarItemClick(ActionBarItem item, int position) {
		switch (position) {
		case 0:
			doRegister();
			break;
		case 1:
			doShare();
			break;
		case 2:
			doMap();
			break;
		default:
			return false;
		}
		return true;
	}

}
