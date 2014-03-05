package cn.com.xinli.android.doudian.service;

import android.content.Intent;

import org.apache.mina.core.service.IoHandlerAdapter;

import cn.com.xinli.android.doudian.IoHandler.GetSpecialChannelsByPageIoHandler;

public class GetSpecialChannelsByPage extends SyncService {

	@Override
	protected String getKey() {
		return GetSpecialChannelsByPage.class.getSimpleName();
	}

	@Override
	protected IoHandlerAdapter getIoHandler(Intent intent) {
		return new GetSpecialChannelsByPageIoHandler(intent);
	}

}
