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

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import android.accounts.Account;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentFilter;
import android.opengl.Visibility;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.alibaba.android.vlayout.DelegateAdapter;
import com.alibaba.android.vlayout.VirtualLayoutManager;
import com.alibaba.android.vlayout.layout.LinearLayoutHelper;
import com.rss.pad.R;
import com.rss.pad.adapter.base.delegate.SimpleDelegateAdapter;
import com.rss.pad.adapter.entity.DrugDetail;
import com.rss.pad.adapter.entity.DrugInfo;
import com.rss.pad.adapter.entity.LongClickBean;
import com.rss.pad.adapter.entity.ResultBean;
import com.rss.pad.core.BaseActivity;
import com.rss.pad.utils.TokenUtils;
import com.rss.pad.utils.Utils;
import com.rss.pad.utils.XToastUtils;
import com.rss.pad.utils.rss.SocketListener;
import com.rss.pad.utils.rss.SocketReceiver;
import com.rss.pad.utils.rss.XMLHelper;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.xuexiang.xui.adapter.recyclerview.RecyclerViewHolder;
import com.xuexiang.xui.utils.WidgetUtils;
import com.xuexiang.xui.widget.dialog.LoadingDialog;
import com.xuexiang.xui.widget.progress.HorizontalProgressView;
import com.xuexiang.xui.widget.textview.supertextview.SuperButton;
import com.xuexiang.xutil.tip.ToastUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.rss.pad.utils.rss.SocketQueryStr.DRUGDETAILS_1;
import static com.rss.pad.utils.rss.SocketQueryStr.DRUGDETAILS_2;
import static com.rss.pad.utils.rss.SocketQueryStr.DRUGDETAILS_END_1;
import static com.rss.pad.utils.rss.SocketQueryStr.DRUGDETAILS_END_2;
import static com.rss.pad.utils.rss.SocketQueryStr.DRUGDETAILS_END_3;
import static com.rss.pad.utils.rss.SocketQueryStr.DRUGDETAILS_END_4;
import static com.rss.pad.utils.rss.SocketQueryStr.DRUGDETAILS_START_1;
import static com.rss.pad.utils.rss.SocketQueryStr.DRUGDETAILS_START_2;
import static com.rss.pad.utils.rss.SocketQueryStr.OPDETAIL;
import static com.rss.pad.utils.rss.SocketQueryStr.OPDETAIL_END;
import static com.rss.pad.utils.rss.SocketQueryStr.OPDETAIL_START;

public class DrugDetailsActivity extends AppCompatActivity implements SocketListener {

    TextView txt_Drug_Name;
    TextView txt_Drug_Unit;
    TextView txt_Drug_Factory;
    TextView txt_Lack_Num;
    TextView txt_pre;
    HorizontalProgressView mian_progressBar;
    RecyclerView recyclerView;
    SmartRefreshLayout refreshLayout;
    private boolean press;
    private boolean shutdownNow=true;
    private  int temppos;
    private  int tempcur;
    private SimpleDelegateAdapter<DrugDetail> mDrugDetailsAdapter;
    private ScheduledExecutorService scheduledExecutor;
    private String addid="";

    //Sokcet 相关
    LoadingDialog mLoadingDialog;
    boolean show=false;
    private String msg;
    private Handler handler ;
    String lastQuery;
    private SocketReceiver socketReceiver;
    boolean isConnected;

    List<DrugDetail> detail_list=new ArrayList<>();
    List<LongClickBean> longClick_list=new ArrayList<>();
    LongClickBean longClickBean=new LongClickBean();
    ResultBean resultBean=new ResultBean();
    DrugInfo data;
    boolean runflag=true;
    /**
     * 操作码
     */
    int op;
    /**
     * 开始加药 结束加药的错误信息
     */
    String errorinfo;

    /**
     * 第一次启动显示动画
     */
    boolean firststart=false;

