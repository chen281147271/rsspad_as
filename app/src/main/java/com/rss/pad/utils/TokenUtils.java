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

package com.rss.pad.utils;

import android.content.Context;
import android.content.Intent;

import com.rss.pad.activity.LoginActivity;
import com.rss.pad.activity.MainActivity;
import com.rss.pad.adapter.entity.LoginBean;
import com.umeng.analytics.MobclickAgent;
import com.xuexiang.xutil.app.ActivityUtils;
import com.xuexiang.xutil.common.StringUtils;

/**
 * Token管理工具
 *
 * @author xuexiang
 * @since 2019-11-17 22:37
 */
public final class TokenUtils {

    private static String sToken;
    private static String sUserId;
    private static String sUserName;
    private static String sUserFullName;
    private static String sSort;
    private static String sIp;
    private static String sPort;


    private static final String KEY_TOKEN = "com.rss.pad.utils.KEY_TOKEN";
    private static final String KEY_UID = "com.rss.pad.utils.KEY_UID";
    private static final String KEY_UNAME = "com.rss.pad.utils.KEY_UNAME";
    private static final String KEY_UFULLNAME = "com.rss.pad.utils.KEY_UFULLNAME";
    private static final String KEY_SORT = "com.rss.pad.utils.KEY_SORT";
    private static final String KEY_IP = "com.rss.pad.utils.KEY_IP";
    private static final String KEY_PORT = "com.rss.pad.utils.KEY_PORT";

    private TokenUtils() {
        throw new UnsupportedOperationException("u can't instantiate me...");
    }

    private static final String KEY_PROFILE_CHANNEL = "github";

    /**
     * 初始化Token信息
     */
    public static void init(Context context) {
        MMKVUtils.init(context);
        sToken = MMKVUtils.getString(KEY_TOKEN, "");
        sUserId = MMKVUtils.getString(KEY_UID, "0");
        sUserName = MMKVUtils.getString(KEY_UNAME, "");
        sUserFullName = MMKVUtils.getString(KEY_UFULLNAME, "");
        sSort=MMKVUtils.getString(KEY_SORT, "pec");
        sIp=MMKVUtils.getString(KEY_IP, "172.16.75.166");
        sPort=MMKVUtils.getString(KEY_PORT, "2000");
        if(sIp.compareTo("")==0){
            sIp="172.16.75.166";
        }
        if(sSort.compareTo("")==0){
            sSort="pec";
        }
        if(sPort.compareTo("")==0 || !isNumericZidai(sPort)){
            sPort="2000";
        }
        if(sUserId.compareTo("")==0){
            sUserId="0";
        }
        if(sUserFullName.compareTo("")==0){
            sUserFullName="游客";
        }
    }
    public static boolean isNumericZidai(String str) {
        for (int i = 0; i < str.length(); i++) {
            System.out.println(str.charAt(i));
            if (!Character.isDigit(str.charAt(i))) {
                return false;
            }
        }
        return true;
    }
    public static void setToken(String token) {
        sToken = token;
        MMKVUtils.put(KEY_TOKEN, token);
    }

    public static void clearToken() {
        sToken = null;
        MMKVUtils.remove(KEY_TOKEN);
    }
    public static void clearALL() {
        sToken = null;
        MMKVUtils.remove(KEY_TOKEN);
        sUserId = null;
        MMKVUtils.remove(KEY_UID);
        sUserName = null;
        MMKVUtils.remove(KEY_UNAME);
        sUserFullName = null;
        MMKVUtils.remove(KEY_UFULLNAME);
    }

    public static String getToken() {
        return sToken;
    }

    public static boolean hasToken() {
        return MMKVUtils.containsKey(KEY_TOKEN);
    }

    /**
     * 处理登录成功的事件
     *
     * @param token 账户信息
     */
    public static boolean handleLoginSuccess(LoginBean loginBean) {
        if (loginBean!=null && loginBean.getUserId().compareTo("0")!=0) {
           // XToastUtils.success("登录成功！");
            MobclickAgent.onProfileSignIn(KEY_PROFILE_CHANNEL, loginBean.getUserId());
           // setToken(token);
            setsUserFullName(loginBean.getUserFullName());
            setsUserId(loginBean.getUserId());
            setsUserName(loginBean.getUserName());
            return true;
        } else {
            XToastUtils.error("登录失败！");
            return false;
        }
    }

    /**
     * 处理登出的事件
     */
    public static void handleLogoutSuccess() {
        MobclickAgent.onProfileSignOff();
        //登出时，清除账号信息
        clearALL();
        XToastUtils.success("登出成功！");
        //跳转到登录页
       // ActivityUtils.startActivity(LoginActivity.class);

    }



    public static String getsUserId() {
        return sUserId;
    }

    public static void setsUserId(String sUserId) {
        TokenUtils.sUserId = sUserId;
        MMKVUtils.put(KEY_UID, sUserId);
    }

    public static String getsUserName() {
        return sUserName;
    }

    public static void setsUserName(String sUserName) {
        TokenUtils.sUserName = sUserName;
        MMKVUtils.put(KEY_UNAME, sUserName);
    }

    public static String getsUserFullName() {
        return sUserFullName;
    }

    public static void setsUserFullName(String sUserFullName) {
        TokenUtils.sUserFullName = sUserFullName;
        MMKVUtils.put(KEY_UFULLNAME, sUserFullName);
    }

    public static String getsSort() {
        return sSort;
    }

    public static void setsSort(String sSort) {
        TokenUtils.sSort = sSort;
        MMKVUtils.put(KEY_SORT, sSort);
    }

    public static String getsIp() {
        return sIp;
    }

    public static void setsIp(String sIp) {
        TokenUtils.sIp = sIp;
        MMKVUtils.put(KEY_IP, sIp);
    }

    public static String getsPort() {
        return sPort;
    }

    public static void setsPort(String sPort) {
        TokenUtils.sPort = sPort;
        MMKVUtils.put(KEY_PORT, sPort);
    }
}
