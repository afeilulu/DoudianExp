package cn.com.xinli.android.doudian.io;

import android.os.Parcel;
import android.os.Parcelable;

public class DetailRecommend implements Parcelable {

	private String menuId;
	private String poster;
	private String name;
	private String present;
	private int vid;
	private int channeltype;

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(menuId);
		dest.writeString(poster);
		dest.writeString(name);
		dest.writeString(present);
		dest.writeInt(vid);
		dest.writeInt(channeltype);
	}

	public String getMenuId() {
		return menuId;
	}

	public void setMenuId(String menuId) {
		this.menuId = menuId;
	}

	public String getPoster() {
		return poster;
	}

	public void setPoster(String poster) {
		this.poster = poster;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPresent() {
		return present;
	}

	public void setPresent(String present) {
		this.present = present;
	}

	public int getVid() {
		return vid;
	}

	public void setVid(int vid) {
		this.vid = vid;
		this.menuId = "v" + vid;
	}

	public int getChanneltype() {
		return channeltype;
	}

	public void setChanneltype(int channeltype) {
		this.channeltype = channeltype;
	}
}
