package cn.com.xinli.android.doudian.service;

import android.content.Intent;

import org.apache.mina.core.service.IoHandlerAdapter;

import cn.com.xinli.android.doudian.IoHandler.GetProgramRelatedIoHandler;
import cn.com.xinli.android.doudian.IoHandler.GetProgramSourcesIoHandler;

public class GetProgramRelated extends SyncService {

	@Override
	protected String getKey() {
		return GetProgramRelated.class.getSimpleName();
	}

	@Override
	protected IoHandlerAdapter getIoHandler(Intent intent) {
		return new GetProgramRelatedIoHandler(intent);
	}

}
