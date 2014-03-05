package cn.com.xinli.android.doudian.utils;

public class UpgradePackage {
	private int curVersion;
	public int getCurVersion() {
		return curVersion;
	}
	public void setCurVersion(int curVersion) {
		this.curVersion = curVersion;
	}
	private String md5;
	private String size;
	
	public String getSize() {
		return size;
	}
	public void setSize(String size) {
		this.size = size;
	}
	private String Packages;
	private String announcement;
	private String message;
	
	
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	public String getMd5() {
		return md5;
	}
	public void setMd5(String md5) {
		this.md5 = md5;
	}
	public String getPackages() {
		return Packages;
	}
	public void setPackages(String packages) {
		Packages = packages;
	}
	public String getAnnouncement() {
		return announcement;
	}
	public void setAnnouncement(String announcement) {
		this.announcement = announcement;
	}
}
