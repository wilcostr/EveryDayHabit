package za.co.twinc.everydayhabit;

import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by wilco on 2017/07/10.
 * EveryDayHabit
 */

public class MotivationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {

        Log.d("INFORMATION", "Received intent after selection: "+intent.getExtras().get(Intent.EXTRA_CHOSEN_COMPONENT));
        ComponentName target = intent.getParcelableExtra(Intent.EXTRA_CHOSEN_COMPONENT);
        if (target != null) {
            if (target.getClassName().contains("facebook")) {
                //We cannot send text to Facebook, so copy motivation to clipboard
                ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
                ClipData clip = ClipData.newPlainText("Motivation", intent.getStringExtra("Motivation"));
                clipboard.setPrimaryClip(clip);
                Toast.makeText(context, context.getResources().getString(R.string.motivation_copied),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }
}
