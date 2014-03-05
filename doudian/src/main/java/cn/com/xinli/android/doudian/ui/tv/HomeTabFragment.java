package cn.com.xinli.android.doudian.ui.tv;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import cn.com.xinli.android.doudian.R;
import cn.com.xinli.android.doudian.database.DatabaseHandler;
import cn.com.xinli.android.doudian.model.Channel;
import cn.com.xinli.android.doudian.model.Method;
import cn.com.xinli.android.doudian.model.ProgramSimple;
import cn.com.xinli.android.doudian.model.Recent;
import cn.com.xinli.android.doudian.service.DoudianService;
import cn.com.xinli.android.doudian.service.GetCategories;
import cn.com.xinli.android.doudian.service.GetProgramByPage;
import cn.com.xinli.android.doudian.service.GetProgramInCategoryByPage;
import cn.com.xinli.android.doudian.service.GetSpecialChannelsByPage;
import cn.com.xinli.android.doudian.service.GetTotal;
import cn.com.xinli.android.doudian.service.GetTotalInCategory;
import cn.com.xinli.android.doudian.service.GetTotalOfSpecialChannels;
import cn.com.xinli.android.doudian.service.SyncService;
import cn.com.xinli.android.doudian.ui.widget.CScrollView;
import cn.com.xinli.android.doudian.ui.widget.HorizontalListView.ArrayAdapter;
import cn.com.xinli.android.doudian.ui.widget.HorizontalListView.HorizontalVariableListView;
import cn.com.xinli.android.doudian.ui.widget.HorizontalListView.HorizontalVariableListView.OnUpAndDownListener;
import cn.com.xinli.android.doudian.utils.CustomDialogFragment;
import cn.com.xinli.android.doudian.utils.CustomDialogFragment.*;
import cn.com.xinli.android.doudian.utils.DetachableResultReceiver;
import cn.com.xinli.android.doudian.utils.DetachableResultReceiver.Receiver;
import cn.com.xinli.android.doudian.utils.ImageDownloaderLruCache;
import cn.com.xinli.android.doudian.utils.ImageObjectCache;
import cn.com.xinli.android.doudian.utils.ImageObjectCache.ImageCacheParams;
import cn.com.xinli.android.doudian.utils.UIDimensionHolder;
import cn.com.xinli.android.doudian.utils.UIUtils;

public class HomeTabFragment extends Fragment implements Receiver {
	
	public final static String TAG = "HomeTabFragment";
	private static Context mContextActivity;
	private ViewGroup mRootView;
	private static DetachableResultReceiver mReceiver;
	private ArrayList<String> mCategoryList;
	private int mCategoryIndex;
	private int mCategoryCount;
	private static String mChannelId;
	private int mCurrentTabViewId;
	private boolean isReloaded;
	private ImageView mFocusMask;
	private ProgressBar mProgress;
	private LinearLayout mScrollViewRoot;
	private CScrollView mScrollView;
	private LinearLayout mTips;
	private int mScrollViewDistance = 3;
	private int SCROLLVIEWMAXCOUNT = 20;
	private int mCountInLine = 5;
//	private int mRatioItemToDivider = 4;
	private float mRatioItemToDivider = 0.618f;
	private int mHorizontalListViewHorizontalMargin;
	private int mCategoryBlankItemHeight;
	private static int mDividerWidth;
	private int mScrollStartY;
	private SwitchActions switchActions = new SwitchActions();
	private static ImageDownloaderLruCache imageDownloaderLruCache ;
	private static ImageObjectCache imageObjectCache;
	private static ImageCacheParams cacheParams;

	private static FragmentManager fm;
	private static long timeStamp;
	
	private Rect mViewRect = new Rect();

    private Map<String,CardsAdapter> mAdapterMap = new HashMap<String,CardsAdapter>();
    private Map<String,String> mCategoryTotalSizeMap = new HashMap<String,String>();
    private static final int PAGESIZE = 20;
    private DatabaseHandler databaseHandler;

    private ArrayList<Channel> mSpecialChannelList;
    private int mTotalOfSpecailChannels;
    private int mSpecialChannelPageIndex;

    private static final String MYFAVORITE_TAG = "我的收藏";
    private static final String RECENT_TAG = "最近观看";

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
//		setRetainInstance(true);

        databaseHandler = new DatabaseHandler(getActivity());
		
		mContextActivity = this.getActivity();
		mReceiver = new DetachableResultReceiver(new Handler());
        mReceiver.setReceiver(this);
        
//        switchActions = new SwitchActions();
        
        cacheParams = new ImageCacheParams("thumbs");
		// be care of this value
		cacheParams.memCacheSize = 1024 * 1024 * UIUtils.getMemoryClass(getActivity()) / 4;
		cacheParams.memoryCacheEnabled=true;
		cacheParams.diskCacheEnabled = true;
		imageDownloaderLruCache = new ImageDownloaderLruCache(getActivity());
		imageDownloaderLruCache.setImageThumbSize(120);
		imageDownloaderLruCache.setLoadingImage(R.drawable.item_default_image);
		imageObjectCache = ImageObjectCache.findOrCreateCache(getActivity(), cacheParams);
		imageDownloaderLruCache.setImageObjectCache(imageObjectCache);
		imageDownloaderLruCache.setFadeInBitmap(false);
		
