package cn.com.xinli.android.doudian.ui.widget;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import cn.com.xinli.android.doudian.R;

public class SelectView extends LinearLayout {
	private final String TAG = "SelectView";
	int curindex = 0;
	Context mContext;
	OnItemClickListener mListtener;
    OnItemSelectedListener mOnItemSelectedListener;

	public SelectView(Context context) {
		super(context);
		mContext = context;
	}

	public SelectView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mContext = context;
	}

	public void setOnItemClickListener(OnItemClickListener listtener) {
		mListtener = listtener;
	}

    public void setOnItemSelectedListener(OnItemSelectedListener listtener) {
        mOnItemSelectedListener = listtener;
    }

	public void setList(ArrayList<String> list) {
		if (list != null) {
			for (int i = 0; i < list.size(); i++) {
				Button item = new Button(mContext);
				item.setBackgroundResource(R.drawable.source_selector_one);
				item.setTextColor(Color.WHITE);
				item.setTextSize(mContext.getResources().getDimension(
						R.dimen.detail_name));
//				item.setPadding(50, 50, 50, 50);
                item.setSingleLine(true);
				item.setText(list.get(i));
				item.setId(i);
				addView(item);
				item.setOnClickListener(new OnClickListener() {
					@Override
					public void onClick(View v) {
						if (mListtener != null) {
							setSelected(v.getId());
							mListtener.onItemClicked(v, v.getId());
						}
					}
				});
			}
		}
		setSelected(-1);
	}

	public void setSelected(int position) {
		Log.i(TAG, "position=" + position);
		if (position < 0) {
			if (curindex < getChildCount()) {
				getChildAt(curindex).setBackgroundResource(
						R.drawable.source_selector_two);
			}
			return;
		}
		if (curindex != position) {
			curindex = position;
			for (int i = 0; i < getChildCount(); i++) {
				getChildAt(i).setBackgroundResource(R.drawable.source_selector_one);
			}
			if (curindex < getChildCount()) {
				getChildAt(curindex).setBackgroundResource(
						R.drawable.source_selector_two);
			}
		}
        mOnItemSelectedListener.onItemSelected(position);
	}

	public interface OnItemClickListener {
		public void onItemClicked(View view, int position);
	}

    public interface OnItemSelectedListener {
        public void onItemSelected(int position);
    }
}
