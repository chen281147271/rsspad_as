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

package com.rss.pad.activity;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.os.StrictMode;
import android.speech.RecognizerIntent;
import android.text.TextUtils;
import android.util.Log;
import android.view.ActionProvider;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.SearchView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.viewpager.widget.ViewPager;

import com.easysocket.EasySocket;
import com.easysocket.config.EasySocketOptions;
import com.easysocket.entity.OriginReadData;
import com.easysocket.entity.SocketAddress;
import com.easysocket.interfaces.conn.ISocketActionListener;
import com.easysocket.interfaces.conn.SocketActionListener;
import com.easysocket.utils.LogUtil;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.rss.pad.adapter.entity.DrugInfo;
import com.rss.pad.fragment.AboutFragment;
import com.rss.pad.fragment.SettingsFragment;
import com.rss.pad.R;
import com.rss.pad.core.BaseActivity;
import com.rss.pad.core.BaseFragment;
import com.rss.pad.fragment.news.NewsFragment;
import com.rss.pad.fragment.profile.ProfileFragment;
import com.rss.pad.fragment.trending.TrendingFragment;
import com.rss.pad.utils.MMKVUtils;
import com.rss.pad.utils.TokenUtils;
import com.rss.pad.utils.Utils;
import com.rss.pad.utils.XToastUtils;
import com.rss.pad.utils.rss.NetWorkUtils;
import com.rss.pad.utils.rss.QRListener;
import com.rss.pad.utils.rss.QRReceiver;
import com.rss.pad.utils.rss.SocketListener;
import com.rss.pad.utils.rss.SocketReceiver;
import com.rss.pad.utils.rss.XMLHelper;
import com.xuexiang.xaop.annotation.SingleClick;
import com.xuexiang.xui.adapter.FragmentAdapter;
import com.xuexiang.xui.utils.ResUtils;
import com.xuexiang.xui.utils.SnackbarUtils;
import com.xuexiang.xui.utils.ThemeUtils;
import com.xuexiang.xui.widget.imageview.RadiusImageView;
import com.xuexiang.xui.widget.searchview.MaterialSearchView;
import com.xuexiang.xutil.XUtil;
import com.xuexiang.xutil.common.ClickUtils;
import com.xuexiang.xutil.common.CollectionUtils;
import com.xuexiang.xutil.display.Colors;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

import static com.rss.pad.utils.rss.SocketQueryStr.OPDEFAULT;
import static com.rss.pad.utils.rss.SocketQueryStr.OPDETAIL;
import static com.rss.pad.utils.rss.SocketQueryStr.OPINFO;
import static com.rss.pad.utils.rss.SocketQueryStr.OP_LOGIN;

/**
 * 程序主页面,只是一个简单的Tab例子
 *
 * @author xuexiang
 * @since 2019-07-07 23:53
 */
public class MainActivity extends BaseActivity implements
        View.OnClickListener, ViewPager.OnPageChangeListener,
        BottomNavigationView.OnNavigationItemSelectedListener,
        ClickUtils.OnClick2ExitListener, Toolbar.OnMenuItemClickListener, SocketListener,QRListener {
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.view_pager)
    ViewPager viewPager;
    @BindView(R.id.search_view)
    MaterialSearchView mSearchView;
    private Unbinder mUnbinder;
    /**
     * 底部导航栏
     */
    @BindView(R.id.bottom_navigation)
    BottomNavigationView bottomNavigation;
    /**
     * 侧边栏
     */
