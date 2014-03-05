package cn.com.xinli.android.doudian.utils;

public class TaskStatus {
    private long id;
    private String name;
    private int state;
    private long total;
    private long size;

    public TaskStatus(String name, int state) {
        this.name = name;
        this.state = state;
    }

    public TaskStatus(long id, String name, long size, long total) {
        this.id = id;
        this.name = name;
        this.size = size;
        this.total = total;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    public long getTotal() {
        return total;
    }

    public void setTotal(long total) {
        this.total = total;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

}