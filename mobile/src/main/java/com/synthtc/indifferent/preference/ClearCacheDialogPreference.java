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
package com.synthtc.indifferent.preference;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.preference.DialogPreference;
import android.util.AttributeSet;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.PicassoTools;
import com.synthtc.indifferent.api.Meh;
import com.synthtc.indifferent.ui.NavigationDrawerFragment;
import com.synthtc.indifferent.util.MehCache;

/**
 * Created by Chris on 2/5/2015.
 */
public class ClearCacheDialogPreference extends DialogPreference {

    public ClearCacheDialogPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ClearCacheDialogPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        super.onPrepareDialogBuilder(builder);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                PicassoTools.clearCache(Picasso.with(getContext()));
                MehCache.getInstance(getContext()).clear();
            }
        });
        builder.setNegativeButton(android.R.string.cancel, null);
    }
}
