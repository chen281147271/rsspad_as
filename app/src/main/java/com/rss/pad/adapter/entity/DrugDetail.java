/*
 * Copyright (C) 2020 xuexiangjys(xuexiangjys@163.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.rss.pad.adapter.entity;

public class DrugDetail {
    private String locId;
    private String locName;
    private int curNum;
    private int maxNum;
    private int locState;
    private boolean isadd=false;
    private int originalcurNum;
    public DrugDetail() {
    }

    public DrugDetail(String locId, String locName, int curNum, int maxNum, int locState, boolean isadd, int originalcurNum) {
        this.locId = locId;
        this.locName = locName;
        this.curNum = curNum;
        this.maxNum = maxNum;
        this.locState = locState;
        this.isadd = isadd;
        this.originalcurNum = originalcurNum;
    }

    @Override
    public String toString() {
        return "DrugDetail{" +
                "locId='" + locId + '\'' +
                ", locName='" + locName + '\'' +
                ", curNum=" + curNum +
                ", maxNum=" + maxNum +
                ", locState=" + locState +
                ", isadd=" + isadd +
                ", originalcurNum=" + originalcurNum +
                '}';
    }

    public int getOriginalcurNum() {
        return originalcurNum;
    }

    public void setOriginalcurNum(int originalcurNum) {
        this.originalcurNum = originalcurNum;
    }

    public boolean isIsadd() {
        return isadd;
    }

    public void setIsadd(boolean isadd) {
        this.isadd = isadd;
    }

    public String getLocId() {
        return locId;
    }

    public void setLocId(String locId) {
        this.locId = locId;
    }

    public String getLocName() {
        return locName;
    }

    public void setLocName(String locName) {
        this.locName = locName;
    }

    public int getCurNum() {
        return curNum;
    }

    public void setCurNum(int curNum) {
        this.curNum = curNum;
    }

    public int getMaxNum() {
        return maxNum;
    }

    public void setMaxNum(int maxNum) {
        this.maxNum = maxNum;
    }

    public int getLocState() {
        return locState;
    }

    public void setLocState(int locState) {
        this.locState = locState;
    }
}
