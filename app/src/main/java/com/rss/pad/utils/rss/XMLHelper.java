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

package com.rss.pad.utils.rss;

import com.rss.pad.adapter.entity.DrugDetail;
import com.rss.pad.adapter.entity.DrugDetailBean;
import com.rss.pad.adapter.entity.DrugInfoBean;
import com.rss.pad.adapter.entity.DrugInfo;
import com.rss.pad.adapter.entity.LoginBean;
import com.rss.pad.adapter.entity.ResultBean;

import java.util.ArrayList;
import java.util.List;

public final class XMLHelper {

    /**
     * 获取药品列表
     * @param str
     * @return
     */
    public static List<DrugInfo> GetDrugInfo(String str){
        List<DrugInfo> resList=new ArrayList<>();
        List<Object>medInfoS=QueryResult.readXMLToList("medInfo",str, DrugInfoBean.class,"rsl");
        List<DrugInfoBean>list=new ArrayList<DrugInfoBean>();
        for(int i=0;i<medInfoS.size();i++){
            list.add((DrugInfoBean)medInfoS.get(i));
        }
        for(int i=0;i<list.size();i++){
            DrugInfo drugInfo = new DrugInfo();
            drugInfo.setCurNum(Integer.parseInt(list.get(i).getCurMainNum()));
            drugInfo.setDrugName(list.get(i).getMedName());
            drugInfo.setFactory(list.get(i).getFactoryName());
            drugInfo.setLackNum(Integer.parseInt(list.get(i).getMaxMainNum())-Integer.parseInt(list.get(i).getCurMainNum()));
            drugInfo.setPercent((int)Math.floor((float)Integer.parseInt(list.get(i).getCurMainNum())/(float) Integer.parseInt(list.get(i).getMaxMainNum())*100));
            drugInfo.setUnit(list.get(i).getMedUnit());
            drugInfo.setMedId(Integer.parseInt(list.get(i).getMedId()));
            resList.add(drugInfo);
        }
        return  resList;
    }

    /**
     * 获取药品明细
     * @param str
     * @return
     */
    public static List<DrugDetail> GetDrugDetail(String str){
        List<DrugDetail> resList=new ArrayList<>();
        List<Object>drugDetailS=QueryResult.readXMLToList("locInfo",str, DrugDetailBean.class,"rsl");
        List<DrugDetailBean>list=new ArrayList<DrugDetailBean>();
        for(int i=0;i<drugDetailS.size();i++){
            list.add((DrugDetailBean)drugDetailS.get(i));
        }
        for(int i=0;i<list.size();i++){
            DrugDetail drugDetail=new DrugDetail();
            drugDetail.setLocId(list.get(i).getLocId());
            drugDetail.setCurNum(Integer.parseInt(list.get(i).getCurNum()));
            drugDetail.setIsadd(false);
            drugDetail.setLocName(list.get(i).getLocName());
            drugDetail.setLocState(Integer.parseInt(list.get(i).getLocState()));
            drugDetail.setMaxNum(Integer.parseInt(list.get(i).getMaxNum()));
            drugDetail.setOriginalcurNum(Integer.parseInt(list.get(i).getCurNum()));
            resList.add(drugDetail);
        }
        return  resList;
    }

    /**
     * 获取加药、结束加药的返回值
     * @param str
     * @return
     */
    public static ResultBean GetResult(String str){
        return QueryResult.readXMLToListHead("rdis",str);
    }
    public static LoginBean GetLogin(String str){
        return QueryResult.readXMLLogin("rdis",str);
    }
}
