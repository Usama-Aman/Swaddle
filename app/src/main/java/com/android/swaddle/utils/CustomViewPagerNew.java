package com.android.swaddle.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;

public class CustomViewPagerNew extends ViewPager {
    public CustomViewPagerNew(@NonNull Context context) {
        super(context);
    }

    public CustomViewPagerNew(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }
//    We need to override the onMeasure method for the customviewpager, in order for it to take up appropriate height based on its child views.The customviewpager will measure the height of its first child(usually at position 0) and set its height equal to that value :

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        try {
            int currentPagePosition = 0;
            View child = getChildAt(currentPagePosition);
            if (child != null) {
                child.measure(widthMeasureSpec, MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED));
                int h = child.getMeasuredHeight();
                heightMeasureSpec = MeasureSpec.makeMeasureSpec(h, MeasureSpec.EXACTLY);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}