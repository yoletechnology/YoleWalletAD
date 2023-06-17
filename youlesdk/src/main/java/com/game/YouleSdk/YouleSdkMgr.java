package com.game.YouleSdk;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import com.game.MobileAdsSDK.MobileAdsMgr;
import java.util.HashMap;

public class YouleSdkMgr {

    private String TAG = "YouleSdkMgr";
    private static  YouleSdkMgr _instance = null;
    private NetUtil request = null;
    private Context var =  null;
    private PhoneInfo info =  null;
    private String payOrderNum = "";//支付的订单号
    private HashMap<String,String> list;
    private boolean isPlayerIng = false;

    private String appkey = "";
    private String model = "";
    private String AP_ID = "";
    private String CP_ID = "";
    private String API_KEY = "";
    private String rewardedAdId = "";
    public static YouleSdkMgr getsInstance() {
        if(YouleSdkMgr._instance == null)
        {
            YouleSdkMgr._instance = new YouleSdkMgr();
        }
        return YouleSdkMgr._instance;
    }
    private YouleSdkMgr() {
        Log.e(TAG,"YouleSdkMgr");
    }
    public void initAd(Context var1, HashMap<String,String> var2,boolean isDebugger)
    {

        list = var2;
        appkey = list.get("appkey");
        model = list.get("model");
        AP_ID = list.get("AP_ID");
        CP_ID = list.get("CP_ID");
        API_KEY = list.get("API_KEY");
        rewardedAdId = list.get("RewardedAdId");

        request = new NetUtil(appkey,model);
        var = var1;
        MobileAdsMgr.getsInstance().initAd(var1);

        info = new PhoneInfo(var1);
    }
    public void preloadAd(Activity var1)
    {
        MobileAdsMgr.getsInstance().preloadAd(var1,rewardedAdId);
        MobileAdsMgr.getsInstance().preloadRewardedAd(false);
    }

    public void  startPay(Activity var1,CallBackFunction callBack) throws Exception {
        if(isPlayerIng == true)
        {
            Log.i(TAG,"YouleSdkMgr.startPay 正在支付中");
            callBack.onCallBack(false);
            return;
        }

        isPlayerIng = true;
        LoadingDialog.getInstance(var1).show();//显示


        boolean isAd = false;
        if(isAd == false && (request.userCode.length() <= 0 || request.choiceId.length() <= 0 || request.paymentType.length() <= 0))
        {
            Log.i(TAG,"YouleSdkMgr.startPay sdk初始化参数错误；userCode:"+request.userCode+";choiceId:"+request.choiceId+";paymentId"+request.paymentType);
            isAd = true;
        }
        if(isAd == false && (request.paymentType.indexOf("AD") != -1))
        {
            Log.i(TAG,"YouleSdkMgr.startPay 支付方式为广告");
            isAd = true;
        }

        if( isAd == true)
        {
            MobileAdsMgr.getsInstance().showRewardedAd(new CallBackFunction() {

                @Override
                public void onCallBack(boolean data) {
                    isPlayerIng = false;
                    callBack.onCallBack(data);
                    LoadingDialog.getInstance(var1).hide();//显示

                }
            });
            return;
        }
        else
        {
            Log.i(TAG,"短信支付 直接返回失败");
            isPlayerIng = false;
            callBack.onCallBack(false);
            LoadingDialog.getInstance(var1).hide();//显示
        }


    }

    public void smsPaymentNotify(boolean  paymentStatus)
    {
        new Thread(new Runnable(){
            @Override
            public void run() {
                request.smsPaymentNotify(
                        YouleSdkMgr.getsInstance().payOrderNum,
                        paymentStatus == true ? "SUCCESSFUL" : "FAILED");
            }
        }).start();
    }

}
