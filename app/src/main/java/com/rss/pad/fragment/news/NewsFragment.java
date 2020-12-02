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

package com.rss.pad.fragment.news;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.alibaba.android.vlayout.DelegateAdapter;
import com.alibaba.android.vlayout.VirtualLayoutManager;
import com.alibaba.android.vlayout.layout.GridLayoutHelper;
import com.alibaba.android.vlayout.layout.LinearLayoutHelper;
import com.easysocket.EasySocket;
import com.easysocket.config.EasySocketOptions;
import com.easysocket.entity.OriginReadData;
import com.easysocket.entity.SocketAddress;
import com.easysocket.interfaces.conn.ISocketActionListener;
import com.easysocket.interfaces.conn.SocketActionListener;
import com.easysocket.utils.LogUtil;
import com.rss.pad.activity.DrugDetailsActivity;
import com.rss.pad.activity.MainActivity;
import com.rss.pad.adapter.base.delegate.SimpleDelegateAdapter;
import com.rss.pad.adapter.base.delegate.SingleDelegateAdapter;
import com.rss.pad.adapter.entity.DrugInfo;
import com.rss.pad.core.BaseFragment;
import com.rss.pad.utils.DemoDataProvider;
import com.rss.pad.utils.MMKVUtils;
import com.rss.pad.utils.TokenUtils;
import com.rss.pad.utils.Utils;
import com.rss.pad.utils.XToastUtils;
import com.rss.pad.utils.rss.NetWorkUtils;
import com.rss.pad.utils.rss.QRListener;
import com.rss.pad.utils.rss.QRReceiver;
import com.rss.pad.utils.rss.QueryResult;
import com.rss.pad.utils.rss.SocketListener;
import com.rss.pad.utils.rss.SocketQueryStr;
import com.rss.pad.utils.rss.SocketReceiver;
import com.rss.pad.utils.rss.XMLHelper;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.rss.pad.R;
import com.rss.pad.adapter.entity.NewInfo;
import com.xuexiang.xpage.annotation.Page;
import com.xuexiang.xpage.enums.CoreAnim;
import com.xuexiang.xrouter.annotation.AutoWired;
import com.xuexiang.xui.adapter.recyclerview.RecyclerViewHolder;
import com.xuexiang.xui.adapter.simple.AdapterItem;
import com.xuexiang.xui.utils.WidgetUtils;
import com.xuexiang.xui.widget.actionbar.TitleBar;
import com.xuexiang.xui.widget.banner.widget.banner.SimpleImageBanner;
import com.xuexiang.xui.widget.dialog.LoadingDialog;
import com.xuexiang.xui.widget.imageview.ImageLoader;
import com.xuexiang.xui.widget.imageview.RadiusImageView;
import com.xuexiang.xui.widget.progress.HorizontalProgressView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;

import static com.rss.pad.utils.rss.SocketQueryStr.DRUGINFO_1;
import static com.rss.pad.utils.rss.SocketQueryStr.DRUGINFO_2;
import static com.rss.pad.utils.rss.SocketQueryStr.DRUGINFO_3;
import static com.rss.pad.utils.rss.SocketQueryStr.OPDETAIL;
import static com.rss.pad.utils.rss.SocketQueryStr.OPINFO;
import static com.rss.pad.utils.rss.SocketQueryStr.OP_DETAIL_QR;

/**
 * 首页动态
 *
 * @author xuexiang
 * @since 2019-10-30 00:15
 */
@Page(anim = CoreAnim.none)
public class NewsFragment extends BaseFragment implements SocketListener, QRListener {

    @BindView(R.id.recyclerView)
    RecyclerView recyclerView;
    @BindView(R.id.refreshLayout)
    SmartRefreshLayout refreshLayout;

    private SimpleDelegateAdapter<DrugInfo> mNewsAdapter;
    List<DrugInfo> drug_list = new ArrayList<>();

    //Sokcet 相关
    LoadingDialog mLoadingDialog;
    boolean show = false;
    private String msg;
    private Handler handler;
    String lastQuery;
    private SocketReceiver socketReceiver;
    boolean isConnected;
    private QRReceiver receiver;
    boolean runflag = true;
    /**
     * 条码
     */
    String QDCODE = "";
    /**
     * 是否需要查询明细
     */
    boolean isNeedDetails = false;

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
        return R.layout.fragment_news;
    }

