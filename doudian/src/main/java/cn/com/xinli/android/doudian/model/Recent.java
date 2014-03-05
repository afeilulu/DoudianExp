package cn.com.xinli.android.doudian.model;

public class Recent {

    private String channelId;
    private int hashCode;
    private String name;
    private String updateStatus;
    private String poster;
    private String source;
    private int episode;
    private int durationInSec;
    private int currentPositionInSec;

    public int getCurrentPositionInSec() {
        return currentPositionInSec;
    }

    public void setCurrentPositionInSec(int currentPositionInSec) {
        this.currentPositionInSec = currentPositionInSec;
    }

    public int getDurationInSec() {
        return durationInSec;
    }

    public void setDurationInSec(int durationInSec) {
        this.durationInSec = durationInSec;
    }

    public int getEpisode() {
        return episode;
    }

    public void setEpisode(int episode) {
        this.episode = episode;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getUpdateStatus() {
        return updateStatus;
    }

    public void setUpdateStatus(String updateStatus) {
        this.updateStatus = updateStatus;
    }

    public int getHashCode() {
        return hashCode;
    }

    public void setHashCode(int hashCode) {
        this.hashCode = hashCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPoster() {
        return poster;
    }

    public void setPoster(String poster) {
        this.poster = poster;
    }

    public String getChannelId() {
        return channelId;
    }

    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }
}
