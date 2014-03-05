package cn.com.xinli.android.doudian.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xmlpull.v1.XmlSerializer;

import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.util.Xml;

/**
 * @author lijiang
 * 
 */
public class XmlOperate {
private static final String TAG="XmlOperate";
	private static String writeApprove(Map<String,String> map) {
		XmlSerializer serializer = Xml.newSerializer();
		StringWriter writer = new StringWriter();
		try {
			serializer.setOutput(writer);
			serializer.startDocument("UTF-8", true);
			serializer.startTag("", "config");
			serializer.startTag("", "mac");
			serializer.text(map.get("mac"));
			serializer.endTag("", "mac");
			serializer.startTag("", "userId");
			serializer.text(map.get("userId"));
			serializer.endTag("", "userId");
			
			serializer.startTag("", "sessionId");
			serializer.text(map.get("sessionId")==null?"":map.get("sessionId"));
			serializer.endTag("", "sessionId");
			
			serializer.startTag("", "success");
			serializer.text(map.get("success")==null?"":map.get("success"));
			serializer.endTag("", "success");
			
			serializer.startTag("", "group");
			serializer.text(map.get("group")==null?"":map.get("group"));
			serializer.endTag("", "group");
			
			serializer.startTag("", "hasExpired");
			serializer.text(map.get("hasExpired")==null?"":map.get("hasExpired"));
			serializer.endTag("", "hasExpired");
			
			serializer.startTag("", "expired");
			serializer.text(map.get("expired")==null?"":map.get("expired"));
			serializer.endTag("", "expired");
			
			serializer.startTag("", "successTime");
			serializer.text(map.get("successTime")==null?"":map.get("successTime"));
			serializer.endTag("", "successTime");
			
			serializer.endTag("", "config");
			
			serializer.endDocument();
			return writer.toString();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public static boolean writeApproveToXml(String path, Map<String,String> map) {
		OutputStream os=null;
		try {
			File file = new File(path);
			if(file.exists()){
				file.delete();
			}
			file.createNewFile();
			String txt=writeApprove(map);
			os=new FileOutputStream(file);
			os.write(txt.getBytes());
			//将文件放入private目录
//			String fileName=path.substring(path.lastIndexOf("/")+1,path.length());
//			SecureFile secureFile=new SecureFile(fileName);
//			secureFile.createFile();
//			boolean b=secureFile.write(path, false);
//			Log.i(TAG, "------secureFile----write:"+b);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}finally{
			if(os!=null){
				try {
					os.flush();
					os.close();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		return true;
	}
	public static Map<String,String> readApproveFromXml(String path){
		//先从private中把已保存的MConfig.xml取出放到 path 
//		String fileName=path.substring(path.lastIndexOf("/")+1,path.length());
//		SecureFile secureFile=new SecureFile(fileName);
//		boolean exists=secureFile.exists();
//		Log.i(TAG, "---SecureFile--exists---------:"+exists);
//		if(secureFile.exists()){
//			File file = new File(path);
//			if(!file.exists()){
//				try {
//					file.createNewFile();
//				} catch (IOException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//			}
//			boolean read=secureFile.read(path);
//			Log.i(TAG, "----SecureFile---read-----:"+read);
//		}
		DocumentBuilderFactory docBuilderFactory = null;
		  DocumentBuilder docBuilder = null;
		  Document doc = null;
		  Map<String,String> map=new HashMap<String, String>();
		  try {
			File file = new File(path);
			if(!file.exists()){
				return map;
			}
		   docBuilderFactory = DocumentBuilderFactory.newInstance();
		   docBuilder = docBuilderFactory.newDocumentBuilder();
		   doc = docBuilder.parse(new FileInputStream(file));
		   Element root = doc.getDocumentElement();
		   NodeList macNodeList = root.getElementsByTagName("mac");
		   Node nd = macNodeList.item(0);
		   map.put("mac", nd!=null?nd.getTextContent():"");
		   
		   NodeList userIdNodeList = root.getElementsByTagName("userId");
		   Node nd2 = userIdNodeList.item(0);
		   map.put("userId", nd2!=null?nd2.getTextContent():"");
		   
		   NodeList sessionIdNodeList = root.getElementsByTagName("sessionId");
		   Node nd3 = sessionIdNodeList.item(0);
		   map.put("sessionId", nd3!=null?nd3.getTextContent():"");
		   
		   NodeList successNodeList = root.getElementsByTagName("success");
		   Node nd4 = successNodeList.item(0);
		   map.put("success", nd4!=null?nd4.getTextContent():"");
		   
		   NodeList groupNodeList = root.getElementsByTagName("group");
		   Node nd5 = groupNodeList.item(0);
		   map.put("group", nd5!=null?nd5.getTextContent():"");
		   
		   NodeList hasExpiredNodeList = root.getElementsByTagName("hasExpired");
		   Node nd6 = hasExpiredNodeList.item(0);
		   map.put("hasExpired", nd6!=null?nd6.getTextContent():"");
		   
		   NodeList expiredNodeList = root.getElementsByTagName("expired");
		   Node nd7 = expiredNodeList.item(0);
		   map.put("expired", nd7!=null?nd7.getTextContent():"");
		   
		   NodeList successTimeNodeList = root.getElementsByTagName("successTime");
		   Node nd8 = successTimeNodeList.item(0);
		   map.put("successTime", nd8!=null?nd8.getTextContent():"");
		   
		  } catch (ParserConfigurationException e) {
		   e.printStackTrace();
		  } catch (SAXException e) {
		   e.printStackTrace();
		  } catch (IOException e) {
		   e.printStackTrace();
		  } finally {
		   doc = null;
		   docBuilder = null;
		   docBuilderFactory = null;
		  }
		  return map;
	}
	
	
	public static Map<String,String> readAuthFromSP(SharedPreferences sp){
		String mac=sp.getString("mac","");
		String userId=sp.getString("userId","");
		String sessionId=sp.getString("sessionId","");
		String success=sp.getString("success","");
		String group=sp.getString("group","");
		String hasExpired=sp.getString("hasExpired","");
		String expired=sp.getString("expired","");
		String successTime=sp.getString("successTime","");
		Map<String,String> map=new HashMap<String, String>();
		map.put("mac", mac);
		map.put("userId", userId);
		map.put("sessionId", sessionId);
		map.put("success", success);
		map.put("group", group);
		map.put("hasExpired", hasExpired);
		map.put("expired", expired);
		map.put("successTime", successTime);
		return map;
	}
	public static void writeAuthToSP(Map<String,String> map,SharedPreferences sp) {
		Editor e=sp.edit();
		e.putString("mac", map.get("mac"));
		e.putString("userId", map.get("userId"));
		e.putString("sessionId", map.get("sessionId"));
		e.putString("success", map.get("success"));
		e.putString("group", map.get("group"));
		e.putString("hasExpired", map.get("hasExpired"));
		e.putString("expired", map.get("expired"));
		e.putString("successTime", map.get("successTime"));
		e.commit();
	}
}
