package za.co.twinc.everydayhabit;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;

public class NewHabitActivity extends AppCompatActivity {

    Intent requestIntent;
    private EditText editTextHabit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_habit);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        // Get intent
        requestIntent = getIntent();

        editTextHabit = (EditText) findViewById(R.id.editTextHabit);

    }

    public void onButtonDoneClick(View view) {
        requestIntent.putExtra("habit", String.valueOf(editTextHabit.getText()));
        setResult(Activity.RESULT_OK, requestIntent);
        finish();
    }

}
