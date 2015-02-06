package com.synthtc.indifferent.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

import com.synthtc.indifferent.IndifferentReceiver;
import com.synthtc.indifferent.MainActivity;

import org.joda.time.DateTime;
import org.joda.time.DurationFieldType;

/**
 * Created by Chris on 1/10/2015.
 */
public class Alarm {
    public static final int REQUEST_CODE = 0;
    public static final int REQUEST_CODE_RECHECK = 1;
    public static final int THIRTY_SEC_MILLIS = 30000;
    private static boolean mRecheck = false;

    public static void set(Context context, boolean recheck) {
        handleAlarm(context, true, recheck);
    }

    public static void cancel(Context context, boolean recheck) {
        handleAlarm(context, false, recheck);
    }

    private static void handleAlarm(Context context, boolean set, boolean recheck) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(context, IndifferentReceiver.class);
        intent.setAction(IndifferentReceiver.INTENT_MEH);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context, recheck ? REQUEST_CODE_RECHECK : REQUEST_CODE, intent, 0);

        if (set) {
            if (recheck && !mRecheck) {
                mRecheck = true;
                long recheckTime = SystemClock.elapsedRealtime() + THIRTY_SEC_MILLIS;
                alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, recheckTime + THIRTY_SEC_MILLIS, THIRTY_SEC_MILLIS, pendingIntent);
                Log.d(MainActivity.LOGTAG, "ReCheck alarm set for " + recheckTime + " repeating every " + THIRTY_SEC_MILLIS);
            } else if (!recheck) {
                long midnightEastern = DateTime.now(Helper.TIME_ZONE).withTimeAtStartOfDay().withFieldAdded(DurationFieldType.days(), 1).getMillis();
                alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, midnightEastern, AlarmManager.INTERVAL_DAY, pendingIntent);
                Log.d(MainActivity.LOGTAG, "Alarm set for " + midnightEastern + " repeating every " + AlarmManager.INTERVAL_DAY);
            }
        } else {
            alarmManager.cancel(pendingIntent);
            Log.d(MainActivity.LOGTAG, "Alarm canceled. Recheck: " + recheck);
        }
    }
}
