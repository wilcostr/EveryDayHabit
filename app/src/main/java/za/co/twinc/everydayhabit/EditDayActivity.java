package za.co.twinc.everydayhabit;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

public class EditDayActivity extends AppCompatActivity {

    private Intent requestIntent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_day);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);

        // Get intent data
        requestIntent = getIntent();

        TextView textViewHabit = (TextView)findViewById(R.id.text_view_habit);
        String dayString = requestIntent.getStringExtra("day_string");
        if (dayString == null)
            dayString = getString(R.string.edit_day_today);
        textViewHabit.setText(getString(R.string.edit_day_did_you) +
                requestIntent.getStringExtra("habit").toLowerCase() +
                dayString);

        // These start off invisible
        final TextView textView2 = (TextView) findViewById(R.id.textView2);
        final CheckBox checkBox = (CheckBox) findViewById(R.id.checkBox);

        final Button button_done = (Button) findViewById(R.id.button_done);
        final EditText editText = (EditText) findViewById(R.id.editText);

        String comment = requestIntent.getStringExtra("comment");
        if (comment != null && !comment.isEmpty() && !comment.equals(getString(R.string.txt_no_log)))
            editText.setText(comment);

        final RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radio_edit_day);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // checkedId is the RadioButton selected
                if (checkedId == R.id.radioButton_yes ){
                    textView2.setText(getString(R.string.edit_day_comments));
                    checkBox.setVisibility(View.GONE);
                    editText.setHint(getString(R.string.edit_day_log_comment));
                }
                else{
                    textView2.setText(getString(R.string.edit_day_why));
                    checkBox.setVisibility(View.VISIBLE);
                    editText.setHint(getString(R.string.edit_day_reason));
                }
            }
        });

        int radioState = requestIntent.getIntExtra("entry",0);
        if (radioState > 0)
            radioGroup.check(R.id.radioButton_no);
        if (radioState == 2)
            checkBox.setChecked(true);


        button_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Check if anything is to be added to the log
                String reason = String.valueOf(editText.getText());
                if (reason.length()>0) requestIntent.putExtra("reason", reason);

                if (radioGroup.getCheckedRadioButtonId() == R.id.radioButton_yes){
                    processClick(0);
                }
                else {
                    boolean legit = checkBox.isChecked();
                    if (legit) processClick(2);
                    else processClick(1);
                }
            }
        });

        editText.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (event != null && (event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                    //editText.setInputType(0);
                    InputMethodManager in = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                    in.hideSoftInputFromWindow(editText.getApplicationWindowToken(),InputMethodManager.HIDE_NOT_ALWAYS);
                    editText.setInputType(1);
                }
                return false;
            }
        });

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }

    private void processClick(int state) {
        requestIntent.putExtra("state", state);
        setResult(Activity.RESULT_OK, requestIntent);
        finish();
    }
}
