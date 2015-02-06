package com.synthtc.indifferent.ui;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;

/**
 * Created by Chris on 2/3/2015.
 */
public class SquareImageView extends ImageView {
    int mOrientation;

    public SquareImageView(Context context) {
        super(context);
        mOrientation = context.getResources().getConfiguration().orientation;
    }

    public SquareImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mOrientation = context.getResources().getConfiguration().orientation;
    }

    public SquareImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mOrientation = context.getResources().getConfiguration().orientation;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        setMeasuredDimension(getMeasuredWidth(), getMeasuredWidth());
    }
}
