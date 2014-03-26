package cn.com.xinli.android.doudian.ui.tv;

import android.app.TabActivity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;

import java.util.ArrayList;

import cn.com.xinli.android.doudian.R;
import cn.com.xinli.android.doudian.model.Channel;
import cn.com.xinli.android.doudian.model.Method;
import cn.com.xinli.android.doudian.service.DoudianService;
import cn.com.xinli.android.doudian.service.GetNormalChannelsByPage;
import cn.com.xinli.android.doudian.service.GetTotalOfNormalChannels;
import cn.com.xinli.android.doudian.service.SyncService;
import cn.com.xinli.android.doudian.utils.ChannelsHolder;
import cn.com.xinli.android.doudian.utils.DetachableResultReceiver;
import cn.com.xinli.android.doudian.utils.DetachableResultReceiver.Receiver;

@SuppressWarnings("deprecation")
public class HomeBlueActivity extends TabActivity implements Receiver {

    public static final String TAG = "HomeFragment";
    public static final String TABVIEWID = "tabviewid";
    public final static String MYDOUDIAN_TAB = "mydoudian";
    public final static String RECOMMEND_TAB = "recommend";
    private final static String FILTER_TAB = "filter";
    private final static String SEARCH_TAB = "search";
    private final static String MYDOUDIAN_TAB_NAME = "我的逗点";
    private final static String RECOMMEND_TAB_NAME = "推荐";
    private final static String[] mIgnoreChannels = {"24", "25", "26", "27"};
    private static final int PAGESIZE = 20;
    private ViewGroup mRootView;
    private int baseSize;
    private DetachableResultReceiver mReceiver;
    private ArrayList<Channel> mChannelList;
    private TabHost mTabHost;
    private TabWidget mTabWidget;
    private ImageView mTabNavLeft;
    private ImageView mTabNavRight;
    private ImageView mFocusMask;
    private HomeTabFragment mFragment;
    private int mTabIndex = 1000;
    private boolean mBackFromRecommendActivity;
    private int mTotalOfNormalChannels;
    private int mChannelPageIndex;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_home_v4);

        mTabNavLeft = (ImageView) findViewById(R.id.tabnavleft);
        mTabNavRight = (ImageView) findViewById(R.id.tabnavright);
        mTabNavLeft.setVisibility(View.VISIBLE);
        mTabNavRight.setVisibility(View.VISIBLE);

        mReceiver = new DetachableResultReceiver(new Handler());
        mReceiver.setReceiver(this);

        Intent intent = new Intent(Intent.ACTION_SYNC, null, this,
                GetTotalOfNormalChannels.class);
        Method method = new Method();
        method.setName(DoudianService.GET_TOTAL_OF_NORMAL_CHANNELS);
        intent.putExtra(Intent.EXTRA_TEXT, GetTotalOfNormalChannels.class.getSimpleName());
        intent.putExtra(Intent.EXTRA_UID, new Gson().toJson(method));
        intent.putExtra(SyncService.EXTRA_STATUS_RECEIVER, mReceiver);
        startService(intent);

        // set TabWidget width
        mTabWidget = (TabWidget) findViewById(android.R.id.tabs);
//  		LinearLayout.LayoutParams llp2 = new LinearLayout.LayoutParams(baseSize * 5 / 2, LayoutParams.WRAP_CONTENT);
//  		mTabWidget.setLayoutParams(llp2);
//  		mTabWidget.setDividerDrawable(R.drawable.tabwidget_divider);
        mTabWidget.setDividerDrawable(null);

