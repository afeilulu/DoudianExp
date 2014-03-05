package cn.com.xinli.android.doudian.IoHandler;

import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;

import org.apache.mina.core.buffer.IoBuffer;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.future.IoFutureListener;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.transport.socket.DatagramSessionConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.util.zip.DataFormatException;
import java.util.zip.Inflater;

import cn.com.xinli.android.doudian.service.SyncService;

public abstract class AbstractIoHandler extends IoHandlerAdapter {
	
	private final static Logger LOGGER = LoggerFactory.getLogger(AbstractIoHandler.class);
	private static final String KEY = "name_of_method";
	private static final int PACKAGE_LENGTH = 1450;
    private static final int MAX_LENGTH = 5120;
	protected static final Charset CHARSET = Charset.forName("UTF-8");
	public final static int CONNECT_TIMEOUT=10000;// ms
	private final static int SESSION_IDLE_TIMEOUT = 10;// second

    protected Intent mIntent;
    protected ResultReceiver mReceiver;
    protected String mKey;
    protected IoConnector connector;
    
    protected void run(final IoConnector connector, InetSocketAddress inetSocketAddress){
    	
    	ConnectFuture connFuture = connector.connect(inetSocketAddress);
		connFuture.awaitUninterruptibly();
		connFuture.addListener(new IoFutureListener<ConnectFuture>() {
		        public void operationComplete(ConnectFuture future) {
		            if (future.isConnected()) {
		                LOGGER.debug("...connected");
		                
		                IoSession session = future.getSession();
		                session.setAttribute(KEY, mKey);
		                
		                DatagramSessionConfig cfg = ((DatagramSessionConfig) session
		                        .getConfig());
		                cfg.setBothIdleTime(SESSION_IDLE_TIMEOUT);
		                cfg.setCloseOnPortUnreachable(true);
		                
	                	IoBuffer buffer = IoBuffer.allocate(PACKAGE_LENGTH);
	            		try {
	            			buffer.putString(mIntent.getStringExtra(Intent.EXTRA_UID), CHARSET.newEncoder());
	            		} catch (CharacterCodingException e) {
	            			// TODO Auto-generated catch block
	            			e.printStackTrace();
	            		}
	            		buffer.flip();
	            		session.write(buffer);
	            		
		            } else {
		                LOGGER.error("Not connected...exiting");
		            }
		        }
		        
		    });
		
    }

    /**
     * decompress byte[]
     * @param buffer
     * @return
     * @throws DataFormatException
     * @throws UnsupportedEncodingException
     */
    protected String getResult(IoBuffer buffer) throws DataFormatException, UnsupportedEncodingException {
        // decompress the bytes
        byte[] output = new byte[PACKAGE_LENGTH];
        buffer.get(output);
        Inflater decompresser = new Inflater();
        decompresser.setInput(output, 0, PACKAGE_LENGTH);
        byte[] result = new byte[MAX_LENGTH];
        int resultLength = 0;
        resultLength = decompresser.inflate(result);
        decompresser.end();
        return new String(result,0,resultLength,"UTF-8");
    }
	
	@Override
	public void exceptionCaught(IoSession session, Throwable cause)
          throws Exception {
       cause.printStackTrace();
       
       sendError();
       
       session.close(true);
       connector.dispose();
    }
 
    @Override
    public void messageReceived(IoSession session, Object message)
            throws Exception {
         LOGGER.debug("Session recv...");
    }

    @Override
    public void messageSent(IoSession session, Object message) throws Exception{
        LOGGER.debug("Message sent...");
    }

    @Override
    public void sessionClosed(IoSession session) throws Exception {
        LOGGER.debug("Session closed...");
    }

    @Override
    public void sessionCreated(IoSession session) throws Exception {
        LOGGER.debug("Session created...");
    }

    @Override
    public void sessionIdle(IoSession session, IdleStatus status)
            throws Exception {
        LOGGER.debug("Session idle...");
        
        sendError();
        
        // disconnect an idle client
        session.close(true);
        connector.dispose();
    }

    @Override
    public void sessionOpened(IoSession session) throws Exception {
        LOGGER.debug("Session opened...");
    }
    
    private void sendError(){
         Bundle bundle = new Bundle();
         bundle.putString(Intent.EXTRA_TEXT, mKey);
         mReceiver.send(SyncService.STATUS_ERROR, bundle);
    }

}