		// define horizontal list view left and right margin
		switch (mCountInLine) {
		case 5:
			mHorizontalListViewHorizontalMargin = 64;
			break;

		default:
			mHorizontalListViewHorizontalMargin = 32;
			break;
		}
		
	}
	
	public void reload(String id, int currentTabViewId){
		switchActions.setId(id);
		switchActions.setCurrentTabViewId(currentTabViewId);
		switchActions.run();
		
		mFocusMask.setVisibility(View.INVISIBLE);
		
		if (id.equals(HomeBlueActivity.MYDOUDIAN_TAB))
			mTips.setVisibility(View.VISIBLE);
		else
			mTips.setVisibility(View.GONE);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		mRootView = (ViewGroup) inflater.inflate(R.layout.fragment_stack, null);
//		mRootView.setBackgroundDrawable(getResources().getDrawable(android.R.drawable.gallery_thumb));
		
		mFocusMask = (ImageView)mRootView.findViewById(R.id.focusmask);
		mViewRect.set(mFocusMask.getLeft(),mFocusMask.getTop(),mFocusMask.getRight(),mFocusMask.getBottom());
		mProgress = (ProgressBar) mRootView.findViewById(R.id.progress);
		mScrollViewRoot = (LinearLayout) mRootView.findViewById(R.id.scrollviewroot);
		mTips = (LinearLayout) mRootView.findViewById(R.id.tips);
		mScrollView = (CScrollView)mRootView.findViewById(R.id.scrollview);
		mScrollView.setOverScrollMode(ScrollView.OVER_SCROLL_NEVER);
		mScrollView.setSmoothScrollingEnabled(true);
		
		mScrollView.setPadding(0, 0, 0, 0);
		
        if (getArguments() != null){
			mChannelId = getArguments().getString("id");
			mCurrentTabViewId = getArguments().getInt("tabviewid");
			reload(mChannelId,mCurrentTabViewId);
		}

        fm = ((Activity)mContextActivity).getFragmentManager();
        
		return mRootView;
	}

	@Override
	public void onLowMemory() {
		super.onLowMemory();
////		mDataSet.clear();
//		mScrollViewRoot.removeAllViews();
		Log.d(TAG, "onLowMemory happened");
	}
	
	@Override
	public void onDetach() {
		super.onDetach();
//		imageDownloaderLruCache.setExitTasksEarly(true);
//		objectDownloaderCache.setExitTasksEarly(true);
	}
	
	@Override
	public void onPause() {
		super.onPause();
//		imageDownloaderLruCache.setExitTasksEarly(true);
//		objectDownloaderCache.setExitTasksEarly(true);
	}

	@Override
	public void onResume() {
		super.onResume();
//		imageDownloaderLruCache.setExitTasksEarly(false);
//		objectDownloaderCache.setExitTasksEarly(false);
	}

	@Override
	public void onReceiveResult(int resultCode, Bundle resultData) {

		Log.d(TAG,"resultCode = " + resultCode);
		switch (resultCode) {
			case SyncService.STATUS_RUNNING: {
				String serviceKey = resultData.getString(Intent.EXTRA_TEXT);
				
				if (serviceKey.equalsIgnoreCase(GetCategories.class
						.getSimpleName())
                        || serviceKey.equalsIgnoreCase(GetSpecialChannelsByPage.class
                            .getSimpleName())) {

                    if (mCategoryList != null)
                        mCategoryList.clear();

                    for (CardsAdapter ca:mAdapterMap.values()){
                        ca.clear();
                        ca = null;
                    }
                    mAdapterMap.clear();
                    mCategoryTotalSizeMap.clear();
                    mScrollViewRoot.removeAllViews();

					// start loading
					mProgress.setVisibility(View.VISIBLE);

                    isReloaded = true;
					
				}
				
				break;
			}
			case SyncService.STATUS_FINISHED: {
				String serviceKey = resultData.getString(Intent.EXTRA_TEXT);
				
//				if (!isVisible()) break;
				
				if (serviceKey.equalsIgnoreCase(GetCategories.class
						.getSimpleName())) {

                    long time = resultData.getLong(Intent.EXTRA_REFERRER);
                    if (time != timeStamp) {
                        isReloaded = true;
                        mScrollViewRoot.removeAllViews();

                        if (mCategoryList != null){
                            mCategoryList.clear();
                            mCategoryList = null;
                        }
                        return;
                    }

                    isReloaded = false;
                    mScrollViewRoot.removeAllViews();

                    mCategoryList = (ArrayList<String>) resultData
                            .getSerializable(GetCategories.class.getSimpleName());
					
					if (mCategoryList != null && mCategoryList.size() > 0){
							mCategoryCount = mCategoryList.size();
							mCategoryIndex = 0;
							String category = mCategoryList.get(mCategoryIndex);
							getFilllScrollViewByCategoryId(category);
							
							mScrollStartY = 0;// start scroll from top
					} else{
						mCategoryCount = 0;
						mCategoryIndex = 0;
						Toast.makeText(getActivity(), R.string.network_timeout, Toast.LENGTH_SHORT).show();
					}
                } else if (serviceKey.equalsIgnoreCase(GetTotalInCategory.class
                        .getSimpleName())){

                    Method key = new Gson().fromJson(resultData.getString(Intent.EXTRA_UID),Method.class);

                    if (!key.getChannelID().equals(mChannelId))
                        return;

                    String category = key.getCategories()[0];
                    String result = (String) resultData.getSerializable(GetTotalInCategory.class.getSimpleName());
                    if (!mCategoryTotalSizeMap.containsKey(category) && result != null){
                        mCategoryTotalSizeMap.put(category,result);
                    }

				} else if (serviceKey.equalsIgnoreCase(GetProgramInCategoryByPage.class
						.getSimpleName())){

					Method key = new Gson().fromJson(resultData.getString(Intent.EXTRA_UID),Method.class);

					if (!key.getChannelID().equals(mChannelId)){
						isReloaded = true;
						return;
					} else {
						isReloaded = false;
					}

                    String category = key.getCategories()[0];
					
					// load finished
					mProgress.setVisibility(View.INVISIBLE);
					
					ArrayList<ProgramSimple> mmItemList = (ArrayList<ProgramSimple>) resultData
                            .getSerializable(GetProgramInCategoryByPage.class.getSimpleName());

					if (mmItemList != null && mmItemList.size() > 0){
                        CardsAdapter adapter = mAdapterMap.get(category);
                        // construct horizontal list view only on first time.
                        if (adapter == null){

                            fill(category, mmItemList);
                            // get next
                            if ((mCategoryIndex + 1) < mScrollViewDistance)
                                getNextCategory();

                        } else {
                            for (ProgramSimple ps:mmItemList){
                                adapter.add(ps);
                            }
                        }

                    }

				} else if (serviceKey.equalsIgnoreCase(GetTotalOfSpecialChannels.class
                        .getSimpleName())) {

                    mTotalOfSpecailChannels = Integer.parseInt((String) resultData.getSerializable(GetTotalOfSpecialChannels.class.getSimpleName()));
                    if (mTotalOfSpecailChannels > 0){
                        if (mSpecialChannelList == null)
                            mSpecialChannelList = new ArrayList<Channel>();

                        mSpecialChannelList.clear();
                        mSpecialChannelPageIndex = 1;

                        Intent intent = new Intent(Intent.ACTION_SYNC, null, mContextActivity,
                                GetSpecialChannelsByPage.class);
                        Method method = new Method();
                        method.setName(DoudianService.GET_SPECIAL_CHANNELS_BY_PAGE);
                        method.setPageIndex(mSpecialChannelPageIndex);
                        intent.putExtra(Intent.EXTRA_TEXT,GetSpecialChannelsByPage.class.getSimpleName());
                        intent.putExtra(Intent.EXTRA_UID, new Gson().toJson(method));
                        intent.putExtra(SyncService.EXTRA_STATUS_RECEIVER, mReceiver);
                        timeStamp = System.currentTimeMillis();
                        intent.putExtra(Intent.EXTRA_REFERRER,timeStamp);
                        mContextActivity.startService(intent);
                    }
                } else if (serviceKey.equalsIgnoreCase(GetSpecialChannelsByPage.class
                        .getSimpleName())) {

                    ArrayList<Channel> mDataList = (ArrayList<Channel>) resultData.getSerializable(GetSpecialChannelsByPage.class.getSimpleName());

                    if (mDataList != null && mDataList.size() > 0) {

                        if (mSpecialChannelList == null)
                            mSpecialChannelList = new ArrayList<Channel>();

                        for (int i = 0; i < mDataList.size(); i++) {
                            mSpecialChannelList.add(mDataList.get(i));
                        }
                        mDataList.clear();
                        mDataList = null;
                    }

                    if (mSpecialChannelList.size() < mTotalOfSpecailChannels){
                        // get next page
                        mSpecialChannelPageIndex++;
                        Intent intent = new Intent(Intent.ACTION_SYNC, null, getActivity(),
                                GetSpecialChannelsByPage.class);
                        Method method = new Method();
                        method.setName(DoudianService.GET_SPECIAL_CHANNELS_BY_PAGE);
                        method.setPageIndex(mSpecialChannelPageIndex);
                        intent.putExtra(Intent.EXTRA_TEXT,GetSpecialChannelsByPage.class.getSimpleName());
                        intent.putExtra(Intent.EXTRA_UID, new Gson().toJson(method));
                        intent.putExtra(SyncService.EXTRA_STATUS_RECEIVER, mReceiver);
                        timeStamp = System.currentTimeMillis();
                        intent.putExtra(Intent.EXTRA_REFERRER,timeStamp);
                        getActivity().startService(intent);

                        return;
                    }

                    if (mSpecialChannelList != null && mSpecialChannelList.size() > 0){
                        // handle special channel as category
                        // start get program by page

                        isReloaded = false;
                        mScrollViewRoot.removeAllViews();

                        if (mCategoryList == null)
                            mCategoryList = new ArrayList<String>();

                        mCategoryList.clear();
                        for (Channel channel:mSpecialChannelList){
                            mCategoryList.add(channel.getName());
                        }

                        if (mCategoryList != null && mCategoryList.size() > 0){
                            mCategoryCount = mCategoryList.size();
                            mCategoryIndex = 0;
                            String category = mCategoryList.get(mCategoryIndex);
                            getFilllScrollViewByCategoryId(category);

                            mScrollStartY = 0;// start scroll from top
                        } else{
                            mCategoryCount = 0;
                            mCategoryIndex = 0;
                            Toast.makeText(getActivity(), R.string.network_timeout, Toast.LENGTH_SHORT).show();
                        }
                    }
                } else if (serviceKey.equalsIgnoreCase(GetTotal.class.getSimpleName())){

                    Method key = new Gson().fromJson(resultData.getString(Intent.EXTRA_UID),Method.class);

                    String category = getChannelNameById(key.getChannelID());
                    String result = (String) resultData.getSerializable(GetTotal.class.getSimpleName());
                    if (!mCategoryTotalSizeMap.containsKey(category)){
                        mCategoryTotalSizeMap.put(category,result);
                    }
                } else if (serviceKey.equalsIgnoreCase(GetProgramByPage.class.getSimpleName())){

                    if(!mChannelId.equals(HomeBlueActivity.RECOMMEND_TAB))
                        return;

                    Method key = new Gson().fromJson(resultData.getString(Intent.EXTRA_UID),Method.class);

                    String category = getChannelNameById(key.getChannelID());

                    // load finished
                    mProgress.setVisibility(View.INVISIBLE);

                    ArrayList<ProgramSimple> mmItemList = (ArrayList<ProgramSimple>) resultData
                            .getSerializable(GetProgramByPage.class.getSimpleName());

                    if (mmItemList != null && mmItemList.size() > 0){
                        CardsAdapter adapter = mAdapterMap.get(category);
                        // construct horizontal list view only on first time.
                        if (adapter == null){

                            fill(category, mmItemList);
                            // get next
                            if ((mCategoryIndex + 1) < mScrollViewDistance)
                                getNextCategory();

                        } else {
                            for (ProgramSimple ps:mmItemList){
                                adapter.add(ps);
                            }
                        }

                    }

                }

				break;
			}
			case SyncService.STATUS_ERROR: {
				if (!isVisible()) break;
				// Error happened down in SyncService, show as toast.
//				final String errorText = getString(R.string.app_name,
//						resultData.getString(Intent.EXTRA_TEXT));
				mProgress.setVisibility(View.INVISIBLE);
				Toast.makeText(mContextActivity, getString(R.string.network_timeout), Toast.LENGTH_SHORT).show();

				break;
			}

		}
	}
	
	private class CardsAdapter extends ArrayAdapter {

		private Context mContext;
		private int resId1;
		private int flag; // 1:videoinfo, 0:videoindex
//		private LruCache<Integer, Bitmap> mMemoryCache;
        private Object tag;

        public void setTag(Object tag){
            this.tag = tag;
        }

        public Object getTag(){
            return this.tag;
        }

		public CardsAdapter(Context context, int itemResourceId, int _flag) {
			mContext = context;
			resId1 = itemResourceId;
			flag = _flag;

			/*final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

			// Use 1/8th of the available memory for this memory cache.
			final int cacheSize = maxMemory;
			mMemoryCache = new LruCache<Integer, Bitmap>(cacheSize) {
				@Override
				protected int sizeOf(Integer key, Bitmap bitmap) {
					// The cache size will be measured in kilobytes rather than
					// number of items.
					return bitmap.getRowBytes() * bitmap.getHeight() / 1024;
				}
			};*/
			
		}
				
		@Override
		public int getItemViewType( int position ) {
//			if ( position % 2 == 1 ) {
//				return 1;
//			}
			return 0;
		}

		@Override
		public int getViewTypeCount() {
			return 1;
		}

		@Override
		public View getView(final int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder=null;
			View view = convertView;
			
			if (view == null) {
//				view = LayoutInflater.from( mContext ).inflate( type == 0 ? resId1 : resId2, parent, false );
				view = LayoutInflater.from( mContext ).inflate(resId1, parent, false );
				viewHolder = new ViewHolder();
				viewHolder.textView = (TextView) view.findViewById(R.id.card_text);
				viewHolder.updateView = (TextView) view.findViewById(R.id.card_update);
				viewHolder.imageView = (ImageView) view.findViewById(R.id.card_image);
				view.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) view.getTag();
			}

            if (!(getItem(position) instanceof ProgramSimple))
                return view;

            final ProgramSimple video = (ProgramSimple) getItem(position);

            if (viewHolder != null && video != null){
                viewHolder.textView.setText(video.getName());
                if (video.getUpdateStatus() != null && video.getUpdateStatus().endsWith("f")){
                    viewHolder.updateView.setText("全 "+video.getUpdateStatus().substring(0,video.getUpdateStatus().length()-1)+" 集");
                    viewHolder.updateView.setBackgroundResource(android.R.color.black);
                    viewHolder.updateView.setVisibility(View.VISIBLE);
                } else if (video.getUpdateStatus() != null && Integer.parseInt(video.getUpdateStatus()) > 0){
                    viewHolder.updateView.setText("更新至第"+video.getUpdateStatus()+"集");
                    viewHolder.updateView.setBackgroundResource(android.R.color.black);
                    viewHolder.updateView.setVisibility(View.VISIBLE);
                } else {
                    viewHolder.updateView.setText(null);
                    viewHolder.updateView.setVisibility(View.INVISIBLE);
                }
                if (video.getPoster() != null)
                    imageDownloaderLruCache.download(video.getPoster(), viewHolder.imageView, null);
                else if (video.getHashCode() == 0){
                    if (video.getChannelId().equals(MYFAVORITE_TAG)){
                        viewHolder.imageView.setImageResource(R.drawable.favorite_default);
                    } else if(video.getChannelId().equals(RECENT_TAG)){
                        viewHolder.imageView.setImageResource(R.drawable.latest_default);
                    }
                }

            }

//				view.setBackgroundResource(android.R.drawable.btn_default);

            view.setOnClickListener(new View.OnClickListener(){

                @Override
                public void onClick(View v) {
                    if (video.getHashCode() != 0){
                        Intent intent = new Intent(mContextActivity, DetailActivity.class);
                        intent.putExtra(Intent.EXTRA_UID, new Gson().toJson(video));
                        ((Activity) mContextActivity).startActivityForResult(intent, 1);
                    }
                }
            });



            if (mChannelId.equals(HomeBlueActivity.MYDOUDIAN_TAB)){
                view.setOnKeyListener(new View.OnKeyListener() {

                    @Override
                    public boolean onKey(final View v, int keyCode, KeyEvent event) {
                        if (event.getAction() == KeyEvent.ACTION_DOWN
                                && keyCode == KeyEvent.KEYCODE_MENU
                                && video.getHashCode() != 0){

                            CustomDialogFragment dialog = (CustomDialogFragment) fm.findFragmentByTag(CustomDialogFragment.TAG);
                            if (dialog == null){
                                dialog = new CustomDialogFragment(fm,null);
                            }

                            dialog.setOnItemDeleteListener(new OnItemDeleteListener() {

                                @Override
                                public void delete() {
                                    remove(position);

                                    if (tag.equals(MYFAVORITE_TAG))
                                        databaseHandler.deleteFavorite(video);
                                    else {
                                        Recent recent = new Recent();
                                        recent.setHashCode(video.getHashCode());
                                        recent.setName(video.getName());
                                        databaseHandler.deleteRecent(recent);
                                    }

                                    mScrollView.scrollTo(0, 0); // scroll to top
                                    getActivity().findViewById(mCurrentTabViewId).requestFocus(); // move focus up
                                    mFocusMask.setVisibility(View.INVISIBLE);
                                    switchActions.run();
                                    CustomDialogFragment dialog = (CustomDialogFragment) fm.findFragmentByTag(CustomDialogFragment.TAG);
                                    if (dialog!=null)
                                        dialog.dismiss();

                                }
                            });
                            dialog.setOnAllDeleteListener(new OnAllDeleteListener() {

                                @Override
                                public void delete() {
                                    if (tag.equals(MYFAVORITE_TAG))
                                        databaseHandler.deleteAllFavorites();
                                    else {
                                        databaseHandler.deleteAllRecent();
                                    }

                                    mScrollView.scrollTo(0, 0); // scroll to top
                                    getActivity().findViewById(mCurrentTabViewId).requestFocus(); // move focus up
                                    mFocusMask.setVisibility(View.INVISIBLE);
                                    switchActions.run();
                                    CustomDialogFragment dialog = (CustomDialogFragment) fm.findFragmentByTag(CustomDialogFragment.TAG);
                                    if (dialog!=null)
                                        dialog.dismiss();
                                }
                            });

                            dialog.showDialog(CustomDialogFragment.TAG);
                        }
                        return false;
                    }
                });
            }
			
			return view;
		}
