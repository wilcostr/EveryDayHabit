package za.co.twinc.everydayhabit;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.support.v7.widget.Toolbar;
import android.view.View;

import java.lang.String;


public class SettingsActivity extends Activity {
    private static Intent intent;

    public static final  String KEY_PREF_NOTIFICATION_SWITCH =   "switch_preference_notification";
    public static final  String KEY_PREF_LEGIT_SWITCH =          "switch_preference_legit";
    public static final  String KEY_PREF_NOTIFICATION_TONE =     "ringtone_preference";
    private static final String KEY_PREF_ABOUT =                 "simple_text_about";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);
        Toolbar bar = (Toolbar) findViewById(R.id.toolbar);
        bar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        intent = getIntent();

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(R.id.settings_container, new SettingsFragment())
                .commit();
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK,intent);
        super.onBackPressed();
    }

    public static class SettingsFragment extends PreferenceFragment{

        final SharedPreferences.OnSharedPreferenceChangeListener listener =
                new SharedPreferences.OnSharedPreferenceChangeListener(){
                    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                        // listener implementation
                        if (key.equals(KEY_PREF_NOTIFICATION_SWITCH)){
                            Preference pref = findPreference(KEY_PREF_NOTIFICATION_TONE);
                            if (((SwitchPreference)findPreference(key)).isChecked()) {
                                MainActivity.setAllNotifications(getActivity());
                                pref.setEnabled(true);
                            }
                            else {
                                MainActivity.cancelAllNotifications(getActivity());
                                pref.setEnabled(false);
                            }
                        }
                    }
                };

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);

            Preference p;
            PreferenceManager preferenceManager = getPreferenceManager();

            // Set about string
            p = preferenceManager.findPreference(KEY_PREF_ABOUT);
            String versionName;
            try {
                versionName = getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName;
            }catch (PackageManager.NameNotFoundException e){
                versionName = "Not Found";
            }
            p.setSummary(versionName);

            // Set notification tone
            p = preferenceManager.findPreference(KEY_PREF_NOTIFICATION_TONE);
            String toneStr = PreferenceManager.getDefaultSharedPreferences(getActivity())
                    .getString(KEY_PREF_NOTIFICATION_TONE, Settings.System.DEFAULT_NOTIFICATION_URI.toString());
            if ( toneStr.equals(Settings.System.DEFAULT_NOTIFICATION_URI.toString()) ) {
                PreferenceManager.getDefaultSharedPreferences(getActivity()).edit()
                        .putString(KEY_PREF_NOTIFICATION_TONE, Settings.System.DEFAULT_NOTIFICATION_URI.toString())
                        .apply();
            }
            Ringtone tone = RingtoneManager.getRingtone(getActivity(), Uri.parse(toneStr));
            p.setSummary(tone.getTitle(getActivity()));
            if (p.getSummary().equals("Unknown ringtone")) p.setSummary("None");
            // Update summary on change of notification tone
            p.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object o) {
                    Ringtone tone = RingtoneManager.getRingtone(getActivity(), Uri.parse((String)o));
                    preference.setSummary(tone.getTitle(getActivity()));
                    if (preference.getSummary().equals("Unknown ringtone")) preference.setSummary("None");
                    return true;
                }
            });
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceManager().getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(listener);
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceManager().getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(listener);
        }
    }
}

