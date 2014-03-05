package cn.com.xinli.android.doudian.service;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import org.apache.mina.core.service.IoHandlerAdapter;

public abstract class SyncService extends IntentService {
	private static final String TAG = "SyncService";
	public static final String EXTRA_STATUS_RECEIVER = "STATUS_RECEIVER";

	public static final int STATUS_RUNNING = 0x1;
	public static final int STATUS_ERROR = 0x2;
	public static final int STATUS_FINISHED = 0x100;
	
	public static final String SERVER_IP_ADDRESS = "192.168.4.189";
	public static final int SERVER_PORT = 9876;

	public SyncService() {
		super(TAG);
	}

	protected abstract String getKey();
	
	protected abstract IoHandlerAdapter getIoHandler(Intent intent);

	@Override
	public void onCreate() {
		super.onCreate();
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		Log.d(TAG, Thread.currentThread().getId() + " onHandleIntent(intent=" + intent.toString() + ")");
		
		final ResultReceiver receiver = intent
				.getParcelableExtra(EXTRA_STATUS_RECEIVER);
		
		if (receiver != null){
			final Bundle bundle = new Bundle();
			bundle.putString(Intent.EXTRA_TEXT, getKey());
			// return conditions
			bundle.putString(Intent.EXTRA_UID, intent.getStringExtra(Intent.EXTRA_UID));
			receiver.send(STATUS_RUNNING, bundle);
		}
		
		getIoHandler(intent);
		
	}
}
