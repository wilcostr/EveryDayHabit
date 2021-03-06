package za.co.twinc.everydayhabit;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.TimePicker;

import java.util.Locale;

public class HabitSettings extends Activity {
    private static Intent intent;

    private static final String KEY_PREF_HABIT_DESCRIPTION =     "edit_text_preference_habit_text";
    private static final String KEY_PREF_HABIT_SUMMARY =         "edit_text_preference_habit_summary";
    private static final String KEY_PREF_NOTIFICATION_TIME =     "time_picker_preference_notification";
    private static final String KEY_PREF_DATE =                  "simple_text_habit_date";
    private static final String KEY_PREF_DELETE =                "simple_text_habit_delete";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_settings);
        Toolbar bar = findViewById(R.id.toolbar);
        bar.setTitle(getResources().getString(R.string.action_edit_habit));
        bar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });

        intent = getIntent();

        // Display the fragment as the main content.
        getFragmentManager().beginTransaction()
                .replace(R.id.settings_container, new HabitDialogFragment())
                .commit();
    }

    @Override
    public void onBackPressed() {
        setResult(RESULT_OK,intent);
        super.onBackPressed();
    }

    public static class HabitDialogFragment extends PreferenceFragment
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
                    }
                };

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            // Load the preferences from an XML resource
            addPreferencesFromResource(R.xml.habit_preferences);

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

            // Delete habit
            p = preferenceManager.findPreference(KEY_PREF_DELETE);
            p.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                @Override
                public boolean onPreferenceClick(Preference preference) {
                    intent.putExtra("delete", true);
                    getActivity().onBackPressed();
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

        @Override
        public void onTimeSet(TimePicker view, int hour, int minute) {
            timePref.setSummary(String.format(Locale.UK, "%02d",hour)  + ":" +
                    String.format(Locale.UK, "%02d",minute));
            intent.putExtra("habit_time", hour*60+minute);
        }
    }
}