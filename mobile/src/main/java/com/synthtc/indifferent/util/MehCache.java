package com.synthtc.indifferent.util;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.synthtc.indifferent.MainActivity;
import com.synthtc.indifferent.api.Meh;

import org.joda.time.Instant;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

/**
 * Created by Chris on 1/8/2015.
 */
public class MehCache {
    private static final String JSON_EXT = ".json";
    private static MehCache mInstance;
    private Context mContext;
    private HashMap<Instant, Meh> mCache = new HashMap<>();
    private Gson mGson = new Gson();
    private DateTimeFormatter mFormatter = DateTimeFormat.forPattern("yyyyMMdd");

    public MehCache(Context context) {
        mContext = context;
    }

    public static synchronized MehCache getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new MehCache(context);
        }
        return mInstance;
    }

    public boolean put(Instant instant, Meh meh, boolean overwrite) {
        boolean success = false;
        Log.d(MainActivity.LOGTAG, "put " + instant + " " + overwrite);
        if (!mCache.containsKey(instant) || mCache.containsKey(instant) && overwrite) {
            String fileName = instant.toString(mFormatter) + JSON_EXT;
            mCache.put(instant, meh);
            try {
                FileOutputStream fos = mContext.openFileOutput(fileName, Context.MODE_PRIVATE);
                fos.write(mGson.toJson(meh).getBytes());
                fos.close();
                success = true;
                Log.d(MainActivity.LOGTAG, "put success " + fileName);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return success;
    }

    public Meh get(Instant instant) {
        Meh meh = null;
        if (mCache.containsKey(instant)) {
            Log.d(MainActivity.LOGTAG, "get Memory " + instant);
            meh = mCache.get(instant);
        } else if (instant.isAfterNow()) {
            // So Sorry Cant peer into the future
            Log.d(MainActivity.LOGTAG, "get Future " + instant);
        } else {
            Log.d(MainActivity.LOGTAG, "get FileSystem " + instant);
            String fileName = instant.toString(mFormatter) + JSON_EXT;
            JsonParser parser = new JsonParser();
            try {
                File file = mContext.getFileStreamPath(fileName);
                JsonElement element = parser.parse(new FileReader(file));
                meh = mGson.fromJson(element, Meh.class);
                mCache.put(instant, meh);
                Log.d(MainActivity.LOGTAG, "get FileSystem FileFound " + fileName);
            } catch (FileNotFoundException e) {
                Log.i(MainActivity.LOGTAG, "get FileSystem FileNotFound " + fileName);
            }
        }
        return meh;
    }
}
