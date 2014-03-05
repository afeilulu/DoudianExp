package cn.com.xinli.android.doudian.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.TextView;

/**
 * Created by chen on 3/5/14.
 */
public class ProgressTextView extends TextView {
    private final static String TAG = "ProgressTextView";
    private int mMax;
    private int mProgress;

    public ProgressTextView(Context context) {
        super(context);
    }

    public ProgressTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ProgressTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.e(TAG, "onMeasure");
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        Log.e(TAG, "onLayout");
        super.onLayout(changed, left, top, right, bottom);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        Log.e(TAG, "onDraw");
        super.onDraw(canvas);
    }

    /**
     * Sets the current seek bar progress (between 0 and max).
     *
     * @see #setMax(int)
     */
    public void setProgress(int progress) {
        Log.e(TAG, "setProgress = " + progress);
        mProgress = progress;
        invalidate();
    }

    /**
     * Sets the maximum download progress value. Defaults to 100.
     */
    public void setMax(int max) {
        mMax = max;
        invalidate();
    }

}
