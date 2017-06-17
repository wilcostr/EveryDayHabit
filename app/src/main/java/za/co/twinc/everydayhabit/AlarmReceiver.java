package za.co.twinc.everydayhabit;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.view.View;

import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import static android.content.Context.NOTIFICATION_SERVICE;
import static android.support.v4.app.NotificationCompat.CATEGORY_REMINDER;

/**
 * Created by wilco on 2017/02/02.
 * EveryDayHabit
 */

public class AlarmReceiver extends BroadcastReceiver
{
    // The app's AlarmManager, which provides access to the system alarm services.
    private AlarmManager alarmMgr;
    // The pending intent that is triggered when the alarm fires.
    private PendingIntent alarmIntent;

    @Override
    public void onReceive(Context context, Intent intent)
    {
        int habitNum = intent.getIntExtra("habit_number",-1);

        long timeStart = MainActivity.getDateFromPrefs(context, MainActivity.HABIT_PREFS+habitNum
        );
        long timeDiff = System.currentTimeMillis() - timeStart;
        long numDays =  TimeUnit.DAYS.convert(timeDiff,TimeUnit.MILLISECONDS);

        if (timeDiff < 0 || MainActivity.getIntFromPrefs(context,MainActivity.HABIT_PREFS+habitNum,
                "log_entry_"+numDays, -1) != -1) {
            // Only starting habit tomorrow OR already reported progress today
            return;
        }

        // Create intent to open Main, load habit number in extras
        Intent openMainIntent = new Intent(context, MainActivity.class);
        openMainIntent.putExtra("habit", habitNum);
        openMainIntent.putExtra("day", (int)numDays);

        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(MainActivity.class);
        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(openMainIntent);

        PendingIntent openMainPendingIntent = stackBuilder.getPendingIntent(habitNum,
                PendingIntent.FLAG_ONE_SHOT);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String ringtonePreference = prefs.getString(SettingsActivity.KEY_PREF_NOTIFICATION_TONE,
                Settings.System.DEFAULT_NOTIFICATION_URI.toString());
        Uri ringtoneUri = Uri.parse(ringtonePreference);

        // Create notification builder
        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(context)
                .setSmallIcon(R.drawable.small_icon)
                .setLargeIcon(BitmapFactory.decodeResource(context.getResources(), R.mipmap.ic_launcher_edh))
                .setContentTitle(context.getString(R.string.app_name))
                .setContentText(intent.getStringExtra("habit_text"))
                .setContentIntent(openMainPendingIntent)
                .setAutoCancel(true)
                .setCategory(CATEGORY_REMINDER)
                .setVibrate(new long[]{1000, 200, 100, 200})
                .setSound(ringtoneUri);

        // Build the notification
        Notification notification = mBuilder.build();

        // Hide small icon in Lollipop and Marshmallow notification pull down list as it looks shit
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP &&
                Build.VERSION.SDK_INT <= Build.VERSION_CODES.M) {
            int smallIconViewId = context.getResources().getIdentifier("right_icon", "id", android.R.class.getPackage().getName());
            //noinspection deprecation
            if (notification.contentView != null)
                //noinspection deprecation
                notification.contentView.setViewVisibility(smallIconViewId, View.INVISIBLE);
        }

        // Issues notification
        NotificationManager mNotifyMgr = (NotificationManager)context.getSystemService(NOTIFICATION_SERVICE);
        mNotifyMgr.notify(habitNum, notification);
    }

    public void setAlarm(Context context, String habitText, int habitNum, int habitTime) {
        alarmMgr = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, AlarmReceiver.class);
        intent.putExtra("habit_text",habitText);
        intent.putExtra("habit_number",habitNum);
        alarmIntent = PendingIntent.getBroadcast(context, habitNum, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        int day = habitTime/(60*24);
        int hour = (habitTime-24*day)/60;
        int minute = habitTime%60;

        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);

        alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP,
                calendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, alarmIntent);
    }

    public void cancelAlarm() {
        // If the alarm has been set, cancel it.
        if (alarmMgr != null) {
            alarmMgr.cancel(alarmIntent);
        }
    }

    public void setBootReceiver(Context context){
        // Enable {@code BootReceiver} to automatically restart the alarm when the
        // device is rebooted.
        ComponentName receiver = new ComponentName(context, BootReceiver.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP);
    }

    public void cancelBootReceiver(Context context){
        // Disable {@code BootReceiver} so that it doesn't automatically restart the
        // alarm when the device is rebooted.
        ComponentName receiver = new ComponentName(context, BootReceiver.class);
        PackageManager pm = context.getPackageManager();

        pm.setComponentEnabledSetting(receiver,
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                PackageManager.DONT_KILL_APP);
    }
}