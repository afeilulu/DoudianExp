package cn.com.xinli.android.doudian.utils;

/**
 * Created by chen on 1/23/14.
 */
public class UIDimensionHolder {

    private static final UIDimensionHolder holder = new UIDimensionHolder();
    public static UIDimensionHolder getInstance(){
        return holder;
    }

    public int getItemHeight() {
        return itemHeight;
    }

    public void setItemHeight(int itemHeight) {
        this.itemHeight = itemHeight;
    }

    public int getScrollViewWidth() {
        return scrollViewWidth;
    }

    public void setScrollViewWidth(int scrollViewWidth) {
        this.scrollViewWidth = scrollViewWidth;
    }

    public int getCategoryBlankItemHeight() {
        return categoryBlankItemHeight;
    }

    public void setCategoryBlankItemHeight(int categoryBlankItemHeight) {
        this.categoryBlankItemHeight = categoryBlankItemHeight;
    }

    private int itemHeight;
    private int scrollViewWidth;
    private int categoryBlankItemHeight;
}
