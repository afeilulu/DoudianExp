package cn.com.xinli.android.doudian.ui.tv;

/**
 * Created by chen on 2/21/14.
 */

import android.annotation.TargetApi;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.media.MediaPlayer.OnSeekCompleteListener;
import android.media.MediaPlayer.OnVideoSizeChangedListener;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import cn.com.xinli.android.doudian.R;
import cn.com.xinli.android.doudian.database.DatabaseHandler;
import cn.com.xinli.android.doudian.model.Episode;
import cn.com.xinli.android.doudian.model.LocalVideoInfo;
import cn.com.xinli.android.doudian.model.ProgramSimple;
import cn.com.xinli.android.doudian.model.Recent;
import cn.com.xinli.android.doudian.model.Source;
import cn.com.xinli.android.doudian.ui.BaseActivity;
import cn.com.xinli.android.doudian.ui.widget.TappableSurfaceView;
import cn.com.xinli.android.doudian.ui.widget.TransparentThumbSeekBar;
import cn.com.xinli.android.doudian.utils.ControlHider;
import cn.com.xinli.android.doudian.utils.SourceHolder;

public class PlayerActivity extends BaseActivity implements
        OnCompletionListener,
        OnPreparedListener,
        OnErrorListener,
        OnSeekCompleteListener,
        OnVideoSizeChangedListener,
        SurfaceHolder.Callback {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = false;
    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 5000;
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };
    /**
     * If set, will toggle the system UI visibility upon interaction. Otherwise,
     * will show the system UI visibility upon interaction.
     */
    private static final boolean TOGGLE_ON_CLICK = true;
    private final static int mButtonFixedHeight = 80;
    private final static int alphaValue = 200;
    private final static int mForwardStep = 20;
    private final static int mForwardWaitingTime = 5000;
    private static String TAG = "PlayerActivity";
    private Thread.UncaughtExceptionHandler onBlooey =
            new Thread.UncaughtExceptionHandler() {
                public void uncaughtException(Thread thread, Throwable ex) {
                    Log.e(TAG, "Uncaught exception", ex);
                    goBlooey(ex);
                }
            };
    Handler mHideHandler = new Handler();
    Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            mTopPanelHider.hide();
        }
    };
    private boolean hasActiveHolder;
    private int width = 0;
    private int height = 0;
    private MediaPlayer player;
    private TappableSurfaceView surface;
    private SurfaceHolder holder;
    private View topPanel = null;
    private View bottomPanel = null;
    private View sizeSelectorPanel = null;
    private Button sizeSelectorButton = null;
    private Button qualityButton = null;
    private View qualityPanel = null;
    private Button episodeButton = null;
    private View episodePanel = null;
    private Button sourceButton = null;
    private View sourcePanel = null;
    private TextView mTextEpisodeIndex = null;
    private View lastView = null;
    private TextView mProgresssTextView = null;
    private TextView mDurationTextView = null;
    private boolean mAnimationStartFromNow = false;
    View.OnFocusChangeListener OnMenuFocusChangeListener = new View.OnFocusChangeListener() {
        @Override
        public void onFocusChange(View view, boolean b) {
            if (b && mAnimationStartFromNow) {
                subMenuToggle2(view);
            }
        }
    };
    private int mLastPlayPosition;
    private boolean mPlayerFromStart;
    private TappableSurfaceView.TapListener onTap =
            new TappableSurfaceView.TapListener() {
                public void onTap(MotionEvent event) {
//                    if (event.getY() < surface.getHeight() / 2) {
//                        topPanel.setVisibility(View.VISIBLE);
//                    } else {
//                        bottomPanel.setVisibility(View.VISIBLE);
//                    }
                }
            };
    private long mForwardActionStartTime;
    private boolean mForwardFlag = false;
    private int m3u8LastSeekPosition;
    private boolean mIsJumpToUrlFinished = false;
    private ArrayList<LocalVideoInfo> mLocalVideos;
    private boolean isPaused = false;
    private TransparentThumbSeekBar timeline = null;
    private ImageView media = null;
    private TextView whereyougo = null;
    private ProgressBar mWaiting = null;
    //    private StreamProxy mProxy;
    private String mHost;
    /**
     * The instance of the {@link cn.com.xinli.android.doudian.utils.ControlHider} for this activity.
     */
    private ControlHider mTopPanelHider;
    private Collection<Source> mSourcesList;
    private Map<String, Boolean> mSourceAutoSwitchStatus;// String is source alias,Boolean true means this source has been detected or not.
    //    private Map<String, Collection<Episode>> mEpisodeMap;
    private String mSiteName;
    private String mPlayPage;
    private int mEpisodeIndex;
    private int mEpisodeTotal;
    private ProgramSimple mProgramSimple;
    private AsyncHttpClient mHttpc = new AsyncHttpClient();
    private DatabaseHandler databaseHandler;
    private int mMenuId;
    private String[] mDetectResult;
    private int mQualityPanelHeight;
    private int mSourcePanelHeight;
    private int mHd;// current hd
    private int mSize = -1; // screen size ,0:full size (default),1:keep ratio
    private View.OnClickListener onSizeChange = new View.OnClickListener() {
        public void onClick(View v) {
            if (mSize == 0) {
                keepRatio(findViewById(R.id.sizeRatio));
                playPauseToggle();
            } else if (mSize == 1) {
                fullScreen(findViewById(R.id.sizeFull));
                playPauseToggle();
            } else {
                mSize = 0;
            }
        }
    };
    private ArrayList<Integer> mHdAll;
    private int mStartTime;
    private int mEndTime;
    private int mDuration;// in seconds
    private boolean mIsM3u8;
    private int mCurrentPosition;
    private View.OnClickListener onMenu = new View.OnClickListener() {
        public void onClick(View v) {

            // 换集
            if (mTopPanelHider.isVisible() && v.getId() == episodeButton.getId()) {
                int index = Integer.parseInt(mTextEpisodeIndex.getText().toString());
                if (index - 1 != mEpisodeIndex) {
                    mEpisodeIndex = index - 1;

                    // reset timeline position
                    m3u8LastSeekPosition = 0;
                    mCurrentPosition = 0;
                    setFlagOfProgress(mCurrentPosition);
                    if (mHdAll != null) {
                        mHdAll.clear();
                        mHdAll = null;
                    }

                    detect();
                    return;
                }
            }

            // 快进
            if (mForwardFlag) {
                SeekTo(timeline.getProgress() * 1000);
                mForwardFlag = false;
                return;
            }

            playPauseToggle();
        }
    };
    private View.OnClickListener onSourceChange = new View.OnClickListener() {
        public void onClick(View v) {
            if (!v.getTag().toString().equals(mSiteName)) {

                boolean noEpisodeInSource = false;
                String tmpSource = v.getTag().toString();
                for (Source source : mSourcesList) {
                    if (source.getAlias().equals(tmpSource)) {
                        if (mEpisodeIndex >= source.getEpisodes().size()) {
                            Throwable throwable = new Throwable("7:no episode in this source;" + source.getName());
                            goBlooey(throwable);
                            noEpisodeInSource = true;
                            break;
                        }
                    }
                }
                if (noEpisodeInSource)
                    return;

                mSiteName = v.getTag().toString();
                mCurrentPosition = timeline.getProgress();

                if (mCurrentPosition > 0)
                    mPlayerFromStart = false;
                else
                    mPlayerFromStart = true;

                // change selected background
                v.setBackgroundResource(R.drawable.player_menu_button_selected);
                v.setPadding(10, 10, 10, 10);
                for (int i = 0; i < ((ViewGroup) sourcePanel).getChildCount(); i++) {
                    View view = ((ViewGroup) sourcePanel).getChildAt(i);
                    if (view != null && !view.getTag().toString().equals(mSiteName)) {
                        view.setBackgroundResource(R.drawable.player_menu_button_normal);
                        view.setPadding(10, 10, 10, 10);
                    }
                }

                // reset quality
                mHd = 0;
                mHdAll.clear();
                mHdAll = null;
                mQualityPanelHeight = 0;

                detect();
            }
        }
    };
    private View.OnClickListener onQualityChange = new View.OnClickListener() {
        public void onClick(View v) {
            if (mHd != Integer.parseInt(v.getTag().toString())) {
                mHd = Integer.parseInt(v.getTag().toString());
                mCurrentPosition = timeline.getProgress();

                // change selected background
                v.setBackgroundResource(R.drawable.player_menu_button_selected);
                for (int i = 0; i < ((ViewGroup) qualityPanel).getChildCount(); i++) {
                    View view = ((ViewGroup) qualityPanel).getChildAt(i);
                    if (view != null && Integer.parseInt(view.getTag().toString()) != mHd) {
                        view.setBackgroundResource(R.drawable.player_menu_button_normal);
                    }
                }

                detect();
            }
        }
    };

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);

        Thread.setDefaultUncaughtExceptionHandler(onBlooey);

        setContentView(R.layout.activity_player);

        mSourcesList = SourceHolder.getInstance().getSourcesList();
        mSourceAutoSwitchStatus = new HashMap<String, Boolean>();
        for (Source source : mSourcesList) {
            // no detected anyone at first
            mSourceAutoSwitchStatus.put(source.getAlias(), false);
            Log.e(TAG, source.getAlias());
        }
        mProgramSimple = new Gson().fromJson(getIntent().getStringExtra(Intent.EXTRA_UID), ProgramSimple.class);
        mEpisodeIndex = getIntent().getIntExtra(Intent.EXTRA_REFERRER, 0);
        mHost = getIntent().getStringExtra(Intent.EXTRA_SHORTCUT_NAME);

        databaseHandler = new DatabaseHandler(this);

        surface = (TappableSurfaceView) findViewById(R.id.surface);
        surface.addTapListener(onTap);
        holder = surface.getHolder();
        holder.addCallback(this);
