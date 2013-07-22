/**
 *	Copyright 2010-2013 Norio bvba
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

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Nickname;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.QuickContact;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.text.Html;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.apps.analytics.GoogleAnalyticsTracker;

import java.util.Arrays;
import java.util.Date;

import be.norio.twunch.android.R;
import be.norio.twunch.android.provider.TwunchContract.Twunches.Query;
import be.norio.twunch.android.util.AnalyticsUtils;
import be.norio.twunch.android.util.FragmentUtils;
import be.norio.twunch.android.util.Util;
import butterknife.InjectView;
import butterknife.Views;

public class TwunchDetailsFragment extends SherlockFragment implements LoaderManager.LoaderCallbacks<Cursor> {

	Cursor mCursor;
	String[] mParticipants;
    @InjectView(R.id.twunchTitle)
    public TextView mTitleView;
    @InjectView(R.id.twunchAddress)
    public TextView mAddressView;
    @InjectView(R.id.twunchDistance)
    public TextView mDistanceView;
    @InjectView(R.id.twunchDate)
    public TextView mDateView;
    @InjectView(R.id.twunchDays)
    public TextView mDaysView;
    @InjectView(R.id.twunchNote)
    public TextView mNoteView;
    @InjectView(R.id.twunchNumberParticipants)
    public TextView mNumParticipantsView;
    @InjectView(R.id.twunchParticipants)
    public ListView mParticipantsView;

    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setHasOptionsMenu(true);
		AnalyticsUtils.trackPageView(AnalyticsUtils.Pages.TWUNCH_DETAILS);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View rootView = inflater.inflate(R.layout.fragment_twunch_details, null);
		return rootView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		getLoaderManager().initLoader(Query._TOKEN, getArguments(), this);
	}

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Views.inject(this, view);
    }

    @Override
	public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.fragment_twunch_details, menu);
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
		return new CursorLoader(getActivity(), (Uri) args.getParcelable(FragmentUtils.ARGUMENT_URI), Query.PROJECTION, null, null,
				null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, final Cursor cursor) {
		if (!cursor.moveToFirst()) {
			return;
		}

		mCursor = cursor;

		// Title
        mTitleView.setText(cursor.getString(Query.TITLE));

		// Address
        mAddressView.setText(cursor.getString(Query.ADDRESS));

		// Distance
		long distance = cursor.getLong(Query.DISTANCE);
		if (distance > 0) {
			mDistanceView.setText(String.format(getString(R.string.distance), distance / 1000f));
			mDistanceView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    doMap();
                }
            });
		} else {
			mDistanceView.setVisibility(View.INVISIBLE);
		}

		// Date
        mDateView.setText(String.format(
                getString(R.string.date),
                DateUtils.formatDateTime(getActivity(), cursor.getLong(Query.DATE), DateUtils.FORMAT_SHOW_WEEKDAY
                        | DateUtils.FORMAT_SHOW_DATE),
                DateUtils.formatDateTime(getActivity(), cursor.getLong(Query.DATE), DateUtils.FORMAT_SHOW_TIME)));

		// Days
		final long msInDay = 86400000;
		int days = (int) (cursor.getLong(Query.DATE) / msInDay - new Date().getTime() / msInDay);
		mDaysView.setText(days == 0 ? getString(R.string.today) : String.format(
                getResources().getQuantityString(R.plurals.days_to_twunch, days), days));
		if (Util.isIceCreamSandwich()) {
			mDaysView.setOnClickListener(new OnClickListener() {

                @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
                @Override
                public void onClick(View v) {
                    try {
                        GoogleAnalyticsTracker.getInstance().trackEvent(AnalyticsUtils.EventCategories.TWUNCH_DETAILS,
                                AnalyticsUtils.EventActions.ADD_TO_CALENDAR, null, 1);
                        Intent intent = new Intent(Intent.ACTION_INSERT)
                                .setData(Events.CONTENT_URI)
                                .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, cursor.getLong(Query.DATE))
                                .putExtra(CalendarContract.EXTRA_EVENT_END_TIME,
                                        cursor.getLong(Query.DATE) + DateUtils.HOUR_IN_MILLIS)
                                .putExtra(Events.TITLE, "Twunch " + cursor.getString(Query.TITLE))
                                .putExtra(Events.DESCRIPTION, "Twunch " + cursor.getString(Query.TITLE))
                                .putExtra(Events.EVENT_LOCATION, cursor.getString(Query.ADDRESS))
                                .putExtra(Events.AVAILABILITY, Events.AVAILABILITY_BUSY);
                        startActivity(intent);
                    } catch (Exception e) {
                        // FIXME: handle exception
                    }
                }
            });
		}

		// Note
		final String note = cursor.getString(Query.NOTE);
		if (TextUtils.isEmpty(note)) {
			mNoteView.setVisibility(View.GONE);
		} else {
			mNoteView.setMovementMethod(LinkMovementMethod.getInstance());
			mNoteView.setText(Html.fromHtml(note));
			mNoteView.setVisibility(View.VISIBLE);
		}

		// Number of participants
        mNumParticipantsView.setText(String.format(
                getResources().getQuantityString(R.plurals.numberOfParticipants, cursor.getInt(Query.NUMPARTICIPANTS)),
                cursor.getInt(Query.NUMPARTICIPANTS)));

		// Participants
		mParticipants = cursor.getString(Query.PARTICIPANTS).split(" ");
		Arrays.sort(mParticipants, String.CASE_INSENSITIVE_ORDER);
		mParticipantsView.setAdapter(new ParticipantAdapter());
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		mCursor = null;
	}

	/**
	 * Show the location of this Twunch on a map.
	 */
	private void doMap() {
		GoogleAnalyticsTracker.getInstance().trackEvent(AnalyticsUtils.EventCategories.TWUNCH_DETAILS,
				AnalyticsUtils.EventActions.SHOW_MAP, null, 1);
		final Intent myIntent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?q="
				+ mCursor.getDouble(Query.LATITUDE) + "," + mCursor.getDouble(Query.LONGITUDE)));
		startActivity(myIntent);
	}

	/**
	 * Show the directions to this Twunch.
	 */
	private void doDirections() {
		GoogleAnalyticsTracker.getInstance().trackEvent(AnalyticsUtils.EventCategories.TWUNCH_DETAILS,
				AnalyticsUtils.EventActions.SHOW_DIRECTIONS, null, 1);
		startActivity(new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("google.navigation:q="
				+ mCursor.getDouble(Query.LATITUDE) + "," + mCursor.getDouble(Query.LONGITUDE))));
	}

	/**
	 * Register for this Twunch.
	 */
	private void doRegister() {
		if (mCursor.getInt(Query.CLOSED) == 1) {
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
			GoogleAnalyticsTracker.getInstance().trackEvent(AnalyticsUtils.EventCategories.TWUNCH_DETAILS,
					AnalyticsUtils.EventActions.REGISTER, null, 1);
			final Intent intent = new Intent(Intent.ACTION_SEND);
			intent.setType("text/plain");
			intent.putExtra(Intent.EXTRA_TEXT,
					String.format(getString(R.string.register_text), mCursor.getString(Query.TITLE), mCursor.getString(Query.LINK)));
			startActivity(Intent.createChooser(intent, getString(R.string.register_title)));

		}
	}

	/**
	 * Share information about this Twunch.
	 */
	private void doShare() {
		GoogleAnalyticsTracker.getInstance().trackEvent(AnalyticsUtils.EventCategories.TWUNCH_DETAILS,
				AnalyticsUtils.EventActions.SHARE, null, 1);

		final Intent intent = new Intent(Intent.ACTION_SEND);
		intent.setType("text/plain");
		intent.putExtra(
				Intent.EXTRA_TEXT,
				String.format(
						getString(R.string.share_text),
						mCursor.getString(Query.TITLE),
						DateUtils.formatDateTime(getActivity(), mCursor.getLong(Query.DATE), DateUtils.FORMAT_SHOW_WEEKDAY
								| DateUtils.FORMAT_SHOW_DATE),
						DateUtils.formatDateTime(getActivity(), mCursor.getLong(Query.DATE), DateUtils.FORMAT_SHOW_TIME),
						mCursor.getString(Query.LINK)));
		intent.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.share_subject));
		intent.putExtra(Intent.EXTRA_EMAIL, "");
		startActivity(Intent.createChooser(intent, getString(R.string.share_title)));
	}

	private static class ViewHolder {
		public View rootView;
		public TextView name;
		public ImageView avatar;

		public ViewHolder(View view) {
			rootView = view;
			name = (TextView) view.findViewById(R.id.participantName);
			avatar = (ImageView) view.findViewById(R.id.participantAvatar);

			view.setTag(this);
		}
	}

	private class ParticipantAdapter extends BaseAdapter {

		@Override
		public int getCount() {
			return mParticipants.length;
		}

		@Override
		public Object getItem(int position) {
			return mParticipants[position];
		}

		@Override
		public long getItemId(int position) {
			return position;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			final View view;
			if (convertView == null) {
				LayoutInflater inflater = LayoutInflater.from(getActivity());
				view = inflater.inflate(R.layout.listitem_participant, null);
				new ViewHolder(view);
			} else {
				view = convertView;
			}
			final ViewHolder vh = (ViewHolder) view.getTag();

			vh.name.setText("@" + mParticipants[position]);
			view.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View v) {
					String[] projection = new String[] { ContactsContract.Contacts._ID, ContactsContract.Contacts.LOOKUP_KEY };
					Cursor rawTwitterContact = getActivity().getContentResolver().query(Data.CONTENT_URI, projection,
							Nickname.NAME + " = ?", new String[] { mParticipants[position] }, null);
					if (rawTwitterContact.getCount() > 0) {
						// Show the QuickContact action bar
						rawTwitterContact.moveToFirst();
						final Uri contactUri = Uri.withAppendedPath(Contacts.CONTENT_LOOKUP_URI,
								rawTwitterContact.getString(rawTwitterContact.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY)));
						QuickContact.showQuickContact(TwunchDetailsFragment.this.getActivity(), vh.name, contactUri,
								ContactsContract.QuickContact.MODE_LARGE, null);
					} else {
						// Show the twitter profile
						final Intent myIntent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("http://twitter.com/"
								+ mParticipants[position]));
						startActivity(myIntent);
					}
				}
			});
			return view;
		}
	}

}
