package cn.com.xinli.android.doudian.service;

import android.content.Intent;

import org.apache.mina.core.service.IoHandlerAdapter;

import cn.com.xinli.android.doudian.IoHandler.GetCategoriesIoHandler;
import cn.com.xinli.android.doudian.IoHandler.GetYearsIoHandler;

public class GetYears extends SyncService {

	@Override
	protected String getKey() {
		return GetYears.class.getSimpleName();
	}

	@Override
	protected IoHandlerAdapter getIoHandler(Intent intent) {
		return new GetYearsIoHandler(intent);
	}

}
