package za.co.twinc.everydayhabit;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;

/**
 * Created by wilco on 2016/12/28.
 */


public class SwipeActivity extends FragmentActivity {
    // When requested, this adapter returns a DemoObjectFragment,
    // representing an object in the collection.
    SwipeAdapter swipeAdapter;
    ViewPager viewPager;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.content_swipe);

        // ViewPager and its adapters use support library
        // fragments, so use getSupportFragmentManager.
        swipeAdapter = new SwipeAdapter(getSupportFragmentManager());
        viewPager = (ViewPager) findViewById(R.id.pager);
        viewPager.setAdapter(swipeAdapter);
    }
}
