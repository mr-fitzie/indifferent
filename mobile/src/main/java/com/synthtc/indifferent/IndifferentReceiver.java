package com.synthtc.indifferent;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.gson.Gson;
import com.synthtc.indifferent.api.Meh;
import com.synthtc.indifferent.ui.SettingsFragment;
import com.synthtc.indifferent.util.Alarm;
import com.synthtc.indifferent.util.MehCache;
import com.synthtc.indifferent.util.VolleySingleton;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Instant;
import org.json.JSONObject;

/**
 * Created by Chris on 1/10/2015.
 */
public class IndifferentReceiver extends BroadcastReceiver {
    public static final String INTENT_MEH = "com.synthtc.indifferent.MEH";
    private static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager = null;

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (mNotificationManager == null) {
            mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        }
        final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        String action = intent.getAction();
        if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            Log.e(MainActivity.LOGTAG, "BOOT COMPLETED");
            if (prefs.getBoolean(SettingsFragment.KEY_ALARM_ENABLE, SettingsFragment.DEFAULT_ALARM_ENABLE)) {
                Alarm.set(context);
            }
        } else if (action.equals(INTENT_MEH)) {
            Log.e(MainActivity.LOGTAG, "alarm triggered");
            final MehCache mehCache = MehCache.getInstance(context);
            final Instant today = DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay().toInstant();
            Meh meh = mehCache.get(today);
            if (meh != null) {
                requestImage(context, meh);
            } else {
                String url = context.getString(R.string.api_url, context.getString(R.string.api_key));
                Response.Listener<JSONObject> responseListener = new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject jsonObject) {
                        String createdAt = null;
                        Gson gson = new Gson();
                        Meh meh = gson.fromJson(jsonObject.toString(), Meh.class);
                        if (meh.getDeal() != null && meh.getDeal().getTopic() != null) {
                            createdAt = meh.getDeal().getTopic().getCreatedAt();
                            Log.d(MainActivity.LOGTAG, "using Deal createdAt");
                        } else if (meh.getPoll() != null && meh.getPoll().getStartDate() != null) {
                            createdAt = meh.getPoll().getStartDate();
                            Log.d(MainActivity.LOGTAG, "using poll startDate");
                        } else if (meh.getVideo() != null && meh.getVideo().getStartDate() != null) {
                            createdAt = meh.getVideo().getStartDate();
                            Log.d(MainActivity.LOGTAG, "using Video startDate");
                        }
                        if (createdAt != null) {
                            DateTime dateTime = DateTime.parse(createdAt).withTimeAtStartOfDay();
                            Instant dealDate = dateTime.withTimeAtStartOfDay().toInstant();
                            if (dealDate == today) {
                                mehCache.put(dealDate, jsonObject, true);
                                requestImage(context, meh);
                                Log.d(MainActivity.LOGTAG, "VolleyResponse " + meh.getDeal().getId());
                            } else {
                                int retryMin = Integer.valueOf(prefs.getString(SettingsFragment.KEY_ALARM_RETRY_MIN, SettingsFragment.DEFAULT_ALARM_RETRY_MIN));
                                if (DateTime.now().getMinuteOfHour() < retryMin) {
                                    checkAgain(context);
                                }
                            }
                        }
                    }
                };
                Response.ErrorListener responseErrorListener = new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError volleyError) {
                        Log.e(MainActivity.LOGTAG, "VolleyError", volleyError);
                        checkAgain(context);
                    }
                };
                JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, responseListener, responseErrorListener);
                VolleySingleton.getInstance(context.getApplicationContext()).addToRequestQueue(request);
            }
        }
    }

    private void checkAgain(final Context context) {
        Log.d(MainActivity.LOGTAG, "checkingAgain");
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                context.sendBroadcast(new Intent(INTENT_MEH));
            }
        }, Alarm.FIFTEEN_SEC_MILLIS);
    }

    private void requestImage(final Context context, final Meh meh) {
        if (mNotificationManager == null) {
            mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        }

        ImageLoader.ImageListener imageListener = new ImageLoader.ImageListener() {
            @Override
            public void onResponse(ImageLoader.ImageContainer imageContainer, boolean isImmediate) {
                createNotification(context, meh, imageContainer.getBitmap());
            }

            @Override
            public void onErrorResponse(VolleyError volleyError) {
                Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_sad_face);
                createNotification(context, meh, bitmap);
            }
        };
        VolleySingleton.getInstance(context.getApplicationContext()).getImageLoader().get(meh.getDeal().getPhotos()[0], imageListener);
    }

    private void createNotification(Context context, Meh meh, Bitmap bitmap) {
        if (mNotificationManager == null) {
            mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        }

        Intent appIntent = new Intent(context, MainActivity.class);
        appIntent.setAction(Intent.ACTION_MAIN);
        appIntent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        PendingIntent appPendingIntent = PendingIntent.getActivity(context, 0, appIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(meh.getDeal().getUrl()));
        PendingIntent browserPendingIntent = PendingIntent.getActivity(context, 0, browserIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        String summary = context.getString(R.string.notify_summary, meh.getDeal().getItems()[0].getCondition(), meh.getDeal().getPrices(context));

        NotificationCompat.BigPictureStyle style = new NotificationCompat.BigPictureStyle()
                .bigPicture(bitmap)
                .setSummaryText(summary);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setSmallIcon(R.drawable.ic_stat_meh)
                .setLargeIcon(bitmap)
                .setContentTitle(meh.getDeal().getTitle())
                .setContentText(summary)
                .setAutoCancel(true)
                .setOnlyAlertOnce(true)
                .setContentIntent(appPendingIntent)
                .setFullScreenIntent(appPendingIntent, true)
                .addAction(R.drawable.ic_stat_meh, context.getString(R.string.app_name), appPendingIntent)
                .addAction(R.drawable.ic_stat_browser, context.getString(R.string.action_browser), browserPendingIntent)
                .setStyle(style);

        mNotificationManager.notify(NOTIFICATION_ID, builder.build());
    }
}