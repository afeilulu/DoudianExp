package cn.com.xinli.android.doudian.service;

import android.content.Intent;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.apache.mina.core.service.IoHandlerAdapter;

import cn.com.xinli.android.doudian.IoHandler.GetProgramDetailIoHandler;
import cn.com.xinli.android.doudian.model.Method;

public class GetProgramDetail extends SyncService {

	@Override
	protected String getKey() {
		return GetProgramDetail.class.getSimpleName();
	}

	@Override
	protected IoHandlerAdapter getIoHandler(Intent intent) {
		return new GetProgramDetailIoHandler(intent);
	}

}
