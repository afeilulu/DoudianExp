package cn.com.xinli.android.doudian.IoHandler;

import android.content.Intent;
import android.os.Bundle;

import com.google.gson.Gson;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.transport.socket.nio.NioDatagramConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.ArrayList;

import cn.com.xinli.android.doudian.model.Channel;
import cn.com.xinli.android.doudian.service.SyncService;

public class GetSpecialChannelsByPageIoHandler extends AbstractIoHandler {

	private final static Logger LOGGER = LoggerFactory.getLogger(GetSpecialChannelsByPageIoHandler.class);

	public GetSpecialChannelsByPageIoHandler(Intent intent){
		mIntent = intent;
		
		mReceiver = mIntent
 				.getParcelableExtra(SyncService.EXTRA_STATUS_RECEIVER);
    	mKey = mIntent.getStringExtra(Intent.EXTRA_TEXT);
    	
    	/*if (UIUtils.connectors == null){
    		UIUtils.connectors = new HashMap<String,IoConnector>();
    	}
    	
    	connector = UIUtils.connectors.get(mKey);
    	if (connector == null){
    		connector = new NioDatagramConnector();
    		connector.setConnectTimeoutMillis(CONNECT_TIMEOUT);
    		connector.setHandler(this);
//    		connector.getFilterChain().addLast("logger", new LoggingFilter());
    		UIUtils.connectors.put(mKey, connector);
    	} else {
    		LOGGER.debug("get existed connector");
    		
    		connector.dispose();
    		connector = new NioDatagramConnector();
    		connector.setConnectTimeoutMillis(CONNECT_TIMEOUT);
    		connector.setHandler(this);
    		UIUtils.connectors.put(mKey, connector);
    	}*/
        connector = new NioDatagramConnector();
        connector.setConnectTimeoutMillis(CONNECT_TIMEOUT);
        connector.setHandler(this);

		InetSocketAddress inetSocketAddress = new InetSocketAddress(SyncService.SERVER_IP_ADDRESS, SyncService.SERVER_PORT);
		
		run(connector, inetSocketAddress);
		
	}
	
	@Override
    public void messageReceived(IoSession session, Object message)
            throws Exception {
		LOGGER.debug("Session recv...");
		if (message instanceof IoBuffer) {
 			IoBuffer buffer = (IoBuffer) message;
// 			String result = buffer.getString(CHARSET.newDecoder());
            String result = getResult(buffer);
// 			System.out.println("Received:" + str);
// 			LOGGER.debug("parameter " + session.getAttribute(KEY));
 			
	        result = new Gson().fromJson(result, String.class);
	 		ArrayList<Channel> list = new ArrayList<Channel>();
	 		String[] results = result.split(";");
	 		for (int i = 0; i < results.length; i++) {
				Channel channel = new Channel();
				channel.setId(results[i].split(":")[0]);
				channel.setName(results[i].split(":")[1]);
	 			list.add(channel);
	 		}
	 		
	 		if (mReceiver != null) {
	 			Bundle bundle = new Bundle();
	 			bundle.putSerializable(mKey, list);
	 			bundle.putString(Intent.EXTRA_TEXT, mKey);
	 			bundle.putString(Intent.EXTRA_UID, mIntent.getStringExtra(Intent.EXTRA_UID));
                // for time stamp
                bundle.putLong(Intent.EXTRA_REFERRER, mIntent.getLongExtra(Intent.EXTRA_REFERRER,0));
	 			mReceiver.send(SyncService.STATUS_FINISHED, bundle);
	 		}
	 		
        } 
		session.close(true);
        connector.dispose();
    }

}
