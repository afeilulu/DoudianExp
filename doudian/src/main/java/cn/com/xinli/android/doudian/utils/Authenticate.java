package cn.com.xinli.android.doudian.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.NetworkInterface;
import java.net.URL;
import java.util.Collections;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import android.text.TextUtils;
import android.util.Log;

public class Authenticate {
	private static final String TAG = "Authenticate";
	private final static int connTimeOut = 10000;
	// 生产：http://moho.funhd.cn TYPE = "A10s"
	// 测试：http://192.168.2.243:8080 TYPE = "A10"
	public static final String UPDATEAPKHOST = "http://moho.funhd.cn";
	private static final String UPDATEAPKURL = "/upgradeManagement/upgrade";
	private static final String TYPE = "doudianAlone";
	private final static String MAC_PATH = "/system/etc/MAC";

	public static UpgradePackage getUpgradePackages(int version,
			String macAddress) {
		UpgradePackage upgrade = null;

		// String mac = getMacAddr();
		String mac = null;
		if (TextUtils.isEmpty(macAddress))
			mac = getMacAddress("eth0");
		else
			mac = macAddress;

		Log.i(TAG, "upgrade=mac==" + mac);

		if (TextUtils.isEmpty(mac))
			return upgrade;

		try {
			StringBuffer sb = new StringBuffer();
			// sb.append(UPDATEAPKHOST).append(UPDATEAPKURL).append("?username=")
			// .append(mac);
			// sb.append("&version=").append(version);
			// sb.append("&type=" + TYPE);

			sb.append(UPDATEAPKHOST)
				.append(UPDATEAPKURL)
				.append("?version=")
				.append(version)
				.append("&type=" + TYPE);

			Log.i(TAG, "upgrade=" + sb.toString());

			URL url = new URL(sb.toString());
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setConnectTimeout(connTimeOut);
			conn.connect();
			if (conn.getResponseCode() == 200) {
				InputStream inputS = conn.getInputStream();
				SAXParserFactory spf = SAXParserFactory.newInstance();
				SAXforUpgradePackageHandler upgradeHandler = new SAXforUpgradePackageHandler();
				SAXParser saxparser = spf.newSAXParser();
				saxparser.parse(inputS, upgradeHandler);
				upgrade = upgradeHandler.getUpgradePackage();
				inputS.close();
				conn.disconnect();
			}
			return upgrade;
		} catch (Exception e) {
			e.printStackTrace();
			Log.e(TAG, e.getMessage(), e);
			return upgrade;
		}
	}

	/*
	 * private static String getMacAddr() { IBinder b = ServiceManager
	 * .getService(Context.NETWORKMANAGEMENT_SERVICE); INetworkManagementService
	 * networkManagement = INetworkManagementService.Stub .asInterface(b); if
	 * (networkManagement != null) { InterfaceConfiguration iconfig = null; try
	 * { iconfig = networkManagement.getInterfaceConfig("eth0"); } catch
	 * (Exception e) { e.printStackTrace(); } finally { return iconfig.hwAddr !=
	 * null ? iconfig.hwAddr : ""; } } else { return ""; } }
	 */

	/**
	 * 获取Mac地址 Returns MAC address of the given interface name.
	 * 
	 * @param interfaceName
	 *            eth0, wlan0 or NULL=use first interface
	 * @return mac address or empty string
	 */
	public static String getMacAddress(String interfaceName) {
		String mMac = readCustomMac();
		if (mMac.equals("")) {
			try {
				List<NetworkInterface> interfaces = Collections
						.list(NetworkInterface.getNetworkInterfaces());
				for (NetworkInterface intf : interfaces) {
					if (interfaceName != null) {
						if (!intf.getName().equalsIgnoreCase(interfaceName))
							continue;
					}
					byte[] mac = intf.getHardwareAddress();
					if (mac == null)
						return "";
					StringBuilder buf = new StringBuilder();
					for (int idx = 0; idx < mac.length; idx++)
						buf.append(String.format("%02X:", mac[idx]));
					if (buf.length() > 0)
						buf.deleteCharAt(buf.length() - 1);
					return buf.toString();
				}
			} catch (Exception ex) {
			} // for now eat exceptions
		} else {
			return mMac;
		}
		return "";
	}

	private static String readCustomMac() {
		String mac = "";
		File file = new File(MAC_PATH);
		try {
			InputStream inputStream = new FileInputStream(file);
			InputStreamReader reader = new InputStreamReader(inputStream);
			BufferedReader bufferedReader = new BufferedReader(reader);
			mac = bufferedReader.readLine().trim();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return mac;
	}
}