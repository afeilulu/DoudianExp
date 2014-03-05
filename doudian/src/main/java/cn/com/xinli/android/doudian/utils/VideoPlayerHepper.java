package cn.com.xinli.android.doudian.utils;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import cn.com.xinli.android.doudian.io.ProbeEpisode;
import cn.com.xinli.android.doudian.io.ProbeSectionUri;
public class VideoPlayerHepper {
	
	public final  static String PROBE_URI = "http://127.0.0.1:8089/videositeproxy/probe";
	public final  static String PROBE_M3U8_URI = "http://127.0.0.1:8089/videositeproxy/proxy.ts?videokey=";
//    private final  static String HTTP_SERVER_PATH = "/data/system/httpserver/mohohttpserver.sh";
    public final  static String PROBE_BUFFER = "http://127.0.0.1:8089/videositeproxy/proxy.ts?videokey=state&time=";
	
	private static final String TAG = "VideoPlayerHepper"; 
	
//	private static  HttpURLConnection  connect = null ;
	private static  int connect_is_closed  ;
	
	
	public static HttpURLConnection openConn(HttpURLConnection  connect, String connect_uri, boolean isGetBufferInfo){
		 //Log.i(TAG, "the connect uri is :" + connect_uri);
		 
		 try {
			URL myFileUrl  = new URL(connect_uri);
			 //打开连接，设置连接没有关闭的状态  即 connect_is_closed = 0 
			 connect = (HttpURLConnection) myFileUrl.openConnection();
			 connect_is_closed = 0;
			 
			 connect.setConnectTimeout(20000);
			 connect.setDoInput(true);
			 connect.connect();
			 return connect;
		} catch (Exception e) {
			Log.e(TAG, "连接侦听服务器拒绝链接"+e.getMessage());
			
			return null;
		}
	} 
	
