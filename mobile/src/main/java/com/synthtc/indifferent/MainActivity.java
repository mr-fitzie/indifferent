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

package com.synthtc.indifferent;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.StateListDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.method.LinkMovementMethod;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.StateSet;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.getbase.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.synthtc.indifferent.api.Deal;
import com.synthtc.indifferent.api.Meh;
import com.synthtc.indifferent.ui.ImageFragment;
import com.synthtc.indifferent.ui.NavigationDrawerFragment;
import com.synthtc.indifferent.ui.SettingsFragment;
import com.synthtc.indifferent.util.Alarm;
import com.synthtc.indifferent.util.Helper;
import com.synthtc.indifferent.util.MehCache;
import com.synthtc.indifferent.util.VolleySingleton;
import com.viewpagerindicator.CirclePageIndicator;

import org.joda.time.DateTime;
import org.joda.time.Instant;
import org.json.JSONObject;

import in.uncod.android.bypass.Bypass;

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

public class MainActivity extends ActionBarActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks {
    public static final String LOGTAG = "Indifferent";
    private static Instant mToday;
    /**
     * Fragment managing the behaviors, interactions and presentation of the navigation drawer.
     */
    private NavigationDrawerFragment mNavigationDrawerFragment;
    /**
     * Used to store the last screen title. For use in {@link #restoreActionBar()}.
     */
    private CharSequence mTitle;
    private int mColor = R.color.primary_material_dark;
    private int mColorStatus = R.color.primary_dark_material_dark;
    private int mColorNav = R.color.primary_dark_material_dark;
    private boolean mIsPaused = false;
    private Fragment mFragmentToLoad = null;
    private boolean mInitialized = false;
    private static int mBackgroundImageSize;
    private static int mOrientation;
    private static int mPaddingSize;

