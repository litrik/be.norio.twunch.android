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

import android.app.ListFragment;
import android.content.Context;
import android.os.Bundle;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

import be.norio.twunch.android.R;
import be.norio.twunch.android.data.DataManager;
import be.norio.twunch.android.data.model.Twunch;
import be.norio.twunch.android.otto.BusProvider;
import be.norio.twunch.android.otto.TwunchClickedEvent;
import be.norio.twunch.android.util.Util;
import butterknife.ButterKnife;
import butterknife.InjectView;

public class TwunchListFragment extends ListFragment {

	private TwunchAdapter mAdapter;
    private List<Twunch> mTwunches;

    @Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

        mTwunches = DataManager.getInstance().getTwunches();
		mAdapter = new TwunchAdapter(getActivity(), R.layout.listitem_twunch, mTwunches);
		setListAdapter(mAdapter);
	}

    static class ViewHolder {
		public View rootView;
        @InjectView(R.id.twunchTitle)
		public TextView title;
        @InjectView(R.id.twunchAddress)
		public TextView address;
        @InjectView(R.id.twunchDistance)
		public TextView distance;
        @InjectView(R.id.twunchDate)
		public TextView date;
        @InjectView(R.id.twunchDays)
		public TextView days;

        public ViewHolder(View v) {
            ButterKnife.inject(this, v);
            v.setTag(this);
        }
    }
    private class TwunchAdapter extends ArrayAdapter<Twunch> {

        public TwunchAdapter(Context context, int resource, List<Twunch> objects) {
            super(context, resource, objects);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final View view;
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(getActivity());
                view = inflater.inflate(R.layout.listitem_twunch, null);
                new ViewHolder(view);
            } else {
                view = convertView;
            }
            final ViewHolder vh = (ViewHolder) view.getTag();

            final Twunch twunch = getItem(position);

            vh.title.setText(twunch.getTitle());

            vh.address.setText(twunch.getAddress());
			//vh.address.setTypeface(null, cursor.getInt(Query.NEW) == 1 ? Typeface.BOLD : Typeface.NORMAL);

            final double lat = twunch.getLatitude();
			final double lon = twunch.getLongitude();
			float distance = twunch.getDistance();
			if (lat != 0 && lon != 0 && distance > 0) {
				vh.distance.setText(String.format(view.getContext().getString(R.string.distance), distance / 1000f));
				vh.distance.setVisibility(View.VISIBLE);
			} else {
				vh.distance.setVisibility(View.INVISIBLE);
			}

            vh.date.setText(String.format(
					view.getContext().getString(R.string.date),
					DateUtils.formatDateTime(view.getContext(), twunch.getDate(), DateUtils.FORMAT_SHOW_WEEKDAY
							| DateUtils.FORMAT_SHOW_DATE),
					DateUtils.formatDateTime(view.getContext(), twunch.getDate(), DateUtils.FORMAT_SHOW_TIME)));
			//vh.date.setTypeface(null, cursor.getInt(Query.NEW) == 1 ? Typeface.BOLD : Typeface.NORMAL);

            int days = (int) ((twunch.getDate() - Util.getStartOfToday()) / DateUtils.DAY_IN_MILLIS);
			vh.days.setText(days == 0 ? getString(R.string.today) : String.format(
					getResources().getQuantityString(R.plurals.days_to_twunch, days), days));
            return view;
		}

	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		BusProvider.getInstance().post(
				new TwunchClickedEvent(mTwunches.get(position)));
	}

}
