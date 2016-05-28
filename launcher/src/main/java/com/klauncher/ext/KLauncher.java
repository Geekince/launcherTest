package com.klauncher.ext;

import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.v7.internal.widget.CompatTextView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.launcher3.Launcher;
import com.android.launcher3.backup.LbkPackager;
import com.android.launcher3.backup.LbkUtil;
import com.android.launcher3.settings.SettingsValue;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshScrollView;
import com.klauncher.kinflow.cards.CardIdMap;
import com.klauncher.kinflow.cards.CardsListManager;
import com.klauncher.kinflow.cards.adapter.CardItemDecoration;
import com.klauncher.kinflow.cards.adapter.CardsAdapter;
import com.klauncher.kinflow.cards.manager.CardContentManagerFactory;
import com.klauncher.kinflow.cards.manager.MainControl;
import com.klauncher.kinflow.cards.model.CardInfo;
import com.klauncher.kinflow.common.factory.MessageFactory;
import com.klauncher.kinflow.common.utils.CacheNavigation;
import com.klauncher.kinflow.common.utils.CommonUtils;
import com.klauncher.kinflow.common.utils.Const;
import com.klauncher.kinflow.navigation.adapter.NavigationAdapter;
import com.klauncher.kinflow.navigation.model.Navigation;
import com.klauncher.kinflow.search.model.HotWord;
import com.klauncher.kinflow.search.model.HotWordItemDecoration;
import com.klauncher.kinflow.utilities.Dips;
import com.klauncher.kinflow.utilities.KinflowLog;
import com.klauncher.kinflow.utilities.NetworkUtils;
import com.klauncher.kinflow.weather.model.Weather;
import com.klauncher.launcher.R;
import com.klauncher.ping.PingManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;

public class KLauncher extends Launcher implements SharedPreferences.OnSharedPreferenceChangeListener, MainControl.ResponseListener {

    private static final String TAG = "KLauncher";
    /**
     * 个推 第三方应用Master Secret，修改为正确的值
     */
    private static final String MASTERSECRET = "1JazIIe2Id5WbdT8gTMak2";

    /*private static final String HOME_PAGE = "file:///android_asset/home.html";
    private static final String APP_NAME = "今日头题";
    private static final String NEWS_BAIDU = "http://m.baidu.com/news?fr=mohome&ssid=1dc073756e6c6169724503&from=&uid=&pu=sz%40224_220%2Cta%40iphone___3_537&bd_page_type=1";
    private static final String DEFAULT_APP_PACKAGENAME= "com.ss.android.article.news";
    private static final String DEFAULT_APP_CLASS ="com.ss.android.article.news.activity.SplashActivity";
    private static final String APP_DLD_LINK = "http://mobile.baidu.com/simple?action=content&type=soft&docid=8356755";

    private boolean firstRun = true;*/
    private boolean isShown = false;
    AlertDialog mAlertDialog;
    static final int SETTING_WIFI = 59;

    //    private WebView mWebView;
    //kinflow
    RelativeLayout mWeatherLayout;
    //private LinearLayout mLayoutContent;
    TextView tv_temperature;
    TextView tv_city;
    TextView tv_weather;
    ImageView iv_weatherType;
    ImageView iv_searchMode;
    ImageView iv_searchIcon;
    TextView tv_searchHint;
    CompatTextView tv_hotWord1;
    CompatTextView tv_hotWord2;
    CompatTextView tv_hotWordTop;
    ImageView iv_refresh;
    RelativeLayout randomNewsLine;
    RecyclerView navigationRecyclerView;

    PullToRefreshScrollView mPullRefreshScrollView;
    ScrollView mScrollView;
    RecyclerView mCardsView;

    private HotWord hotWord1 = HotWord.getDefaultHotWord1();
    private HotWord hotWord2 = HotWord.getDefaultHotWord2();
    private HotWord hintHotWord = HotWord.getHintHotWord();
    private HotWord topHotWord = HotWord.getTopHotWord();
    private MainControl mMainControl;

