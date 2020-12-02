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

package com.rss.pad.fragment.profile;

import android.content.Intent;
import android.util.Log;

import androidx.annotation.Nullable;

import com.rss.pad.activity.DrugDetailsActivity;
import com.rss.pad.activity.LoginActivity;
import com.rss.pad.adapter.entity.DrugInfo;
import com.rss.pad.adapter.entity.LoginBean;
import com.rss.pad.core.BaseFragment;
import com.rss.pad.fragment.AboutFragment;
import com.rss.pad.fragment.SettingsFragment;
import com.rss.pad.R;
import com.rss.pad.utils.TokenUtils;
import com.rss.pad.utils.Utils;
import com.xuexiang.xaop.annotation.SingleClick;
import com.xuexiang.xpage.annotation.Page;
import com.xuexiang.xpage.enums.CoreAnim;
import com.xuexiang.xui.widget.actionbar.TitleBar;
import com.xuexiang.xui.widget.dialog.DialogLoader;
import com.xuexiang.xui.widget.imageview.RadiusImageView;
import com.xuexiang.xui.widget.textview.supertextview.SuperTextView;
import com.xuexiang.xutil.XUtil;
import com.xuexiang.xutil.app.ActivityUtils;

import butterknife.BindView;

import static com.rss.pad.utils.rss.SocketQueryStr.OP_LOGIN;

/**
 * @author xuexiang
 * @since 2019-10-30 00:18
 */
@Page(anim = CoreAnim.none)
public class ProfileFragment extends BaseFragment implements SuperTextView.OnSuperTextViewClickListener {
    //    @BindView(R.id.riv_head_pic)
//    RadiusImageView rivHeadPic;
    @BindView(R.id.menu_logout)
    SuperTextView menulogout;
    @BindView(R.id.uFullName_index)
    SuperTextView uFullName;
    @BindView(R.id.menu_settings)
    SuperTextView menusettings;
    @BindView(R.id.menu_about)
    SuperTextView menuabout;

    @BindView(R.id.txt_ip_index)
    SuperTextView txtipindex;
    @BindView(R.id.txt_port_index)
    SuperTextView txtportindex;
    @BindView(R.id.txt_sort_index)
    SuperTextView txtsortindex;

    /**
     * @return 返回为 null意为不需要导航栏
     */
    @Override
    protected TitleBar initTitle() {
        return null;
    }

    /**
     * 布局的资源id
     *
     * @return
     */
    @Override
    protected int getLayoutId() {
        return R.layout.fragment_profile;
    }

    /**
     * 初始化控件
     */
    @Override
    protected void initViews() {
        txtipindex.setRightString(TokenUtils.getsIp());
        txtportindex.setRightString(TokenUtils.getsPort());
        if(TokenUtils.getsSort().compareTo("NUM")==0) {
            txtsortindex.setRightString("数量");
        }else {
            txtsortindex.setRightString("百分比");
        }
        uFullName.setRightString(TokenUtils.getsUserFullName());
    }

    @Override
    protected void initListeners() {
        menulogout.setOnSuperTextViewClickListener(this);
        menusettings.setOnSuperTextViewClickListener(this);
        menuabout.setOnSuperTextViewClickListener(this);
    }

    @SingleClick
    @Override
    public void onClick(SuperTextView view) {
        switch(view.getId()) {
            case R.id.menu_logout:
                DialogLoader.getInstance().showConfirmDialog(
                        getContext(),
                        getString(R.string.lab_logout_confirm),
                        getString(R.string.lab_yes),
                        (dialog, which) -> {
                            dialog.dismiss();
                            // XUtil.getActivityLifecycleHelper().exit();
                            TokenUtils.handleLogoutSuccess();
                            Intent intent=new Intent(getContext(),LoginActivity.class);
                            startActivityForResult(intent,OP_LOGIN);
                        },
                        getString(R.string.lab_no),
                        (dialog, which) -> dialog.dismiss()
                );
                break;
            case R.id.menu_settings:
                openNewPage(SettingsFragment.class);
                break;
            case R.id.menu_about:
                openNewPage(AboutFragment.class);
                break;
            default:
                break;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        LoginBean loginBean=new LoginBean();
        loginBean=(LoginBean)data.getSerializableExtra("data");
        uFullName.setRightString(loginBean.getUserFullName());
    }
}
