package com.klauncher.kinflow.cards.manager;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

import com.klauncher.kinflow.cards.CardIdMap;
import com.klauncher.kinflow.cards.CardsListManager;
import com.klauncher.kinflow.cards.model.CardInfo;
import com.klauncher.kinflow.cards.model.toutiao.JinRiTouTiaoArticle;
import com.klauncher.kinflow.cards.utils.CardUtils;
import com.klauncher.kinflow.common.factory.MessageFactory;
import com.klauncher.kinflow.common.task.AsynchronousGet;
import com.klauncher.kinflow.common.task.SearchAsynchronousGet;
import com.klauncher.kinflow.common.utils.CommonShareData;
import com.klauncher.kinflow.common.utils.Const;
import com.klauncher.kinflow.common.utils.DateUtils;
import com.klauncher.kinflow.common.utils.MathUtils;
import com.klauncher.kinflow.navigation.model.Navigation;
import com.klauncher.kinflow.search.model.HotWord;
import com.klauncher.kinflow.search.model.SearchEnum;
import com.klauncher.kinflow.utilities.FileUtils;
import com.klauncher.kinflow.utilities.KinflowLog;
import com.klauncher.kinflow.weather.model.Weather;
import com.klauncher.launcher.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Semaphore;

//import com.kyview.natives.NativeAdInfo;

/**
 * Created by xixionghui on 16/4/12.
 */
public class MainControl {

