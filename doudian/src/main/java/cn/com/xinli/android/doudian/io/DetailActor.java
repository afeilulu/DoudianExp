package cn.com.xinli.android.doudian.io;

import android.os.Parcel;
import android.os.Parcelable;

public class DetailActor implements Parcelable {
	// type: 2 为导演，３ 为演员、主持人、歌手，但不显示
	private int type;
	private String name;
	private String ext;

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(type);
		dest.writeString(name);
		dest.writeString(ext);

	}

	public static final Parcelable.Creator<DetailActor> CREATOR = new Creator<DetailActor>() {
		public DetailActor createFromParcel(Parcel source) {
			DetailActor actor = new DetailActor();
			actor.setType(source.readInt());
			actor.setName(source.readString());
			actor.setExt(source.readString());
			return actor;
		}

		public DetailActor[] newArray(int size) {
			return new DetailActor[size];
		}
	};

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getExt() {
		return ext;
	}

	public void setExt(String ext) {
		this.ext = ext;
	}

}
