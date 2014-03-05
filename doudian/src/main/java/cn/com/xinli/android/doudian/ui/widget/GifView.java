package cn.com.xinli.android.doudian.ui.widget;

import java.io.InputStream;

import cn.com.xinli.android.doudian.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Movie;
import android.net.Uri;
import android.util.AttributeSet;
import android.view.View;

public class GifView extends View {
	
	private Movie mMovie;
	private long movieStart;
	private int gifId;

	public GifView(Context context) {
	    super(context);
	    initializeView();
	}

	public GifView(Context context, AttributeSet attrs) {
	    super(context, attrs);
	    setAttrs(attrs);
	    initializeView();
	}

	public GifView(Context context, AttributeSet attrs, int defStyle) {
	    super(context, attrs, defStyle);
	    setAttrs(attrs);
	    initializeView();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
	    canvas.drawColor(Color.TRANSPARENT);
	    super.onDraw(canvas);
	    long now = android.os.SystemClock.uptimeMillis();
	    if (movieStart == 0) {
	        movieStart = now;
	    }
	    if (mMovie != null) {
	        int relTime = (int) ((now - movieStart) % mMovie.duration());
	        mMovie.setTime(relTime);
	        mMovie.draw(canvas, getWidth() - mMovie.width(), getHeight() - mMovie.height());
	        this.invalidate();
	    }
	}
	
	public void setGIFResource(int resId) {
	    this.gifId = resId;
	    initializeView();
	}

	public int getGIFResource() {
	    return this.gifId;
	}

	private void initializeView() {
	    if (gifId != 0) {
	        InputStream is = getContext().getResources().openRawResource(gifId);
	        mMovie = Movie.decodeStream(is);
	        movieStart = 0;
	        this.invalidate();
	    }
	}
	
	private void setAttrs(AttributeSet attrs) {
	    if (attrs != null) {
	        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.GifView, 0, 0);
	        String gifSource = a.getString(R.styleable.GifView_src);
	        //little workaround here. Who knows better approach on how to easily get resource id - please share
	        String sourceName = Uri.parse(gifSource).getLastPathSegment().replace(".gif", "");
	        setGIFResource(getResources().getIdentifier(sourceName, "drawable", getContext().getPackageName()));
	        a.recycle();
	    }
	}
}
