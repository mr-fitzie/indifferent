package com.synthtc.indifferent.ui;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
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
        int dimen = 0;
        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            Log.d("BLAH", "LANDSCAPE");
            dimen = container.getMeasuredHeight();
        } else {
            Log.d("BLAH", "PORTRAIT");
            dimen = container.getMeasuredWidth();
        }
        if (dimen != 0) {
            Picasso.with(getActivity())
                    .load(mImageUrl)
                    .resize(dimen, dimen)
                    .centerCrop()
                    .placeholder(R.drawable.ic_cached)
                    .error(R.drawable.ic_error)
                    .into(mImageView);
        }
        Log.d("BLAH", mImageUrl);
        Log.d("BLAH", dimen + " " + container.getMeasuredHeight() + " " + container.getHeight() + " " + container.getMeasuredWidth() + " " + container.getWidth());
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
