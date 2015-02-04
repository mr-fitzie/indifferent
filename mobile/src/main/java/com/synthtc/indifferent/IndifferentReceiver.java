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
import android.graphics.drawable.Drawable;
import android.net.Uri;
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
import com.squareup.picasso.Target;
import com.synthtc.indifferent.api.Meh;
import com.synthtc.indifferent.ui.SettingsFragment;
import com.synthtc.indifferent.util.Alarm;
import com.synthtc.indifferent.util.Helper;
import com.synthtc.indifferent.util.MehCache;
import com.synthtc.indifferent.util.VolleySingleton;

import org.joda.time.DateTime;
import org.joda.time.Instant;
import org.json.JSONObject;

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
            if (prefs.getBoolean(SettingsFragment.KEY_ALARM_ENABLE, SettingsFragment.DEFAULT_ALARM_ENABLE)) {
                Alarm.set(context, false);
            }
        } else if (action.equals(INTENT_MEH)) {
            //Log.d(MainActivity.LOGTAG, "alarm triggered");
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
                        if (meh.getDeal() != null && instant != null && instant.equals(today) || intent.hasExtra("from")) {
                            mehCache.put(instant, jsonObject, true);
                            requestImage(context, meh);
                            //Log.d(MainActivity.LOGTAG, "VolleyResponse " + meh.getDeal().getId());
                            Alarm.cancel(context, true);
                        } else {
                            int retryMin = Integer.valueOf(prefs.getString(SettingsFragment.KEY_ALARM_RETRY_MIN, SettingsFragment.DEFAULT_ALARM_RETRY_MIN));
                            //Log.d(MainActivity.LOGTAG, "IndifferentReceiver not today (" + today.toString() + ") gonna try again for total of " + retryMin + " min");
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
                        Log.e(MainActivity.LOGTAG, "VolleyError", volleyError);
                        Alarm.set(context, true);
                    }
                };
                JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, responseListener, responseErrorListener);
                VolleySingleton.getInstance(context.getApplicationContext()).addToRequestQueue(request);
            }
        }
    }

    private void requestImage(final Context context, final Meh meh) {
        if (mNotificationManager == null) {
            mNotificationManager = NotificationManagerCompat.from(context);
        }

        PicassoFutureTarget target = new PicassoFutureTarget(context, meh);
        Picasso.with(context)
                .load(meh.getDeal().getPhotos()[0])
                .resize(500, 500)
                .centerCrop()
                .placeholder(R.drawable.ic_cached)
                .error(R.drawable.ic_error)
                .into(target);
    }

    private void createNotification(Context context, Meh meh, Bitmap bitmap) {
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
        String titleLower = title.toLowerCase();
        if (titleLower.contains("speaker") && titleLower.contains("dock")) {
            resId = R.drawable.ic_stat_sad;
        } else if (titleLower.contains("fukubukuro")) {
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
            builder.setColor(Color.parseColor(meh.getDeal().getTheme().getAccentColor()));
        }

        mNotificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    private class PicassoFutureTarget implements Target {
        private Context mContext;
        private Meh mMeh;

        public PicassoFutureTarget(Context context, Meh meh) {
            mContext = context;
            mMeh = meh;
        }

        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            createNotification(mContext, mMeh, bitmap);
        }

        @Override
        public void onBitmapFailed(Drawable errorDrawable) {
            Bitmap bitmap = BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_error);
            createNotification(mContext, mMeh, bitmap);
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {
            // do nothing
        }
    }
}
