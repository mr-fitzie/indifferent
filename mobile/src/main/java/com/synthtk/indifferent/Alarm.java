package com.synthtk.indifferent;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

/**
 * Created by Chris on 1/10/2015.
 */
public class Alarm {
    public static final int REQUEST_CODE = 0;
    public static final int FIFTEEN_SEC_MILLIS = 15000;

    public static void set(Context context) {
        handleAlarm(context, true);
    }

    public static void cancel(Context context) {
        handleAlarm(context, false);
    }

    private static void handleAlarm(Context context, boolean set) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, IndifferentReceiver.class);
        intent.setAction(IndifferentReceiver.INTENT_MEH);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, REQUEST_CODE, intent, 0);

        if (set) {
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, DateTime.now(DateTimeZone.forID("US/Eastern")).withTimeAtStartOfDay().getMillis(), AlarmManager.INTERVAL_DAY, pendingIntent);
            //alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + FIFTEEN_SEC_MILLIS, FIFTEEN_SEC_MILLIS, pendingIntent);
        } else {
            alarmManager.cancel(pendingIntent);
        }
        // END_INCLUDE (configure_alarm_manager);
        Log.i("RepeatingAlarmFragment", "Alarm set. " + set);
    }
}
