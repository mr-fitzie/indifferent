package com.synthtc.indifferent.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.synthtc.indifferent.IndifferentReceiver;
import com.synthtc.indifferent.MainActivity;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.DurationFieldType;

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
            long midnightEastern = DateTime.now(DateTimeZone.forID("US/Eastern")).withTimeAtStartOfDay().withFieldAdded(DurationFieldType.days(), 1).getMillis();
            //midnightEastern = DateTime.now().withFieldAdded(DurationFieldType.minutes(), 15).getMillis();
            alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, midnightEastern, AlarmManager.INTERVAL_DAY, pendingIntent);
            //alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + FIFTEEN_SEC_MILLIS, FIFTEEN_SEC_MILLIS, pendingIntent);
            Log.i(MainActivity.LOGTAG, "Alarm set for " + midnightEastern + " repeating every " + AlarmManager.INTERVAL_DAY);
        } else {
            alarmManager.cancel(pendingIntent);
            Log.i(MainActivity.LOGTAG, "Alarm canceled. ");
        }
    }
}
