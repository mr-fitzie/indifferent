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

package com.synthtc.indifferent.ui;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.squareup.picasso.Picasso;
import com.synthtc.indifferent.R;

/**
 * This fragment will populate the children of the ViewPager from {@link com.synthtc.indifferent.MainActivity.DealFragment}.
 */
public class ImageFragment extends Fragment {
    private static final String IMAGE_DATA_EXTRA = "extra_image_data";
    private final String LOGTAG = this.getClass().getSimpleName();
    private String mImageUrl;
    private SquareImageView mImageView;

    /**
     * Empty constructor as per the Fragment documentation
     */
    public ImageFragment() {
    }

    /**
     * Factory method to generate a new instance of the fragment given an image number.
     *
     * @param imageUrl The image url to load
     * @return A new instance of ImageDetailFragment with imageNum extras
     */
    public static ImageFragment newInstance(String imageUrl) {
        final ImageFragment f = new ImageFragment();
        final Bundle args = new Bundle();
        args.putString(IMAGE_DATA_EXTRA, imageUrl);
        f.setArguments(args);
        return f;
    }

    /**
     * Populate image using a url from extras, use the convenience factory method
     * {@link ImageFragment#newInstance(String)} to create this fragment.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mImageUrl = getArguments() != null ? getArguments().getString(IMAGE_DATA_EXTRA) : null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mImageView = (SquareImageView) inflater.inflate(R.layout.fragment_image, container, false);
        int dimen;
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            //Log.d(LOGTAG, "LANDSCAPE");
            dimen = container.getMeasuredHeight();
        } else {
            //Log.d(LOGTAG, "PORTRAIT");
            dimen = container.getMeasuredWidth();
        }
        if (dimen != 0) {
            //Log.d(LOGTAG, mImageUrl);
            Picasso.with(getActivity())
                    .load(mImageUrl)
                    .resize(dimen, dimen)
                    .centerCrop()
                    .placeholder(R.drawable.ic_cached)
                    .error(R.drawable.ic_error)
                    .into(mImageView);
        }
        //Log.d(LOGTAG, dimen + " " + container.getMeasuredHeight() + " " + container.getHeight() + " " + container.getMeasuredWidth() + " " + container.getWidth());
        return mImageView;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mImageView != null) {
            mImageView.setImageDrawable(null);
        }
    }
}
