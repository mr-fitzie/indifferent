package com.synthtc.indifferent.util;

import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.synthtc.indifferent.api.Meh;

import org.joda.time.DateTime;
import org.joda.time.Instant;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

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
            mInstance.loadCacheFromDisk();
        }
        return mInstance;
    }

    public boolean put(Instant instant, JSONObject jsonObject, boolean overwrite) {
        return put(instant, jsonObject.toString(), overwrite);
    }

    public boolean put(Instant instant, String json, boolean overwrite) {
        boolean success = false;
        //Log.d(MainActivity.LOGTAG, "put " + instant + " " + overwrite);
        if (!mCache.containsKey(instant) || mCache.containsKey(instant) && overwrite) {
            Meh meh = mGson.fromJson(json, Meh.class);
            mCache.put(instant, meh);
            try {
                String filename = instant.toString(mFormatter) + JSON_EXT;
                FileOutputStream fos = mContext.openFileOutput(filename, Context.MODE_PRIVATE);
                fos.write(json.getBytes());
                fos.close();
                success = true;
                //Log.d(MainActivity.LOGTAG, "put success " + filename);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return success;
    }

    public Instant getInstant(Meh meh) {
        String createdAt = null;
        if (meh.getDeal() != null && meh.getDeal().getTopic() != null) {
            createdAt = meh.getDeal().getTopic().getCreatedAt();
            //Log.d(MainActivity.LOGTAG, "using Deal createdAt " + createdAt);
        } else if (meh.getPoll() != null && meh.getPoll().getStartDate() != null) {
            createdAt = meh.getPoll().getStartDate();
            //Log.d(MainActivity.LOGTAG, "using Poll startDate " + createdAt);
        } else if (meh.getVideo() != null && meh.getVideo().getStartDate() != null) {
            createdAt = meh.getVideo().getStartDate();
            //Log.d(MainActivity.LOGTAG, "using Video startDate " + createdAt);
        }
        Instant instant = null;
        if (createdAt != null) {
            DateTime dateTime = DateTime.parse(createdAt);
            instant = dateTime.withZone(Helper.TIME_ZONE).withTimeAtStartOfDay().toInstant();
        }
        return instant;
    }

    public Meh get(Instant instant) {
        Meh meh = null;
        if (mCache.containsKey(instant)) {
            //Log.d(MainActivity.LOGTAG, "get Memory " + instant);
            meh = mCache.get(instant);
        } else if (instant.isAfterNow()) {
            // So Sorry Cant peer into the future
            //Log.d(MainActivity.LOGTAG, "get Future " + instant);
        } else {
            //Log.d(MainActivity.LOGTAG, "get FileSystem " + instant);
            String filename = instant.toString(mFormatter) + JSON_EXT;
            meh = loadCacheFromFile(filename);
        }
        return meh;
    }

    private void loadCacheFromDisk() {
        String[] list = mContext.fileList();
        for (String file : list) {
            if (file.endsWith(JSON_EXT)) {
                loadCacheFromFile(file);
            }
        }
    }

    private Meh loadCacheFromFile(String filename) {
        Meh meh = null;
        JsonParser parser = new JsonParser();
        try {
            File file = mContext.getFileStreamPath(filename);
            JsonElement element = parser.parse(new FileReader(file));
            meh = mGson.fromJson(element, Meh.class);
            Instant instant = getInstant(meh);
            mCache.put(instant, meh);
            //Log.d(LOGTAG, "loadFromFile FileFound " + filename);
        } catch (FileNotFoundException e) {
            //Log.d(LOGTAG, "loadFromFile FileNotFound " + filename);
        } catch (NullPointerException e) {
            //Log.e(LOGTAG, "loadFromFile NullPointerException " + filename, e);
        }
        return meh;
    }

    public List<Meh> getAll() {
        List<Instant> mehKeys = new ArrayList<>(mCache.keySet());

        Collections.sort(mehKeys, new Comparator<Instant>() {
            @Override
            public int compare(Instant lhs, Instant rhs) {
                return rhs.compareTo(lhs);
            }
        });
        List<Meh> mehValues = new ArrayList<>(mehKeys.size());
        for (Instant key : mehKeys) {
            mehValues.add(mCache.get(key));
        }
        return mehValues;
    }
}
