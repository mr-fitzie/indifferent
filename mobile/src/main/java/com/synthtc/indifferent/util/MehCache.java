/**
 * Copyright 2015 SYNTHTC
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.synthtc.indifferent.util;

import android.content.Context;
import android.content.Intent;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.synthtc.indifferent.api.Meh;

import org.joda.time.DateTime;
import org.joda.time.Instant;
import org.joda.time.Minutes;
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
    public static final String INTENT_CACHE_UPDATED = "com.synthtc.indifferent.CACHE_UPDATED";
    private static final String JSON_EXT = ".json";
    private static MehCache mInstance;
    private Context mContext;
    private HashMap<Instant, Meh> mCache = new HashMap<>();
    private Gson mGson = new Gson();
    private DateTimeFormatter mFormatter = DateTimeFormat.forPattern("yyyyMMdd");

    public MehCache(Context context) {
        mContext = context;
    }

    /**
     * Get the Instance/Singleton for the cache
     *
     * @param context Context for instance
     * @return {#MehCache} instance
     */
    public static synchronized MehCache getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new MehCache(context);
            mInstance.loadCacheFromDisk();
        }
        return mInstance;
    }

    /**
     * Add an item to the cache
     *
     * @param instant    key for the item
     * @param jsonObject JSON as {#Link JSONObject} for item
     * @param overwrite  overwrite the item
     * @return true if successful insert
     */
    public boolean put(Instant instant, JSONObject jsonObject, boolean overwrite) {
        return put(instant, jsonObject.toString(), overwrite);
    }

    /**
     * Add an item to the cache
     *
     * @param instant   key for the item
     * @param json      JSON in {#Link String} form for item
     * @param overwrite overwrite the item
     * @return true if successful insert
     */
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

    /**
     * Return the {#Link Instant} for the provided {#Link Meh}
     *
     * @param meh {#Link Meh}
     * @return {#Link Instant}
     */
    public Instant getInstant(Meh meh) {
        return getInstant(meh, Minutes.ONE);
    }

    /**
     * Return the {#Link Instant} for the provided {#Link Meh}
     *
     * @param meh           {#Link Meh}
     * @param minutesWithin item can be +/- these minutes
     * @return {#Link Instant}
     */
    public Instant getInstant(Meh meh, Minutes minutesWithin) {
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
            DateTime createdTime = DateTime.parse(createdAt);
            DateTime ideal = new DateTime(Helper.TIME_ZONE)
                    .withDate(createdTime.getYear(), createdTime.getMonthOfYear(), createdTime.getDayOfMonth())
                    .withTime(0, 0, 0, 0)
                    .withTimeAtStartOfDay();
            if (minutesWithin != null && Minutes.minutesBetween(createdTime.toInstant(), ideal).isLessThan(minutesWithin)) {
                instant = ideal.toInstant();
            } else {
                instant = createdTime.withZone(Helper.TIME_ZONE).withTimeAtStartOfDay().toInstant();
            }
        }
        return instant;
    }

    /**
     * Retrieve a item from the cache
     *
     * @param instant {#Link Instant} key for item
     * @return {#Link Meh}
     */
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
        } catch (FileNotFoundException | NullPointerException e) {
            //Log.d(LOGTAG, "loadFromFile FileNotFound " + filename);
        }
        return meh;
    }

    /**
     * Returns all {#Link Meh} in the cache
     *
     * @return
     */
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

    /**
     * Clears the cache with the exception of the current day
     */
    public void clear() {
        Instant instant = DateTime.now(Helper.TIME_ZONE).withTimeAtStartOfDay().toInstant();
        String todayFile = instant.toString(mFormatter) + JSON_EXT;
        String[] list = mContext.fileList();
        for (String file : list) {
            if (file.endsWith(JSON_EXT) && !file.equals(todayFile)) {
                mContext.deleteFile(file);
            }
        }
        mCache.clear();
        // Reload the current file
        get(instant);
        mContext.sendBroadcast(new Intent(INTENT_CACHE_UPDATED));
    }
}
