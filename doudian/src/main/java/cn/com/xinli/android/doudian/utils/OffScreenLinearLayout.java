package cn.com.xinli.android.doudian.utils;

import cn.com.xinli.android.doudian.R;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

public class OffScreenLinearLayout extends LinearLayout {

	private Context myContext;

    public OffScreenLinearLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        myContext = context;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
    {
        super.onMeasure(widthMeasureSpec+((int)myContext.getResources().getDimension(R.dimen.offscreen_offset)), heightMeasureSpec);
    }
    
}
