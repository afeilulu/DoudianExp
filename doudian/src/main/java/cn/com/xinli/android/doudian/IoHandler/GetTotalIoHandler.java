package cn.com.xinli.android.doudian.IoHandler;

import android.content.Intent;
import android.os.Bundle;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.transport.socket.nio.NioDatagramConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

import cn.com.xinli.android.doudian.service.SyncService;


public class GetTotalIoHandler extends AbstractIoHandler {

	private final static Logger LOGGER = LoggerFactory.getLogger(GetTotalIoHandler.class);

	public GetTotalIoHandler(Intent intent){
		mIntent = intent;
		
		mReceiver = mIntent
 				.getParcelableExtra(SyncService.EXTRA_STATUS_RECEIVER);
    	mKey = mIntent.getStringExtra(Intent.EXTRA_TEXT);

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
 			
 			if (mReceiver != null) {
    			Bundle bundle = new Bundle();
    			bundle.putSerializable(mKey, result);
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
