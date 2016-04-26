package com.klauncher.kinflow.common.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.klauncher.kinflow.navigation.model.Navigation;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by xixionghui on 2016/3/18.
 */
public class CacheNavigation {

    //xml--->SharedPreference--->Object
    //用一个对象来操作此文件
    //控制默认数据

    public static final int DEFAULT_ORDER_0 = 0;
    public static final int DEFAULT_ORDER_1 = 1;
    public static final int DEFAULT_ORDER_2 = 2;
    public static final int DEFAULT_ORDER_3 = 3;
    public static final int DEFAULT_ORDER_4 = 4;
    public static final int DEFAULT_ORDER_5 = 5;
    public static final int DEFAULT_ORDER_6 = 6;
    public static final int DEFAULT_ORDER_7 = 7;

    public static final String DEFAULT_NAVIGATION_ICON_0 = "nav0";
    public static final String DEFAULT_NAVIGATION_ICON_1 = "nav1";
    public static final String DEFAULT_NAVIGATION_ICON_2 = "nav2";
    public static final String DEFAULT_NAVIGATION_ICON_3 = "nav3";
    public static final String DEFAULT_NAVIGATION_ICON_4 = "nav4";
    public static final String DEFAULT_NAVIGATION_ICON_5 = "nav5";
    public static final String DEFAULT_NAVIGATION_ICON_6 = "nav6";
    public static final String DEFAULT_NAVIGATION_ICON_7 = "nav7";

    public static final int DEFAULT_NAVIGATION_COUNT = 8;

    String fileName = "cacheNavigation";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private Gson gson;

    //获取Navigation的顺序：getAll--->getDefaultList---->createDefaultList-----(将此三个封装成一个方法)

    private static CacheNavigation instancce;
    private Context context;

    private CacheNavigation() {
    }

    public static CacheNavigation getInstancce() {
        if (null == instancce) instancce = new CacheNavigation();
        return instancce;
    }

