package cn.com.xinli.android.doudian.service;

public interface DoudianService {

	public static final String GET_Normal_CHANNELS_BY_PAGE = "GETNORMALCHANNELSBYPAGE";
	public static final String GET_SPECIAL_CHANNELS_BY_PAGE = "GETSPECIALCHANNELSBYPAGE";
	public static final String GET_CHANNEL = "GETCHANNEL";
	public static final String GET_PROGRAM_DETAIL = "GETPROGRAMDETAIL";
	public static final String GET_PROGRAM_BY_PAGE = "GETPROGRAMBYPAGE";
	public static final String GET_PROGRAM_IN_CATEGORY_BY_PAGE = "GETPROGRAMINCATEGORYBYPAGE";
	public static final String GET_PROGRAM_IN_AREA_BY_PAGE = "GETPROGRAMINAREABYPAGE";
	public static final String GET_PROGRAM_IN_AREA_CATEGORY_BY_PAGE = "GETPROGRAMINAREACATEGORYBYPAGE";
	public static final String GET_PROGRAM_SOURCES = "GETPROGRAMSOURCES";
	public static final String GET_PROGRAM_EPISODES_BY_PAGE = "GETPROGRAMEPISODESBYPAGE";
	public static final String GET_PROGRAM_RELATED = "GETPROGRAMRELATED";
	
	public static final String GET_TOTAL = "GETTOTAL";
	public static final String GET_TOTAL_IN_CATEGORY = "GETTOTALINCATEGORY";
	public static final String GET_TOTAL_IN_AREA = "GETTOTALINAREA";
	public static final String GET_TOTAL_IN_AREA_CATEGORY = "GETTOTALINAREACATEGORY";
	public static final String GET_TOTAL_OF_NORMAL_CHANNELS = "GETTOTALOFNORMALCHANNELS";
	public static final String GET_TOTAL_OF_SPECIAL_CHANNELS = "GETTOTALOFSPECIALCHANNELS";
	
	public static final String GET_YEARS = "GETYEARS";
	public static final String GET_AREAS = "GETAREAS";
	public static final String GET_CATEGORIES = "GET_CATEGORIES";
	
	/**
	 * get all normal channelID:channelName with comma connected
	 * @return
	 */
	String getNormalChannelsByPage(int pageIndex);
	
	/**
	 * get all special(id>999) channelID:channelName with comma connected
	 * @return
	 */
	String getSpecialChannelsByPage(int pageIndex);
	
	/**
	 * get Json string of Channel
	 * @param channelID
	 * @return
	 */
	String getChannel(String channelID);
	
	/**
	 * get all years in Channel with comma connected
	 * @param channelID
	 * @return
	 */
	String getYears(String channelID);
	
	/**
	 * get top 10 areas in Channel with comma connected
	 * @param channelID
	 * @return
	 */
	String getAreas(String channelID);
	
	String getTops(String channelID);
	String getTop(String channelID, String topID);
	String getProgramInTop(String channelID, String topID);
	
	/**
	 * get Json string of ProgramSimple object
	 * @param channelID
	 * @param programHashcode
	 * @param programName
	 * @return
	 */
	String getProgramDetail(String channelID, int programHashcode, String programName);
	
	/**
	 * get Json string of source list in one Program.
	 * @param channelID
	 * @param programHashcode
	 * @return
	 */
	String getProgramSources(String channelID, int programHashcode);
	
	/**
	 * get string of EpisodeName:EpisodeUrl with comma connected by page
	 * @param channelID
	 * @param programHashcode
	 * @param sourceAlias
	 * @param pageIndex
	 * @return
	 */
	String getProgramEpisodesByPage(String channelID, int programHashcode, String sourceAlias, int pageIndex);
	
	/**
	 * get Json string of Relation object
	 * @param channelID
	 * @param programHashcode
	 * @return
	 */
	String getProgramRelated(String channelID, int programHashcode);
	
	/**
	 * get string of hashcode:programeName with comma connected by page
	 * @param channelID
	 * @param maxYear
	 * @param minYear
	 * @param pageIndex
	 * @return
	 */
	String getProgramByPage(String channelID, int maxYear, int minYear, int pageIndex);
	
	/**
	 * get string of hashcode:programeName with comma connected by page
	 * search repository by categories
	 * @param channelID
	 * @param maxYear
	 * @param minYear
	 * @param pageIndex
	 * @param categories
	 * @return
	 */
	String getProgramInCategoryByPage(String channelID, int maxYear, int minYear, int pageIndex, String[] categories);
	
	/**
	 * get string of hashcode:programeName with comma connected by page
	 * search repository by areas
	 * @param channelID
	 * @param maxYear
	 * @param minYear
	 * @param pageIndex
	 * @param areas
	 * @return
	 */
	String getProgramInAreaByPage(String channelID, int maxYear, int minYear, int pageIndex, String[] areas);
	
	/**
	 * get string of hashcode:programeName with comma connected by page
	 * search repository by categories and areas
	 * @param channelID
	 * @param maxYear
	 * @param minYear
	 * @param pageIndex
	 * @param areas
	 * @param categories
	 * @return
	 */
	String getProgramInAreaCategoryByPage(String channelID, int maxYear, int minYear, int pageIndex, String[] areas, String[] categories);
	
	/**
	 * get total count of programs in one Channel
	 * @param channelID
	 * @param maxYear
	 * @param minYear
	 * @return
	 */
	String getTotal(String channelID, int maxYear, int minYear);
	
	/**
	 * get total count of programs in one Channel
	 * search repository by categories
	 * @param channelID
	 * @param maxYear
	 * @param minYear
	 * @param categories
	 * @return
	 */
	String getTotalInCategory(String channelID, int maxYear, int minYear, String[] categories);
	
	/**
	 * get total count of programs in one Channel
	 * search repository by areas
	 * @param channelID
	 * @param maxYear
	 * @param minYear
	 * @param areas
	 * @return
	 */
	String getTotalInArea(String channelID, int maxYear, int minYear, String[] areas);
	
	/**
	 * get total count of programs in one Channel
	 * search repository by categories and areas
	 * @param channelID
	 * @param maxYear
	 * @param minYear
	 * @param areas
	 * @param categories
	 * @return
	 */
	String getTotalInAreaCategory(String channelID, int maxYear, int minYear, String[] areas, String[] categories);
	
	/**
	 * get total count of all normal channels
	 * @return
	 */
	String getTotalOfNormalChannels();
	
	/**
	 * get total count of all normal channels
	 * @return
	 */
	String getTotalOfSpecialChannels();

	/**
	 * get top 10 categories in Channel with comma connected
	 * @param channelID
	 * @return
	 */
	String getCategories(String channelID);	
	
	
}
