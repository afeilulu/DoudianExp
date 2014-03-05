package cn.com.xinli.android.doudian.io;

import java.util.ArrayList;

import android.os.Parcel;
import android.os.Parcelable;

public class DetailName implements Parcelable {

	private String name;
	private ArrayList<DetailEpisode> episode;

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(getName());
		dest.writeList(episode);
	}
	public static final Parcelable.Creator<DetailName> CREATOR = new Creator<DetailName>() {  
        public DetailName createFromParcel(Parcel source) {  
        	DetailName mDetailName = new DetailName();
        	mDetailName.setName(source.readString());
        	mDetailName.setEpisode(source.readArrayList(DetailEpisode.class.getClassLoader()));
            return mDetailName;  
        }  
        public DetailName[] newArray(int size) {  
            return new DetailName[size];  
        }  
    };
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ArrayList<DetailEpisode> getEpisode() {
		return episode;
	}

	public void setEpisode(ArrayList<DetailEpisode> episode) {
		this.episode = episode;
	}
}
