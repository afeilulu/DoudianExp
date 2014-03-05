package cn.com.xinli.android.doudian.ui.widget;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;
import cn.com.xinli.android.doudian.R;

public class Clavier implements OnKeyListener {
	private Context mContext;
	private TextView mSerachText;
	private String[] keys;
	private PopupWindow window;

	public Clavier(Context context) {
		mContext = context;
	}

	public void showAsDropDown(View anchor) {
		if (window != null) {
			window.showAsDropDown(anchor, 5, -112);
		}
	}

	public Clavier build(final TextView serachText, char[] abc) {
		keys = new String[abc.length];
		for (int i = 0; i < keys.length; i++) {
			keys[i] = String.valueOf(abc[i]);
		}
		mSerachText = serachText;
		window = new PopupWindow(mContext);
		// 设置PopupWindow显示和隐藏时的动画
		window.setAnimationStyle(R.style.PopupAnimation);
		// 设置PopupWindow的大小（宽度和高度）
		ColorDrawable dw = new ColorDrawable(android.R.color.transparent);
		window.setBackgroundDrawable(dw);
		window.setWidth(px2dip(150));
		window.setHeight(px2dip(150));
		window.setFocusable(true); // 设置PopupWindow可获得焦点
		window.setTouchable(true); // 设置PopupWindow可触摸
		window.setOutsideTouchable(true);// 设置PopupWindow外部区域是否可触摸
		// 设置PopupWindow的内容view
		LayoutInflater layoutInflater = (LayoutInflater) mContext
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view = layoutInflater.inflate(R.layout.clavier_layout, null);
		ImageButton ok = (ImageButton) view.findViewById(R.id.key_OK);
		final TextView tops = (TextView) view.findViewById(R.id.key_top);
		final TextView lefts = (TextView) view.findViewById(R.id.key_left);
		final TextView rights = (TextView) view.findViewById(R.id.key_right);
		final TextView bottons = (TextView) view.findViewById(R.id.key_botton);
		bottons.setText(keys[0]);
		lefts.setText(keys[1]);
		tops.setText(keys[2]);
		rights.setText(keys[3]);
		bottons.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				CharSequence s = bottons.getText();
				if (s != null && !"".equals(s)) {
					mSerachText.setText(mSerachText.getText() + keys[0]);
				}
			}
		});
		lefts.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				CharSequence s = lefts.getText();
				if (s != null && !"".equals(s)) {
					mSerachText.setText(mSerachText.getText() + keys[1]);
				}
			}
		});
		tops.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				CharSequence s = tops.getText();
				if (s != null && !"".equals(s)) {
					mSerachText.setText(mSerachText.getText() + keys[2]);
				}
			}
		});
		rights.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				CharSequence s = rights.getText();
				if (s != null && !"".equals(s)) {
					mSerachText.setText(mSerachText.getText() + keys[3]);
				}
			}
		});

		ok.setOnKeyListener(this);
		window.setContentView(view);
		return this;
	}

	@Override
	public boolean onKey(View v, int keyCode, KeyEvent event) {
		if (event.getAction() == KeyEvent.ACTION_DOWN) {
			return true;
		}
		CharSequence cs = mSerachText.getText();
		switch (keyCode) {
		case KeyEvent.KEYCODE_DPAD_UP:
			if (keys[2] != null && !"".equals(keys[2].trim())) {
				mSerachText.setText(cs + keys[2]);
				window.dismiss();
			}
			break;
		case KeyEvent.KEYCODE_DPAD_LEFT:
			mSerachText.setText(cs + keys[1]);
			window.dismiss();
			break;
		case KeyEvent.KEYCODE_ENTER:
		case KeyEvent.KEYCODE_DPAD_CENTER:
			window.dismiss();
			break;
		case KeyEvent.KEYCODE_DPAD_RIGHT:
			mSerachText.setText(cs + keys[3]);
			window.dismiss();
			break;
		case KeyEvent.KEYCODE_DPAD_DOWN:
			mSerachText.setText(cs + keys[0]);
			window.dismiss();
			break;
		default:
			break;
		}

		return false;
	}

	public int dip2px(float dipValue) {
		final float scale = mContext.getResources().getDisplayMetrics().density;
		return (int) (dipValue * scale + 0.5f);
	}

	public int px2dip(float pxValue) {
		final float scale = mContext.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}
}
