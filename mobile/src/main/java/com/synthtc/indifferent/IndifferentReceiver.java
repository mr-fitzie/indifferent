/**
 * Copyright 2015 SYNTHTC

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */
package com.synthtc.indifferent;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import com.synthtc.indifferent.api.Meh;
import com.synthtc.indifferent.ui.SettingsFragment;
import com.synthtc.indifferent.util.Alarm;
import com.synthtc.indifferent.util.Helper;
import com.synthtc.indifferent.util.MehCache;
import com.synthtc.indifferent.util.VolleySingleton;

import org.joda.time.DateTime;
import org.joda.time.Instant;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Locale;

import in.uncod.android.bypass.Bypass;

/**
 * Created by Chris on 1/10/2015.
 */
public class IndifferentReceiver extends BroadcastReceiver {
    public static final String INTENT_MEH = "com.synthtc.indifferent.MEH";
    private static final int NOTIFICATION_ID = 1;
    private NotificationManagerCompat mNotificationManager = null;

    @Override
    public void onReceive(final Context context, final Intent intent) {
        if (mNotificationManager == null) {
            mNotificationManager = NotificationManagerCompat.from(context);
        }

        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        String action = intent.getAction();
        if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            //Log.d(MainActivity.LOGTAG, "BOOT COMPLETED");
            Helper.log(Log.DEBUG, "BOOT COMPLETED");
            if (prefs.getBoolean(SettingsFragment.KEY_ALARM_ENABLE, SettingsFragment.DEFAULT_ALARM_ENABLE)) {
                Alarm.set(context, false);
            }
        } else if (action.equals(INTENT_MEH)) {
            //Log.d(MainActivity.LOGTAG, "alarm triggered");
            Helper.log(Log.DEBUG, "alarm triggered");
            final MehCache mehCache = MehCache.getInstance(context);
            final Instant today = DateTime.now(Helper.TIME_ZONE).withTimeAtStartOfDay().toInstant();
            Meh meh = mehCache.get(today);
            if (meh != null && meh.getDeal() != null) {
                requestImage(context, meh);
            } else {
                String url = context.getString(R.string.api_url, context.getString(R.string.api_key));
                Response.Listener<JSONObject> responseListener = new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        Gson gson = new Gson();
                        Meh meh = gson.fromJson(jsonObject.toString(), Meh.class);
                        Instant instant = mehCache.getInstant(meh);
                        //Log.d(MainActivity.LOGTAG, "IndifferentReceiver onResponse " + instant.toString());
                        Helper.log(Log.DEBUG, "IndifferentReceiver onResponse " + instant.toString());
                        if (meh.getDeal() != null && instant != null && instant.equals(today) || intent.hasExtra("from")) {
                            mehCache.put(instant, jsonObject, true);
                            requestImage(context, meh);
                            //Log.d(MainActivity.LOGTAG, "VolleyResponse " + meh.getDeal().getId());
                            Helper.log(Log.DEBUG, "VolleyResponse " + meh.getDeal().getId());
                            Alarm.cancel(context, true);
                        } else {
                            int retryMin = Integer.valueOf(prefs.getString(SettingsFragment.KEY_ALARM_RETRY_MIN, SettingsFragment.DEFAULT_ALARM_RETRY_MIN));
                            //Log.d(MainActivity.LOGTAG, "IndifferentReceiver not today (" + today.toString() + ") gonna try again for total of " + retryMin + " min");
                            Helper.log(Log.DEBUG, "IndifferentReceiver not today (" + today.toString() + ") gonna try again for total of " + retryMin + " min");
                            if (DateTime.now().getMinuteOfHour() < retryMin) {
                                Alarm.set(context, true);
                            } else {
                                Alarm.cancel(context, true);
                            }
                        }
                    }
                };
                Response.ErrorListener responseErrorListener = new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        //Log.e(MainActivity.LOGTAG, "VolleyError", volleyError);
                        Helper.log(Log.ERROR, "VolleyError", volleyError);
                        Alarm.set(context, true);
                    }
                };
                JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, (String) null, responseListener, responseErrorListener);
                VolleySingleton.getInstance(context.getApplicationContext()).addToRequestQueue(request);
            }
        }
    }

    private void requestImage(final Context context, final Meh meh) {
        Log.d(MainActivity.LOGTAG, "requestImage");
        new Thread(new Runnable() {
            @Override
            public void run() {
                Bitmap bitmap;
                try {
                    bitmap = Picasso.with(context)
                            .load(meh.getDeal().getPhotos()[0])
                            .priority(Picasso.Priority.HIGH)
                            .resize(500, 500)
                            .centerCrop()
                            .get();
                } catch (IOException e) {
                    Helper.log(Log.ERROR, "requestImage", e);
                    bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_error);
                }
                createNotification(context, meh, bitmap);
            }
        }).start();
    }

    private void createNotification(Context context, Meh meh, Bitmap bitmap) {
        Helper.log(Log.DEBUG, "createNotification");
        if (mNotificationManager == null) {
            mNotificationManager = NotificationManagerCompat.from(context);
        }

        Intent appIntent = new Intent(context, MainActivity.class);
        appIntent.setAction(Intent.ACTION_MAIN);
        appIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        PendingIntent appPendingIntent = PendingIntent.getActivity(context, 0, appIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(meh.getDeal().getUrl()));
        PendingIntent browserPendingIntent = PendingIntent.getActivity(context, 0, browserIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        String title = meh.getDeal().getTitle();
        String summary = context.getString(R.string.notify_summary, meh.getDeal().getItems()[0].getCondition(), meh.getDeal().getPrices(context));

        int resId = R.drawable.ic_stat_meh;
        String titleLower = title.toLowerCase(Locale.US);
        String[] sad_words = context.getResources().getStringArray(R.array.sad_words);
        boolean foundSadWord = false;
        for (String sad_word : sad_words) {
            foundSadWord = titleLower.contains(sad_word);
            if (!foundSadWord) {
                break;
            }
        }
        if (foundSadWord) {
            resId = R.drawable.ic_stat_sad;
        } else if (titleLower.contains(context.getString(R.string.deal_fukubukuro).toLowerCase(Locale.US))) {
            resId = R.drawable.ic_stat_fuku;
        }

        NotificationCompat.Action browserAction =
                new NotificationCompat.Action.Builder(R.drawable.ic_stat_browser,
                        context.getString(R.string.action_browser), browserPendingIntent)
                        .build();

        Bypass bypass = new Bypass(context);
        CharSequence features = bypass.markdownToSpannable(meh.getDeal().getFeatures());

        // Create a big text style for the second page
        NotificationCompat.BigTextStyle secondPageStyle = new NotificationCompat.BigTextStyle();
        secondPageStyle.setBigContentTitle(context.getString(R.string.deal_features))
                .bigText(features);

        // Create second page notification
        Notification secondPageNotification =
                new NotificationCompat.Builder(context)
                        .setStyle(secondPageStyle)
                        .build();

        NotificationCompat.WearableExtender wearableExtender =
                new NotificationCompat.WearableExtender()
                        .addPage(secondPageNotification)
                        .addAction(browserAction)
                        .setBackground(bitmap);

        NotificationCompat.BigPictureStyle style = new NotificationCompat.BigPictureStyle()
                .bigPicture(bitmap)
                .setSummaryText(summary);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setSmallIcon(resId)
                .setLargeIcon(bitmap)
                .setContentTitle(title)
                .setContentText(summary)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .setContentIntent(appPendingIntent)
                .setFullScreenIntent(appPendingIntent, true)
                .addAction(resId, context.getString(R.string.app_name), appPendingIntent)
                .addAction(browserAction)
                .setStyle(style)
                .extend(wearableExtender);

        if (resId == R.drawable.ic_stat_fuku) {
            builder.setColor(Color.RED);
        } else if (meh.getDeal().getTheme() != null && meh.getDeal().getTheme().getAccentColor() != null) {
            builder.setColor(Helper.getColor(meh.getDeal().getTheme().getAccentColor()));
        }

        mNotificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}
