package com.synthtc.indifferent.util;

import android.content.Context;
import android.graphics.Color;

import com.synthtc.indifferent.MainActivity;

import org.joda.time.DateTimeZone;

/**
 * Created by Chris on 1/12/2015.
 */
public class Helper {
    public static DateTimeZone TIME_ZONE = DateTimeZone.forID("US/Eastern");
    private static String LOGTAG = MainActivity.LOGTAG + ".Helper";

    public static int getForegroundColor(Context context, int backgroundColor) {
        return getForegroundColor(context, backgroundColor,
                android.support.v7.appcompat.R.color.primary_text_default_material_light, android.support.v7.appcompat.R.color.primary_text_default_material_dark);
    }

    /**
     * http://stackoverflow.com/a/2241471
     *
     * @param context
     * @param backgroundColor
     * @return
     */
    public static int getForegroundColor(Context context, int backgroundColor, int lightColorId, int darkColorId) {
        int r = Color.red(backgroundColor);
        int g = Color.green(backgroundColor);
        int b = Color.blue(backgroundColor);
        int v = (int) Math.sqrt((r * r * .299) + (g * g * .587) + (b * b * .114));
        int id = v > 130 ? lightColorId : darkColorId;
        return context.getResources().getColor(id);
    }

    /**
     * http://stackoverflow.com/a/6615053
     *
     * @param color
     * @param foreground
     * @return
     */
    public static int getHighlightColor(int color, String foreground) {
        int r = Color.red(color);
        int g = Color.green(color);
        int b = Color.blue(color);
        int rc;
        int gc;
        int bc;
        if ("dark".equals(foreground)) {
            rc = (int) (r * 0.25);
            gc = (int) (g * 0.25);
            bc = (int) (b * 0.25);
        } else {
            rc = (int) (r + (0.25 * (255 - r)));
            gc = (int) (g + (0.25 * (255 - g)));
            bc = (int) (b + (0.25 * (255 - b)));
        }
        return Color.rgb(rc, gc, bc);
    }

//    public static void log(int logLevel, String text) {
//        log(logLevel, text, null);
//    }
//
//    public static void log(int logLevel, String text, Exception e) {
//        String level;
//        switch (logLevel) {
//            case Log.ASSERT:
//            case Log.DEBUG:
//                level = "DEBUG";
//                Log.d(LOGTAG, text, e);
//                break;
//            case Log.ERROR:
//                level = "ERROR";
//                Log.e(LOGTAG, text, e);
//                break;
//            case Log.INFO:
//                level = "INFO";
//                Log.i(LOGTAG, text, e);
//                break;
//            case Log.VERBOSE:
//                level = "VERBOSE";
//                Log.v(LOGTAG, text, e);
//                break;
//            case Log.WARN:
//                level = "WARN";
//                Log.w(LOGTAG, text, e);
//                break;
//            default:
//                level = "WTF";
//                Log.wtf(LOGTAG, text, e);
//        }
//
//        File dump = new File(Environment.getExternalStorageDirectory() + File.separator + "indifferent-log.txt");
//        try {
//            FileWriter writer = new FileWriter(dump, true);
//            String trace = "";
//            if (e != null) {
//                trace = "\n" + e.toString();
//            }
//            writer.write("[" + DateTime.now().toString() + "::" + level + "] " + text + trace + "\n");
//            writer.flush();
//            writer.close();
//        } catch (IOException ie) {
//            Log.e(LOGTAG, "Could not write log", ie);
//        }
//    }
}
