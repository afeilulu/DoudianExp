package cn.com.xinli.android.doudian.ui.tv;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import cn.com.xinli.android.doudian.R;
import cn.com.xinli.android.doudian.database.DatabaseHandler;
import cn.com.xinli.android.doudian.model.Channel;
import cn.com.xinli.android.doudian.model.Episode;
import cn.com.xinli.android.doudian.model.Method;
import cn.com.xinli.android.doudian.model.ProgramDetail;
import cn.com.xinli.android.doudian.model.ProgramSimple;
import cn.com.xinli.android.doudian.model.Recent;
import cn.com.xinli.android.doudian.model.Source;
import cn.com.xinli.android.doudian.service.DoudianService;
import cn.com.xinli.android.doudian.service.GetProgramDetail;
import cn.com.xinli.android.doudian.service.GetProgramEpisodesByPage;
import cn.com.xinli.android.doudian.service.GetProgramRelated;
import cn.com.xinli.android.doudian.service.GetProgramSources;
import cn.com.xinli.android.doudian.service.SyncService;
import cn.com.xinli.android.doudian.ui.widget.HorizontalListView.HorizontalVariableListView;
import cn.com.xinli.android.doudian.ui.widget.HorizontalListViewAdapter;
import cn.com.xinli.android.doudian.utils.ChannelsHolder;
import cn.com.xinli.android.doudian.utils.DetachableResultReceiver;
import cn.com.xinli.android.doudian.utils.ImageDownloaderLruCache;
import cn.com.xinli.android.doudian.utils.ImageObjectCache;
import cn.com.xinli.android.doudian.utils.ImageObjectCache.ImageCacheParams;
import cn.com.xinli.android.doudian.utils.SourceHolder;
import cn.com.xinli.android.doudian.utils.UIUtils;

public class DetailFragment extends Fragment implements
        DetachableResultReceiver.Receiver {
    private static final String TAG = "DetailFragment";
    int curPy = 0;
    int curJm = 0;
    int curIn = 0;
    int hsize = 1;
    private int mCountInLine = 7;
    private DetachableResultReceiver mReceiver;
    private ImageDownloaderLruCache imageDownloaderLruCache;
    private ArrayList<String> hostory;
    private ImageView tvPoster;
    private TextView tvPresent;
    private TextView tvTime;
    private TextView tvDirector;
    private TextView tvActor;
    private ImageView tvHD;
    private TextView tvScore;
    private RatingBar rbScore;
    private TextView tvRecount;
    private HorizontalVariableListView hListView;
    private TextView tvDetail;
    private Button btnPlay;
    private Button btnFavorite;
    private ImageView focusmask;
    private TextView mTextViewEpisodeIndex;
    private String mSiteName = null;
    private int focuswidth;
    private int focusheight;
    private OnKeyListener btnKey = new OnKeyListener() {
        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                int awidth = hListView.getMeasuredWidth();
                focusheight = 246;
                focuswidth = (int) (focusheight * 0.618f + 10);
                FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(
                        focuswidth, focusheight);
                lp.setMargins((awidth - focuswidth + 10) / 2, -10, 0, 0);
                focusmask.setLayoutParams(lp);
                hListView.centerChildRequestFocus();
            }
            return false;
        }
    };
    private Drawable drawPlay;
    private Drawable drawActor;
    private Drawable drawFavorite;
    private Drawable drawFavorited;
    //    private String mChannelId;
