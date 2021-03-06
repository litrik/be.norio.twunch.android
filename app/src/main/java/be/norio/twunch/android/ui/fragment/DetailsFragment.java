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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Nickname;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.QuickContact;
import android.text.Html;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;

import java.util.Arrays;

import be.norio.twunch.android.R;
import be.norio.twunch.android.data.DataManager;
import be.norio.twunch.android.data.model.Twunch;
import be.norio.twunch.android.otto.AvatarAvailableEvent;
import be.norio.twunch.android.util.AnalyticsUtils;
import be.norio.twunch.android.util.AvatarManager;
import be.norio.twunch.android.util.Util;
import butterknife.InjectView;
import butterknife.OnClick;

public class DetailsFragment extends BaseFragment {

    private static final String ARG_ID = "ARG_ID";
    Twunch mTwunch;
    @InjectView(R.id.title)
    public TextView mTitle;
    @InjectView(R.id.address)
    public TextView mAddress;
    @InjectView(R.id.date)
    public TextView mDate;
    @InjectView(R.id.note)
    public TextView mNote;
    @InjectView(R.id.map)
    public View mMap;
    @InjectView(R.id.map_separator)
    public View mMapSeparator;
    @InjectView(R.id.participants)
    public AdapterView mParticipants;
    private ParticipantAdapter mAdapter;

    public static DetailsFragment newInstance(String id) {
        DetailsFragment f = new DetailsFragment();
        Bundle args = new Bundle(1);
        args.putString(ARG_ID, id);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        AnalyticsUtils.trackPageView(AnalyticsUtils.Pages.DETAILS);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_details, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mTwunch = DataManager.getInstance().getTwunch(getArguments().getString(ARG_ID));

        if(mTwunch == null) {
            return;
        }

        // Title
        mTitle.setText(mTwunch.getTitle());

        // Address
        StringBuffer sb = new StringBuffer();
        sb.append(mTwunch.getAddress());
        if (mTwunch.hasLocation()) {
            if (mTwunch.getDistance() < Float.MAX_VALUE) {
                sb.append(" (");
                sb.append(Util.formatDistance(view.getContext(), mTwunch.getDistance()));
                sb.append(")");
            }
        } else {
            mMap.setVisibility(View.INVISIBLE);
            mMapSeparator.setVisibility(View.INVISIBLE);
        }
        mAddress.setText(sb.toString());

        // Date
        final long date = mTwunch.getDate();
        mDate.setText(Util.formatDate(view.getContext(), date));

        // Note
        final String note = mTwunch.getNote();
        if (TextUtils.isEmpty(note)) {
            mNote.setVisibility(View.GONE);
        } else {
            mNote.setMovementMethod(LinkMovementMethod.getInstance());
            mNote.setText(Html.fromHtml(note));
            mNote.setVisibility(View.VISIBLE);
        }

        String[] participants = mTwunch.getParticipants().toArray(new String[mTwunch.getParticipants().size()]);
        Arrays.sort(participants, String.CASE_INSENSITIVE_ORDER);

        // Participants
        mAdapter = new ParticipantAdapter(getActivity(), R.layout.item_participant, participants);
        mParticipants.setAdapter(mAdapter);

    }

