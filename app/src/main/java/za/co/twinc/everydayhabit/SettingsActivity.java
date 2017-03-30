package za.co.twinc.everydayhabit;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TimePicker;

import java.util.Locale;
import java.lang.String;


public class SettingsActivity extends Activity {
    public static Intent intent;

    public static final String KEY_PREF_HABIT_DESCRIPTION = "edit_text_preference_habit_text";
    public static final String KEY_PREF_HABIT_SUMMARY = "edit_text_preference_habit_summary";
    public static final String KEY_PREF_NOTIFICATION_TIME = "time_picker_preference_notification";
    public static final String KEY_PREF_NOTIFICATION_SWITCH = "switch_preference_notification";

    public static final String KEY_PREF_DATE = "simple_text_habit_date";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        intent = getIntent();

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(android.R.id.content, new SettingsFragment())
                .commit();
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        LinearLayout root = (LinearLayout) findViewById(android.R.id.list).getParent().getParent().getParent();
        Toolbar bar = (Toolbar) LayoutInflater.from(this).inflate(R.layout.settings_toolbar, root, false);
        //bar.setTitleTextColor(Color.WHITE);
        bar.setBackgroundColor(Color.LTGRAY);
        root.addView(bar, 0); // insert at top
        bar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
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

        SharedPreferences.OnSharedPreferenceChangeListener listener =
                new SharedPreferences.OnSharedPreferenceChangeListener(){
                    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
                        // listener implementation
                        if (key.equals(KEY_PREF_HABIT_DESCRIPTION) || key.equals(KEY_PREF_HABIT_SUMMARY)) {
                            Preference pref = findPreference(key);
                            // Set summary to be the user-description for the selected value
                            pref.setSummary(prefs.getString(key, ""));
                            // Return new values
                            if (key.equals(KEY_PREF_HABIT_DESCRIPTION))
                                intent.putExtra("habit_text",prefs.getString(key,""));
                            else intent.putExtra("habit_summary",prefs.getString(key,""));
                        }
                        else if (key.equals(KEY_PREF_NOTIFICATION_SWITCH)){
                            MainActivity.setAllNotifications(getActivity());

                        }
                    }
                };

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.preferences);

            Preference p;
            // Set habit text
            p = getPreferenceManager().findPreference(KEY_PREF_HABIT_DESCRIPTION);
            p.setSummary(intent.getStringExtra("habit_text"));
            ((EditTextPreference)p).setText(intent.getStringExtra("habit_text"));

            // Set habit summary
            p = getPreferenceManager().findPreference(KEY_PREF_HABIT_SUMMARY);
            p.setSummary(intent.getStringExtra("habit_summary"));
            ((EditTextPreference)p).setText(intent.getStringExtra("habit_summary"));

            // Set habit notification time
            p = getPreferenceManager().findPreference(KEY_PREF_HABIT_SUMMARY);
            p.setSummary(intent.getStringExtra("habit_summary"));
            ((EditTextPreference)p).setText(intent.getStringExtra("habit_summary"));

            // Set habit start date
            p = getPreferenceManager().findPreference(KEY_PREF_DATE);
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

