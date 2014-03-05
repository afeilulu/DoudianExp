package cn.com.xinli.android.doudian.io;

import java.util.List;

import android.os.Parcel;
import android.os.Parcelable;
import android.os.Parcelable.Creator;

public class Detail implements Parcelable {

	private String menuId;
	private boolean delete;
	private String poster;
	private String name;
	private String area;
	private String premiereDate;
	private String hdFlag;
	private String scores;
	private int score;
	private String paraChannel;
	private String paraSubChannel;
	private String channel;
	private String subChannel;
	private String summer;
	private String version;
	private String isTotal;
	private String totalNumber;
	private String presentNumber;
	private List<String> directs;
	private List<String> actors;

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(menuId);
		dest.writeByte((byte)(delete?1:0));
		dest.writeString(poster);
		dest.writeString(name);
		dest.writeString(area);
		dest.writeString(premiereDate);
		dest.writeString(hdFlag);
		dest.writeString(scores);
		dest.writeInt(score);
		dest.writeString(paraChannel);
		dest.writeString(paraSubChannel);
		dest.writeString(channel);
		dest.writeString(subChannel);
		dest.writeString(summer);
		dest.writeString(version);
		dest.writeString(isTotal);
		dest.writeString(totalNumber);
		dest.writeString(presentNumber);
		dest.writeList(getDirects());
		dest.writeList(getActors());
	}
	public static final Parcelable.Creator<Detail> CREATOR = new Creator<Detail>() {  
        public Detail createFromParcel(Parcel source) {  
        	Detail mDetail = new Detail();
        	mDetail.setMenuId(source.readString());
        	mDetail.setDelete(source.readByte()==1);
        	mDetail.setPoster(source.readString());
        	mDetail.setName(source.readString());
        	mDetail.setArea(source.readString());
        	mDetail.setPremiereDate(source.readString());
        	mDetail.setHdFlag(source.readString());
        	mDetail.setScores(source.readString());
        	mDetail.setScore(source.readInt());
        	mDetail.setParaChannel(source.readString());
        	mDetail.setParaSubChannel(source.readString());
        	mDetail.setChannel(source.readString());
        	mDetail.setSubChannel(source.readString());
        	mDetail.setSummer(source.readString());
        	mDetail.setVersion(source.readString());
        	mDetail.setIsTotal(source.readString());
        	mDetail.setTotalNumber(source.readString());
        	mDetail.setPresentNumber(source.readString());
        	mDetail.setDirects(source.readArrayList(List.class.getClassLoader()));
        	mDetail.setActors(source.readArrayList(List.class.getClassLoader()));
            return mDetail;  
        }  
        public Detail[] newArray(int size) {  
            return new Detail[size];  
        }  
    };
	public String getMenuId() {
		return menuId;
	}

	public void setMenuId(String menuId) {
		this.menuId = menuId;
	}

	public boolean isDelete() {
		return delete;
	}

	public void setDelete(boolean delete) {
		this.delete = delete;
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

	public String getArea() {
		return area;
	}

	public void setArea(String area) {
		this.area = area;
	}

	public String getPremiereDate() {
		return premiereDate;
	}

	public void setPremiereDate(String premiereDate) {
		this.premiereDate = premiereDate;
	}

	public String getHdFlag() {
		return hdFlag;
	}

	public void setHdFlag(String hdFlag) {
		this.hdFlag = hdFlag;
	}

	public String getScores() {
		return scores;
	}

	public void setScores(String scores) {
		this.scores = scores;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}

	public String getParaChannel() {
		return paraChannel;
	}

	public void setParaChannel(String paraChannel) {
		this.paraChannel = paraChannel;
	}

	public String getParaSubChannel() {
		return paraSubChannel;
	}

	public void setParaSubChannel(String paraSubChannel) {
		this.paraSubChannel = paraSubChannel;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public String getSubChannel() {
		return subChannel;
	}

	public void setSubChannel(String subChannel) {
		this.subChannel = subChannel;
	}

	public String getSummer() {
		return summer;
	}

	public void setSummer(String summer) {
		this.summer = summer;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getIsTotal() {
		return isTotal;
	}

	public void setIsTotal(String isTotal) {
		this.isTotal = isTotal;
	}

	public String getTotalNumber() {
		return totalNumber;
	}

	public void setTotalNumber(String totalNumber) {
		this.totalNumber = totalNumber;
	}

	public String getPresentNumber() {
		return presentNumber;
	}

	public void setPresentNumber(String presentNumber) {
		this.presentNumber = presentNumber;
	}

	public List<String> getDirects() {
		return directs;
	}

	public void setDirects(List<String> directs) {
		this.directs = directs;
	}

	public List<String> getActors() {
		return actors;
	}

	public void setActors(List<String> actors) {
		this.actors = actors;
	}

	public String getConfirmChannel() {
		if (paraChannel != null && !"".equals(paraChannel)) {
			return paraChannel;
		} else {
			return channel;
		}
	}

	public String getConfirmSubChannel() {
		if (paraSubChannel != null && !"".equals(paraSubChannel)) {
			return paraSubChannel;
		} else {
			return subChannel;
		}
	}
}
