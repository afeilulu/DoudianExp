package cn.com.xinli.android.doudian.service;

import android.content.Intent;

import org.apache.mina.core.service.IoHandlerAdapter;

import cn.com.xinli.android.doudian.IoHandler.GetTotalInCategoryIoHandler;

public class GetTotalInCategory extends SyncService {

	@Override
	protected String getKey() {
		return GetTotalInCategory.class.getSimpleName();
	}

	@Override
	protected IoHandlerAdapter getIoHandler(Intent intent) {
		return new GetTotalInCategoryIoHandler(intent);
	}

}
