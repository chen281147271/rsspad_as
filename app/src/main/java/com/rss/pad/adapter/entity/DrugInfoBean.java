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

import com.rss.pad.utils.KeepNotProguard;

/**
 * 药品列表原始数据
 */
@KeepNotProguard
public class DrugInfoBean {
    private String medId;
    private String medName;
    private String medUnit;
    private String factoryName;
    private String storageCode;
    private String curMainNum;
    private String maxMainNum;

    public DrugInfoBean() {
    }

    public String getMedId() {
        return medId;
    }

    public void setMedId(String medId) {
        this.medId = medId;
    }

    public String getMedName() {
        return medName;
    }

    public void setMedName(String medName) {
        this.medName = medName;
    }

    public String getMedUnit() {
        return medUnit;
    }

    public void setMedUnit(String medUnit) {
        this.medUnit = medUnit;
    }

    public String getFactoryName() {
        return factoryName;
    }

    public void setFactoryName(String factoryName) {
        this.factoryName = factoryName;
    }

    public String getStorageCode() {
        return storageCode;
    }

    public void setStorageCode(String storageCode) {
        this.storageCode = storageCode;
    }

    public String getCurMainNum() {
        return curMainNum;
    }

    public void setCurMainNum(String curMainNum) {
        this.curMainNum = curMainNum;
    }

    public String getMaxMainNum() {
        return maxMainNum;
    }

    public void setMaxMainNum(String maxMainNum) {
        this.maxMainNum = maxMainNum;
    }
}