    private boolean mNetworkConnected = true;
    private BroadcastReceiver mWifiChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            try {
                if (mNetworkConnected != NetworkUtils.isNetworkAvailable(context)) {
                    mNetworkConnected = NetworkUtils.isNetworkAvailable(context);
                    if (mNetworkConnected) {//断开变为连接:
                        CardsListManager.getInstance().loadCardList();
                        mMainControl.asynchronousRequest(MessageFactory.MESSAGE_WHAT_OBTAION_HOTWORD,
                                MessageFactory.MESSAGE_WHAT_OBTAION_NAVIGATION,
                                MessageFactory.MESSAGE_WHAT_OBTAION_CARD,
                                MessageFactory.MESSAGE_WHAT_OBTAIN_CONFIG
                        );
                    } else {//

                    }
                }
            } catch (Exception e) {

            }
        }
    };

    @Override
    protected boolean hasCustomContentToLeft() {
        Log.e("Klaucher_0512", "KLauncher hasCustomContentToLeft()");
        boolean bKinlowSet = SettingsValue.isKinflowSetOn(this);
        return bKinlowSet;
        //return true;
    }

    @Override
    public void populateCustomContentContainer() {
        Log.e("Klaucher_0512", "KLauncher populateCustomContentContainer()");
        //View customView = getLayoutInflater().inflate(R.layout.custom, null);
        View customView = getLayoutInflater().inflate(R.layout.activity_main, null);
        /*mWebView = (WebView) customView.findViewById(R.id.webcontent);
        if (null != mWebView) {
            setupWebView();
		}*/
        //mLayoutContent = (LinearLayout) customView.findViewById(R.id.layoutcontent);
        initKinflow(customView);

        CustomContentCallbacks callbacks = new CustomContentCallbacks() {
            @Override
            public void onShow(boolean fromResume) {
                LauncherLog.i(TAG, "callbacks: " + fromResume);
                isShown = true;
            }

            @Override
            public void onHide() {
                isShown = false;
            }

            @Override
            public void onScrollProgressChanged(float progress) {
                // TODO Auto-generated method stub

            }

            @Override
            public boolean isScrollingAllowed() {
                // TODO Auto-generated method stub
                return true;
            }
        };

        addToCustomContentPage(customView, callbacks, "custom-view");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //设置透明状态栏
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        com.android.alsapkew.OpsMain.init(this);
        PingManager.getInstance().ping(4, null);
        //启动添加网络设置快捷方式
        Intent startIntent = new Intent(this, ShortCutManagerService.class);
        startIntent.putExtra("add_klaucher3_wifi", true);
        startService(startIntent);
        /*//个推初始化
        IntentFilter mFilter = new IntentFilter();
        mFilter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        mFilter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        mFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(myNetReceiver, mFilter);
        initGeitui();*/
        Log.e("Klaucher_0512", "KLauncher onCreate()");
        //add error
        /*String[] strs = {};
        Log.d("strs",strs[0]);*/
        /*if (PushDemoReceiver.payloadData != null) {
            Toast.makeText(this,PushDemoReceiver.payloadData.toString(),Toast.LENGTH_LONG).show();
        }*/
    }


    @Override
    protected void onResume() {
        super.onResume();
        Log.e("Klaucher_0512", "KLauncher onResume() start1111111111");

        com.android.alsapkew.OpsMain.setActivity(this);

        if (PingManager.getInstance().needReportLauncherAppList()) {
            LbkPackager.startBackup(this, new LbkPackager.BackupListener() {
                @Override
                public void onBackupStart() {
                }

                @Override
                public void onBackupEnd(boolean success) {
                    if (success) {
                        File xmlFile = new File(LbkUtil.getXLauncherLbkBackupTempPath() + File.separator + LbkUtil.DESC_FILE);
                        FileInputStream fis;
                        StringBuilder sb = new StringBuilder();
                        try {
                            fis = new FileInputStream(xmlFile);
                            BufferedReader br = new BufferedReader(new InputStreamReader(fis));
                            String line;
                            while ((line = br.readLine()) != null) {
                                sb.append(line);
                            }
                        } catch (FileNotFoundException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        PingManager.getInstance().reportLauncherAppList(sb.toString());
                    }
                }
            });
        }

        try {
            ComponentName componentName = new ComponentName(
                    "com.ss.android.article.news", "com.ss.android.message.NotifyService");
            Intent service = new Intent();
            service.setComponent(componentName);
            startService(service);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        Log.e("Klaucher_0512", "KLauncher onResume() end1111111111");
    }

    @Override
    public void onBackPressed() {
        /*if (null!= mWebView && mWebView.canGoBack()) {
            mWebView.goBack();
        } else {
            super.onBackPressed();
        }*/
        super.onBackPressed();
    }

    /*private void setupWebView() {
        mWebView.getSettings().setJavaScriptEnabled(true);//设置使用够执行JS脚本
        mWebView.getSettings().setBuiltInZoomControls(true);//设置使支持缩放
        mWebView.getSettings().setDisplayZoomControls(false);
        mWebView.setWebViewClient(new WeWebViewClient());

        mWebView.loadUrl(HOME_PAGE);
    }

    private class WeWebViewClient extends WebViewClient {
        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.d(TAG, "shouldOverrideUrlLoading: " + url);
            if (!isShown) {
                return super.shouldOverrideUrlLoading(view, url);
            }

            if (url.equals(NEWS_BAIDU)) {
                view.loadUrl(url);
                return true;
            }

            if (url.startsWith(HOME_PAGE)) {
                view.loadUrl(url);
            } else {
                if (firstRun) {
                    view.loadUrl(url);
                    firstRun = false;
                } else {
                    start3rdActivity(view, url);
                }
            }
            return true;
        }

        @Override
        public void onReceivedError(WebView view, int errorCode,
                                    String description, String failingUrl) {
            Log.d(TAG, "onReceivedError: " + failingUrl);
        }
    }

    private void start3rdActivity(WebView view, String url) {
        try {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_VIEW);
            intent.setComponent(new ComponentName(DEFAULT_APP_PACKAGENAME, DEFAULT_APP_CLASS));
            Uri content_url = Uri.parse(url);
            intent.setData(content_url);
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            String message = APP_NAME + "未安装";
            final AlertDialog aboutDialog = new AlertDialog.Builder(KLauncher.this).
                    setMessage(message).
                    setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_VIEW);
                            Uri content_url = Uri.parse(APP_DLD_LINK);
                            intent.setData(content_url);
                            startActivity(intent);
                        }
                    }).create();
            aboutDialog.show();
            view.loadUrl(url);
        }
    }*/
    int downX, downY;
    private int DRAG_X_THROD = 0;
    private int SCROLL_X = 0;
    private final int ANIM_DURATION = 300;

    private void initKinflow(View customview) {
        try {
            // View kinflowView = getLayoutInflater().inflate(R.layout.activity_main, null);
            View kinflowView = customview;
            //initview
            mWeatherLayout = (RelativeLayout) kinflowView.findViewById(R.id.weather_header);
            tv_temperature = (TextView) kinflowView.findViewById(R.id.temperature);
            tv_city = (TextView) kinflowView.findViewById(R.id.city);
            tv_weather = (TextView) kinflowView.findViewById(R.id.weather);
            iv_weatherType = (ImageView) kinflowView.findViewById(R.id.weather_type);
            tv_searchHint = (TextView) kinflowView.findViewById(R.id.search_hint);
            iv_searchMode = (ImageView) kinflowView.findViewById(R.id.search_mode);
            iv_searchIcon = (ImageView) kinflowView.findViewById(R.id.search_icon);
            tv_hotWordTop = (CompatTextView) kinflowView.findViewById(R.id.hot_word_top);
            tv_hotWord1 = (CompatTextView) kinflowView.findViewById(R.id.hot_word_1);
            tv_hotWord2 = (CompatTextView) kinflowView.findViewById(R.id.hot_word_2);
            iv_refresh = (ImageView) kinflowView.findViewById(R.id.refresh_hotWord);
            randomNewsLine = (RelativeLayout) kinflowView.findViewById(R.id.random_news_line);
            navigationRecyclerView = (RecyclerView) kinflowView.findViewById(R.id.navigation_recyclerView);
            mCardsView = (RecyclerView) kinflowView.findViewById(R.id.scroll_view_cards);
            mPullRefreshScrollView = (PullToRefreshScrollView) kinflowView.findViewById(R.id.pull_refresh_scrollview);
            //mLayoutContent.addView(kinflowView, new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
            //listener
            tv_searchHint.setOnClickListener(this);
            iv_searchMode.setOnClickListener(this);
            iv_searchIcon.setOnClickListener(this);
            iv_refresh.setOnClickListener(this);
            tv_hotWordTop.setOnClickListener(this);
            tv_hotWord1.setOnClickListener(this);
            tv_hotWord2.setOnClickListener(this);
            mWeatherLayout.setOnClickListener(this);
            //初始化数据---default data
            CardsListManager.getInstance().init(this);
            tv_hotWord1.setText(hotWord1.getWord());
            tv_hotWord2.setText(hotWord2.getWord());
            navigationRecyclerView.setLayoutManager(new GridLayoutManager(this, 4, GridLayoutManager.VERTICAL, false));
            List<Navigation> navigationList = CacheNavigation.getInstance().getAll();
            Collections.sort(navigationList);
            navigationRecyclerView.setAdapter(new NavigationAdapter(KLauncher.this, navigationList));
            navigationRecyclerView.addItemDecoration(new HotWordItemDecoration(16, 32, false));

//        CardsListManager.getInstance().loadCardList();
            mCardsView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
            mCardsView.addItemDecoration(new CardItemDecoration(Dips.dipsToIntPixels(12, this)));
            //mCardInfoList = CardsListManager.getInstance().getInfos();
            mCardsView.setAdapter(new CardsAdapter(this, null));//最初传入空,等待收到数据后更新

            //异步获取所有
//        requestLocation();//此版本已没有天气模块,但是保留天气模块相关代码
            mMainControl = new MainControl(KLauncher.this, this);
            mMainControl.asynchronousRequest(MessageFactory.MESSAGE_WHAT_OBTAION_HOTWORD,
                    MessageFactory.MESSAGE_WHAT_OBTAION_NAVIGATION,
                    MessageFactory.MESSAGE_WHAT_OBTAION_CARD,
                    MessageFactory.MESSAGE_WHAT_OBTAIN_CONFIG
//                    MessageFactory.MESSAGE_WHAT_OBTAIN_CONFIG_SWITCH,
//                    MessageFactory.MESSAGE_WHAT_OBTAIN_FUNCTION_LIST
            );
            //注册监听
            CacheNavigation.getInstance().registerOnSharedPreferenceChangeListener(this);
//        CacheLocation.getInstance().registerOnSharedPreferenceChangeListener(this);//此版本已没有天气模块,但是保留天气模块相关代码
            //初始化下拉刷新
            mPullRefreshScrollView.setOnRefreshListener(new PullToRefreshBase.OnRefreshListener<ScrollView>() {

                @Override
                public void onRefresh(PullToRefreshBase<ScrollView> refreshView) {
                    //当下拉刷新的时候
                    //                requestLocation();
                    //loadCardList
                    CardsListManager.getInstance().loadCardList();
                    mMainControl.asynchronousRequest(MessageFactory.MESSAGE_WHAT_OBTAION_HOTWORD,
                            MessageFactory.MESSAGE_WHAT_OBTAION_NAVIGATION,
                            MessageFactory.MESSAGE_WHAT_OBTAION_CARD,
                            MessageFactory.MESSAGE_WHAT_OBTAIN_CONFIG
//                            MessageFactory.MESSAGE_WHAT_OBTAIN_CONFIG_SWITCH,
//                            MessageFactory.MESSAGE_WHAT_OBTAIN_FUNCTION_LIST
                    );
                }
            });

            mScrollView = mPullRefreshScrollView.getRefreshableView();
            mScrollView.setFillViewport(true);
            mPullRefreshScrollView.setMode(PullToRefreshBase.Mode.PULL_FROM_START);//关闭加载更多
            //
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);//A
//        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);//B
//        toggleHideyBar();
            //注册网络监听
            IntentFilter filter = new IntentFilter();
            filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
            filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
            filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
            this.registerReceiver(mWifiChangeReceiver, filter);
        } catch (Exception e) {
            log("初始化信息流的时候出错initKinflow: " + e.getMessage());
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
    }


    @Override
    public void scrollToKinflow() {
//        log("滑到信息流界面");
        try {
            if (CommonUtils.getInstance().allowSkip())
                CommonUtils.getInstance().openDefaultBrowserUrl(this, Const.URL_2345_HOMEPAGE);
        } catch (Exception e) {
            log("每天一次跳转到2345界面出错");
        }
    }

    /**
     * 请求定位:此版本已没有天气模块,但是保留天气模块相关代码
     */
    /*
    void requestLocation() {
        if (!DeviceState.gspIsOPen(this)) {
            mAlertDialog = DialogUtils.getCommontAlertDialog(this,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mAlertDialog.dismiss();
                        }
                    }, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mAlertDialog.dismiss();
                            final Intent intentWiFi = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            intentWiFi.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivityForResult(intentWiFi, SETTING_WIFI);
                        }
                    });
            mAlertDialog.show();
        } else {
            startService(new Intent(KLauncher.this, LocationService.class));
        }
    }
    */
    @Override
    public void onClick(View view) {
//        startService(new Intent(KLauncher.this, LocationService.class));
        super.onClick(view);
        switch (view.getId()) {
            case R.id.hot_word_1://热词1监听
                CommonUtils.getInstance().openHotWord(this, hotWord1.getUrl());
                break;
            case R.id.hot_word_2://热词2监听
                CommonUtils.getInstance().openHotWord(this, hotWord2.getUrl());
                break;
            case R.id.hot_word_top:
                CommonUtils.getInstance().openHotWord(this, topHotWord.getUrl());
                break;
            case R.id.refresh_hotWord://刷新监听
//                asynchronousRequest(MessageFactory.MESSAGE_WHAT_OBTAION_HOTWORD);
                //此处传入mCardInfoList无用
                ObjectAnimator animator = ObjectAnimator.ofFloat(iv_refresh, "rotation", 0f, 360f);
                animator.setDuration(500);
                animator.start();
                mMainControl.asynchronousRequest(MessageFactory.MESSAGE_WHAT_OBTAION_HOTWORD);
                break;
            case R.id.search_hint://搜索框监听
                Intent intent = new Intent(KLauncher.this, com.klauncher.kinflow.search.SearchActivity.class);
                if (null == hintHotWord || null == hintHotWord.getWord())
                    hintHotWord = HotWord.getHintHotWord();
                intent.putExtra(com.klauncher.kinflow.search.SearchActivity.HITN_HOT_WORD_KEY, hintHotWord);
                startActivity(intent);
                break;
            case R.id.search_mode:
            case R.id.search_icon:
                if (null != hintHotWord && null != hintHotWord.getWord()) {
                    CommonUtils.getInstance().openHotWord(this, hintHotWord.getUrl());
                } else {
                    Toast.makeText(this, "没有获取到搜索热词,请刷新", Toast.LENGTH_SHORT).show();
                }
                break;
            //
            /*
            case R.id.weather_header:
                requestLocation();
                log("启动墨迹天气");
                if (CommonUtils.getInstance().isInstalledAPK(this, OpenMode.COMPONENT_NAME_MOJI_TIANQI))
                    CommonUtils.getInstance().openApp(this, OpenMode.COMPONENT_NAME_MOJI_TIANQI);
                break;
            */
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        CardContentManagerFactory.clearAllOffset();
        CacheNavigation.getInstance().unregisterOnSharedPreferenceChangeListener(this);
//        CacheLocation.getInstance().unregisterOnSharedPreferenceChangeListener(this);
        Log.e("Klaucher_0512", "KLauncher onDestroy()");
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        /*
        Log.d("MainActivity", "收到缓存数据变更通知 key=" +
                key);
        if (key.equals(CacheLocation.KEY_LAT_LNG)) {//位置信息变更----->驱动天气数据变更
            mMainControl.obtainCityName();
        } else {//Navigation变更

        }
        */

        //Navigation变更
    }

    /**
     * 当更新天气,此版本已没有天气模块,但是保留天气模块相关代码
     *
     * @param weatherObject
     */
    @Override
    public void onWeatherUpdate(Weather weatherObject) {
        tv_temperature.setText(weatherObject.getTemp());
        tv_city.setText(weatherObject.getCity());
        tv_weather.setText(weatherObject.getWeather());

        String weatherType = weatherObject.getWeather();
        if (weatherType.equals("晴")) {
            iv_weatherType.setBackgroundResource(R.drawable.sunny);

        } else if (weatherType.equals("多云")) {
            iv_weatherType.setBackgroundResource(R.drawable.cloudy);

        } else if (weatherType.equals("阴")) {
            iv_weatherType.setBackgroundResource(R.drawable.overcast);

        } else if (weatherType.equals("阵雨")) {
            iv_weatherType.setBackgroundResource(R.drawable.shower);

        } else if (weatherType.equals("雷阵雨")) {
            iv_weatherType.setBackgroundResource(R.drawable.thundershow);

        } else if (weatherType.equals("雷阵雨伴有冰雹")) {
            iv_weatherType.setBackgroundResource(R.drawable.thundershower_with_hail);

        } else if (weatherType.equals("雨夹雪")) {
            iv_weatherType.setBackgroundResource(R.drawable.sleet);

        } else if (weatherType.equals("小雨")) {
            iv_weatherType.setBackgroundResource(R.drawable.light_rain);

        } else if (weatherType.equals("中雨")) {
            iv_weatherType.setBackgroundResource(R.drawable.moderate_rain);

        } else if (weatherType.equals("大雨")) {
            iv_weatherType.setBackgroundResource(R.drawable.heavy_rain);

        } else if (weatherType.equals("暴雨")) {
            iv_weatherType.setBackgroundResource(R.drawable.storm);

        } else if (weatherType.equals("大暴雨")) {
            iv_weatherType.setBackgroundResource(R.drawable.heavy_storm);

        } else if (weatherType.equals("特大暴雨")) {
            iv_weatherType.setBackgroundResource(R.drawable.severe_storm);

        } else if (weatherType.equals("阵雪")) {
            iv_weatherType.setBackgroundResource(R.drawable.snow_flurry);

        } else if (weatherType.equals("小雪")) {
            iv_weatherType.setBackgroundResource(R.drawable.light_snow);

        }
    }

    @Override
    public void onCompleted() {
        mPullRefreshScrollView.onRefreshComplete();
    }

    @Override
    public void onHotWordUpdate(List<HotWord> hotWordList) {
        //更新热词
        if (null != hotWordList && hotWordList.size() != 0) {
            log("==========更新热词===========");
            hintHotWord = hotWordList.get(0);
            tv_searchHint.setHint(hintHotWord.getWord());
            hotWord1 = hotWordList.get(1);
            tv_hotWord1.setText(hotWord1.getWord());
            hotWord2 = hotWordList.get(2);
            tv_hotWord2.setText(hotWord2.getWord());
            topHotWord = hotWordList.get(3);
            tv_hotWordTop.setText(topHotWord.getWord());
        }
    }

    @Override
    public void onNavigationUpdate(List<Navigation> navigationList) {
        //更新导航页
        if (null != navigationList && navigationList.size() != 0) {
            log("==========更新导航页===========");
            NavigationAdapter adapter = (NavigationAdapter) navigationRecyclerView.getAdapter();
            adapter.updateNavigationList(navigationList);
            adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void onCardInfoUpdate(List<CardInfo> cardInfoList) {
        //更新CardsAdapter
        if (null != cardInfoList && cardInfoList.size() != 0) {
            log("==========更新CardsAdapter===========");
//            CardsAdapter cardsAdapter = (CardsAdapter) mCardsView.getAdapter();
//            cardsAdapter.updateCards(cardInfoList);
            mCardsView.removeAllViews();
            CardsAdapter cardsAdapter = new CardsAdapter(this, cardInfoList);
            mCardsView.setAdapter(cardsAdapter);
        }
    }

    @Override
    public void onAddAdview(CardInfo cardInfo) {
        CardsAdapter adapter = (CardsAdapter) mCardsView.getAdapter();
        if (null == adapter) return;
        int adapterItemCount = adapter.getItemCount();
        if (adapterItemCount <= 1) {
            log("Card的个数<=1,所以不添加adview广告");
            return;
        }
        int type = adapter.getItemViewType(adapter.getItemCount() - 1);
        if (type != CardIdMap.ADVERTISEMENT_ADVIEW) {//没有adview----添加
            adapter.addCard(cardInfo);
            log("==========添加adview完成==========");
        } else if (type == CardIdMap.ADVERTISEMENT_ADVIEW) {//有adview-----删除换新
            adapter.notifyItemRemoved(adapter.getItemCount() - 1);
            adapter.addCard(cardInfo);
            log("==========更新adview完成==========");
        }
    }

    final protected static void log(String msg) {
        KinflowLog.i(msg);
    }

}
