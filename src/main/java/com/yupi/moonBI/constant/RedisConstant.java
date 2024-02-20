package com.yupi.moonBI.constant;

public interface RedisConstant {
    String CHART_LIST_CACHE_KEY = "MoonBI:chartlist";
    //缓存时间为30分钟
    Long CHART_LIST_CACHE_TIME = 30L;
    String CHART_CHCHE_ID="MoonBI:Chart:id:";
    Long CHART_CACHE_TIME = 30L;
}
