package za.co.twinc.everydayhabit;

import android.app.NotificationManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Calendar;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    public static final String MAIN_PREFS = "main_app_prefs";
    public static final String HABIT_PREFS = "habit_prefs_";
    public static final int NUM_LOG_ENTRIES = 49;

    public static final int EDIT_DAY_REQUEST        = 1;
    public static final int NEW_HABIT_REQUEST       = 2;
    private final int       SETTINGS_REQUEST        = 3;
    private final int       CONGRATULATE_REQUEST    = 4;

    private int current_habit;

    private TextView textViewSuccessRate, textViewCurrentStreak, textViewLongestStreak;
    private TextView textViewMotivation;

    private SwipeAdapter swipeAdapter;
    private ViewPager viewPager;
    private static AlarmReceiver alarmReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences main_log = getSharedPreferences(MAIN_PREFS, 0);

        // Try to get intent that opened main (only the case when opened from notification)
        Intent startMainIntent = getIntent();
        current_habit = -1;
        int currentDay = -1;

        if (startMainIntent != null) {
            current_habit = startMainIntent.getIntExtra("habit", -1);
            currentDay = startMainIntent.getIntExtra("day", -1);
        }

        if (current_habit != -1){
            //Main activity started with intent
            SharedPreferences.Editor editor = main_log.edit();
            editor.putInt("habit_to_display", current_habit);
            editor.apply();
            // Send intent to EditDayActivity
            Intent editDay = new Intent(getApplicationContext(), EditDayActivity.class);
            editDay.putExtra("clicked_position", currentDay);
            editDay.putExtra("habit", getStringFromPrefs(HABIT_PREFS+current_habit,
                    "habit",getString(R.string.edit_day_default)));

            startActivityForResult(editDay, EDIT_DAY_REQUEST);
        }
        else{
            current_habit = main_log.getInt("habit_to_display",0);
        }

        // Display First Use Info if no habits created
        if (getIntFromPrefs(MAIN_PREFS,"num_habits",0) == 0){
            current_habit = -1;
            Intent intent = new Intent(getApplicationContext(), FirstUseActivity.class);
            startActivity(intent);
        }

        // Set alarmReceiver for notifications
        alarmReceiver = new AlarmReceiver();
        setAllNotifications();

        // ViewPager and its adapters use support library
        // fragments, so use getSupportFragmentManager.
        swipeAdapter = new SwipeAdapter(getSupportFragmentManager());
        viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(swipeAdapter);
        viewPager.setCurrentItem(currentHabitIndex());
        viewPager.setCurrentItem(currentHabitIndex());

        TabLayout tabs = (TabLayout) findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);

        //Initialise stats
        textViewSuccessRate = (TextView) findViewById(R.id.textViewSuccessRate);
        textViewCurrentStreak = (TextView) findViewById(R.id.textViewCurrentStreak);
        textViewLongestStreak = (TextView) findViewById(R.id.textViewLongestStreak);

        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected (int position) {
                SharedPreferences main_log = getSharedPreferences(MAIN_PREFS, 0);
                SharedPreferences.Editor editor = main_log.edit();
                int habit_num = habitNumFromIndex(position);
                editor.putInt("habit_to_display", habit_num);
                editor.apply();
                current_habit = habit_num;
                displayHabitContent();
            }
        });
        displayHabitContent();

        //MobileAds.initialize(getApplicationContext(),"ca-app-pub-5782047288878600~9640464773");
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("5F2995EE0A8305DEB4C48C77461A7362")
                .build();
        mAdView.loadAd(adRequest);

        //Initialise and load motivation text
        textViewMotivation = (TextView) findViewById(R.id.textView_motivation);
        loadNewMotivation();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    private void displayHabitContent(){
        // Quick return if we are displaying add new habit button
        if (current_habit==-1) return;

        int streak_longest, streak_current, days_fail, days_fail_legit, days_success;

        SharedPreferences habit_log = getSharedPreferences(HABIT_PREFS+current_habit, 0);
        SharedPreferences.Editor habit_editor = habit_log.edit();

        // Include/Exclude legit excuses based on settings
        SharedPreferences settingsPref = PreferenceManager.getDefaultSharedPreferences(this);
        boolean includeLegit = settingsPref.getBoolean(SettingsActivity.KEY_PREF_LEGIT_SWITCH, false);

        long timeDiff = System.currentTimeMillis() - habit_log.getLong("date", 1490000000000L);
        long numDays =  TimeUnit.DAYS.convert(timeDiff,TimeUnit.MILLISECONDS);

        streak_current = 0; streak_longest = 0;
        days_success = 0; days_fail = 0; days_fail_legit = 0;
        for (int i = 0; i <= numDays; i++) {
            int log_entry = habit_log.getInt("log_entry_"+i, -1);

            if (log_entry == -1 && i < numDays) {
                log_entry = 1;             // Assume failure with no report
                habit_editor.putString("log_reason_"+i, getString(R.string.txt_no_log));
                habit_editor.putInt("log_entry_"+i, 1);
            }
            if (log_entry == 0) {          // Successful day
                days_success ++;
                streak_current ++;
            }
            else if (log_entry == 1){      // Failure with no legit reason
                days_fail ++;
                streak_current = 0;
            }
            else if (log_entry == 2){      // Failure with legit reason
                if (includeLegit){
                    days_success ++;
                    streak_current ++;
                }
                else{
                    days_fail_legit ++;
                    streak_current = 0;
                }
            }
            if (streak_current > streak_longest) streak_longest = streak_current;
        }
        habit_editor.apply();

        //Set stats
        int total_days = days_success+days_fail+days_fail_legit;

        String successRate = "0%";
        String currentStreak = String.format(Locale.UK,"%d",streak_current);
        String longestStreak = String.format(Locale.UK,"%d",streak_longest);
        if (total_days > 0) successRate = String.format(Locale.UK,"%.1f%%",((100.0*days_success)/total_days));
        textViewSuccessRate.setText(successRate);
        textViewCurrentStreak.setText(currentStreak);
        textViewLongestStreak.setText(longestStreak);

        if (total_days==NUM_LOG_ENTRIES && habit_log.getBoolean("showCongrats",true)){
            Intent intent = new Intent(this, Congratulations.class);
            intent.putExtra("habitText", habit_log.getString("habit",getString(R.string.txt_habit)));
            intent.putExtra("rate", successRate);
            intent.putExtra("streak", longestStreak);
            startActivityForResult(intent,CONGRATULATE_REQUEST);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (id){
            case R.id.action_settings:
                Intent startSettings = new Intent(getApplicationContext(), SettingsActivity.class);
                startSettings.putExtra("habit_text",
                        getStringFromPrefs(HABIT_PREFS+current_habit,"habit","Habit"));
                startSettings.putExtra("habit_summary",
                        getStringFromPrefs(HABIT_PREFS+current_habit,"habit_summary","Habit"));
                startSettings.putExtra("habit_time",
                        getIntFromPrefs(HABIT_PREFS+current_habit,"notify",8));

                Calendar calendar = Calendar.getInstance();
                calendar.setTimeInMillis(getDateFromPrefs(HABIT_PREFS+current_habit));
                String dateString = DateFormat.getDateFormat(this).format(calendar.getTime());
                startSettings.putExtra("habit_date", dateString);

                startActivityForResult(startSettings, SETTINGS_REQUEST);
                return true;
            case R.id.action_new_habit:
                Intent newHabit = new Intent(getApplicationContext(), NewHabitActivity.class);
                startActivityForResult(newHabit, NEW_HABIT_REQUEST);
                return true;
            case R.id.action_delete_habit:
                deleteHabit();
                return true;
            case R.id.action_start:
                Intent intent = new Intent(getApplicationContext(), FirstUseActivity.class);
                startActivity(intent);
                return true;
            case R.id.action_rate:
                //TODO: Rate this app.
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public static String getStringFromPrefs(Context ctx, String pref, String key, String default_return){
        SharedPreferences log = ctx.getSharedPreferences(pref, 0);
        return log.getString(key, default_return);
    }

    // Overload method for non-static calls
    private String getStringFromPrefs(String pref, String key, String default_return){
        return getStringFromPrefs(this, pref, key, default_return);
    }

    public static int getIntFromPrefs(Context ctx, String pref, String key, int default_return){
        SharedPreferences log = ctx.getSharedPreferences(pref, 0);
        return log.getInt(key, default_return);
    }

    // Overload method for non-static calls
    private int getIntFromPrefs(String pref, String key, int default_return){
        return getIntFromPrefs(this, pref, key, default_return);
    }

    public static long getDateFromPrefs(Context ctx, String pref){
        SharedPreferences log = ctx.getSharedPreferences(pref, 0);
        return log.getLong("date", 1490000000000L);
    }

    // Overload method for non-static calls
    private long getDateFromPrefs(String pref){
        return getDateFromPrefs(this, pref);
    }

    public void onButtonNextMotivationClick(View view){
        loadNewMotivation();
    }

    private void deleteHabit(){
        // Can't delete the button
        if (current_habit == -1) return;

        // Remove notification
        // Call setHabitNotification for alarmReceiver to set up the correct intent to cancel
        setHabitNotification(getApplicationContext(), current_habit);
        alarmReceiver.cancelAlarm();

        final SharedPreferences habit_log = getSharedPreferences(HABIT_PREFS+current_habit, 0);

        final String habitText = habit_log.getString("habit",getString(R.string.txt_habit));

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete: " + habitText);

        builder.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                // Wipe shared preferences log for habit
                SharedPreferences.Editor habit_editor = habit_log.edit();
                habit_editor.clear();

                // Get shared preferences of main log
                SharedPreferences main_log = getSharedPreferences(MAIN_PREFS, 0);
                int size = main_log.getInt("num_habits",0);
                SharedPreferences.Editor main_editor = main_log.edit();
                main_editor.putInt("num_habits", size-1);

                if (size>1) {
                    // Shift all habits after current_habit by one to overwrite current_habit
                    int[] habitMap = loadHabitMap();
                    boolean shift = false;
                    for (int i = 0; i < size; i++) {
                        if (shift)
                            habitMap[i - 1] = habitMap[i];
                        if (habitMap[i] == current_habit) {
                            shift = true;
                            // Display the next habit (limit to new size-1 if last habit deleted)
                            current_habit = habitMap[Math.min(i + 1, size - 2)];
                        }

                    }
                    saveHabitMap(habitMap);
                }

                Toast.makeText(MainActivity.this, habitText+" has been deleted.",
                        Toast.LENGTH_LONG).show();

                habit_editor.apply();
                main_editor.apply();
                swipeAdapter.notifyDataSetChanged();
            }
        });

        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        builder.create().show();
    }

    private void saveHabitMap(int[] map){
        SharedPreferences main_log = getSharedPreferences(MAIN_PREFS, 0);
        SharedPreferences.Editor main_editor = main_log.edit();
        for(int i=0;i<main_log.getInt("num_habits",0);i++)
            main_editor.putInt("habit_map_" + i, map[i]);
        main_editor.apply();
    }

    private static int[] loadHabitMap(Context ctx){
        SharedPreferences main_log = ctx.getSharedPreferences(MAIN_PREFS, 0);
        int size = main_log.getInt("num_habits",1);
        int[] map = new int[size];
        for(int i=0;i<size;i++)
            map[i] = main_log.getInt("habit_map_" + i, i);
        return map;
    }

    // Overload method for non-static calls
    private int[] loadHabitMap(){ return loadHabitMap(this);}

    private int currentHabitIndex(){
        int[] map = loadHabitMap();
        int i = 0;
        while(i<map.length){
            if (map[i] == current_habit)
                return i;
            i++;
        }
        return 0;
    }

    public static int habitNumFromIndex(Context ctx, int idx){
        if (idx < getIntFromPrefs(ctx, MAIN_PREFS, "num_habits",0))
            return loadHabitMap(ctx)[idx];
        // Return -1 if index exceeds limit
        return -1;
    }

    // Overload method for non-static calls
    private int habitNumFromIndex(int idx) { return habitNumFromIndex(this, idx);}

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Make sure the request was successful
        if (resultCode == RESULT_OK) {
            // Check which request we're responding to
            if (requestCode == SETTINGS_REQUEST) {
                SharedPreferences habit_log = getSharedPreferences(HABIT_PREFS+current_habit, 0);
                SharedPreferences.Editor habit_editor = habit_log.edit();
                habit_editor.putString("habit", data.getStringExtra("habit_text"));
                habit_editor.putString("habit_summary", data.getStringExtra("habit_summary"));
                habit_editor.putInt("notify", data.getIntExtra("habit_time",0));
                habit_editor.apply();

                // Update notification
                setHabitNotification(current_habit);

                // Update display
                swipeAdapter.notifyDataSetChanged();
                displayHabitContent();

            }
            else if (requestCode == EDIT_DAY_REQUEST) {
                // Cancel the notification if it is still visible
                NotificationManager mNotifyMgr = (NotificationManager)getApplicationContext().getSystemService(NOTIFICATION_SERVICE);
                mNotifyMgr.cancel(current_habit);

                // Update habit_log
                SharedPreferences habit_log = getSharedPreferences(HABIT_PREFS+current_habit, 0);
                SharedPreferences.Editor habit_editor = habit_log.edit();
                int log_position = data.getIntExtra("clicked_position",0);
                int log_entry    = data.getIntExtra("state",0);
                habit_editor.putInt("log_entry_" + log_position, log_entry);

                String reason = data.getStringExtra("reason");
                if (reason != null)
                    habit_editor.putString("log_reason_" + log_position, reason);
                else
                    habit_editor.remove("log_reason_" + log_position);
                habit_editor.apply();
                swipeAdapter.notifyDataSetChanged();
                displayHabitContent();
            }
            else if (requestCode == NEW_HABIT_REQUEST) {
                SharedPreferences main_log = getSharedPreferences(MAIN_PREFS, 0);
                SharedPreferences.Editor main_editor = main_log.edit();

                String newHabitText = data.getStringExtra("habit");
                int newHabitTime    = data.getIntExtra("time",0);

                int num_habits = main_log.getInt("num_habits",0);
                int next_habit = main_log.getInt("next_habit",0);

                // Create shared preference log for new habit
                SharedPreferences habit_log = getSharedPreferences(HABIT_PREFS+next_habit, 0);
                SharedPreferences.Editor habit_editor = habit_log.edit();
                habit_editor.putString("habit", newHabitText);
                habit_editor.putString("habit_summary", newHabitText.split(" ", 2)[0]);
                habit_editor.putInt("notify", newHabitTime);

                // Save first day, setting hours and minutes to zero
                Calendar cal = Calendar.getInstance();
                long timeNow = System.currentTimeMillis();
                cal.setTimeInMillis(timeNow);
                cal.set(Calendar.HOUR_OF_DAY,0);
                cal.set(Calendar.MINUTE,0);
                if (data.getBooleanExtra("tomorrow",true))
                    cal.add(Calendar.DATE,1);
                habit_editor.putLong("date", cal.getTimeInMillis());

                habit_editor.apply();

                // Show new habit
                current_habit = next_habit;

                // Update main_log habit counts
                main_editor.putInt("num_habits", num_habits+1);
                main_editor.putInt("habit_map_"+num_habits, next_habit);

                main_editor.putInt("next_habit", next_habit+1);
                main_editor.apply();
                swipeAdapter.notifyDataSetChanged();
                viewPager.setCurrentItem(num_habits);

                // Set notification
                setHabitNotification(next_habit);
                // Activate boot receiver if this is the first habit added
                if (num_habits==0) alarmReceiver.setBootReceiver(getApplicationContext());
            }
            else if (requestCode == CONGRATULATE_REQUEST){
                // Update habit_log to not show congratulations message again
                SharedPreferences habit_log = getSharedPreferences(HABIT_PREFS+current_habit, 0);
                SharedPreferences.Editor habit_editor = habit_log.edit();
                habit_editor.putBoolean("showCongrats", false);
                habit_editor.apply();
            }
        }
    }

    // Set a recurring notification
    private static void setHabitNotification(Context ctx, int habitNum){
        // Return if Notifications switched off in settings
        SharedPreferences settingsPref = PreferenceManager.getDefaultSharedPreferences(ctx);
        boolean notify = settingsPref.getBoolean(SettingsActivity.KEY_PREF_NOTIFICATION_SWITCH, false);
        if (!notify)
            return;

        String habitText = getStringFromPrefs(ctx, HABIT_PREFS+habitNum,"habit","perform your habit");
        int habitTime = getIntFromPrefs(ctx, HABIT_PREFS+habitNum,"notify",18*60);

        // Add a day if the notification time is already passed today
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(System.currentTimeMillis());
        if(cal.get(Calendar.HOUR_OF_DAY)*60 + cal.get(Calendar.MINUTE) > habitTime)
            habitTime += 24*60;

        alarmReceiver.setAlarm(ctx, habitText, habitNum, habitTime);
    }

    // Overload method for non-static calls
    private void setHabitNotification(int habitNum){setHabitNotification(this, habitNum); }

    public static void setAllNotifications(Context ctx){
        int[] map = loadHabitMap(ctx);
        int i = 0;
        while(i<map.length){
            setHabitNotification(ctx, map[i]);
            i++;
        }
        alarmReceiver.setBootReceiver(ctx);
    }

    // Overload method for non-static calls
    private void setAllNotifications(){ setAllNotifications(this);}

    // Clear a notification
    public static void cancelAllNotifications(Context ctx){
        int[] map = loadHabitMap(ctx);
        int i = 0;
        while(i<map.length) {
            // Call setHabitNotification for alarmReceiver to set up the correct intent to enable
            // cancelling the individual notification
            setHabitNotification(ctx, map[i]);
            alarmReceiver.cancelAlarm();
            i++;
        }
        // Stop notifications being set on device boot
        alarmReceiver.cancelBootReceiver(ctx);
    }

    private void loadNewMotivation(){
        //Start network thread here
        new GetMotivation().execute
                ("http://api.forismatic.com/api/1.0/?method=getQuote&lang=en&format=text");
    }

    private class GetMotivation extends AsyncTask<String , Void ,String> {
        String server_response;

        @Override
        protected String doInBackground(String... strings) {

            URL url;
            HttpURLConnection urlConnection;

            try {
                url = new URL(strings[0]);
                urlConnection = (HttpURLConnection) url.openConnection();

                int responseCode = urlConnection.getResponseCode();

                if(responseCode == HttpURLConnection.HTTP_OK){
                    server_response = readStream(urlConnection.getInputStream());
                    return server_response;
                }

            } catch (IOException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            if (s != null)
                textViewMotivation.setText(s.replace("(","\n- ").replace(")",""));
        }
    }

    // Converting InputStream to String
    private String readStream(InputStream in) {

        BufferedReader reader = null;
        StringBuilder response = new StringBuilder();
        try {
            reader = new BufferedReader(new InputStreamReader(in));
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return response.toString();
    }

    // Class containing the adapter to cycle through Fragments by swiping
    private class SwipeAdapter extends FragmentPagerAdapter {
        SwipeAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = new PageFragment();
            Bundle bundle = new Bundle();
            bundle.putInt("num",position);
            fragment.setArguments(bundle);
            return fragment;
        }

        @Override
        public int getCount() {
            return getIntFromPrefs(MAIN_PREFS, "num_habits", 0) + 1;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            // Add new habit at end of habit list
            if (getIntFromPrefs(MAIN_PREFS, "num_habits",0)-1 < position)
                return getString(R.string.txt_new_habit);

            // Return habit summary for tab title
            int habitNum = habitNumFromIndex(position);
            return getStringFromPrefs(HABIT_PREFS+habitNum, "habit_summary", getString(R.string.txt_habit));
        }

        public int getItemPosition(Object object){
            return POSITION_NONE;
        }
    }
}
