package za.co.twinc.everydayhabit;

import android.app.Activity;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TimePicker;
import android.support.v7.widget.Toolbar;

public class NewHabitActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_habit);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        // Hide Button till a habit is entered
        final Button buttonDone = (Button)findViewById(R.id.button_add_habit);
        buttonDone.setEnabled(false);

        EditText editTextHabit = (EditText)findViewById(R.id.editTextHabit);
        editTextHabit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.toString().trim().length()==0){
                    buttonDone.setEnabled(false);
                } else {
                    buttonDone.setEnabled(true);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

    }

    public void onButtonDoneClick(View view) {
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getSupportFragmentManager(), "timePicker");
    }

    public static class TimePickerFragment extends DialogFragment
            implements TimePickerDialog.OnTimeSetListener {

        @NonNull
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
            requestIntent.putExtra("habit", String.valueOf(editTextHabit.getText()).trim());
            if(radioButtonTomorrow.isChecked()) requestIntent.putExtra("tomorrow", true);
            else requestIntent.putExtra("tomorrow", false);
            requestIntent.putExtra("time", hour*60+minute);
            getActivity().setResult(Activity.RESULT_OK, requestIntent);
            getActivity().finish();
        }
    }
}
