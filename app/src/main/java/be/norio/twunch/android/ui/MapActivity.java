/**
 *	Copyright 2012-2014 Norio bvba
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

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.google.android.apps.analytics.GoogleAnalyticsTracker;

import be.norio.twunch.android.R;
import be.norio.twunch.android.util.AnalyticsUtils;

public class MapActivity extends BaseActivity {

    public static void start(Context context) {
        Intent intent = new Intent(context, MapActivity.class);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_map);

        AnalyticsUtils.trackPageView(AnalyticsUtils.Pages.TWUNCH_MAP);
    }

}