    /**
     * 缓存当前量的最新值
     */
    int NewcurNum;
    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drug_details);
        // 首先获取到意图对象
        Intent intent = getIntent();

        // 获取到传递过来的姓名
        data=(DrugInfo)getIntent().getSerializableExtra("data");

        txt_Drug_Name=(TextView)findViewById(R.id.Drug_Name);
        txt_Drug_Unit=(TextView)findViewById(R.id.Drug_Unit);
        txt_Drug_Factory=(TextView)findViewById(R.id.Drug_Factory);
        txt_Lack_Num=(TextView)findViewById(R.id.Lack_Num);
        txt_pre=(TextView)findViewById(R.id.Main_PER_Num);
        mian_progressBar=(HorizontalProgressView)findViewById(R.id.main_progressBar);
        recyclerView=(RecyclerView)findViewById(R.id.recyclerView);
        refreshLayout=(SmartRefreshLayout)findViewById(R.id.refreshLayout);
        refreshLayout.setEnableRefresh(false);//是否启用下拉刷新功能
        refreshLayout.setEnableLoadMore(false);//是否启用上拉加载功能
        System.out.println(data.toString());
        mLoadingDialog = WidgetUtils.getLoadingDialog(this)
                .setLoadingIcon(R.drawable.ic_appicon)
                .setIconScale(0.4F)
                .setLoadingSpeed(8);
        iniData();
        initViews();
        initSocketReceiver();
        sendMsgIndex(String.valueOf(data.getMedId()));
        new Thread(new showLoading()).start();
        new Thread(new plusOrSubQueue()).start();
        new Thread(new insertPlusOrSubQueue()).start();
        handler =new Handler(){
        @Override
        public void handleMessage(Message msg) {
            int viewId = msg.what;
            switch (viewId){
                case R.id.subtract:
                   System.out.print("-");    //减小操作
                    Log.d("test", "减小操作");
                    btn_refresh(msg.arg1, msg.arg2,0,(int)msg.obj);
                    break;
                case R.id.plus:
                    Log.d("test", "增大操作");
                    btn_refresh(msg.arg1, msg.arg2,1,(int)msg.obj);
                    break;
                case 1:
                    mDrugDetailsAdapter.refresh(detail_list);
                    refreshHead();
                    mLoadingDialog.dismiss();
                    break;
                case 2:
                    XToastUtils.toast("请求失败！请检查盒剂主程序是否开启！");
                    mLoadingDialog.dismiss();
                    break;
                case 3:
                    XToastUtils.toast("开启加药失败："+errorinfo);
                    mLoadingDialog.dismiss();
                    break;
                case 4:
                    XToastUtils.toast("结束加药失败："+errorinfo);
                    mLoadingDialog.dismiss();
                    break;
                case 5:
                   // XToastUtils.toast("结束加药失败："+errorinfo);
                    btn_refresh(msg.arg1, msg.arg2);
                    break;
            }
        }
    };
        //initListeners();
        //mDrugDetailsAdapter.refresh(detail_list);
    }
