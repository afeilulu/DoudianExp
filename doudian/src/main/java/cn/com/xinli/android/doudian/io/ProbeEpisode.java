package cn.com.xinli.android.doudian.io;

import java.util.List;

public class ProbeEpisode  {
	//视频段的文件数
	private int play_uri_count;
	
	//视频源个数
	private int source_count;
	
	//默认视频源
	private String source_default;
	
	

	public String getSource_default() {
		return source_default;
	}

	public void setSource_default(String source_default) {
		this.source_default = source_default;
	}

	public int getSource_count() {
		return source_count;
	}

	public void setSource_count(int source_count) {
		this.source_count = source_count;
	}

	/**
	 * s
	 */
	private List<ProbeSectionUri> play_section_uri;
	
	//视频总时长 float 
	private Float video_duration;

	public int getPlay_uri_count() {
		return play_uri_count;
	}

	public List<ProbeSectionUri> getPlay_section_uri() {
		return play_section_uri;
	}

	public void setPlay_section_uri(List<ProbeSectionUri> play_section_uri) {
		this.play_section_uri = play_section_uri;
	}

	public void setPlay_uri_count(int play_uri_count) {
		this.play_uri_count = play_uri_count;
	}

	public Float getVideo_duration() {
		return video_duration;
	}

	public void setVideo_duration(Float video_duration) {
		this.video_duration = video_duration;
	}

	
	
   
	
	

	

}