//        holder.setFormat(PixelFormat.TRANSLUCENT);
//        holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
        surface.setZOrderOnTop(false);

        topPanel = findViewById(R.id.top_panel);
        bottomPanel = findViewById(R.id.bottom_panel);

        timeline = (TransparentThumbSeekBar) findViewById(R.id.timeline);

        media = (ImageView) findViewById(R.id.media);
        whereyougo = (TextView) findViewById(R.id.where_you_go);
        mWaiting = (ProgressBar) findViewById(R.id.waiting);

        sizeSelectorPanel = findViewById(R.id.sizeSelectorPanel);
        qualityPanel = findViewById(R.id.qualityPanel);
        episodePanel = findViewById(R.id.episodePanel);
        sourcePanel = findViewById(R.id.sourcePanel);

        sizeSelectorButton = (Button) findViewById(R.id.sizeSelectorButton);
        qualityButton = (Button) findViewById(R.id.qualityButton);
        episodeButton = (Button) findViewById(R.id.episodeButton);
        sourceButton = (Button) findViewById(R.id.sourceButton);

        sizeSelectorButton.setOnFocusChangeListener(OnMenuFocusChangeListener);
        qualityButton.setOnFocusChangeListener(OnMenuFocusChangeListener);
        episodeButton.setOnFocusChangeListener(OnMenuFocusChangeListener);
        sourceButton.setOnFocusChangeListener(OnMenuFocusChangeListener);

        sizeSelectorButton.setOnClickListener(onMenu);
        qualityButton.setOnClickListener(onMenu);
        episodeButton.setOnClickListener(onMenu);
        sourceButton.setOnClickListener(onMenu);

        mTextEpisodeIndex = (TextView) findViewById(R.id.episodeIndex);

        ((TextView) findViewById(R.id.name)).setText(mProgramSimple.getName());
        mTextEpisodeIndex.setText(String.valueOf(mEpisodeIndex + 1));

        findViewById(R.id.sizeFull).setOnClickListener(onSizeChange);
        findViewById(R.id.sizeRatio).setOnClickListener(onSizeChange);

        mProgresssTextView = (TextView) findViewById(R.id.progress_text);
        mDurationTextView = (TextView) findViewById(R.id.duration);

        // Set up an instance of SystemUiHider to control the system UI for
        // this activity.
        mTopPanelHider = ControlHider.getInstance(this, surface);
        mTopPanelHider
                .setOnVisibilityChangeListener(new ControlHider.OnVisibilityChangeListener() {
                    // Cached values.

                    @Override
                    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
                    public void onVisibilityChange(final boolean visible) {
                        topPanel.setAlpha(visible ? 0.7f : 0f);
                        View focusedMenu = findViewById(mMenuId);
                        if (focusedMenu != null) {
                            focusedMenu.requestFocus();
                            subMenuToggle2(focusedMenu);
                        } else {
                            subMenuToggle2(getCurrentFocus());
                        }
                    }
                });

        setUpSubMenuSources();
        sourceButton.requestFocus();

        // Avoiding Clipping when Animating a View on top of a SurfaceView
        ((ViewGroup) surface.getParent()).addView(new View(this));

        mPlayerFromStart = true;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
