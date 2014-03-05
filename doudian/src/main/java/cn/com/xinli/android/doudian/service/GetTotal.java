package cn.com.xinli.android.doudian.service;

import android.content.Intent;

import org.apache.mina.core.service.IoHandlerAdapter;

import cn.com.xinli.android.doudian.IoHandler.GetTotalIoHandler;

public class GetTotal extends SyncService {

	@Override
	protected String getKey() {
		return GetTotal.class.getSimpleName();
	}

	@Override
	protected IoHandlerAdapter getIoHandler(Intent intent) {
		return new GetTotalIoHandler(intent);
	}

}
