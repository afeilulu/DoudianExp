package cn.com.xinli.android.doudian.model;

/**
 * Created by chen on 3/5/14.
 */
public class LocalVideoInfo {

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    private String url;
    private int duration;// in seconds

}
