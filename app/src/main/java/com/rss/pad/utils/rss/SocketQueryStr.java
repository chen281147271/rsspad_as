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

public class SocketQueryStr {
    //操作码
    /**
     * 默认请求
     */
    public static  final int OPDEFAULT=0;
    /**
     * 首页请求
     */
    public static  final int OPINFO=1;
    /**
     * 详情页请求
     */
    public static  final int OPDETAIL=2;
    /**
     * 详情页-开始加药
     */
    public static  final int OPDETAIL_START=3;
    /**
     * 详情页-结束加药
     */
    public static  final int OPDETAIL_END=4;
    /**
     * 登入请求
     */
    public static  final int OP_LOGIN=5;
    /**
     * 扫码枪的请求直接获取明细
     */
    public static  final int OP_DETAIL_QR=6;
    //首页请求
    public static final String DRUGINFO_1 = "<rdis><operCode>1002</operCode><queryCond><code>";
    public static final String DRUGINFO_2 = "</code><order>";
    public static final String DRUGINFO_3 = "</order></queryCond></rdis>";
    //药槽详情
    public static final String DRUGDETAILS_1="<rdis><operCode>1003</operCode><queryCond><code>";
    public static final String DRUGDETAILS_2="<code></queryCond></rdis>";
    //开始加药
    public static final String DRUGDETAILS_START_1="<rdis><operCode>1004</operCode><locInfo><locId>";
    public static final String DRUGDETAILS_START_2="</locId></locInfo></rdis>";
    //结束加药
    public static final String DRUGDETAILS_END_1="<rdis><operCode>1005</operCode><userId>";
    public static final String DRUGDETAILS_END_2="</userId><locInfo><locId>";
    public static final String DRUGDETAILS_END_3="</locId><curNum>";
    public static final String DRUGDETAILS_END_4="</curNum></locInfo></rdis>";
    //登入请求
    public static final String LOGIN_1="<rdis><operCode>1001</operCode><LoginInfo><name>";
    public static final String LOGIN_2="</name><password>";
    public static final String LOGIN_3="</password></LoginInfo></rdis>";
}
