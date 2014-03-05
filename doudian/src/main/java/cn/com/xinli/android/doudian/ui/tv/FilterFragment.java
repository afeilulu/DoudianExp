package cn.com.xinli.android.doudian.ui.tv;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.Display;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.com.xinli.android.doudian.R;
import cn.com.xinli.android.doudian.model.Channel;
import cn.com.xinli.android.doudian.model.Method;
import cn.com.xinli.android.doudian.model.ProgramSimple;
import cn.com.xinli.android.doudian.service.DoudianService;
import cn.com.xinli.android.doudian.service.GetAreas;
import cn.com.xinli.android.doudian.service.GetCategories;
import cn.com.xinli.android.doudian.service.GetChannels;
import cn.com.xinli.android.doudian.service.GetProgramByPage;
import cn.com.xinli.android.doudian.service.GetProgramInAreaByPage;
import cn.com.xinli.android.doudian.service.GetProgramInAreaCategoryByPage;
import cn.com.xinli.android.doudian.service.GetProgramInCategoryByPage;
import cn.com.xinli.android.doudian.service.GetTotal;
import cn.com.xinli.android.doudian.service.GetTotalInArea;
import cn.com.xinli.android.doudian.service.GetTotalInAreaCategory;
import cn.com.xinli.android.doudian.service.GetTotalInCategory;
import cn.com.xinli.android.doudian.service.GetYears;
import cn.com.xinli.android.doudian.service.SyncService;
import cn.com.xinli.android.doudian.utils.ChannelsHolder;
import cn.com.xinli.android.doudian.utils.DetachableResultReceiver;
import cn.com.xinli.android.doudian.utils.ImageDownloaderLruCache;
import cn.com.xinli.android.doudian.utils.ImageObjectCache;
import cn.com.xinli.android.doudian.utils.UIUtils;


/**
 * 分类_年代_地区_类型
 * 分类：获取基本频道数据
 * 年代：某频道下的年代，前9个+更早
 * 地区：某频道下的地区，前10个
 * 类型：某频道下的类型，前10个
 * 没有选择时，结果显示全部的。选择后马上更新显示结果。
 */
public class FilterFragment extends Fragment implements DetachableResultReceiver.Receiver {
	private ImageDownloaderLruCache imageDownloaderLruCache;
	private ViewGroup root;
	private LinearLayout upFilterLayout;
	private LinearLayout downFilterLayout;
	private DetachableResultReceiver mReceiver;
	private ImageView imageLeft;
	private ImageView imageRight;
	private TextView pagesText;
	private final int BEGIN = 0;// 取数据的起始位置
	private final int COUNT = 200;// 取数据的个数
	private ArrayList<ProgramSimple> resultVideosSum = new ArrayList<ProgramSimple>();// 累加每次的结果
	private long videosSumPages = 0;// 累加每次结果页数
	private static final String TAG = "FilterFragment";
	private int startViewId = 10000;
	private final int NUMBER = 5;// 一行显示video个数
	private Display display;
	private long totalPages = 0;
	private int currentPage = 1;
	private long totalRecords = 0;// 总记录数
	private boolean initLayout = false;// 布局是否初始化
	private long turingAddDataPage = 0;// 翻页时需要加载数据的页号
	private boolean channelSpinnerLoading = false;
	private boolean otherSpinnerLoading = false;
	private boolean filterDataLoading = false;
    private boolean lastPageProgramDataReceived = false;
	private ProgressBar progressBar;
	private ImageObjectCache imageObjectCache;

	private PopupWindow window;
	private ListView filterList;
	Button btnChannel;
	Button btnCategory;
	Button btnRegion;
	Button btnPremiereDate;
	Button btnSortType;
	private SimpleAdapter adapterChannel;
	private SimpleAdapter adapterCategory;
	private SimpleAdapter adapterPremiereDate;
	private SimpleAdapter adapterRegion;
	private SimpleAdapter adapterSortType;
	private List<Map<String, String>> fillMapsChannel = new ArrayList<Map<String, String>>();
	private List<Map<String, String>> fillMapsCategory = new ArrayList<Map<String, String>>();
	private List<Map<String, String>> fillMapsPremiereDate = new ArrayList<Map<String, String>>();
	private List<Map<String, String>> fillMapsRegion = new ArrayList<Map<String, String>>();
	private List<Map<String, String>> fillMapsSortType = new ArrayList<Map<String, String>>();
	private String[] from = { "name", "filterId" };
	private int[] to = { R.id.name, R.id.filterId };
	private View popupView;
	private float mRatioItemToDivider = 0.618f;
	private ImageView filterInit;
	private TextView emptyTxt;
	private Map<String ,String> sortTypeMap=new HashMap<String, String>();
//	private static ObjectDownloaderCache objectDownloaderCache;

    private String mChannelId; // only one channel is select at a time,other wise blank is shown
    private int mPageIndex;
    private int mPageOfShown;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		root = (ViewGroup) inflater.inflate(R.layout.fragment_filter,
				container, false);
		progressBar = (ProgressBar) root.findViewById(R.id.progress_bar);
		emptyTxt=(TextView) root.findViewById(R.id.empty_txt);
		display = getActivity().getWindowManager().getDefaultDisplay();
		upFilterLayout = (LinearLayout) root.findViewById(R.id.upFilterLayout);
		downFilterLayout = (LinearLayout) root
				.findViewById(R.id.downFilterLayout);
		filterInit=(ImageView) root.findViewById(R.id.filter_init);
		ImageObjectCache.ImageCacheParams cacheParams = new ImageObjectCache.ImageCacheParams("thumbs");
		// be care of this value
		cacheParams.memCacheSize = 1024 * 1024 * UIUtils
				.getMemoryClass(getActivity()) / 8;
		imageDownloaderLruCache = new ImageDownloaderLruCache(getActivity());
		imageDownloaderLruCache.setImageThumbSize(120);
		imageDownloaderLruCache.setLoadingImage(R.drawable.item_default_image);
		imageObjectCache = ImageObjectCache.findOrCreateCache(getActivity(),
				cacheParams);
		imageDownloaderLruCache.setImageObjectCache(imageObjectCache);
		imageDownloaderLruCache.setFadeInBitmap(false);

