/**
 *	Copyright 2011 Norio bvba
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

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;
import android.util.Xml;

public class TwunchManager {

	static final String TABLE_NAME = "twunches";

	static final String COLUMN_ID = "id";
	static final String COLUMN_ADDED = "added";
	static final String COLUMN_TITLE = "title";
	static final String COLUMN_ADDRESS = "address";
	static final String COLUMN_NOTE = "note";
	static final String COLUMN_DATE = "date";
	static final String COLUMN_LINK = "link";
	static final String COLUMN_LATITUDE = "latitude";
	static final String COLUMN_LONGITUDE = "longitude";
	static final String COLUMN_PARTICIPANTS = "participants";
	static final String COLUMN_NUMPARTICIPANTS = "numparticipants";

	protected TwunchManager() {
	}

	static private TwunchManager instance = null;

	static public TwunchManager getInstance() {
		if (null == instance) {
			instance = new TwunchManager();
		}
		return instance;
	}

	private class TwunchParser {

		// Names of the XML tags
		static final String TWUNCH_ELEMENT = "twunch";
		static final String ID_ELEMENT = "id";
		static final String TITLE_ELEMENT = "title";
		static final String ADDRESS_ELEMENT = "address";
		static final String DATE_ELEMENT = "date";
		static final String LAT_ELEMENT = "latitude";
		static final String LON_ELEMENT = "longitude";
		static final String LINK_ELEMENT = "link";
		static final String NOTE_ELEMENT = "note";
		static final String PARTICIPANT_ELEMENT = "participant";

		final URL feedUrl;
		final Context context;
		final SimpleDateFormat dateFormat;
		final long timestamp = (new Date()).getTime();
		private SQLiteDatabase db;

		public TwunchParser(Context context, String feedUrl) {
			try {
				this.context = context;
				this.feedUrl = new URL(feedUrl);
				dateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
				dateFormat.setCalendar(Calendar.getInstance(TimeZone.getTimeZone("UTC")));
			} catch (MalformedURLException e) {
				throw new RuntimeException(e);
			}
		}

		protected InputStream getInputStream() throws IOException {
			return feedUrl.openConnection().getInputStream();
		}

		public void parse() throws IOException, SAXException {
			db = dbHelper.getWritableDatabase();
			TwunchHandler handler = new TwunchHandler();
			Xml.parse(this.getInputStream(), Xml.Encoding.UTF_8, handler);
			// TODO: Delete old twunches
			dbHelper.close();
		}

		public class TwunchHandler extends DefaultHandler {
			private ContentValues values;
			private StringBuffer participants;
			private int numParticipants;
			private StringBuilder builder;
			private boolean doingHtml = false;

			@Override
			public void characters(char[] ch, int start, int length) throws SAXException {
				super.characters(ch, start, length);
				builder.append(ch, start, length);
			}

			@Override
			public void endElement(String uri, String localName, String name) throws SAXException {
				super.endElement(uri, localName, name);
				if (doingHtml && !localName.equalsIgnoreCase(NOTE_ELEMENT)) {
					builder.append(' ');
					return;
				}
				if (values != null) {
					if (localName.equalsIgnoreCase(ID_ELEMENT)) {
						values.put(COLUMN_ID, builder.toString());
					} else if (localName.equalsIgnoreCase(TITLE_ELEMENT)) {
						values.put(COLUMN_TITLE, builder.toString());
					} else if (localName.equalsIgnoreCase(ADDRESS_ELEMENT)) {
						values.put(COLUMN_ADDRESS, builder.toString());
					} else if (localName.equalsIgnoreCase(LAT_ELEMENT)) {
						values.put(COLUMN_LATITUDE, builder.toString());
					} else if (localName.equalsIgnoreCase(LON_ELEMENT)) {
						values.put(COLUMN_LONGITUDE, builder.toString());
					} else if (localName.equalsIgnoreCase(DATE_ELEMENT)) {
						try {
							values.put(COLUMN_DATE, dateFormat.parse(builder.toString()).getTime());
						} catch (ParseException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else if (localName.equalsIgnoreCase(LINK_ELEMENT)) {
						values.put(COLUMN_LINK, builder.toString());
					} else if (localName.equalsIgnoreCase(NOTE_ELEMENT)) {
						values.put(COLUMN_NOTE, builder.toString());
						doingHtml = false;
					} else if (localName.equalsIgnoreCase(PARTICIPANT_ELEMENT)) {
						participants.append('@');
						participants.append(builder);
						participants.append(' ');
						numParticipants++;
					} else if (localName.equalsIgnoreCase(TWUNCH_ELEMENT)) {
						values.put(COLUMN_PARTICIPANTS, participants.toString());
						values.put(COLUMN_NUMPARTICIPANTS, numParticipants);
						Log.d(TwunchApplication.LOG_TAG, "Inserting twunch " + values.getAsString(COLUMN_ID));
						if (db.insert(TABLE_NAME, null, values) == -1) {
							Log.d(TwunchApplication.LOG_TAG,
									"Insert failed. Instead trying update of twunch " + values.getAsString(COLUMN_ID));
							db.update(TABLE_NAME, values, COLUMN_ID + " = '" + values.getAsString(COLUMN_ID) + "'", null);
						}
					}
				}
			}

			@Override
			public void startDocument() throws SAXException {
				super.startDocument();
				builder = new StringBuilder();
			}

			@Override
			public void endDocument() throws SAXException {
			}

			@Override
			public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
				super.startElement(uri, localName, name, attributes);
				if (doingHtml) {
					return;
				}
				builder.setLength(0);
				if (localName.equalsIgnoreCase(TWUNCH_ELEMENT)) {
					values = new ContentValues();
					participants = new StringBuffer();
					numParticipants = 0;
					values.put(COLUMN_ADDED, timestamp);
				} else if (localName.equalsIgnoreCase(NOTE_ELEMENT)) {
					doingHtml = true;
				}
			}

		}
	}

	private DatabaseHelper dbHelper;

	public void loadTwunches(Context context) throws Exception {
		// FIXME: Prevent multiple simultaneous downloads
		dbHelper = new DatabaseHelper(context);
		TwunchParser tp = new TwunchParser(context, "http://twunch.be/events.xml?when=future");
		tp.parse();
	}

	public String getDistanceToTwunch(Context context, double lat, double lon) {
		LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
		String p = locationManager.getBestProvider(new Criteria(), true);
		if (p != null && p.length() > 0) {
			Location location = locationManager.getLastKnownLocation(p);
			if (location != null) {
				float[] distance = new float[1];
				Location.distanceBetween(location.getLatitude(), location.getLongitude(), lat, lon, distance);
				return String.format(context.getString(R.string.distance), distance[0] / 1000);
			}
		}
		return null;
	}

}