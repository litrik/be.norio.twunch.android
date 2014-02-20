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

package be.norio.twunch.android.util;

import android.app.FragmentManager;
import android.content.Context;
import android.text.format.DateUtils;
import android.text.format.Time;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import be.norio.twunch.android.BuildConfig;
import be.norio.twunch.android.R;
import be.norio.twunch.android.ui.fragment.HtmlDialogFragment;

public class Util {

    public static String readTextFromResource(Context context, int resourceId) {
        InputStream raw = context.getResources().openRawResource(resourceId);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        int i;
        try {
            i = raw.read();
            while (i != -1) {
                stream.write(i);
                i = raw.read();
            }
            raw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return stream.toString();
    }

    public static long getStartOfToday() {
        Time t = new Time(Time.getCurrentTimezone());
        t.setToNow();
        t.hour = 0;
        t.minute = 0;
        t.second = 0;
        return t.toMillis(false);
    }

    public static String formatDistance(Context context, float distance) {
        if (distance < 1000) {
            return context.getString(R.string.distance_below_1km, distance);
        } else if (distance < 10000) {
            return context.getString(R.string.distance_below_10km, distance / 1000f);
        } else {
            return context.getString(R.string.distance_above_10km, distance / 1000f);
        }
    }

    public static String formatDate(Context context, long date) {
        return context.getString(R.string.date, DateUtils.formatDateTime(context, date, DateUtils.FORMAT_SHOW_WEEKDAY | DateUtils.FORMAT_SHOW_DATE), DateUtils.formatDateTime(context, date, DateUtils.FORMAT_SHOW_TIME));
    }

}
