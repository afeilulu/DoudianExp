package cn.com.xinli.android.doudian.utils;

import java.util.ArrayList;

import cn.com.xinli.android.doudian.model.Source;

/**
 * Singleton class for holding source and episodes of a program
 * Created by chen on 1/15/14.
 */
public class SourceHolder {

    private static final SourceHolder holder = new SourceHolder();
    private ArrayList<Source> sourcesList;
//    private Map<String, Collection<Episode>> episodeMap;

    public static SourceHolder getInstance() {
        return holder;
    }

    public ArrayList<Source> getSourcesList() {
        return sourcesList;
    }

    public void setSourcesList(ArrayList<Source> sourcesList) {
        this.sourcesList = sourcesList;
    }

//    public Map<String, Collection<Episode>> getEpisodeMap() {
//        return episodeMap;
//    }

//    public void setEpisodeMap(Map<String, Collection<Episode>> episodeMap) {
//        this.episodeMap = episodeMap;
//    }

    public void clear(){
        if (sourcesList != null)
            sourcesList.clear();

//        if (episodeMap != null)
//            episodeMap.clear();
    }

}