//        delayedHide(100);


    }

    @Override
    protected void onResume() {
        super.onResume();

        mAnimationStartFromNow = false;
        isPaused = false;
        surface.postDelayed(onEverySecond, 1000);
        surface.postDelayed(onEvery3Second, 2000);

        detect();
    }

    @Override
    protected void onPause() {
        super.onPause();

        isPaused = true;
        lastView = null;

        surface.setVisibility(View.GONE);

        addToRecent();

        if (player != null) {
            player.stop();
            player.release();
            player = null;
        }

        surface.removeTapListener(onTap);

//        if (mProxy != null)
//            mProxy.stop();

        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(TAG, "onKeyDown");

        if ((keyCode == KeyEvent.KEYCODE_DPAD_LEFT || keyCode == KeyEvent.KEYCODE_DPAD_RIGHT)
                && !mTopPanelHider.isVisible()) {
            forwardAction(keyCode);
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_BACK && mTopPanelHider.isVisible()) {
            playPauseToggle();
            return true;
        }

        if (keyCode == KeyEvent.KEYCODE_BACK && !mTopPanelHider.isVisible())
            return (super.onKeyDown(keyCode, event));

        if (!mTopPanelHider.isVisible())
            return true;

        if (getCurrentFocus().getId() == episodeButton.getId()) {
//            mEpisodeTotal = SourceHolder.getInstance().getEpisodeMap().get(mSiteName).size();
            for (Source source : mSourcesList) {
                if (source.getAlias().equals(mSiteName)) {
                    mEpisodeTotal = source.getEpisodes().size();
                    break;
                }
            }
            int index = Integer.parseInt(mTextEpisodeIndex.getText().toString()) - 1;
            if (keyCode == KeyEvent.KEYCODE_DPAD_UP) {
                if (index == 0)
                    index = mEpisodeTotal - 1;
                else
                    index--;

                mTextEpisodeIndex.setText(String.valueOf(index + 1));
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_DPAD_DOWN) {
                if (index == mEpisodeTotal - 1)
                    index = 0;
                else
                    index++;

                mTextEpisodeIndex.setText(String.valueOf(index + 1));
                return true;
            }
        }

        return (super.onKeyDown(keyCode, event));
    }

    public void onCompletion(MediaPlayer mediaPlayer) {
        Log.e(TAG, "onCompletion");
        // reset timeline position
        m3u8LastSeekPosition = 0;
        mCurrentPosition = 0;
        if (mHdAll != null) {
            mHdAll.clear();
            mHdAll = null;
        }

        // go to play next or quit
        mEpisodeIndex++;
        String nextEpisode = getEpisodeWebPageUrl(mSiteName, mEpisodeIndex);
        if (nextEpisode == null) {
            this.finish();
        } else {
            detect();
        }

    }

    public void onPrepared(MediaPlayer mediaplayer) {
        width = player.getVideoWidth();
        height = player.getVideoHeight();

        if (width != 0 && height != 0) {
//            holder.setFixedSize(width, height);
            //        keepRatio(player);
            if (mSize == 0) {
                fullScreen(findViewById(R.id.sizeFull));
            } else if (mSize == 1) {
                keepRatio(findViewById(R.id.sizeRatio));
            } else {
                mSize = 0;
                findViewById(R.id.sizeFull).setBackgroundResource(R.drawable.player_menu_button_selected);
            }

            if (mCurrentPosition > 0 && !mIsM3u8)
                SeekTo(mCurrentPosition * 1000);
            else {
                mediaplayer.start();
            }
        }

        findViewById(R.id.waiting).setVisibility(View.INVISIBLE);
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i2) {
        Log.d(TAG, "onError");
        return false;
    }

    @Override
    public void onSeekComplete(MediaPlayer mediaPlayer) {
        Log.d(TAG, "onSeekComplete");
        mediaPlayer.start();
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mediaPlayer, int i, int i2) {
        Log.d(TAG, "onVideoSizeChanged");
    }

    public void surfaceChanged(SurfaceHolder surfaceholder, int i, int j, int k) {
        // no-op
        Log.d(TAG, "surfaceChanged");
    }

    public void surfaceDestroyed(SurfaceHolder surfaceholder) {
        // no-op
        Log.d(TAG, "surfaceDestroyed");

        synchronized (this) {
            hasActiveHolder = false;

            synchronized (this) {
                this.notifyAll();
            }
        }
    }

    public void surfaceCreated(SurfaceHolder holder) {
        // no-op
        Log.d(TAG, "surfaceCreated");

        synchronized (this) {
            hasActiveHolder = true;
            this.notifyAll();
        }
    }

    private void playVideo(String url) {
        Log.e(TAG, "playVideo:" + url);
        try {
            if (player == null) {
                player = new MediaPlayer();
                player.setScreenOnWhilePlaying(true);
            } else {
                player.stop();
                player.reset();
            }

            player.setDataSource(url);
//            player.setDisplay(holder);
            synchronized (this) {
                while (!hasActiveHolder) {
                    try {
                        this.wait();
                    } catch (InterruptedException e) {
                        Log.e(TAG, "surface view create error");
                    }
                }
                player.setDisplay(holder);
            }

            player.setAudioStreamType(AudioManager.STREAM_MUSIC);
            player.setOnPreparedListener(this);
            player.prepareAsync();
            player.setOnCompletionListener(this);

            player.setOnErrorListener(this);
            player.setOnSeekCompleteListener(this);
            player.setOnVideoSizeChangedListener(this);
        } catch (Throwable t) {
            Log.e(TAG, "Exception in media prep", t);
            goBlooey(t);
        }
    }

    private void goBlooey(Throwable t) {
        /*AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder
                .setTitle("Exception!")
                .setMessage(t.toString())
                .setPositiveButton("OK", null)
                .show();*/

        boolean needToCheckAnotherSource = false;
        int errorStringR = R.string.player_error_0;
        if (t.getMessage().startsWith("1")) {
            errorStringR = R.string.player_error_1;
        } else if (t.getMessage().startsWith("2")) {
            needToCheckAnotherSource = true;
            errorStringR = R.string.player_error_2;
        } else if (t.getMessage().startsWith("3")) {
            needToCheckAnotherSource = true;
            errorStringR = R.string.player_error_3;
        } else if (t.getMessage().startsWith("4")) {
            needToCheckAnotherSource = true;
            errorStringR = R.string.player_error_4;
        } else if (t.getMessage().startsWith("5")) {
            needToCheckAnotherSource = true;
            errorStringR = R.string.player_error_5;
        } else if (t.getMessage().startsWith("6")) {
            errorStringR = R.string.player_error_6;
        } else if (t.getMessage().startsWith("7")) {
            String[] tmpStr = t.getMessage().split(";");
            if (tmpStr.length > 1) {
                errorStringR = R.string.player_error_7;
                Toast.makeText(getApplicationContext(), tmpStr[1] + getResources().getString(errorStringR), Toast.LENGTH_SHORT).show();
            }
            return;
        }

        if (needToCheckAnotherSource) {
            // now we start to switch another unchecked source
            // if none exists, exit this activity
            boolean unCheckedSourceExisted = false;
            for (Map.Entry entry : mSourceAutoSwitchStatus.entrySet()) {
                Boolean checked = (Boolean) entry.getValue();
                if (!checked.booleanValue()) {
                    mSiteName = (String) entry.getKey();
                    unCheckedSourceExisted = true;
                    break;
                }
            }
            if (unCheckedSourceExisted) {
                for (int i = 0; i < ((ViewGroup) sourcePanel).getChildCount(); i++) {
                    View view = ((ViewGroup) sourcePanel).getChildAt(i);
                    if (view.getTag().toString().equals(mSiteName)) {
                        view.setBackgroundResource(R.drawable.player_menu_button_selected);
                    } else {
                        view.setBackgroundResource(R.drawable.player_menu_button_normal);
                    }
                    view.setPadding(10, 10, 10, 10);
                }

                detect();
            } else {
                Toast.makeText(getApplicationContext(), errorStringR, Toast.LENGTH_SHORT).show();
                finish();
            }
        } else {
            Toast.makeText(getApplicationContext(), errorStringR, Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    public void keepRatio(View view) {
        mSize = 1;

        int widthN;
        int heightN;

        float videoProportion = (float) width / (float) height;

        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager windowManager = getWindowManager();
        windowManager.getDefaultDisplay().getMetrics(metrics);
        int screenWidth = metrics.widthPixels;
        int screenHeight = metrics.heightPixels;
        float screenProportion = (float) screenWidth / (float) screenHeight;

        if (videoProportion > screenProportion) {
            widthN = screenWidth;
            heightN = (int) ((float) screenWidth / videoProportion);
        } else {
            widthN = (int) (videoProportion * (float) screenHeight);
            heightN = screenHeight;
        }

//        ViewGroup.LayoutParams lp = surface.getLayoutParams();
//        lp.width = widthN - 1;
//        lp.height = heightN - 1;
//        surface.setLayoutParams(lp);
//        surface.invalidate();

        holder.setFixedSize(widthN - 1, heightN - 1);

        view.setBackgroundResource(R.drawable.player_menu_button_selected);
        findViewById(R.id.sizeFull).setBackgroundResource(R.drawable.player_menu_button_normal);

    }

    public void originalSize(View view) {
        holder.setFixedSize(width, height);
        playPauseToggle();
    }

    public void fullScreen(View view) {
        mSize = 0;
        DisplayMetrics metrics = new DisplayMetrics();
        WindowManager windowManager = getWindowManager();
        windowManager.getDefaultDisplay().getMetrics(metrics);
        int screenWidth = metrics.widthPixels;
        int screenHeight = metrics.heightPixels;
        holder.setFixedSize(screenWidth, screenHeight);

        view.setBackgroundResource(R.drawable.player_menu_button_selected);
        findViewById(R.id.sizeRatio).setBackgroundResource(R.drawable.player_menu_button_normal);

    }

    private void playPauseToggle() {

        if (player != null) {
            if (player.isPlaying() && !mTopPanelHider.isVisible()) {
                media.setImageResource(R.drawable.ic_media_pause);
                media.setVisibility(View.VISIBLE);
                player.pause();
            } else if (mTopPanelHider.isVisible()) {
                media.setImageResource(R.drawable.ic_media_play);
                media.setVisibility(View.INVISIBLE);
                player.start();
            }
        }

        if (TOGGLE_ON_CLICK) {
            mTopPanelHider.toggle();
        } else {
            mTopPanelHider.show();
        }
    }

    private String getEpisodeWebPageUrl(String sourceAlias, int position) {

        String playUrl = null;

        if (mSourcesList == null) {
            Log.e(TAG, "source all null-----------------------------------");
            return null;
        }

        boolean noEpisodeOnThisSource = false;

        Iterator iterator = mSourcesList.iterator();
        while (iterator.hasNext()) {
            Source source = (Source) iterator.next();

            if (sourceAlias == null) {
                mSiteName = source.getAlias();
                sourceAlias = mSiteName;

                ArrayList<Episode> episodes = source.getEpisodes();
                if (position < episodes.size()) {
                    Episode episode = episodes.get(position);
                    playUrl = source.getUrlPrefix() + "/" + episode.getUrl();
                    Log.d(TAG, "playUrl=" + playUrl);
                    break;
                }

            } else {

                if (source.getAlias().equals(sourceAlias)) {
                    mSiteName = source.getAlias();
                    ArrayList<Episode> episodes = source.getEpisodes();
                    if (position < episodes.size()) {
                        Episode episode = episodes.get(position);
                        playUrl = source.getUrlPrefix() + "/" + episode.getUrl();
                        Log.e(TAG, "playUrl=" + playUrl);
                        break;
                    } else {
                        noEpisodeOnThisSource = true;
                        break;
                    }
                }
            }
        }

        if (noEpisodeOnThisSource) {
            // we need play on another source which has the episode
            while (iterator.hasNext()) {
                Source source = (Source) iterator.next();
                ArrayList<Episode> episodes = source.getEpisodes();
                if (position < episodes.size()) {
                    Episode episode = episodes.get(position);
                    playUrl = source.getUrlPrefix() + "/" + episode.getUrl();
                    mSiteName = source.getAlias();
                    Log.d(TAG, "playUrl=" + playUrl);
                    break;
                }
            }
        }

        // yes,this source is checked and do not care the result
        mSourceAutoSwitchStatus.put(mSiteName, true);

        return playUrl;
    }

    private void detect() {

//        if (mProxy == null || mProxy.mHost == null) {
        if (mHost == null) {
            Throwable throwable = new Throwable("1:HttpServer is not ready!");
            goBlooey(throwable);
            return;
        }

        mWaiting.setAlpha(1);
        whereyougo.setAlpha(1);
        media.setVisibility(View.INVISIBLE);
        mWaiting.setVisibility(View.VISIBLE);

        mPlayPage = getEpisodeWebPageUrl(mSiteName, mEpisodeIndex);

        String siteName = mSiteName;
        for (Source source : SourceHolder.getInstance().getSourcesList()) {
            if (source.getAlias().equals(mSiteName)) {
                siteName = source.getName();
                break;
            }
        }
        whereyougo.setText("正在前往" + siteName + "为您播放");
        whereyougo.setVisibility(View.VISIBLE);

        if (mPlayPage == null || mPlayPage.isEmpty()) {
            Throwable throwable = new Throwable("2:play url is null!");
            goBlooey(throwable);
            return;
        }

        final Uri uri = new Uri.Builder().scheme("http")
                .encodedAuthority(mHost)
                .encodedPath("/proxy/detect")
                .appendQueryParameter("sitename", mSiteName)
                .appendQueryParameter("videokey", "[" + mHd + "]" + mPlayPage)
                .build();
        mHttpc.get(uri.toString(), null, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(String data) {
                Log.e(TAG, "detect finished,return contents: " + data);
                if (data != null) {
                    mDetectResult = data.split("\n");

                    if (mDetectResult.length > 1 && Integer.parseInt(mDetectResult[0]) > 0) {
                        if (mHdAll == null || mHdAll.size() == 0) {
                            videoInfoParse(mDetectResult);
                            setUpSubMenuQuality();

                            // set position on first load
                            if (mPlayerFromStart) {
                                mCurrentPosition = 0;
                                setFlagOfProgress(mCurrentPosition);
                            }
                        }

                        if (mEpisodeIndex + 1 == Integer.parseInt(mTextEpisodeIndex.getText().toString())) {
                            // hide ui
                            playPauseToggle();
                        } else {
                            // next episode load successfully
                            mTextEpisodeIndex.setText(String.valueOf(mEpisodeIndex + 1));
                            mCurrentPosition = 0;
                            setFlagOfProgress(mCurrentPosition);
                        }

                        playVideo(uriPrehandle(mDetectResult[1]));
                    } else {
                        media.setVisibility(View.INVISIBLE);
                        Throwable t = new Throwable("3:wrong data format");
                        goBlooey(t);
                    }
                } else {
                    media.setVisibility(View.INVISIBLE);
                    Throwable t = new Throwable("4:null data from server");
                    goBlooey(t);
                }

            }

            @Override
            public void onFailure(Throwable e, String data) {
                Log.e(TAG, "detect failed. uri = " + uri);
                if (!isPaused) {
                    // need to check another source
                    Throwable t = new Throwable("5:detect failed");
                    goBlooey(t);
                }
            }

        });
    }

    private String uriPrehandle(String uri) {
        if (uri.endsWith("m3u8")) {
            mIsM3u8 = true;
            if (mCurrentPosition == 0)
                return uri + ".ts";
            else
                return uri + ".ts.vod." + mCurrentPosition;
        } else
            mIsM3u8 = false;

        return uri;
    }

    /**
     * construct quality sub_menu
     * It will change only depending on program changed
     */
    private void setUpSubMenuQuality() {
        ((ViewGroup) qualityPanel).removeAllViews();
        for (Integer i : mHdAll) {
            addNewSubMenuButtonOfQuality(i);
        }
        if (((ViewGroup) qualityPanel).getChildCount() > 0) {
            View view = ((ViewGroup) qualityPanel).getChildAt(((ViewGroup) qualityPanel).getChildCount() - 1);
            view.setNextFocusDownId(view.getId());
        }
    }

    /**
     * construct sources sub_menu from source holder
     */
    private void setUpSubMenuSources() {

        ((ViewGroup) sourcePanel).removeAllViews();
        for (Source source : SourceHolder.getInstance().getSourcesList()) {
            addNewSubMenuButtonOfSource(source);
        }

        // the first source is selected in default
        if (mSiteName == null) {
            View view = ((ViewGroup) sourcePanel).getChildAt(0);
            if (view != null) {
                mSiteName = view.getTag().toString();
                view.setBackgroundResource(R.drawable.player_menu_button_selected);
                view.setPadding(10, 10, 10, 10);
            }
        }

        if (((ViewGroup) sourcePanel).getChildCount() > 0) {
            View view = ((ViewGroup) sourcePanel).getChildAt(((ViewGroup) sourcePanel).getChildCount() - 1);
            view.setNextFocusDownId(view.getId());
        }

    }

    private void addToRecent() {
        Recent item = new Recent();
        item.setHashCode(mProgramSimple.getHashCode());
        item.setName(mProgramSimple.getName());
        item.setPoster(mProgramSimple.getPoster());
        item.setUpdateStatus(mProgramSimple.getUpdateStatus());
        item.setChannelId(mProgramSimple.getChannelId());
        item.setSource(mSiteName);
        item.setEpisode(mEpisodeIndex);
        item.setDurationInSec(mDuration);
        item.setCurrentPositionInSec(timeline.getProgress());
        databaseHandler.deleteRecent(item);
        databaseHandler.addRecent(item);

    }

    /**
     * sample:
     * hd=3,hdAll=8,startTime=0,endTime=150,duration=2729,sitename=qq
     *
     * @param mDetectResult
     */
    private void videoInfoParse(String[] mDetectResult) {
        Log.e(TAG, "videoInfoParse");

        String info = mDetectResult[mDetectResult.length - 1];

        int hdAll;

        String[] tmpStrings = info.split(",");
        for (int i = 0; i < tmpStrings.length; i++) {
            if (tmpStrings[i].startsWith("hd")) {
                if (tmpStrings[i].startsWith("hdAll")) {
                    hdAll = Integer.parseInt(tmpStrings[i].split("=")[1]);
                    HdParser(hdAll);
                } else {
                    mHd = Integer.parseInt(tmpStrings[i].split("=")[1]);
                }
            }

            if (tmpStrings[i].startsWith("startTime")) {
                mStartTime = Integer.parseInt(tmpStrings[i].split("=")[1]);
            }

            if (tmpStrings[i].startsWith("endTime")) {
                mEndTime = Integer.parseInt(tmpStrings[i].split("=")[1]);
            }

            if (tmpStrings[i].startsWith("duration")) {
                mDuration = Integer.parseInt(tmpStrings[i].split("=")[1]);
                ((TextView) findViewById(R.id.duration)).setText(seconds2TimeString(mDuration));
                timeline.setMax(mDuration);
                Log.e(TAG, "mDuration=" + mDuration);
            }
        }

        if (mLocalVideos == null)
            mLocalVideos = new ArrayList<LocalVideoInfo>();
        else
            mLocalVideos.clear();

        for (int i = 0; i < Integer.parseInt(mDetectResult[0]); i++) {
            LocalVideoInfo localVideoInfo = new LocalVideoInfo();
            String[] tmpStrs = mDetectResult[i + 1].split("-mohoduration-");
            if (tmpStrs.length > 1) {
                localVideoInfo.setUrl(tmpStrs[0]);
                localVideoInfo.setDuration(Integer.parseInt(tmpStrs[1]));
            } else {
                localVideoInfo.setUrl(tmpStrs[0]);
                localVideoInfo.setDuration(mDuration);
            }
            mLocalVideos.add(localVideoInfo);
        }
    }

    /**
     * 清晰度含义： 00111111表示360,480,640,720,1280,1920
     * 1920:蓝光 0x20
     * 1280:超清 0x10
     * 720:高清 0x08
     * 640:标清 0x04
     * 480:流畅 0x02
     * 360:极速 0x01
     *
     * @param all
     */
    private void HdParser(int all) {
        if (mHdAll == null)
            mHdAll = new ArrayList<Integer>();
        else
            mHdAll.clear();

        if ((all & 0X01) == 1) {
            mHdAll.add(0);
        }

        if ((all & 0X02) == 2) {
            mHdAll.add(1);
        }

        if ((all & 0X04) == 4) {
            mHdAll.add(2);
        }

        if ((all & 0X08) == 8) {
            mHdAll.add(3);
        }

        if ((all & 0X10) == 16) {
            mHdAll.add(4);
        }

        if ((all & 0X20) == 32) {
            mHdAll.add(5);
        }
    }

    private void addNewSubMenuButtonOfQuality(int quality) {
        int idStart = 100;
        Button button = (Button) getLayoutInflater().inflate(R.layout.submenu_button_template, null);
        switch (quality) {
            case 0:
                button.setText(R.string.quality_360);
                break;
            case 1:
                button.setText(R.string.quality_480);
                break;
            case 2:
                button.setText(R.string.quality_640);
                break;
            case 3:
                button.setText(R.string.quality_720);
                break;
            case 4:
                button.setText(R.string.quality_1280);
                break;
            case 5:
                button.setText(R.string.quality_1920);
                break;
        }
        button.setTag(quality);
        button.setNextFocusLeftId(sizeSelectorButton.getId());
        button.setNextFocusRightId(episodeButton.getId());
        button.setOnClickListener(onQualityChange);
        button.setLayoutParams(new LinearLayout.LayoutParams(150, mButtonFixedHeight));
        if (quality == mHd) {
            button.setBackgroundResource(R.drawable.player_menu_button_selected);
        }
//        button.setAlpha(0.7f);
        button.setId(idStart++);

        ((ViewGroup) qualityPanel).addView(button);

        mQualityPanelHeight = mQualityPanelHeight + mButtonFixedHeight;
    }

    private Runnable onEverySecond = new Runnable() {
        public void run() {

            // disappear after reset 3 seconds
            if (System.currentTimeMillis() - mForwardActionStartTime > mForwardWaitingTime + 3000) {
                bottomPanel.setVisibility(View.INVISIBLE);
                mProgresssTextView.setVisibility(View.INVISIBLE);
            }

            // 设回正在播放的位置
            if (mForwardFlag && System.currentTimeMillis() - mForwardActionStartTime > mForwardWaitingTime) {
                mForwardFlag = false;
                if (mIsM3u8) {
                    timeline.setProgress(m3u8LastSeekPosition + player.getCurrentPosition() / 1000);
                } else {
                    timeline.setProgress(player.getCurrentPosition() / 1000);
                }
//                mProgresssTextView.setX(timeline.getSeekBarThumb().getBounds().centerX() + 25);
                mProgresssTextView.animate().translationX(timeline.getSeekBarThumb().getBounds().centerX() + 25);
            }

            // 正常设置位置
            if (!isPaused && player != null && !mForwardFlag) {

                if (mIsM3u8) {
                    timeline.setProgress(m3u8LastSeekPosition + player.getCurrentPosition() / 1000);
                } else {
                    timeline.setProgress(player.getCurrentPosition() / 1000);
                }
                mProgresssTextView.setText(seconds2TimeString(timeline.getProgress()));

                if (timeline.getSecondaryProgress() < timeline.getProgress()) {
                    timeline.setSecondaryProgress(timeline.getProgress());
                }

                mLastPlayPosition = player.getCurrentPosition();
            }

            if (!isPaused) {
                surface.postDelayed(onEverySecond, 1000);
            }
        }
    };

    private void addNewSubMenuButtonOfSource(Source source) {
        int idStart = 10;
        Button button = (Button) getLayoutInflater().inflate(R.layout.submenu_button_template, null);
        button.setText(source.getName());
        button.setTag(source.getAlias());
        button.setNextFocusLeftId(episodeButton.getId());
        button.setOnClickListener(onSourceChange);
        button.setLayoutParams(new LinearLayout.LayoutParams(150, mButtonFixedHeight));
        if (mSiteName != null && source.getAlias().equals(mSiteName)) {
            button.setBackgroundResource(R.drawable.player_menu_button_selected);
        }
        if (source.getAlias().contains("youku"))
            button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.site_youku, 0, 0, 0);
        else if (source.getAlias().contains("sohu"))
            button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.site_sohu, 0, 0, 0);
        else if (source.getAlias().contains("qq"))
            button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.site_qq, 0, 0, 0);
        else if (source.getAlias().contains("qiyi"))
            button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.site_qiyi, 0, 0, 0);
        else if (source.getAlias().contains("funshion"))
            button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.site_funshion, 0, 0, 0);
        else if (source.getAlias().contains("pptv"))
            button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.site_pptv, 0, 0, 0);
        else if (source.getAlias().contains("tudou"))
            button.setCompoundDrawablesWithIntrinsicBounds(R.drawable.site_tudou, 0, 0, 0);

        button.setPadding(10, 10, 10, 10);
        button.setId(idStart++);

        ((ViewGroup) sourcePanel).addView(button);

        mSourcePanelHeight = mSourcePanelHeight + mButtonFixedHeight;
    }

    private String seconds2TimeString(int seconds) {
        long hours = seconds / 3600,
                remainder = seconds % 3600,
                minutes = remainder / 60,
                secs = remainder % 60;

        return ((hours < 10 ? "0" : "") + hours
                + ":" + (minutes < 10 ? "0" : "") + minutes
                + ":" + (secs < 10 ? "0" : "") + secs);
    }

    private void forwardAction(int keyCode) {

        if (mProgresssTextView.getVisibility() != View.VISIBLE) {
            bottomPanel.setVisibility(View.VISIBLE);
            mProgresssTextView.setVisibility(View.VISIBLE);
        }

        mForwardActionStartTime = System.currentTimeMillis();
        mForwardFlag = true;

        int position = timeline.getProgress();
        if (keyCode == KeyEvent.KEYCODE_DPAD_LEFT) {
            // 快退
            if (position > mForwardStep)
                position = position - mForwardStep;
            else
                position = 0;
        } else if (keyCode == KeyEvent.KEYCODE_DPAD_RIGHT) {
            // 快进
            if (position < mDuration - mForwardStep)
                position = position + mForwardStep;
            else
                position = mDuration;
        }

        setFlagOfProgress(position);
    }

    private void SeekTo(int milliSeconds) {
        if (!mIsM3u8) {
            if (mLocalVideos.size() == 1)
                player.seekTo(milliSeconds);
            else {
                if (mIsJumpToUrlFinished) {
                    player.seekTo(milliSeconds);
                    mIsJumpToUrlFinished = false;
                } else {
                    String jumpToUrl = null;
                    int countPosition = 0;
                    for (int i = 0; i < mLocalVideos.size(); i++) {
                        jumpToUrl = mLocalVideos.get(i).getUrl();
                        countPosition = countPosition + mLocalVideos.get(i).getDuration() * 1000;
                        if (countPosition > milliSeconds) {
                            break;
                        }
                    }
                    mCurrentPosition = milliSeconds - countPosition;
                    mIsJumpToUrlFinished = true;
                    detect();
                }
            }
            return;
        }

        m3u8LastSeekPosition = milliSeconds / 1000;
        playVideo(mLocalVideos.get(0).getUrl() + ".ts.vod." + m3u8LastSeekPosition);

    }

    private void subMenuToggle2(View view) {
        if (view == null) return;

        if (lastView == null) {
            mAnimationStartFromNow = true;
            lastView = sizeSelectorPanel;
            return;
        }

        mMenuId = view.getId();
        switch (view.getId()) {
            case R.id.sizeSelectorButton:
                if (lastView != sizeSelectorPanel) {
                    lastView.setAlpha(0f);
                    lastView.setVisibility(View.GONE);
                    sizeSelectorPanel.setVisibility(View.VISIBLE);
                    sizeSelectorPanel.animate().alpha(0.7f);
                    lastView = sizeSelectorPanel;
                } else {
                    if (mTopPanelHider.isVisible()) {
                        lastView.setVisibility(View.VISIBLE);
                        lastView.animate().alpha(0.7f);
                    } else {
                        lastView.setAlpha(0f);
                        lastView.setVisibility(View.GONE);
                    }
                }
                break;
            case R.id.qualityButton:
                if (lastView != qualityPanel) {
                    lastView.setAlpha(0f);
                    lastView.setVisibility(View.GONE);
                    qualityPanel.setVisibility(View.VISIBLE);
                    qualityPanel.animate().alpha(0.7f);
                    lastView = qualityPanel;
                } else {
                    if (mTopPanelHider.isVisible()) {
                        lastView.setVisibility(View.VISIBLE);
                        lastView.animate().alpha(0.7f);
                    } else {
                        lastView.setAlpha(0f);
                        lastView.setVisibility(View.GONE);
                    }
                }
                break;
            case R.id.episodeButton:
                if (lastView != episodePanel) {
                    lastView.setAlpha(0f);
                    lastView.setVisibility(View.GONE);
                    episodePanel.setVisibility(View.VISIBLE);
                    episodePanel.animate().alpha(0.7f);
                    lastView = episodePanel;
                    mTextEpisodeIndex.setText(String.valueOf(mEpisodeIndex + 1));
                } else {
                    if (mTopPanelHider.isVisible()) {
                        lastView.setVisibility(View.VISIBLE);
                        lastView.animate().alpha(0.7f);
                    } else {
                        lastView.setAlpha(0f);
                        lastView.setVisibility(View.GONE);
                    }
                }
                break;
            case R.id.sourceButton:
                if (lastView != sourcePanel) {
                    lastView.setAlpha(0f);
                    lastView.setVisibility(View.GONE);
                    sourcePanel.setVisibility(View.VISIBLE);
                    sourcePanel.animate().alpha(0.7f);
                    lastView = sourcePanel;
                } else {
                    if (mTopPanelHider.isVisible()) {
                        lastView.setVisibility(View.VISIBLE);
                        lastView.animate().alpha(0.7f);
                    } else {
                        lastView.setAlpha(0f);
                        lastView.setVisibility(View.GONE);
                    }
                }
                break;
            default:
                break;
        }
    }

    private void setFlagOfProgress(int seconds) {
        timeline.setProgress(seconds);
        mProgresssTextView.setAlpha(1f);
        mProgresssTextView.setText(seconds2TimeString(seconds));
//                  mProgresssTextView.setX(timeline.getSeekBarThumb().getBounds().centerX() + 25);
        mProgresssTextView.animate().cancel();
        mProgresssTextView.animate().setStartDelay(200);
        mProgresssTextView.animate().translationX(timeline.getSeekBarThumb().getBounds().centerX() + 25);
    }

    /**
     * 返回值 ：
     * spd:63,,,pre:0,,,state:0,,,buf:364,,,st:-1,,,at:2729
     * spd:当前速度，63 K/s
     * pre: 缓冲进度 0-100
     * state:下载状态 200 下载完成
     * buf: 缓冲的可以供给播放的秒数  364：还可以播放364秒
     * st: m3u8 快进后的基础时间  ；
     * at:视频的总时间
     */
    private void getHttpServerState() {

//        if (mProxy == null || mProxy.mHost == null) {
        if (mHost == null) {
            Throwable throwable = new Throwable("1:HttpServer is not ready!");
            goBlooey(throwable);
            return;
        }

        int currentProgress = timeline.getProgress();

        final Uri uri = new Uri.Builder().scheme("http")
                .encodedAuthority(mHost)
                .encodedPath("/proxy/state")
                .appendQueryParameter("pauseflag", "0")
                .appendQueryParameter("seektime", "0")
                .appendQueryParameter("time", String.valueOf(currentProgress))
                .build();

        mHttpc.get(uri.toString(), null, new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(String data) {
//                Log.e(TAG, "get state return contents: " + data);
                if (data != null) {
                    String[] stateResult = data.split(",,,");
                    if (stateResult.length > 5) {
                        // update duration. Sometime we can not get duration through detect.
                        // but in this thread, we can reset it.
                        String[] nameValue = stateResult[5].split(":");
                        if (nameValue.length > 1) {
                            mDuration = Integer.parseInt(nameValue[1]);
                            mDurationTextView.setText(seconds2TimeString(mDuration));
                            timeline.setMax(mDuration);
                        }

                        // update second progress
                        nameValue = stateResult[3].split(":");
                        if (nameValue.length > 1) {
                            timeline.setSecondaryProgress(timeline.getProgress() + Integer.parseInt(nameValue[1]));
                        }

                        // if player stuck, show download speed and bottom panel
                        if (!mForwardFlag) {
                            nameValue = stateResult[0].split(":");
                            if (player != null
                                    && !isPaused
                                    && nameValue.length > 1
                                    && player.getCurrentPosition() - mLastPlayPosition > 3000) {
                                whereyougo.setText(nameValue[1] + "K/s");
                                whereyougo.setVisibility(View.VISIBLE);
                                bottomPanel.setVisibility(View.VISIBLE);
                                mProgresssTextView.setVisibility(View.VISIBLE);
                                mWaiting.setVisibility(View.VISIBLE);
                            } else {
                                whereyougo.setText("");
                                whereyougo.setVisibility(View.GONE);
                                bottomPanel.setVisibility(View.INVISIBLE);
                                mProgresssTextView.setVisibility(View.INVISIBLE);
                                mWaiting.setVisibility(View.INVISIBLE);
                            }
                        }

                    }

                }
            }

            @Override
            public void onFailure(Throwable e, String data) {
                Log.e(TAG, "get state failed. uri = " + uri);
            }

        });
    }


    /**
     * Thread for get http serve state and update ui
     */
    private Runnable onEvery3Second = new Runnable() {
        public void run() {

            getHttpServerState();

            if (!isPaused) {
                surface.postDelayed(onEvery3Second, 3000);
            }
        }
    };


}
