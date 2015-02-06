package com.squareup.picasso;

/**
 * http://stackoverflow.com/a/23544650
 */
public class PicassoTools {
    public static void clearCache (Picasso p) {
        p.cache.clear();
    }
}
