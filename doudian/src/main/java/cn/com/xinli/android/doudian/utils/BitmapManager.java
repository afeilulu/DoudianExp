package cn.com.xinli.android.doudian.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.util.LruCache;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class BitmapManager {
	private static final String TAG = "BitmapManager";
	private LruCache<String, Bitmap> mMemoryCache;

	public BitmapManager() {
		mMemoryCache = new LruCache<String, Bitmap>(1024 * 1024 * 8) {
			@Override
			protected int sizeOf(String key, Bitmap bitmap) {
				return bitmap.getByteCount();
			}
		};

	}

	public Bitmap loadBitmap(String url) {
		return loadBitmap(url, 0, 0);
	}

	public Bitmap loadBitmap(String url, int width, int height) {
		Bitmap bitmap = getBitmapFromCache(url);
		if (bitmap == null) {
			Log.w(TAG, "downloadBitmap=" + url);
			bitmap = downloadBitmap(url, width, height);
		}
		return bitmap;
	}
	
	public void clearCache() {
		mMemoryCache.evictAll();
	}

	private Bitmap getBitmapFromCache(String url) {
		return mMemoryCache.get(url);
	}

	/**
	 * 下载图片-可指定显示图片的高宽
	 * 
	 * @param url
	 * @param width
	 * @param height
	 */
	private Bitmap downloadBitmap(String url, int width, int height) {
		InputStream input = null;
		HttpURLConnection conn = null;
		Bitmap bitmap = null;
		try {
			// http加载图片
			conn = (HttpURLConnection) new URL(url).openConnection();
			conn.setRequestMethod("GET");
			conn.setConnectTimeout(10000);
			conn.setReadTimeout(20000);
			if (conn.getResponseCode() == 200) {
				input = conn.getInputStream();
				bitmap = BitmapFactory.decodeStream(input);
				if (bitmap != null) {
					if (width > 0 && height > 0) {
						// 指定显示图片的高宽
						bitmap = Bitmap.createScaledBitmap(bitmap, width,
								height, true);
					}
					// 放入缓存
					mMemoryCache.put(url, bitmap);
				}
			}
		} catch (Exception e) {
			Log.e(TAG, "downloadBitmap()", e);
		} finally {
			try {
				input.close();
				conn.disconnect();
			} catch (IOException e) {
				Log.e(TAG, "close()", e);
			}
		}
		return bitmap;
	}
}