//    @BindView(R.id.nav_view)
//    NavigationView navView;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;

    private String[] mTitles;

    // 是否已连接
    private boolean isConnected;
    private ISocketActionListener socketActionListener;
    String msg;
    Context mContext;
    boolean show;//开始请求
    int trycount;//请求重试次数
    boolean newmsg = false;//接收到了一个完整的信息
    long start;//开始请求计时
    String lastQuery;//最后一次请求的查询字符串
    private SocketReceiver socketReceiver;
    int op = 0;//操作码
    StringBuilder sb;
    boolean runflag = true;
    Menu menu;
    /**
     * 缓存接受的数据
     */
    List result = new ArrayList();
    byte[] all;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mUnbinder = ButterKnife.bind(this);
        sb = new StringBuilder();
        new Thread(new StatRequest()).start();
        initViews();

        iniSocket();

        initSocketReceiver();

        initListeners();

    }

    @Override
    protected boolean isSupportSlideBack() {
        return false;
    }

    private void initViews() {
        mTitles = ResUtils.getStringArray(R.array.home_titles);
        toolbar.setTitle(mTitles[0]);
        // toolbar.inflateMenu(R.menu.menu_main);
        toolbar.setOnMenuItemClickListener(this);


        // initHeader();

        //主页内容填充
        BaseFragment[] fragments = new BaseFragment[]{
                new NewsFragment(),
                //new TrendingFragment(),
                new ProfileFragment()
        };
        FragmentAdapter<BaseFragment> adapter = new FragmentAdapter<>(getSupportFragmentManager(), fragments);
        viewPager.setOffscreenPageLimit(mTitles.length - 1);
        viewPager.setAdapter(adapter);

        setSupportActionBar(toolbar);
        mSearchView.setVoiceSearch(false);
//        mSearchView.setCursorDrawable(R.drawable.custom_cursor);//自定义光标颜色
        mSearchView.setEllipsize(true);
        //mSearchView.setSuggestions(getResources().getStringArray(R.array.query_suggestions));
        mSearchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                // SnackbarUtils.Long(mSearchView, "Query: " + query).show();
//                Intent intent = new Intent();
//                intent.setAction("RSSSOCKETMSGBYCODE");
//                intent.putExtra("data",query);
//                sendBroadcast(intent);

                Utils.sendBroadcast(mContext, "RSSSOCKETMSGBYCODE", "data", query, "op", OPINFO);

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                //Do some magic
                return false;
            }
        });
        mSearchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {
                //Do some magic
            }

            @Override
            public void onSearchViewClosed() {
                //Do some magic
            }
        });
        mSearchView.setSubmitOnClick(true);

        this.mContext = this;
    }

//    private void initHeader() {
//        navView.setItemIconTintList(null);
//        View headerView = navView.getHeaderView(0);
//        LinearLayout navHeader = headerView.findViewById(R.id.nav_header);
//        RadiusImageView ivAvatar = headerView.findViewById(R.id.iv_avatar);
//        TextView tvAvatar = headerView.findViewById(R.id.tv_avatar);
//        TextView tvSign = headerView.findViewById(R.id.tv_sign);
//
//        if (Utils.isColorDark(ThemeUtils.resolveColor(this, R.attr.colorAccent))) {
//            tvAvatar.setTextColor(Colors.WHITE);
//            tvSign.setTextColor(Colors.WHITE);
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                ivAvatar.setImageTintList(ResUtils.getColors(R.color.xui_config_color_white));
//            }
//        } else {
//            tvAvatar.setTextColor(ThemeUtils.resolveColor(this, R.attr.xui_config_color_title_text));
//            tvSign.setTextColor(ThemeUtils.resolveColor(this, R.attr.xui_config_color_explain_text));
//            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
//                ivAvatar.setImageTintList(ResUtils.getColors(R.color.xui_config_color_gray_3));
//            }
//        }
//
//        // TODO: 2019-10-09 初始化数据
//        ivAvatar.setImageResource(R.drawable.ic_default_head);
//        tvAvatar.setText(R.string.app_name);
//        tvSign.setText("这个家伙很懒，什么也没有留下～～");
//        navHeader.setOnClickListener(this);
//    }

    protected void initListeners() {
//        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
//        drawerLayout.addDrawerListener(toggle);
//        toggle.syncState();

        //侧边栏点击事件
//        navView.setNavigationItemSelectedListener(menuItem -> {
//            if (menuItem.isCheckable()) {
//                drawerLayout.closeDrawers();
//                return handleNavigationItemSelected(menuItem);
//            } else {
//                switch (menuItem.getItemId()) {
//                    case R.id.nav_settings:
//                        openNewPage(SettingsFragment.class);
//                        break;
//                    case R.id.nav_about:
//                        openNewPage(AboutFragment.class);
//                        break;
//                    default:
//                        XToastUtils.toast("点击了:" + menuItem.getTitle());
//                        break;
//                }
//            }
//            return true;
//        });

        //主页事件监听
        viewPager.addOnPageChangeListener(this);
        bottomNavigation.setOnNavigationItemSelectedListener(this);
    }

    /**
     * 处理侧边栏点击事件
     *
     * @param menuItem
     * @return
     */
