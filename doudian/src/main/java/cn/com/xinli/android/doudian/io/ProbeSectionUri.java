package cn.com.xinli.android.doudian.io;


/**
 * 视频探测路径的描述
 * @author gaojt
 *
 */
public class ProbeSectionUri { 
	private String uri;  //实际的播放地址
	private float duration; //某段视频的播放长度
	public String getUri() {
		return uri;
	}
	public void setUri(String uri) {
		this.uri = uri;
	}
	public float getDuration() {
		return duration;
	}
	public void setDuration(float duration) {
		this.duration = duration;
	}
}
