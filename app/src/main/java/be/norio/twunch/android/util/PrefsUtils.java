/**
 *	Copyright 2012 Norio bvba
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

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.Log;
import android.util.LruCache;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import be.norio.twunch.android.data.TwunchData;

public class PrefsUtils {

    private static final String TAG = PrefsUtils.class.getSimpleName();
    private static final boolean LOGV = true;
    private static final boolean LOGD = false;

    private static final int VER_LEGACY = 0;
    private static final int VER_LAUNCH = 1;

    private static final int PREFS_VERSION = VER_LAUNCH;

    private static final String KEY_PREFS_VERSION = "prefs_version";

    // Keys
    public static final String KEY_LAST_UPDATE = "last_update";
    private static final String KEY_LAST_RUN_VERSION = "last_run_version";
    private static final String KEY_TWITTER_TOKEN = "twitter_token";
    private static final String KEY_DATA = "data";
    private static final String KEY_SORT = "sort";
    private static final String KEY_AVATARS = "avatars";
    private static final String KEY_NOTIFICATION = "notification";
    private static final String KEY_VIBRATE = "vibrate";
    public static final String KEY_SOUND = "sound";

    // Default values
    public static final long DEFAULT_LAST_UPDATE = 0;

    // Values
    public static final int SORT_DATE = 0;
    public static final int SORT_DISTANCE = 1;
    public static final int SORT_POPULARITY = 2;

    private static Context CONTEXT;

    private PrefsUtils() {
    }

    private static SharedPreferences getPrefs() {
        return PreferenceManager.getDefaultSharedPreferences(CONTEXT);
    }

    public static Context getContext() {
        return CONTEXT;
    }

    public static void initialize(Application application) {
        CONTEXT = application;

        int version = getPrefs().getInt(KEY_PREFS_VERSION, VER_LEGACY);

        switch (version) {
            case VER_LEGACY:
                if (LOGV)
                    Log.v(TAG, "Upgrading settings from version " + version + " to version " + VER_LAUNCH);
                version = VER_LAUNCH;
        }

        getPrefs().edit().putInt(KEY_PREFS_VERSION, version).apply();
    }

    private static boolean contains(String key) {
        return getPrefs().contains(key);
    }

    public static int getLastRunVersion() {
        return getPrefs().getInt(KEY_LAST_RUN_VERSION, 0);
    }

    public static void setLastRunVersion(int value) {
        getPrefs().edit().putInt(KEY_LAST_RUN_VERSION, value).apply();
    }

    public static String getTwitterToken() {
        return getPrefs().getString(KEY_TWITTER_TOKEN, null);
    }

    public static void setTwitterToken(String value) {
        getPrefs().edit().putString(KEY_TWITTER_TOKEN, value).apply();
    }

    public static TwunchData getData() {
        final String string = getPrefs().getString(KEY_DATA, null);
        if (TextUtils.isEmpty(string)) {
            return new TwunchData();
        } else
            return (new GsonBuilder()).create().fromJson(string, TwunchData.class);
    }

    public static void setData(TwunchData data) {
        final String string = new Gson().toJson(data);
        getPrefs().edit().putString(KEY_DATA, string).apply();
    }

    public static boolean isDataAvailable() {
        return !TextUtils.isEmpty(getPrefs().getString(KEY_DATA, null));
    }

    public static void setSort(int value) {
        getPrefs().edit().putInt(KEY_SORT, value).apply();
    }

    public static int getSort() {
        return getPrefs().getInt(KEY_SORT, SORT_DATE);
    }

    public static LruCache<String, String> getAvatars() {
        final LruCache<String, String> cache = new LruCache<String, String>(200);
        final String string = getPrefs().getString(KEY_AVATARS, null);
        if (!TextUtils.isEmpty(string)) {
            Map<String, String> map = new HashMap<String, String>();
            Map<String, String> avatars = (new GsonBuilder()).create().fromJson(string, map.getClass());
            Iterator it = avatars.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry pairs = (Map.Entry) it.next();
                cache.put((String) pairs.getKey(), (String) pairs.getValue());
                it.remove(); // avoids a ConcurrentModificationException
            }
        }
        return cache;
    }

    public static void setAvatars(LruCache<String, String> avatars) {
        final String string = new Gson().toJson(avatars.snapshot());
        getPrefs().edit().putString(KEY_AVATARS, string).apply();
    }

    public static boolean isVibrateEnabled() {
        return getPrefs().getBoolean(KEY_VIBRATE, true);
    }

    public static boolean isNotificationEnabled() {
        return getPrefs().getBoolean(KEY_NOTIFICATION, true);
    }

    public static Uri getSound() {
        String s = getPrefs().getString(KEY_SOUND, null);
        return s == null ? RingtoneManager.getActualDefaultRingtoneUri(CONTEXT, RingtoneManager.TYPE_NOTIFICATION) : Uri.parse(s);
    }

}
