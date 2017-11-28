package za.co.twinc.everydayhabit;

import android.app.Activity;
import android.content.DialogInterface;
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
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.format.DateUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

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
        Toolbar bar = findViewById(R.id.toolbar);
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

        // Track the clicks on the version preference
        long clickTracker;
        int clickCounter;

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

            clickTracker = 0L;
            clickCounter = 0;

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
            if (p != null) {
                p.setSummary(versionName);
                p.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    @Override
                    public boolean onPreferenceClick(Preference preference) {
                        // Reset if taking longer than two seconds
                        if (System.currentTimeMillis() - clickTracker > 2000)
                            clickCounter = 0;

                        // Set the time of the first click of the sequence
                        if (clickCounter == 0)
                            clickTracker = System.currentTimeMillis();

                            // This is the third click
                        else if (clickCounter == 2) {
                            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                            final EditText input = new EditText(getActivity());
                            //input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                            builder.setView(input);

                            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (input.getText().toString().toLowerCase().equals("twincapps")) {
                                        // Activate premium for a year, save the date in clickTracker
                                        clickTracker += DateUtils.YEAR_IN_MILLIS;
                                        SharedPreferences mainPrefs = getActivity().getSharedPreferences(
                                                MainActivity.MAIN_PREFS, 0);
                                        SharedPreferences.Editor editor = mainPrefs.edit();
                                        editor.putLong("premium", clickTracker);
                                        editor.apply();

                                        Toast.makeText(getActivity(), R.string.welcome_premium, Toast.LENGTH_LONG).show();
                                    }

                                }
                            });
                            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });

                            builder.show();


                        }
                        clickCounter++;
                        return true;
                    }
                });
            }

            // Set notification tone
            p = preferenceManager.findPreference(KEY_PREF_NOTIFICATION_TONE);
            String toneStr = PreferenceManager.getDefaultSharedPreferences(getActivity())
                    .getString(KEY_PREF_NOTIFICATION_TONE, Settings.System.DEFAULT_NOTIFICATION_URI.toString());
            if ( toneStr.equals(Settings.System.DEFAULT_NOTIFICATION_URI.toString()) ) {
                PreferenceManager.getDefaultSharedPreferences(getActivity()).edit()
                        .putString(KEY_PREF_NOTIFICATION_TONE, Settings.System.DEFAULT_NOTIFICATION_URI.toString())
                        .apply();
            }

            setRingtoneSummary(p, toneStr);

            // Update summary on change of notification tone
            if (p != null){
                p.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                    @Override
                    public boolean onPreferenceChange(Preference preference, Object o) {
                        return setRingtoneSummary(preference, (String)o);
                    }
                });
            }
        }

        private boolean setRingtoneSummary(Preference preference, String summary){
            Ringtone tone = RingtoneManager.getRingtone(getActivity(), Uri.parse(summary));
            if (tone != null)
                try {
                    preference.setSummary(tone.getTitle(getActivity()));
                }catch (java.lang.SecurityException se){
                    preference.setSummary("Unknown ringtone");
                }
            else
                preference.setSummary("None");
            if (preference.getSummary().equals("Unknown ringtone"))
                preference.setSummary("None");
            return true;
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

