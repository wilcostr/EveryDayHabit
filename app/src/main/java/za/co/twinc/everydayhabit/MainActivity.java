package za.co.twinc.everydayhabit;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.FrameLayout;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

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
import java.net.MalformedURLException;
import java.net.URL;

public class MainActivity extends AppCompatActivity {

    public static Context context;
    public static final String MAIN_PREFS = "main_app_prefs";
    public static final String HABIT_PREFS = "habit_prefs_";
    public static final int NUM_LOG_ENTRIES = 49;
    static final int EDIT_DAY_REQUEST = 1;
    static final int NEW_HABIT_REQUEST = 2;
    int streak_longest, streak_current, days_fail, days_fail_legit, days_success;
    int current_habit;

    private AdView mAdView;
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

        // ViewPager and its adapters use support library
        // fragments, so use getSupportFragmentManager.
        swipeAdapter = new SwipeAdapter(getSupportFragmentManager());
        viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(swipeAdapter);
        //TODO: Fix viewPager height

        //Initialise stats
        textViewSuccessRate = (TextView) findViewById(R.id.textViewSuccessRate);
        textViewCurrentStreak = (TextView) findViewById(R.id.textViewCurrentStreak);
        textViewLongestStreak = (TextView) findViewById(R.id.textViewLongestStreak);

        viewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected (int position) {
                SharedPreferences main_log = context.getSharedPreferences(MAIN_PREFS, 0);
                SharedPreferences.Editor editor = main_log.edit();
                editor.putInt("habit_to_display", position);
                editor.commit();
                current_habit = position;
                displayHabitContent();
                //textViewLongestStreak.setText(""+position);
            }
        });

        //MobileAds.initialize(getApplicationContext(),"ca-app-pub-5782047288878600~9640464773");
        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("5F2995EE0A8305DEB4C48C77461A7362")
                .build();
        mAdView.loadAd(adRequest);

        //Initialise and load motivation text
        textViewMotivation = (TextView) findViewById(R.id.textView_motivation);
        loadNewMotivation();

        //TODO: Ticks should be set at one central place
        //Set ticks for success, fail, fail_legit
        final int[] TICKS = {R.drawable.tick_green, R.drawable.tick_red, R.drawable.tick_orange_2};


        //Get log_entries from HABIT_PREFS and calculate stats
        SharedPreferences main_log = getSharedPreferences(MAIN_PREFS, 0);
        current_habit = main_log.getInt("habit_to_display",0);

        gridContent = (GridView) findViewById(R.id.content_grid);
        displayHabitContent();


        gridContent.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                SharedPreferences log = getSharedPreferences(HABIT_PREFS+current_habit, 0);

                int prev_log_entry = log.getInt("log_entry_" + (position - 1), -1);
                int next_log_entry = log.getInt("log_entry_" + (position + 1), -1);
                int cur_log_entry = log.getInt("log_entry_" + position, -1);
                if ((prev_log_entry >= 0 && next_log_entry == -1) || position == 0) {
                    // Send intent to EditDayActivity
                    Intent editDay = new Intent(getApplicationContext(), EditDayActivity.class);
                    editDay.putExtra("clicked_position", position);

                    startActivityForResult(editDay, EDIT_DAY_REQUEST);
                } else if (next_log_entry >= 0) {
                    String reason = log.getString("log_reason_" + position,
                            "You were so lazy, you didn't even give a reason!");
                    //TODO: Add multiple praise rondomised
                    if (cur_log_entry == 0) reason = "Good going!";
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

    public void displayHabitContent(){
        int[] log_entries = new int[NUM_LOG_ENTRIES];
        SharedPreferences habit_log = getSharedPreferences(HABIT_PREFS+current_habit, 0);

        streak_current = 0; streak_longest = 0;
        days_success = 0; days_fail = 0; days_fail_legit = 0;
        for (int i = 0; i < NUM_LOG_ENTRIES; i++) {
            log_entries[i] = habit_log.getInt("log_entry_" + i, -1);

            if (log_entries[i] == 0) {          //successful day
                days_success ++;
                streak_current ++;
            }
            else if (log_entries[i] == 1){      //Failure with no legit reason
                days_fail ++;
                streak_current = 0;
            }
            else if (log_entries[i] == 2){      //Failure with legit reason
                days_fail_legit ++;
                streak_current = 0;
            }
            if (streak_current > streak_longest) streak_longest = streak_current;
        }

        //Set stats
        int total_days = days_success+days_fail+days_fail_legit;
        if (total_days == 0) textViewSuccessRate.setText("0%");
        else {
            textViewSuccessRate.setText(String.format("%.1f",((100.0*days_success)/total_days))+"%");
        }
        textViewCurrentStreak.setText(""+streak_current);
        textViewLongestStreak.setText(""+streak_longest);

        //Initialise gridContent with latest log_entries
        gridContent.setAdapter(new ImageAdapter(context, log_entries));
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
                return true;
            case R.id.action_reset:
                resetAll();
                recreate();
                return true;
            case R.id.action_new_habit:
                Intent newHabit = new Intent(getApplicationContext(), NewHabitActivity.class);
                startActivityForResult(newHabit, NEW_HABIT_REQUEST);
                return true;
            case R.id.test:
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
        editor.commit();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Make sure the request was successful
        if (resultCode == RESULT_OK) {
            // Check which request we're responding to
            if (requestCode == EDIT_DAY_REQUEST) {
                SharedPreferences habit_log = getSharedPreferences(HABIT_PREFS+current_habit, 0);
                SharedPreferences.Editor habit_editor = habit_log.edit();
                int log_position = data.getExtras().getInt("clicked_position");
                int log_entry = data.getExtras().getInt("state");
                habit_editor.putInt("log_entry_" + log_position, log_entry);

                String reason = data.getExtras().getString("reason");
                if (reason != null) habit_editor.putString("log_reason_" + log_position, reason);
                habit_editor.commit();
                displayHabitContent();
            }
            else if (requestCode == NEW_HABIT_REQUEST) {
                SharedPreferences main_log = getSharedPreferences(MAIN_PREFS, 0);
                SharedPreferences.Editor main_editor = main_log.edit();

                String newHabit = data.getExtras().getString("habit");
                int new_num_habits = main_log.getInt("num_habits",0) + 1;
                int next_habit = main_log.getInt("next_habit",0);

                //Create shared preference main_log for new habit
                SharedPreferences habit_log = getSharedPreferences(HABIT_PREFS+next_habit, 0);
                SharedPreferences.Editor habit_editor = habit_log.edit();
                habit_editor.putString("habit", newHabit);
                habit_editor.commit();

                main_editor.putInt("num_habits", new_num_habits);
                main_editor.putInt("next_habit", next_habit+1);
                main_editor.commit();
                swipeAdapter.notifyDataSetChanged();
                viewPager.setCurrentItem(new_num_habits-1);
            }

        }
    }

    public void loadNewMotivation(){
        //Start network thread here
        new GetMotivation().execute
                ("http://api.forismatic.com/api/1.0/?method=getQuote&lang=en&format=text");
    }

    public class GetMotivation extends AsyncTask<String , Void ,String> {
        String server_response;

        @Override
        protected String doInBackground(String... strings) {

            URL url;
            HttpURLConnection urlConnection = null;

            try {
                url = new URL(strings[0]);
                urlConnection = (HttpURLConnection) url.openConnection();

                int responseCode = urlConnection.getResponseCode();

                if(responseCode == HttpURLConnection.HTTP_OK){
                    server_response = readStream(urlConnection.getInputStream());
                    return server_response;
                }

            } catch (MalformedURLException e) {
                e.printStackTrace();
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
        StringBuffer response = new StringBuffer();
        try {
            reader = new BufferedReader(new InputStreamReader(in));
            String line = "";
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

    public class SwipeAdapter extends FragmentStatePagerAdapter {
        public SwipeAdapter(FragmentManager fm) {
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
            SharedPreferences log = getSharedPreferences(MAIN_PREFS, 0);
            return log.getInt("num_habits", 1);
        }
    }

    public static class PageFragment extends Fragment {
        TextView textViewTemp;
        @Override
        public View onCreateView(LayoutInflater inflater,
                                 ViewGroup container, Bundle savedInstanceState) {
            View view = inflater.inflate(R.layout.content_page_fragment, container, false);
            Bundle bundle = getArguments();

            //Load Shared Preferences of habit
            SharedPreferences habit_log = context.getSharedPreferences(HABIT_PREFS+bundle.getInt("num"), 0);

            textViewTemp = (TextView) view.findViewById(R.id.textView_swipe);
            textViewTemp.setText(habit_log.getString("habit","No Habit Set"));
            return view;
        }
    }

}
