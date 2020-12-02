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

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;

import com.rss.pad.R;
import com.rss.pad.activity.DrugDetailsActivity;
import com.rss.pad.activity.MainActivity;
import com.rss.pad.adapter.entity.DrugInfo;
import com.rss.pad.adapter.entity.LoginBean;
import com.rss.pad.core.BaseFragment;
import com.rss.pad.fragment.profile.ProfileFragment;
import com.rss.pad.utils.RandomUtils;
import com.rss.pad.utils.SettingUtils;
import com.rss.pad.utils.TokenUtils;
import com.rss.pad.utils.Utils;
import com.rss.pad.utils.XToastUtils;
import com.rss.pad.utils.rss.SocketListener;
import com.rss.pad.utils.rss.SocketReceiver;
import com.rss.pad.utils.rss.XMLHelper;
import com.xuexiang.xaop.annotation.SingleClick;
import com.xuexiang.xpage.annotation.Page;
import com.xuexiang.xpage.enums.CoreAnim;
import com.xuexiang.xui.utils.CountDownButtonHelper;
import com.xuexiang.xui.utils.ResUtils;
import com.xuexiang.xui.utils.ThemeUtils;
import com.xuexiang.xui.utils.WidgetUtils;
import com.xuexiang.xui.widget.actionbar.TitleBar;
import com.xuexiang.xui.widget.button.roundbutton.RoundButton;
import com.xuexiang.xui.widget.dialog.LoadingDialog;
import com.xuexiang.xui.widget.edittext.materialedittext.MaterialEditText;
import com.xuexiang.xutil.app.ActivityUtils;

import butterknife.BindView;
import butterknife.OnClick;

import static com.rss.pad.utils.rss.SocketQueryStr.LOGIN_1;
import static com.rss.pad.utils.rss.SocketQueryStr.LOGIN_2;
import static com.rss.pad.utils.rss.SocketQueryStr.LOGIN_3;
import static com.rss.pad.utils.rss.SocketQueryStr.OP_LOGIN;


/**
 * 登录页面
 *
 * @author xuexiang
 * @since 2019-11-17 22:15
 */
@Page(anim = CoreAnim.none)
public class LoginFragment extends BaseFragment implements SocketListener {

    @BindView(R.id.et_phone_number)
    MaterialEditText etPhoneNumber;
    @BindView(R.id.et_verify_code)
    MaterialEditText etVerifyCode;
//    @BindView(R.id.btn_get_verify_code)
//    RoundButton btnGetVerifyCode;

