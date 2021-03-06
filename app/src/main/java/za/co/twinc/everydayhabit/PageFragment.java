package za.co.twinc.everydayhabit;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.getkeepsafe.taptargetview.TapTarget;
import com.getkeepsafe.taptargetview.TapTargetView;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import static za.co.twinc.everydayhabit.MainActivity.EDIT_DAY_REQUEST;
import static za.co.twinc.everydayhabit.MainActivity.HABIT_PREFS;
import static za.co.twinc.everydayhabit.MainActivity.MAIN_PREFS;
import static za.co.twinc.everydayhabit.MainActivity.NEW_HABIT_REQUEST;
import static za.co.twinc.everydayhabit.MainActivity.NUM_LOG_ENTRIES;
import static za.co.twinc.everydayhabit.MainActivity.getIntFromPrefs;
import static za.co.twinc.everydayhabit.MainActivity.getDateFromPrefs;
import static za.co.twinc.everydayhabit.MainActivity.getStringFromPrefs;
import static za.co.twinc.everydayhabit.MainActivity.habitNumFromIndex;

/**
 * Created by wilco on 2017/04/02.
 * EveryDayHabit
 */

// Class containing Fragments for swiping through
public class PageFragment extends Fragment {
    private GridView gridContent;
    private int habitNum;
    private boolean showHint = true;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.content_page_fragment, container, false);
        Bundle bundle = getArguments();

        TextView textViewHabit = view.findViewById(R.id.textView_swipe);
        gridContent = view.findViewById(R.id.content_grid);

        int habitPosition = bundle.getInt("num");
        if (habitPosition+1 > getIntFromPrefs(getContext(),MAIN_PREFS,"num_habits",0)){
            // Display add new habit button
            textViewHabit.setText(getString(R.string.txt_add_new_habit));
            gridContent.setVisibility(View.GONE);

            ImageButton newHabitButton = view.findViewById(R.id.button_new_habit);
            newHabitButton.setVisibility(View.VISIBLE);

            ImageButton editHabitButton = view.findViewById(R.id.button_edit);
            editHabitButton.setVisibility(View.GONE);

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
        while (getIntFromPrefs(getContext(), HABIT_PREFS+habitNum, "log_entry_"+(NUM_LOG_ENTRIES-1+i),-1) != -1)
            i += 7;
        final int offset = i;

        int[] log_entries = new int[NUM_LOG_ENTRIES];
        SharedPreferences habit_log = getContext().getSharedPreferences(HABIT_PREFS+habitNum, 0);

        long startTime = habit_log.getLong("date", 1490000000000L);
        long timeDiff = System.currentTimeMillis() - startTime;
        long numDays =  TimeUnit.DAYS.convert(timeDiff,TimeUnit.MILLISECONDS);


        // Subtract offset from numDays
        numDays -= offset;

        for (int j = 0; j < NUM_LOG_ENTRIES; j++) {
            log_entries[j] = habit_log.getInt("log_entry_" + (j + offset), -1);
            if (log_entries[j] == -1 && j < numDays)
                log_entries[j] = 3;             // Assume failure with no report
        }

        // Initialise gridContent with latest log_entries
        ImageAdapter imageAdapter = new ImageAdapter(getContext(), log_entries, offset, startTime);
        gridContent.setAdapter(imageAdapter);

        gridContent.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {

                int numDays = getNumDays(offset);

                if ( (position == numDays) &&
                        (getIntFromPrefs(getContext(),HABIT_PREFS+habitNum,"log_entry_"+(position+offset),-1) == -1) ) {
                    // Log progress for today: Send intent to EditDayActivity
                    Intent editDay = new Intent(getContext(), EditDayActivity.class);
                    editDay.putExtra("clicked_position", position+offset);
                    editDay.putExtra("habit",getStringFromPrefs(getContext(),HABIT_PREFS+habitNum,
                            "habit",getString(R.string.edit_day_default)));

                    getActivity().startActivityForResult(editDay, EDIT_DAY_REQUEST);
                }
                else if (position == numDays+1) {
                    // Clicked on tomorrow
                    Toast.makeText(getContext(), getString(R.string.txt_cannot_tomorrow), Toast.LENGTH_SHORT).show();
                }
                else if (position <= numDays) {
                    // Clicked on an old entry
                    String reason = getStringFromPrefs(getContext(),HABIT_PREFS+habitNum,
                            "log_reason_"+(position+offset),
                            getString(R.string.txt_no_reason));
                    if ( (getIntFromPrefs(getContext(), HABIT_PREFS+habitNum,"log_entry_"+(position+offset),-1) == 0) &&
                            (reason.equals(getString(R.string.txt_no_reason))) )
                        reason = getString(R.string.txt_no_comment);
                    Toast.makeText(getContext(), reason, Toast.LENGTH_SHORT).show();
                }
            }
        });

        gridContent.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener(){
            @Override
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                int numDays = getNumDays(offset);
                if (position <= numDays) {
                    // Send intent to EditDayActivity
                    Intent editDay = new Intent(getContext(), EditDayActivity.class);
                    editDay.putExtra("clicked_position", position+offset);
                    editDay.putExtra("habit",getStringFromPrefs(getContext(),HABIT_PREFS+habitNum,
                            "habit",getString(R.string.edit_day_default)));
                    editDay.putExtra("entry",getIntFromPrefs(getContext(),HABIT_PREFS+habitNum,
                            "log_entry_"+(position+offset),-1));
                    editDay.putExtra("comment",getStringFromPrefs(getContext(),HABIT_PREFS+habitNum,
                            "log_reason_"+(position+offset),""));

                    String dayString = getString(R.string.edit_day_today);
                    if (position == numDays-1)
                        dayString = getString(R.string.edit_day_yesterday);
                    else if (position < numDays-1){
                        long dayLong = getDateFromPrefs(getContext(), HABIT_PREFS+habitNum);
                        dayLong += (long)24*60*60*1000*(position+offset);
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTimeInMillis(dayLong);
                        dayString = getString(R.string.edit_day_on) + DateFormat.getDateFormat(getContext()).format(calendar.getTime()) + "?";
                    }
                    editDay.putExtra("day_string",dayString);

                    getActivity().startActivityForResult(editDay, EDIT_DAY_REQUEST);
                }
                return true;
            }
        });

        // If this is the first habit, and still no progress logged
        if ((getIntFromPrefs(getContext(),MAIN_PREFS, "num_habits",-1)==1 ) &&
                (getIntFromPrefs(getContext(),HABIT_PREFS+habitNum,"log_entry_0",-1) == -1)) {
            // Listener to pick up when gridContent is done loading layout
            gridContent.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    // After the layout is loaded add another 1s delay with Handler
                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        public void run() {
                            // Only load one hint
                            if (showHint) {
                                final Activity activity = getActivity();
                                if (activity != null) {
                                    TapTargetView.showFor(activity,
                                            TapTarget.forView(gridContent.getChildAt(0), getString(R.string.day_tip_title),
                                                    getString(R.string.day_tip_text))
                                                    .drawShadow(true),
                                            new TapTargetView.Listener() {          // The listener can listen for regular clicks, long clicks or cancels
                                                @Override
                                                public void onTargetClick(TapTargetView view) {
                                                    super.onTargetClick(view);
                                                    if (getNumDays(0) == -1)
                                                        Toast.makeText(getContext(), getString(R.string.txt_cannot_tomorrow), Toast.LENGTH_SHORT).show();
                                                    else {
                                                        // Log progress for first day: Send intent to EditDayActivity
                                                        Intent editDay = new Intent(getContext(), EditDayActivity.class);
                                                        editDay.putExtra("clicked_position", 0);
                                                        editDay.putExtra("habit", getStringFromPrefs(getContext(), HABIT_PREFS + habitNum,
                                                                "habit", getString(R.string.edit_day_default)));
                                                        activity.startActivityForResult(editDay, EDIT_DAY_REQUEST);
                                                    }
                                                }
                                            });
                                    showHint = false;
                                }
                            }
                        }
                    }, 1000);
                }
            });
        }
        return view;
    }

    private int getNumDays(int offset) {
        long timeStart = getDateFromPrefs(getContext(), HABIT_PREFS+habitNum);
        long timeDiff = System.currentTimeMillis() - timeStart;
        long numDays =  TimeUnit.DAYS.convert(timeDiff,TimeUnit.MILLISECONDS);

        // Subtract offset from numDays
        numDays -= offset;

        // Fix numDays if only starting tomorrow
        if (timeDiff < 0)
            numDays = -1;
        return (int)numDays;
    }
}
