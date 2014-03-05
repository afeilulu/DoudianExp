package cn.com.xinli.android.doudian.service;

import android.content.Intent;

import org.apache.mina.core.service.IoHandlerAdapter;

import cn.com.xinli.android.doudian.IoHandler.GetTotalOfSpecialChannelsIoHandler;

public class GetTotalOfSpecialChannels extends SyncService {

	@Override
	protected String getKey() {
		return GetTotalOfSpecialChannels.class.getSimpleName();
	}

	@Override
	protected IoHandlerAdapter getIoHandler(Intent intent) {
		return new GetTotalOfSpecialChannelsIoHandler(intent);
	}

}
