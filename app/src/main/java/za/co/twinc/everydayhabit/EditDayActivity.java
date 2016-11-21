package za.co.twinc.everydayhabit;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;


public class EditDayActivity extends AppCompatActivity {

    Intent requestIntent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_day);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // Get intent data
        requestIntent = getIntent();

        //These start off invisible
        final TextView textView2 = (TextView) findViewById(R.id.textView2);
        final EditText editText = (EditText) findViewById(R.id.editText);
        final CheckBox checkBox = (CheckBox) findViewById(R.id.checkBox);
        final Button button_done = (Button) findViewById(R.id.button_done);

        final RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radio_edit_day);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // checkedId is the RadioButton selected
                System.out.println("checkedId: " + checkedId);
                if (checkedId == R.id.radioButton_yes ){
                    textView2.setVisibility(View.GONE);
                    editText.setVisibility(View.GONE);
                    checkBox.setVisibility(View.GONE);
                }
                else{
                    textView2.setVisibility(View.VISIBLE);
                    editText.setVisibility(View.VISIBLE);
                    checkBox.setVisibility(View.VISIBLE);
                }


            }
        });
        button_done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (radioGroup.getCheckedRadioButtonId() == R.id.radioButton_yes){
                    processClick(0);
                }
                else {
                    String reason = String.valueOf(editText.getText());
                    if (reason.length()>0) requestIntent.putExtra("reason", reason);
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
    }


    public void processClick(int state) {
        requestIntent.putExtra("state", state);
        setResult(Activity.RESULT_OK, requestIntent);
        finish();
    }

}
