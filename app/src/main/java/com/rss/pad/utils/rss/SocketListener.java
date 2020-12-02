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

import com.rss.pad.adapter.entity.DrugInfo;

public interface SocketListener {
    /**
     * 首页数据
     * @param msg
     */
    public void GetSocketData(String msg);

    /**
     * Socket是否链接
     * @param connected
     */
    public void GetSocketConnected(boolean connected);

    /**
     * 首页按code查找药品
     * @param msg
     */
    public void GetSocketDataByCode(String msg);

    /**
     * 开始请求
     * @param msg
     */
    public void StartRequest(String msg,int op);

    /**
     * 药品明细页数据
     * @param msg
     */
    public void GetSocketDetailsData(String msg);

    /**
     * 登入页面数据
     * @param msg
     */
    public void GetSocketLoginData(String msg);

    /**
     * 扫码枪的请求直接获取明细
     * @param data
     */
    public void getDrugDetailQR(DrugInfo data);
}
