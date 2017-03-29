package za.co.twinc.everydayhabit;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by wilco on 2017/03/23.
 * EveryDayHabit
 */

public class BootReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED"))
        {
            MainActivity.setAllNotifications(MainActivity.context);
        }
    }
}
