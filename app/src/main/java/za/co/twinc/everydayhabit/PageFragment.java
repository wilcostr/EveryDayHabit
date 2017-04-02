package za.co.twinc.everydayhabit;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by wilco on 2017/04/02.
 * EveryDayHabit
 */

// Class containing Fragments for swiping through
public class PageFragment extends Fragment {
    // TextView in a Fragment to display full habit text
    TextView textViewHabit;
    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.content_page_fragment, container, false);
        Bundle bundle = getArguments();
        textViewHabit = (TextView) view.findViewById(R.id.textView_swipe);

        // Load Shared Preferences of habit
        int habitNum = MainActivity.loadHabitMap(getContext())[bundle.getInt("num")];
        String habitText = MainActivity.getStringFromPrefs(getContext(), MainActivity.HABIT_PREFS+habitNum,
                "habit", "No Habit Set");

        textViewHabit.setText(habitText);
        return view;
    }
}