//    private boolean handleNavigationItemSelected(@NonNull MenuItem menuItem) {
//        int index = CollectionUtils.arrayIndexOf(mTitles, menuItem.getTitle());
//        if (index != -1) {
//            toolbar.setTitle(menuItem.getTitle());
//            viewPager.setCurrentItem(index, false);
//            return true;
//        }
//        return false;
//    }
    @Override
    public boolean onMenuItemClick(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_privacy:
                Utils.showPrivacyDialog(this, null);
                break;
            default:
                break;
        }
        return false;
    }

    @SingleClick
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.nav_header:
                XToastUtils.toast("点击头部！");
                break;
            default:
                break;
        }
    }

    //=============ViewPager===================//

    @Override
    public void onPageScrolled(int i, float v, int i1) {

    }

    @Override
    public void onPageSelected(int position) {
        // toolbar.removeAllViews();
        try {
            MenuItem item2 = menu.findItem(R.id.action_search2);
            MenuItem item = bottomNavigation.getMenu().getItem(position);
            toolbar.setTitle(item.getTitle());
            item.setChecked(true);
            if (position == 0) {
                item2.setVisible(true);
                // toolbar.setVisibility(View.VISIBLE);
            } else if (position == 1) {
                //  toolbar.setVisibility(View.GONE);
                item2.setVisible(false);
            }
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
        //updateSideNavStatus(item);
    }

    @Override
    public void onPageScrollStateChanged(int i) {

    }

    //================Navigation================//

    /**
     * 底部导航栏点击事件
     *
     * @param menuItem
     * @return
     */
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        int index = CollectionUtils.arrayIndexOf(mTitles, menuItem.getTitle());
        if (index != -1) {
            toolbar.setTitle(menuItem.getTitle());
            viewPager.setCurrentItem(index, false);

            //updateSideNavStatus(menuItem);
            return true;
        }
        return false;
    }

    /**
     * 更新侧边栏菜单选中状态
     *
     * @param menuItem
     */