    private CountDownButtonHelper mCountDownHelper;
    boolean runflag=true;
    LoadingDialog mLoadingDialog;
    boolean show=false;
    private String msg;
    private Handler handler ;
    String lastQuery;
    private SocketReceiver socketReceiver;
    LoginBean loginBean;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_login;
    }

    @Override
    protected TitleBar initTitle() {
        TitleBar titleBar = super.initTitle()
                .setImmersive(true);
        titleBar.setBackgroundColor(Color.TRANSPARENT);
        titleBar.setTitle("");
//        titleBar.setLeftImageDrawable(ResUtils.getVectorDrawable(getContext(), R.drawable.ic_login_close));
        titleBar.setActionTextColor(ThemeUtils.resolveColor(getContext(), R.attr.colorAccent));
        titleBar.addAction(new TitleBar.TextAction(R.string.title_jump_login) {
            @Override
            public void performAction(View view) {
                onLoginVisitor();
            }
        });
        return titleBar;
    }

    @Override
    protected void initViews() {
//        mCountDownHelper = new CountDownButtonHelper(btnGetVerifyCode, 60);
//
//        //隐私政策弹窗
//        if (!SettingUtils.isAgreePrivacy()) {
//            Utils.showPrivacyDialog(getContext(), (dialog, which) -> {
//                dialog.dismiss();
//                SettingUtils.setIsAgreePrivacy(true);
//            });
//        }
        loginBean=new LoginBean();
        initSocketReceiver();
        new Thread(new showLoading()).start();
        handler=new Handler(){
            @SuppressLint("HandlerLeak")
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){
                    case 1:
                        XToastUtils.toast("用户名或密码错误！");
                        break;
                    case 2:
                        XToastUtils.toast("请求失败！请检查盒剂主程序是否开启！");
                        break;
                    case 3:
                        XToastUtils.success("登入成功！");
                        break;
                }
                mLoadingDialog.dismiss();
            }
        };
        mLoadingDialog = WidgetUtils.getLoadingDialog(getContext())
                .setLoadingIcon(R.drawable.ic_appicon)
                .setIconScale(0.4F)
                .setLoadingSpeed(8);
    }

    @SingleClick
    @OnClick({ R.id.btn_login})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_login:
                if (etPhoneNumber.validate()) {
                    if (etVerifyCode.validate()) {
                        //loginByVerifyCode(etPhoneNumber.getEditValue(), etVerifyCode.getEditValue());
                        mLoadingDialog.show();
                        msg=null;
                        show=true;
                        lastQuery= LOGIN_1+etPhoneNumber.getEditValue()+LOGIN_2+etVerifyCode.getEditValue()+LOGIN_3;
                        Log.d("TAG", "initListeners: 下拉刷新！");
                        Utils.sendBroadcast(getContext(),"RSSSOCKETSTARTREQUEST","data",lastQuery,"op",OP_LOGIN);
                    }
                }
                break;
            default:
                break;
        }
    }

    /**
     * 获取验证码
     */
    private void getVerifyCode(String phoneNumber) {
        // TODO: 2020/8/29 这里只是界面演示而已
        XToastUtils.warning("只是演示，验证码请随便输");
        mCountDownHelper.start();
    }

    /**
     * 根据验证码登录
     *
     * @param phoneNumber 手机号
     * @param verifyCode  验证码
     */
    private void loginByVerifyCode(String phoneNumber, String verifyCode) {
        // TODO: 2020/8/29 这里只是界面演示而已
        onLoginSuccess();
    }

    /**
     * 登录成功的处理
     */
    private void onLoginSuccess() {
        String token = RandomUtils.getRandomNumbersAndLetters(16);
        if (TokenUtils.handleLoginSuccess(loginBean)) {
           // popToBack();
           // ActivityUtils.startActivity(MainActivity.class);
            Intent intent = new Intent(getContext(), ProfileFragment.class);
            intent.putExtra("data",loginBean);
            getActivity().setResult(Activity.RESULT_OK,intent);
            popToBack();
        }
    }
    private void onLoginVisitor() {
        String token = RandomUtils.getRandomNumbersAndLetters(16);
            loginBean.setUserFullName("游客");
            // popToBack();
            // ActivityUtils.startActivity(MainActivity.class);
            Intent intent = new Intent(getContext(), ProfileFragment.class);
            intent.putExtra("data",loginBean);
            getActivity().setResult(Activity.RESULT_OK,intent);
            popToBack();
    }

    @Override
    public void onDestroyView() {
        if (mCountDownHelper != null) {
            mCountDownHelper.recycle();
        }
        super.onDestroyView();
    }

    @Override
    public void GetSocketData(String msg) {

    }

    @Override
    public void GetSocketConnected(boolean connected) {

    }

    @Override
    public void GetSocketDataByCode(String msg) {

    }

    @Override
    public void StartRequest(String msg, int op) {

    }

    @Override
    public void GetSocketDetailsData(String msg) {

    }

    @Override
    public void GetSocketLoginData(String msg) {
        this.msg=msg;
    }
    private  class showLoading implements Runnable{
        @Override
        public void run() {
            try{
                while (runflag) {
                    if (msg!=null && msg.compareTo("FAILED")!=0 && show) {
                        show = false;
                        loginBean= XMLHelper.GetLogin(msg);
                        if(loginBean.getUserId().compareTo("0")==0) {
                            Message mg = new Message();
                            mg.what = 1;
                            handler.sendMessage(mg);
                        }else {
                            onLoginSuccess();
                            Message mg = new Message();
                            mg.what = 3;
                            handler.sendMessage(mg);
                        }
                    }else if(msg!=null && msg.compareTo("FAILED")==0 && show) {
                        show = false;
                        Message mg =new Message();
                        mg.what=2;
                        handler.sendMessage(mg);
                    }
                    Thread.sleep(100);
                }
            }catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
    private void initSocketReceiver() {

        socketReceiver=new SocketReceiver();
        socketReceiver.setListener(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction("RSSSOCKETLOGINMSG");
        filter.addAction("RSSSOCKETCONNECTED");
        getActivity().getApplicationContext().registerReceiver(socketReceiver,filter);
    }

    @Override
    public void onDestroy() {
        getActivity().getApplicationContext().unregisterReceiver(socketReceiver);
        runflag=false;
        super.onDestroy();
    }

    @Override
    public void getDrugDetailQR(DrugInfo data) {

    }
}

