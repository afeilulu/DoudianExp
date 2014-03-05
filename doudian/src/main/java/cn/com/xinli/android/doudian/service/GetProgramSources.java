package cn.com.xinli.android.doudian.service;

import android.content.Intent;

import org.apache.mina.core.service.IoHandlerAdapter;

import cn.com.xinli.android.doudian.IoHandler.GetProgramDetailIoHandler;
import cn.com.xinli.android.doudian.IoHandler.GetProgramSourcesIoHandler;

public class GetProgramSources extends SyncService {

	@Override
	protected String getKey() {
		return GetProgramSources.class.getSimpleName();
	}

	@Override
	protected IoHandlerAdapter getIoHandler(Intent intent) {
		return new GetProgramSourcesIoHandler(intent);
	}

}
