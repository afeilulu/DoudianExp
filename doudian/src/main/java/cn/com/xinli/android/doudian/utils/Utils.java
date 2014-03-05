package cn.com.xinli.android.doudian.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;

public class Utils {

	private Context context;
	private static final String TAG="Utils";
	private final static String MAC_PATH="/system/etc/MAC";
	/**
	 * socet链接超时 <li>30000</li>
	 * */
	private static final int CONNECTION_TIME_OUT = 30000;
	/**
	 * socket超时的时间 <li>60000</li>
	 * */
	private static final int TIME_OUT = 60000;
	public Utils(Context context) {
		this.context = context;
	}
	/**
	 * 检查网络连接
	 * @return
	 */
	public boolean checkNetworkInfo() {
		ConnectivityManager conMan = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo info = conMan.getActiveNetworkInfo();
		if (info == null) {
			return false;
		}
		return true;
	}

	/**
	 * 认证初始化
	 * @return
	 */
	
	public String getStrById(int id) {
		return context.getResources().getString(id);
	}

	/**
	 * 获取Mac地址 Returns MAC address of the given interface name.
	 * 
	 * @param interfaceName
	 *            eth0, wlan0 or NULL=use first interface
	 * @return mac address or empty string
	 */
	public static String getMacAddress(String interfaceName) {
		String mMac=readCustomMac();
		if(mMac.equals("")){
			try {
				List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
				for (NetworkInterface intf : interfaces) {
					if (interfaceName != null) {
						if (!intf.getName().equalsIgnoreCase(interfaceName)) continue;
					}
					byte[] mac = intf.getHardwareAddress();
					if (mac==null) return "";
					StringBuilder buf = new StringBuilder();
					for (int idx=0; idx<mac.length; idx++)
						buf.append(String.format("%02X:", mac[idx]));       
					if (buf.length()>0) buf.deleteCharAt(buf.length()-1);
					return buf.toString();
				}
			} catch (Exception ex) { } // for now eat exceptions
		}else{
			return mMac;
		}
        return "";
	}
	private static String readCustomMac(){
		String mac="";
		File file=new File(MAC_PATH);
		InputStream inputStream=null;
		InputStreamReader reader=null;
		BufferedReader bufferedReader=null;
		try {
			inputStream=new FileInputStream(file);
			reader=new InputStreamReader(inputStream);
			bufferedReader=new BufferedReader(reader);
			mac=bufferedReader.readLine().trim();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
				if(bufferedReader!=null){
					bufferedReader.close();
				}
				if(reader!=null){
					reader.close();
				}
				if(inputStream!=null){
					inputStream.close();
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return mac;
	}
	
	/**
	 * 获取当前连接的网络
	 * 
	 * @param context
	 * @return eth0:有线；wlan0:无线；"":未连接
	 */
	public static String getConnNetWork(Context context) {
		ConnectivityManager connect = (ConnectivityManager) context
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (connect != null) {
			NetworkInfo[] info = connect.getAllNetworkInfo();
			if (info != null) {
				for (int i = 0; i < info.length; i++) {
					if (info[i].getState() == NetworkInfo.State.CONNECTED
							&& info[i].getType() == ConnectivityManager.TYPE_ETHERNET) {
						return "eth0";
					}
					if (info[i].getState() == NetworkInfo.State.CONNECTED
							&& info[i].getType() == ConnectivityManager.TYPE_WIFI) {
						return "wlan0";
					}
				}
			}
		}
		return "";
	}

}
