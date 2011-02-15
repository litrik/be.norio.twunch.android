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

package be.norio.twunch.android.core;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.util.Xml;

/**
 * 
 */
public class TwunchParser {

	// Names of the XML tags
	static final String TWUNCH_ELEMENT = "twunch";
	static final String ID_ELEMENT = "id";
	static final String TITLE_ELEMENT = "title";
	static final String ADDRESS_ELEMENT = "address";
	static final String DATE_ELEMENT = "date";
	static final String LAT_ELEMENT = "latitude";
	static final String LON_ELEMENT = "longitude";
	static final String MAP_ELEMENT = "map";
	static final String LINK_ELEMENT = "link";
	static final String NOTE_ELEMENT = "note";
	static final String PARTICIPANT_ELEMENT = "participant";

	final URL feedUrl;

	public TwunchParser(String feedUrl) {
		try {
			this.feedUrl = new URL(feedUrl);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

	protected InputStream getInputStream() throws IOException {
		return feedUrl.openConnection().getInputStream();
	}

	public List<Twunch> parse() throws IOException, SAXException {
		TwunchHandler handler = new TwunchHandler();
		Xml.parse(this.getInputStream(), Xml.Encoding.UTF_8, handler);
		return handler.getTwunches();
	}

	public static class TwunchHandler extends DefaultHandler {
		private List<Twunch> twunches;
		private Twunch currentTwunch;
		private StringBuilder builder;
		private boolean doingHtml = false;

		public List<Twunch> getTwunches() {
			return this.twunches;
		}

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
			if (this.currentTwunch != null) {
				if (localName.equalsIgnoreCase(ID_ELEMENT)) {
					currentTwunch.setId(builder.toString());
				} else if (localName.equalsIgnoreCase(TITLE_ELEMENT)) {
					currentTwunch.setTitle(builder.toString());
				} else if (localName.equalsIgnoreCase(ADDRESS_ELEMENT)) {
					currentTwunch.setAddress(builder.toString());
				} else if (localName.equalsIgnoreCase(LAT_ELEMENT)) {
					currentTwunch.setLatitude(builder.toString());
				} else if (localName.equalsIgnoreCase(LON_ELEMENT)) {
					currentTwunch.setLongitude(builder.toString());
				} else if (localName.equalsIgnoreCase(DATE_ELEMENT)) {
					currentTwunch.setDate(builder.toString());
				} else if (localName.equalsIgnoreCase(LINK_ELEMENT)) {
					currentTwunch.setLink(builder.toString());
				} else if (localName.equalsIgnoreCase(MAP_ELEMENT)) {
					currentTwunch.setMap(builder.toString());
				} else if (localName.equalsIgnoreCase(NOTE_ELEMENT)) {
					currentTwunch.setNote(builder.toString());
					doingHtml = false;
				} else if (localName.equalsIgnoreCase(PARTICIPANT_ELEMENT)) {
					currentTwunch.addParticipant(builder.toString());
				} else if (localName.equalsIgnoreCase(TWUNCH_ELEMENT)) {
					twunches.add(currentTwunch);
				}
			}
		}

		@Override
		public void startDocument() throws SAXException {
			super.startDocument();
			twunches = new ArrayList<Twunch>();
			builder = new StringBuilder();
		}

		@Override
		public void endDocument() throws SAXException {
			Collections.sort(twunches);
		}

		@Override
		public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
			super.startElement(uri, localName, name, attributes);
			if (doingHtml) {
				return;
			}
			builder.setLength(0);
			if (localName.equalsIgnoreCase(TWUNCH_ELEMENT)) {
				this.currentTwunch = new Twunch();
			} else if (localName.equalsIgnoreCase(NOTE_ELEMENT)) {
				doingHtml = true;
			}
		}

	}
}
