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

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.rss.pad.adapter.entity.DrugInfo;

public class SocketReceiver extends BroadcastReceiver {
    private SocketListener listener;

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        switch (action){
            case "RSSSOCKETMSG":
                if (listener != null) {
                    String data = intent.getStringExtra("data");
                    listener.GetSocketData(data);
                }
                break;
            case "RSSSOCKETCONNECTED":
                if (listener != null) {
                    Boolean data = intent.getBooleanExtra("data",false);
                    listener.GetSocketConnected(data);
                }
                break;
            case "RSSSOCKETMSGBYCODE":
                if (listener != null) {
                    String data = intent.getStringExtra("data");
                    listener.GetSocketDataByCode(data);
                }
                break;
            case "RSSSOCKETSTARTREQUEST":
                if (listener != null) {
                    String data = intent.getStringExtra("data");
                    int op = intent.getIntExtra("op",0);
                    listener.StartRequest(data,op);
                }
                break;
            case "RSSSOCKETDETAILSMSG":
                if (listener != null) {
                    String data = intent.getStringExtra("data");
                    listener.GetSocketDetailsData(data);
                }
                break;
            case "RSSSOCKETLOGINMSG":
                if (listener != null) {
                    String data = intent.getStringExtra("data");
                    listener.GetSocketLoginData(data);
                }
                break;
            case "RSSDRUGDETAILQR":
                if (listener != null) {
                    listener.getDrugDetailQR((DrugInfo)intent.getSerializableExtra("data"));
                }
                break;
        }
    }

    public void setListener(SocketListener listener) {
        this.listener = listener;
    }
}