	/** 
	 * 探测播放地址
	 * @param site_name 网站的名字（字符串），由搜索服务器返回，如tudou，ku6，qiyi等
	 * @param site_version 网站地址探测库版本，整数，如1，2，3
	 * @param page_uri  视频key，对于视频网站来讲，是播放页面的url，当使用query string方式时，必须进行percent encoded转换
	 * flagByHD 0 标清   1 高清
	 * @return null 探测器崩了   vo空数据则是没有侦听到节目
	 */
	public static ProbeEpisode  probe_player_address (String site_name, String site_version,String page_uri,String flagByHD ){
		
		
		
		 List<ProbeSectionUri> play_section_uri = new ArrayList<ProbeSectionUri>();
		 String connect_uri;
		 HttpURLConnection  connect = null ;
		 ProbeEpisode vo = new ProbeEpisode();
		 InputStream is = null;
		 InputStreamReader is_reader = null;
		 BufferedReader bufferReader = null;
		 try {
			 Log.i(TAG, "the connect uri is :====flagByHD===" + flagByHD);
			 if ("".equals(flagByHD))
				 connect_uri = PROBE_URI + "?sitenumber=1&sitename="+site_name+"&siteversion="+site_version+"&videokey=" + URLEncoder.encode("[0]", "UTF-8") + URLEncoder.encode(page_uri) ;
			 else
				 connect_uri = PROBE_URI + "?sitenumber=1&sitename="+site_name+"&siteversion="+site_version+"&videokey=" + URLEncoder.encode("["+ flagByHD +"]", "UTF-8") + URLEncoder.encode(page_uri) ;
			 
			 Log.i(TAG, "the connect uri is :" + connect_uri);
			 
			 connect = openConn(connect,  connect_uri, false);
			 if(connect == null)
			  {//没有打开连接直接返回不再侦听，另外会起个线程启动服务器  
				  return null;
			  }
			 //Log.i(TAG, "the connect uri is22222 :" + connect_uri);
			  
			  is = connect.getInputStream();
			 // Log.i(TAG, "the connect uri is3333 :" + connect_uri);
			  is_reader = new InputStreamReader(is);
			 // Log.i(TAG, "the connect uri is4444 :" + connect_uri);
			  bufferReader = new BufferedReader(is_reader);
			  //Log.i(TAG, "the connect uri is 555555:" + connect_uri);
			 String content = null ; 
			 int index = 0;
			 int uri_count = 0; 
			 Float all_duration = new Float(0);
			 Float duration = new Float(0);
			 while (  (content = bufferReader.readLine())!= null) {
				 Log.i(TAG, "palyer address is :" + content); 
				 try {
					 if (index == 0) {
						 uri_count = Integer.parseInt(content);
						 Log.i(TAG, "palyer address is :uri_count===" + uri_count); 
						 if (uri_count <= 0)
						 {
							//探测器返回空数据，不可为null
							 return vo;
						 }
						 index ++;
						 continue;
					 }
				 }catch(Exception e){}
				 
				 
				 ProbeSectionUri section_uri = new ProbeSectionUri();
				 if (index == 1) {
					 Log.i(TAG, "palyer address is :index == 1==="); 
					 	
					 	String [] array_section_uri = content.split("-");
					 	if (content.contains("mohoDurations"))
						 	section_uri.setUri(content.substring(0, content.indexOf("mohoDurations") - 1));
					 	else if (content.contains("mohoHd"))
						 	section_uri.setUri(content.substring(0, content.indexOf("mohoHd") - 1));
					 	else
					 	{
					 		section_uri.setUri(content);
					 		vo.setSource_count(1);
					 		vo.setSource_default("0");
					 		
					 	}
					 	Log.i(TAG, "palyer address is :section_uri.getUri====" + section_uri.getUri()); 
					 	for (int i=0;i<array_section_uri.length;i++)
					 	{
					 		if ("mohoDurations".equals(array_section_uri[i]))
					 		{
					 			duration = Float.parseFloat(array_section_uri[i+1]);
					 			section_uri.setDuration(duration);
					 		}
					 		if ("mohoAllDurations".equals(array_section_uri[i]))
					 		{
					 			all_duration = Float.parseFloat(array_section_uri[i+1]);
					 		}
					 		if ("moholist".equals(array_section_uri[i]))
					 		{
					 			vo.setSource_count(Integer.parseInt(array_section_uri[i+1]));
					 		}
					 		if ("mohoHd".equals(array_section_uri[i]))
					 		{
					 			vo.setSource_default(array_section_uri[i+1]);
							}
					 	}
					 	
					 		
					
				 } else {
					    String [] array_section_uri = content.split("-");
					 	section_uri.setUri(content.substring(0, content.indexOf("mohoDurations") - 1));
					 	Log.i(TAG, "palyer address is :section_uri.getUri====" + section_uri.getUri()); 
					 	for (int i=0;i<array_section_uri.length;i++)
					 	{
					 		if ("mohoDurations".equals(array_section_uri[i]))
					 		{
						 		duration = Float.parseFloat(array_section_uri[i+1]);
					 			section_uri.setDuration(duration);
					 		}
					 	}
					 	
				 }
				 
				 
				 
				 
				 
				play_section_uri.add(section_uri);
				
			    index ++;
			 }
			 Log.i(TAG, "palyer address is :===all_duration===:===" + all_duration);
			 Log.i(TAG, "palyer address is :==uri_count===:===" + uri_count);
			 Log.i(TAG, "palyer address is :===play_section_uri===:===" + play_section_uri.size());
				
			 vo.setVideo_duration(all_duration);
			 vo.setPlay_uri_count(uri_count);
			 vo.setPlay_section_uri(play_section_uri);
			 
			 return vo;
			 
		} catch (MalformedURLException e) {
			Log.e(TAG, "aaaa"+e.getMessage());
			e.printStackTrace();
			vo.setPlay_section_uri(null);
			return vo;
			
		} catch (IOException e) {
			Log.e(TAG, "bbbb111111"+e.getMessage());
			e.printStackTrace();
			vo.setPlay_section_uri(null);
			return vo;
			
		} catch (Exception e) {
			Log.e(TAG, "cccc"+e.getMessage());
			e.printStackTrace();
			vo.setPlay_section_uri(null);
			return vo;
		} finally {
			try{
				bufferReader.close();
				is_reader.close();
				 is.close();
			}catch(Exception ex){}
			
			  
			  
			
			//关闭连接，并设置连接已经关闭的状态 ，即 connect_is_closed = 1
			if (connect != null)
			{
				 connect.disconnect();
				 connect_is_closed = 1;
			}
			
		}
		
	}
	