    @Override
    protected void onPause() {
        super.onPause();
        mIsPaused = true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        mIsPaused = false;
        loadFragment(mFragmentToLoad);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mNavigationDrawerFragment = (NavigationDrawerFragment) getSupportFragmentManager().findFragmentById(R.id.navigation_drawer);
        mTitle = getTitle();

        // Set up the drawer.
        mNavigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));

        initialize();
    }

    @Override
    public void onNavigationDrawerItemSelected(Meh meh) {
        // update the main content by replacing fragments
        if (!mInitialized) {
            initialize();
        }

        if (meh == null) {
            loadFragment(new SettingsFragment());
            mTitle = getString(R.string.action_settings);
        } else if (meh.getDeal() != null) {
            Instant instant = MehCache.getInstance(this).getInstant(meh);
            loadFragment(DealFragment.newInstance(instant, meh));
        } else {
            loadFragment(PlaceholderFragment.newInstance(PlaceholderFragment.ARG_SECTION_ERROR));
        }
        //Log.d(LOGTAG, "onNavigationDrawerItemSelected");
    }

    private void initialize() {
        if (mInitialized) {
            return;
        }

        mInitialized = true;

        int perm = checkCallingPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE);
        Helper.setHasWritePermission(perm == PackageManager.PERMISSION_GRANTED);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            getWindowManager().getDefaultDisplay().getRealMetrics(displayMetrics);
        } else {
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        }

        mOrientation = getResources().getConfiguration().orientation;
        mBackgroundImageSize = Math.max(displayMetrics.heightPixels, displayMetrics.widthPixels);
        mPaddingSize = getResources().getDimensionPixelSize(R.dimen.activity_horizontal_margin);
        if (mOrientation == Configuration.ORIENTATION_PORTRAIT) {
            int pagerPadding = getResources().getDimensionPixelOffset(R.dimen.abc_control_padding_material);
            mPaddingSize += pagerPadding;
        }

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        if (prefs.getBoolean(SettingsFragment.KEY_ALARM_ENABLE, SettingsFragment.DEFAULT_ALARM_ENABLE)) {
            Alarm.set(this, false);
        }

        if (BuildConfig.DEBUG) {
            Picasso picasso = Picasso.with(this);
            //picasso.setLoggingEnabled(true);
            picasso.setIndicatorsEnabled(true);
        }

        final MehCache mehCache = MehCache.getInstance(this);
        final Gson gson = new Gson();

        mToday = DateTime.now(Helper.TIME_ZONE).withTimeAtStartOfDay().toInstant();
        //Log.d(LOGTAG, "today is " + mToday);
        if (mNavigationDrawerFragment.getCurrentPosition() == 0) {
            final Meh meh = mehCache.get(mToday);
            if (meh != null && meh.getDeal() != null) {
                Instant instant = mehCache.getInstant(meh);
                if (instant != null) {
                    //Log.d(LOGTAG, "initialize in cache");
                    loadFragment(DealFragment.newInstance(instant, meh));
                    restoreActionBar();
                } else {
                    loadFragment(PlaceholderFragment.newInstance(PlaceholderFragment.ARG_SECTION_ERROR));
                }
            } else {
                getCurrentMeh(mehCache, gson);
            }
        } else {
            mNavigationDrawerFragment.selectItem(mNavigationDrawerFragment.getCurrentPosition());
        }
    }

    private void getCurrentMeh(final MehCache mehCache, final Gson gson) {
        //Log.d(LOGTAG, "initialize not in cache");
        final String url = getString(R.string.api_url, getString(R.string.api_key));
        final Response.Listener<JSONObject> responseListener = new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject jsonObject) {
                Meh meh = gson.fromJson(jsonObject.toString(), Meh.class);
                Instant instant = mehCache.getInstant(meh);
                if (meh.getDeal() != null && instant != null) {
                    //Log.d(LOGTAG, "initialize not in cache pulled down, updating sidebar and frag");
                    mehCache.put(instant, jsonObject, true);
                    mNavigationDrawerFragment.updateList(true);
                    loadFragment(DealFragment.newInstance(instant, meh));
                } else {
                    loadFragment(PlaceholderFragment.newInstance(PlaceholderFragment.ARG_SECTION_ERROR));
                }
            }
        };
        Response.ErrorListener responseErrorListener = new Response.ErrorListener() {

            @Override
            public void onErrorResponse(VolleyError volleyError) {
                loadFragment(PlaceholderFragment.newInstance(PlaceholderFragment.ARG_SECTION_ERROR));
                Log.e(LOGTAG, "VolleyError", volleyError);
            }
        };
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, (String) null, responseListener, responseErrorListener);
        VolleySingleton.getInstance(getApplicationContext()).addToRequestQueue(request);
    }

    public void restoreActionBar() {
        //Log.d(LOGTAG, "restoreActionBar");
        ActionBar actionBar = getSupportActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setBackgroundDrawable(new ColorDrawable(mColor));
        actionBar.setTitle(mTitle);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(mColorStatus);
            getWindow().setNavigationBarColor(mColorNav);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (!mNavigationDrawerFragment.isDrawerOpen()) {
            // Only show items in the action bar relevant to this screen
            // if the drawer is not showing. Otherwise, let the drawer
            // decide what to show in the action bar.
            //getMenuInflater().inflate(R.menu.main, menu);
            restoreActionBar();
            return true;
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_refresh) {
            getCurrentMeh(MehCache.getInstance(this), new Gson());
        }

        return super.onOptionsItemSelected(item);
    }

    private void loadFragment(Fragment fragment) {
        if (fragment != null) {
            if (mIsPaused) {
                mFragmentToLoad = fragment;
            } else {
                mFragmentToLoad = null;
                FragmentManager fragmentManager = getSupportFragmentManager();
                fragmentManager.beginTransaction()
                        .replace(R.id.container, fragment)
                        .commit();
            }
        }
    }

    public static class DealFragment extends Fragment {
        public static final String ARG_INSTANT = "deal_instant";
        private static Meh mMeh;
        private static Instant mDealDate;
        private ViewPager mPager;
        private ImagePagerAdapter mAdapter;
        private Target mTarget;

        public DealFragment() {
        }

        public static DealFragment newInstance(Instant instant, Meh mehObject) {
            mDealDate = instant;
            mMeh = mehObject;

            DealFragment fragment = new DealFragment();
            Bundle args = new Bundle();
            args.putLong(ARG_INSTANT, instant.getMillis());
            fragment.setArguments(args);

            return fragment;
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            inflater.inflate(R.menu.main, menu);
            super.onCreateOptionsMenu(menu, inflater);
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            outState.putInt("item", mPager.getCurrentItem());
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
            Log.d(LOGTAG, "DealFragment.onCreateView");
            for (final String url : mMeh.getDeal().getPhotos()) {
                Picasso.with(getActivity()).load(url).fetch();
            }

            boolean tooLate = mDealDate.isBefore(mToday);
            setHasOptionsMenu(!tooLate);

            final View rootView = inflater.inflate(R.layout.fragment_deal, container, false);

            final Deal deal = mMeh.getDeal();
            Deal.Theme theme = deal.getTheme();
            Deal.Story story = deal.getStory();

            int accentColor = Helper.getColor(theme.getAccentColor());
            int foregroundColor = Helper.getForegroundColor(getActivity(), accentColor);
            int backgroundColor = Helper.getColor(theme.getBackgroundColor());
            int textColor = Helper.getForegroundColor(getActivity(), backgroundColor);
            int highlightColor = Helper.getHighlightColor(accentColor, theme.getForeground());

            final View dealLayout = rootView.findViewById(R.id.deal_layout);
            TextView title = (TextView) rootView.findViewById(R.id.title);

            // Set up ViewPager and backing adapter
            mAdapter = new ImagePagerAdapter(getActivity().getSupportFragmentManager(), mMeh.getDeal().getPhotos().length);
            mPager = (ViewPager) rootView.findViewById(R.id.pager);

            mTarget = new Target() {
                @Override
                public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
                    int width = Math.min(mBackgroundImageSize, bitmap.getWidth());
                    int inset;
                    if (mOrientation == Configuration.ORIENTATION_LANDSCAPE) {
                        int pagerSize = getResources().getDimensionPixelSize(R.dimen.viewpager_size);
                        inset = ((pagerSize - width) / 2) + mPaddingSize;
                    } else {
                        inset = (int) (width * -0.2) + mPaddingSize;
                    }
                    BitmapDrawable bitmapDrawable = new BitmapDrawable(getResources(), bitmap);
                    bitmapDrawable.setGravity(Gravity.TOP | Gravity.START);
                    Drawable[] layers = {bitmapDrawable};
                    LayerDrawable layerDrawable = new LayerDrawable(layers);
                    layerDrawable.setLayerInset(0, inset, inset, 0, 0);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        dealLayout.setBackground(layerDrawable);
                    } else {
                        dealLayout.setBackgroundDrawable(layerDrawable);
                    }
                }

                @Override
                public void onBitmapFailed(Drawable errorDrawable) {
                    Helper.log(Log.ERROR, "Could not load BG Image");
                }

                @Override
                public void onPrepareLoad(Drawable placeHolderDrawable) {
                    // Do nothing
                }
            };

            if (mMeh.getDeal().getTheme().getBackgroundImage() != null && !mMeh.getDeal().getTheme().getBackgroundImage().isEmpty()) {
                final String url = mMeh.getDeal().getTheme().getBackgroundImage();
                Picasso.with(getActivity()).load(url).resize(mBackgroundImageSize, mBackgroundImageSize).onlyScaleDown().centerCrop().into(mTarget);
            }
            mPager.setAdapter(mAdapter);
            if (savedInstanceState != null) {
                mPager.setCurrentItem(savedInstanceState.getInt("item"));
            }

            CirclePageIndicator circleIndicator = (CirclePageIndicator) rootView.findViewById(R.id.pager_indicator);
            circleIndicator.setViewPager(mPager);
            circleIndicator.setFillColor(accentColor);
            circleIndicator.setStrokeColor(accentColor);

            Button price = (Button) rootView.findViewById(R.id.price);
            GradientDrawable pill = (GradientDrawable) price.getBackground();

            TextView features = (TextView) rootView.findViewById(R.id.features);
            TextView storyTitle = (TextView) rootView.findViewById(R.id.story_title);
            TextView storyBody = (TextView) rootView.findViewById(R.id.story);
            TextView specifications = (TextView) rootView.findViewById(R.id.specifications);
            FloatingActionButton fab = (FloatingActionButton) rootView.findViewById(R.id.fab_meh);

            fab.setColorNormal(accentColor);
            fab.setColorPressed(highlightColor);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(deal.getUrl()));
                    getActivity().startActivity(browserIntent);
                }
            });

            String prices = deal.getPrices(getActivity());

            if (deal.getSoldOutAt() != null || tooLate) {
                price.setTextColor(getResources().getColor(R.color.primary_material_light));
                price.setText(getString(deal.getSoldOutAt() != null ? R.string.deal_price_soldout : R.string.deal_price_toolate, prices));
                price.setEnabled(false);
                pill.setColor(Helper.getForegroundColor(getActivity(), backgroundColor, R.color.primary_dark_material_dark, R.color.primary_dark_material_light));
            } else {
                GradientDrawable pillPressed = (GradientDrawable) pill.getConstantState().newDrawable().mutate();
                pill.setColor(accentColor);
                pillPressed.setColor(highlightColor);

                StateListDrawable states = new StateListDrawable();
                states.addState(new int[]{android.R.attr.state_pressed}, pillPressed);
                states.addState(StateSet.WILD_CARD, pill);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    price.setBackground(states);
                } else {
                    price.setBackgroundDrawable(states);
                }

                price.setTextColor(foregroundColor);
                price.setText(getString(R.string.deal_price_buy, prices));
                price.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(deal.getUrl().concat("/checkout")));
                        getActivity().startActivity(browserIntent);
                    }
                });
            }

            title.setText(deal.getTitle());
            title.setTextColor(textColor);

            Bypass bypass = new Bypass(getActivity());
            CharSequence markDown = bypass.markdownToSpannable(deal.getFeatures());
            features.setText(markDown);
            features.setTextColor(textColor);
            features.setMovementMethod(LinkMovementMethod.getInstance());
            features.setLinkTextColor(accentColor);

            if (story != null) {
                storyTitle.setText(deal.getStory().getTitle());
                storyTitle.setTextColor(accentColor);
                markDown = bypass.markdownToSpannable(deal.getStory().getBody());
                storyBody.setText(markDown);
                storyBody.setTextColor(textColor);
                storyBody.setMovementMethod(LinkMovementMethod.getInstance());
                storyBody.setLinkTextColor(accentColor);
            } else {
                storyTitle.setVisibility(View.GONE);
                storyBody.setVisibility(View.GONE);
            }

            markDown = bypass.markdownToSpannable(deal.getSpecifications());
            specifications.setText(markDown);
            specifications.setTextColor(textColor);
            specifications.setMovementMethod(LinkMovementMethod.getInstance());
            specifications.setLinkTextColor(accentColor);

            rootView.setBackgroundColor(backgroundColor);
            return rootView;
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            if (mMeh != null && mMeh.getDeal() != null) {
                MainActivity mainActivity = (MainActivity) activity;
                mainActivity.mTitle = mMeh.getDeal().getTitle();
                Deal.Theme theme = mMeh.getDeal().getTheme();
                mainActivity.mColor = Helper.getColor(theme.getAccentColor());
                mainActivity.mColorStatus = Helper.getHighlightColor(mainActivity.mColor, theme.getForeground());
                mainActivity.mColorNav = Helper.getHighlightColor(Helper.getColor(theme.getBackgroundColor()), theme.getForeground());
                //Log.d(LOGTAG, "onAttach " + mainActivity.mTitle + " " + mainActivity.mColor + " " + mainActivity.mColorStatus);
            } else {
                Log.e(LOGTAG, "onAttach meh was null");
            }
        }

        @Override
        public void onDestroy() {
            Picasso.with(getActivity()).cancelRequest(mTarget);
            super.onDestroy();
        }

        private class ImagePagerAdapter extends FragmentStatePagerAdapter {
            private final int mSize;

            public ImagePagerAdapter(FragmentManager fm, int size) {
                super(fm);
                mSize = size;
            }

            @Override
            public int getCount() {
                return mSize;
            }

            @Override
            public Fragment getItem(int position) {
                return ImageFragment.newInstance(mMeh.getDeal().getPhotos()[position]);
            }
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        public static final int ARG_SECTION_ERROR = -1;
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";
        private static int mSectionNumber = ARG_SECTION_ERROR;

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            int layoutId = R.layout.fragment_main;
            if (mSectionNumber < 0) {
                layoutId = R.layout.fragment_error;
            }
            setHasOptionsMenu(true);
            //Log.d(LOGTAG, "PlaceholderFragmentonCreateView " + mSectionNumber);
            return inflater.inflate(layoutId, container, false);
        }

        @Override
        public void onAttach(Activity activity) {
            super.onAttach(activity);
            mSectionNumber = getArguments().getInt(ARG_SECTION_NUMBER);
            MainActivity mainActivity = (MainActivity) activity;
            mainActivity.mTitle = getString(R.string.error_oops);
            mainActivity.mColor = getResources().getColor(R.color.primary_material_dark);
            mainActivity.mColorStatus = getResources().getColor(R.color.primary_dark_material_dark);
            mainActivity.mColorNav = getResources().getColor(R.color.primary_dark_material_dark);
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            inflater.inflate(R.menu.main, menu);
            super.onCreateOptionsMenu(menu, inflater);
        }
    }
}