    @OnClick(R.id.add_calendar)
    public void addToCalendar() {
        try {
            AnalyticsUtils.trackEvent(AnalyticsUtils.EventCategories.DETAILS, AnalyticsUtils.EventActions.ADD_TO_CALENDAR, null, 1);
            final long date = mTwunch.getDate();
            Intent intent = new Intent(Intent.ACTION_INSERT).setData(Events.CONTENT_URI).putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, date).putExtra(CalendarContract.EXTRA_EVENT_END_TIME, date + DateUtils.HOUR_IN_MILLIS).putExtra(Events.TITLE, "Twunch " + mTwunch.getTitle()).putExtra(Events.DESCRIPTION, "Twunch " + mTwunch.getTitle()).putExtra(Events.EVENT_LOCATION, mTwunch.getAddress()).putExtra(Events.AVAILABILITY, Events.AVAILABILITY_BUSY);
            startActivity(intent);
        } catch (Exception e) {
            // FIXME: handle exception
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_details, menu);
        menu.findItem(R.id.action_register).setVisible(!mTwunch.isClosed());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_register:
                doRegister();
                return true;
            case R.id.action_share:
                doShare();
                return true;
        }
        return false;
    }

    /**
     * Show the location of this Twunch on a map.
     */
    @OnClick(R.id.map)
    public void doMap() {
        AnalyticsUtils.trackEvent(AnalyticsUtils.EventCategories.DETAILS, AnalyticsUtils.EventActions.SHOW_MAP, null, 1);
        final Intent myIntent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?q=" + mTwunch.getLatitude() + "," + mTwunch.getLongitude()));
        startActivity(myIntent);
    }

    /**
     * Register for this Twunch.
     */
    private void doRegister() {
        if (mTwunch.isClosed()) {
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
            AnalyticsUtils.trackEvent(AnalyticsUtils.EventCategories.DETAILS, AnalyticsUtils.EventActions.REGISTER, null, 1);
            final Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.register_text, mTwunch.getTitle(), mTwunch.getLink()));
            startActivity(Intent.createChooser(intent, getString(R.string.register_title)));

        }
    }

    /**
     * Share information about this Twunch.
     */
    private void doShare() {
        AnalyticsUtils.trackEvent(AnalyticsUtils.EventCategories.DETAILS, AnalyticsUtils.EventActions.SHARE, null, 1);

        final Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, getString(R.string.share_text, mTwunch.getTitle(), DateUtils.formatDateTime(getActivity(), mTwunch.getDate(), DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_SHOW_DATE), DateUtils.formatDateTime(getActivity(), mTwunch.getDate(), DateUtils.FORMAT_SHOW_TIME), mTwunch.getLink()));
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

    private class ParticipantAdapter extends ArrayAdapter<String> {

        public ParticipantAdapter(Context context, int resource, String[] objects) {
            super(context, resource, objects);
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            final View view;
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(getActivity());
                view = inflater.inflate(R.layout.item_participant, null);
                new ViewHolder(view);
            } else {
                view = convertView;
            }
            final ViewHolder vh = (ViewHolder) view.getTag();

            final String participant = getItem(position);
            vh.name.setText("@" + participant);
            view.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View v) {
                    String[] projection = new String[]{ContactsContract.Contacts._ID, ContactsContract.Contacts.LOOKUP_KEY};
                    Cursor rawTwitterContact = getActivity().getContentResolver().query(Data.CONTENT_URI, projection, Nickname.NAME + " = ?", new String[]{participant}, null);
                    if (rawTwitterContact.getCount() > 0) {
                        // Show the QuickContact action bar
                        rawTwitterContact.moveToFirst();
                        final Uri contactUri = Uri.withAppendedPath(Contacts.CONTENT_LOOKUP_URI, rawTwitterContact.getString(rawTwitterContact.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY)));
                        QuickContact.showQuickContact(DetailsFragment.this.getActivity(), vh.name, contactUri, ContactsContract.QuickContact.MODE_LARGE, null);
                    } else {
                        // Show the Twitter profile
                        final Intent myIntent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("https://twitter.com/" + participant));
                        startActivity(myIntent);
                    }
                }
            });

            if (AvatarManager.isAvatarAvailable(participant)) {
                Picasso.with(getActivity()).load(AvatarManager.getAvatar(participant)).error(R.drawable.blank_avatar).into(vh.avatar);
                vh.avatar.setVisibility(View.VISIBLE);
            } else {
                Picasso.with(getActivity()).load(R.drawable.blank_avatar).into(vh.avatar);
                // vh.avatar.setVisibility(View.INVISIBLE);
                AvatarManager.addToQueue(participant);
            }
            return view;
        }
    }

    @Subscribe
    public void onAvatarAvailable(AvatarAvailableEvent event) {
        mAdapter.notifyDataSetChanged();
    }

}
