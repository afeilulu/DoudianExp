package cn.com.xinli.android.doudian.utils;

import java.util.ArrayList;

import cn.com.xinli.android.doudian.model.Channel;

/**
 * Created by chen on 1/23/14.
 */
public class ChannelsHolder {

    private static final ChannelsHolder holder = new ChannelsHolder();
    public static ChannelsHolder getInstance(){
        return holder;
    }

    public ArrayList<Channel> getChannelArrayList() {
        return channelArrayList;
    }

    public void setChannelArrayList(ArrayList<Channel> channelArrayList) {
        this.channelArrayList = channelArrayList;
    }

    private ArrayList<Channel> channelArrayList;
}