		initFilter();
		initAdapter();
		return root;
	}

	private void initAdapter() {
		adapterChannel = new SimpleAdapter(getActivity(), fillMapsChannel,
				R.layout.popupwindow_list_item, from, to);
		adapterCategory = new SimpleAdapter(getActivity(), fillMapsCategory,
				R.layout.popupwindow_list_item, from, to);
		adapterPremiereDate = new SimpleAdapter(getActivity(),
				fillMapsPremiereDate, R.layout.popupwindow_list_item, from, to);
		adapterRegion = new SimpleAdapter(getActivity(), fillMapsRegion,
				R.layout.popupwindow_list_item, from, to);
		adapterSortType = new SimpleAdapter(getActivity(), fillMapsSortType,
				R.layout.popupwindow_list_item, from, to);
	}

	private void cleanFilter() {
		fillMapsCategory.clear();
		fillMapsPremiereDate.clear();
		fillMapsRegion.clear();
		fillMapsSortType.clear();
		adapterCategory.notifyDataSetChanged();
		adapterPremiereDate.notifyDataSetChanged();
		adapterRegion.notifyDataSetChanged();
		adapterSortType.notifyDataSetChanged();
		btnCategory.setText(getString(R.string.categoryStr));
		btnRegion.setText(getString(R.string.regionStr));
		btnPremiereDate.setText(getString(R.string.premiereDateStr));
		btnSortType.setText(getString(R.string.sortTypeStr));
		removeLayoutItems();
	}

	public void fillChannelData() {
		fillMapsChannel.clear();
        ArrayList<Channel> mDataList = ChannelsHolder.getInstance().getChannelArrayList();
		if (mDataList != null && mDataList.size() > 0) {
			filterInit.setVisibility(View.VISIBLE);

			for (int i = 0; i < mDataList.size(); i++) {
				Channel c = mDataList.get(i);
                Map<String, String> map = new HashMap<String, String>();
                map.put("name", c.getName());
                map.put("filterId", c.getId());
                fillMapsChannel.add(map);
			}
			adapterChannel.notifyDataSetChanged();
		}
		progressBar.setVisibility(View.INVISIBLE);
		channelSpinnerLoading = false;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onActivityCreated(savedInstanceState);
		mReceiver = new DetachableResultReceiver(new Handler());
		mReceiver.setReceiver(this);
		if (!channelSpinnerLoading) {
			progressBar.setVisibility(View.VISIBLE);
			channelSpinnerLoading = true;
		} else {
			Toast.makeText(getActivity(), "正在加载数据请稍后重试。", Toast.LENGTH_SHORT)
					.show();
		}

        fillChannelData();
	}

    /*    *
	 * 填充类型、地区、年代、排序 的数据
	 * @param mDataList

	private void fillOtherData(ArrayList<?> mDataList) {
//		Log.d(TAG, "---------mDataList:---"+mDataList.size());
		if (mDataList != null && mDataList.size() > 0) {
			setFilterBtnEnabled(true);
			for (int i = 0; i < mDataList.size(); i++) {
				Filter filter = (Filter) mDataList.get(i);
//				Log.d(TAG, "---------filter:---"+filter);
				if (filter != null) {
//					Log.d(TAG, "---------filter.getSelects():---"+filter.getSelects().size());
					Set<String> keySet=filter.getSelects().keySet();
					for (String key : keySet) {
						ArrayList<String> dataList = filter.getSelects().get(
								key);
						if (key.equals("分类")) {
							fillCatagoryFilter(dataList);
						} else if (key.equals("地区")) {
							fillRegionFilter(dataList);
						} else if (key.equals("上映年份")) {
							fillPremiereDateFilter(dataList);
						} else if (key.equals("排序")) {
							fillSortTypeFilter(dataList);
						}
					}
					if(!keySet.contains("分类")){
						btnCategory.setEnabled(false);
						btnCategory.setBackgroundResource(R.drawable.filter_disable_selector);
					}else{
						btnCategory.setBackgroundResource(R.drawable.filter_selector);
					}
					if(!keySet.contains("地区")){
						btnRegion.setEnabled(false);
						btnRegion.setBackgroundResource(R.drawable.filter_disable_selector);
					}else{
						btnRegion.setBackgroundResource(R.drawable.filter_selector);
					}
					if(!keySet.contains("上映年份")){
						btnPremiereDate.setEnabled(false);
						btnPremiereDate.setBackgroundResource(R.drawable.filter_disable_selector);
					}else{
						btnPremiereDate.setBackgroundResource(R.drawable.filter_selector);
					}
					if(!keySet.contains("排序")){
						btnSortType.setEnabled(false);
						btnSortType.setBackgroundResource(R.drawable.filter_disable_selector);
					}else{
						btnSortType.setBackgroundResource(R.drawable.filter_selector);
					}
				} else {
					Log.i(TAG, "filter is null");
				}
			}
		}else{//没有对应的过滤器
			setFilterBtnEnabled(false);
		}
		progressBar.setVisibility(View.INVISIBLE);
		otherSpinnerLoading = false;
	}*/
	private void setFilterBtnEnabled(boolean b){
//		btnCategory.setFocusable(b);
//		btnCategory.setFocusableInTouchMode(b);
		if(b){
			btnCategory.setBackgroundResource(R.drawable.filter_selector);
			btnPremiereDate.setBackgroundResource(R.drawable.filter_selector);
			btnRegion.setBackgroundResource(R.drawable.filter_selector);
			btnSortType.setBackgroundResource(R.drawable.filter_selector);
		}else{
			btnCategory.setBackgroundResource(R.drawable.filter_disable_selector);
			btnPremiereDate.setBackgroundResource(R.drawable.filter_disable_selector);
			btnRegion.setBackgroundResource(R.drawable.filter_disable_selector);
			btnSortType.setBackgroundResource(R.drawable.filter_disable_selector);
		}
		btnCategory.setEnabled(b);
		
		btnPremiereDate.setFocusable(b);
		btnPremiereDate.setFocusableInTouchMode(b);
		btnPremiereDate.setEnabled(b);
		
		btnRegion.setFocusable(b);
		btnRegion.setFocusableInTouchMode(b);
		btnRegion.setEnabled(b);
		
//		btnSortType.setFocusable(b);
//		btnSortType.setFocusableInTouchMode(b);
//		btnSortType.setEnabled(b);
		
		
	}
	private void fillCatagoryFilter(ArrayList<String> mList) {
		fillMapsCategory.clear();
		Map<String, String> map1 = new HashMap<String, String>();
		map1.put("name", "全部");
		fillMapsCategory.add(map1);
		for (String str : mList) {
			Map<String, String> map = new HashMap<String, String>();
//			Log.i(TAG, "------------catagory---------:"+str);
			map.put("name", str);
			fillMapsCategory.add(map);
		}
		adapterCategory.notifyDataSetChanged();
	}

	private void fillRegionFilter(ArrayList<String> mList) {
		fillMapsRegion.clear();
		Map<String, String> map1 = new HashMap<String, String>();
		map1.put("name", "全部");
		fillMapsRegion.add(map1);
		for (String str : mList) {
			Map<String, String> map = new HashMap<String, String>();
			map.put("name", str);
			fillMapsRegion.add(map);
		}
		adapterRegion.notifyDataSetChanged();
	}

	private void fillPremiereDateFilter(ArrayList<String> mList) {
		fillMapsPremiereDate.clear();
		Map<String, String> map1 = new HashMap<String, String>();
		map1.put("name", "全部");
		fillMapsPremiereDate.add(map1);
//		Collections.sort(mList,new Comparator<String>(){  
//            @Override  
//            public int compare(String b1, String b2) {  
//                return b2.compareTo(b1);  
//            }  
//              
//        }); 
		for (String str : mList) {
			Map<String, String> map = new HashMap<String, String>();
			map.put("name", str);
			fillMapsPremiereDate.add(map);
		}
		
		adapterPremiereDate.notifyDataSetChanged();
	}

	private void fillSortTypeFilter(ArrayList<String> mList) {
		fillMapsSortType.clear();
		Map<String, String> map1 = new HashMap<String, String>();
		map1.put("name", "全部");
		fillMapsSortType.add(map1);
		for (String str : mList) {
			Map<String, String> map = new HashMap<String, String>();
			map.put("name", str);
			fillMapsSortType.add(map);
		}
		adapterSortType.notifyDataSetChanged();
	}

	private void initFilter() {
		int tabviewid = getArguments().getInt("tabviewid");
		btnChannel = (Button) root.findViewById(R.id.btn_channel);
		btnCategory = (Button) root.findViewById(R.id.btn_category);
		btnRegion = (Button) root.findViewById(R.id.btn_region);
		btnPremiereDate = (Button) root.findViewById(R.id.btn_premiere_date);
		btnSortType = (Button) root.findViewById(R.id.btn_sort_type);
        btnSortType.setVisibility(View.GONE);
		btnChannel.setNextFocusUpId(tabviewid);
		btnChannel.setNextFocusLeftId(btnChannel.getId());
		btnCategory.setNextFocusUpId(tabviewid);
		btnRegion.setNextFocusUpId(tabviewid);
		btnPremiereDate.setNextFocusUpId(tabviewid);
		btnSortType.setNextFocusUpId(tabviewid);
		btnSortType.setNextFocusRightId(btnSortType.getId());

		btnChannel.setOnClickListener(listener);
		btnCategory.setOnClickListener(listener);
		btnRegion.setOnClickListener(listener);
		btnPremiereDate.setOnClickListener(listener);
		btnSortType.setOnClickListener(listener);
		btnChannel.setWidth(filterBtnWidth());
		btnCategory.setWidth(filterBtnWidth());
		btnRegion.setWidth(filterBtnWidth());
		btnPremiereDate.setWidth(filterBtnWidth());
		btnSortType.setWidth(filterBtnWidth());
		imageLeft = (ImageView) root.findViewById(R.id.left_image);
		imageRight = (ImageView) root.findViewById(R.id.right_image);
		pagesText = (TextView) root.findViewById(R.id.pages);
	}
	View.OnClickListener listener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			showPopupWindow(v);
		}
	};

	private void showPopupWindow(final View view) {
		LayoutInflater lay = (LayoutInflater) getActivity().getSystemService(
				Context.LAYOUT_INFLATER_SERVICE);
		popupView = lay.inflate(R.layout.popupwindow_list, null);
		filterList = (ListView) popupView.findViewById(R.id.filterList);
		window = new PopupWindow(popupView, view.getWidth(), 200, true);
		int[] location = new int[2];
		view.getLocationOnScreen(location);
		switch (view.getId()) {
		case R.id.btn_channel:
			filterList.setAdapter(adapterChannel);
			if (!adapterChannel.isEmpty()) {
				window.update();
				window.showAtLocation(view, Gravity.NO_GRAVITY, location[0]
						+ popupView.getWidth(), location[1] + view.getHeight());
				view.setBackgroundResource(R.drawable.filter_belongto);
			}
			break;
		case R.id.btn_category:
			filterList.setAdapter(adapterCategory);
			if (!adapterCategory.isEmpty()) {
				window.update();
				window.showAtLocation(view, Gravity.NO_GRAVITY, location[0]
						+ popupView.getWidth(), location[1] + view.getHeight());
				view.setBackgroundResource(R.drawable.filter_belongto);
			}
			break;
		case R.id.btn_region:
			filterList.setAdapter(adapterRegion);
			if (!adapterRegion.isEmpty()) {
				window.update();
				window.showAtLocation(view, Gravity.NO_GRAVITY, location[0]
						+ popupView.getWidth(), location[1] + view.getHeight());
				view.setBackgroundResource(R.drawable.filter_belongto);
			}
			break;
		case R.id.btn_premiere_date:
			filterList.setAdapter(adapterPremiereDate);
			if (!adapterPremiereDate.isEmpty()) {
				window.update();
				window.showAtLocation(view, Gravity.NO_GRAVITY, location[0]
						+ popupView.getWidth(), location[1] + view.getHeight());
				view.setBackgroundResource(R.drawable.filter_belongto);
			}
			break;
		case R.id.btn_sort_type:
			filterList.setAdapter(adapterSortType);
			if (!adapterSortType.isEmpty()) {
				window.update();
				window.showAtLocation(view, Gravity.NO_GRAVITY, location[0]
						+ popupView.getWidth(), location[1] + view.getHeight());
				view.setBackgroundResource(R.drawable.filter_belongto);
			}
			break;
		default:
			break;
		}

		filterList.setOnKeyListener(new View.OnKeyListener() {
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
//				Log.d(TAG, "---------keyCode-------:"+keyCode);
				if (event.getAction() == KeyEvent.ACTION_DOWN
						&& keyCode == KeyEvent.KEYCODE_BACK){
					view.setBackgroundResource(R.drawable.filter_selector);
					window.dismiss();
				}
				//向上
				if(event.getAction() == KeyEvent.ACTION_DOWN && keyCode==KeyEvent.KEYCODE_DPAD_UP){
					if(filterList.getSelectedItemPosition()==0){
						view.setBackgroundResource(R.drawable.filter_selector);
						window.dismiss();
						return true;
					}
					
				}
				return false;
			}
		});
		filterList.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				// TODO Auto-generated method stub
				Button b = (Button) view;
				TextView nameTxt = (TextView) arg1.findViewById(R.id.name);
				switch (b.getId()) {
				case R.id.btn_channel:
                    // start from here
                    // choose a channel
                    // we need top 10 years/top 10 areas/top 10 categories of this channel
					filterInit.setVisibility(View.GONE);
					b.setText("分类:" + nameTxt.getText());
					TextView filterTxt = (TextView) arg1
							.findViewById(R.id.filterId);
					String filter = filterTxt.getText().toString();

                    cleanFilter();
                    initQueryVideoByFilter();

					if (!otherSpinnerLoading) {
						otherSpinnerLoading = true;

                        // get channel id
                        mChannelId = fillMapsChannel.get(arg2).get("filterId");
                        startGetYearsService();
                        startGetAreasService();
                        startGetCategoriesService();
                        startGetTotal();

						progressBar.setVisibility(View.VISIBLE);
					} else {
						Toast.makeText(getActivity(), "正在加载数据请稍后重试。",
								Toast.LENGTH_SHORT).show();
					}

					break;
				case R.id.btn_category:
					b.setText("类型:" + nameTxt.getText());
					initQueryVideoByFilter();
                    startGetTotal();
					break;
				case R.id.btn_region:
					b.setText("地区:" + nameTxt.getText());
					initQueryVideoByFilter();
                    startGetTotal();
					break;
				case R.id.btn_premiere_date:
					b.setText("年代:" + nameTxt.getText());
					initQueryVideoByFilter();
                    startGetTotal();
					break;
				case R.id.btn_sort_type:
					b.setText("排序:" + nameTxt.getText());
					initQueryVideoByFilter();
                    startGetTotal();
					break;
				default:
					break;
				}
				b.setBackgroundResource(R.drawable.filter_selector);
				window.dismiss();
			}
		});
	}

	private int filterBtnWidth() {
		Display d = getActivity().getWindowManager().getDefaultDisplay();
		return d.getWidth() / 5;
	}

	private void initQueryVideoByFilter() {
		removeLayoutItems();
		turingAddDataPage = 0;
		totalRecords = 0;
		videosSumPages = 0;
		totalPages = 0;
		currentPage = 1;
		imageLeft.setVisibility(View.INVISIBLE);
		imageRight.setVisibility(View.INVISIBLE);
		pagesText.setVisibility(View.INVISIBLE);
		resultVideosSum.clear();

        mPageIndex = 1;
        mPageOfShown = 1;
	}

	private String formatFilterText(String t) {
		String bt = t.substring(t.indexOf(":") + 1, t.length());
//		Log.d(TAG, "------------bt------------------:" + bt);
		if (bt.equals("请选择")) {
			return "";
		}
		return bt;
	}

	private void removeLayoutItems() {
		initLayout = false;
		upFilterLayout.removeAllViews();
		downFilterLayout.removeAllViews();
	}

	private void setViewInfoByVideo(ProgramSimple ps, View rootView) {
		// 设置rootView layout
		LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
				width(), height());
		params.setMargins(0, 0, (int)MarginRight(), 0);
		rootView.setLayoutParams(params);
		rootView.setFocusable(true);
		rootView.setFocusableInTouchMode(true);
		rootView.setBackgroundResource(R.drawable.button_selector);
        int hashCode = ps.getHashCode();
		String poster = ps.getPoster();
		String name = ps.getName();
		TextView vName = (TextView) rootView.findViewById(R.id.card_text);
		ImageView image = (ImageView) rootView.findViewById(R.id.card_image);
		TextView updateTxt = (TextView) rootView.findViewById(R.id.card_update);
		updateTxt.setVisibility(View.INVISIBLE);
		imageDownloaderLruCache.download(poster, image, null);
		// TextView vid = (TextView) rootView.findViewById(R.id.vId);
		// vid.setText(videoId);
		rootView.setTag(hashCode);
		vName.setText(name);
		// vName.setSingleLine(true);
		// vName.setEllipsize(TruncateAt.MARQUEE);
		// vName.setMarqueeRepeatLimit(3);
		// vName.setFocusable(true);

		if (ps.getUpdateStatus() != null && ps.getUpdateStatus().length() > 0) {
			updateTxt.setText("更新至第" + ps.getUpdateStatus() + "集");
		}

		if (!updateTxt.getText().toString().equals("")) {
			updateTxt.setBackgroundResource(android.R.color.black);
			updateTxt.setAlpha(0.5f);
            updateTxt.setVisibility(View.VISIBLE);
		}

		// 设置下标为0、5、6、11的key事件
		int rVid = rootView.getId();
		// 上一页
		if (rVid == startViewId || rVid == startViewId + 5) {
			rootView.setNextFocusLeftId(rootView.getId());
			rootView.setOnKeyListener(new View.OnKeyListener() {

				@Override
				public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
					// TODO Auto-generated method stub
					// arg1:20下，19上，21左，22右
					// Log.d(TAG, "---------pageup---arg1----------:" + arg1);
					// Log.d(TAG, "---------pageup---arg2----------:" +
					// arg2.getAction());
					if (arg1 == 21 && arg2.getAction() == 0) {
						pageTurningByVideos("up");
						return true;
					}
					return false;
				}
			});
		}
		// 下一页
		if (rVid == startViewId + 4 || rVid == startViewId + 9) {
			rootView.setNextFocusRightId(rootView.getId());
			rootView.setOnKeyListener(new View.OnKeyListener() {

				@Override
				public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
					// TODO Auto-generated method stub
					// Log.d(TAG, "---------nextpage---arg1----------:" + arg1);
					// Log.d(TAG, "---------nextpage---arg2----------:" +
					// arg2.getAction());
					if (arg1 == 22 && arg2.getAction() == 0) {
						pageTurningByVideos("down");
						return true;
					}
					return false;
				}
			});
		}
		rootView.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
                int hashCode = (Integer)v.getTag();
                int start = (currentPage - 1) * NUMBER * 2;
                int end  = start + NUMBER * 2 + 1;
                ProgramSimple video = null;
                for (int i=start;i<end;i++){
                    ProgramSimple ps = resultVideosSum.get(i);
                    if (ps != null && ps.getHashCode() == hashCode){
                        video = ps;
                        break;
                    }
                }

				Intent intent = new Intent(getActivity(), DetailActivity.class);
                intent.putExtra(Intent.EXTRA_UID, new Gson().toJson(video));
                getActivity().startActivity(intent);