	/** 
	 * 探测播放地址
	 * @return string[0] speed；string[1] precentage
	 */
	public static int[]  currBufferInfo (int currTime, long threadId, boolean isM3u8, int mDuration, int mSeektime, int pauseflag){
		 int[] resultItem = new int[4];
		 String connect_uri;
		 HttpURLConnection  connect = null ;
		 ProbeEpisode vo = new ProbeEpisode();
		 InputStream is = null;
		 InputStreamReader is_reader = null;
		 BufferedReader bufferReader = null;
		 try {
			 if (isM3u8)
				 connect_uri = PROBE_BUFFER +  currTime;
			 else
			     connect_uri = PROBE_BUFFER +  currTime + "&alltime=" + mDuration + "&seektime=" + mSeektime;
			 if (pauseflag == 1)
				 connect_uri += "&pauseflag=" + pauseflag;
			 Log.i(TAG, "the connect uri is :" + connect_uri);
			 
			 connect = openConn(connect,  connect_uri, true);
			 if(connect == null)
			  {//没有打开连接直接返回不再侦听，另外会起个线程启动服务器  
				  return null;
			  }
			 Log.i(TAG, "the connect uri is :  open connect" + threadId);
			 
			 //Log.i(TAG, "the connect uri is22222 :" + connect_uri);
			  
			  is = connect.getInputStream();
			 // Log.i(TAG, "the connect uri is3333 :" + connect_uri);
			  is_reader = new InputStreamReader(is);
			 // Log.i(TAG, "the connect uri is4444 :" + connect_uri);
			  bufferReader = new BufferedReader(is_reader);
			  //Log.i(TAG, "the connect uri is 555555:" + connect_uri);
			 String content = null ; 
			 int index = 0;
			 int uri_count = 0; 
			 Float all_duration = new Float(0);
			 Float duration = new Float(0);
			 while (  (content = bufferReader.readLine())!= null) {
				 Log.i(TAG, "palyer address is :" + content); 
				 resultItem[0] = Integer.parseInt(content.split(",,,")[0].split(":")[1]);
				 resultItem[1] = Integer.parseInt(content.split(",,,")[1].split(":")[1]);
				 resultItem[2] = Integer.parseInt(content.split(",,,")[2].split(":")[1]);
				 resultItem[3] = Integer.parseInt(content.split(",,,")[3].split(":")[1]);
				 break;
			 }
			 Log.i(TAG, "palyer address is0000 :==speed==" + resultItem[0] + "==precentage==" + resultItem[1] + "==state==" + resultItem[2] + "==buffer==" + resultItem[3] ); 
			   return resultItem;
			 
				
			 
		} catch (MalformedURLException e) {
			Log.e(TAG, "aaaa"+e.getMessage());
			return null;
			
		} catch (IOException e) {
			Log.e(TAG, "bbbb111111"+e.getMessage());
			e.printStackTrace();
			return null;
			
		} catch (Exception e) {
			Log.e(TAG, "cccc"+e.getMessage());
			e.printStackTrace();
			return null;
		} finally {
			try{
				bufferReader.close();
				is_reader.close();
				is.close();
				//关闭连接，并设置连接已经关闭的状态 ，即 connect_is_closed = 1
				if (connect != null)
				{
					 connect.disconnect();
					 Log.i(TAG, "the connect uri is :  dis connect" + threadId);
						
				}
			}catch(Exception ex){
				Log.e(TAG, "disconnect==========error");
				ex.fillInStackTrace();
				ex.printStackTrace();
			}
			
		}
		
	}
	
	
//	/**
//	 * 启动视频探测服务
//	 * @throws Exception
//	 */
//	public static boolean startHttpServer() throws Exception {
//		Runtime runtime = Runtime.getRuntime(); 
//		Process proc = runtime.exec(HTTP_SERVER_PATH); //这句话就是shell与高级语言间的调用
//		//如果有参数的话可以用另外一个被重载的exec方法
//		InputStream inputstream = proc.getInputStream();
//		InputStreamReader inputstreamreader = new InputStreamReader(inputstream);
//		BufferedReader bufferedreader = new BufferedReader(inputstreamreader);
//		String line = "";
//		StringBuilder sb = new StringBuilder(line);
//		while ((line = bufferedreader.readLine()) != null) {
//			sb.append(line);
//			sb.append('\n');
//		}
//		//释放资源
//		inputstream.close();
//		inputstreamreader.close();
//		bufferedreader.close();
//		if (sb.indexOf("success") > -1) {
//			return true;
//		}else {
//			return false;
//		}
//	}
	
//	/**
//	 * 释放连接资源
//	 */
//	public static void releasePlayerResource(){
//		//Log.i(TAG, "判断释放资源");
//		if (connect != null && connect_is_closed == 0) {
//			//Log.i(TAG, "释放探测连接资源");
//			connect.disconnect();
//		}
//		
//	}
	
}
