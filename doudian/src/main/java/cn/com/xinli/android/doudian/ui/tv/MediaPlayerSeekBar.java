package cn.com.xinli.android.doudian.ui.tv;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.SeekBar;
public class MediaPlayerSeekBar extends SeekBar {

	private static final String TAG = "MediaPlayerSeekBar";

    private onChange SeekBarChange;

    /**
     * 进度改变接口
     * @author terry
     *
     */
    public interface onChange {
        public void onStopTrackingTouch(MediaPlayerSeekBar seekBar);

        public void onStartTrackingTouch(MediaPlayerSeekBar seekBar);

        public void onProgressChanged(MediaPlayerSeekBar seekBar, int progress,
                boolean fromUser);
    }

     
    
    public MediaPlayerSeekBar(Context context) {
        this(context, null);
        // TODO Auto-generated constructor stub
    }

    public MediaPlayerSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        //this.setOnTouchListener(this);
        this.setOnSeekBarChangeListener(new OnSeekBarChangeListener() {

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
                if (SeekBarChange != null) {
                    SeekBarChange.onStopTrackingTouch(MediaPlayerSeekBar.this);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // TODO Auto-generated method stub
                if (SeekBarChange != null) {
                    SeekBarChange.onStartTrackingTouch(MediaPlayerSeekBar.this);
                }
            }

            @Override
            public void onProgressChanged(final SeekBar seekBar,
                    final int progress, boolean fromUser) {
                if (SeekBarChange != null) {
                    SeekBarChange.onProgressChanged(MediaPlayerSeekBar.this, progress,
                            fromUser);
                }
            }
        });
       
    }
    
    private int mKeyProgressIncrement = 30000;
    
    @Override
    public synchronized void setMax(int max) {
        super.setMax(max);

        setKeyProgressIncrement(30000);
       
    }

   

    /**
     * 设置进度改变事件
     * @param change
     */
    public void setOnSeekBarChange(onChange change) {
        this.SeekBarChange = change;
    }


}