//                getActivity().startActivityForResult(intent, 1);
			}
		});

		rootView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
			
			@Override
			public void onFocusChange(View arg0, boolean arg1) {
				// TODO Auto-generated method stub
				TextView vName = (TextView) arg0.findViewById(R.id.card_text);
				if(arg1){
					vName.setSelected(true);
				}else{
					vName.setSelected(false);
				}
			}
		});
	}

	private void initLayoutByVideos() {

		progressBar.setVisibility(View.INVISIBLE);
		filterDataLoading = false;
		if (resultVideosSum != null) {

			videosSumPages = getTotalPages(resultVideosSum.size());
			if (!initLayout) {
				int size = resultVideosSum.size();
				if (size > NUMBER * 2) {
					for (int i = 0; i < NUMBER; i++) {
						View item = getActivity().getLayoutInflater().inflate(
								R.layout.horizontal_list_item, upFilterLayout,
								false);
						item.setId(startViewId + i);
						setViewInfoByVideo(resultVideosSum.get(i), item);
						upFilterLayout.addView(item);
					}
					for (int i = NUMBER; i < NUMBER * 2; i++) {
						View item = getActivity().getLayoutInflater().inflate(
								R.layout.horizontal_list_item,
								downFilterLayout, false);
						item.setId(startViewId + i);
						setViewInfoByVideo(resultVideosSum.get(i), item);
						downFilterLayout.addView(item);
					}
				} else {
					if (size <= NUMBER) {
						for (int i = 0; i < size; i++) {
							View item = getActivity().getLayoutInflater()
									.inflate(R.layout.horizontal_list_item,
											upFilterLayout, false);
							item.setId(startViewId + i);
							setViewInfoByVideo(resultVideosSum.get(i), item);
							upFilterLayout.addView(item);
						}
					} else {
						for (int i = 0; i < NUMBER; i++) {
							View item = getActivity().getLayoutInflater()
									.inflate(R.layout.horizontal_list_item,
											upFilterLayout, false);
							item.setId(startViewId + i);
							setViewInfoByVideo(resultVideosSum.get(i), item);
							upFilterLayout.addView(item);
						}
						for (int i = NUMBER; i < size; i++) {
							View item = getActivity().getLayoutInflater()
									.inflate(R.layout.horizontal_list_item,
											downFilterLayout, false);
							item.setId(startViewId + i);
							setViewInfoByVideo(resultVideosSum.get(i), item);
							downFilterLayout.addView(item);
						}
					}
				}
				initLayout = true;
				refreshPageInfo();
			} else {
				// 返回数据时当前页为加载数据页则刷新当前页
				if (currentPage == turingAddDataPage
						|| currentPage == (turingAddDataPage - 1)) {
//					Log.d(TAG, "---------refreshPage--------:" + currentPage);
					refreshPage(currentPage);
				}
			}
		}
	}

	private void refreshPageInfo() {
		if(totalRecords==0){
			pagesText.setVisibility(View.INVISIBLE);
			imageLeft.setVisibility(View.INVISIBLE);
			imageRight.setVisibility(View.INVISIBLE);
			emptyTxt.setVisibility(View.VISIBLE);
			return ;
		}
		emptyTxt.setVisibility(View.INVISIBLE);
		pagesText.setText(currentPage + "/" + totalPages + "页");
		pagesText.setVisibility(View.VISIBLE);
		if (currentPage > 1) {
			imageLeft.setImageResource(R.drawable.filter_left_more);
		} else {
			imageLeft.setImageResource(R.drawable.filter_left_nomore);
		}
		imageLeft.setVisibility(View.VISIBLE);
		if (currentPage < totalPages) {
			imageRight.setImageResource(R.drawable.filter_right_more);
		} else {
			imageRight.setImageResource(R.drawable.filter_right_nomore);
		}
		imageRight.setVisibility(View.VISIBLE);
		int positionU = 0;
		StringBuffer sbVidUp=new StringBuffer();
		for (int i = 0; i < upFilterLayout.getChildCount(); i++) {
			View v = upFilterLayout.getChildAt(i);
			sbVidUp.append(v.getTag()).append(",");
			v.setNextFocusRightId(-1);
			if (v.getVisibility() == View.INVISIBLE) {
				positionU = i;
				break;
			}
		}
//		Log.d(TAG, "-------positionU-------:"+positionU);
		if (positionU >= 0) {
			View v=null;
			if(positionU==0){
				 v = upFilterLayout.getChildAt(upFilterLayout.getChildCount() - 1);
			}else{
				 v = upFilterLayout.getChildAt(positionU - 1);
			}
            if (v != null)
			    v.setNextFocusRightId(v.getId());
		}

		int positionD = 0;
		StringBuffer sbVidDown=new StringBuffer();
		for (int i = 0; i < downFilterLayout.getChildCount(); i++) {
			View v = downFilterLayout.getChildAt(i);
			sbVidDown.append(v.getTag()).append(",");
			v.setNextFocusRightId(-1);
			if (v.getVisibility() == View.INVISIBLE) {
				positionD = i;
				break;
			}
		}
//		Log.d(TAG, "-------positionD-------:"+positionD);
		if (positionD >= 0) {
			View v=null;
			if(positionD==0){
				 v = downFilterLayout.getChildAt(downFilterLayout.getChildCount() - 1);
			}else{
				 v = downFilterLayout.getChildAt(positionD - 1);
			}
			if (v != null)
				v.setNextFocusRightId(v.getId());
		}

	}

	private void updateVideoInfo(ProgramSimple ps, View rootView) {
		int hashCode = ps.getHashCode();
		String poster = ps.getPoster();
		String name = ps.getName();
		TextView vName = (TextView) rootView.findViewById(R.id.card_text);
		ImageView image = (ImageView) rootView.findViewById(R.id.card_image);
		TextView updateTxt = (TextView) rootView.findViewById(R.id.card_update);
		updateTxt.setVisibility(View.INVISIBLE);
		imageDownloaderLruCache.download(poster, image, null);
		// TextView vid = (TextView) rootView.findViewById(R.id.vId);
		// vid.setText(videoId);
		rootView.setTag(hashCode);
		vName.setText(name);
		if (ps.getUpdateStatus() != null && ps.getUpdateStatus().length() > 0) {
			updateTxt.setText("更新至第" + ps.getUpdateStatus() + "集");
		}else{
			updateTxt.setText("");
		}
		if (!updateTxt.getText().toString().equals("")) {
			updateTxt.setBackgroundResource(android.R.color.black);
			updateTxt.setAlpha(0.5f);
            updateTxt.setVisibility(View.VISIBLE);
		}
		rootView.setVisibility(View.VISIBLE);
	}


    /**
     * 翻页,页数
     * @param turning
     */
	private void pageTurningByVideos(String turning) {
		// 翻页的范围必须在1-totalPages之间
		if (currentPage >= 1 && currentPage <= videosSumPages) {
			if (turning.equals("up")) {
				if (currentPage == 1) {
					return;
				}
				currentPage -= 1;
			} else if (turning.equals("down")) {
				if (currentPage == totalPages) {
					return;
				}
				if (currentPage < videosSumPages) {
					currentPage += 1;
				}
				// 再次加载数据
				startGetProgramsByPage();
			}
			refreshPage(currentPage);
		}
	}

	private void refreshPage(int currentPage) {
		int start = (NUMBER * 2) * (currentPage - 1);
		int end = start + NUMBER * 2;
		// Log.d(TAG, "-----------currentPage----------:"+currentPage);
		// Log.d(TAG,
		// "-----------turingAddDataPage----------:"+turingAddDataPage);
		// Log.d(TAG, "-----------start--------:" + start);
		// Log.d(TAG, "------------end-----------:" + end);
		// Log.d(TAG, "------------resultVideosSum-----------:" +
		// resultVideosSum.size());
		// Log.d(TAG, "------------totalRecords-----------:" + totalRecords);
		// Log.d(TAG, "------------videosSumPages-----------:" +
		// videosSumPages);
		int k = 0;
		for (int i = start; i < end; i++) {
			if (i < resultVideosSum.size()) {
				if (k < NUMBER) {
					View item = upFilterLayout.findViewById(startViewId + k);
					updateVideoInfo(resultVideosSum.get(i), item);
				} else {
					View item = downFilterLayout.findViewById(startViewId + k);
					updateVideoInfo(resultVideosSum.get(i), item);
				}
				k++;
			}
		}
		// 隐藏没有更新的View,并设置焦点位置
//		 Log.d(TAG, "---------k---------:"+k);
		if (k >= 1 && k <= NUMBER) {
			//焦点位置
			int position=0;
			boolean change=false;
			for (int i = 0; i < upFilterLayout.getChildCount(); i++) {
				if (i > (k - 1)) {
					change=true;
					upFilterLayout.getChildAt(i).setVisibility(View.INVISIBLE);
				}
				if(upFilterLayout.getChildAt(i).getVisibility()==View.VISIBLE){
					position=i;
				}
			}
//			Log.d(TAG, "--------upFilterLayout requestFocus position--------:"+position);
			if(change || position==NUMBER-1){
				upFilterLayout.getChildAt(position).requestFocus();
			}
			for (int i = 0; i < downFilterLayout.getChildCount(); i++) {
				downFilterLayout.getChildAt(i).setVisibility(View.INVISIBLE);
			}
		}
		if (k > NUMBER && k <= NUMBER * 2) {
			int newK = k - NUMBER - 1;
			//焦点位置
			int position=0;
			boolean change=false;
			for (int i = 0; i < downFilterLayout.getChildCount(); i++) {
				View view = downFilterLayout.getChildAt(i);
				if (i > newK) {
					change=true;
					view.setVisibility(View.INVISIBLE);
				}
				if(view.getVisibility()==View.VISIBLE){
					position=i;
				}
			}
//			Log.d(TAG, "-------- downFilterLayout requestFocus position--------:"+position);
			if(change){
				downFilterLayout.getChildAt(position).requestFocus();
			}
		}
		refreshPageInfo();
	}

	private long getTotalPages(long totalRecord) {
		if (totalRecord < NUMBER * 2) {
			return 1;
		} else {
			long count = totalRecord / (NUMBER * 2);
			long remainderCount = totalRecord % (NUMBER * 2) == 0 ? 0 : 1;
			return count + remainderCount;
		}
		// Log.d(TAG, "-------------totalPages----------:" + totalPages);
	}

	@Override
	public void onReceiveResult(int resultCode, Bundle resultData) {

		String serviceKey = resultData.getString(Intent.EXTRA_TEXT);
		switch (resultCode) {
		case SyncService.STATUS_RUNNING:
			if (isVisible()){
				if (serviceKey.equalsIgnoreCase(GetTotal.class
						.getSimpleName())
                        || serviceKey.equalsIgnoreCase(GetTotalInCategory.class.getSimpleName())
                        || serviceKey.equalsIgnoreCase(GetTotalInArea.class.getSimpleName())
                        || serviceKey.equalsIgnoreCase(GetTotalInAreaCategory.class.getSimpleName())){

                    otherSpinnerLoading = false;

					for (int i = 0; i < downFilterLayout.getChildCount(); i++) {
						View v = downFilterLayout.getChildAt(i);
						TextView updateTxt = (TextView) v.findViewById(R.id.card_update);
						updateTxt.setVisibility(View.INVISIBLE);
					}
					for (int i = 0; i < upFilterLayout.getChildCount(); i++) {
						View v = upFilterLayout.getChildAt(i);
						TextView updateTxt = (TextView) v.findViewById(R.id.card_update);
						updateTxt.setVisibility(View.INVISIBLE);
					}
				}
			}
			break;
		case SyncService.STATUS_FINISHED:
			if (!isVisible())
				break;

			/*if (serviceKey.equalsIgnoreCase(GetFilter.class
					.getSimpleName())) {
				ArrayList<?> mDataList = (ArrayList) resultData
						.getSerializable(GetFilter.class.getSimpleName());
				fillOtherData(mDataList);
			} else if (serviceKey.equalsIgnoreCase(GetVideoListByFilter.class
					.getSimpleName())) {
				ArrayList<?> mDataList = (ArrayList) resultData
						.getSerializable(GetVideoListByFilter.class
								.getSimpleName());
				fillVideoByFilter(mDataList);
			}else if (serviceKey.equalsIgnoreCase(GetSearchReqService.class
					.getSimpleName())){
				ArrayList<?> mDataList = (ArrayList) resultData
						.getSerializable(GetSearchReqService.class
								.getSimpleName());
				int page= Integer.parseInt(resultData.getString(Intent.EXTRA_SUBJECT));
//				Log.d(TAG, "----------currentPage-----------:"+currentPage);
//				Log.d(TAG, "----------page-----------:"+page);
				if(page==currentPage){
					updatePresentNumberOrPresentTime(mDataList);
				}
			}*/

            if (serviceKey.equalsIgnoreCase(GetYears.class.getSimpleName())){
                ArrayList<String> mDataList = (ArrayList) resultData
                        .getSerializable(GetYears.class.getSimpleName());
                    fillPremiereDateFilter(mDataList);
            } else if (serviceKey.equalsIgnoreCase(GetAreas.class.getSimpleName())){
                ArrayList<String> mDataList = (ArrayList) resultData
                        .getSerializable(GetAreas.class.getSimpleName());
                fillRegionFilter(mDataList);
            } else if (serviceKey.equalsIgnoreCase(GetCategories.class.getSimpleName())){
                ArrayList<String> mDataList = (ArrayList) resultData
                        .getSerializable(GetCategories.class.getSimpleName());
                fillCatagoryFilter(mDataList);
            } else if (serviceKey.equalsIgnoreCase(GetTotal.class
                    .getSimpleName())
                    || serviceKey.equalsIgnoreCase(GetTotalInCategory.class.getSimpleName())
                    || serviceKey.equalsIgnoreCase(GetTotalInArea.class.getSimpleName())
                    || serviceKey.equalsIgnoreCase(GetTotalInAreaCategory.class.getSimpleName())){

                setFilterBtnEnabled(true);

                mPageIndex = 1;
                // we get total number
                String result = (String) resultData.getSerializable(serviceKey);
                totalRecords = Long.parseLong(result);
                totalPages = getTotalPages(totalRecords);

                // start getting program data
                startGetProgramsByPage();

            } else if (serviceKey.equalsIgnoreCase(GetProgramInAreaCategoryByPage.class
                    .getSimpleName())
                    || serviceKey.equalsIgnoreCase(GetProgramInAreaByPage.class.getSimpleName())
                    || serviceKey.equalsIgnoreCase(GetProgramInCategoryByPage.class.getSimpleName())
                    || serviceKey.equalsIgnoreCase(GetProgramByPage.class.getSimpleName())){

                lastPageProgramDataReceived = true;

                progressBar.setVisibility(View.INVISIBLE);

                ArrayList<ProgramSimple> mDataList = (ArrayList) resultData
                        .getSerializable(serviceKey);

                // if a fresh request
                if (mPageIndex < 2)
                    resultVideosSum.clear();

                // just one page data received, append to a global preserved variable
                for (ProgramSimple ps:mDataList){
                    resultVideosSum.add(ps);
                }

                initLayoutByVideos();
            }

			break;
		case SyncService.STATUS_ERROR:

			if (!isVisible())
				break;
			Toast.makeText(getActivity(), getString(R.string.network_timeout), Toast.LENGTH_SHORT)
					.show();
			if (serviceKey.equalsIgnoreCase(GetYears.class
					.getSimpleName())
                    || serviceKey.equalsIgnoreCase(GetAreas.class.getSimpleName())
                    || serviceKey.equalsIgnoreCase(GetCategories.class.getSimpleName())) {
				otherSpinnerLoading = false;
			} else if (serviceKey.equalsIgnoreCase(GetProgramInAreaCategoryByPage.class
                    .getSimpleName())
                    || serviceKey.equalsIgnoreCase(GetProgramInAreaByPage.class.getSimpleName())
                    || serviceKey.equalsIgnoreCase(GetProgramInCategoryByPage.class.getSimpleName())
                    || serviceKey.equalsIgnoreCase(GetProgramByPage.class.getSimpleName())){

                lastPageProgramDataReceived = false;
				filterDataLoading = false;
			}
			progressBar.setVisibility(View.INVISIBLE);
			break;

		}
	}

	private int width() {
		int left=imageLeft.getLeft();
		int right=display.getWidth()-imageRight.getRight();
		int centerWidth=display.getWidth()-left-right;
		int viewWidth=(int)((centerWidth-(height()+80)*mRatioItemToDivider-MarginRight()))/NUMBER;
		
//		Log.d(TAG, "---left---:"+left);
//		Log.d(TAG, "---right---:"+right);
//		Log.d(TAG, "---centerWidth---:"+centerWidth);
//		Log.d(TAG, "---viewWidth---:"+ viewWidth);
		return viewWidth;
	}

	private int height() {
		RelativeLayout layout=(RelativeLayout) root.findViewById(R.id.centerLayout);
		int pageTxtBottom=pagesText.getHeight();
		int centerHeight=(layout.getHeight()-pageTxtBottom)/2;
//		Log.d(TAG, "----------layout.getHeight()---------:"+layout.getHeight());
//		Log.d(TAG, "----------pageTxtBottom---------:"+pageTxtBottom);
//		Log.d(TAG, "----------centerHeight---------:"+centerHeight);
		return centerHeight;
	}
	
	private float MarginRight(){ 
		return (height()+80)*mRatioItemToDivider/(NUMBER-1);
	}

    private void startGetYearsService(){
        Intent intent = new Intent(Intent.ACTION_SYNC, null, getActivity(),
                GetYears.class);
        Method method = new Method();
        method.setName(DoudianService.GET_YEARS);
        method.setChannelID(mChannelId);
        intent.putExtra(Intent.EXTRA_TEXT,GetYears.class.getSimpleName());
        intent.putExtra(Intent.EXTRA_UID, new Gson().toJson(method));
        intent.putExtra(SyncService.EXTRA_STATUS_RECEIVER, mReceiver);
        getActivity().startService(intent);
    }

    private void startGetAreasService(){
        Intent intent = new Intent(Intent.ACTION_SYNC, null, getActivity(),
                GetAreas.class);
        Method method = new Method();
        method.setName(DoudianService.GET_AREAS);
        method.setChannelID(mChannelId);
        intent.putExtra(Intent.EXTRA_TEXT,GetAreas.class.getSimpleName());
        intent.putExtra(Intent.EXTRA_UID, new Gson().toJson(method));
        intent.putExtra(SyncService.EXTRA_STATUS_RECEIVER, mReceiver);
        getActivity().startService(intent);
    }

    private void startGetCategoriesService(){
        Intent intent = new Intent(Intent.ACTION_SYNC, null, getActivity(),
                GetCategories.class);
        Method method = new Method();
        method.setName(DoudianService.GET_CATEGORIES);
        method.setChannelID(mChannelId);
        intent.putExtra(Intent.EXTRA_TEXT,GetCategories.class.getSimpleName());
        intent.putExtra(Intent.EXTRA_UID, new Gson().toJson(method));
        intent.putExtra(SyncService.EXTRA_STATUS_RECEIVER, mReceiver);
        getActivity().startService(intent);
    }

    /**
     * according user selection,four kind of services would be invoked
     * if year is not selected, do not specify it in method object
     * if area and category both are not selected, invoke GetTotal
     * if area and category both are selected, invoke GetTotalInAreaCategory
     * if area is not selected and category is, invoke GetTotalInCategory
     * if area is selected and category is not, invoke GetTotalInArea
     */
    private void startGetTotal(){

        String year = formatFilterText(btnPremiereDate.getText().toString());
        String category = formatFilterText(btnCategory.getText().toString());
        String area = formatFilterText(btnRegion.getText().toString());

        if (year.equals("全部")) year = "";
        if (category.equals("全部")) category = "";
        if (area.equals("全部")) area = "";

        Intent intent;
        Method method = new Method();
        method.setChannelID(mChannelId);
        if (year.length() > 0){
            method.setMaxYear(Integer.parseInt(year));
            method.setMinYear(Integer.parseInt(year));
        }

        String[] categories = new String[1];
        String[] areas = new String[1];
        if (category.length() > 0 && area.length() > 0){
            categories[0] = category;
            areas[0] = area;
            method.setName(DoudianService.GET_TOTAL_IN_AREA_CATEGORY);
            method.setCategories(categories);
            method.setAreas(areas);

            intent = new Intent(Intent.ACTION_SYNC, null, getActivity(),
                    GetTotalInAreaCategory.class);
            intent.putExtra(Intent.EXTRA_TEXT,GetTotalInAreaCategory.class.getSimpleName());
        } else if (category.length() > 0){
            categories[0] = category;
            method.setName(DoudianService.GET_TOTAL_IN_CATEGORY);
            method.setCategories(categories);

            intent = new Intent(Intent.ACTION_SYNC, null, getActivity(),
                    GetTotalInCategory.class);
            intent.putExtra(Intent.EXTRA_TEXT,GetTotalInCategory.class.getSimpleName());
        } else if (area.length() > 0){
            areas[0] = area;
            method.setName(DoudianService.GET_TOTAL_IN_AREA);
            method.setAreas(areas);

            intent = new Intent(Intent.ACTION_SYNC, null, getActivity(),
                    GetTotalInArea.class);
            intent.putExtra(Intent.EXTRA_TEXT,GetTotalInArea.class.getSimpleName());
        } else {
            method.setName(DoudianService.GET_TOTAL);

            intent = new Intent(Intent.ACTION_SYNC, null, getActivity(),
                    GetTotal.class);
            intent.putExtra(Intent.EXTRA_TEXT,GetTotal.class.getSimpleName());
        }

        intent.putExtra(Intent.EXTRA_UID, new Gson().toJson(method));
        intent.putExtra(SyncService.EXTRA_STATUS_RECEIVER, mReceiver);
        getActivity().startService(intent);
    }

    /**
     * according user selection,four kind of services would be invoked
     * if year is not selected, do not specify it in method object
     * if area and category both are not selected, invoke GetProgramByPage
     * if area and category both are selected, invoke GETPROGRAMINAREACATEGORYBYPAGE
     * if area is not selected and category is, invoke GetProgramInCategoryByPage
     * if area is selected and category is not, invoke GetProgramInAreaByPage
     */
    private void startGetProgramsByPage(){

        if (totalRecords <= resultVideosSum.size() )
            return;

        if (!lastPageProgramDataReceived)
            mPageIndex--;

        String year = formatFilterText(btnPremiereDate.getText().toString());
        String category = formatFilterText(btnCategory.getText().toString());
        String area = formatFilterText(btnRegion.getText().toString());

        if (year.equals("全部")) year = "";
        if (category.equals("全部")) category = "";
        if (area.equals("全部")) area = "";

        Intent intent;
        Method method = new Method();
        method.setChannelID(mChannelId);
        if (year.length() > 0){
            method.setMaxYear(Integer.parseInt(year));
            method.setMinYear(Integer.parseInt(year));
        }
        method.setPageIndex(mPageIndex++);

        String[] categories = new String[1];
        String[] areas = new String[1];
        if (category.length() > 0 && area.length() > 0){
            categories[0] = category;
            areas[0] = area;
            method.setName(DoudianService.GET_PROGRAM_IN_AREA_CATEGORY_BY_PAGE);
            method.setCategories(categories);
            method.setAreas(areas);

            intent = new Intent(Intent.ACTION_SYNC, null, getActivity(),
                    GetProgramInAreaCategoryByPage.class);
            intent.putExtra(Intent.EXTRA_TEXT,GetProgramInAreaCategoryByPage.class.getSimpleName());
        } else if (category.length() > 0){
            categories[0] = category;
            method.setName(DoudianService.GET_PROGRAM_IN_CATEGORY_BY_PAGE);
            method.setCategories(categories);

            intent = new Intent(Intent.ACTION_SYNC, null, getActivity(),
                    GetProgramInCategoryByPage.class);
            intent.putExtra(Intent.EXTRA_TEXT,GetProgramInCategoryByPage.class.getSimpleName());
        } else if (area.length() > 0){
            areas[0] = area;
            method.setName(DoudianService.GET_PROGRAM_IN_AREA_BY_PAGE);
            method.setAreas(areas);

            intent = new Intent(Intent.ACTION_SYNC, null, getActivity(),
                    GetProgramInAreaByPage.class);
            intent.putExtra(Intent.EXTRA_TEXT,GetProgramInAreaByPage.class.getSimpleName());
        } else {
            method.setName(DoudianService.GET_PROGRAM_BY_PAGE);

            intent = new Intent(Intent.ACTION_SYNC, null, getActivity(),
                    GetProgramByPage.class);
            intent.putExtra(Intent.EXTRA_TEXT,GetProgramByPage.class.getSimpleName());
        }

        intent.putExtra(Intent.EXTRA_UID, new Gson().toJson(method));
        intent.putExtra(SyncService.EXTRA_STATUS_RECEIVER, mReceiver);
        getActivity().startService(intent);
    }
}
