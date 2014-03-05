package cn.com.xinli.android.doudian.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ScrollView;

public class CScrollView extends ScrollView {
	
	public CScrollView(Context context) {
		super(context);
	}
	
	public CScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public CScrollView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public boolean onInterceptTouchEvent(MotionEvent ev) {
//		return super.onInterceptTouchEvent(ev);
		return false;
	}

	@Override
	public boolean onTouchEvent(MotionEvent ev) {
//		return super.onTouchEvent(ev);
		return false;
	}
	
	

}
