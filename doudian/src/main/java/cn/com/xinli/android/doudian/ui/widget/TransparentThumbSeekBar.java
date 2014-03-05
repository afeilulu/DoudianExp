package cn.com.xinli.android.doudian.ui.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.widget.SeekBar;

/**
 * Created by chen on 3/5/14.
 */
public class TransparentThumbSeekBar extends SeekBar {

    public TransparentThumbSeekBar(Context context) {
        super(context);
    }
    public TransparentThumbSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }
    public TransparentThumbSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    Drawable mThumb;
    @Override
    public void setThumb(Drawable thumb) {
        super.setThumb(thumb);
        mThumb = thumb;
    }
    public Drawable getSeekBarThumb() {
        return mThumb;
    }

}
