package za.co.twinc.everydayhabit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.TimeUnit;

import static za.co.twinc.everydayhabit.MainActivity.EDIT_DAY_REQUEST;
import static za.co.twinc.everydayhabit.MainActivity.HABIT_PREFS;
import static za.co.twinc.everydayhabit.MainActivity.MAIN_PREFS;
import static za.co.twinc.everydayhabit.MainActivity.NEW_HABIT_REQUEST;
import static za.co.twinc.everydayhabit.MainActivity.NUM_LOG_ENTRIES;
import static za.co.twinc.everydayhabit.MainActivity.getIntFromPrefs;
import static za.co.twinc.everydayhabit.MainActivity.getLongFromPrefs;
import static za.co.twinc.everydayhabit.MainActivity.getStringFromPrefs;
import static za.co.twinc.everydayhabit.MainActivity.habitNumFromIndex;

/**
 * Created by wilco on 2017/04/02.
 * EveryDayHabit
 */

// Class containing Fragments for swiping through
public class PageFragment extends Fragment {
    // TextView in a Fragment to display full habit text
    TextView textViewHabit;
    private GridView gridContent;
    private int habitNum;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.content_page_fragment, container, false);
        Bundle bundle = getArguments();

        textViewHabit = (TextView) view.findViewById(R.id.textView_swipe);
        gridContent = (GridView) view.findViewById(R.id.content_grid);

        int habitPosition = bundle.getInt("num");
        if (habitPosition+1 > getIntFromPrefs(getContext(),MAIN_PREFS,"num_habits",0)){
            // Display add new habit button
            textViewHabit.setText(getString(R.string.txt_add_new_habit));
            gridContent.setVisibility(View.GONE);

            ImageButton newHabitButton = (ImageButton) view.findViewById(R.id.button_new_habit);
            newHabitButton.setVisibility(View.VISIBLE);

            newHabitButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent newHabit = new Intent(getContext(), NewHabitActivity.class);
                    getActivity().startActivityForResult(newHabit, NEW_HABIT_REQUEST);
                }
            });

            return view;
        }

        // Load Shared Preferences of habit
        habitNum = habitNumFromIndex(getContext(), habitPosition);
        final String habitText = getStringFromPrefs(getContext(), HABIT_PREFS+habitNum,
                "habit", getString(R.string.txt_habit));

        textViewHabit.setText(habitText);

        // Calculate offset to display past 49 days
        int i = 0;
        while (getIntFromPrefs(getContext(), HABIT_PREFS+habitNum, "log_entry_"+(48+i),-1) != -1)
            i += 7;
        final int offset = i;

        displayHabitContent(offset);

        gridContent.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                long timeStart = getLongFromPrefs(getContext(),HABIT_PREFS+habitNum,"date", 1490000000000L);
                long timeDiff = System.currentTimeMillis() - timeStart;
                long numDays =  TimeUnit.DAYS.convert(timeDiff,TimeUnit.MILLISECONDS);

                // Subtract offset from numDays
                numDays -= offset;

                // Fix numDays if only starting tomorrow
                if (timeDiff < 0)
                    numDays = -1;

                if (position == numDays) {
                    // Send intent to EditDayActivity
                    Intent editDay = new Intent(getContext(), EditDayActivity.class);
                    editDay.putExtra("clicked_position", position+offset);
                    editDay.putExtra("habit",getStringFromPrefs(getContext(),HABIT_PREFS+habitNum,
                            "habit",getString(R.string.edit_day_default)));

                    getActivity().startActivityForResult(editDay, EDIT_DAY_REQUEST);
                }
                else if (position == numDays+1) {
                    // Clicked on tomorrow
                    Toast.makeText(getContext(), "Cannot edit tomorrow's entry", Toast.LENGTH_LONG).show();
                }
                else if (position < numDays) {
                    // Clicked on an old entry
                    String reason = getStringFromPrefs(getContext(),HABIT_PREFS+habitNum,
                            "log_reason_" + position,
                            getString(R.string.txt_no_reason));
                    String praise = getPraise();
                    if (getIntFromPrefs(getContext(), HABIT_PREFS+habitNum,"log_entry_"+position,-1) == 0)
                        reason = praise;
                    Toast.makeText(getContext(), reason, Toast.LENGTH_LONG).show();
                }
            }
        });

        return view;
    }

    public void displayHabitContent(int offset){
        int[] log_entries = new int[NUM_LOG_ENTRIES];
        SharedPreferences habit_log = getContext().getSharedPreferences(HABIT_PREFS+habitNum, 0);

        long timeDiff = System.currentTimeMillis() - habit_log.getLong("date", 1490000000000L);
        long numDays =  TimeUnit.DAYS.convert(timeDiff,TimeUnit.MILLISECONDS);

        // Subtract offset from numDays
        numDays -= offset;

        for (int i = 0; i < NUM_LOG_ENTRIES; i++) {
            log_entries[i] = habit_log.getInt("log_entry_" + (i + offset), -1);
            if (log_entries[i] == -1 && i < numDays)
                log_entries[i] = 1;             // Assume failure with no report
        }

        // Initialise gridContent with latest log_entries
        gridContent.setAdapter(new ImageAdapter(getContext(), log_entries, offset));
    }

    public String getPraise(){
        String[] praise = getResources().getStringArray(R.array.praise);
        return praise[(int)(Math.random()*praise.length)];
//        switch (Math.random()*)
//        getString(R.string.praise)Math.random()
    }
}
