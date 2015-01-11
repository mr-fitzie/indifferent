package com.synthtk.indifferent;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.synthtk.indifferent.api.Meh;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Instant;

/**
 * Created by Chris on 1/10/2015.
 */
public class IndifferentReceiver extends BroadcastReceiver {
    public static final String INTENT_MEH = "com.synthtk.indifferent.MEH";
    private static final int NOTIFICATION_ID = 1;
    private NotificationManager mNotificationManager = null;

    @Override
    public void onReceive(final Context context, Intent intent) {
        if (mNotificationManager == null) {
            mNotificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        }

        String action = intent.getAction();
        if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
            Alarm.set(context);
            Log.e(MainActivity.LOGTAG, "BOOT COMPLETED");
        } else if (action.equals(INTENT_MEH)) {
            final MehCache mehCache = MehCache.getInstance(context);
            final Instant today = DateTime.now(DateTimeZone.UTC).withTimeAtStartOfDay().toInstant();
            Meh meh = mehCache.get(today);
            if (meh != null) {
                requestImage(context, meh);
            } else {
                String url = context.getString(R.string.api_url, context.getString(R.string.api_key));
                Response.Listener<Meh> responseListener = new Response.Listener<Meh>() {
                    @Override
                    public void onResponse(Meh meh) {
                        DateTime dateTime = DateTime.parse(meh.getDeal().getTopic().getCreatedAt());
                        Instant dealDate = dateTime.withTimeAtStartOfDay().toInstant();
                        if (dealDate == today) {
                            mehCache.put(dealDate, meh, true);
                            requestImage(context, meh);
                            Log.d(MainActivity.LOGTAG, "VolleyResponse " + meh.getDeal().getId());
                        } else {
                            checkAgain(context);
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
                GsonRequest request = new GsonRequest(url, Meh.class, null, responseListener, responseErrorListener);
                VolleySingleton.getInstance(context).addToRequestQueue(request);
            }
            Log.e(MainActivity.LOGTAG, "alarm triggered");
        }
    }

    private void checkAgain(final Context context) {
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
        VolleySingleton.getInstance(context).getImageLoader().get(meh.getDeal().getPhotos()[0], imageListener);
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

        NotificationCompat.BigPictureStyle style = new NotificationCompat.BigPictureStyle()
                .bigPicture(bitmap);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context)
                .setCategory(NotificationCompat.CATEGORY_ALARM)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setSmallIcon(R.drawable.ic_stat_meh)
                .setLargeIcon(bitmap)
                .setContentTitle(meh.getDeal().getTitle())
                .setContentText(context.getString(R.string.notify_summary, meh.getDeal().getItems()[0].getCondition(), meh.getDeal().getPrices(context)))
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
