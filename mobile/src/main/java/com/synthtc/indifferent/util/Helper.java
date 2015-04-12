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

package com.synthtc.indifferent.util;

import android.content.Context;
import android.graphics.Color;
import android.os.Environment;
import android.util.Log;

import com.synthtc.indifferent.BuildConfig;
import com.synthtc.indifferent.MainActivity;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

/**
 * Created by Chris on 1/12/2015.
 */
public class Helper {
    public static DateTimeZone TIME_ZONE = DateTimeZone.forID("US/Eastern");
    private static String LOGTAG = MainActivity.LOGTAG + ".Helper";
    private static Boolean mHasWritePermission = null;

    public static int getColor(String hexColor) {
        int color = Color.WHITE; // Default to white
        if (hexColor != null) {
            if (hexColor.length() == 4) { // #fff
                char[] chars = hexColor.toCharArray();
                hexColor = "" + chars[0] + chars[1] + chars[1] + chars[2] + chars[2] + chars[3] + chars[3];
            }
            try {
                color = Color.parseColor(hexColor);
            } catch (IllegalArgumentException e) {
                Helper.log(Log.ERROR, "Could not parse color: " + hexColor, e);
            }
        }
        return color;
    }

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

    public static void log(int logLevel, String text) {
        log(logLevel, text, null);
    }

    public static void log(int logLevel, String text, Exception e) {
        String level;
        switch (logLevel) {
            case Log.ASSERT:
            case Log.DEBUG:
                level = "DEBUG";
                Log.d(LOGTAG, text, e);
                break;
            case Log.ERROR:
                level = "ERROR";
                Log.e(LOGTAG, text, e);
                break;
            case Log.INFO:
                level = "INFO";
                Log.i(LOGTAG, text, e);
                break;
            case Log.VERBOSE:
                level = "VERBOSE";
                Log.v(LOGTAG, text, e);
                break;
            case Log.WARN:
                level = "WARN";
                Log.w(LOGTAG, text, e);
                break;
            default:
                level = "WTF";
                Log.wtf(LOGTAG, text, e);
        }

        if (BuildConfig.DEBUG && mHasWritePermission != null && mHasWritePermission) {
            File dump = new File(Environment.getExternalStorageDirectory() + File.separator + "indifferent-log.txt");
            try {
                FileWriter writer = new FileWriter(dump, true);
                String trace = "";
                if (e != null) {
                    trace = "\n" + e.toString();
                }
                writer.write("[" + DateTime.now().toString() + "::" + level + "] " + text + trace + "\n");
                writer.flush();
                writer.close();
            } catch (IOException ie) {
                Log.e(LOGTAG, "Could not write log", ie);
            }
        }
    }

    public static void setHasWritePermission(boolean hasWritePermission) {
        mHasWritePermission = hasWritePermission;
    }
}