    public void createCacheFile(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(fileName, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
        gson = new GsonBuilder().create();
    }

    /**
     * Navigation Object to String
     *
     * @param navigation
     * @return
     * @throws NullPointerException
     */
    String navigationToString(Navigation navigation) throws NullPointerException {
        if (null == navigation) throw new NullPointerException("Navigation is null");
        return gson.toJson(navigation);
    }

    /**
     * String to Navigation
     *
     * @param navigationJson
     * @return
     * @throws NullPointerException
     */
    Navigation stringToNavigation(String navigationJson) throws NullPointerException {
        if (null == navigationJson) throw new NullPointerException("navigationJson is null");
        return gson.fromJson(navigationJson, Navigation.class);
    }

    /**
     * 增加
     *
     * @param navigation
     */
    public void putNavigation(Navigation navigation) throws NullPointerException {
        if (null == navigation) throw new NullPointerException("navigation is null");
        String key = String.valueOf(navigation.getNavOrder());
        String navigationToString = navigationToString(navigation);
        editor.putString(key, navigationToString);
        editor.apply();
    }

    /**
     * 获取
     *
     * @param order
     * @return
     */
    public Navigation getNavigation(String order) throws NullPointerException {
        if (order == null) throw new NullPointerException("order is null");
        String navigationToString = sharedPreferences.getString(order, "");//默认Navigation---->8个默认要补全
        Navigation navigation = stringToNavigation(navigationToString);
        return navigation;
    }

    /**
     * 修改,通过Navigation
     *
     * @param newNavigation
     */
    public void setNavigation(Navigation newNavigation) throws NullPointerException {
        if (null == newNavigation) throw new NullPointerException("navigation is null");
        removeNavigation(String.valueOf(newNavigation.getNavOrder()));
        putNavigation(newNavigation);
    }

    /**
     * 删除，通过Navigation
     *
     * @param navigation
     */
    public void removeNavigation(Navigation navigation) throws NullPointerException {
        if (null == navigation) throw new NullPointerException("navigation is null");
        editor.remove(String.valueOf(navigation.getNavOrder()));
    }

    /**
     * 删除，通过Navigation
     *
     * @param navigation
     */
    public void removeNavigation(String order) throws NullPointerException {
        if (order == null) throw new NullPointerException("order is null");
        editor.remove(order);
    }

    /**
     * 增加一列Navigation
     *
     * @param navigationList
     */
    public void putNavigationList(List<Navigation> navigationList) {
        clear();//增加之前先清空已缓存Navigation
        for (Navigation navigation : navigationList) {
            putNavigation(navigation);
        }
    }

    /**
     * 删除一列Navigation==清空
     *
     * @param orders
     */
    public void removeNavigationList(List<String> orders) {
        for (String order : orders) {
            removeNavigation(order);
        }
    }

    /**
     * 清空所有数据
     */
    public void clear() {
        editor.clear();
    }

    /**
     * 获取所有的Navigation
     * 如果不存在new Navigation，则使用 default Navigation。两者二选一
     *
     * @return
     */
    public List<Navigation> getAll() {
        List<Navigation> navigationList = new ArrayList<>();
        HashMap<String, String> allNavigationToString = (HashMap<String, String>) sharedPreferences.getAll();
        Set<Map.Entry<String, String>> entrySet = allNavigationToString.entrySet();
        if (entrySet.size() != 0) {//缓存了Navigation
            for (Map.Entry entry : entrySet) {
                navigationList.add(stringToNavigation((String) entry.getValue()));
            }
        } else {//没有缓存Navigation
            navigationList.addAll(createAllDefaultNavigation());
        }
        return navigationList;
    }

    /**
     * 创建默认的Navigation对象，此对象不包含icon属性。
     *
     * @param order
     * @return
     */
    public Navigation createDefaultNavigation(int order) {
        Navigation navigation = new Navigation();
        List<String> navOpenOptions = new ArrayList<>();
        switch (order) {
            case DEFAULT_ORDER_0:
                navigation.setNavOrder(DEFAULT_ORDER_0);
                navigation.setNavId(String.valueOf(DEFAULT_ORDER_0));
                navigation.setNavName("新浪");
                navigation.setNavUrl("http://sina.cn/");
                navOpenOptions.add("com.klauncher.kinflow/com.klauncher.kinflow.browser.KinflowBrower");
                navigation.setNavOpenOptions(navOpenOptions);
                navigation.setNavIcon("default/sina");
                //有待剩余字段完成..
                break;
            case DEFAULT_ORDER_1:
                navigation.setNavOrder(DEFAULT_ORDER_1);
                navigation.setNavId(String.valueOf(DEFAULT_ORDER_1));
                navigation.setNavName("淘宝");
                navigation.setNavUrl("http://www.taobao.com");
                navOpenOptions.add("com.klauncher.kinflow/com.klauncher.kinflow.browser.KinflowBrower");
                navigation.setNavOpenOptions(navOpenOptions);
                navigation.setNavIcon("default/taobao");
                //有待剩余字段完成..
                break;
            case DEFAULT_ORDER_2:
                navigation.setNavOrder(DEFAULT_ORDER_2);
                navigation.setNavId(String.valueOf(DEFAULT_ORDER_2));
                navigation.setNavName("京东");
                navigation.setNavUrl("http://m.jd.com");
                navOpenOptions.add("com.klauncher.kinflow/com.klauncher.kinflow.browser.KinflowBrower");
                navigation.setNavOpenOptions(navOpenOptions);
                navigation.setNavIcon("default/jd");
                //有待剩余字段完成..
                break;
            case DEFAULT_ORDER_3:
                navigation.setNavOrder(DEFAULT_ORDER_3);
                navigation.setNavId(String.valueOf(DEFAULT_ORDER_3));
                navigation.setNavName("同城");
                navigation.setNavUrl("http://m.58.com");
                navOpenOptions.add("com.klauncher.kinflow/com.klauncher.kinflow.browser.KinflowBrower");
                navigation.setNavOpenOptions(navOpenOptions);
                navigation.setNavIcon("default/tongcheng");
                //有待剩余字段完成..
                break;
            case DEFAULT_ORDER_4:
                navigation.setNavOrder(DEFAULT_ORDER_4);
                navigation.setNavId(String.valueOf(DEFAULT_ORDER_4));
                navigation.setNavName("美团");
                navigation.setNavUrl("http://i.meituan.com/");
                navOpenOptions.add("com.klauncher.kinflow/com.klauncher.kinflow.browser.KinflowBrower");
                navigation.setNavOpenOptions(navOpenOptions);
                navigation.setNavIcon("default/meituan");
                //有待剩余字段完成..
                break;
            case DEFAULT_ORDER_5:
                navigation.setNavOrder(DEFAULT_ORDER_5);
                navigation.setNavId(String.valueOf(DEFAULT_ORDER_5));
                navigation.setNavName("携程");
                navigation.setNavUrl("http://m.ctrip.com");
                navOpenOptions.add("com.klauncher.kinflow/com.klauncher.kinflow.browser.KinflowBrower");
                navigation.setNavOpenOptions(navOpenOptions);
                navigation.setNavIcon("default/ctrip");
                //有待剩余字段完成..
                break;
            case DEFAULT_ORDER_6:
                navigation.setNavOrder(DEFAULT_ORDER_6);
                navigation.setNavId(String.valueOf(DEFAULT_ORDER_6));
                navigation.setNavName("优酷");
                navigation.setNavUrl("http://www.youku.com/");
                navOpenOptions.add("com.klauncher.kinflow/com.klauncher.kinflow.browser.KinflowBrower");
                navigation.setNavOpenOptions(navOpenOptions);
                navigation.setNavIcon("default/youku");
                //有待剩余字段完成..
                break;
            case DEFAULT_ORDER_7:
                navigation.setNavOrder(DEFAULT_ORDER_7);
                navigation.setNavId(String.valueOf(DEFAULT_ORDER_7));
                navigation.setNavName("更多");
                navigation.setNavUrl("http://m.hao123.com/");
                navOpenOptions.add("com.klauncher.kinflow/com.klauncher.kinflow.browser.KinflowBrower");
                navigation.setNavOpenOptions(navOpenOptions);
                navigation.setNavIcon("default/");
                //有待剩余字段完成..
                break;
        }
        putNavigation(navigation);//保存持久化
        return navigation;
    }


    /**
     * 获取所有default Navigation，同上一样，不包含icon属性
     *
     * @return
     */
    public List<Navigation> createAllDefaultNavigation() {
        List<Navigation> navigationList = new ArrayList<>();
        for (int i = 0; i < DEFAULT_NAVIGATION_COUNT; i++) {
            navigationList.add(createDefaultNavigation(i));
        }
        return navigationList;
    }


    /**
     * 注册监听
     *
     * @param listener
     */
    public void registerOnSharedPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        sharedPreferences.registerOnSharedPreferenceChangeListener(listener);
    }

    /**
     * 取消监听
     *
     * @param listener
     */
    public void unregisterOnSharedPreferenceChangeListener(SharedPreferences.OnSharedPreferenceChangeListener listener) {
        sharedPreferences.unregisterOnSharedPreferenceChangeListener(listener);
    }


}