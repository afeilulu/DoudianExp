package cn.com.xinli.android.doudian.ui;


import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.DialogInterface.OnKeyListener;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import cn.com.xinli.android.doudian.R;
import cn.com.xinli.android.doudian.utils.UIUtils;

public abstract class BaseDialogFragment extends DialogFragment {
	public final static String TAG = "BaseDialogFragment";
	public OnDiaLogListener listener;
	public FragmentManager fm;

	public void setDialogResult(OnDiaLogListener listener) {
		this.listener = listener;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		if (UIUtils.isHoneycomb()) {
			setStyle(STYLE_NO_TITLE, android.R.style.Theme_Holo_Dialog);
		} else {
			setStyle(STYLE_NO_TITLE, android.R.style.Theme_Dialog);
		}
		super.onCreate(savedInstanceState);
	}

	public BaseDialogFragment(FragmentManager fm, Bundle data) {
		this.fm = fm;
		setArguments(data);
	}

	public void showDialog(String tag) {
		try {
			show(fm, tag);
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		getDialog().setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(DialogInterface dialog, int keyCode,
					KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_SEARCH)
					return true;
				else
					return false;
			}
		});
	}

	@Override
	public final View onCreateView(LayoutInflater inflater,
			ViewGroup container, Bundle bundle) {
		Log.d(TAG, "onCreateView executed");
		getDialog().getWindow().setBackgroundDrawableResource(
				android.R.color.transparent);
		return createView(inflater, container, bundle);
	}

	public void setTitle(String title) {
		if (title != null)
			getDialog().setTitle(title);
		else
			getDialog().setTitle(R.string.alert_title);
	}

	/**
	 * 创建视图
	 * 
	 * @param inflater
	 * @param container
	 * @param bundle
	 * @return
	 */
	protected abstract View createView(LayoutInflater inflater,
			ViewGroup container, Bundle bundle);

	public interface OnDiaLogListener {
		public void OnFinishDialLog(Object o);
	}

}
