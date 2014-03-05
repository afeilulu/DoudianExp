package cn.com.xinli.android.doudian.io;

import java.io.File;
import java.util.concurrent.ExecutionException;

import cn.com.xinli.android.doudian.utils.BitmapUtilsTask;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

public class RankItem implements Parcelable{
	
	private String mTitle;
	private String mId;
	private String mPosterUrl;
	
	private boolean isLoaded;
	private Bitmap mBitmap;
	private File bitmapFile;

	public RankItem(Context context, String title, String id ,String posterUrl){
		this.mTitle = title;
		this.mId = id;
		this.mPosterUrl = posterUrl;
//		mBitmap = getBitmap();
		final File cacheDir = context.getCacheDir();
        bitmapFile = new File(cacheDir, "k" + mId + ".jpg");
        
	}

	public String getmTitle() {
		return mTitle;
	}

	public void setmTitle(String mTitle) {
		this.mTitle = mTitle;
	}

	public String getmId() {
		return mId;
	}

	public void setmId(String mId) {
		this.mId = mId;
	}

	public String getmPosterUrl() {
		return mPosterUrl;
	}

	public void setmPosterUrl(String mPosterUrl) {
		this.mPosterUrl = mPosterUrl;
	}

	@Override
	public int describeContents() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void writeToParcel(Parcel arg0, int arg1) {
		arg0.writeString(mTitle);
		arg0.writeString(mId);
		arg0.writeString(mPosterUrl);
		
	}
	
	/*public Bitmap getBitmap() {
        if (mBitmap != null)
            return mBitmap;

        try {
            mBitmap = new BitmapUtilsTask().execute(mPosterUrl, "thumb").get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return mBitmap;
    }*/
	
	public Bitmap getLargeBitmap(int optSize) {
        Bitmap b = null;
        try {
            b = new BitmapUtilsTask().execute(mPosterUrl, "large",bitmapFile,optSize).get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return b;

    }

    public void loadLargeBitmap() {
        if (!isLoaded) {
            new BitmapUtilsTask().execute(mPosterUrl, "load",bitmapFile);
            isLoaded = true;
        }
    }
	
	public void clear() {
        if (mBitmap != null)
            mBitmap.recycle();
        mBitmap = null;
    }
	
}
