package za.co.twinc.everydayhabit;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.provider.Settings;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.TimePicker;

import java.util.Locale;
import java.lang.String;


public class SettingsActivity extends Activity {
    private static Intent intent;

    private static final String KEY_PREF_HABIT_DESCRIPTION =     "edit_text_preference_habit_text";
    private static final String KEY_PREF_HABIT_SUMMARY =         "edit_text_preference_habit_summary";
    private static final String KEY_PREF_NOTIFICATION_TIME =     "time_picker_preference_notification";
    public static final  String KEY_PREF_NOTIFICATION_SWITCH =   "switch_preference_notification";
    public static final  String KEY_PREF_LEGIT_SWITCH =          "switch_preference_legit";
    public static final  String KEY_PREF_NOTIFICATION_TONE =     "ringtone_preference";
    private static final String KEY_PREF_ABOUT =                 "simple_text_about";
    private static final String KEY_PREF_DATE =                  "simple_text_habit_date";

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


    public static class SettingsFragment extends PreferenceFragment
            implements TimePickerDialog.OnTimeSetListener{

        //Make timePicker available throughout the class
        Preference timePref;

        final SharedPreferences.OnSharedPreferenceChangeListener listener =
                new SharedPreferences.OnSharedPreferenceChangeListener(){
                    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                        // listener implementation
                        if (key.equals(KEY_PREF_HABIT_DESCRIPTION) || key.equals(KEY_PREF_HABIT_SUMMARY)) {
                            Preference pref = findPreference(key);
                            // Set summary to be the user-description for the selected value
                            pref.setSummary(prefs.getString(key, ""));
                            // Return new values
                            if (key.equals(KEY_PREF_HABIT_DESCRIPTION))
                                intent.putExtra("habit_text",prefs.getString(key,"").trim());
                            else intent.putExtra("habit_summary",prefs.getString(key,"").trim());
                        }
                        else if (key.equals(KEY_PREF_NOTIFICATION_SWITCH)){
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
            // Set habit text
            p = preferenceManager.findPreference(KEY_PREF_HABIT_DESCRIPTION);
            p.setSummary(intent.getStringExtra("habit_text"));
            ((EditTextPreference)p).setText(intent.getStringExtra("habit_text"));

            // Set habit summary
            p = preferenceManager.findPreference(KEY_PREF_HABIT_SUMMARY);
            p.setSummary(intent.getStringExtra("habit_summary"));
            ((EditTextPreference)p).setText(intent.getStringExtra("habit_summary"));

            // Set habit notification time
            p = preferenceManager.findPreference(KEY_PREF_HABIT_SUMMARY);
            p.setSummary(intent.getStringExtra("habit_summary"));
            ((EditTextPreference)p).setText(intent.getStringExtra("habit_summary"));

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

            // Set habit start date
            p = preferenceManager.findPreference(KEY_PREF_DATE);
            p.setSummary(intent.getStringExtra("habit_date"));

            // Get notification time
            int hour = intent.getIntExtra("habit_time",0)/60;
            int minute = intent.getIntExtra("habit_time",0)%60;
            // Add TimePickerDialog
            final TimePickerDialog.OnTimeSetListener timeSetListener = this;
            timePref = findPreference(KEY_PREF_NOTIFICATION_TIME);
            timePref.setSummary(String.format(Locale.UK, "%02d",hour)  + ":" +
                                String.format(Locale.UK, "%02d",minute));
            timePref.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    int hour   = Integer.parseInt(timePref.getSummary().subSequence(0,2).toString());
                    int minute = Integer.parseInt(timePref.getSummary().subSequence(3,5).toString());
                    new TimePickerDialog(getActivity(), timeSetListener, hour, minute,
                            DateFormat.is24HourFormat(getActivity())).show();
                    return false;
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

        @Override
        public void onTimeSet(TimePicker view, int hour, int minute) {
            timePref.setSummary(String.format(Locale.UK, "%02d",hour)  + ":" +
                                String.format(Locale.UK, "%02d",minute));
            intent.putExtra("habit_time", hour*60+minute);
        }
    }
}

