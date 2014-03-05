package cn.com.xinli.android.doudian.service;

import android.content.Intent;

import org.apache.mina.core.service.IoHandlerAdapter;

import cn.com.xinli.android.doudian.IoHandler.GetProgramInAreaByPageIoHandler;
import cn.com.xinli.android.doudian.IoHandler.GetProgramInCategoryByPageIoHandler;

public class GetProgramInAreaByPage extends SyncService {

	@Override
	protected String getKey() {
		return GetProgramInAreaByPage.class.getSimpleName();
	}

	@Override
	protected IoHandlerAdapter getIoHandler(Intent intent) {
		return new GetProgramInAreaByPageIoHandler(intent);
	}

}
