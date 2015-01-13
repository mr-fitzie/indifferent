package com.synthtc.indifferent.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.NetworkImageView;
import com.synthtc.indifferent.R;
import com.synthtc.indifferent.util.VolleySingleton;

/**
 * This fragment will populate the children of the ViewPager from {@link com.synthtc.indifferent.MainActivity.DealFragment}.
 */
public class ImageFragment extends Fragment {
    private static final String IMAGE_DATA_EXTRA = "extra_image_data";
    private String mImageUrl;
    private NetworkImageView mImageView;

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
        mImageView = new NetworkImageView(getActivity());
        mImageView.setDefaultImageResId(R.drawable.ic_meh);
        mImageView.setErrorImageResId(R.drawable.ic_sad_face);
//        ViewGroup.LayoutParams params = mImageView.getLayoutParams();
//        params.height = ViewGroup.LayoutParams.MATCH_PARENT;
//        params.width = ViewGroup.LayoutParams.MATCH_PARENT;
//        mImageView.setLayoutParams(params);

        // Get the ImageLoader through your singleton class.
        ImageLoader mImageLoader = VolleySingleton.getInstance(getActivity().getApplicationContext()).getImageLoader();

        // Set the URL of the image that should be loaded into this view, and
        // specify the ImageLoader that will be used to make the request.
        mImageView.setImageUrl(mImageUrl, mImageLoader);

        return mImageView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        // Use the parent activity to load the image asynchronously into the ImageView (so a single
        // cache can be used over all pages in the ViewPager
        //if (MainActivity.class.isInstance(getActivity())) {
        //mImageFetcher = ((ImageDetailActivity) getActivity()).getImageFetcher();
        //mImageFetcher.loadImage(mImageUrl, mImageView);
        //}

        // Pass clicks on the ImageView to the parent activity to handle
        //if (View.OnClickListener.class.isInstance(getActivity())) {
        //mImageView.setOnClickListener((View.OnClickListener) getActivity());
        //}
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mImageView != null) {
            mImageView.setImageDrawable(null);
        }
    }
}
