package cn.com.xinli.android.doudian.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.Gallery;

public class XinliGallery extends Gallery {
	
	private OnKeyListener mOnKeyListener;

	public XinliGallery(Context context) {
		super(context);
	}
	
	public XinliGallery(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public XinliGallery(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	
	@Override
	public boolean dispatchKeyEvent(KeyEvent event) {
		if (mOnKeyListener != null && mOnKeyListener.onKey(this, event.getKeyCode(), event)) {
			return true;
		}
		return super.dispatchKeyEvent(event);
	}
	
	@Override
	public void setOnKeyListener(OnKeyListener onKeyListener) {
		mOnKeyListener = onKeyListener;
	}
}
