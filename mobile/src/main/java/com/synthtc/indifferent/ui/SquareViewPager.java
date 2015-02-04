package com.synthtc.indifferent.ui;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;
import android.util.Log;

/**
 * Created by Chris on 2/3/2015.
 */
public class SquareViewPager extends ViewPager {
    int mOrientation;

    public SquareViewPager(Context context) {
        super(context);
        mOrientation = context.getResources().getConfiguration().orientation;
    }

    public SquareViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
        mOrientation = context.getResources().getConfiguration().orientation;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        Log.d("BLAH3", mOrientation + " " + getMeasuredWidth() + " " + getMeasuredHeight());
        setMeasuredDimension(getMeasuredWidth(), getMeasuredWidth());
    }
}
