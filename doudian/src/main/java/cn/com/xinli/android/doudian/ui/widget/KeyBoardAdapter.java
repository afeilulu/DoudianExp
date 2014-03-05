package cn.com.xinli.android.doudian.ui.widget;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import cn.com.xinli.android.doudian.R;

public class KeyBoardAdapter extends BaseAdapter {
	// 定义Context
	private Context mContext;
	private TextView mSearch;
	private int baseId = 5000;
	// 定义整型数组 即图片源
	private Integer[] mImageIds = { R.drawable.key_1, R.drawable.key_2,
			R.drawable.key_3, R.drawable.key_4, R.drawable.key_5,
			R.drawable.key_6, R.drawable.key_7, R.drawable.key_8,
			R.drawable.key_9, R.drawable.key_d, R.drawable.key_0,
			R.drawable.key_c };
	private Integer[] mImageIdf = { R.drawable.keyf_1, R.drawable.keyf_2,
			R.drawable.keyf_3, R.drawable.keyf_4, R.drawable.keyf_5,
			R.drawable.keyf_6, R.drawable.keyf_7, R.drawable.keyf_8,
			R.drawable.keyf_9, R.drawable.keyf_d, R.drawable.keyf_0,
			R.drawable.keyf_c };
	private String[] result = new String[] { "1ABC", "2DEF", "3GHI", "4JKL",
			"5M N", "6OPQ", "7RST", "8UVW", "9XYZ" };
	private ImageView[] item;

	public KeyBoardAdapter(Context context, final TextView search) {
		mContext = context;
		mSearch = search;
		item = new ImageView[12];
	}

	// 获取图片的个数
	public int getCount() {
		return mImageIds.length;
	}

	// 获取图片在库中的位置
	public Object getItem(int position) {
		return position;
	}

	// 获取图片ID
	public long getItemId(int position) {
		return position;
	}

	public View getView(final int position, View convertView, ViewGroup parent) {
		final ImageView imageView;
		if (convertView == null) {
			// 给ImageView设置资源
			imageView = new ImageView(mContext);
			imageView.setId(baseId + position);
			// 设置显示比例类型
			imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
			imageView.setFocusable(true);
			imageView.setFocusableInTouchMode(true);
			imageView.setOnFocusChangeListener(new OnFocusChangeListener() {
				@Override
				public void onFocusChange(View v, boolean hasFocus) {
					if (hasFocus) {
						imageView.setImageResource(mImageIdf[position]);
					} else {
						imageView.setImageResource(mImageIds[position]);
					}
				}
			});
			imageView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					if (position < result.length) {
						new Clavier(mContext).build(mSearch,
								result[position].toCharArray()).showAsDropDown(
								v);
					} else {
						keyActionDown(position);
					}
				}
			});
			if (item[position] == null) {
				item[position] = imageView;
			}
		} else {
			imageView = (ImageView) convertView;
		}
		imageView.setImageResource(mImageIds[position]);

		return imageView;
	}

	public void selected(int position) {
		View v = item[position];
		if (v != null) {
			if (v.isFocusableInTouchMode()) {
				v.requestFocusFromTouch();
			} else {
				v.requestFocus();
			}
		}
	}

	public void setFocusLeftId() {
		item[0].setNextFocusLeftId(item[0].getId());
		item[3].setNextFocusLeftId(item[3].getId());
		item[6].setNextFocusLeftId(item[6].getId());
		item[9].setNextFocusLeftId(item[9].getId());
	}

	public void setFocusRightId(boolean right) {
		if (right) {
			int resId = R.id.search_result_list;
			item[2].setNextFocusRightId(resId);
			item[5].setNextFocusRightId(resId);
			item[8].setNextFocusRightId(resId);
			item[11].setNextFocusRightId(resId);
		} else {
			item[2].setNextFocusRightId(item[2].getId());
			item[5].setNextFocusRightId(item[5].getId());
			item[8].setNextFocusRightId(item[8].getId());
			item[11].setNextFocusRightId(item[11].getId());
		}
	}

	public void setFocusUpId(int resId) {
		item[0].setNextFocusUpId(resId);
		item[1].setNextFocusUpId(resId);
		item[2].setNextFocusUpId(resId);
	}

	private void keyActionDown(int position) {
		CharSequence cs = mSearch.getText();
		if (position == 9) {
			if (cs.length() > 0) {
				mSearch.setText(cs.subSequence(0, cs.length() - 1));
			}
		} else if (position == 10) {
			mSearch.setText(cs + "0");
		} else {
			mSearch.setText("");
		}
	}
}