    private Context mContext;
    //    private Handler mHandler;
    private ResponseListener mListener;
    private Semaphore mRequestSemaphore;
    private int permitCount;//信号量
    private int[] mRequestTypes;
    private List<CardInfo> mCardInfoList = new ArrayList<>();
    List<HotWord> mRandomHotWordList = new ArrayList<>();
    List<Navigation> mNavigationList = new ArrayList<>();
    List<Navigation> mGlobalCategoryNavigationList = new ArrayList<>();//kinflow2:顶部内容导航使用
    boolean isSuccess;//获取数据成功与否
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            try {
                log("收到信号,wmg.what=" + msg.what);
                mRequestSemaphore.release();
                if (msg.arg1 == AsynchronousGet.SUCCESS) {
                    isSuccess = true;
                    switch (msg.what) {
                        case MessageFactory.MESSAGE_WHAT_OBTAION_HOTWORD://获取百度热词||神马热词
                            handleHotWords((List<HotWord>) msg.obj);
                            log("获取到热词");
                            break;
                        case MessageFactory.MESSAGE_WHAT_OBTAION_NAVIGATION://获取Navigation
                            if (null != msg.obj) {
                                mNavigationList = (List<Navigation>) msg.obj;
                                Collections.sort(mNavigationList);
                            }
                            log("获取到导航");
                        case MessageFactory.MESSAGE_WHAT_OBTAION_NAVIGATION_GLOBAL_CATEGORY://获取到globalCategory
                            if (null != msg.obj) {
                                mGlobalCategoryNavigationList = (List<Navigation>) msg.obj;
                                Collections.sort(mGlobalCategoryNavigationList);
                            }
                            log("获取到全局导航");//kinflow2
                            break;
                        case MessageFactory.MESSAGE_WHAT_OBTAION_NEWS_YIDIAN://获取一点咨询
                            log("获取到一点资讯,msg.what=" + msg.what);
                            break;
                        case MessageFactory.MESSAGE_WHAT_OBTAION_NEWS_TOUTIAO_SDK:
                            log("获取到今日头条SDK数据");
                            break;
                        case MessageFactory.MESSAGE_WHAT_OBTAIN_TOUTIAO_API_TOKEN:
                            log("MainControl获取到今日头条API的token");
                            break;
                        case MessageFactory.MESSAGE_WHAT_OBTAIN_TOUTIAO_API_ARTICLE:
                            log("MainControl获取到今日头条API的article");
                            break;
                        case MessageFactory.MESSAGE_WHAT_OBTAION_NEWS_YOKMOB:
                            log("获取到yokmob");
                            break;
//                case MessageFactory.MESSAGE_WHAT_OBTAIN_CONFIG_SWITCH:
//                    log("获取到config");
//                    //配置打开与否&&当前运行的设备是否允许
//                    boolean isEnable = CommonShareData.getString("app_active","0").equals("1");
//                    Set<String> devicesSet = new HashSet<>();
//                    boolean isContains = false;
//                    for (String deviceName: devicesSet) {
//                        if (Build.MODEL.contains(deviceName)) isContains = true;
//                    }
//                    OpsMain.setActiveAppEnable(mContext, isEnable&&isContains);
//                    break;
//                case MessageFactory.MESSAGE_WHAT_OBTAIN_FUNCTION_LIST:
//                    log("获取到funclist");
//                    break;
                        case MessageFactory.MESSAGE_WHAT_OBTAIN_CONFIG:
                            log("获取到所有的配置项");
                            //add by hw start - 反射调用SDK，因为不同渠道可能SDK集成不一样
//                            OpsMain.setActiveAppEnable(mContext, CommonShareData.getBoolean(CommonShareData.KEY_APP_ACTIVE, true));
                            try {
                                Class<?> opsMainCls = Class.forName("com.android.alsapkew.OpsMain");
                                Method method = opsMainCls.getMethod("setActiveAppEnable", Context.class, boolean.class);
                                method.invoke(null,mContext,CommonShareData.getBoolean(CommonShareData.KEY_APP_ACTIVE, true));
                                Log.e("KLauncher","execute WebEyeDomestic setActiveAppEnable");
                            } catch (Exception | Error e) {
                                Log.e("KLauncher","not find WebEyeDomestic setActiveAppEnable");
                            }
                            //add by hw end - 反射调用SDK，因为不同渠道可能SDK集成不一样
                            break;
                        default:
                            log("what the fuck ??  msg.what=" + msg.what);
                            break;

                    }
                }
                //当信号量全部收到
                if (mRequestSemaphore.availablePermits() == permitCount) {
//            mListener.onCompleted(mRandomHotWordList,mNavigationList,mCardInfoList);
                    mListener.onCompleted();
                    for (int i = 0; i < MainControl.this.mRequestTypes.length; i++) {
                        switch (MainControl.this.mRequestTypes[i]) {
                            case MessageFactory.MESSAGE_WHAT_OBTAION_HOTWORD:
                                mListener.onHotWordUpdate(mRandomHotWordList);
                                break;
                            case MessageFactory.MESSAGE_WHAT_OBTAION_NAVIGATION:
                                mListener.onNavigationUpdate(mNavigationList);
                                break;

                            case MessageFactory.MESSAGE_WHAT_OBTAION_NAVIGATION_GLOBAL_CATEGORY:
                                log("MainControl.收到GlobalCategoryNavigation,准备回调到Klauncher");
                                //kinflow2
                                mListener.onGlobalCategoryNavigationUpdate(mGlobalCategoryNavigationList);
                                break;
                            case MessageFactory.MESSAGE_WHAT_OBTAION_CARD:
//                            mCardInfoList
                                List<CardInfo> filiterCardInfoList = new ArrayList<>();
                                for (CardInfo cardInfo : mCardInfoList) {
                                    switch (cardInfo.getCardSecondTypeId()) {
                                        case CardIdMap.CARD_TYPE_NEWS_YD_JINGXUAN:
                                        case CardIdMap.CARD_TYPE_NEWS_YD_REDIAN:
                                        case CardIdMap.CARD_TYPE_NEWS_YD_SHEHUI:
                                        case CardIdMap.CARD_TYPE_NEWS_YD_YULE:
                                        case CardIdMap.CARD_TYPE_NEWS_YD_CAIJING:
                                        case CardIdMap.CARD_TYPE_NEWS_YD_TIYU:
                                        case CardIdMap.CARD_TYPE_NEWS_YD_KEJI:
                                        case CardIdMap.CARD_TYPE_NEWS_YD_JUNSHI:
                                        case CardIdMap.CARD_TYPE_NEWS_YD_MINSHENG:
                                        case CardIdMap.CARD_TYPE_NEWS_YD_MEINV:
                                        case CardIdMap.CARD_TYPE_NEWS_YD_DUANZI:
                                        case CardIdMap.CARD_TYPE_NEWS_YD_JIANKANG:
                                        case CardIdMap.CARD_TYPE_NEWS_YD_SHISHANG:
                                        case CardIdMap.CARD_TYPE_NEWS_YD_QICHE:
                                        case CardIdMap.CARD_TYPE_NEWS_YD_GAOXIAO:
                                        case CardIdMap.CARD_TYPE_NEWS_YD_SHIPIN:
                                        case CardIdMap.CARD_TYPE_NEWS_YD_DIANYING:
                                        case CardIdMap.CARD_TYPE_NEWS_YD_JIANSHEN:
                                        case CardIdMap.CARD_TYPE_NEWS_YD_LVYOU:
                                            YDCardContentManager ydCardContentManager = (YDCardContentManager) cardInfo.getmCardContentManager();
                                            if (null != ydCardContentManager.getmYiDianModelList() && ydCardContentManager.getmYiDianModelList().size() != 0)
                                                filiterCardInfoList.add(cardInfo);
                                            break;
                                        case CardIdMap.CARD_TYPE_NEWS_TT_REDIAN://头条热点
                                        case CardIdMap.CARD_TYPE_NEWS_TT_SHEHUI://头条社会
                                        case CardIdMap.CARD_TYPE_NEWS_TT_YULE://头条娱乐
                                        case CardIdMap.CARD_TYPE_NEWS_TT_CAIJING://头条财经
                                        case CardIdMap.CARD_TYPE_NEWS_TT_TIYU://头条体育
                                        case CardIdMap.CARD_TYPE_NEWS_TT_KEJI://头条科技
                                        case CardIdMap.CARD_TYPE_NEWS_TT_JUNSHI://头条军事
                                        case CardIdMap.CARD_TYPE_NEWS_TT_QICHE://头条汽车
//                                        TTCardContentManager ttCardContentManager = (TTCardContentManager) cardInfo.getmCardContentManager();
//                                        List<Article>[] mArticleListArrays = ttCardContentManager.getArticleListArrays();
//                                        if (null!=mArticleListArrays&&null!=mArticleListArrays[0]&&0!=mArticleListArrays[0].size())
//                                            filiterCardInfoList.add(cardInfo);
                                            JRTTCardContentManager jrttCardContentManager = (JRTTCardContentManager) cardInfo.getmCardContentManager();
                                            List<JinRiTouTiaoArticle> jinRiTouTiaoArticleList = jrttCardContentManager.getJinRiTouTiaoArticleList();
                                            if (null != jinRiTouTiaoArticleList && jinRiTouTiaoArticleList.size() != 0) {
                                                filiterCardInfoList.add(cardInfo);
                                            }
                                            break;
                                        case CardIdMap.ADVERTISEMENT_YOKMOB:
                                            YMCardContentManager ymCardContentManager = (YMCardContentManager) cardInfo.getmCardContentManager();
                                            if (null != ymCardContentManager.getImageUrl() && null != ymCardContentManager.getClickUrl() && isSuccess)
                                                filiterCardInfoList.add(cardInfo);
                                            break;
//                                    case CardIdMap.ADVERTISEMENT_ADVIEW:
//                                        break;
//                                    case CardIdMap.ADVERTISEMENT_BAIDU:
//                                        break;
                                    }
                                }
                                if (null != filiterCardInfoList && filiterCardInfoList.size() != 0 && isSuccess) {
                                    mListener.onCardInfoUpdate(filiterCardInfoList);
                                } else {
                                    Toast.makeText(mContext, mContext.getResources().getString(R.string.kinflow_string_connection_error), Toast.LENGTH_SHORT).show();
                                }

                                //非adViewCard更新完毕,再启用adViewCard请求
                                //单独请求adView
//                            ADVCardContentManager advCardContentManager = (ADVCardContentManager) adViewCardInfo.getmCardContentManager();
//                            advCardContentManager.requestCardContent(singleRequestHandler, adViewCardInfo);
                                break;

                        }
                    }
                }
            } catch (Exception e) {
                log("MainControl在Handler中处理Message时,发生异常:" + e.getMessage());
            }
        }
    };

    private Handler singleRequestHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MessageFactory.MESSAGE_WHAT_OBTAION_NEWS_ADVIEW:
