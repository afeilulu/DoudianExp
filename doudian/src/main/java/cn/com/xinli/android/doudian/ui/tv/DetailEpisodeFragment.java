package cn.com.xinli.android.doudian.ui.tv;

import android.app.FragmentManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collection;

import cn.com.xinli.android.doudian.R;
import cn.com.xinli.android.doudian.io.DetailSource;
import cn.com.xinli.android.doudian.model.Episode;
import cn.com.xinli.android.doudian.model.Source;
import cn.com.xinli.android.doudian.ui.BaseDialogFragment;
import cn.com.xinli.android.doudian.ui.widget.ArrayColorAdapter;
import cn.com.xinli.android.doudian.ui.widget.SelectView;
import cn.com.xinli.android.doudian.ui.widget.SelectView.OnItemClickListener;
import cn.com.xinli.android.doudian.utils.DetachableResultReceiver;
import cn.com.xinli.android.doudian.utils.SourceHolder;

public class DetailEpisodeFragment extends BaseDialogFragment implements
		DetachableResultReceiver.Receiver {

	private DetachableResultReceiver mReceiver;

	public DetailEpisodeFragment(FragmentManager fm, Bundle data) {
		super(fm, data);
	}

	public final static String TAG = "DetailEpisodeFragment";
	ArrayList<DetailSource> dslist;
	private SelectView tvSourceList;
	ListView leftList;
	ListView rightListView;
	ArrayAdapter<String> leftAdapter;
	ArrayColorAdapter<String> rightAdapter;
	ArrayList<String> sources = new ArrayList<String>();
//	ArrayList<String> leftlist = new ArrayList<String>();
	ArrayList<String> rightlist = new ArrayList<String>();
	int curPy = 0;
	int curJm = 0;
	int curIn = 0;

	@Override
	protected View createView(LayoutInflater inflater, ViewGroup container,
			Bundle bundle) {
//		Intent actdata = getActivity().getIntent();
//		String menuId = actdata.getStringExtra(Intent.EXTRA_UID);
		mReceiver = new DetachableResultReceiver(new Handler());
		mReceiver.setReceiver(this);

		View v = inflater.inflate(R.layout.fragment_detail_episode, container,
				false);
		tvSourceList = (SelectView) v.findViewById(R.id.source_list);
//		leftList = (ListView) v.findViewById(R.id.episode_left);
        rightListView = (ListView) v.findViewById(R.id.episode_right);
//		tvSourceList.setNextFocusDownId(R.id.episode_left);
		/*leftAdapter = new ArrayAdapter<String>(getActivity(),
				R.layout.listview_text, leftlist);
		leftList.setAdapter(leftAdapter);
		leftList.setOnItemSelectedListener(new OnItemSelectedListener() {
			@Override
			public void onItemClicked(AdapterView<?> parent, View view,
					int position, long id) {
				curJm = position;
				updateRight();
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
		leftList.setOnKeyListener(new OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {
					if (rightList.isFocusableInTouchMode()) {
						rightList.requestFocusFromTouch();
					} else {
						rightList.requestFocus();
					}
					rightList.setSelection(curIn);
					return true;
				} else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
					if (rightList.isFocusableInTouchMode()) {
						rightList.requestFocusFromTouch();
					} else {
						rightList.requestFocus();
					}
					rightList.setSelection(curIn);
					return true;
				}
				return false;
			}
		});
		leftList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				curJm = position;
				updateRight();
			}
		});*/
		rightAdapter = new ArrayColorAdapter<String>(getActivity(),
				R.layout.listview_text, rightlist);
        rightListView.setAdapter(rightAdapter);
        rightListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                curIn = position;
                if (listener != null) {
                    Integer[] index = new Integer[3];
                    index[0] = curPy;
                    index[1] = curJm;
                    index[2] = curIn;
                    listener.OnFinishDialLog(index);
                    dismiss();
                }
                Log.i(TAG, "curPy=" + curPy + ",curJm=" + curJm + ",curIn="
                        + curIn);
            }
        });
		/*ListViewLoop.EnLoop(rightList, new LoopListener() {
			@Override
			public void setPos(int id) {
				curIn = id;
			}

			@Override
			public int getPos() {
				return curIn;
			}
		},new LRListener() {
			@Override
			public void onRight() {
				if (listener != null) {
					Integer[] index = new Integer[3];
					index[0] = curPy;
					index[1] = curJm;
					index[2] = curIn;
					listener.OnFinishDialLog(index);
					dismiss();
				}
			}
			@Override
			public void onLeft() {
				if (leftList.isFocusableInTouchMode()) {
					leftList.requestFocusFromTouch();
				} else {
					leftList.requestFocus();
				}
				leftList.setSelection(curJm);
			}
		});
		Bundle data = getArguments();
		if (data != null) {
			dslist = data.getParcelableArrayList("dslist");
			curPy = data.getInt("pyIndex", 0);
			updateSource();
			updateLeft();
			updateRight();
		}*/
        updateSource();
		return v;
	}

	@Override
	public void onResume() {
		super.onResume();
		if (rightListView.isFocusableInTouchMode()) {
            rightListView.requestFocusFromTouch();
		} else {
            rightListView.requestFocus();
		}
	}

	private void updateSource() {
        for (Source source: SourceHolder.getInstance().getSourcesList()){
            sources.add(source.getName());
        }

		tvSourceList.setList(sources);
		tvSourceList.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClicked(View view, int position) {
				Log.i(TAG, "view id=" + position);
				curPy = position;
				curJm = 0;
                updateRight(position);
			}
		});
        tvSourceList.setOnItemSelectedListener(new SelectView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int position) {
                updateRight(position);
            }
        });
        tvSourceList.setSelected(0);
	}

	/*private void updateLeft() {
		if (curPy < sources.size()) {
			if (curPy < dslist.size()) {
				leftlist.clear();
				DetailSource source = (DetailSource) dslist.get(curPy);
				ArrayList<DetailName> namelist = source.getName();
				for (int i = 0; i < namelist.size(); i++) {
					leftlist.addRecent(namelist.get(i).getName());
				}
				leftAdapter.notifyDataSetChanged();
			}
		}

	}*/

	private void updateRight(int position) {
        Source source = SourceHolder.getInstance().getSourcesList().get(position);
        Collection<Episode> episodeList = SourceHolder.getInstance().getEpisodeMap().get(source.getAlias());

        if (episodeList != null && episodeList.size() > 1){
            rightListView.setVisibility(View.VISIBLE);
            rightlist.clear();
            for (Episode episode:episodeList){
                rightlist.add(episode.getName());
            }

            rightAdapter.notifyDataSetChanged();
        } else {
            rightListView.setVisibility(View.GONE);
        }
	}

	@Override
	public void onReceiveResult(int resultCode, Bundle resultData) {

	}
}
