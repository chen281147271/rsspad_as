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

package com.rss.pad.fragment;

import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.view.View;
import android.widget.CompoundButton;

import com.rss.pad.R;
import com.rss.pad.activity.LoginActivity;
import com.rss.pad.activity.MainActivity;
import com.rss.pad.core.BaseFragment;
import com.rss.pad.utils.RestartAPPTool;
import com.rss.pad.utils.TokenUtils;
import com.rss.pad.utils.XToastUtils;
import com.xuexiang.xaop.annotation.SingleClick;
import com.xuexiang.xpage.annotation.Page;
import com.xuexiang.xui.widget.dialog.DialogLoader;
import com.xuexiang.xui.widget.textview.supertextview.SuperTextView;
import com.xuexiang.xutil.XUtil;
import com.xuexiang.xutil.app.ActivityUtils;
import com.xuexiang.xutil.app.AppUtils;

import butterknife.BindView;

/**
 * @author xuexiang
 * @since 2019-10-15 22:38
 */
@Page(name = "设置")
public class SettingsFragment extends BaseFragment implements SuperTextView.OnSuperTextViewClickListener {

//    @BindView(R.id.menu_common)
//    SuperTextView menuCommon;
//    @BindView(R.id.menu_privacy)
//    SuperTextView menuPrivacy;
//    @BindView(R.id.menu_push)
//    SuperTextView menuPush;
//    @BindView(R.id.menu_helper)
//    SuperTextView menuHelper;
//    @BindView(R.id.menu_change_account)
//    SuperTextView menuChangeAccount;
//    @BindView(R.id.menu_logout)
//    SuperTextView menuLogout;
    @BindView(R.id.super_switch_tv)
    SuperTextView superTextView_switch;
    @BindView(R.id.menu_save)
    SuperTextView menusave;
    @BindView(R.id.txt_ip)
    SuperTextView txtip;
    @BindView(R.id.txt_port)
    SuperTextView txtport;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_settings;
    }

    @Override
    protected void initViews() {
//        menuCommon.setOnSuperTextViewClickListener(this);
//        menuPrivacy.setOnSuperTextViewClickListener(this);
//        menuPush.setOnSuperTextViewClickListener(this);
//        menuHelper.setOnSuperTextViewClickListener(this);
//        menuChangeAccount.setOnSuperTextViewClickListener(this);
//        menuLogout.setOnSuperTextViewClickListener(this);
        txtip.setCenterEditString(TokenUtils.getsIp());
        txtport.setCenterEditString(TokenUtils.getsPort());
        if(TokenUtils.getsSort().compareTo("pec")==0){
            superTextView_switch.setLeftString("排序方式：百分比");
            superTextView_switch.setSwitchIsChecked(false);
        }else {
            superTextView_switch.setLeftString("排序方式：数量");
            superTextView_switch.setSwitchIsChecked(true);
        }
    }

    @Override
    protected void initListeners() {

        superTextView_switch.setSwitchCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked){
                    superTextView_switch.setLeftString("排序方式：数量");
                }else {
                    superTextView_switch.setLeftString("排序方式：百分比");
                }
            }
        });

        menusave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String ip=txtip.getCenterEditValue();
                String port=txtport.getCenterEditValue();
                String Sort="";
                if(superTextView_switch.getLeftString().compareTo("排序方式：数量")==0){
                    Sort="NUM";
                }else {
                    Sort="pec";
                }
                TokenUtils.setsIp(ip);
                TokenUtils.setsPort(port);
                TokenUtils.setsSort(Sort);

                DialogLoader.getInstance().showConfirmDialog(
                        getContext(),
                        getString(R.string.lab_restrattips),
                        getString(R.string.lab_yes),
                        (dialog, which) -> {
                            dialog.dismiss();
                            showRestar();
                        },
                        getString(R.string.lab_no),
                        (dialog, which) -> dialog.dismiss()
                );

            }
        });
        super.initListeners();
    }

    public void showRestar(){
        DialogLoader.getInstance().showTipDialog(getContext(), -1, "保存配置", "更改配置，需要重启系统！", "重启", (dialog, which) -> {
            //重启app
//            final Intent intent =  getActivity().getApplicationContext().getPackageManager().getLaunchIntentForPackage(getActivity().getApplicationContext().getPackageName());
//            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            startActivity(intent);
//            AppUtils.rebootApp(1000);
//            Intent intent = getActivity().getApplicationContext().getPackageManager()
//                    .getLaunchIntentForPackage(getActivity().getApplication().getPackageName());
//            PendingIntent restartIntent = PendingIntent.getActivity(getActivity().getApplicationContext(), 0, intent, 0);
//            AlarmManager mgr = (AlarmManager)getActivity().getSystemService(Context.ALARM_SERVICE);
//            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 1000, restartIntent); // 1秒钟后重启应用
//            System.exit(0);
//            new Handler().postDelayed(new Runnable() {
//                @Override
//                public void run() {
//                    Intent LaunchIntent = getActivity().getApplicationContext().getPackageManager().getLaunchIntentForPackage(getActivity().getApplication().getPackageName());
//                    LaunchIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//                    startActivity(LaunchIntent);
//                    System.exit(0);
//                }
//            }, 1000);// 1秒钟后重启应用
           AppUtils.exitApp();
        //    RestartAPPTool.restartAPP(getActivity().getApplicationContext(),1000);

        });
    }

    @SingleClick
    @Override
    public void onClick(SuperTextView superTextView) {
//        switch (superTextView.getId()) {
//            case R.id.menu_common:
//            case R.id.menu_privacy:
//            case R.id.menu_push:
//            case R.id.menu_helper:
//                XToastUtils.toast(superTextView.getLeftString());
//                break;
//            case R.id.menu_change_account:
//                XToastUtils.toast(superTextView.getCenterString());
//                break;
//            case R.id.menu_logout:
//                DialogLoader.getInstance().showConfirmDialog(
//                        getContext(),
//                        getString(R.string.lab_logout_confirm),
//                        getString(R.string.lab_yes),
//                        (dialog, which) -> {
//                            dialog.dismiss();
//                            XUtil.getActivityLifecycleHelper().exit();
//                            TokenUtils.handleLogoutSuccess();
////                            Intent intent=new Intent(getContext(), LoginActivity.class);
////                            //启动
////                            startActivity(intent);
//                        },
//                        getString(R.string.lab_no),
//                        (dialog, which) -> dialog.dismiss()
//                );
//                break;
//            default:
//                break;
//        }
    }
}
