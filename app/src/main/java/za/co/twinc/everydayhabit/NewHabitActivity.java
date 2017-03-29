package za.co.twinc.everydayhabit;

import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TimePicker;
import android.support.v7.widget.Toolbar;

public class NewHabitActivity extends AppCompatActivity {

    public static Context context;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_habit);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        context = this;

        //TODO: Hide Button till a habit is entered

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

    }

    public void onButtonDoneClick(View view) {
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getSupportFragmentManager(), "timePicker");
    }


    public static class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            int hour = 18;
            int minute = 0;

            // Create a new instance of TimePickerDialog and return it
            final TimePickerDialog tp = new TimePickerDialog(getActivity(), this, hour, minute,
                    DateFormat.is24HourFormat(getActivity()));

            tp.setTitle(getString(R.string.new_habit_notify_title));
            tp.setMessage(getString(R.string.new_habit_notify));
            return tp;
        }

        public void onTimeSet(TimePicker view, int hour, int minute) {
            //get habit data
            EditText editTextHabit = (EditText)getActivity().findViewById(R.id.editTextHabit);
            RadioButton radioButtonTomorrow = (RadioButton) getActivity().findViewById(R.id.radioButtonTomorrow);

            //Return new habit details to main
            Intent requestIntent = getActivity().getIntent();
            requestIntent.putExtra("habit", String.valueOf(editTextHabit.getText()));
            if(radioButtonTomorrow.isChecked()) requestIntent.putExtra("tomorrow", true);
            else requestIntent.putExtra("tomorrow", false);
            requestIntent.putExtra("time", hour*60+minute);
            getActivity().setResult(Activity.RESULT_OK, requestIntent);
            getActivity().finish();
        }

    }

}
