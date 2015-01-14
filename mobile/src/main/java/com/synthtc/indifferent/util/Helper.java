package com.synthtc.indifferent.util;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.synthtc.indifferent.MainActivity;
import com.synthtc.indifferent.api.Meh;

import org.joda.time.DateTimeZone;

/**
 * Created by Chris on 1/12/2015.
 */
public class Helper {
    public static DateTimeZone TIME_ZONE = DateTimeZone.forID("US/Eastern");
    private static String LOGTAG = MainActivity.LOGTAG + ".Helper";

    /**
     * http://stackoverflow.com/a/2241471
     *
     * @param context
     * @param backgroundColor
     * @return
     */
    public static int getForegroundColor(Context context, int backgroundColor) {
        int r = Color.red(backgroundColor);
        int g = Color.green(backgroundColor);
        int b = Color.blue(backgroundColor);
        int v = (int) Math.sqrt((r * r * .299) + (g * g * .587) + (b * b * .114));
        int id = v > 130 ? android.support.v7.appcompat.R.color.primary_text_default_material_light : android.support.v7.appcompat.R.color.primary_text_default_material_dark;
        return context.getResources().getColor(id);
    }

    public static int getHighlightColor(int color, String foreground) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        if ("dark".equals(foreground)) {
            hsv[2] = 1.0f - 0.8f * (1.0f - hsv[2]);
        } else {
            hsv[2] = 0.2f + 0.8f * hsv[2];
        }
        return Color.HSVToColor(hsv);
    }

    public static void cacheImages(Context context, Meh meh) {
        ImageLoader loader = VolleySingleton.getInstance(context.getApplicationContext()).getImageLoader();
        for (final String url : meh.getDeal().getPhotos()) {
            ImageLoader.ImageListener listener = new ImageLoader.ImageListener() {
                @Override
                public void onResponse(ImageLoader.ImageContainer imageContainer, boolean isImmediate) {
                    Log.d(LOGTAG, "Finished loading: " + url + " cached:" + isImmediate);
                }

                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    Log.e(LOGTAG, "Could not load: " + url, volleyError);
                }
            };
            loader.get(url, listener);
        }
    }
}
