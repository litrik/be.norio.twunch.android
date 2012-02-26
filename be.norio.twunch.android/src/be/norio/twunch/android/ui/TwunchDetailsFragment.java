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

import java.util.Arrays;
import java.util.Date;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Nickname;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.QuickContact;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Html;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;
import be.norio.twunch.android.R;
import be.norio.twunch.android.provider.TwunchContract.Twunches;
import be.norio.twunch.android.util.FragmentUtils;
import be.norio.twunch.android.util.Util;

import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class TwunchDetailsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

	Cursor mCursor;
	String[] participants;
	Float distance;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_twunch_details, null);
		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		getLoaderManager().initLoader(TwunchDetailsQuery._TOKEN, getArguments(), this);
	}

	private interface TwunchDetailsQuery {
		int _TOKEN = 0x1;

		String[] PROJECTION = { BaseColumns._ID, Twunches.TITLE, Twunches.ADDRESS, Twunches.DATE, Twunches.NUMPARTICIPANTS,
				Twunches.LATITUDE, Twunches.LONGITUDE, Twunches.PARTICIPANTS, Twunches.NOTE, Twunches.LINK, Twunches.CLOSED };

		int _ID = 0;
		int TITLE = 1;
		int ADDRESS = 2;
		int DATE = 3;
		int NUMPARTICIPANTS = 4;
		int LATITUDE = 5;
		int LONGITUDE = 6;
		int PARTICIPANTS = 7;
		int NOTE = 8;
		int LINK = 9;
		int CLOSED = 10;

	}

	@Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.twunch, menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menuMap:
			doMap();
			return true;
		case R.id.menuRegister:
			doRegister();
			return true;
		case R.id.menuShare:
			doShare();
			return true;
		case R.id.menuDirections:
			doDirections();
			return true;
		}
		return false;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(getActivity(), (Uri) args.getParcelable(FragmentUtils.ARGUMENT_URI), TwunchDetailsQuery.PROJECTION,
				null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		if (!cursor.moveToFirst()) {
			return;
		}

		mCursor = cursor;
		// Title
		((TextView) getView().findViewById(R.id.twunchTitle)).setText(cursor.getString(TwunchDetailsQuery.TITLE));
		// Address
		((TextView) getView().findViewById(R.id.twunchAddress)).setText(cursor.getString(TwunchDetailsQuery.ADDRESS));
		// Distance
		distance = Util.getDistanceToTwunch(getActivity(), cursor.getFloat(TwunchDetailsQuery.LATITUDE),
				cursor.getFloat(TwunchDetailsQuery.LONGITUDE));
		((TextView) getView().findViewById(R.id.twunchDistance)).setText(String.format(getString(R.string.distance), distance));
		getView().findViewById(R.id.twunchDistance).setVisibility(distance == null ? View.GONE : View.VISIBLE);
		getView().findViewById(R.id.twunchDistanceSeparator).setVisibility(distance == null ? View.GONE : View.VISIBLE);
		// Date
		((TextView) getView().findViewById(R.id.twunchDate)).setText(String.format(
				getString(R.string.date),
				DateUtils.formatDateTime(getActivity(), cursor.getLong(TwunchDetailsQuery.DATE), DateUtils.FORMAT_SHOW_WEEKDAY
						| DateUtils.FORMAT_SHOW_DATE),
				DateUtils.formatDateTime(getActivity(), cursor.getLong(TwunchDetailsQuery.DATE), DateUtils.FORMAT_SHOW_TIME)));
		// Days
		final long msInDay = 86400000;
		int days = (int) (cursor.getLong(TwunchDetailsQuery.DATE) / msInDay - new Date().getTime() / msInDay);
		((TextView) getView().findViewById(R.id.twunchDays)).setText(days == 0 ? getString(R.string.today) : String.format(
				getResources().getQuantityString(R.plurals.days_to_twunch, days), days));
		// Note
		TextView noteView = ((TextView) getView().findViewById(R.id.twunchNote));
		if (cursor.getString(TwunchDetailsQuery.NOTE) == null || cursor.getString(TwunchDetailsQuery.NOTE).length() == 0) {
			noteView.setVisibility(View.GONE);
		} else {
			noteView.setMovementMethod(LinkMovementMethod.getInstance());
			noteView.setText(Html.fromHtml(cursor.getString(TwunchDetailsQuery.NOTE)));
			noteView.setVisibility(View.VISIBLE);
		}
		// Number of participants
		((TextView) getView().findViewById(R.id.twunchNumberParticipants)).setText(String.format(
				getResources().getQuantityString(R.plurals.numberOfParticipants, cursor.getInt(TwunchDetailsQuery.NUMPARTICIPANTS)),
				cursor.getInt(TwunchDetailsQuery.NUMPARTICIPANTS)));
		// Participants
		GridView participantsView = ((GridView) getView().findViewById(R.id.twunchParticipants));
		participants = cursor.getString(TwunchDetailsQuery.PARTICIPANTS).split(" ");
		Arrays.sort(participants, String.CASE_INSENSITIVE_ORDER);
		participantsView.setAdapter(new ContactAdapter(getActivity()));
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mCursor = null;
	}

	/**
	 * Show the location of this Twunch on a map.
	 */
	private void doMap() {
		final Intent myIntent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?q="
				+ mCursor.getDouble(TwunchDetailsQuery.LATITUDE) + "," + mCursor.getDouble(TwunchDetailsQuery.LONGITUDE)));
		startActivity(myIntent);
	}

	/**
	 * Show the directions to this Twunch.
	 */
	private void doDirections() {
		startActivity(new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("google.navigation:q="
				+ mCursor.getDouble(TwunchDetailsQuery.LATITUDE) + "," + mCursor.getDouble(TwunchDetailsQuery.LONGITUDE))));
	}

	/**
	 * Register for this Twunch.
	 */
	private void doRegister() {
		if (mCursor.getInt(TwunchDetailsQuery.CLOSED) == 1) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setMessage(R.string.register_closed);
			builder.setCancelable(false);
			builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int id) {
					// Do nothing
				}
			});
			builder.create().show();

		} else {
			final Intent intent = new Intent(Intent.ACTION_SEND);
			intent.setType("text/plain");
			intent.putExtra(
					Intent.EXTRA_TEXT,
					String.format(getString(R.string.register_text), mCursor.getString(TwunchDetailsQuery.TITLE),
							mCursor.getString(TwunchDetailsQuery.LINK)));
			startActivity(Intent.createChooser(intent, getString(R.string.register_title)));

		}
	}

	/**
	 * Share information about this Twunch.
	 */
	private void doShare() {
		final Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.putExtra(Intent.EXTRA_TEXT, String.format(
				getString(R.string.share_text),
				mCursor.getString(TwunchDetailsQuery.TITLE),
				DateUtils.formatDateTime(getActivity(), mCursor.getLong(TwunchDetailsQuery.DATE), DateUtils.FORMAT_SHOW_WEEKDAY
						| DateUtils.FORMAT_SHOW_DATE),
				DateUtils.formatDateTime(getActivity(), mCursor.getLong(TwunchDetailsQuery.DATE), DateUtils.FORMAT_SHOW_TIME),
				mCursor.getString(TwunchDetailsQuery.LINK)));
		intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_subject));
		intent.putExtra(Intent.EXTRA_EMAIL, "");
		startActivity(Intent.createChooser(intent, getString(R.string.share_title)));
	}

	private class ContactAdapter extends BaseAdapter {
		private final Context context;

		public ContactAdapter(Context c) {
			context = c;
		}

		@Override
		public int getCount() {
			return participants.length;
		}

		@Override
		public Object getItem(int position) {
			return participants[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			final TextView textView;
			if (convertView == null) {
				LayoutInflater inflater = LayoutInflater.from(getActivity());
				textView = (TextView) inflater.inflate(R.layout.participant, null);
			} else {
				textView = (TextView) convertView;
			}
			textView.setText("@" + participants[position]);
			textView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					String[] projection = new String[] { ContactsContract.Contacts._ID, ContactsContract.Contacts.LOOKUP_KEY };
					Cursor rawTwitterContact = getActivity().getContentResolver().query(Data.CONTENT_URI, projection,
							Nickname.NAME + " = ?", new String[] { participants[position] }, null);
					if (rawTwitterContact.getCount() > 0) {
						// Show the QuickContact action bar
						rawTwitterContact.moveToFirst();
						final Uri contactUri = Uri.withAppendedPath(Contacts.CONTENT_LOOKUP_URI,
								rawTwitterContact.getString(rawTwitterContact.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY)));
						QuickContact.showQuickContact(context, textView, contactUri, ContactsContract.QuickContact.MODE_LARGE, null);
					} else {
						// Show the twitter profile
						final Intent myIntent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("http://twitter.com/"
								+ participants[position]));
						startActivity(myIntent);
					}
				}
			});
			return textView;
		}
	}

}