/*
		private void addBitmapToMemoryCache(int key, Bitmap bitmap) {
			if (getBitmapFromMemCache(key) == null) {
				mMemoryCache.put(key, bitmap);
			}
		}

		private Bitmap getBitmapFromMemCache(int key) {
			return mMemoryCache.get(key);
		}
*/
		private class ViewHolder {
			TextView textView;
			ImageView imageView;
			TextView updateView;
		}


	}
	
	private void fill(String category, Collection itemList){
		if (isReloaded) {
			mScrollViewRoot.removeAllViews();
			return;
		}
		
		if (itemList == null || itemList.isEmpty())
			return;

		if (mCategoryList == null)
			return;

        // to ensure the category is in this Channel
		int indexInCategoryList = -1;
		for (int i = 0; i < mCategoryList.size(); i++) {
			String c = mCategoryList.get(i);
			if (c!=null && c.equals(category)){
				indexInCategoryList = i;
				break;
			}
		}
		
		// start new channel
		if (indexInCategoryList < 0){
			mScrollViewRoot.removeAllViews();
            return;
		}

//		if (!isVisible()) return;
		
		View item = ((Activity)mContextActivity).getLayoutInflater()
				.inflate(R.layout.category_list_item, mScrollViewRoot,false);
		TextView text = (TextView) item.findViewById(R.id.categoryname);
		int titleHeight = text.getLayoutParams().height;
		final TextView counter = (TextView) item.findViewById(R.id.categorycounter);
        counter.setTag(category);
		int flag = 0;
		if (mChannelId.equals(HomeBlueActivity.MYDOUDIAN_TAB))
			flag = 1;
		CardsAdapter adapter = new CardsAdapter(getActivity(),R.layout.horizontal_list_item,flag);
		final HorizontalVariableListView hListView = (HorizontalVariableListView) item.findViewById(R.id.categoryhorizontallist);
        hListView.setAutoAppendData(true);
        adapter.setTag(category);
        hListView.setTag(category);
        if (!mAdapterMap.containsKey(category)){
            mAdapterMap.put(category,adapter);
        }
		
//		mDividerWidth = (mScrollView.getWidth()- mScrollView.getPaddingLeft() - mScrollView.getPaddingRight()) / (mCountInLine * (mRatioItemToDivider+1) - 1);
//		final int itemHeight = (mScrollView.getHeight() - mScrollView.getPaddingTop() - mScrollView.getPaddingBottom() - 3 * titleHeight - 20) / 2;
//        int itemHeight = 274;
//        int mScrollViewWidth = 1280;
//        int mScrollViewWidth = mScrollView.getMeasuredWidth();

        int itemHeight;
        int mScrollViewWidth;
        if (UIDimensionHolder.getInstance().getItemHeight() > 0)
            itemHeight = UIDimensionHolder.getInstance().getItemHeight();
        else {
            itemHeight = (mScrollView.getHeight() - mScrollView.getPaddingTop() - mScrollView.getPaddingBottom() - 3 * titleHeight - 20) / 2;
            UIDimensionHolder.getInstance().setItemHeight(itemHeight);
        }
        if (UIDimensionHolder.getInstance().getScrollViewWidth() > 0)
            mScrollViewWidth = UIDimensionHolder.getInstance().getScrollViewWidth();
        else {
            mScrollViewWidth = mScrollView.getMeasuredWidth();
            UIDimensionHolder.getInstance().setScrollViewWidth(mScrollViewWidth);
        }

//		mDividerWidth = (int) (((mScrollView.getWidth()- mScrollView.getPaddingLeft() - mScrollView.getPaddingRight()) - mRatioItemToDivider*itemHeight*mCountInLine)/(mCountInLine - 1));
		mDividerWidth = (int) (((mScrollViewWidth-mHorizontalListViewHorizontalMargin * 2 - mScrollView.getPaddingLeft() - mScrollView.getPaddingRight()) - mRatioItemToDivider*itemHeight*mCountInLine)/(mCountInLine - 1));
		
//		imageDownloaderLruCache.setExitTasksEarly(false);
		hListView.setAdapter(adapter);
		adapter.addAll(itemList);
		
		text.setText(category);
//		counter.setText(String.valueOf(itemList.size()));
		
		// set horizontal listview
		RelativeLayout.LayoutParams lp = (android.widget.RelativeLayout.LayoutParams) hListView.getLayoutParams();
		lp.height = itemHeight;
		lp.width = mScrollViewWidth-mHorizontalListViewHorizontalMargin * 2;
		lp.setMargins(mHorizontalListViewHorizontalMargin, 0, mHorizontalListViewHorizontalMargin, 0);
		hListView.setLayoutParams(lp);
		hListView.setSize(lp.width - mScrollView.getPaddingLeft() - mScrollView.getPaddingRight(), 
				itemHeight,
				mCountInLine,
				mRatioItemToDivider);
		
		// set focus mask imageview
		RelativeLayout.LayoutParams focusMaskLp = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
//		int listItemWidth = (int) (mRatioItemToDivider * itemHeight);
		int leftMargin = 0;
		if (getActivity() instanceof RecommendActivity)
			leftMargin	= (int)(mScrollViewWidth/2 - (mRatioItemToDivider * itemHeight)/2 - 0.5);
		else
			leftMargin	= (int)(mScrollViewWidth/2 - (mRatioItemToDivider * itemHeight)/2);
		focusMaskLp.setMargins(leftMargin - 11, titleHeight - 3, 0, 0);
		focusMaskLp.width = (int) (mRatioItemToDivider * itemHeight + 20);
		focusMaskLp.height =  lp.height + 18;
		mFocusMask.setLayoutParams(focusMaskLp);
		mFocusMask.requestLayout();

		item.setTag(indexInCategoryList);

		hListView.setOnItemSelectedListener(new OnItemSelectedListener(){

			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int position, long arg3) {

                if (Integer.parseInt(mCategoryTotalSizeMap.get(counter.getTag())) == 0)
                    counter.setText("");
                else
				    counter.setText( String.valueOf(position + 1) + "/" + mCategoryTotalSizeMap.get(counter.getTag()));

				mFocusMask.setVisibility(View.VISIBLE);

                int total;
                try{
                    total = Integer.parseInt(mCategoryTotalSizeMap.get(counter.getTag()));
                } catch (NumberFormatException e){
                    total = 0;
                }
                int currentTotal = hListView.getAdapter().getCount();
                if ( currentTotal - position == 10 && currentTotal != total){
                    // get next page data
                    int pageIndex = currentTotal / PAGESIZE + 1;

                    if(mChannelId.equals(HomeBlueActivity.RECOMMEND_TAB)) {
                        Intent intent = new Intent(Intent.ACTION_SYNC, null, getActivity(),
                                GetProgramByPage.class);
                        intent.putExtra(Intent.EXTRA_TEXT,GetProgramByPage.class.getSimpleName());
                        Method method = new Method();
                        method.setName(DoudianService.GET_PROGRAM_BY_PAGE);
                        method.setChannelID(getChannelIdByName((String) hListView.getTag()));
                        method.setPageIndex(pageIndex);
                        intent.putExtra(Intent.EXTRA_UID, new Gson().toJson(method));
                        intent.putExtra(SyncService.EXTRA_STATUS_RECEIVER, mReceiver);
                        timeStamp = System.currentTimeMillis();
                        intent.putExtra(Intent.EXTRA_REFERRER,timeStamp);
                        getActivity().startService(intent);


                    } else if(mChannelId.equals(HomeBlueActivity.MYDOUDIAN_TAB)) {
                        // do nothing
                    } else {
                        Intent intent = new Intent(Intent.ACTION_SYNC, null, getActivity(),
                                GetProgramInCategoryByPage.class);
                        intent.putExtra(Intent.EXTRA_TEXT,GetProgramInCategoryByPage.class.getSimpleName());
                        Method method = new Method();
                        method.setName(DoudianService.GET_PROGRAM_IN_CATEGORY_BY_PAGE);
                        method.setChannelID(mChannelId);
                        String[] categories = new String[1];
                        categories[0] = (String) hListView.getTag();
                        method.setCategories(categories);
                        method.setPageIndex(pageIndex);
                        intent.putExtra(Intent.EXTRA_UID, new Gson().toJson(method));
                        intent.putExtra(SyncService.EXTRA_STATUS_RECEIVER, mReceiver);
                        timeStamp = System.currentTimeMillis();
                        intent.putExtra(Intent.EXTRA_REFERRER,timeStamp);
                        getActivity().startService(intent);
                    }
                }
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				if (!(getActivity() instanceof RecommendActivity)){
                    if (Integer.parseInt(mCategoryTotalSizeMap.get(counter.getTag())) == 0)
                        counter.setText("");
                    else
					    counter.setText(mCategoryTotalSizeMap.get(counter.getTag()));

					mFocusMask.setVisibility(View.INVISIBLE);
				}
			}});
		hListView.setOnUpandDownListener(new OnUpAndDownListener() {
			
			@Override
			public boolean go(boolean upOrDown) {

				// true is up;false is down
				if (mScrollView.getScrollY() > 0
						|| !(getActivity() instanceof RecommendActivity) ){
                    if (Integer.parseInt(mCategoryTotalSizeMap.get(counter.getTag())) == 0)
                        counter.setText("");
                    else
					    counter.setText(mCategoryTotalSizeMap.get(counter.getTag()));
				}
				
				final int itemHeight = mScrollViewRoot.getChildAt(0).getHeight();
				
				/*if (upOrDown){
					// up
					mScrollView.smoothScrollBy(0, -itemHeight);
				}
				else{
					// down
					mScrollView.smoothScrollBy(0, itemHeight);
				}*/
				
				mScrollView.post(new Runnable() {
			        public void run() {
			        	View v = mScrollViewRoot.getFocusedChild();
			        	if (v != null){
			        		int targetY =  ((Integer) v.getTag()) * itemHeight;
			        		if (targetY !=mScrollView.getScrollY()){
			        			mScrollView.smoothScrollTo(0, targetY);
			        			getFocusedHLV();
			        		}
			        	}
			        } 
				});
				
				// get next
				if (!upOrDown)
					getNextCategory();

                return true;
			}

		});
		// give the status of this line
		if (indexInCategoryList == 0 && mCurrentTabViewId !=0){
			hListView.setUpAndDownFocus(true, mCurrentTabViewId, false, 0, true); // the first one
//			if (mCategoryList.size() == 1){
//				// only one category
//				hListView.setUpAndDownFocus(true, mCurrentTabViewId, false, 0, true); // it is the last one also
//			}
		} else if (indexInCategoryList == 0 && mCurrentTabViewId == 0) 
			hListView.setUpAndDownFocus(false, 0, false, 0, true); // first one in Recommend Activity
		else
			hListView.setUpAndDownFocus(true, 0, false, 0, false); //set me as the last one firstly
			
		
		hListView.setFixPosition(true);
		
