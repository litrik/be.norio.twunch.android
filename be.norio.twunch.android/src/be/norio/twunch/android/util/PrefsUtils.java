package be.norio.twunch.android.util;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

public class PrefsUtils {

	private static final String TAG = PrefsUtils.class.getSimpleName();
	private static final boolean LOGV = true;

	private static final int VER_LEGACY = 0;
	private static final int VER_LAUNCH = 1;

	private static final int PREFS_VERSION = VER_LAUNCH;

	private static final String KEY_PREFS_VERSION = "prefs_version";

	private static boolean APPLY_AVAILABLE = false;

	// Keys
	public static final String KEY_LAST_UPDATE = "last_update";

	// Default values
	public static final long DEFAULT_LAST_UPDATE = 0;

	private static Context CONTEXT;

	static {
		try {
			SharedPreferences.Editor.class.getMethod("apply", new Class[0]);
			APPLY_AVAILABLE = true;
		} catch (NoSuchMethodException e) {
			APPLY_AVAILABLE = false;
		}
	}

	private PrefsUtils() {
	}

	public static void apply(SharedPreferences.Editor editor) {
		if (APPLY_AVAILABLE) {
			if (LOGV)
				Log.v(TAG, "Using apply");
			editor.apply();
		} else {
			if (LOGV)
				Log.v(TAG, "Using commit");
			editor.commit();
		}
	}

	private static SharedPreferences getPrefs() {
		return PreferenceManager.getDefaultSharedPreferences(CONTEXT);
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

		PrefsUtils.apply(getPrefs().edit().putInt(KEY_PREFS_VERSION, version));
	}

	private static boolean contains(String key) {
		return getPrefs().contains(key);
	}

	public static long getLastUpdate() {
		return getPrefs().getLong(KEY_LAST_UPDATE, DEFAULT_LAST_UPDATE);
	}

	public static void setLastUpdate(long value) {
		PrefsUtils.apply(getPrefs().edit().putLong(KEY_LAST_UPDATE, value));
	}

}
