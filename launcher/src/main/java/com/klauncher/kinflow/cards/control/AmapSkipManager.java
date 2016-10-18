package com.klauncher.kinflow.cards.control;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.klauncher.ext.KLauncherApplication;
import com.klauncher.kinflow.common.utils.CommonUtils;
import com.klauncher.launcher.BuildConfig;

/**
 * Created by xixionghui on 16/7/22.
 * 此类服务于附近电影院,和周末去哪儿.两个模块.
 * 当用户点击大图banner时,选择正确的uri跳转到高德地图.如果高德地图没有安装则跳转到蜜罐详情页.
 */
public class AmapSkipManager {

    private Context mContext;
    public AmapSkipManager (Context context) {
        setContext(context);
    }


    public void setContext(Context context) {
        if (null!=context) {
            mContext = context;
        } else {
            mContext = KLauncherApplication.mKLauncherApplication;
        }
    }

    public Context getContext() {
        if (null==mContext)  mContext = KLauncherApplication.mKLauncherApplication;
        return mContext;
    }

    //我的位置
    public Uri getMyLocation() {
        return Uri.parse("androidamap://myLocation?sourceApplication=" + BuildConfig.APPLICATION_ID);
    }

    /**
     * 周边分类:直接进行搜索.如果关键字缺失,则跳转到:周边搜(进入周边搜索界面)
     * 先定位,在搜索周边
     * @param points
     * @return
     */
    public Uri getArroundPoi(String... points) {
        //dat=androidamap://arroundpoi?sourceApplication=softname&keywords=银行|加油站|电影院&dev=0
        if (CommonUtils.stringArrayContainsNull(points)) {
            Toast.makeText(getContext(), "请输入兴趣点", Toast.LENGTH_SHORT).show();
            //androidamap://openFeature?featureName=AroundSearch&sourceApplication=softname
            return Uri.parse("androidamap://openFeature?featureName=AroundSearch&sourceApplication="+BuildConfig.APPLICATION_ID);
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (String point :
                points) {
            stringBuilder.append(point).append("|");
        }
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        return Uri.parse("androidamap://arroundpoi?sourceApplication=" + BuildConfig.APPLICATION_ID+"&keywords="+stringBuilder.toString()+"&dev=0");
    }

    /**
     * 一筐搜(不定位,直接进入搜索结果页面)
     * dat=androidamap://poi?sourceApplication=softname&keywords=银行|加油站|电影院&dev=0
     * @param
     * @return
     */
    public Uri getYiKuangSou (String ... points) {
        if (CommonUtils.stringArrayContainsNull(points)) {
            Toast.makeText(getContext(), "请输入兴趣点", Toast.LENGTH_SHORT).show();
            //androidamap://openFeature?featureName=AroundSearch&sourceApplication=softname
            return Uri.parse("androidamap://openFeature?featureName=AroundSearch&sourceApplication="+BuildConfig.APPLICATION_ID);
        }
        StringBuilder stringBuilder = new StringBuilder();
        for (String point :
                points) {
            stringBuilder.append(point).append("|");
        }
        stringBuilder.deleteCharAt(stringBuilder.length()-1);
        return Uri.parse("androidamap://poi?sourceApplication=" + BuildConfig.APPLICATION_ID+"&keywords="+stringBuilder.toString()+"&dev=0");
    }


   public void skip2Amap(Uri uri) {
        Log.e("Kinflow", ("要跳转高德的uri = " + uri.toString()));
        try {
            boolean isInstallAmap = CommonUtils.getInstance().isInstalledAPK(mContext, "com.autonavi.minimap/com.autonavi.map.activity.SplashActivity");
            if (!isInstallAmap) {
                launch(mContext,172556,"com.autonavi.minimap",6);
            } else {
                try {
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    getContext().startActivity(intent);
                }catch (ActivityNotFoundException exception) {
//            Toast.makeText(getContext(), "您尚未安装高德地图", Toast.LENGTH_SHORT).show();
                    launch(mContext,172556,"com.autonavi.minimap",6);
                }
            }
        }  catch (Exception e) {
            Log.d("Kinflow", ("跳转到高德地图app,出现未知异常" + e.getMessage()));
        }
    }

    //下载高德地图
    private static final String APP_ID = "appId";
    private static final String PKG_NAME = "pkg_name";
    private static final String APP_CATEGORY_ID = "category_id";
    private static final String PAGE_TITLE = "pageTitle";
    private static final String PAGE_CLASS_NAME = "pageClassName";
    private static final String PAGE_TAG = "pageTag";

    public void launch(Context from, long appId, String packageName, int app_categoryId) {
        try {
            Bundle bundle = new Bundle();
            bundle.putLong(APP_ID, appId);
            bundle.putString(PKG_NAME, packageName);
            bundle.putInt(APP_CATEGORY_ID, app_categoryId);
            String className = "com.miguan.market.app_business.app_detail.ui.AppDetailFragment";
            String title = "应用详情";

            //-------------
            Intent i = new Intent();
            ComponentName componentName = new ComponentName("com.miguan.market","com.miguan.market.component.SingleFragmentActivity");
            i.setComponent(componentName);
            i.putExtra(PAGE_TITLE, title);
            i.putExtra(PAGE_CLASS_NAME, className);
            i.putExtra(PAGE_TAG, className);
            if (bundle != null) i.putExtras(bundle);
            from.startActivity(i);
        } catch (ActivityNotFoundException activityNotFoundException) {
            Toast.makeText(getContext(), "请先下载高德地图", Toast.LENGTH_LONG).show();
        }catch (Exception e) {
            e.printStackTrace();
        }
    }
}