//    private void makedata(){
//        int i=10;
//        while (i>0) {
//            drug_list.add(new DrugInfo("阿司匹林", 10, i*10, 50,"深圳康泰制药", "0.25g*24粒/盒"));
//            i--;
//        }
//    }

    /**
     * 初始化控件
     */
    @Override
    protected void initViews() {
        initSocketReceiver();
        initQRReceiver();
        new Thread(new showLoading()).start();
        handler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 1:
                        mNewsAdapter.refresh(drug_list);
                        break;
                    case 2:
                        XToastUtils.toast("请求失败！请检查盒剂主程序是否开启！");
                        break;
                    case 3:
                        XToastUtils.toast("条码：" + QDCODE + "不存在！");
                        break;
                }
                refreshLayout.finishRefresh();
                mLoadingDialog.dismiss();
            }
        };
        refreshLayout.setEnableLoadMore(false);
        VirtualLayoutManager virtualLayoutManager = new VirtualLayoutManager(getContext());
        recyclerView.setLayoutManager(virtualLayoutManager);
        RecyclerView.RecycledViewPool viewPool = new RecyclerView.RecycledViewPool();
        recyclerView.setRecycledViewPool(viewPool);
        viewPool.setMaxRecycledViews(0, 10);
        mLoadingDialog = WidgetUtils.getLoadingDialog(getContext())
                .setLoadingIcon(R.drawable.ic_appicon)
                .setIconScale(0.4F)
                .setLoadingSpeed(8);
        // mLoadingDialog.show();
        //makedata();
//        //轮播条
//        SingleDelegateAdapter bannerAdapter = new SingleDelegateAdapter(R.layout.include_head_view_banner) {
//            @Override
//            public void onBindViewHolder(@NonNull RecyclerViewHolder holder, int position) {
//                SimpleImageBanner banner = holder.findViewById(R.id.sib_simple_usage);
//                banner.setSource(DemoDataProvider.getBannerList())
//                        .setOnItemClickListener((view, item, position1) -> XToastUtils.toast("headBanner position--->" + position1)).startScroll();
//            }
//        };
//
//        //九宫格菜单
//        GridLayoutHelper gridLayoutHelper = new GridLayoutHelper(4);
//        gridLayoutHelper.setPadding(0, 16, 0, 0);
//        gridLayoutHelper.setVGap(10);
//        gridLayoutHelper.setHGap(0);
//        SimpleDelegateAdapter<AdapterItem> commonAdapter = new SimpleDelegateAdapter<AdapterItem>(R.layout.adapter_common_grid_item, gridLayoutHelper, DemoDataProvider.getGridItems(getContext())) {
//            @Override
//            protected void bindData(@NonNull RecyclerViewHolder holder, int position, AdapterItem item) {
//                if (item != null) {
//                    RadiusImageView imageView = holder.findViewById(R.id.riv_item);
//                    imageView.setCircle(true);
//                    ImageLoader.get().loadImage(imageView, item.getIcon());
//                    holder.text(R.id.tv_title, item.getTitle().toString().substring(0, 1));
//                    holder.text(R.id.tv_sub_title, item.getTitle());
//
//                    holder.click(R.id.ll_container, v -> XToastUtils.toast("点击了：" + item.getTitle()));
//                }
//            }
//        };
//
//        //资讯的标题
//        SingleDelegateAdapter titleAdapter = new SingleDelegateAdapter(R.layout.adapter_title_item) {
//            @Override
//            public void onBindViewHolder(@NonNull RecyclerViewHolder holder, int position) {
//                holder.text(R.id.tv_title, "资讯");
//                holder.text(R.id.tv_action, "更多");
//                holder.click(R.id.tv_action, v -> XToastUtils.toast("更多"));
//            }
//        };

        //资讯
        mNewsAdapter = new SimpleDelegateAdapter<DrugInfo>(R.layout.adapter_drug_card_view_list_item, new LinearLayoutHelper()) {
            @Override
            protected void bindData(@NonNull RecyclerViewHolder holder, int position, DrugInfo item) {
                if (item != null) {
                    holder.text(R.id.Drug_Name, item.getDrugName());
                    holder.text(R.id.Drug_Factory, item.getFactory());
                    holder.text(R.id.Drug_Unit, item.getUnit());
                    holder.text(R.id.percent, item.getPercent());
                    holder.text(R.id.Lack_Num, "缺" + String.valueOf(item.getLackNum()));
                    HorizontalProgressView progressBar = (HorizontalProgressView) holder.findViewById(R.id.progressBar);
                    if (item.getPercent() < 31) {
                        progressBar.setStartColor(getResources().getColor(R.color.red_start));
                        progressBar.setEndColor(getResources().getColor(R.color.red_end));
                    } else if (item.getPercent() < 81) {
                        progressBar.setStartColor(getResources().getColor(R.color.green_start));
                        progressBar.setEndColor(getResources().getColor(R.color.green_end));
                    } else {
                        progressBar.setStartColor(getResources().getColor(R.color.light_orange));
                        progressBar.setEndColor(getResources().getColor(R.color.dark_orange));
                    }
                    progressBar.setStartProgress(0);
                    progressBar.setEndProgress(item.getPercent());
                    progressBar.startProgressAnimation();
                    holder.text(R.id.PER_Num, String.valueOf(item.getPercent()) + "%");
                    TextView PERNum = (TextView) holder.findViewById(R.id.PER_Num);

                    if (item.getPercent() < 31) {
                        PERNum.setTextColor(getResources().getColor(R.color.red_start));
                    } else if (item.getPercent() < 81) {
                        PERNum.setTextColor(getResources().getColor(R.color.green_start));
                    } else {
                        PERNum.setTextColor(getResources().getColor(R.color.light_orange));
                    }
                    //progressBar.setProgress(item.getPercent());
                    holder.click(R.id.DrugInfo, new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //XToastUtils.toast("点击了：" + item.getDrugName());
                            Intent intent = new Intent(getContext(), DrugDetailsActivity.class);
                            intent.putExtra("data", item);
                            startActivityForResult(intent, OPDETAIL);
                        }
                    });
                }
            }
        };

        DelegateAdapter delegateAdapter = new DelegateAdapter(virtualLayoutManager);
