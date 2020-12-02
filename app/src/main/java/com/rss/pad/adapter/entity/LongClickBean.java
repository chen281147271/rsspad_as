package com.rss.pad.adapter.entity;

/**
 * 按钮长按队列
 */
public class LongClickBean {
    public int viewId;
    public int cur;
    public int pos;
    public int max;

    public int getViewId() {
        return viewId;
    }

    public void setViewId(int viewId) {
        this.viewId = viewId;
    }

    public int getCur() {
        return cur;
    }

    public void setCur(int cur) {
        this.cur = cur;
    }

    public int getPos() {
        return pos;
    }

    public void setPos(int pos) {
        this.pos = pos;
    }

    public int getMax() {
        return max;
    }

    public void setMax(int max) {
        this.max = max;
    }

    @Override
    public String toString() {
        return "LongClickBean{" +
                "viewId=" + viewId +
                ", cur=" + cur +
                ", pos=" + pos +
                ", max=" + max +
                '}';
    }
}
