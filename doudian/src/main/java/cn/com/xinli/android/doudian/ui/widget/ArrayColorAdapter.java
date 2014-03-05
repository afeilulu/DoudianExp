package cn.com.xinli.android.doudian.ui.widget;

import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class ArrayColorAdapter<T> extends ArrayAdapter<T> {
	private int index = -1;
	private int selcolor;
	private int defcolor;

	public ArrayColorAdapter(Context context, int resource,
			int textViewResourceId, List<T> objects) {
		super(context, resource, textViewResourceId, objects);
	}

	public ArrayColorAdapter(Context context, int textViewResourceId,
			List<T> objects) {
		super(context, textViewResourceId, objects);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = super.getView(position, convertView, parent);
		if (index >= 0) {
			if (position == index) {
				TextView tv = (TextView) v;
				tv.setTextColor(selcolor);
			} else {
				TextView tv = (TextView) v;
				tv.setTextColor(defcolor);
			}
		}
		return v;
	}
	/**
	 * 
	 * @param ind　选中下标
	 * @param sel　选中颜色
	 * @param def　默认颜色
	 */
	public void setItemColor(int ind, int sel, int def) {
		index = ind;
		selcolor = sel;
		defcolor = def;
	}

}
