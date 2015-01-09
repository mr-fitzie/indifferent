package com.synthtk.indifferent;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.synthtk.indifferent.api.Meh;

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
    private final String LOGTAG = this.getClass().getSimpleName();
    Context mContext;
    HashMap<Instant, Meh> mCache = new HashMap<>();
    Gson mGson = new Gson();
    DateTimeFormatter mFormatter = DateTimeFormat.forPattern("yyyyMMdd");

    public MehCache(Context context) {
        mContext = context;
    }

    public boolean put(Instant instant, Meh meh, boolean overwrite) {
        boolean success = false;
        Log.d(LOGTAG, "put " + instant + " " + overwrite);
        if (!mCache.containsKey(instant) || mCache.containsKey(instant) && overwrite) {
            String date = instant.toString(mFormatter);
            mCache.put(instant, meh);
            try {
                FileOutputStream fos = mContext.openFileOutput(date + JSON_EXT, Context.MODE_PRIVATE);
                fos.write(mGson.toJson(meh).getBytes());
                fos.close();
                success = true;
                Log.d(LOGTAG, "put success");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return success;
    }

    public Meh get(Instant instant) {
        Meh meh = null;
        if (mCache.containsKey(instant)) {
            Log.d(LOGTAG, "get Memory " + instant);
            meh = mCache.get(instant);
        } else if (instant.isAfterNow()) {
            // So Sorry Cant peer into the future
            Log.d(LOGTAG, "get Future " + instant);
        } else {
            JsonParser parser = new JsonParser();
            try {
                String date = instant.toString(mFormatter);
                File file = mContext.getFileStreamPath(date + JSON_EXT);
                JsonElement element = parser.parse(new FileReader(file));
                meh = mGson.fromJson(element, Meh.class);
                Log.d(LOGTAG, "get FileSystem " + instant);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
        return meh;
    }
}
