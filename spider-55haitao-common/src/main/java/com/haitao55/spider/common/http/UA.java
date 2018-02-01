package com.haitao55.spider.common.http;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 
 * 功能：user agent
 * 
 * @author Arthur.Liu
 * @time 2016年5月31日 下午3:21:39
 * @version 1.0
 */
public enum UA {

    CHROME_31(
            "Mozilla/5.0 (Windows NT 5.1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/31.0.1650.16 Safari/537.36"),

    CHROME_29(
            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/29.0.1547.62 Safari/537.36"),

    IE_8("Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; Trident/4.0)"),

    IE_10(
            "Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; WOW64; Trident/6.0)");

    private String value;

    UA(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static Collection<String> codes() {
        List<String> list = new ArrayList<String>();
        for (UA ua : values()) {
            list.add(ua.getValue());
        }
        return list;
    }

}