//  		mTabHost = (TabHost)findViewById(android.R.id.tabhost);
//  		mTabHost.setup(this, getSupportFragmentManager(), R.id.realtabcontent);
        mTabHost = getTabHost();

        //filter
        View filterView = buildIndicator(R.string.filter_titile);
        Bundle filterArg = new Bundle();
        filterArg.putInt("tabviewid", filterView.getId());
        mTabHost.addTab(mTabHost.newTabSpec("filter_id")
                .setIndicator(filterView)
                .setContent(android.R.id.tabcontent));
        // search
        View searchView = buildIndicator(R.string.search_titile);
        Bundle searchArg = new Bundle();
        searchArg.putInt("tabviewid", searchView.getId());
        mTabHost.addTab(mTabHost.newTabSpec("search_id")
                .setIndicator(searchView)
                .setContent(android.R.id.tabcontent));
        mTabHost.setVisibility(View.INVISIBLE);

        // 我的逗点
        View myView = buildIndicator(MYDOUDIAN_TAB_NAME);
        Bundle myArg = new Bundle();
        myArg.putInt("tabviewid", myView.getId());
        mTabHost.addTab(mTabHost.newTabSpec(MYDOUDIAN_TAB)
                .setIndicator(myView)
                .setContent(android.R.id.tabcontent));

        // 推荐
        View recoView = buildIndicator(RECOMMEND_TAB_NAME);
        Bundle recoArg = new Bundle();
        recoArg.putInt("tabviewid", recoView.getId());
        mTabHost.addTab(mTabHost.newTabSpec(RECOMMEND_TAB)
                .setIndicator(recoView)
                .setContent(android.R.id.tabcontent));

        mTabHost.setOnTabChangedListener(new OnTabChangeListener() {
            @Override
            public void onTabChanged(String tabId) {
                if (isFinishing())
                    return;

                ShowHideTabNavArrow(tabId);

                if (tabId.equals("search_id")) {

                    if (mFragment != null)
                        getFragmentManager().beginTransaction()
                                .remove(mFragment).commit();

                    FilterFragment filterFragment = (FilterFragment) getFragmentManager()
                            .findFragmentByTag(FILTER_TAB);
                    if (filterFragment != null) {
                        getFragmentManager().beginTransaction()
                                .remove(filterFragment).commit();
                    }

                    SearchFragment searchFragment = (SearchFragment) getFragmentManager()
                            .findFragmentByTag(SEARCH_TAB);
                    if (searchFragment == null) {
                        searchFragment = new SearchFragment();
                        Bundle args = new Bundle();
                        args.putString("id", tabId);
                        args.putInt("tabviewid", mTabHost.getCurrentTabView()
                                .getId());
                        searchFragment.setArguments(args);
                    }

                    getFragmentManager().beginTransaction()
                            .replace(R.id.tabcontainer, searchFragment, SEARCH_TAB)
                            .disallowAddToBackStack()
                            .commit();

                } else if (tabId.equals("filter_id")) {

                    if (mFragment != null)
                        getFragmentManager().beginTransaction()
                                .remove(mFragment).commit();

                    SearchFragment searchFragment = (SearchFragment) getFragmentManager()
                            .findFragmentByTag(SEARCH_TAB);
                    if (searchFragment != null) {
                        getFragmentManager().beginTransaction()
                                .remove(searchFragment).commit();
                    }

                    FilterFragment filterFragment = (FilterFragment) getFragmentManager()
                            .findFragmentByTag(FILTER_TAB);
                    if (filterFragment == null) {
                        filterFragment = new FilterFragment();
                        Bundle args = new Bundle();
                        args.putString("id", tabId);
                        args.putInt("tabviewid", mTabHost.getCurrentTabView()
                                .getId());
                        filterFragment.setArguments(args);
                    }

                    getFragmentManager().beginTransaction()
                            .replace(R.id.tabcontainer, filterFragment, FILTER_TAB)
                            .disallowAddToBackStack()
                            .commit();

                } else {

                    SearchFragment searchFragment = (SearchFragment) getFragmentManager()
                            .findFragmentByTag(SEARCH_TAB);
                    if (searchFragment != null) {
                        getFragmentManager().beginTransaction()
                                .remove(searchFragment).commit();
                    }

                    FilterFragment filterFragment = (FilterFragment) getFragmentManager()
                            .findFragmentByTag(FILTER_TAB);
                    if (filterFragment != null) {
                        getFragmentManager().beginTransaction()
                                .remove(filterFragment).commit();
                    }

                    mFragment = (HomeTabFragment) getFragmentManager()
                            .findFragmentByTag("hori");
                    /*if (mFragment == null) {
						mFragment = new HomeTabFragment();
						Bundle args = new Bundle();
						args.putString("id", tabId);
						args.putInt("tabviewid", mTabHost.getCurrentTabView()
								.getId());
						mFragment.setArguments(args);
						getFragmentManager().beginTransaction()
								.replace(R.id.tabcontainer, mFragment, "hori")
								.disallowAddToBackStack()
								.commit();
					} else {
						mFragment.reload(tabId, mTabHost.getCurrentTabView()
								.getId());
					}*/

                    if (mFragment == null || mBackFromRecommendActivity) {
                        if (mFragment != null) {
                            mFragment.clear();
                            getFragmentManager().beginTransaction()
                                    .remove(mFragment)
                                    .commit();
                        }
                        mFragment = new HomeTabFragment();
                        Bundle args = new Bundle();
                        args.putString("id", tabId);
                        args.putInt("tabviewid", mTabHost.getCurrentTabView()
                                .getId());
                        mFragment.setArguments(args);
                        getFragmentManager().beginTransaction()
                                .replace(R.id.tabcontainer, mFragment, "hori")
                                .disallowAddToBackStack()
                                .commit();
                        mBackFromRecommendActivity = false;
                    } else {
                        mFragment.reload(tabId, mTabHost.getCurrentTabView()
                                .getId());
                    }

                }
            }
        });

    }

    /**
     * Build a {@link View} to be used as a tab indicator, setting the requested string resource as
     * its label.
     */
    private View buildIndicator(int textRes) {
        TextView indicator = (TextView) this.getLayoutInflater().inflate(R.layout.tab_indicator,
                mTabWidget, false);
        indicator.setId(mTabIndex++);
        indicator.setText(textRes);
        indicator.setFocusable(true);
        indicator.setFocusableInTouchMode(true);
        return indicator;
    }

    /**
     * Build a {@link View} to be used as a tab indicator, setting the requested string resource as
     * its label.
     */
    private View buildIndicator(String text) {
        TextView indicator = (TextView) this.getLayoutInflater().inflate(R.layout.tab_indicator,
                mTabWidget, false);
        indicator.setId(mTabIndex++);
        indicator.setText(text);
        indicator.setFocusable(true);
        indicator.setFocusableInTouchMode(true);
        return indicator;
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        Log.d(TAG, "resultCode = " + resultCode);

        switch (resultCode) {
            case SyncService.STATUS_RUNNING: {
                break;
            }
            case SyncService.STATUS_FINISHED: {
                String serviceKey = resultData.getString(Intent.EXTRA_TEXT);
                if (isFinishing()) break;

                if (serviceKey.equalsIgnoreCase(GetTotalOfNormalChannels.class
                        .getSimpleName())) {
                    mTotalOfNormalChannels = Integer.parseInt((String) resultData.getSerializable(GetTotalOfNormalChannels.class.getSimpleName()));
                    if (mTotalOfNormalChannels > 0) {

                        mChannelList = ChannelsHolder.getInstance().getChannelArrayList();
                        if (mChannelList != null && mChannelList.size() > 0)
                            initTabHostData();
                        else {
                            mChannelList = new ArrayList<Channel>();
                            mChannelPageIndex = 1;

                            Intent intent = new Intent(Intent.ACTION_SYNC, null, this,
                                    GetNormalChannelsByPage.class);
                            Method method = new Method();
                            method.setName(DoudianService.GET_Normal_CHANNELS_BY_PAGE);
                            method.setPageIndex(mChannelPageIndex);
                            intent.putExtra(Intent.EXTRA_TEXT, GetNormalChannelsByPage.class.getSimpleName());
                            intent.putExtra(Intent.EXTRA_UID, new Gson().toJson(method));
                            intent.putExtra(SyncService.EXTRA_STATUS_RECEIVER, mReceiver);
                            startService(intent);
                        }
                    }
                } else if (serviceKey.equalsIgnoreCase(GetNormalChannelsByPage.class
                        .getSimpleName())) {
                    ArrayList<Channel> mDataList = (ArrayList<Channel>) resultData.getSerializable(GetNormalChannelsByPage.class.getSimpleName());

                    if (mDataList != null && mDataList.size() > 0) {
                        for (int i = 0; i < mDataList.size(); i++) {
                            mChannelList.add(mDataList.get(i));
                        }
                        mDataList.clear();
                        mDataList = null;
                    }

                    if (mChannelList.size() < mTotalOfNormalChannels) {
                        // get next page
                        mChannelPageIndex++;
                        Intent intent = new Intent(Intent.ACTION_SYNC, null, this,
                                GetNormalChannelsByPage.class);
                        Method method = new Method();
                        method.setName(DoudianService.GET_Normal_CHANNELS_BY_PAGE);
                        method.setPageIndex(mChannelPageIndex);
                        intent.putExtra(Intent.EXTRA_TEXT, GetNormalChannelsByPage.class.getSimpleName());
                        intent.putExtra(Intent.EXTRA_UID, new Gson().toJson(method));
                        intent.putExtra(SyncService.EXTRA_STATUS_RECEIVER, mReceiver);
                        startService(intent);

                        return;
                    }

                    if (mChannelList != null && mChannelList.size() > 0) {

                        if (ChannelsHolder.getInstance().getChannelArrayList() == null)
                            ChannelsHolder.getInstance().setChannelArrayList(mChannelList);

                        initTabHostData();
                    } else {
                        Toast.makeText(this, "初始化数据异常，请稍候再试！", Toast.LENGTH_LONG).show();
                        Log.d(TAG, " data is null");
                    }
                }

                break;
            }
            case SyncService.STATUS_ERROR: {
                if (isFinishing()) break;
                // Error happened down in SyncService, show as toast.
                final String errorText = getString(R.string.app_name,
                        resultData.getString(Intent.EXTRA_TEXT));
                break;
            }

        }

    }

    @Override
    public void onBackPressed() {

        if (mTabHost != null && getCurrentFocus() != null && mTabHost.getCurrentTabView() != null && getCurrentFocus().getId() != mTabHost.getCurrentTabView().getId()) {

            View view = mTabHost.getCurrentTabView();
            if (view != null)
                view.requestFocus();

            mFocusMask = (ImageView) findViewById(R.id.focusmask);
            if (mFocusMask != null)
                mFocusMask.setVisibility(View.INVISIBLE);

            ScrollView scrollView = (ScrollView) findViewById(R.id.scrollview);
            if (scrollView != null)
                scrollView.scrollTo(0, 0);
        } else
            super.onBackPressed();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_SEARCH) {
            mTabHost.setCurrentTab(1);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1 && resultCode == 1 && data != null) {
            // return from DetailActivity
            mTabHost.getCurrentTabView().requestFocus();
            ScrollView scrollView = (ScrollView) findViewById(R.id.scrollview);
            if (scrollView != null) {
                scrollView.scrollTo(0, 0);
                mFragment.reload(mTabHost.getCurrentTabTag(), mTabHost.getCurrentTabView()
                        .getId());
            }
        } else if (requestCode == 2) {
            // return from RecommendActivity
            mBackFromRecommendActivity = true;
        }
    }

    private void ShowHideTabNavArrow(String tabId) {
        int index = mTabWidget.indexOfChild(mTabHost.getCurrentTabView());

        if (index == 0)
            mTabNavLeft.setVisibility(View.INVISIBLE);
        else
            mTabNavLeft.setVisibility(View.VISIBLE);

        if (index == mTabWidget.getChildCount() - 1)
            mTabNavRight.setVisibility(View.INVISIBLE);
        else
            mTabNavRight.setVisibility(View.VISIBLE);
    }

    private void initTabHostData() {
        mTabHost.setVisibility(View.VISIBLE);

        for (int i = 0; i < mChannelList.size(); i++) {
            Channel c = mChannelList.get(i);

            mTabHost.addTab(mTabHost.newTabSpec(c.getId())
                    .setIndicator(buildIndicator(c.getName()))
                    .setContent(android.R.id.tabcontent));
        }

        // for last tab focus move
        mTabWidget.getChildAt(0).setNextFocusLeftId(mTabWidget.getChildAt(0).getId());
        mTabWidget.getChildAt(mTabWidget.getChildCount() - 1).setNextFocusRightId(mTabWidget.getChildAt(mTabWidget.getChildCount() - 1).getId());
        mTabWidget.getChildAt(mTabWidget.getChildCount() - 1).setNextFocusLeftId(mTabWidget.getChildAt(mTabWidget.getChildCount() - 2).getId());

        // 默认在推荐
        mTabHost.setCurrentTab(3);
    }

}