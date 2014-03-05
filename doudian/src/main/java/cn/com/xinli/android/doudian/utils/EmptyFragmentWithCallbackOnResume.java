package cn.com.xinli.android.doudian.utils;

import android.app.Activity;
import android.app.Fragment;

public class EmptyFragmentWithCallbackOnResume extends Fragment {
	public static String TAG = "EmptyFragmentWithCallbackOnResume";
	OnFragmentAttachedListener mListener = null;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentAttachedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement OnFragmentAttachedListener");
        }
    }

	@Override
    public void onResume() {
        super.onResume();
        if (mListener != null) {
            mListener.OnFragmentAttached();
        }
    }

	public interface OnFragmentAttachedListener {
        public void OnFragmentAttached();
    }
}
