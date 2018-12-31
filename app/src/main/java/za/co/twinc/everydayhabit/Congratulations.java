package za.co.twinc.everydayhabit;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

public class Congratulations extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_congratulations);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        String habitText = intent.getStringExtra("habitText").trim().toLowerCase();
        String habitRate = intent.getStringExtra("rate");
        String habitStreak = intent.getStringExtra("streak");

        TextView textView = findViewById(R.id.textViewCongratulations);
        textView.setText(getString(R.string.congratulations_text, habitText, habitRate, habitStreak));
    }

    @SuppressWarnings("UnusedParameters")
    public void onButtonCongratulationsClick(View view){
        setResult(Activity.RESULT_OK);
        finish();
    }
}
