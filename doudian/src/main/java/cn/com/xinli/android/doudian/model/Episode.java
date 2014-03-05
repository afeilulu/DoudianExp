package cn.com.xinli.android.doudian.model;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;

public class Episode {
	private String name;
	private String stage;
	private String url;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
//		this.url = getSuffix(url);
        this.url = url;
	}

	public String getStage() {
		return stage;
	}

	/**
	 * format date to yyyyMMdd
	 * @param stage
	 */
	public void setStage(String stage) {
		if (stage != null && stage.contains("-")){
			String[] tmpStrs = stage.split("-");
			stage = "";
			for(int i = 0;i < tmpStrs.length; i++){
				stage = stage + tmpStrs[i];
			}
		}
		this.stage = stage;
	}
	
	/*
	 * url handle to get unique string
	 * url example:
	 * 
	 * Movie:
	 * http://cps.youku.com/redirect.html?id=0000028f&url=http://v.youku.com/v_show/id_XNjAzMjk2NjI4.html
	 * return "id_XNjAzMjk2NjI4.html"
	 * 
	 * Drama:
	 * http://v.youku.com/v_show/id_XNjI0MTkxNjAw.html?tpa=dW5pb25faWQ9MTAyMjEzXzEwMDAwNl8wMV8wMQ
	 * return "id_XNjI0MTkxNjAw.html"
	 * 
	 * http://tv.sohu.com/20131106/n389699061.shtml?txid=4e4df35dda9d8ed32c874b1ad590ef59
	 * return "20131106/n389699061.shtml"
	 * 
	 * http://vod.kankan.com/v/68/68469/350747.shtml?id=731021
	 * return "350747.shtml"
	 * 
	 * http://www.letv.com/ptv/vplay/2167432.html
	 * return "2167432.html"
	 * 
	 * http://www.tudou.com/albumplay/b-7vgta7Wjg/hjUbcrhuDJE.html?tpa=dW5pb25faWQ9MTAyMjEzXzEwMDAwMV8wMV8wMQ
	 * return "hjUbcrhuDJE.html"
	 * 
	 * http://v.pptv.com/show/o2TJRq4UhMIloy8.html
	 * return "http://v.pptv.com/show/o2TJRq4UhMIloy8.html"
	 * 
	 * http://v.pps.tv/play_39G73A.html#from_360
	 * return "play_39G73A.html"
	 * 
	 * http://www.funshion.com/subject/play/104723/1?alliance=152055
	 * return "1"
	 * 
	 * http://www.56.com/u76/v_MTAwMTI4NTc3.html
	 * return "u76/v_MTAwMTI4NTc3.html"
	 * 
	 */
	public static String getSuffix(String url){
		
		String suffix = "";
		
		if (url.contains("redirect")){
			try {
				URL aUrl = new URL(url);
				String query = aUrl.getQuery();
				String[] pairs = query.split("&");
			    for (String pair : pairs) {
			        int idx = pair.indexOf("url=");
			        if (idx >= 0){
				        try {
							url = URLDecoder.decode(pair.substring(idx + 1), "UTF-8");
							break;
						} catch (UnsupportedEncodingException e) {
							e.printStackTrace();
						}
			        }
			    }
			} catch (MalformedURLException e) {
				System.err.println("error:mailformed url");
			}
			
		}

		if (url == null || url.indexOf("/") < 0){
			return suffix;
		}
		
		int lastIndex = url.lastIndexOf("?");
		if (lastIndex > 0) {
			suffix = url.substring(0,lastIndex);
		} else {
			suffix = url;
		}
		
		lastIndex = suffix.lastIndexOf("/");
		if (lastIndex > 0){
			if (url.indexOf("56.com") > -1 || url.indexOf("sohu.com") > -1 ){
				String tmpStr = suffix.substring(0,lastIndex);
				int lastlastIndex = tmpStr.lastIndexOf("/"); 
				suffix = suffix.substring(lastlastIndex + 1);
			} else {
				suffix = suffix.substring(lastIndex + 1);
			}
		}
		
		return suffix;
	}

}
