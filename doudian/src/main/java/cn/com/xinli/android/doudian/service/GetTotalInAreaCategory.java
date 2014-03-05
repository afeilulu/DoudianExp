package cn.com.xinli.android.doudian.service;

import android.content.Intent;

import org.apache.mina.core.service.IoHandlerAdapter;

import cn.com.xinli.android.doudian.IoHandler.GetTotalInAreaCategoryIoHandler;
import cn.com.xinli.android.doudian.IoHandler.GetTotalInCategoryIoHandler;

public class GetTotalInAreaCategory extends SyncService {

	@Override
	protected String getKey() {
		return GetTotalInAreaCategory.class.getSimpleName();
	}

	@Override
	protected IoHandlerAdapter getIoHandler(Intent intent) {
		return new GetTotalInAreaCategoryIoHandler(intent);
	}

}
