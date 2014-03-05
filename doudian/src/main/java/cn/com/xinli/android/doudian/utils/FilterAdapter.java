package cn.com.xinli.android.doudian.utils;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

import cn.com.xinli.android.doudian.R;

public class FilterAdapter extends BaseAdapter {
	private List<AdapterItem> mList;
	private Context mContext;

	public FilterAdapter(List<AdapterItem> mList, Context mContext) {
		this.mList = mList;
		this.mContext = mContext;
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		return mList.size();
	}

	@Override
	public Object getItem(int arg0) {
		// TODO Auto-generated method stub
		return mList.get(arg0);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater _LayoutInflater = LayoutInflater.from(mContext);
		convertView = _LayoutInflater.inflate(R.layout.filter_adapter_item,
				null);
//		Log.d("FilterAdapter",
//				"--------parent---------:" + parent.getChildCount()
//						+ "      height:" + parent.getHeight());
//		if (parent.getLayoutParams() instanceof FrameLayout.LayoutParams) {
//			FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
//					LayoutParams.MATCH_PARENT, 150);
//			parent.setLayoutParams(params);
//			Log.d("FilterAdapter", "--------parent---getLayoutParams------:"
//					+ parent.getLayoutParams().height);
//			View view=(View) parent.getParent();
//			android.view.WindowManager.LayoutParams layoutParams=(android.view.WindowManager.LayoutParams) view.getLayoutParams();
//			layoutParams.height=100;
//			Log.d("FilterAdapter", layoutParams.debug("-------params1------"));
//			Log.d("FilterAdapter","layoutParams.alpha:"+ layoutParams.alpha);
//			view.setLayoutParams(layoutParams);
//		}
		if (convertView != null) {
			TextView _TextView1 = (TextView) convertView
					.findViewById(R.id.textView1);
			TextView _TextView2 = (TextView) convertView
					.findViewById(R.id.textView2);
			_TextView1.setText(mList.get(position).getName());
			_TextView2.setText(mList.get(position).getFilter());
		}
		return convertView;
	}

}
