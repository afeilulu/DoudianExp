package cn.com.xinli.android.doudian.service;

import android.content.Intent;

import org.apache.mina.core.service.IoHandlerAdapter;

import cn.com.xinli.android.doudian.IoHandler.GetAreasIoHandler;
import cn.com.xinli.android.doudian.IoHandler.GetYearsIoHandler;

public class GetAreas extends SyncService {

	@Override
	protected String getKey() {
		return GetAreas.class.getSimpleName();
	}

	@Override
	protected IoHandlerAdapter getIoHandler(Intent intent) {
		return new GetAreasIoHandler(intent);
	}

}
