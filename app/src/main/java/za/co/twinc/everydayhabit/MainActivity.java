package za.co.twinc.everydayhabit;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.appindexing.Action;
import com.google.android.gms.appindexing.AppIndex;
import com.google.android.gms.appindexing.Thing;
import com.google.android.gms.common.api.GoogleApiClient;

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

    public static Context context;
    public static final String MAIN_PREFS = "main_app_prefs";
    public static final String HABIT_PREFS = "habit_prefs_";
    public static final int NUM_LOG_ENTRIES = 49;
    static final int EDIT_DAY_REQUEST = 1;
    static final int NEW_HABIT_REQUEST = 2;
    static final int SETTINGS_REQUEST = 3;
    int streak_longest, streak_current, days_fail, days_fail_legit, days_success;
    int current_habit;

    private GridView gridContent;
    private TextView textViewSuccessRate, textViewCurrentStreak, textViewLongestStreak;
    private TextView textViewMotivation;

    SwipeAdapter swipeAdapter;
    ViewPager viewPager;


    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    private GoogleApiClient client;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        context = this;

        SharedPreferences main_log = getSharedPreferences(MAIN_PREFS, 0);

        current_habit = getIntent().getIntExtra("habit",-1);

        if (current_habit != -1){
            //Main activity started with intent
            SharedPreferences.Editor editor = main_log.edit();
            editor.putInt("habit_to_display", current_habit);
            editor.apply();
            // Send intent to EditDayActivity
            Intent editDay = new Intent(getApplicationContext(), EditDayActivity.class);
            //TODO: Add editDay logic here
            editDay.putExtra("clicked_position", 13);
            editDay.putExtra("habit", getStringFromPrefs(HABIT_PREFS+current_habit,
                    "habit",getString(R.string.edit_day_default)));

            startActivityForResult(editDay, EDIT_DAY_REQUEST);
        }
        else{
            current_habit = main_log.getInt("habit_to_display",0);
        }

        // ViewPager and its adapters use support library
        // fragments, so use getSupportFragmentManager.
        swipeAdapter = new SwipeAdapter(getSupportFragmentManager());
        viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(swipeAdapter);
        viewPager.setCurrentItem(currentHabitIndex());

        TabLayout tabs = (TabLayout) findViewById(R.id.tabs);
        tabs.setupWithViewPager(viewPager);

        //TODO: Fix viewPager height

        //Initialise stats
        textViewSuccessRate = (TextView) findViewById(R.id.textViewSuccessRate);
        textViewCurrentStreak = (TextView) findViewById(R.id.textViewCurrentStreak);
        textViewLongestStreak = (TextView) findViewById(R.id.textViewLongestStreak);

        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected (int position) {
                SharedPreferences main_log = getSharedPreferences(MAIN_PREFS, 0);
                SharedPreferences.Editor editor = main_log.edit();
                int habit_num = loadHabitMap()[position];
                editor.putInt("habit_to_display", habit_num);
                editor.apply();
                current_habit = habit_num;
                displayHabitContent();
            }
        });

        //MobileAds.initialize(getApplicationContext(),"ca-app-pub-5782047288878600~9640464773");
        AdView mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("5F2995EE0A8305DEB4C48C77461A7362")
                .build();
        mAdView.loadAd(adRequest);

        //Initialise and load motivation text
        textViewMotivation = (TextView) findViewById(R.id.textView_motivation);
        loadNewMotivation();

        //TODO: Ticks should be set at one central place
        //Set ticks for success, fail, fail_legit
        //final int[] TICKS = {R.drawable.tick_green, R.drawable.tick_red, R.drawable.tick_orange_2};

        gridContent = (GridView) findViewById(R.id.content_grid);
        displayHabitContent();

        gridContent.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                long timeStart = getLongFromPrefs(HABIT_PREFS+current_habit,"date", 1490000000000L);
                long timeDiff = System.currentTimeMillis() - timeStart;
                long numDays =  TimeUnit.DAYS.convert(timeDiff,TimeUnit.MILLISECONDS);

                if (position == numDays) {
                    // Send intent to EditDayActivity
                    Intent editDay = new Intent(getApplicationContext(), EditDayActivity.class);
                    editDay.putExtra("clicked_position", position);
                    editDay.putExtra("habit",getStringFromPrefs(HABIT_PREFS+current_habit,
                            "habit",getString(R.string.edit_day_default)));

                    startActivityForResult(editDay, EDIT_DAY_REQUEST);
                } else if (position < numDays) {
                    String reason = getStringFromPrefs(HABIT_PREFS+current_habit,
                            "log_reason_" + position,
                            "You were so lazy, you didn't even give a reason!");
                    //TODO: Add multiple praise randomised
                    if (getIntFromPrefs(HABIT_PREFS+current_habit,"log_entry_"+position,-1) == 0)
                        reason = "Well done, keep it up!";
                    Toast.makeText(MainActivity.this, reason, Toast.LENGTH_LONG).show();
                }
            }
        });

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client = new GoogleApiClient.Builder(this).addApi(AppIndex.API).build();
    }

    public static String getStringFromPrefs(Context ctx, String pref, String key, String default_return){
        //TODO: Use this everywhere!
        SharedPreferences log = ctx.getSharedPreferences(pref, 0);
        return log.getString(key, default_return);
    }

    // Overload method for non-static calls
    public String getStringFromPrefs(String pref, String key, String default_return){
        return getStringFromPrefs(this, pref, key, default_return);
    }

    public static int getIntFromPrefs(Context ctx, String pref, String key, int default_return){
        //TODO: Use this everywhere!
        SharedPreferences log = ctx.getSharedPreferences(pref, 0);
        return log.getInt(key, default_return);
    }

    // Overload method for non-static calls
    public int getIntFromPrefs(String pref, String key, int default_return){
        return getIntFromPrefs(this, pref, key, default_return);
    }

    public static long getLongFromPrefs(Context ctx, String pref, String key, long default_return){
        SharedPreferences log = ctx.getSharedPreferences(pref, 0);
        return log.getLong(key, default_return);
    }

    // Overload method for non-static calls
    public long getLongFromPrefs(String pref, String key, long default_return){
        return getLongFromPrefs(this, pref, key, default_return);
    }


    public void displayHabitContent(){
        int[] log_entries = new int[NUM_LOG_ENTRIES];
        SharedPreferences habit_log = getSharedPreferences(HABIT_PREFS+current_habit, 0);

        long timeDiff = System.currentTimeMillis() - habit_log.getLong("date", 1490000000000L);
        long numDays =  TimeUnit.DAYS.convert(timeDiff,TimeUnit.MILLISECONDS);

        streak_current = 0; streak_longest = 0;
        days_success = 0; days_fail = 0; days_fail_legit = 0;
        for (int i = 0; i < NUM_LOG_ENTRIES; i++) {
            log_entries[i] = habit_log.getInt("log_entry_" + i, -1);

            if (log_entries[i] == -1 && i < numDays)
                log_entries[i] = 1;             // Assume failure with no report

            if (log_entries[i] == 0) {          // Successful day
                days_success ++;
                streak_current ++;
            }
            else if (log_entries[i] == 1){      // Failure with no legit reason
                days_fail ++;
                streak_current = 0;
            }
            else if (log_entries[i] == 2){      // Failure with legit reason
                days_fail_legit ++;
                streak_current = 0;
            }
            if (streak_current > streak_longest) streak_longest = streak_current;
        }

        //Set stats
        int total_days = days_success+days_fail+days_fail_legit;

        // TODO: Include/Exclude legit based on settings
        if (total_days == 0) textViewSuccessRate.setText("0%");
        else textViewSuccessRate.setText(String.format(Locale.UK,"%.1f%%",((100.0*days_success)/total_days)));
        textViewCurrentStreak.setText(String.format(Locale.UK,"%d",streak_current));
        textViewLongestStreak.setText(String.format(Locale.UK,"%d",streak_longest));

        //Initialise gridContent with latest log_entries
        gridContent.setAdapter(new ImageAdapter(this, log_entries));
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
                calendar.setTimeInMillis(getLongFromPrefs(HABIT_PREFS+current_habit,"date",1490000000000L));
                String dateString = DateFormat.getDateFormat(this).format(calendar.getTime());
                startSettings.putExtra("habit_date", dateString);

                startActivityForResult(startSettings, SETTINGS_REQUEST);
                return true;
            case R.id.action_reset:
                resetAll();
                recreate();
                return true;
            case R.id.action_new_habit:
                Intent newHabit = new Intent(getApplicationContext(), NewHabitActivity.class);
                startActivityForResult(newHabit, NEW_HABIT_REQUEST);
                return true;
            case R.id.action_delete_habit:
                deleteHabit();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * ATTENTION: This was auto-generated to implement the App Indexing API.
     * See https://g.co/AppIndexing/AndroidStudio for more information.
     */
    public Action getIndexApiAction() {
        Thing object = new Thing.Builder()
                .setName("Main Page") // TODO: Define a title for the content shown.
                // TODO: Make sure this auto-generated URL is correct.
                .setUrl(Uri.parse("http://[ENTER-YOUR-URL-HERE]"))
                .build();
        return new Action.Builder(Action.TYPE_VIEW)
                .setObject(object)
                .setActionStatus(Action.STATUS_TYPE_COMPLETED)
                .build();
    }

    @Override
    public void onStart() {
        super.onStart();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        client.connect();
        AppIndex.AppIndexApi.start(client, getIndexApiAction());
    }

    @Override
    public void onStop() {
        super.onStop();

        // ATTENTION: This was auto-generated to implement the App Indexing API.
        // See https://g.co/AppIndexing/AndroidStudio for more information.
        AppIndex.AppIndexApi.end(client, getIndexApiAction());
        client.disconnect();
    }

    public void onButtonNextMotivationClick(View view){
        loadNewMotivation();
    }

    public void resetAll(){
        //TODO: Reset all shared preferences
        SharedPreferences log = getSharedPreferences(MAIN_PREFS, 0);
        SharedPreferences.Editor editor = log.edit();
        editor.clear();
        editor.apply();
    }

    public void deleteHabit(){

        final SharedPreferences habit_log = getSharedPreferences(HABIT_PREFS+current_habit, 0);
        final String habitText = habit_log.getString("habit","Habit");

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete: " + habitText);

        builder.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {

                //Wipe shared preferences log for habit
                SharedPreferences.Editor habit_editor = habit_log.edit();
                habit_editor.clear();

                //Get shared preferences of main log
                SharedPreferences main_log = getSharedPreferences(MAIN_PREFS, 0);
                int size = main_log.getInt("num_habits",0);
                SharedPreferences.Editor main_editor = main_log.edit();
                main_editor.putInt("num_habits", size-1);

                int[] habitMap = loadHabitMap();
                boolean move = false;
                for (int i=0; i<size; i++){
                    if(move)
                        habitMap[i-1] = habitMap[i];
                    if(habitMap[i] == current_habit)
                        move = true;
                }

                Toast.makeText(MainActivity.this, habitText+" has been deleted.",
                        Toast.LENGTH_LONG).show();

                habit_editor.apply();
                main_editor.apply();
                saveHabitMap(habitMap);

                //TODO: Not display first habit after delete
                current_habit = habitMap[0];
                swipeAdapter.notifyDataSetChanged();
                viewPager.setCurrentItem(0);

            }
        });

        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        builder.create().show();

    }

    public boolean saveHabitMap(int[] map){
        SharedPreferences main_log = getSharedPreferences(MAIN_PREFS, 0);
        SharedPreferences.Editor main_editor = main_log.edit();
        for(int i=0;i<main_log.getInt("num_habits",0);i++)
            main_editor.putInt("habit_map_" + i, map[i]);
        return main_editor.commit();
    }

    public static int[] loadHabitMap(Context ctx){
        SharedPreferences main_log = ctx.getSharedPreferences(MAIN_PREFS, 0);
        int size = main_log.getInt("num_habits",1);
        int[] map = new int[size];
        for(int i=0;i<size;i++)
            map[i] = main_log.getInt("habit_map_" + i, i);
        return map;
    }

    // Overload method for non-static calls
    public int[] loadHabitMap(){ return loadHabitMap(this);}

    public int currentHabitIndex(){
        int[] map = loadHabitMap();
        int i = 0;
        while(i<map.length){
            if (map[i] == current_habit)
                return i;
            i++;
        }
        return 0;
    }

    // TODO: public int habitIndex(int num)

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

                setAllNotificatoins();

                swipeAdapter.notifyDataSetChanged();
                recreate();
            }
            else if (requestCode == EDIT_DAY_REQUEST) {
                SharedPreferences habit_log = getSharedPreferences(HABIT_PREFS+current_habit, 0);
                SharedPreferences.Editor habit_editor = habit_log.edit();
                int log_position = data.getIntExtra("clicked_position",0);
                int log_entry    = data.getIntExtra("state",0);
                habit_editor.putInt("log_entry_" + log_position, log_entry);

                String reason = data.getStringExtra("reason");
                if (reason != null) habit_editor.putString("log_reason_" + log_position, reason);
                habit_editor.apply();
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

                // Save first day, setting hours and minutes to zero.
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(System.currentTimeMillis());
                cal.set(Calendar.HOUR_OF_DAY,0);
                cal.set(Calendar.MINUTE,0);
                if (data.getBooleanExtra("tomorrow",true))
                    cal.add(Calendar.DATE,1);
                habit_editor.putLong("date", cal.getTimeInMillis());

                habit_editor.apply();

                // Update main_log habit counts
                main_editor.putInt("num_habits", num_habits+1);
                main_editor.putInt("habit_map_"+num_habits, next_habit);

                main_editor.putInt("next_habit", next_habit+1);
                main_editor.apply();
                swipeAdapter.notifyDataSetChanged();
                viewPager.setCurrentItem(num_habits);

                // Set notification
                setHabitNotification(next_habit);
            }

        }
    }

    // Set a recurring notification
    public static void setHabitNotification(Context ctx, int habitNum){
        String habitText = getStringFromPrefs(ctx, HABIT_PREFS+habitNum,"habit","perform your habit");
        int habitTime = getIntFromPrefs(ctx, HABIT_PREFS+habitNum,"notify",18*60);

        long timeStart = getLongFromPrefs(ctx, HABIT_PREFS+habitNum,"date", 1490000000000L);
        long timeDiff = System.currentTimeMillis() - timeStart;
        long numDays =  TimeUnit.DAYS.convert(timeDiff,TimeUnit.MILLISECONDS);

        if (timeDiff < 0 || getIntFromPrefs(ctx,HABIT_PREFS+habitNum,"log_entry_"+numDays, -1) != -1) {
            // Only starting habit tomorrow OR already reported progress today
            habitTime += 24*60; // Next notification only tomorrow
        }

        AlarmReceiver alarmReceiver = new AlarmReceiver();
        alarmReceiver.setAlarm(ctx, habitText, habitNum, habitTime);
    }

    // Overload method for non-static calls
    public void setHabitNotification(int habitNum){setHabitNotification(this, habitNum); }

    public static void setAllNotifications(Context ctx){
        int[] map = loadHabitMap(ctx);
        int i = 0;
        while(i<map.length){
            setHabitNotification(ctx, map[i]);
            i++;
        }
    }

    // Overload method for non-static calls
    public void setAllNotificatoins(){ setAllNotifications(this);}

    public void loadNewMotivation(){
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
            return getIntFromPrefs(MAIN_PREFS, "num_habits", 0);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            // PageTitle displays habit summaries in tabs above the Fragments being swiped
            int habitNum = loadHabitMap()[position];
            return getStringFromPrefs(HABIT_PREFS+habitNum, "habit_summary", "Habit");
        }
    }



    // Class containing Fragments for swiping through
    public static class PageFragment extends Fragment {
        // TextView in a Fragment to display full habit text
        TextView textViewHabit;
        @Override
        public View onCreateView(LayoutInflater inflater,
                                 ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.content_page_fragment, container, false);
            Bundle bundle = getArguments();
            textViewHabit = (TextView) view.findViewById(R.id.textView_swipe);

            // TODO: Remove use of context here (maybe move PageFragment to own class?
            // Load Shared Preferences of habit
            int habitNum = loadHabitMap(context)[bundle.getInt("num")];
            SharedPreferences habit_log = context.getSharedPreferences(HABIT_PREFS+habitNum, 0);
            String habitText = habit_log.getString("habit","No Habit Set");

            textViewHabit.setText(habitText);
            return view;
        }
    }

}