//		mScrollViewRoot.addView(item);
		if (mScrollViewRoot.getChildCount() > 0)
			mScrollViewRoot.addView(item, mScrollViewRoot.getChildCount()-1);
		else {
			mScrollViewRoot.removeAllViews();
			mScrollViewRoot.addView(item, 0);
			// addRecent last fake item
			View fakeItem = ((Activity)mContextActivity).getLayoutInflater()
					.inflate(R.layout.category_list_item_fake, mScrollViewRoot,false);
			LayoutParams flp = fakeItem.getLayoutParams();
//			mCategoryBlankItemHeight = mScrollView.getHeight() - mScrollViewRoot.getChildAt(0).getHeight();
//            mCategoryBlankItemHeight=659;
            int mCategoryBlankItemHeight;
            if (UIDimensionHolder.getInstance().getCategoryBlankItemHeight() > 0)
                mCategoryBlankItemHeight = UIDimensionHolder.getInstance().getCategoryBlankItemHeight();
            else{
                mCategoryBlankItemHeight = mScrollView.getHeight() - mScrollViewRoot.getChildAt(0).getHeight();
                UIDimensionHolder.getInstance().setCategoryBlankItemHeight(mCategoryBlankItemHeight);
            }

			flp.height = mCategoryBlankItemHeight;
			fakeItem.setLayoutParams(flp);
			fakeItem.setFocusable(false);
			fakeItem.setFocusableInTouchMode(false);
			mScrollViewRoot.addView(fakeItem);
		}
		
		// after addRecent reset focus status between 0 and last item
		if (indexInCategoryList > 0){
			View v = mScrollViewRoot.getChildAt(indexInCategoryList-1);
			if (v != null){
				HorizontalVariableListView hlv = (HorizontalVariableListView) v.findViewById(R.id.categoryhorizontallist);
				if (hlv != null){
					if (indexInCategoryList == 1)
						hlv.setUpAndDownFocus(true, mCurrentTabViewId, true, 0, true);
					else
						hlv.setUpAndDownFocus(true, 0, true, 0, false);
				}
			}
		}
		
		// request focus on key down on current tab view
