package cn.com.xinli.android.doudian.io;

import android.os.Parcel;
import android.os.Parcelable;

public class DetailResp implements Parcelable {
	private int code;
	private String action;
	private String msg;

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(code);
		dest.writeString(action);
		dest.writeString(getMsg());

	}

	public static final Parcelable.Creator<DetailResp> CREATOR = new Creator<DetailResp>() {
		public DetailResp createFromParcel(Parcel source) {
			DetailResp actor = new DetailResp();
			actor.setCode(source.readInt());
			actor.setAction(source.readString());
			actor.setMsg(source.readString());
			return actor;
		}

		public DetailResp[] newArray(int size) {
			return new DetailResp[size];
		}
	};

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getAction() {
		return action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	public String getMsg() {
		return msg;
	}

	public void setMsg(String msg) {
		this.msg = msg;
	}

}
