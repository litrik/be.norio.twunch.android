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
import android.content.Context;
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
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;

import java.util.Arrays;
import java.util.Date;

import be.norio.twunch.android.R;
import be.norio.twunch.android.data.DataManager;
import be.norio.twunch.android.data.model.Twunch;
import be.norio.twunch.android.otto.AvatarAvailableEvent;
import be.norio.twunch.android.util.AnalyticsUtils;
import be.norio.twunch.android.util.AvatarManager;
import butterknife.InjectView;

public class DetailsFragment extends BaseFragment {

    private static final String ARG_ID = "ARG_ID";
    Twunch mTwunch;
    @InjectView(R.id.twunchTitle)
    public TextView mTitleView;
    @InjectView(R.id.twunchAddress)
    public TextView mAddressView;
    @InjectView(R.id.twunchDistance)
    public Button mDistanceView;
    @InjectView(R.id.twunchDate)
    public TextView mDateView;
    @InjectView(R.id.twunchDays)
    public Button mDaysView;
    @InjectView(R.id.twunchNote)
    public TextView mNoteView;
    @InjectView(R.id.twunchNumberParticipants)
    public Button mNumParticipantsView;
    @InjectView(R.id.twunchParticipants)
    public ListView mParticipantsView;
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

        AnalyticsUtils.trackPageView(AnalyticsUtils.Pages.TWUNCH_DETAILS);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_details, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mTwunch = DataManager.getInstance().getTwunch(getArguments().getString(ARG_ID));

        // Title
        mTitleView.setText(mTwunch.getTitle());

        // Address
        mAddressView.setText(mTwunch.getAddress());

        // Distance
        if (mTwunch.hasLocation()) {
            long distance = (long) mTwunch.getDistance();
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
        final long date = mTwunch.getDate();
        mDateView.setText(String.format(
                getString(R.string.date),
                DateUtils.formatDateTime(getActivity(), date, DateUtils.FORMAT_SHOW_WEEKDAY
                        | DateUtils.FORMAT_SHOW_DATE),
                DateUtils.formatDateTime(getActivity(), date, DateUtils.FORMAT_SHOW_TIME)));

        // Days
        final long msInDay = 86400000;
        int days = (int) (date / msInDay - new Date().getTime() / msInDay);
        mDaysView.setText(days == 0 ? getString(R.string.today) : String.format(
                getResources().getQuantityString(R.plurals.days_to_twunch, days), days));
        mDaysView.setOnClickListener(new OnClickListener() {

            @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
            @Override
            public void onClick(View v) {
                try {
                    AnalyticsUtils.trackEvent(AnalyticsUtils.EventCategories.TWUNCH_DETAILS, AnalyticsUtils.EventActions.ADD_TO_CALENDAR, null, 1);
                    Intent intent = new Intent(Intent.ACTION_INSERT)
                            .setData(Events.CONTENT_URI)
                            .putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, date)
                            .putExtra(CalendarContract.EXTRA_EVENT_END_TIME,
                                    date + DateUtils.HOUR_IN_MILLIS)
                            .putExtra(Events.TITLE, "Twunch " + mTwunch.getTitle())
                            .putExtra(Events.DESCRIPTION, "Twunch " + mTwunch.getTitle())
                            .putExtra(Events.EVENT_LOCATION, mTwunch.getAddress())
                            .putExtra(Events.AVAILABILITY, Events.AVAILABILITY_BUSY);
                    startActivity(intent);
                } catch (Exception e) {
                    // FIXME: handle exception
                }
            }
        });

        // Note
        final String note = mTwunch.getNote();
        if (TextUtils.isEmpty(note)) {
            mNoteView.setVisibility(View.GONE);
        } else {
            mNoteView.setMovementMethod(LinkMovementMethod.getInstance());
            mNoteView.setText(Html.fromHtml(note));
            mNoteView.setVisibility(View.VISIBLE);
        }

