package cn.com.xinli.android.doudian.utils;

import android.app.FragmentManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import cn.com.xinli.android.doudian.R;
import cn.com.xinli.android.doudian.ui.BaseDialogFragment;

public class CustomDialogFragment extends BaseDialogFragment{
	public final static String TAG="dialog";

	public CustomDialogFragment(FragmentManager fm, Bundle data) {
		super(fm, data);
	}

	private OnItemDeleteListener mItemDeleteListener;
	private OnAllDeleteListener mAllDeleteListener;
	
	
	public interface OnItemDeleteListener{
		public abstract void delete();
	}
	
	public void setOnItemDeleteListener(OnItemDeleteListener itemDeleteListener){
		mItemDeleteListener = itemDeleteListener;
	}
	
	public interface OnAllDeleteListener{
		public abstract void delete();
	}
	
	public void setOnAllDeleteListener(OnAllDeleteListener allDeleteListener){
		mAllDeleteListener = allDeleteListener;
	}

	@Override
	protected View createView(LayoutInflater inflater, ViewGroup container,
			Bundle bundle) {
		View v = inflater.inflate(R.layout.custom_dialog, container,
				false);
		v.findViewById(R.id.dialog_cancel).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				dismiss();
			}
		});
		
		v.findViewById(R.id.item_delete).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (mItemDeleteListener != null)
					mItemDeleteListener.delete();
			}
		});
		
		v.findViewById(R.id.all_item_delete).setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				if (mAllDeleteListener != null)
					mAllDeleteListener.delete();
			}
		});
		return v;
	}


	
}