//		if (indexInCategoryList == 0 && isVisible() && getActivity() != null){
        if (indexInCategoryList == 0 && getActivity() != null){
			View v = getActivity().findViewById(mCurrentTabViewId);

			if (v != null){
				v.setOnKeyListener(new OnKeyListener() {
					
					@Override
					public boolean onKey(View v, int keyCode, KeyEvent event) {
						if (event.getAction() == KeyEvent.ACTION_DOWN
								&& keyCode == KeyEvent.KEYCODE_DPAD_DOWN){
							
							View item = mScrollViewRoot.getChildAt(0);
							if (item != null){
								HorizontalVariableListView hlv = (HorizontalVariableListView) item.findViewById(R.id.categoryhorizontallist);
								if (hlv != null) {
									hlv.centerChildRequestFocus();
								
									// get next
									getNextCategory();
								}
							}
							
							return true;
						}
						return false;
					}
				});
			}
		}
		
		// set focus in recommend activity
		if (getActivity() instanceof RecommendActivity
				&& mCategoryList.size() > 0
				&& indexInCategoryList == 0){
			mScrollView.postDelayed(new Runnable() {
				public void run() {
					hListView.requestFocus();
					/*int i= hListView.getChildAtPosition( (float)mScrollView.getWidth()/2, mScrollView.getY()+itemHeight/2 );
					if (i>=0){
						View child = hListView.getChildAt(i);
						if (child != null)
							child.requestFocus();
					}*/
					hListView.centerChildRequestFocus();
		        } 
			},150);
		}
		
		itemList.clear();
		itemList = null;
	}
	
	private void getFilllScrollViewByCategoryId(String category){

        if (getActivity() == null)
            return;
		
		if (category == null)
			return;

        if(mChannelId.equals(HomeBlueActivity.RECOMMEND_TAB)) {
            Intent i = new Intent(Intent.ACTION_SYNC, null, getActivity(),
                    GetTotal.class);
            i.putExtra(Intent.EXTRA_TEXT,GetTotal.class.getSimpleName());
            Method m = new Method();
            m.setName(DoudianService.GET_TOTAL);
            m.setChannelID(getChannelIdByName(category));
            i.putExtra(Intent.EXTRA_UID, new Gson().toJson(m));
            i.putExtra(SyncService.EXTRA_STATUS_RECEIVER, mReceiver);
            getActivity().startService(i);

            Intent intent = new Intent(Intent.ACTION_SYNC, null, getActivity(),
                    GetProgramByPage.class);
            intent.putExtra(Intent.EXTRA_TEXT,GetProgramByPage.class.getSimpleName());
            Method method = new Method();
            method.setName(DoudianService.GET_PROGRAM_BY_PAGE);
            method.setChannelID(getChannelIdByName(category));
            method.setPageIndex(1);
            intent.putExtra(Intent.EXTRA_UID, new Gson().toJson(method));
            intent.putExtra(SyncService.EXTRA_STATUS_RECEIVER, mReceiver);
            timeStamp = System.currentTimeMillis();
            intent.putExtra(Intent.EXTRA_REFERRER,timeStamp);
            getActivity().startService(intent);
        } else {
            Intent i = new Intent(Intent.ACTION_SYNC, null, getActivity(),
                    GetTotalInCategory.class);
            i.putExtra(Intent.EXTRA_TEXT,GetTotalInCategory.class.getSimpleName());
            Method m = new Method();
            m.setName(DoudianService.GET_TOTAL_IN_CATEGORY);
            m.setChannelID(mChannelId);
            String[] categories = new String[1];
            categories[0] = category;
            m.setCategories(categories);
            i.putExtra(Intent.EXTRA_UID, new Gson().toJson(m));
            i.putExtra(SyncService.EXTRA_STATUS_RECEIVER, mReceiver);
            getActivity().startService(i);

            Intent intent = new Intent(Intent.ACTION_SYNC, null, getActivity(),
                    GetProgramInCategoryByPage.class);
            intent.putExtra(Intent.EXTRA_TEXT,GetProgramInCategoryByPage.class.getSimpleName());
            Method method = new Method();
            method.setName(DoudianService.GET_PROGRAM_IN_CATEGORY_BY_PAGE);
            method.setChannelID(mChannelId);
            method.setCategories(categories);
            method.setPageIndex(1);
            intent.putExtra(Intent.EXTRA_UID, new Gson().toJson(method));
            intent.putExtra(SyncService.EXTRA_STATUS_RECEIVER, mReceiver);
            timeStamp = System.currentTimeMillis();
            intent.putExtra(Intent.EXTRA_REFERRER,timeStamp);
            getActivity().startService(intent);
        }
	}
	
	private class SwitchActions implements Runnable{
		
		private int currentTabViewId;
		private long startTime;

		@Override
		public void run() {
			while (System.currentTimeMillis() - startTime < 200){
			}
			
			isReloaded = true;
			mScrollViewRoot.removeAllViews();
			
			mCurrentTabViewId = currentTabViewId;
			if (mChannelId.equals(HomeBlueActivity.MYDOUDIAN_TAB)){
                isReloaded = false;
                if (mCategoryList == null)
                    mCategoryList = new ArrayList<String>();

                mCategoryList.clear();
                mCategoryList.add(RECENT_TAG);
                mCategoryList.add(MYFAVORITE_TAG);

                mAdapterMap.clear();
                mCategoryTotalSizeMap.clear();

                mCategoryCount = mCategoryList.size();
                mCategoryIndex = 0;
                if (!mCategoryTotalSizeMap.containsKey(mCategoryList.get(mCategoryIndex)))
                    mCategoryTotalSizeMap.put(mCategoryList.get(mCategoryIndex),String.valueOf(databaseHandler.getRecentsCount()));
                Collection recents = databaseHandler.getAllRecents();
                if (recents.size() == 0){
                    ProgramSimple fakeItem = new ProgramSimple();
                    fakeItem.setHashCode(0);
                    fakeItem.setName(getString(R.string.no_recent));
                    fakeItem.setUpdateStatus(null);
                    fakeItem.setPoster(null);
                    fakeItem.setChannelId(RECENT_TAG);
                    recents.add(fakeItem);
                }
                fill(mCategoryList.get(mCategoryIndex), recents);

                mCategoryIndex = 1;
                if (!mCategoryTotalSizeMap.containsKey(mCategoryList.get(mCategoryIndex)))
                    mCategoryTotalSizeMap.put(mCategoryList.get(mCategoryIndex),String.valueOf(databaseHandler.getFavoritesCount()));
                Collection favorites = databaseHandler.getAllFavorites();
                if (favorites.size() == 0){
                    ProgramSimple fakeItem = new ProgramSimple();
                    fakeItem.setHashCode(0);
                    fakeItem.setName(getString(R.string.no_favorite));
                    fakeItem.setUpdateStatus(null);
                    fakeItem.setPoster(null);
                    fakeItem.setChannelId(MYFAVORITE_TAG);
                    favorites.add(fakeItem);
                }
                fill(mCategoryList.get(mCategoryIndex), favorites);

			} else if(mChannelId.equals(HomeBlueActivity.RECOMMEND_TAB)){
                Intent intent = new Intent(Intent.ACTION_SYNC, null, getActivity(),
                        GetTotalOfSpecialChannels.class);
                Method method = new Method();
                method.setName(DoudianService.GET_TOTAL_OF_SPECIAL_CHANNELS);
                intent.putExtra(Intent.EXTRA_TEXT,GetTotalOfSpecialChannels.class.getSimpleName());
                intent.putExtra(Intent.EXTRA_UID, new Gson().toJson(method));
                intent.putExtra(SyncService.EXTRA_STATUS_RECEIVER, mReceiver);
                timeStamp = System.currentTimeMillis();
                intent.putExtra(Intent.EXTRA_REFERRER,timeStamp);
                getActivity().startService(intent);
			} else if (mCurrentTabViewId > 1003) {
                // main channel start from1004
                Intent intent = new Intent(Intent.ACTION_SYNC, null, getActivity(),
                        GetCategories.class);
                intent.putExtra(Intent.EXTRA_TEXT,GetCategories.class.getSimpleName());
                Method method = new Method();
                method.setName(DoudianService.GET_CATEGORIES);
                method.setChannelID(mChannelId);
                intent.putExtra(Intent.EXTRA_UID, new Gson().toJson(method));
                intent.putExtra(SyncService.EXTRA_STATUS_RECEIVER, mReceiver);
                timeStamp = System.currentTimeMillis();
                intent.putExtra(Intent.EXTRA_REFERRER,timeStamp);
                getActivity().startService(intent);
			}

            // do not move focus down until category showed
            View v = getActivity().findViewById(mCurrentTabViewId);
            v.setNextFocusDownId(mCurrentTabViewId);
		}
		
		public void setId(String id){
			if (!id.equals(mChannelId))
				startTime = System.currentTimeMillis();
			
			mChannelId = id;
		}
		
		public void setCurrentTabViewId(int id){
			currentTabViewId = id;
		}
		
	}
	
	public void clear(){
		if (mScrollView != null)
			mScrollView.removeAllViews();
		
		if (mCategoryList != null){
			mCategoryList.clear();
			mCategoryList = null;
		}
	}
	
	private void getNextCategory(){
		if (mCategoryList != null 
				&& mCategoryIndex < mCategoryList.size() - 1
				&& (mCategoryIndex-1) < SCROLLVIEWMAXCOUNT){
			mCategoryIndex++;
			String category = mCategoryList.get(mCategoryIndex);
			getFilllScrollViewByCategoryId(category);
		}
	}
	
	private void getFocusedHLV(){
		
		for (int i = 0; i < mScrollViewRoot.getChildCount(); i++) {
			View v = mScrollViewRoot.getChildAt(i);
			if (v != null){
				HorizontalVariableListView hListView = (HorizontalVariableListView) v.findViewById(R.id.categoryhorizontallist);
				if (hListView != null){
					int mCenterX = hListView.getLeft() + hListView.getWidth()/2;
					int mCenterY = hListView.getTop() + hListView.getHeight()/2;
					if (mViewRect.contains(mCenterX, mCenterY)){
						hListView.requestFocus();
						hListView.centerChildRequestFocus();
					}
				}
			}
		}
	}

    private String getChannelIdByName(String name){

        for (Channel channel:mSpecialChannelList){
            if (channel.getName().equals(name))
                return channel.getId();
        }

        return null;
    }

    private String getChannelNameById(String id){
        for (Channel channel:mSpecialChannelList){
            if (channel.getId().equals(id))
                return channel.getName();
        }

        return null;
    }
}