//    private void makedata(){
//        int i=10;
//        while (i>0) {
//            detail_list.add(new DrugDetail(String.valueOf(i), "1-2-3", i*10, 100,1,false ));
//            i--;
//        }
//    }
    private void updateAddOrSubtract(int viewId,int cur,int pos,int max) {
        final int vid = viewId;
        scheduledExecutor = Executors.newSingleThreadScheduledExecutor();
        scheduledExecutor.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
                    Message msg = new Message();
                    msg.what = vid;
                    msg.arg1 = pos;
                    msg.arg2 = cur;
                    msg.obj = max;
                    handler.sendMessage(msg);
            }
        }, 0, 100, TimeUnit.MILLISECONDS);    //每间隔100ms发送Message
        shutdownNow=false;
    }
    private void stopAddOrSubtract() {
        if (scheduledExecutor != null) {
            Log.d("test", "shutdownNow");
            scheduledExecutor.shutdownNow();
            scheduledExecutor = null;
            shutdownNow=true;
        }
    }
    private void  iniData(){
        txt_Drug_Name.setText(data.getDrugName());
        txt_Drug_Factory.setText(data.getFactory());
        txt_Drug_Unit.setText(data.getUnit());
        txt_Lack_Num.setText("缺"+String.valueOf(data.getLackNum()));
        txt_pre.setText(String.valueOf(data.getPercent())+"%");
        if(data.getPercent()<31){
            mian_progressBar.setStartColor(getResources().getColor(R.color.red_start));
            mian_progressBar.setEndColor(getResources().getColor(R.color.red_end));
        }else {
            mian_progressBar.setStartColor(getResources().getColor(R.color.green_start));
            mian_progressBar.setEndColor(getResources().getColor(R.color.green_end));
        }
        if(!firststart) {
            mian_progressBar.setProgress(data.getPercent());
        }else {
            mian_progressBar.setStartProgress(0);
            mian_progressBar.setEndProgress(data.getPercent());
            mian_progressBar.startProgressAnimation();
        }

        if(data.getPercent()<31){
            txt_pre.setTextColor(getResources().getColor(R.color.red_start));
        }else if (data.getPercent()<81){
            txt_pre.setTextColor(getResources().getColor(R.color.green_start));
        }else {
            txt_pre.setTextColor(getResources().getColor(R.color.light_orange));
        }
        //mian_progressBar.setProgress(data.getPercent());
    }
    private void initViews(){
        //makedata();
        VirtualLayoutManager virtualLayoutManager = new VirtualLayoutManager(this);
        RecyclerView.ItemAnimator animator = recyclerView.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }
        recyclerView.setFocusableInTouchMode(false);
        recyclerView.setFocusable(false);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setLayoutManager(virtualLayoutManager);
        RecyclerView.RecycledViewPool viewPool = new RecyclerView.RecycledViewPool();
        recyclerView.setRecycledViewPool(viewPool);
        viewPool.setMaxRecycledViews(0, 10);
        mDrugDetailsAdapter=new SimpleDelegateAdapter<DrugDetail>(R.layout.adapter_drug_details_card_view_list_item,new LinearLayoutHelper()) {
            @SuppressLint({"ClickableViewAccessibility"})
            @Override
            protected void bindData(@NonNull RecyclerViewHolder holder, int position, DrugDetail item) {
                if(item!=null){
                    holder.text(R.id.locName,item.getLocName());
                    holder.text(R.id.Cur_Max_Num,String.valueOf(item.getCurNum())+"/"+String.valueOf(item.getMaxNum()));
                    int percent= (int)Math.floor((float)item.getCurNum()/(float) item.getMaxNum()*100);
                    HorizontalProgressView progressBar=(HorizontalProgressView) holder.findViewById(R.id.item_progressBar);
                    if(percent<31){
                        progressBar.setStartColor(getResources().getColor(R.color.red_start));
                        progressBar.setEndColor(getResources().getColor(R.color.red_end));
                    }else if (percent<81){
                        progressBar.setStartColor(getResources().getColor(R.color.green_start));
                        progressBar.setEndColor(getResources().getColor(R.color.green_end));
                    }else {
                        progressBar.setStartColor(getResources().getColor(R.color.light_orange));
                        progressBar.setEndColor(getResources().getColor(R.color.dark_orange));
                    }
                    if(!firststart) {
                        progressBar.setProgress(percent);
                    }else {
                        progressBar.setStartProgress(0);
                        progressBar.setEndProgress(percent);
                        progressBar.startProgressAnimation();
                    }
                    ImageButton imageButton = (ImageButton) holder.findViewById(R.id.iv_image);
                    if(item.isIsadd()){
                        imageButton.setImageResource(R.drawable.ic_end_add);
                    }else {
                        imageButton.setImageResource(R.drawable.ic_start_add);
                    }
                    FrameLayout frameLayout = (FrameLayout)holder.findViewById(R.id.op_area);
                    if(item.isIsadd()){
                        frameLayout.setVisibility(View.VISIBLE);
                    }else {
                        frameLayout.setVisibility(View.GONE);
                    }
                    int lackNum=item.getMaxNum()-item.getCurNum();
                    holder.text(R.id.item_LackNum,String.valueOf(lackNum));
                    //region 加药按钮
                    SuperButton btn_plus= (SuperButton)holder.findViewById(R.id.plus);
                    btn_plus.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            Log.d("test", "onLongClick"+"press"+String.valueOf(press));
                            if (item.getCurNum() + 1 <= item.getMaxNum() && !press) {

                                // btn_refresh(position, item.getCurNum() + 1);
                                Log.d("test", "onLongClick"+String.valueOf(position)+"cur"+String.valueOf(item.getCurNum() + 1));
                               // updateAddOrSubtract(v.getId(),item.getCurNum() + 1,position,item.getMaxNum());    //手指按下时触发不停的发送消息
                                startInsertPlusOrSubQueue(v.getId(),item.getCurNum() + 1,position,item.getMaxNum());
                            }
                            return false;
                        }
                    });
                    btn_plus.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            if(v.getId() == R.id.plus){
                                if(event.getAction() == MotionEvent.ACTION_UP){
                                    Log.d("test", "plus button ---> cancel");
                                    System.out.print("111111");
                                    //stopAddOrSubtract();
                                    stop_longClick();
                                    press=false;
                                    //   mButton.setBackgroundResource(R.drawable.green);
                                }
                                if(event.getAction() == MotionEvent.ACTION_DOWN){
                                }
                            }
                            return false;
                        }
                    });

                    btn_plus.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(!press) {
                                System.out.println("click");
                                if (item.getCurNum() + 1 <= item.getMaxNum()) {
                                    btn_refresh(position, item.getCurNum() + 1);
                                }
                            }
                        }
                    });
                    SuperButton btn_subtract= (SuperButton)holder.findViewById(R.id.subtract);
                    btn_subtract.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {
                            if (item.getCurNum()-1>=0 && !press) {
                                //stopAddOrSubtract();
                                stop_longClick();
                                //updateAddOrSubtract(v.getId(),item.getCurNum() - 1,position,item.getMaxNum());    //手指按下时触发不停的发送消息
                                startInsertPlusOrSubQueue(v.getId(),item.getCurNum() - 1,position,item.getMaxNum());
                            }
                            return false;
                        }
                    });
                    btn_subtract.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            if(v.getId() == R.id.subtract){
                                if(event.getAction() == MotionEvent.ACTION_UP){
                                    Log.d("test", "subtract button ---> cancel");
                                    //stopAddOrSubtract();
                                    stop_longClick();
                                    press=false;
                                }
                                if(event.getAction() == MotionEvent.ACTION_DOWN){
                                }
                            }
                            return false;
                        }
                    });
                    btn_subtract.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (item.getCurNum()-1>=0) {
                                btn_refresh(position, item.getCurNum() - 1);
                            }
                        }
                    });
                    //endregion

                    imageButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            if(addid!="" && addid.compareTo(item.getLocName())==0){
                                stop_longClick();
                                imageButton.setImageResource(R.drawable.ic_start_add);
                                frameLayout.setVisibility(View.GONE);
                                item.setIsadd(false);
                                addid="";
                                sendMsg_End(TokenUtils.getsUserId(),item.getLocId(),String.valueOf(item.getCurNum()-item.getOriginalcurNum()));
                            }else if (!item.isIsadd() && addid==""){
                                firststart=false;
                                imageButton.setImageResource(R.drawable.ic_end_add);
                                frameLayout.setVisibility(View.VISIBLE);
                                item.setIsadd(true);
                                addid=item.getLocName();
                                sendMsg_Start(item.getLocId());
                                if(position==detail_list.size()-1) {
                                    ((LinearLayoutManager) recyclerView.getLayoutManager()).scrollToPositionWithOffset(position, 0);
                                }
                            }else {
                                XToastUtils.toast("请先结束locid:"+addid);
                            }
                        }
                    });


                }
            }
        };
        DelegateAdapter delegateAdapter = new DelegateAdapter(virtualLayoutManager);
        delegateAdapter.addAdapter(mDrugDetailsAdapter);

        recyclerView.setAdapter(delegateAdapter);
    }
    private void btn_refresh(int position,int cur,int op,int max){
        Log.d("btn_refresh", "cur"+String.valueOf(cur)+"position"+String.valueOf(position));
        if(!shutdownNow) {
            if (!press) {
                detail_list.get(position).setCurNum(cur);
                temppos = position;
                tempcur = cur;
                press = true;
            } else {
                if (op == 1) {
                    if (tempcur + 1 <= max) {
                        detail_list.get(temppos).setCurNum(++tempcur);
                    }
                } else {
                    if (tempcur - 1 >= 0) {
                        detail_list.get(temppos).setCurNum(--tempcur);
                    }
                }
            }
            Log.d("test", "position" + String.valueOf(temppos) + "cur" + String.valueOf(tempcur));
            // mDrugDetailsAdapter.refresh(detail_list);
            int percent= (int)Math.floor((float)tempcur/(float) max*100);
            mDrugDetailsAdapter.refresh(temppos, detail_list.get(position));
            NewcurNum=temppos;
            refreshHead();
        }else {
            press=false;
        }

    }
    private void btn_refresh(int position,int cur){
        detail_list.get(position).setCurNum(cur);
        mDrugDetailsAdapter.refresh(position, detail_list.get(position));
        NewcurNum=cur;
        refreshHead();

    }

    @Override
    public void onBackPressed() {
        if(addid=="") {
            Intent intent = new Intent(this,DrugDetailsActivity.class);
            intent.putExtra("data", data);
            setResult(RESULT_OK,intent);
            this.finish();
            super.onBackPressed();
        }else {
            XToastUtils.toast("请先结束locid:"+addid);
        }
    }

    /**
     * 首页数据请求
     * @param code
     */
    private void sendMsgIndex(String code){
        firststart=true;
        mLoadingDialog.show();
        op=OPDETAIL;
        msg=null;
        show=true;
        lastQuery= DRUGDETAILS_1+code+DRUGDETAILS_2;
        Utils.sendBroadcast(this,"RSSSOCKETSTARTREQUEST","data",lastQuery,"op",OPDETAIL);
    }

    /**
     * 开始加药
     * @param code
     */
    private void sendMsg_Start(String code){
        mLoadingDialog.show();
        op=OPDETAIL_START;
        msg=null;
        show=true;
        lastQuery= DRUGDETAILS_START_1+code+DRUGDETAILS_START_2;
        Utils.sendBroadcast(this,"RSSSOCKETSTARTREQUEST","data",lastQuery,"op",OPDETAIL);
    }

    /**
     * 结束加药
     * @param uid
     * @param locid
     * @param curnum
     */
    private void sendMsg_End(String uid,String locid,String curnum){
        mLoadingDialog.show();
        op=OPDETAIL_END;
        msg=null;
        show=true;
        lastQuery= DRUGDETAILS_END_1+uid+DRUGDETAILS_END_2+locid+DRUGDETAILS_END_3+curnum+DRUGDETAILS_END_4;
        Utils.sendBroadcast(this,"RSSSOCKETSTARTREQUEST","data",lastQuery,"op",OPDETAIL);
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
    public void StartRequest(String msg,int op) {

    }

    @Override
    public void GetSocketLoginData(String msg) {

    }

    @Override
    public void GetSocketDetailsData(String msg) {
        this.msg=msg;
    }

    private void initSocketReceiver() {

        socketReceiver=new SocketReceiver();
        socketReceiver.setListener(this);
        IntentFilter filter = new IntentFilter();
        filter.addAction("RSSSOCKETDETAILSMSG");
        filter.addAction("RSSSOCKETCONNECTED");
        filter.addAction("RSSDRUGDETAILQR");
        registerReceiver(socketReceiver,filter);
    }
    private  class showLoading implements Runnable{
        @Override
        public void run() {
            try{
                while (runflag) {
                    if (msg!=null && msg.compareTo("FAILED")!=0 && show) {
                        show = false;
                        Message mg =new Message();
                        switch (op){
                            case OPDETAIL:
                                detail_list.clear();
                                detail_list= XMLHelper.GetDrugDetail(msg);
                                mg.what=1;
                                handler.sendMessage(mg);
                                break;
                            case OPDETAIL_START:
                                resultBean=XMLHelper.GetResult(msg);
                                if(resultBean.getErrorCode().compareTo("0")==0){
                                    mg.what=1;
                                    handler.sendMessage(mg);
                                }else {
                                    mg.what=3;
                                    errorinfo=resultBean.getErrorMessage();
                                    handler.sendMessage(mg);
                                }
                                break;
                            case OPDETAIL_END:
                                resultBean=XMLHelper.GetResult(msg);
                                if(resultBean.getErrorCode().compareTo("0")==0){
                                    mg.what=1;
                                    handler.sendMessage(mg);
                                }else {
                                    mg.what=4;
                                    errorinfo=resultBean.getErrorMessage();
                                    handler.sendMessage(mg);
                                }
                                break;
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

    @Override
    protected void onDestroy() {
        unregisterReceiver(socketReceiver);
        runflag=false;
        super.onDestroy();
    }


    /**
     * 刷新头部数据
     */
    private void refreshHead(){
        int cur=0;
        int max=0;
        int percent=0;
        int lacknum=0;
        for (int i=0;i<detail_list.size();i++){
            cur+=detail_list.get(i).getCurNum();
            max+=detail_list.get(i).getMaxNum();
            lacknum=max-cur;
            if(max!=0) {
                percent = (int) Math.floor((float) cur / (float) max * 100);
            }else {
                percent=0;
            }
        }
        data.setPercent(percent);
        data.setCurNum(cur);
        data.setLackNum(lacknum);
        iniData();
    }

    @Override
    public void getDrugDetailQR(DrugInfo data) {
        if(addid=="") {
            this.data=data;
            iniData();
            sendMsgIndex(String.valueOf(data.getMedId()));
        }else {
            XToastUtils.toast("请先结束locid:"+addid);
        }

    }
    private void stop_longClick(){
        shutdownNow=true;
    }
    private void startInsertPlusOrSubQueue(int viewId, int cur, int pos, int max){
        longClickBean.pos=pos;
        longClickBean.cur=cur;
        longClickBean.max=max;
        longClickBean.viewId=viewId;
        longClick_list.add(longClickBean);
        shutdownNow=false;
        Log.d("TAG", "启动长按线程: cur"+longClickBean.toString());
    }
    private  class insertPlusOrSubQueue implements Runnable{
        @Override
        public void run() {
            try{
                LongClickBean temp = new LongClickBean();
                while (runflag) {
                    if(!shutdownNow) {
                        LongClickBean temp2 = new LongClickBean();
                        temp2.max=longClickBean.max;
                        temp2.viewId=longClickBean.viewId;
                        temp2.pos=longClickBean.pos;
                        if(R.id.plus==longClickBean.viewId){
                            if(longClickBean.cur+1>longClickBean.max){
                                temp2.cur=longClickBean.max;
                            }else {
                                temp2.cur = longClickBean.cur+1;
                            }
                        }else if(R.id.subtract==longClickBean.viewId){
                            if(longClickBean.cur-1<0){
                                longClickBean.cur=0;
                            }else {
                                temp2.cur = longClickBean.cur-1;
                            }
                        }
                        longClickBean=temp2;
                        longClick_list.add(temp2);
                        Log.d("TAG", "新增一条数据: temp2:"+temp2.toString());
                    }
                    Thread.sleep(100);
                }
            }catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


    }
    private  class plusOrSubQueue implements Runnable{
        @Override
        public void run() {
            try{
                while (runflag) {
                    if(longClick_list.size()>0 && !shutdownNow && addid!=""){
                        Message msg = new Message();
                        msg.what = 5;
                        msg.arg1 = longClick_list.get(0).pos;
                        msg.arg2 = longClick_list.get(0).cur;
                        msg.obj = longClick_list.get(0).max;
                        handler.sendMessage(msg);
                        Thread.sleep(100);
                        Log.d("TAG", "移除一条数据 run: "+longClick_list.get(0).toString());
                        longClick_list.remove(0);
                    }else {
                        longClick_list.clear();
                        Thread.sleep(10);
                    }
                }
            }catch (Exception  e) {
                e.printStackTrace();
            }
        }


    }
}