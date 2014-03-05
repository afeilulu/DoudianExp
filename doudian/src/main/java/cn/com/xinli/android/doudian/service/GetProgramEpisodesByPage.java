package cn.com.xinli.android.doudian.service;

import android.content.Intent;

import org.apache.mina.core.service.IoHandlerAdapter;

import cn.com.xinli.android.doudian.IoHandler.GetProgramEpisodesByPageIoHandler;
import cn.com.xinli.android.doudian.IoHandler.GetProgramSourcesIoHandler;

public class GetProgramEpisodesByPage extends SyncService {

	@Override
	protected String getKey() {
		return GetProgramEpisodesByPage.class.getSimpleName();
	}

	@Override
	protected IoHandlerAdapter getIoHandler(Intent intent) {
		return new GetProgramEpisodesByPageIoHandler(intent);
	}

}