//    private int mHashCode;
//    private String mUpdate;
    private DatabaseHandler databaseHandler;
    private ProgramSimple mProgramSimple;
    private ArrayList<Source> mSourcesList;
    //    private Map<String, Collection<Episode>> mEpisodeMap;
    private AsyncHttpClient mHttpc = new AsyncHttpClient();
    private String mSourcesFromHttpServer;
    private int mPosition;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mReceiver = new DetachableResultReceiver(new Handler());
        mReceiver.setReceiver(this);
        hostory = new ArrayList<String>();

        /*Bundle args = getArguments();
        Method method = new Gson().fromJson(args.getString(Intent.EXTRA_UID,null),Method.class);
        mChannelId = method.getChannelID();
        mHashCode = method.getHashCode();
        mUpdate = args.getString(Intent.EXTRA_TEXT);*/

        Bundle args = getArguments();
        mProgramSimple = new Gson().fromJson(args.getString(Intent.EXTRA_UID, null), ProgramSimple.class);

        databaseHandler = new DatabaseHandler(getActivity());

    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        review(mProgramSimple);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_detail, null);
        tvPoster = (ImageView) root.findViewById(R.id.tv_poster);
        tvPresent = (TextView) root.findViewById(R.id.tv_present);

        tvTime = (TextView) root.findViewById(R.id.tv_time);
        tvDirector = (TextView) root.findViewById(R.id.tv_director);
        tvActor = (TextView) root.findViewById(R.id.tv_actor);
        tvHD = (ImageView) root.findViewById(R.id.tv_hd_flag);
        tvScore = (TextView) root.findViewById(R.id.tv_score);
        rbScore = (RatingBar) root.findViewById(R.id.star_score);
        tvRecount = (TextView) root.findViewById(R.id.recount);
        hListView = (HorizontalVariableListView) root
                .findViewById(R.id.detail_recommend);
        tvDetail = (TextView) root.findViewById(R.id.tv_detail);
        btnPlay = (Button) root.findViewById(R.id.btn_play);
        btnFavorite = (Button) root.findViewById(R.id.btn_favorite);
        focusmask = (ImageView) root.findViewById(R.id.focusmask);

        drawPlay = getResources().getDrawable(R.drawable.btn_play);
        drawActor = getResources().getDrawable(R.drawable.btn_actor);
        drawFavorite = getResources().getDrawable(R.drawable.btn_favorite);
        drawFavorited = getResources().getDrawable(R.drawable.btn_favorite_sel);
        btnPlay.setOnKeyListener(btnKey);
        btnFavorite.setOnKeyListener(btnKey);
        btnFavorite.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                ProgramSimple ps = (ProgramSimple) btnFavorite.getTag(R.string.programe_simple_object);
                Object result = btnFavorite.getTag(R.string.favorite_object);
                if (result == null) {
                    // addRecent to favorites
                    databaseHandler.addFavorite(ps);
                    btnFavorite.setTag(R.string.favorite_object, ps);
                    btnFavorite.setText("已收藏");

                    Intent intent = new Intent();
                    intent.putExtra(Intent.EXTRA_UID, new Gson().toJson(mProgramSimple));
                    getActivity().setResult(1, intent);
                } else {
                    // remove from favorites
                    databaseHandler.deleteFavorite(ps);
                    btnFavorite.setTag(R.string.favorite_object, null);
                    btnFavorite.setText("收藏");

                    Intent intent = new Intent();
                    intent.putExtra(Intent.EXTRA_UID, new Gson().toJson(mProgramSimple));
                    getActivity().setResult(1, intent);
                }
            }
        });
        hListView.setUpAndDownFocus(false, R.id.btn_play, false, 0, true);
        hListView.setOnItemSelectedListener(new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                tvRecount.setText((position + 1) + "/" + hsize);
                focusmask.setVisibility(View.VISIBLE);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                tvRecount.setText(String.valueOf(hsize));
                focusmask.setVisibility(View.INVISIBLE);
            }
        });
        btnPlay.setOnFocusChangeListener(new OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if (hasFocus) {
                    focusmask.setVisibility(View.INVISIBLE);
                }
            }
        });

        // imageDownload
        ImageCacheParams cacheParams = new ImageCacheParams("thumbs");
        // be care of this value
        cacheParams.memCacheSize = 1024 * 1024 * UIUtils
                .getMemoryClass(getActivity()) / 8;
        cacheParams.memoryCacheEnabled = false;
        imageDownloaderLruCache = new ImageDownloaderLruCache(getActivity());
        imageDownloaderLruCache.setImageThumbSize(200);
        imageDownloaderLruCache.setLoadingImage(R.drawable.item_default_image);
        imageDownloaderLruCache.setImageObjectCache(ImageObjectCache
                .findOrCreateCache(getActivity(), cacheParams));

        mTextViewEpisodeIndex = (TextView) root.findViewById(R.id.episodeIndex);

        return root;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        BitmapDrawable bitmapDrawable = (BitmapDrawable) tvPoster.getDrawable();
        Bitmap bitmap = null;
        if (bitmapDrawable != null) {
            bitmap = bitmapDrawable.getBitmap();
            if (bitmap != null)
                bitmap.recycle();
        }

        try {
            this.finalize();
        } catch (Throwable e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onReceiveResult(int resultCode, Bundle resultData) {
        String actionkey = resultData.getString(Intent.EXTRA_TEXT);
        switch (resultCode) {
            case SyncService.STATUS_RUNNING:
                break;
            case SyncService.STATUS_FINISHED:
                if (isVisible() && actionkey != null) {
                    if (actionkey.equals(GetProgramDetail.class.getSimpleName())) {
                        updateDetail(resultData);
                    } else if (actionkey.equals(GetProgramSources.class
                            .getSimpleName())) {
                        updateSources(resultData);
                    } else if (actionkey.equals(GetProgramEpisodesByPage.class.getSimpleName())) {
                        updateEpisode(resultData);
                    } else if (actionkey.equals(GetProgramRelated.class
                            .getSimpleName())) {
                        updateRelated(resultData);
                    }
                }
                break;
            case SyncService.STATUS_ERROR:
                if (isAdded()) {
                    if (!actionkey.equals(GetProgramEpisodesByPage.class.getSimpleName()))
                        showMsg(getString(R.string.network_timeout));
                }
                break;
            default:
                Log.w(TAG, "warring resultCode=" + resultCode);
        }
    }

    /**
     * 更新详情页面
     *
     * @param bundle
     */
    public void updateDetail(Bundle bundle) {
        ProgramDetail detail = new Gson().fromJson((String) bundle
                .getSerializable(GetProgramDetail.class.getSimpleName()), ProgramDetail.class);
        Method detailMethod = new Gson().fromJson(bundle.getString(Intent.EXTRA_UID), Method.class);

        if (detail == null) {
            btnPlay.setEnabled(false);
            btnFavorite.setEnabled(false);
            return;
        }

        // updateRecent channelId to normal channel id like the id of "电影",not the id of "专题1"
        mProgramSimple.setHashCode(detailMethod.getHashCode());
        mProgramSimple.setPoster(detail.getPoster());
        mProgramSimple.setName(detail.getName());
        // TODO
//        mProgramSimple.setUpdateStatus();
        mProgramSimple.setChannelId(getNormalChannelId(detail));

        // get source from http server
        getSourceFromHttpServer();

        // 查询相关推荐
        Intent relatedIntent = new Intent(Intent.ACTION_SYNC, null,
                getActivity(), GetProgramRelated.class);
        relatedIntent.putExtra(Intent.EXTRA_TEXT, GetProgramRelated.class.getSimpleName());
        Method method = new Method();
        method.setName(DoudianService.GET_PROGRAM_RELATED);
        method.setChannelID(mProgramSimple.getChannelId());
        method.setHashCode(mProgramSimple.getHashCode());
        relatedIntent.putExtra(Intent.EXTRA_UID, new Gson().toJson(method));
        relatedIntent.putExtra(SyncService.EXTRA_STATUS_RECEIVER, mReceiver);
        getActivity().startService(relatedIntent);


        if (btnPlay.isFocusableInTouchMode()) {
            btnPlay.requestFocusFromTouch();
        } else {
            btnPlay.requestFocus();
        }

        if (detail.getArea() != null && !"".equals(detail.getArea())) {
            tvTime.setText(detail.getShowTime() + "  "
                    + detail.getArea());
        } else {
            tvTime.setText(detail.getShowTime());
        }
        // 导演演员显示方案
        tvDirector.setText(null);
        tvActor.setText(null);
        if ("电视剧".equals(detail.getChannel())
                || "电影".equals(detail.getChannel())) {
            if (detail.getDirector().length() > 0) {
                tvDirector.setText("导演："
                        + detail.getDirector());
            }
            if (detail.getActor().length() > 0) {
                tvActor.setText("演员："
                        + detail.getActor());
            }
        } else if ("综艺".equals(detail.getChannel())) {
            if (detail.getDirector().length() > 0) {
                tvDirector.setText("主持人："
                        + detail.getDirector());
            }
            if (detail.getActor().length() > 0) {
                tvActor.setText("嘉宾："
                        + detail.getActor());
            }
        }
        tvDetail.setText("简介：\n　　" + detail.getDescription());
        tvScore.setText(detail.getScore());
        rbScore.setProgress(Math.round(new Float(detail.getScore())));

        // is this program favorite?
        ProgramSimple result = databaseHandler.getFavorite(detail.getHashcode(), detail.getName());
        ProgramSimple ps = new ProgramSimple();
        ps.setChannelId(mProgramSimple.getChannelId());
        ps.setHashCode(detail.getHashcode());
        ps.setName(detail.getName());
        ps.setPoster(detail.getPoster());
        ps.setUpdateStatus(mProgramSimple.getUpdateStatus());
        btnFavorite.setTag(R.string.programe_simple_object, ps);
        btnFavorite.setTag(R.string.favorite_object, result);
        if (result == null) {
            btnFavorite.setText("收藏");
        } else {
            btnFavorite.setText("已收藏");
        }

        Recent recent = databaseHandler.getRecent(detail.getHashcode(), detail.getName());
        if (recent == null)
            mPosition = 0;
        else {
            mSiteName = recent.getSource();
            mPosition = recent.getEpisode();
        }

        mTextViewEpisodeIndex.setVisibility(View.VISIBLE);
        mTextViewEpisodeIndex.setText(String.valueOf(mPosition + 1));
    }

    /**
     * 更新节目源
     *
     * @param bundle
     */
    @SuppressWarnings("unchecked")
    public void updateSources(Bundle bundle) {
        mSourcesList = SourceHolder.getInstance().getSourcesList();
//        mEpisodeMap = SourceHolder.getInstance().getEpisodeMap();

        if (mSourcesList != null) {
            mSourcesList.clear();
        }

        Type collectionType = new TypeToken<Collection<Source>>() {
        }.getType();
        mSourcesList = new Gson().fromJson((String) bundle
                .getSerializable(GetProgramSources.class.getSimpleName()), collectionType);

        if (mSourcesList != null && mSourcesList.size() > 0) {

            SourceHolder.getInstance().setSourcesList(mSourcesList);

            btnPlay.setClickable(true);
            btnPlay.setFocusable(true);
            btnPlay.setFocusableInTouchMode(true);
            btnPlay.setCompoundDrawablesWithIntrinsicBounds(drawPlay, null,
                    null, null);
            if (btnPlay.isFocusableInTouchMode()) {
                btnPlay.requestFocusFromTouch();
            } else {
                btnPlay.requestFocus();
            }

//            if (mEpisodeMap == null) {
//                mEpisodeMap = new HashMap<String, Collection<Episode>>();
//                SourceHolder.getInstance().setEpisodeMap(mEpisodeMap);
//            } else
//                mEpisodeMap.clear();


            // 已得到所有分集信息
            // 匹配http server返回的源和抓取来的源，以http server返回的源为基准
            Iterator<Source> sourceIterator = mSourcesList.iterator();
            while (sourceIterator.hasNext()) {
                Source source = sourceIterator.next();
                if (mSourcesFromHttpServer != null && !mSourcesFromHttpServer.contains(source.getAlias())) {
                    sourceIterator.remove();
                }
            }

            if (mSourcesList.size() > 0) {
                mSiteName = mSourcesList.get(0).getAlias();
            }

            for (Source s : mSourcesList) {
//                mEpisodeMap.put(s.getAlias(), null);
                // 获取分集信息
                Intent intent = new Intent(Intent.ACTION_SYNC, null, getActivity(),
                        GetProgramEpisodesByPage.class);
                intent.putExtra(Intent.EXTRA_TEXT, GetProgramEpisodesByPage.class.getSimpleName());
                Method method = new Method();
                method.setName(DoudianService.GET_PROGRAM_EPISODES_BY_PAGE);
                method.setChannelID(mProgramSimple.getChannelId());
                method.setHashCode(mProgramSimple.getHashCode());
                method.setSourceAlias(s.getAlias());
                method.setPageIndex(1);
                intent.putExtra(Intent.EXTRA_UID, new Gson().toJson(method));
                intent.putExtra(SyncService.EXTRA_STATUS_RECEIVER, mReceiver);
                getActivity().startService(intent);
            }

        }
    }

    private void updateEpisode(Bundle bundle) {

//        mEpisodeMap = SourceHolder.getInstance().getEpisodeMap();
        mSourcesList = SourceHolder.getInstance().getSourcesList();

        Method key = new Gson().fromJson(bundle.getString(Intent.EXTRA_UID, null), Method.class);

        ArrayList<Episode> list = (ArrayList<Episode>) bundle
                .getSerializable(GetProgramEpisodesByPage.class.getSimpleName());

        if (list != null && list.size() > 0) {

            Source source = null;
            for (Source s : mSourcesList) {
                if (s.getAlias().equals(key.getSourceAlias())) {
                    source = s;
                    break;
                }
            }
//            if (mEpisodeMap.get(key.getSourceAlias()) == null) {
//                mEpisodeMap.put(key.getSourceAlias(), list);
//            } else {
//                Collection<Episode> episodesList = mEpisodeMap.get(key.getSourceAlias());
//                for (Episode episode : list) {
//                    episodesList.add(episode);
//                }
//                mEpisodeMap.put(key.getSourceAlias(), episodesList);
//            }
            if (source == null) {
                source = new Source();
                source.setAlias(key.getSourceAlias());
                source.setEpisodes(list);
                mSourcesList.add(source);
            } else {
                ArrayList<Episode> episodes = source.getEpisodes();
                if (episodes == null) {
                    episodes = new ArrayList<Episode>();
                    source.setEpisodes(episodes);
                }
                episodes.addAll(list);
            }

//            Collection<Episode> episodesList = mEpisodeMap.get(key.getSourceAlias());
            Collection<Episode> episodesList = source.getEpisodes();
            int modLeft = episodesList.size() % 20;
//            int episodePageIndex = episodesList.size() / 20 + 1;
            int episodePageIndex = key.getPageIndex() + 1;

            // 分集信息没有全部获取
            // 获取分集信息
            Intent intent = new Intent(Intent.ACTION_SYNC, null, getActivity(),
                    GetProgramEpisodesByPage.class);
            intent.putExtra(Intent.EXTRA_TEXT, GetProgramEpisodesByPage.class.getSimpleName());
            Method method = new Method();
            method.setName(DoudianService.GET_PROGRAM_EPISODES_BY_PAGE);
            method.setChannelID(mProgramSimple.getChannelId());
            method.setHashCode(mProgramSimple.getHashCode());
            method.setSourceAlias(key.getSourceAlias());
            method.setPageIndex(episodePageIndex);
            intent.putExtra(Intent.EXTRA_UID, new Gson().toJson(method));
            intent.putExtra(SyncService.EXTRA_STATUS_RECEIVER, mReceiver);
            getActivity().startService(intent);

        } else {

            btnPlay.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mSourcesList = SourceHolder.getInstance().getSourcesList();
//                    mEpisodeMap = SourceHolder.getInstance().getEpisodeMap();

                    if (mSourcesList == null || mSourcesList.size() == 0 || mSourcesFromHttpServer == null) {
                        showMsg("未找到播放源，请稍候再试！");
                        return;
                    }

                    /*DetailEpisodeFragment adf = (DetailEpisodeFragment) getActivity()
                            .getFragmentManager().findFragmentByTag(
                                    "episodeDialog");
                    if (adf == null) {
                        adf = new DetailEpisodeFragment(getActivity()
                                .getFragmentManager(), null);
                        adf.showDialog("episodeDialog");
                        adf.setDialogResult(new OnDiaLogListener() {
                            @Override
                            public void OnFinishDialLog(Object o) {
                                Integer[] index = (Integer[]) o;
                                curPy = index[0];
                                curJm = index[1];
                                curIn = index[2];
                                startPlay();
                            }
                        });
                    }*/

                    startPlay();

                }
            });

            mTextViewEpisodeIndex.setOnKeyListener(new OnKeyListener() {
                @Override
                public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                    if (keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                        int mEpisodeTotal = 1;
//                        mEpisodeTotal = SourceHolder.getInstance().getEpisodeMap().get(mSiteName).size();
                        for (Source source : mSourcesList) {
                            if (source.getAlias().equals(mSiteName)) {
                                mEpisodeTotal = source.getEpisodes().size();
                            }
                        }
                        int index = Integer.parseInt(mTextViewEpisodeIndex.getText().toString()) - 1;
                        if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                            if (index == 0)
                                index = mEpisodeTotal - 1;
                            else
                                index--;

                            mTextViewEpisodeIndex.setText(String.valueOf(index + 1));
                            return true;
                        } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                            if (index == mEpisodeTotal - 1)
                                index = 0;
                            else
                                index++;

                            mTextViewEpisodeIndex.setText(String.valueOf(index + 1));
                            return true;
                        }

                    }
                    return false;
                }
            });
        }
    }

    /**
     * 更新相关推荐
     *
     * @param resultData
     */
    @SuppressWarnings("unchecked")
    public void updateRelated(Bundle resultData) {

        Method key = new Gson().fromJson(resultData.getString(Intent.EXTRA_UID, null), Method.class);

        final ArrayList<ProgramSimple> mmItemList = (ArrayList<ProgramSimple>) resultData
                .getSerializable(GetProgramRelated.class.getSimpleName());

        if (mmItemList != null && mmItemList.size() > 0) {
            HorizontalListViewAdapter hla = (HorizontalListViewAdapter) hListView
                    .getAdapter();
            if (hla == null) {
                hla = new HorizontalListViewAdapter(getActivity(),
                        imageDownloaderLruCache);
                hListView.setAdapter(hla);
            } else {
                hla.clear();
            }
            hla.addAll(mmItemList);
            View v = ((View) hListView.getParent());
            hListView.setSize(v.getMeasuredWidth() - 50, 230, mCountInLine,
                    0.618f);
            hListView.setFixPosition(true);
            hListView.setAutoAppendData(false);
            //            tvRecount.setText(hla.getRecentsCount());
            hsize = hla.getCount();

            hla.setOnItemClickListener(new HorizontalListViewAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(int hashCode, int position) {
                    mProgramSimple = mmItemList.get(position);
                    review(mProgramSimple);
                }
            });
        }

    }

    private void review(ProgramSimple programSimple) {

        curPy = 0;
        curJm = 0;
        curIn = 0;
        tvRecount.setText("");
        btnPlay.setClickable(false);

        SourceHolder.getInstance().clear();

        ((DetailActivity) getActivity()).setTitle(programSimple.getName());
        imageDownloaderLruCache.setImageThumbSize(200);
        imageDownloaderLruCache
                .download(programSimple.getPoster(), tvPoster, null);

        if (programSimple.getUpdateStatus() != null && programSimple.getUpdateStatus().endsWith("f")) {
            tvPresent.setText("全 " + programSimple.getUpdateStatus().substring(0, programSimple.getUpdateStatus().length() - 1) + " 集");
            tvPresent.setVisibility(View.VISIBLE);
        } else if (programSimple.getUpdateStatus() != null && !programSimple.getUpdateStatus().endsWith("f")) {
            tvPresent.setText("更新至第" + programSimple.getUpdateStatus() + "集");
            tvPresent.setVisibility(View.VISIBLE);
        } else {
            tvPresent.setText("");
            tvPresent.setVisibility(View.INVISIBLE);
        }

        // 查询详情
        Intent detailIntent = new Intent(Intent.ACTION_SYNC, null,
                getActivity(), GetProgramDetail.class);
        detailIntent.putExtra(Intent.EXTRA_TEXT, GetProgramDetail.class.getSimpleName());
        Method method = new Method();
        method.setName(DoudianService.GET_PROGRAM_DETAIL);
        // this channelId will be normal channel id or special channel id
        // depending on where it clicked from.
        // it will be updated to normal channel id after detail got.
        method.setChannelID(programSimple.getChannelId());
        method.setHashCode(programSimple.getHashCode());
        method.setProgramName(programSimple.getName());
        detailIntent.putExtra(Intent.EXTRA_UID, new Gson().toJson(method));
        detailIntent.putExtra(SyncService.EXTRA_STATUS_RECEIVER, mReceiver);
        getActivity().startService(detailIntent);

    }

    private void startPlay() {
        mPosition = Integer.parseInt(mTextViewEpisodeIndex.getText().toString()) - 1;
        Intent play = new Intent();
        play.setClass(getActivity(), PlayerActivity.class);
        play.putExtra(Intent.EXTRA_UID, new Gson().toJson(mProgramSimple));
        play.putExtra(Intent.EXTRA_REFERRER, mPosition);
        getActivity().startActivity(play);
    }

    private void showMsg(String msg) {
        if (msg != null) {
            Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
        }
    }

    private String getNormalChannelId(ProgramDetail pd) {
        ArrayList<Channel> channels = ChannelsHolder.getInstance().getChannelArrayList();
        if (channels != null && channels.size() > 0) {
            String[] channelArray = pd.getChannel().split("/");
            for (int i = 0; i < channelArray.length; i++) {
                String name = channelArray[i];
                for (Channel channel : channels) {
                    if (name.equals(channel.getName())) {
                        return channel.getId();
                    }
                }
            }
            return null;
        } else
            return null;
    }

    private void getSourceFromHttpServer() {
        final Uri uri = new Uri.Builder().scheme("http")
                .encodedAuthority("127.0.0.1:8098")
                .encodedPath("/proxy/sitename")
                .build();
        mHttpc.get(uri.toString(), null, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(String data) {
                Log.d(TAG, "detect finished,return contents: " + data);
                if (data != null) {
                    String[] lines = data.split("\n");
                    if (lines.length > 0) {
                        mSourcesFromHttpServer = lines[0];
                        Log.e(TAG, "source from http server : " + mSourcesFromHttpServer);

                        Method method = new Method();
                        // 查询节目源
                        Intent sourceIntent = new Intent(Intent.ACTION_SYNC, null,
                                getActivity(), GetProgramSources.class);
                        sourceIntent.putExtra(Intent.EXTRA_TEXT, GetProgramSources.class.getSimpleName());
                        method.setName(DoudianService.GET_PROGRAM_SOURCES);
                        method.setChannelID(mProgramSimple.getChannelId());
                        method.setHashCode(mProgramSimple.getHashCode());
                        sourceIntent.putExtra(Intent.EXTRA_UID, new Gson().toJson(method));
                        sourceIntent.putExtra(SyncService.EXTRA_STATUS_RECEIVER, mReceiver);
                        getActivity().startService(sourceIntent);

                    } else {
                        mSourcesFromHttpServer = null;
                    }
                } else {
                    mSourcesFromHttpServer = null;
                }

            }

            @Override
            public void onFailure(Throwable e, String data) {
                Log.e(TAG, "detect failed. uri = " + uri.toString());
                mSourcesFromHttpServer = null;
            }

        });
    }

}
