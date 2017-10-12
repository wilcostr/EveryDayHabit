package za.co.twinc.everydayhabit;

import android.app.Activity;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TimePicker;
import android.support.v7.widget.Toolbar;


public class NewHabitActivity extends AppCompatActivity{

    private EditText editTextHabit;
    private RadioGroup radioGroup;
    private Button buttonDone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_habit);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null)
            actionBar.setDisplayHomeAsUpEnabled(true);

        //get habit data
        editTextHabit = findViewById(R.id.editTextHabit);

        // Initialise objects that are affected by the presence of data in the NEW HABIT field
        radioGroup = findViewById(R.id.radioGroup);
        buttonDone = findViewById(R.id.button_add_habit);

        shouldShowObjects(false);

        EditText editTextHabit = findViewById(R.id.editTextHabit);
        editTextHabit.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if(charSequence.toString().trim().length()==0){
                    shouldShowObjects(false);
                } else {
                    shouldShowObjects(true);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

    }

    private void shouldShowObjects(boolean show){
        buttonDone.setEnabled(show);
        if (show)
            radioGroup.setVisibility(View.VISIBLE);
        else
            radioGroup.setVisibility(View.GONE);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Back button in actionbar finishes the activity
            case android.R.id.home:
                this.finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @SuppressWarnings("UnusedParameters")
    public void onButtonDoneClick(View view) {
        DialogFragment newFragment = new TimePickerFragment();
        newFragment.show(getFragmentManager(),"timePicker");
    }

    @SuppressWarnings("UnusedParameters")
    public void onButtonSuggestionsClick(View view){
        showRadioButtonDialog();
    }

    private void showRadioButtonDialog() {
        final String[] values = getResources().getStringArray(R.array.habit_suggestions);
        // custom dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.suggestions_button)
                .setItems(values, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        editTextHabit.setText(values[i]);
                    }
                })
                .create().show();
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
            EditText editTextHabit = getActivity().findViewById(R.id.editTextHabit);
            RadioButton radioButtonTomorrow = getActivity().findViewById(R.id.radioButtonTomorrow);

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
