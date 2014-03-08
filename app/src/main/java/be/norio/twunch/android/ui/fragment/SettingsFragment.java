package be.norio.twunch.android.ui.fragment;

import android.app.Activity;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.view.MenuItem;

import be.norio.twunch.android.BuildConfig;
import be.norio.twunch.android.R;
import be.norio.twunch.android.util.AnalyticsUtils;
import be.norio.twunch.android.util.PrefsUtils;

public class SettingsFragment extends PreferenceFragment {

    private Preference mSoundPref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        addPreferencesFromResource(R.xml.preferences);

        final Preference version = findPreference("version");
        version.setTitle(getActivity().getString(R.string.version, BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE));

        final Preference whats_new = findPreference("whats_new");
        whats_new.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                showWhatsNew();
                return true;
            }
        });

        final Preference about = findPreference("about");
        about.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                showAbout();
                return true;
            }
        });

        mSoundPref = findPreference(PrefsUtils.KEY_SOUND);

        refreshSummaries();
//        startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + getPackageName())));

    }

    @Override
    public void onResume()
    {
        super.onResume();
        refreshSummaries();
    }


    void refreshSummaries()
    {
        final Activity context = getActivity();
        final Uri sound = PrefsUtils.getSound();
        if (sound != null)
        {
            Ringtone ringtone = RingtoneManager.getRingtone(context, sound);
            if (ringtone != null)
            {
                mSoundPref.setSummary(ringtone.getTitle(context));
            }
        }
    }

    private void showWhatsNew() {
        HtmlDialogFragment.newInstance(getString(R.string.whats_new), R.raw.whats_new, AnalyticsUtils.Pages.WHATS_NEW).show(getFragmentManager(), "whats_new");
    }

    private void showAbout() {
        HtmlDialogFragment.newInstance(getString(R.string.about_title, BuildConfig.VERSION_NAME), R.raw.about,
                AnalyticsUtils.Pages.ABOUT).show(getFragmentManager(), "about");
    }

}
