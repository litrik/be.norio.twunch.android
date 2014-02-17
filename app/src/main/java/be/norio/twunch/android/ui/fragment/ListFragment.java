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
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.style.RelativeSizeSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.squareup.otto.Subscribe;

import be.norio.twunch.android.R;
import be.norio.twunch.android.data.DataManager;
import be.norio.twunch.android.data.model.Twunch;
import be.norio.twunch.android.otto.BusProvider;
import be.norio.twunch.android.otto.TwunchClickedEvent;
import be.norio.twunch.android.otto.TwunchesAvailableEvent;
import be.norio.twunch.android.util.PrefsUtils;
import be.norio.twunch.android.util.Util;
import butterknife.ButterKnife;
import butterknife.InjectView;
import uk.co.senab.actionbarpulltorefresh.library.ActionBarPullToRefresh;
import uk.co.senab.actionbarpulltorefresh.library.PullToRefreshLayout;
import uk.co.senab.actionbarpulltorefresh.library.listeners.OnRefreshListener;

public class ListFragment extends BaseFragment implements OnRefreshListener, AdapterView.OnItemClickListener {

    private TwunchAdapter mAdapter;

    @InjectView(R.id.pull_to_refresh)
    PullToRefreshLayout mPullToRefresh;
    @InjectView(R.id.list)
    ListView mListView;

    int mCurrentSorting;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mCurrentSorting = PrefsUtils.getSort();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_list, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mAdapter = new TwunchAdapter(getActivity(), R.layout.item_twunch);
        mListView.setAdapter(mAdapter);
        mListView.setOnItemClickListener(this);

        ActionBarPullToRefresh.from(getActivity())
                .allChildrenArePullable()
                .listener(this)
                .setup(mPullToRefresh);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_list, menu);
        if (mCurrentSorting == PrefsUtils.SORT_DATE) {
            menu.findItem(R.id.action_sort_date).setChecked(true);
        } else if (mCurrentSorting == PrefsUtils.SORT_DISTANCE) {
            menu.findItem(R.id.action_sort_distance).setChecked(true);
        } else if (mCurrentSorting == PrefsUtils.SORT_POPULARITY) {
            menu.findItem(R.id.action_sort_popularity).setChecked(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_sort_date:
                PrefsUtils.setSort(PrefsUtils.SORT_DATE);
                item.setChecked(true);
                sortData(PrefsUtils.SORT_DATE);
                return true;
            case R.id.action_sort_distance:
                PrefsUtils.setSort(PrefsUtils.SORT_DISTANCE);
                item.setChecked(true);
                sortData(PrefsUtils.SORT_DISTANCE);
                return true;
            case R.id.action_sort_popularity:
                PrefsUtils.setSort(PrefsUtils.SORT_POPULARITY);
                item.setChecked(true);
                sortData(PrefsUtils.SORT_POPULARITY);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }

    }

    static class ViewHolder {
        @InjectView(R.id.sorting)
        public TextView sorting;
        @InjectView(R.id.title)
        public TextView title;
        @InjectView(R.id.address)
        public TextView address;
        @InjectView(R.id.date)
        public TextView date;

        public ViewHolder(View v) {
            ButterKnife.inject(this, v);
            v.setTag(this);
        }
    }

    private class TwunchAdapter extends ArrayAdapter<Twunch> {


        public TwunchAdapter(Context context, int resource) {
            super(context, resource);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final View view;
            if (convertView == null) {
                LayoutInflater inflater = LayoutInflater.from(getActivity());
                view = inflater.inflate(R.layout.item_twunch, null);
                new ViewHolder(view);
            } else {
                view = convertView;
            }
            final ViewHolder vh = (ViewHolder) view.getTag();

            final Twunch twunch = getItem(position);

            String sortText = null;
            switch (mCurrentSorting) {
                case PrefsUtils.SORT_DISTANCE:
                    final double lat = twunch.getLatitude();
                    final double lon = twunch.getLongitude();
                    float distance = twunch.getDistance();
                    if (lat != 0 && lon != 0 && distance > 0) {
                        sortText = Util.formatDistance(getContext(), distance);
                    }
                    break;
                case PrefsUtils.SORT_POPULARITY:
                    final int size = twunch.getParticipants().size();
                    sortText = getResources().getQuantityString(R.plurals.numberOfParticipants, size, size);
                    break;
                default:
                    int days = (int) ((twunch.getDate() - Util.getStartOfToday()) / DateUtils.DAY_IN_MILLIS);
                    sortText = getResources().getQuantityString(R.plurals.days_to_twunch, days, days);
                    break;
            }
            if (TextUtils.isEmpty(sortText)) {
                vh.sorting.setVisibility(View.INVISIBLE);
            } else {
                int split = sortText.indexOf(' ');
                SpannableStringBuilder ssb = new SpannableStringBuilder(sortText.replace(' ', '\n'));
                ssb.setSpan(new RelativeSizeSpan(2f), 0, split, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                vh.sorting.setText(ssb);
                vh.sorting.setVisibility(View.VISIBLE);
            }

            vh.title.setText(twunch.getTitle());

            vh.address.setText(twunch.getAddress());
            //vh.address.setTypeface(null, cursor.getInt(Query.NEW) == 1 ? Typeface.BOLD : Typeface.NORMAL);

            vh.date.setText(Util.formatDate(view.getContext(), twunch.getDate()));
            //vh.date.setTypeface(null, cursor.getInt(Query.NEW) == 1 ? Typeface.BOLD : Typeface.NORMAL);

            return view;
        }

    }

    @Override
    public void onRefreshStarted(View view) {
        DataManager.getInstance().loadTwunches(true);
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        BusProvider.getInstance().post(
                new TwunchClickedEvent(mAdapter.getItem(position)));
    }

    @Subscribe
    public void onTwunchesAvailableEvent(TwunchesAvailableEvent event) {
        mAdapter.clear();
        mAdapter.addAll(event.getTwunches());
        sortData(mCurrentSorting);
        mPullToRefresh.setRefreshComplete();
    }


    private void sortData(int sorting) {
        mCurrentSorting = sorting;
        switch (sorting) {
            case PrefsUtils.SORT_DISTANCE:
                mAdapter.sort(Twunch.COMPARATOR_DISTANCE);
                return;
            case PrefsUtils.SORT_POPULARITY:
                mAdapter.sort(Twunch.COMPARATOR_POPULARITY);
                return;
            default:
                mAdapter.sort(Twunch.COMPARATOR_DATE);
                return;
        }

    }
}
