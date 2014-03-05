package cn.com.xinli.android.doudian.model;

import java.util.ArrayList;

public class Source {
	private String name;
	private String alias;
	private String epStatus;
	private String epTotal;
	private ArrayList<Episode> episodes;
	private String urlPrefix;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getAlias() {
		return alias;
	}

	public void setAlias(String alias) {
		this.alias = alias;
	}

	public void setEpStatus(String epStatus) {
		this.epStatus = epStatus;
	}
	
	public String getEpStatus() {
		return this.epStatus;
	}
 
	public String getEpTotal() {
		return epTotal;
	}

	public void setEpTotal(String epTotal) {
		this.epTotal = epTotal;
	}

	public ArrayList<Episode> getEpisodes() {
		return episodes;
	}

	public void setEpisodes(ArrayList<Episode> episodes) {
		this.episodes = episodes;
	}

	public String getUrlPrefix() {
		return urlPrefix;
	}

	public void setUrlPrefix(String url) {
		
		String prefix = "";

		if (url == null || url.lastIndexOf("/") < 0){
			return;
		}
		
		int lastIndex = url.lastIndexOf("/");
		prefix = url.substring(0,lastIndex);
		if (this.alias.equalsIgnoreCase("56") || this.alias.equalsIgnoreCase("sohu") ){
			int lastlastIndex = prefix.lastIndexOf("/"); 
			prefix = url.substring(0,lastlastIndex);
		}
		
		this.urlPrefix = prefix;
	}

	public void copyUrlPrefix(String url) {
		this.urlPrefix = url;
	}
}
