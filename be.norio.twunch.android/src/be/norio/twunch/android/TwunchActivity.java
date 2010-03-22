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

import java.util.regex.Pattern;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.text.util.Linkify;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import be.norio.twunch.android.core.Twunch;

public class TwunchActivity extends Activity {

	public static String PARAMETER_INDEX = "index";

	private Twunch twunch;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		twunch = ((TwunchApplication) getApplication()).getTwunches().get(getIntent().getIntExtra(PARAMETER_INDEX, 0));

		setContentView(R.layout.twunch);

		renderHeadline(twunch, findViewById(R.id.twunchHeadLine));
		((Button) findViewById(R.id.ButtonMap)).setText(twunch.getAddress());
		TextView participantsView = ((TextView) findViewById(R.id.twunchParticipants));
		participantsView.setText(twunch.getParticipants());
		Linkify.addLinks(participantsView, Pattern.compile("@([A-Za-z0-9-_]+)"), "http://twitter.com/");
		((Button) findViewById(R.id.ButtonMap)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				String uri = twunch.getMap();
				final Intent myIntent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uri));
				startActivity(myIntent);
			}
		});
		((Button) findViewById(R.id.ButtonJoin)).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				final Intent intent = new Intent(Intent.ACTION_SEND);
				intent.setType("text/plain");
				intent.putExtra(Intent.EXTRA_TEXT, "@twunch "
						+ String.format(getString(R.string.tweet), twunch.getTitle(), twunch.getLink()));
				startActivity(Intent.createChooser(intent, "Send tweet"));
			}
		});
	}

	static void renderHeadline(Twunch twunch, View view) {
		Context context = view.getContext();
		((TextView) view.findViewById(R.id.twunchTitle)).setText(twunch.getTitle());
		((TextView) view.findViewById(R.id.twunchDate)).setText(String.format(context.getString(R.string.date), DateUtils
				.formatDateTime(context, twunch.getDate().getTime(), DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_SHOW_DATE),
				DateUtils.formatDateTime(context, twunch.getDate().getTime(), DateUtils.FORMAT_SHOW_TIME)));
		StringBuffer extra = new StringBuffer();
		if (twunch.hasLatLon()) {
			LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
			String p = locationManager.getBestProvider(new Criteria(), true);
			if (p.length() > 0) {
				Location location = locationManager.getLastKnownLocation(p);
				float[] distance = new float[1];
				Location.distanceBetween(location.getLatitude(), location.getLongitude(), twunch.getLatitude(), twunch.getLongitude(),
						distance);
				extra.append(String.format(context.getString(R.string.distance), distance[0] / 1000));
				extra.append(" - ");
			}
		}
		extra.append(twunch.getNumberOfParticipants() == 1 ? context.getString(R.string.participants_one) : String.format(context
				.getString(R.string.participants), twunch.getNumberOfParticipants()));
		((TextView) view.findViewById(R.id.twunchExtra)).setText(extra);
	}
}
