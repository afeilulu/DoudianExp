package cn.com.xinli.android.doudian.utils;

import android.app.Activity;
import android.view.View;

/**
 * A utility class that helps with showing and hiding system UI such as the
 * status bar and navigation/system bar. This class uses backward-compatibility
 * techniques described in <a href=
 * "http://developer.android.com/training/backward-compatible-ui/index.html">
 * Creating Backward-Compatible UIs</a> to ensure that devices running any
 * version of ndroid OS are supported. More specifically, there are separate
 * implementations of this abstract class: for newer devices,
 * {@link #getInstance} will return a {@link com.example.player.util.SystemUiHiderHoneycomb} instance,
 * while on older devices {@link #getInstance} will return a
 * {@link com.example.player.util.SystemUiHiderBase} instance.
 * <p>
 * For more on system bars, see <a href=
 * "http://developer.android.com/design/get-started/ui-overview.html#system-bars"
 * > System Bars</a>.
 *
 * @see android.view.View#setSystemUiVisibility(int)
 * @see android.view.WindowManager.LayoutParams#FLAG_FULLSCREEN
 */
public class ControlHider {

    /**
     * The activity associated with this UI hider object.
     */
    protected Activity mActivity;

    /**
     * The view on which {@link android.view.View#setSystemUiVisibility(int)} will be called.
     */
    protected View mAnchorView;

    /**
     * The current visibility callback.
     */
    protected OnVisibilityChangeListener mOnVisibilityChangeListener = sDummyListener;

    /**
     * Whether or not the system UI is currently visible. This is a cached value
     * from calls to {@link #hide()} and {@link #show()}.
     */
    private boolean mVisible = true;

    /**
     * Creates and returns an instance of {@link com.example.player.util.ControlHider} that is
     * appropriate for this device. The object will be either a
     * {@link com.example.player.util.SystemUiHiderBase} or {@link com.example.player.util.SystemUiHiderHoneycomb} depending on
     * the device.
     *
     * @param activity The activity whose window's system UI should be
     *            controlled by this class.
     * @param anchorView The view on which
     *            {@link android.view.View#setSystemUiVisibility(int)} will be called.
     */
    public static ControlHider getInstance(Activity activity, View anchorView) {
        return new ControlHider(activity,anchorView);
    }

    protected ControlHider(Activity activity, View anchorView) {
        mActivity = activity;
        mAnchorView = anchorView;
    }

    /**
     * Returns whether or not the controller is visible.
     */
    public boolean isVisible(){
        return mVisible;
    };

    /**
     * Hide the system UI.
     */
    public void hide(){
        mVisible = false;
        mOnVisibilityChangeListener.onVisibilityChange(mVisible);
    };

    /**
     * Show the system UI.
     */
    public void show(){
        mVisible = true;
        mOnVisibilityChangeListener.onVisibilityChange(mVisible);
    };

    /**
     * Toggle the visibility of the system UI.
     */
    public void toggle() {
        if (isVisible()) {
            hide();
        } else {
            show();
        }
    }

    /**
     * Registers a callback, to be triggered when the system UI visibility
     * changes.
     */
    public void setOnVisibilityChangeListener(OnVisibilityChangeListener listener) {
        if (listener == null) {
            listener = sDummyListener;
        }

        mOnVisibilityChangeListener = listener;
    }

    /**
     * A dummy no-op callback for use when there is no other listener set.
     */
    private static OnVisibilityChangeListener sDummyListener = new OnVisibilityChangeListener() {
        @Override
        public void onVisibilityChange(boolean visible) {
        }
    };

    /**
     * A callback interface used to listen for system UI visibility changes.
     */
    public interface OnVisibilityChangeListener {
        /**
         * Called when the system UI visibility has changed.
         * 
         * @param visible True if the system UI is visible.
         */
        public void onVisibilityChange(boolean visible);
    }
}