//        delegateAdapter.addAdapter(bannerAdapter);
//        delegateAdapter.addAdapter(commonAdapter);
//        delegateAdapter.addAdapter(titleAdapter);
        delegateAdapter.addAdapter(mNewsAdapter);

        recyclerView.setAdapter(delegateAdapter);
    }

    @Override
    protected void initListeners() {
        //下拉刷新
        refreshLayout.setOnRefreshListener(refreshLayout -> {
            // TODO: 2020-02-25 这里只是模拟了网络请求
            refreshLayout.getLayout().postDelayed(() -> {
                isNeedDetails = false;
                msg = null;
                show = true;
                lastQuery = DRUGINFO_1 + "" + DRUGINFO_2 + TokenUtils.getsSort() + DRUGINFO_3;
                Log.d("TAG", "initListeners: 下拉刷新！");
                Utils.sendBroadcast(getContext(), "RSSSOCKETSTARTREQUEST", "data", lastQuery, "op", OPINFO);

            }, 200);
        });
        //上拉加载
//        refreshLayout.setOnLoadMoreListener(refreshLayout -> {
//            // TODO: 2020-02-25 这里只是模拟了网络请求
//            refreshLayout.getLayout().postDelayed(() -> {
//                mNewsAdapter.loadMore(DemoDataProvider.getDemoNewInfos());
//                refreshLayout.finishLoadMore();
//            }, 1000);
//        });
        refreshLayout.autoRefresh();//第一次进入触发自动刷新，演示效果
    }


    private class showLoading implements Runnable {
        @Override
        public void run() {
            try {
                while (runflag) {
                    if (msg != null && msg.compareTo("FAILED") != 0 && show) {
                        show = false;

                        drug_list.clear();
                        drug_list = XMLHelper.GetDrugInfo(msg);
                        if (drug_list.size() != 0) {
                            Message mg = new Message();
                            mg.what = 1;
                            handler.sendMessage(mg);
                        } else {
                            Message mg = new Message();
                            mg.what = 3;
                            handler.sendMessage(mg);
                        }
                        //查询明细
                        if (drug_list.size() == 1 && drug_list.get(0).getMedId() != 0 && isNeedDetails) {
                            // Utils.sendBroadcast(getContext(),"RSSDRUGDETAILQR","data",lastQuery,"op",OP_DETAIL_QR);
                            Intent intent = new Intent();
                            intent.setAction("RSSDRUGDETAILQR");
                            intent.putExtra("data", drug_list.get(0));
                            getContext().sendBroadcast(intent);
                        }
                    } else if (msg != null && msg.compareTo("FAILED") == 0 && show) {
                        show = false;
                        Message mg = new Message();
                        mg.what = 2;
                        handler.sendMessage(mg);
                    }
                    Thread.sleep(100);
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }


    private void initSocketReceiver() {

        socketReceiver = new SocketReceiver();
        socketReceiver.setListener(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction("RSSSOCKETMSG");
        filter.addAction("RSSSOCKETCONNECTED");
        filter.addAction("RSSSOCKETMSGBYCODE");
        getActivity().getApplicationContext().registerReceiver(socketReceiver, filter);
    }

    @Override
    public void GetSocketData(String msg) {
        System.out.println("监听器收到了 msg：" + msg);
        this.msg = msg;
    }

    @Override
    public void GetSocketConnected(boolean connected) {
        System.out.println("监听器收到了 connected：" + connected);
        isConnected = connected;
    }

    @Override
    public void GetSocketDataByCode(String msg) {
        System.out.println("监听器收到了 GetSocketDataByCode：" + msg);
        mLoadingDialog.show();
        // TODO: 2020-02-25 这里只是模拟了网络请求
        refreshLayout.getLayout().postDelayed(() -> {
            isNeedDetails = false;
            this.msg = null;
            show = true;
            lastQuery = DRUGINFO_1 + msg + DRUGINFO_2 + TokenUtils.getsSort() + DRUGINFO_3;
            Utils.sendBroadcast(getContext(), "RSSSOCKETSTARTREQUEST", "data", lastQuery, "op", OPINFO);
            // EasySocket.getInstance().upString(lastQuery);
        }, 200);
    }

    @Override
    public void StartRequest(String msg, int op) {

    }

    @Override
    public void GetSocketDetailsData(String msg) {

    }

    @Override
    public void GetSocketLoginData(String msg) {

    }

    private void initQRReceiver() {
        receiver = new QRReceiver();
        receiver.setListener(this);
        getActivity().getApplicationContext().registerReceiver(receiver, new IntentFilter("com.scanner.broadcast"));
    }

    @Override
    public void getQRData(String data) {
        if (!mLoadingDialog.isShowing()) {
            QDCODE = data;
            refreshLayout.getLayout().postDelayed(() -> {
                mLoadingDialog.show();
                isNeedDetails = true;
                msg = null;
                show = true;
                lastQuery = DRUGINFO_1 + data + DRUGINFO_2 + TokenUtils.getsSort() + DRUGINFO_3;
                Utils.sendBroadcast(getContext(), "RSSSOCKETSTARTREQUEST", "data", lastQuery, "op", OPINFO);
                // EasySocket.getInstance().upString(lastQuery);
            }, 200);
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        DrugInfo resultdata = new DrugInfo();
        resultdata = (DrugInfo) data.getSerializableExtra("data");
        Log.d("TAG", "onActivityResult: " + resultdata.toString());
        for (int i = 0; i < drug_list.size(); i++) {
            if (drug_list.get(i).getMedId() == resultdata.getMedId()) {
                drug_list.get(i).setCurNum(resultdata.getCurNum());
                drug_list.get(i).setLackNum(resultdata.getLackNum());
                drug_list.get(i).setPercent(resultdata.getPercent());
                mNewsAdapter.refresh(i, drug_list.get(i));
                break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onDestroy() {
        getActivity().getApplicationContext().unregisterReceiver(socketReceiver);
        getActivity().getApplicationContext().unregisterReceiver(receiver);
        runflag = false;
        super.onDestroy();
    }

    @Override
    public void getDrugDetailQR(DrugInfo data) {

    }
}