//    private void updateSideNavStatus(MenuItem menuItem) {
//        MenuItem side = navView.getMenu().findItem(menuItem.getItemId());
//        if (side != null) {
//            side.setChecked(true);
//        }
//    }

    /**
     * 菜单、返回键响应
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            ClickUtils.exitBy2Click(2000, this);
        }
        return true;
    }

    @Override
    public void onRetry() {
        XToastUtils.toast("再按一次退出程序");
    }

    @Override
    public void onExit() {
        XUtil.exitApp();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        getMenuInflater().inflate(R.menu.menu_search, menu);

        MenuItem item = menu.findItem(R.id.action_search2);
        mSearchView.setMenuItem(item);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (mSearchView.isSearchOpen()) {
            mSearchView.closeSearch();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == MaterialSearchView.REQUEST_VOICE && resultCode == RESULT_OK) {
            ArrayList<String> matches = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            if (matches != null && matches.size() > 0) {
                String searchWrd = matches.get(0);
                if (!TextUtils.isEmpty(searchWrd)) {
                    mSearchView.setQuery(searchWrd, false);
                }
            }

            return;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        System.out.println("onDestroy");
        mUnbinder.unbind();
        runflag = false;
        EasySocket.getInstance().destroyConnection();
        unregisterReceiver(socketReceiver);
//        System.exit(0);
//        android.os.Process.killProcess(android.os.Process.myPid());
        super.onDestroy();
    }

    @Override
    protected void onStop() {

        System.out.println("onStop");
        //this.finish();
        super.onStop();
    }

    private void iniSocket() {
        // 初始化socket
        initEasySocket();
        // 监听socket行为
        EasySocket.getInstance().subscribeSocketAction(socketActionListener);
    }

    /**
     * 初始化EasySocket
     */
    private void initEasySocket() {
        socketActionListener = new SocketActionListener() {
            /**
             * socket连接成功
             * @param socketAddress
             */
            @Override
            public void onSocketConnSuccess(SocketAddress socketAddress) {
                super.onSocketConnSuccess(socketAddress);
                LogUtil.d("连接成功");
                // controlConnect.setText("socket已连接，点击断开连接");
                isConnected = true;
//                MMKVUtils.put("RSSSOCKETCONNECTED", isConnected);
//                Intent intent = new Intent();
//                intent.setAction("RSSSOCKETCONNECTED");
//                intent.putExtra("data",isConnected);
//                sendBroadcast(intent);

                Utils.sendBroadcast(mContext, "RSSSOCKETCONNECTED", "data", isConnected, "op", OPDEFAULT);
            }

            /**
             * socket连接失败
             * @param socketAddress
             * @param isNeedReconnect 是否需要重连
             */
            @Override
            public void onSocketConnFail(SocketAddress socketAddress, Boolean isNeedReconnect) {
                super.onSocketConnFail(socketAddress, false);
                //controlConnect.setText("socket连接被断开，点击进行连接");
                isConnected = false;
//                MMKVUtils.put("RSSSOCKETCONNECTED", isConnected);
//                Intent intent = new Intent();
//                intent.setAction("RSSSOCKETCONNECTED");
//                intent.putExtra("data",isConnected);
//                sendBroadcast(intent);
                Utils.sendBroadcast(mContext, "RSSSOCKETCONNECTED", "data", isConnected, "op", OPDEFAULT);
            }

            /**
             * socket断开连接
             * @param socketAddress
             * @param isNeedReconnect 是否需要重连
             */
            @Override
            public void onSocketDisconnect(SocketAddress socketAddress, Boolean isNeedReconnect) {
                super.onSocketDisconnect(socketAddress, isNeedReconnect);
                LogUtil.d("socket断开连接，是否需要重连：" + isNeedReconnect);
                // controlConnect.setText("socket连接被断开，点击进行连接");
                isConnected = false;
//                MMKVUtils.put("RSSSOCKETCONNECTED", isConnected);
//                Intent intent = new Intent();
//                intent.setAction("RSSSOCKETCONNECTED");
//                intent.putExtra("data",isConnected);
//                sendBroadcast(intent);
                Utils.sendBroadcast(mContext, "RSSSOCKETCONNECTED", "data", isConnected, "op", OPDEFAULT);
            }

            /**
             * socket接收的数据
             * @param socketAddress
             * @param originReadData
             */
            @Override
            public void onSocketResponse(SocketAddress socketAddress, OriginReadData originReadData) {
                super.onSocketResponse(socketAddress, originReadData);
                //LogUtil.d("socket监听器收到数据=" + originReadData.getBodyString());
                if (msg == null) {
                    msg = "";
                    sb.delete(0, sb.length());
                }
                // sb.append(originReadData.getBodyString());

                if (all == null) {
                    all = new byte[originReadData.getBodyData().length];
                    System.arraycopy(originReadData.getBodyData(), 0, all, 0, originReadData.getBodyData().length);
                } else {
                    int len = all.length;
                    byte[] temp = new byte[all.length];
                    System.arraycopy(all, 0, temp, 0, all.length);
                    all = new byte[originReadData.getBodyData().length + len];
                    System.arraycopy(temp, 0, all, 0, temp.length);
                    System.arraycopy(originReadData.getBodyData(), 0, all, temp.length, originReadData.getBodyData().length);
                }
                if (all != null) {
                    try {
                        msg = new String(all, "UTF-8");
                        // System.out.println("str:"+msg);
                    } catch (UnsupportedEncodingException e) {
                        e.printStackTrace();
                    }
                }
                // msg+= originReadData.getBodyString();
                if (msg.indexOf("</rdis>") != -1) {
                    // msg=sb.toString();
                    System.out.println("完整数据=" + msg);
                    newmsg = true;
                    all = null;
                }
            }
        };
        // socket配置
        EasySocketOptions options = new EasySocketOptions.Builder()
                .setSocketAddress(new SocketAddress(TokenUtils.getsIp(), Integer.parseInt(TokenUtils.getsPort()))) // 主机地址
                .setConnectTimeout(1000)
                .setRequestTimeout(1000)
                .setOpenRequestTimeout(false)
                .setMaxReadBytes(5 * 1024)
                // 最好定义一个消息协议，方便解决 socket黏包、分包的问题
                // .setReaderProtocol(new DefaultMessageProtocol()) // 默认的消息协议
                .build();

        // 初始化EasySocket
        EasySocket.getInstance()
                .options(options) // 项目配置
                .createConnection();// 创建一个socket连接
    }

    public static byte[] byteMerger(byte[] byte_1, byte[] byte_2) {
        byte[] byte_3 = new byte[byte_1.length + byte_2.length];
        System.arraycopy(byte_1, 0, byte_3, 0, byte_1.length);
        System.arraycopy(byte_2, 0, byte_3, byte_1.length, byte_2.length);
        return byte_3;
    }

    private class StatRequest implements Runnable {
        long haoshi;

        @Override
        public void run() {
            try {
                while (runflag) {
                    if (newmsg && show) {
                        show = false;
                        newmsg = false;
                        sendmsg(true);
                    } else if (show && !newmsg) {
                        haoshi = System.currentTimeMillis() - start;
                        System.out.println(haoshi);
                        Thread.sleep(100);

                        if (haoshi > 2000) {
                            trycount++;
                            if (trycount <= 2) {
                                start = System.currentTimeMillis();
                                if (NetWorkUtils.isNetworkConnected(getApplicationContext())) {
                                    isConnected = false;
                                    connnect(true);
                                    Thread.sleep(200);
                                    connnect(false);
                                    Thread.sleep(200);
                                    msg = null;
                                    if (isConnected) {
                                        EasySocket.getInstance().upString(lastQuery);
                                    }
                                    System.out.println("请求超时 正在尝试第：" + String.valueOf(trycount) + "重连！");
                                } else {
                                    System.out.println("网络未开启！");
                                    show = false;
                                    sendmsg(false);
                                }
                            } else {
                                System.out.println("请检查盒剂主程序是否开启！");
                                show = false;
                                sendmsg(false);
                            }
                        }
                    }
                    Thread.sleep(200);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 消息分发
     *
     * @param isSuccess
     */
    private void sendmsg(boolean isSuccess) {
        if (isSuccess) {

            switch (op) {
                case OPINFO:
                    Utils.sendBroadcast(mContext, "RSSSOCKETMSG", "data", msg, "op", OPDEFAULT);
                    break;
                case OPDETAIL:
                    Utils.sendBroadcast(mContext, "RSSSOCKETDETAILSMSG", "data", msg, "op", OPDEFAULT);
                    break;
                case OP_LOGIN:
                    Utils.sendBroadcast(mContext, "RSSSOCKETLOGINMSG", "data", msg, "op", OPDEFAULT);
                    break;
            }
        } else {
            switch (op) {
                case OPINFO:
                    Utils.sendBroadcast(mContext, "RSSSOCKETMSG", "data", "FAILED", "op", OPDEFAULT);
                    break;
                case OPDETAIL:
                    Utils.sendBroadcast(mContext, "RSSSOCKETDETAILSMSG", "data", "FAILED", "op", OPDEFAULT);
                    break;
                case OP_LOGIN:
                    Utils.sendBroadcast(mContext, "RSSSOCKETLOGINMSG", "data", "FAILED", "op", OPDEFAULT);
                    break;
            }
        }
    }

    private void connnect(Boolean isConnected) {
        try {

            if (isConnected) {
                EasySocket.getInstance().disconnect(false);
            } else {
                EasySocket.getInstance().connect();
            }
            msg = "";
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public void StartRequest(String msg, int op) {
//        if(!isConnected) {
//            connnect(false);
//        }
        this.op = op;
        start = System.currentTimeMillis();
        lastQuery = msg;
        show = true;
        newmsg = false;
        this.msg = null;
        trycount = 0;
        if (NetWorkUtils.isNetworkConnected(getApplicationContext()) && isConnected) {
            EasySocket.getInstance().upString(lastQuery);
        } else {
            isConnected = false;
        }
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
    public void GetSocketDetailsData(String msg) {

    }

    @Override
    public void GetSocketLoginData(String msg) {

    }

    private void initSocketReceiver() {

        socketReceiver = new SocketReceiver();
        socketReceiver.setListener(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction("RSSSOCKETMSG");
        filter.addAction("RSSSOCKETCONNECTED");
        filter.addAction("RSSSOCKETMSGBYCODE");
        filter.addAction("RSSSOCKETSTARTREQUEST");
        registerReceiver(socketReceiver, filter);
    }

    @Override
    public void getDrugDetailQR(DrugInfo data) {

    }

    @Override
    public void getQRData(String data) {

    }
}
