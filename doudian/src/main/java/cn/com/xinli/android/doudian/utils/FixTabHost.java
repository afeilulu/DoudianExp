package cn.com.xinli.android.doudian.utils;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TabHost;

/**
 * 
 * @author chen
 * fix for samsung i589 z579 tabhost NPE problem
 */
public class FixTabHost extends TabHost {

	public FixTabHost(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public FixTabHost(Context context) {
		super(context);
	}
	
	@Override
    public void dispatchWindowFocusChanged(boolean hasFocus) {
        // API LEVEL <7 occasionally throws a NPE here
        if(getCurrentView() != null){
            super.dispatchWindowFocusChanged(hasFocus);
        }

    }

}
