package cn.com.xinli.android.doudian.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.os.AsyncTask;
import android.os.Build;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import cn.com.xinli.android.doudian.R;

/**
 * This helper class download images from the Internet and binds those with the
 * provided ImageView.
 * 
 * <p>
 * It requires the INTERNET permission, which should be added to your
 * application's manifest file.
 * </p>
 * 
 * A local cache of downloaded images is maintained internally to improve
 * performance.
 */
public class ImageDownloaderLruCache {

	private static final String LOG_TAG = "ImageDownloaderLruCache";
	private Context mContext;
	private ImageObjectCache mImageObjectCache;
	private Bitmap mLoadingBitmap;
	private boolean mFadeInBitmap = false;
	private static final int FADE_IN_TIME = 300;
	private boolean mExitTasksEarly = false;
	private int mImageThumbSize;
	private int mImageThumbSize2=64;
	
	public static final String ITEM_MORE = "item_more";
	public static final String FAVORITE_DEFAULT = "favorite_default";
	public static final String LATEST_DEFAULT = "latest_default";

	public ImageDownloaderLruCache(Context context) {
		super();
		this.mContext = context;
		mImageThumbSize = mContext.getResources().getDimensionPixelSize(
				R.dimen.image_thumbnail_size);
//		this.businessDataSerch = new BusinessDataSerch(mContext);
	}

	public void setImageThumbSize(int size) {
		mImageThumbSize2 = size;
	}
	
	public void setFadeInBitmap(boolean flag){
		mFadeInBitmap = flag;
	}

	/**
	 * Set the {@link ImageObjectCache} object to use with this ImageWorker.
	 * 
	 * @param cacheCallback
	 */
	public void setImageObjectCache(ImageObjectCache cacheCallback) {
		mImageObjectCache = cacheCallback;
	}

	/**
	 * Download the specified image from the Internet and binds it to the
	 * provided ImageView. The binding is immediate if the image is found in the
	 * cache and will be done asynchronously otherwise. A null bitmap will be
	 * associated to the ImageView if an error occurs.
	 * 
	 * @param url
	 *            The URL of the image to download.
	 * @param imageView
	 *            The ImageView to bind the downloaded image to.
	 */
	public void download(String url, ImageView imageView) {
	    download(url, imageView, null);
	}


	/**
	 * Same as {@link #download(String, ImageView)}, with the possibility to
	 * provide an additional cookie that will be used when the image will be
	 * retrieved.
	 * 
	 * @param url
	 *            The URL of the image to download.
	 * @param imageView
	 *            The ImageView to bind the downloaded image to.
	 * @param cookie
	 *            A cookie String that will be used by the http connection.
	 */
	public void download(String url, ImageView imageView, String cookie) {

        url = preFormatUrl(url);

        Bitmap bitmap = getBitmapFromCache(url);

        if (bitmap == null) {
            forceDownload(url, imageView, cookie);
        } else {
            cancelPotentialDownload(url, imageView);
            imageView.setImageBitmap(bitmap);
        }
	}

	/*
	 * Same as download but the image is always downloaded and the cache is not
	 * used. Kept private at the moment as its interest is not clear. private
	 * void forceDownload(String url, ImageView view) { forceDownload(url, view,
	 * null); }
	 */

