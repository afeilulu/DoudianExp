package cn.com.xinli.android.doudian.utils;

import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ListView;

public class ListViewLoop {
	private ListView mListView;
	private LoopListener mLoopListener;
	private LRListener mLRListener;

	private ListViewLoop(ListView listView, LoopListener loopListener) {
		mListView = listView;
		mLoopListener = loopListener;
	}
	
	private ListViewLoop(ListView listView, LoopListener loopListener,LRListener lrListener) {
		mListView = listView;
		mLoopListener = loopListener;
		mLRListener = lrListener;
	}

	public static void EnLoop(ListView listView, LoopListener loopListener,LRListener lrListener) {
		new ListViewLoop(listView, loopListener,lrListener).looper();
	}

	private void looper() {
		mListView.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				mLoopListener.setPos(position);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
		mListView.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (event.getAction() == KeyEvent.ACTION_DOWN) {
					if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
						if (mLoopListener.getPos() == mListView.getCount() - 1) {
							mListView.setSelection(0);
							return true;
						}
					} else if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
						if (mLoopListener.getPos() == 0) {
							mListView.setSelection(mListView.getCount() - 1);
							return true;
						}
					} else if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
						if(mLRListener!=null) {
							mLRListener.onLeft();
							return true;
						}
					} else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
						if(mLRListener!=null) {
							mLRListener.onRight();
							return true;
						}
					}
				}
				return false;
			}
		});
	}

	public interface LoopListener {
		public void setPos(int id);

		public int getPos();
	}

	public interface LRListener {
		public void onRight();

		public void onLeft();
	}
}
