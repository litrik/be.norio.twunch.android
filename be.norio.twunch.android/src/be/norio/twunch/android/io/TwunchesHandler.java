/*
 * Copyright 2011 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package be.norio.twunch.android.io;

import static org.xmlpull.v1.XmlPullParser.END_DOCUMENT;
import static org.xmlpull.v1.XmlPullParser.END_TAG;
import static org.xmlpull.v1.XmlPullParser.START_TAG;
import static org.xmlpull.v1.XmlPullParser.TEXT;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

import android.content.ContentProvider;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import be.norio.twunch.android.provider.TwunchContract;
import be.norio.twunch.android.provider.TwunchContract.Twunches;

import com.google.android.apps.iosched.io.XmlHandler;
import com.google.android.apps.iosched.util.Lists;

/**
 * Abstract class that handles reading and parsing an {@link XmlPullParser} into
 * a set of {@link ContentProviderOperation}. It catches recoverable network
 * exceptions and rethrows them as {@link HandlerException}. Any local
 * {@link ContentProvider} exceptions are considered unrecoverable.
 * <p>
 * This class is only designed to handle simple one-way synchronization.
 */
public class TwunchesHandler extends XmlHandler {

	final long timestamp = (new Date()).getTime();
	final SimpleDateFormat dateFormat;

	public TwunchesHandler() {
		super(TwunchContract.CONTENT_AUTHORITY);
		dateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss'Z'");
		dateFormat.setCalendar(Calendar.getInstance(TimeZone.getTimeZone("UTC")));
	}

	@Override
	public ArrayList<ContentProviderOperation> parse(XmlPullParser parser, ContentResolver resolver) throws XmlPullParserException,
			IOException {
		final ArrayList<ContentProviderOperation> batch = Lists.newArrayList();

		batch.add(ContentProviderOperation.newDelete(Twunches.CONTENT_URI).build());

		int type;
		while ((type = parser.next()) != END_DOCUMENT) {
			if (type == START_TAG && Tags.TWUNCH.equals(parser.getName())) {
				ContentProviderOperation operation = parseTwunch(parser, resolver);
				if (operation == null) {
					break;
				}
				batch.add(operation);
			}
		}

		return batch;
	}

	private ContentProviderOperation parseTwunch(XmlPullParser parser, ContentResolver resolver) throws XmlPullParserException,
			IOException {
		ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(Twunches.CONTENT_URI);

		final int depth = parser.getDepth();

		String participants = "";
		int numParticipants = 0;
		String tag = null;
		int type;
		while (((type = parser.next()) != END_TAG || parser.getDepth() > depth) && type != END_DOCUMENT) {
			if (type == START_TAG) {
				tag = parser.getName();
			} else if (type == END_TAG) {
				tag = null;
			} else if (type == TEXT) {
				final String text = parser.getText();
				if (Tags.ID.equals(tag)) {
					builder.withValue(Twunches.ID, text);
				} else if (Tags.TITLE.equals(tag)) {
					builder.withValue(Twunches.TITLE, text);
				} else if (Tags.ADDRESS.equals(tag)) {
					builder.withValue(Twunches.ADDRESS, text);
				} else if (Tags.NOTE.equals(tag)) {
					builder.withValue(Twunches.NOTE, text);
				} else if (Tags.DATE.equals(tag)) {
					try {
						builder.withValue(Twunches.DATE, dateFormat.parse(text).getTime());
					} catch (ParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else if (Tags.LINK.equals(tag)) {
					builder.withValue(Twunches.LINK, text);
				} else if (Tags.LAT.equals(tag)) {
					builder.withValue(Twunches.LATITUDE, text);
				} else if (Tags.LON.equals(tag)) {
					builder.withValue(Twunches.LONGITUDE, text);
				} else if (Tags.PARTICIPANT.equals(tag)) {
					numParticipants++;
					participants = participants + text + " ";
				}
			}
		}
		builder.withValue(Twunches.NUMPARTICIPANTS, numParticipants);
		builder.withValue(Twunches.PARTICIPANTS, participants);
		builder.withValue(Twunches.ADDED, timestamp);
		builder.withValue(Twunches.SYNCED, timestamp);
		return builder.build();
	}

	interface Tags {
		String TWUNCH = "twunch";
		String ID = "id";
		String TITLE = "title";
		String ADDRESS = "address";
		String DATE = "date";
		String LAT = "latitude";
		String LON = "longitude";
		String LINK = "link";
		String NOTE = "note";
		String NOTE_CLOSED = "closed";
		String PARTICIPANT = "participant";
	}

}