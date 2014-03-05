package cn.com.xinli.android.doudian.service;

import android.content.Intent;

import org.apache.mina.core.service.IoHandlerAdapter;

import cn.com.xinli.android.doudian.IoHandler.GetProgramInAreaCategoryByPageIoHandler;
import cn.com.xinli.android.doudian.IoHandler.GetProgramInCategoryByPageIoHandler;

public class GetProgramInAreaCategoryByPage extends SyncService {

	@Override
	protected String getKey() {
		return GetProgramInAreaCategoryByPage.class.getSimpleName();
	}

	@Override
	protected IoHandlerAdapter getIoHandler(Intent intent) {
		return new GetProgramInAreaCategoryByPageIoHandler(intent);
	}

}
