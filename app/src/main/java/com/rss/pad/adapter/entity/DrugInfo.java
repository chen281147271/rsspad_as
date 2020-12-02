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

import java.io.Serializable;

public class DrugInfo implements Serializable {
    private String DrugName ;
    private int CurNum ;
    private int LackNum ;
    private int Percent ;
    private String Factory ;
    private String Unit ;
    private int medId;

    @Override
    public String toString() {
        return "DrugInfo{" +
                "DrugName='" + DrugName + '\'' +
                ", CurNum=" + CurNum +
                ", LackNum=" + LackNum +
                ", Percent=" + Percent +
                ", Factory='" + Factory + '\'' +
                ", Unit='" + Unit + '\'' +
                ", medId=" + medId +
                '}';
    }

    public DrugInfo(){

    }

    public DrugInfo(String drugName, int curNum, int lackNum, int percent, String factory, String unit, int medId) {
        DrugName = drugName;
        CurNum = curNum;
        LackNum = lackNum;
        Percent = percent;
        Factory = factory;
        Unit = unit;
        this.medId = medId;
    }

    public int getMedId() {
        return medId;
    }

    public void setMedId(int medId) {
        this.medId = medId;
    }

    public String getDrugName() {
        return DrugName;
    }

    public void setDrugName(String drugName) {
        DrugName = drugName;
    }

    public int getCurNum() {
        return CurNum;
    }

    public void setCurNum(int curNum) {
        CurNum = curNum;
    }

    public int getLackNum() {
        return LackNum;
    }

    public void setLackNum(int lackNum) {
        LackNum = lackNum;
    }

    public int getPercent() {
        return Percent;
    }

    public void setPercent(int percent) {
        Percent = percent;
    }

    public String getFactory() {
        return Factory;
    }

    public void setFactory(String factory) {
        Factory = factory;
    }

    public String getUnit() {
        return Unit;
    }

    public void setUnit(String unit) {
        Unit = unit;
    }
}
