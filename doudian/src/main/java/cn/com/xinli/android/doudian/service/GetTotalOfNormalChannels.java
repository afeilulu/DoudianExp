package cn.com.xinli.android.doudian.service;

import android.content.Intent;

import org.apache.mina.core.service.IoHandlerAdapter;

import cn.com.xinli.android.doudian.IoHandler.GetTotalOfNormalChannelsIoHandler;

public class GetTotalOfNormalChannels extends SyncService {

	@Override
	protected String getKey() {
		return GetTotalOfNormalChannels.class.getSimpleName();
	}

	@Override
	protected IoHandlerAdapter getIoHandler(Intent intent) {
		return new GetTotalOfNormalChannelsIoHandler(intent);
	}

}