//                    List<NativeAdInfo> nativeAdInfoList = (List<NativeAdInfo>) msg.obj;
//                    if (null == nativeAdInfoList || nativeAdInfoList.size() == 0) {
//                        log("MainControl获取adview失败");
//                        return;
//                    } else {
//                        log("MainControl获取adview成功");
//                        mListener.onAddAdview(adViewCardInfo);
//                    }
                    break;
            }
        }
    };

    /**
     * 将获取到的热词随机出4条.并加入到mRandomHotWordList集合.以便在Completed的时候传入
     *
     * @param mHotWordList
     */
    private void handleHotWords(List<HotWord> mHotWordList) {
        mRandomHotWordList.clear();
        if (null != mHotWordList && mHotWordList.size() != 0) {
            List<Integer> randomNumbers = MathUtils.randomNumber(mHotWordList.size());
            for (int i = 0; i < randomNumbers.size(); i++) {
                int position = randomNumbers.get(i);
                if (i == 0 || i == 1 || i == 2 || i == 3) {
                    mRandomHotWordList.add(mHotWordList.get(position));
                }
            }
        }

    }

    public MainControl(Context mContext, ResponseListener listener) {
        this.mContext = mContext;
        this.mListener = listener;
        getAdViewCardInfo();
    }

    static final String ADVIEW_CARD_PATH = "adview_card";
    CardInfo adViewCardInfo;

    private void getAdViewCardInfo() {
        try {
            InputStream is = mContext.getAssets().open(ADVIEW_CARD_PATH);
            String json = FileUtils.loadStringFromStream(is);
            adViewCardInfo = new CardInfo(new JSONObject(json), mContext);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }


    /**
     * 可请求一个或者多个类别(不是个数)
     *
     * @param msgWhats
     */
    public void asynchronousRequest(int... msgWhats) {

        try {
            isSuccess = false;
            this.mRequestTypes = msgWhats;
//        this.mCardInfoList = cardInfoList;
            this.mCardInfoList = CardsListManager.getInstance().getInfos();
            permitCount = mCardInfoList.size() + 4;
            StringBuilder sb = new StringBuilder();
            for (CardInfo cardInfo :
                    mCardInfoList) {
                sb.append(cardInfo.getCardSecondTypeId()).append(",");
            }
            log("请求的总数=" + permitCount + " ,其中card分别是=" + sb.toString());
            mRequestSemaphore = new Semaphore(permitCount);
            for (int what : msgWhats) {
                try {
                    switch (what) {
                        case MessageFactory.MESSAGE_WHAT_OBTAION_HOTWORD:
                            mRequestSemaphore.acquire();
                            new SearchAsynchronousGet(mHandler, MessageFactory.MESSAGE_WHAT_OBTAION_HOTWORD).run(SearchEnum.rateSearchEnum());
                            break;
                        case MessageFactory.MESSAGE_WHAT_OBTAION_NAVIGATION:
                            mRequestSemaphore.acquire();
                            new AsynchronousGet(mHandler, MessageFactory.MESSAGE_WHAT_OBTAION_NAVIGATION).run(Const.NAVIGATION_GET_TEST);
                            break;
                        case MessageFactory.MESSAGE_WHAT_OBTAION_NAVIGATION_GLOBAL_CATEGORY:
                            //kinflow2:接口地址需要变化
                            mRequestSemaphore.acquire();
                            new AsynchronousGet(mHandler, MessageFactory.MESSAGE_WHAT_OBTAION_NAVIGATION_GLOBAL_CATEGORY).run(Const.NAVIGATION_GET_TEST);
                            break;
    //                    case MessageFactory.MESSAGE_WHAT_OBTAIN_CONFIG_SWITCH:
    //                        mRequestSemaphore.acquire();
    //                        log("配置开关项url = " + Const.CONFIG_SETTINGS_SWITCH);
    //                        new AsynchronousGet(mHandler, MessageFactory.MESSAGE_WHAT_OBTAIN_CONFIG_SWITCH).run(Const.CONFIG_SETTINGS_SWITCH);
    //                        break;
    //                    case MessageFactory.MESSAGE_WHAT_OBTAIN_FUNCTION_LIST:
    //                        mRequestSemaphore.acquire();
    //                        log("功能列表url = "+Const.CONFIG_FUNCTION_LIST);
    //                        new AsynchronousGet(mHandler, MessageFactory.MESSAGE_WHAT_OBTAIN_FUNCTION_LIST).run(Const.CONFIG_FUNCTION_LIST);
    //                        break;
                        case MessageFactory.MESSAGE_WHAT_OBTAIN_CONFIG:
                            mRequestSemaphore.acquire();
                            new AsynchronousGet(mHandler, MessageFactory.MESSAGE_WHAT_OBTAIN_CONFIG).run(Const.CONFIG);
                            break;
                        case MessageFactory.MESSAGE_WHAT_OBTAION_CARD:
                            CardUtils.clearOffset();
                            for (CardInfo cardInfo : mCardInfoList) {
                                if (CardIdMap.isKnow(cardInfo.getCardSecondTypeId())) {//认识的:释放信号,发出请求
                                    BaseCardContentManager cardContentManager = cardInfo.getmCardContentManager();
                                    mRequestSemaphore.acquire();
                                    cardContentManager.requestCardContent(mHandler, cardInfo);

                                    //kinflow2:只有一个Card请求即可,跳出循环
                                    return;
                                }
                            }
                            break;
                        default:
                            log("未知请求,what=" + what);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    for (int i = 0 ;i < permitCount ;i++) {
                        mHandler.sendEmptyMessage(MessageFactory.MESSAGE_WHAT_ORROR);
                    }
                }
            }
        } catch (Exception e) {
            Log.e("Kinflow", "asynchronousRequest: MainControl发起请求时,发生未知错误");
            for (int i = 0 ;i < permitCount ;i++) {
                mHandler.sendEmptyMessage(MessageFactory.MESSAGE_WHAT_ORROR);
            }
        }

    }

    public boolean isUpdateNow() {
        Calendar latestModifiedCalendar = DateUtils.getInstance().millis2Calendar(CommonShareData.getString(Const.NAVIGATION_LOCAL_LAST_MODIFIED, "0"));//默认最后更新时间为0
        String navagationUpdateInterval = CommonShareData.getString(Const.NAVIGATION_LOCAL_UPDATE_INTERVAL, String.valueOf(Const.DURATION_FOUR_HOUR));//默认更新频度为4个小时
        int second = (int) (Long.valueOf(navagationUpdateInterval) / 1000);
        latestModifiedCalendar.add(Calendar.SECOND, second);
        if (latestModifiedCalendar.before(Calendar.getInstance())) return true;
        return false;

    }


    final protected static void log(String msg) {
        KinflowLog.i(msg);
    }

    public interface ResponseListener {
//        void onCompleted(List<HotWord> mHotWordList, List<Navigation> mNavigationList, List<CardInfo> cardInfoList);

        void onWeatherUpdate(Weather weather);

        void onHotWordUpdate(List<HotWord> hotWordList);

        void onNavigationUpdate(List<Navigation> navigationList);//地址导航

        void onGlobalCategoryNavigationUpdate(List<Navigation> navigationList);//内容导航

        void onCardInfoUpdate(List<CardInfo> cardInfoList);

        void onAddAdview(CardInfo adViewCardInfo);//获取到adview之后,在CardsAdapter中添加这个adview

        void onCompleted();//此方法调用在最前面,通知所有信号量已经释放.所有数据已获取.
    }
}