        String[] participants = mTwunch.getParticipants().toArray(new String[mTwunch.getParticipants().size()]);
        Arrays.sort(participants, String.CASE_INSENSITIVE_ORDER);
        // Number of participants
        mNumParticipantsView.setText(String.format(
                getResources().getQuantityString(R.plurals.numberOfParticipants, participants.length,
                        participants.length)));

        // Participants
        mAdapter = new ParticipantAdapter(getActivity(), R.layout.item_participant, participants);
        mParticipantsView.setAdapter(mAdapter);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_details, menu);
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

    /**
     * Show the location of this Twunch on a map.
     */
    private void doMap() {
        AnalyticsUtils.trackEvent(AnalyticsUtils.EventCategories.TWUNCH_DETAILS, AnalyticsUtils.EventActions.SHOW_MAP, null, 1);
        final Intent myIntent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?q="
                + mTwunch.getLatitude() + "," + mTwunch.getLongitude()));
        startActivity(myIntent);
    }

    /**
     * Show the directions to this Twunch.
     */
    private void doDirections() {
        AnalyticsUtils.trackEvent(AnalyticsUtils.EventCategories.TWUNCH_DETAILS, AnalyticsUtils.EventActions.SHOW_DIRECTIONS, null, 1);
        startActivity(new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("google.navigation:q="
                + mTwunch.getLatitude() + "," + mTwunch.getLongitude())));
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
            AnalyticsUtils.trackEvent(AnalyticsUtils.EventCategories.TWUNCH_DETAILS, AnalyticsUtils.EventActions.REGISTER, null, 1);
            final Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT,
                    String.format(getString(R.string.register_text), mTwunch.getTitle(), mTwunch.getLink()));
            startActivity(Intent.createChooser(intent, getString(R.string.register_title)));

        }
    }

    /**
     * Share information about this Twunch.
     */
    private void doShare() {
        AnalyticsUtils.trackEvent(AnalyticsUtils.EventCategories.TWUNCH_DETAILS,
                AnalyticsUtils.EventActions.SHARE, null, 1);

        final Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(
                Intent.EXTRA_TEXT,
                String.format(
                        getString(R.string.share_text),
                        mTwunch.getTitle(),
                        DateUtils.formatDateTime(getActivity(), mTwunch.getDate(), DateUtils.FORMAT_SHOW_WEEKDAY
                                | DateUtils.FORMAT_SHOW_DATE),
                        DateUtils.formatDateTime(getActivity(), mTwunch.getDate(), DateUtils.FORMAT_SHOW_TIME),
                        mTwunch.getLink()));
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
                    Cursor rawTwitterContact = getActivity().getContentResolver().query(Data.CONTENT_URI, projection,
                            Nickname.NAME + " = ?", new String[]{participant}, null);
                    if (rawTwitterContact.getCount() > 0) {
                        // Show the QuickContact action bar
                        rawTwitterContact.moveToFirst();
                        final Uri contactUri = Uri.withAppendedPath(Contacts.CONTENT_LOOKUP_URI,
                                rawTwitterContact.getString(rawTwitterContact.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY)));
                        QuickContact.showQuickContact(DetailsFragment.this.getActivity(), vh.name, contactUri,
                                ContactsContract.QuickContact.MODE_LARGE, null);
                    } else {
                        // Show the Twitter profile
                        final Intent myIntent = new Intent(android.content.Intent.ACTION_VIEW, Uri.parse("https://twitter.com/"
                                + participant));
                        startActivity(myIntent);
                    }
                }
            });

            if (AvatarManager.isAvatarAvailable(participant)) {
                vh.avatar.setVisibility(View.VISIBLE);
                Picasso.with(getActivity()).load(AvatarManager.getAvatar(participant)).into(vh.avatar);
            } else {
                vh.avatar.setVisibility(View.INVISIBLE);
                AvatarManager.addToQueue(participant);
            }
            return view;
        }
    }

    @Subscribe
    public void onAvatarAvailable(AvatarAvailableEvent event) {
        System.out.println("ZZ:onAvatarAvailable");
        mAdapter.notifyDataSetChanged();
    }

}
