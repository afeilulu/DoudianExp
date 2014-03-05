package cn.com.xinli.android.doudian.service;

import android.content.Intent;

import org.apache.mina.core.service.IoHandlerAdapter;

import cn.com.xinli.android.doudian.IoHandler.GetNormalChannelsByPageIoHandler;

public class GetNormalChannelsByPage extends SyncService {

	@Override
	protected String getKey() {
		return GetNormalChannelsByPage.class.getSimpleName();
	}

	@Override
	protected IoHandlerAdapter getIoHandler(Intent intent) {
		return new GetNormalChannelsByPageIoHandler(intent);
	}

}
