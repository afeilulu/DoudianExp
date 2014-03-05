package cn.com.xinli.android.doudian.io;

import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;

public class DetailSource implements Parcelable {

	private String source;
	private int sourceSort;
	private String sourceType;
	private String version;
	private String fixed;
	private ArrayList<DetailName> name;

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(source);
		dest.writeInt(getSourceSort());
		dest.writeString(sourceType);
		dest.writeString(version);
		dest.writeString(fixed);
		dest.writeList(name);
	}

	public static final Parcelable.Creator<DetailSource> CREATOR = new Creator<DetailSource>() {
		public DetailSource createFromParcel(Parcel source) {
			DetailSource mDetailSource = new DetailSource();
			mDetailSource.setSource(source.readString());
			mDetailSource.setSourceSort(source.readInt());
			mDetailSource.setSourceType(source.readString());
			mDetailSource.setVersion(source.readString());
			mDetailSource.setFixed(source.readString());
			mDetailSource.setName(source.readArrayList(DetailSource.class
					.getClassLoader()));
			return mDetailSource;
		}

		public DetailSource[] newArray(int size) {
			return new DetailSource[size];
		}
	};

	public String getSource() {
		return source;
	}

	public void setSource(String source) {
		this.source = source;
	}

	public int getSourceSort() {
		return sourceSort;
	}

	public void setSourceSort(int sourceSort) {
		this.sourceSort = sourceSort;
	}

	public String getSourceType() {
		return sourceType;
	}

	public void setSourceType(String sourceType) {
		this.sourceType = sourceType;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getFixed() {
		return fixed;
	}

	public void setFixed(String fixed) {
		this.fixed = fixed;
	}

	public ArrayList<DetailName> getName() {
		return name;
	}

	public void setName(ArrayList<DetailName> name) {
		this.name = name;
	}

}
