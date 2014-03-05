package cn.com.xinli.android.doudian.io;

import android.os.Parcel;
import android.os.Parcelable;

public class DetailEpisode implements Parcelable {

	private String name;
	private String indexNum;
	private String type;
	private String siteVersion;
	private String url;

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(name);
		dest.writeString(indexNum);
		dest.writeString(type);
		dest.writeString(siteVersion);
		dest.writeString(url);
	}
	public static final Parcelable.Creator<DetailEpisode> CREATOR = new Creator<DetailEpisode>() {  
        public DetailEpisode createFromParcel(Parcel source) {  
        	DetailEpisode mDetailEpisode = new DetailEpisode();
        	mDetailEpisode.setName(source.readString());
        	mDetailEpisode.setIndexNum(source.readString());
        	mDetailEpisode.setType(source.readString());
        	mDetailEpisode.setSiteVersion(source.readString());
        	mDetailEpisode.setUrl(source.readString());
            return mDetailEpisode;  
        }  
        public DetailEpisode[] newArray(int size) {  
            return new DetailEpisode[size];  
        }  
    };

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getIndexNum() {
		return indexNum;
	}

	public void setIndexNum(String indexNum) {
		this.indexNum = indexNum;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getSiteVersion() {
		return siteVersion;
	}

	public void setSiteVersion(String siteVersion) {
		this.siteVersion = siteVersion;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}