	/**
	 * Same as download but the image is always downloaded and the cache is not
	 * used. Kept private at the moment as its interest is not clear.
	 */
	private void forceDownload(String url, ImageView imageView, String cookie) {
		// State sanity: url is guaranteed to never be null in
		// DownloadedDrawable and cache keys.
		if (url == null) {
			imageView.setImageDrawable(null);
			return;
		}

		if (cancelPotentialDownload(url, imageView)) {
			BitmapDownloaderTask task = new BitmapDownloaderTask(imageView);
			AsyncDrawable downloadedDrawable = new AsyncDrawable(
					mContext.getResources(), mLoadingBitmap, task);
			imageView.setImageDrawable(downloadedDrawable);
//			task.execute(url, menuId, cookie);
			if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.HONEYCOMB_MR1) {
				task.execute(url, cookie);
	        } else {
	        	ThreadPoolExecutor tpe = (ThreadPoolExecutor) AsyncTask.THREAD_POOL_EXECUTOR;
	        	tpe.setMaximumPoolSize(512);
	        	task.executeOnExecutor(tpe,url, cookie);
	        }
		}
	}

	/**
	 * Returns true if the current download has been canceled or if there was no
	 * download in progress on this image view. Returns false if the download in
	 * progress deals with the same url. The download is not stopped in that
	 * case.
	 */
	private static boolean cancelPotentialDownload(String url,
			ImageView imageView) {
		BitmapDownloaderTask bitmapDownloaderTask = getBitmapDownloaderTask(imageView);

		if (bitmapDownloaderTask != null) {
			String bitmapUrl = bitmapDownloaderTask.url;
			if ((bitmapUrl == null) || (!bitmapUrl.equals(url))) {
				bitmapDownloaderTask.cancel(true);
			} else {
				// The same URL is already being downloaded.
				return false;
			}
		}
		return true;
	}

	/**
	 * @param imageView
	 *            Any imageView
	 * @return Retrieve the currently active download task (if any) associated
	 *         with this imageView. null if there is no such task.
	 */
	private static BitmapDownloaderTask getBitmapDownloaderTask(
			ImageView imageView) {
		if (imageView != null) {
			Drawable drawable = imageView.getDrawable();
			if (drawable instanceof AsyncDrawable) {
				AsyncDrawable downloadedDrawable = (AsyncDrawable) drawable;
				return downloadedDrawable.getBitmapDownloaderTask();
			}
		}
		return null;
	}

	/**
	 * @param url
	 *            The URL of the image that will be retrieved from the cache.
	 * @return The cached bitmap or null if it was not found.
	 */
	private Bitmap getBitmapFromCache(String url) {
		CachedObject cachedObject = mImageObjectCache
				.getCachedObjectFromMemCache(url);
		if (cachedObject != null)
			return mImageObjectCache.getCachedObjectFromMemCache(url)
					.getBitmap();
		else
			return null;
	}

	/**
	 * The actual AsyncTask that will asynchronously download the image.
	 */
	class BitmapDownloaderTask extends AsyncTask<String, Integer, Bitmap> {
		private static final int IO_BUFFER_SIZE = 4 * 1024;
		private String url;
		private String menuId;
		private final WeakReference<ImageView> imageViewReference;

		public BitmapDownloaderTask(ImageView imageView) {
			imageViewReference = new WeakReference<ImageView>(imageView);
		}

		/**
		 * Actual download method.
		 */
		@Override
		protected Bitmap doInBackground(String... params) {
			
			url = params[0];
			
//			Log.d("url", url);
			
			Bitmap bitmap = null;

			// If the image cache is available and this task has not been
			// cancelled by another
			// thread and the ImageView that was originally bound to this task
			// is still bound back
			// to this task and our "exit early" flag is not set then try and
			// fetch the bitmap from
			// the cache
			if (mImageObjectCache != null && !isCancelled()
					&& getAttachedImageView() != null && !mExitTasksEarly) {
				try {
					bitmap = mImageObjectCache.getBitmapFromDiskCache(url,
							mImageThumbSize2);
				} catch (Exception e) {
					e.printStackTrace();
					bitmap = null;
				}
			}

			// If the bitmap was not found in the cache and this task has not
			// been cancelled by
			// another thread and the ImageView that was originally bound to
			// this task is still
			// bound back to this task and our "exit early" flag is not set,
			// then call the main
			// process method (as implemented by a subclass)
			if (bitmap == null && !isCancelled()
					&& getAttachedImageView() != null && !mExitTasksEarly) {

				if (url == null || TextUtils.isEmpty(url)){
					// no url
					if (mLoadingBitmap != null)
						bitmap = mLoadingBitmap;
					else
						bitmap = null;
				}  else if (url.equals(ITEM_MORE)) {
					// get fixed image firstly
					BitmapDrawable drawable = (BitmapDrawable) mContext.getResources().getDrawable(R.drawable.item_more_recommend);
					if (drawable != null)
						bitmap = drawable.getBitmap();
					else
						bitmap = null;
				} else if (url.equals(FAVORITE_DEFAULT)) {
					// get fixed image firstly
					BitmapDrawable drawable = (BitmapDrawable) mContext.getResources().getDrawable(R.drawable.favorite_default);
					if (drawable != null)
						bitmap = drawable.getBitmap();
					else
						bitmap = null;
				} else if (url.equals(LATEST_DEFAULT)) {
					// get fixed image firstly
					BitmapDrawable drawable = (BitmapDrawable) mContext.getResources().getDrawable(R.drawable.latest_default);
					if (drawable != null)
						bitmap = drawable.getBitmap();
					else
						bitmap = null;
				} else {
				
					HttpURLConnection conn = null;
					InputStream is = null;
					try {
//						URL imageUrl = new URL(url);
//						conn = (HttpURLConnection) imageUrl
//								.openConnection();
//						conn.setConnectTimeout(5000);
//						conn.setReadTimeout(10000);
//						is = conn.getInputStream();
						
						final HttpParams httpParameters = new BasicHttpParams();
						// Set the timeout in milliseconds until a connection is established
						HttpConnectionParams.setConnectionTimeout(httpParameters, 3000);
						// Set the default socket timeout(SO_TIMEOUT)
						// in milliseconds which is the timeout for waiting for data
						HttpConnectionParams.setSoTimeout(httpParameters, 10000);
						
						HttpGet httpRequest = new HttpGet(url);
						HttpClient httpclient = new DefaultHttpClient(httpParameters);
						HttpResponse response = (HttpResponse) httpclient
						                    .execute(httpRequest);
						
						if (response.getStatusLine().getStatusCode() != 200){
							response = null;
							httpclient = null;
							httpRequest.abort();
							httpRequest = null;
							return null;
						}
						
						HttpEntity entity = response.getEntity();
						is = entity.getContent();
	
						if (is != null) {
							try {
								
								BitmapFactory.Options options = new BitmapFactory.Options();
								options.inJustDecodeBounds = true;
//								BitmapFactory.decodeStream(new BufferedInputStream(is), null, options);
								ByteArrayOutputStream baos = new ByteArrayOutputStream();
							    byte[] b = new byte[1024];
							    int len = 0;
							    while ((len = is.read(b, 0, 1024)) != -1) {
							    	baos.write(b, 0, len);
							    	baos.flush();
								}
								byte[] bytes = baos.toByteArray();
								BitmapFactory.decodeByteArray(bytes,0,bytes.length,options);
	
								// Calculate inSampleSize
								options.inSampleSize = calculateInSampleSize(
										options, mImageThumbSize2, mImageThumbSize2);
								// Decode bitmap with inSampleSize set
								options.inJustDecodeBounds = false;
								
//								options.inDither = false;
//			                    options.inScaled = false;
//			                    options.inPreferredConfig = Bitmap.Config.ARGB_8888;
//			                    options.inPurgeable = true;
	
								// we need getInputStram again,while using
								// BitmapFactiory.docodeStram();
								
//			                    httpRequest = new HttpGet(url);
//								httpclient = new DefaultHttpClient(httpParameters);
//								response = (HttpResponse) httpclient
//								                    .execute(httpRequest);
//								entity = response.getEntity();
//								is = entity.getContent();
//								bitmap = BitmapFactory.decodeStream(new BufferedInputStream(is), null,
//										options);
								
								bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length,options);
								
							} catch (Exception e) {
								Log.w(LOG_TAG,
										"Error while retrieving bitmap from " + url,
										e);
							} finally {
								if (is != null) {
									is.close();
								}
//								conn.disconnect();
								
								response=null;
								httpclient=null;
								httpRequest=null;
							}
						}
					} catch (IOException e) {
						Log.w(LOG_TAG, "I/O error while retrieving bitmap from "
								+ url, e);
					} catch (IllegalStateException e) {
						Log.w(LOG_TAG, "Incorrect URL: " + url);
					} catch (Exception e) {
						Log.w(LOG_TAG, "Error while retrieving bitmap from " + url,
								e);
					} finally {
						if (is != null) {
							try {
								is.close();
							} catch (IOException e) {
								e.printStackTrace();
							}
						}
//						conn.disconnect();
					}
				}
			}

			// If the bitmap was processed and the image cache is available,
			// then addRecent the processed
			// bitmap to the cache for future use. Note we don't check if the
			// task was cancelled
			// here, if it was, and the thread is still running, we may as well
			// addRecent the processed
			// bitmap to our cache as it might be used again in the future
			/*
			 * if (bitmap != null && mImageObjectCache != null) {
			 * mImageObjectCache.addBitmapToCache(url, bitmap); }
			 */

			if (menuId != null) {
				try {
//					videoInfo = businessDataSerch.getVideoInfo(menuId);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

			}

			if (bitmap != null && mImageObjectCache != null) {
				CachedObject cachedObject = new CachedObject();
				cachedObject.setBitmap(bitmap);
				mImageObjectCache.addBitmapToCache(url, cachedObject);
			}

			return bitmap;
		}

		/**
		 * Once the image is downloaded, associates it to the imageView
		 */
		@Override
		protected void onPostExecute(Bitmap bitmap) {
			if (isCancelled() || mExitTasksEarly) {
				bitmap = null;
			} else {
				final ImageView imageView = getAttachedImageView();
				if (bitmap != null && imageView != null) {
					setImageBitmap(imageView, bitmap);
				}
			}
		}

		public void copy(InputStream in, OutputStream out) throws IOException {
			byte[] b = new byte[IO_BUFFER_SIZE];
			int read;
			while ((read = in.read(b)) != -1) {
				out.write(b, 0, read);
			}
		}

		/**
		 * Returns the ImageView associated with this task as long as the
		 * ImageView's task still points to this task as well. Returns null
		 * otherwise.
		 */
		private ImageView getAttachedImageView() {
			final ImageView imageView = imageViewReference.get();
			final BitmapDownloaderTask bitmapDownloaderTask = getBitmapDownloaderTask(imageView);

			if (this == bitmapDownloaderTask) {
				return imageView;
			}

			return null;
		}

	}

	/**
	 * A custom Drawable that will be attached to the imageView while the work
	 * is in progress. Contains a reference to the actual worker task, so that
	 * it can be stopped if a new binding is required, and makes sure that only
	 * the last started worker process can bind its result, independently of the
	 * finish order.
	 */
	private static class AsyncDrawable extends BitmapDrawable {
		private final WeakReference<BitmapDownloaderTask> bitmapWorkerTaskReference;

		public AsyncDrawable(Resources res, Bitmap bitmap,
				BitmapDownloaderTask bitmapDownloaderTask) {
			super(res, bitmap);

			bitmapWorkerTaskReference = new WeakReference<BitmapDownloaderTask>(
					bitmapDownloaderTask);
		}

		public BitmapDownloaderTask getBitmapDownloaderTask() {
			return bitmapWorkerTaskReference.get();
		}
	}

	/**
	 * 
	 * @param options
	 * @param reqWidth
	 * @param reqHeight
	 * @return
	 */
	public static int calculateInSampleSize(BitmapFactory.Options options,
			int reqWidth, int reqHeight) {
		/*// Raw height and width of image
		 int height = options.outHeight;
		 int width = options.outWidth;
		int inSampleSize = 1;
		int desiredWidth = reqWidth;
		int desiredHeight = reqHeight;
//		Log.i(LOG_TAG, "width:"+width+"        height:"+height);
		if(width>height){
			if (desiredWidth > width)
				desiredWidth = width;
			while (width / 2 > desiredWidth) {
				width /= 2;
				height /= 2;
				inSampleSize *= 2;
			}
		}else{
			if (desiredHeight > height)
				desiredHeight = height;
			while (height / 2 > desiredHeight) {
				width /= 2;
				height /= 2;
				inSampleSize *= 2;
			}
		}*/
		
		int srcWidth = options.outWidth;
        int srcHeight = options.outHeight;

        // Only scale if the source is big enough. This code is just trying
        // to fit a image into a certain width.
        if (reqWidth > srcWidth)
        	reqWidth = srcWidth;

        // Calculate the correct inSampleSize/scale value. This helps reduce
        // memory use. It should be a power of 2
        int inSampleSize = 1;
        while (srcWidth / 2 > reqWidth) {
            srcWidth /= 2;
            srcHeight /= 2;
            inSampleSize *= 2;
        }

		return inSampleSize;
	}

	/**
	 * Set placeholder bitmap that shows when the the background thread is
	 * running.
	 * 
	 * @param bitmap
	 */
	public void setLoadingImage(Bitmap bitmap) {
		mLoadingBitmap = bitmap;
	}

	/**
	 * Set placeholder bitmap that shows when the the background thread is
	 * running.
	 * 
	 * @param resId
	 */
	public void setLoadingImage(int resId) {
		mLoadingBitmap = BitmapFactory.decodeResource(mContext.getResources(),
				resId);
	}

	/**
	 * Called when the processing is complete and the final bitmap should be set
	 * on the ImageView.
	 * 
	 * @param imageView
	 * @param bitmap
	 */
	private void setImageBitmap(ImageView imageView, Bitmap bitmap) {

		if (mFadeInBitmap) {
			// Transition drawable with a transparent drwabale and the final
			// bitmap
			final TransitionDrawable td = new TransitionDrawable(
					new Drawable[] {
							new ColorDrawable(android.R.color.transparent),
							new BitmapDrawable(mContext.getResources(), bitmap) });
			// Set background to loading bitmap
			imageView.setBackgroundDrawable(new BitmapDrawable(mContext
					.getResources(), mLoadingBitmap));

			imageView.setImageDrawable(td);
			td.startTransition(FADE_IN_TIME);
		} else {
			imageView.setImageBitmap(bitmap);
		}
		
		imageView.setTag("loaded"); // indicate bitmap has loaded
		imageView.invalidate();
	}

	public void setExitTasksEarly(boolean exitTasksEarly) {
		mExitTasksEarly = exitTasksEarly;
	}

    protected String preFormatUrl(String url){

        if (!url.startsWith("http://")){
            url = "http://" + url;
        }

        return url.replaceAll(" ","20%");
    }

	/**
	 * The class that will provide the correct implementation of the skip method
	 * this class extends the FilterInputStream
	 *
	 * @author Deep Shah
	 */
	/*private static class FlushedInputStream extends FilterInputStream {
	 
	    *//**
	     * The constructor that takes in the InputStream reference.
	     *
	     * @param inputStream the input stream reference.
	     *//*
	    public FlushedInputStream(final InputStream inputStream) {
	        super(inputStream);
	    }
	 
	    *//**
	     * Overriding the skip method to actually skip n bytes.
	     * This implementation makes sure that we actually skip 
	     * the n bytes no matter what.
	     * {@inheritDoc}
	     *//*
	    @Override
	    public long skip(final long n) throws IOException {
	        long totalBytesSkipped = 0L;
	        //If totalBytesSkipped is equal to the required number 
	        //of bytes to be skipped i.e. "n"
	        //then come out of the loop.
	        while (totalBytesSkipped < n) {
	            //Skipping the left out bytes.
	            long bytesSkipped = in.skip(n - totalBytesSkipped);
	            //If number of bytes skipped is zero then 
	            //we need to check if we have reached the EOF
	            if (bytesSkipped == 0L) {
	                //Reading the next byte to find out whether we have reached EOF.
	                int bytesRead = read();
	                //If bytes read count is less than zero (-1) we have reached EOF.
	                //Cant skip any more bytes.
	                if (bytesRead < 0) {
	                    break;  // we reached EOF
	                } else {
	                    //Since we read one byte we have actually 
	                    //skipped that byte hence bytesSkipped = 1
	                    bytesSkipped = 1; // we read one byte
	                }
	            }
	            //Adding the bytesSkipped to totalBytesSkipped
	            totalBytesSkipped += bytesSkipped;
	        }        
	        return totalBytesSkipped;
	    }
	}*/
}
