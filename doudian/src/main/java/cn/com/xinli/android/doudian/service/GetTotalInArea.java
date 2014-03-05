package cn.com.xinli.android.doudian.service;

import android.content.Intent;

import org.apache.mina.core.service.IoHandlerAdapter;

import cn.com.xinli.android.doudian.IoHandler.GetTotalInAreaIoHandler;
import cn.com.xinli.android.doudian.IoHandler.GetTotalInCategoryIoHandler;

public class GetTotalInArea extends SyncService {

	@Override
	protected String getKey() {
		return GetTotalInArea.class.getSimpleName();
	}

	@Override
	protected IoHandlerAdapter getIoHandler(Intent intent) {
		return new GetTotalInAreaIoHandler(intent);
	}

}
