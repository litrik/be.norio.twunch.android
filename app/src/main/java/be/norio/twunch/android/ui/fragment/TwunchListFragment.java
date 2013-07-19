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

import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import be.norio.twunch.android.R;
import be.norio.twunch.android.otto.BusProvider;
import be.norio.twunch.android.otto.OnTwunchClickedEvent;
import be.norio.twunch.android.provider.TwunchContract.Twunches;
import be.norio.twunch.android.provider.TwunchContract.Twunches.Query;
import be.norio.twunch.android.util.Util;

import com.actionbarsherlock.app.SherlockListFragment;

public class TwunchListFragment extends SherlockListFragment implements LoaderManager.LoaderCallbacks<Cursor> {

	private final static String EXTRA_SORT = "EXTRA_SORT";

	private CursorAdapter mAdapter;

	public static TwunchListFragment newInstance(String sort) {
		TwunchListFragment fragment = new TwunchListFragment();
		Bundle args = new Bundle();
		args.putString(TwunchListFragment.EXTRA_SORT, sort);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mAdapter = new TwunchAdapter(getActivity());
		setListAdapter(mAdapter);

		getLoaderManager().initLoader(Query._TOKEN, getArguments(), this);
	}

	private static class ViewHolder {
		public View rootView;
		public TextView title;
		public TextView address;
		public TextView distance;
		public TextView date;
		public TextView days;

		public ViewHolder(View view) {
			rootView = view;
			title = (TextView) view.findViewById(R.id.twunchTitle);
			address = (TextView) view.findViewById(R.id.twunchAddress);
			distance = (TextView) view.findViewById(R.id.twunchDistance);
			date = (TextView) view.findViewById(R.id.twunchDate);
			days = (TextView) view.findViewById(R.id.twunchDays);

			view.setTag(this);
		}
	}

	class TwunchAdapter extends CursorAdapter {

		public TwunchAdapter(Context context) {
			super(context, null, 0);
		}

		@Override
		public void bindView(View view, Context context, Cursor cursor) {

			ViewHolder vh = (ViewHolder) view.getTag();
			// Title
			vh.title.setText(cursor.getString(Query.NAME));
			// Address
			vh.address.setText(cursor.getString(Query.ADDRESS));
			vh.address.setTypeface(null, cursor.getInt(Query.NEW) == 1 ? Typeface.BOLD : Typeface.NORMAL);
			// Distance
			final double lat = cursor.getDouble(Query.LATITUDE);
			final double lon = cursor.getDouble(Query.LONGITUDE);
			long distance = cursor.getLong(Query.DISTANCE);
			if (lat != 0 && lon != 0 && distance > 0) {
				vh.distance.setText(String.format(view.getContext().getString(R.string.distance), distance / 1000f));
				vh.distance.setVisibility(View.VISIBLE);
			} else {
				vh.distance.setVisibility(View.INVISIBLE);
			}
			// Date
			vh.date.setText(String.format(
					view.getContext().getString(R.string.date),
					DateUtils.formatDateTime(view.getContext(), cursor.getLong(Query.DATE), DateUtils.FORMAT_SHOW_WEEKDAY
							| DateUtils.FORMAT_SHOW_DATE),
					DateUtils.formatDateTime(view.getContext(), cursor.getLong(Query.DATE), DateUtils.FORMAT_SHOW_TIME)));
			vh.date.setTypeface(null, cursor.getInt(Query.NEW) == 1 ? Typeface.BOLD : Typeface.NORMAL);
			// Days
			int days = (int) ((cursor.getLong(Query.DATE) - Util.getStartOfToday()) / DateUtils.DAY_IN_MILLIS);
			vh.days.setText(days == 0 ? getString(R.string.today) : String.format(
					getResources().getQuantityString(R.plurals.days_to_twunch, days), days));
		}

		@Override
		public View newView(Context context, Cursor cursor, ViewGroup parent) {
			View view = LayoutInflater.from(context).inflate(R.layout.listitem_twunch, parent, false);
			new ViewHolder(view);
			return view;
		}
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		BusProvider.getInstance().post(
				new OnTwunchClickedEvent(Twunches.buildTwunchUri(Integer.toString(((Cursor) mAdapter.getItem(position))
						.getInt(Query._ID)))));
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		return new CursorLoader(getActivity(), Twunches.buildFutureTwunchesUri(), Query.PROJECTION, null, null,
				args.getString(EXTRA_SORT));
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
		mAdapter.swapCursor(cursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> arg0) {
		mAdapter.swapCursor(null);
	}
}
