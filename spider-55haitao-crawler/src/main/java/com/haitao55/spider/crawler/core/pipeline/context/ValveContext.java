package com.haitao55.spider.crawler.core.pipeline.context;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.haitao55.spider.crawler.core.model.Url;

/**
 * 
 * 功能：在各Valve之间保存/传递/共享数据
 * 
 * @author Arthur.Liu
 * @time 2016年6月1日 上午10:13:01
 * @version 1.0
 */
@SuppressWarnings("unchecked")
public class ValveContext {

    private static final String URLS = "context_urls";

    private static final ThreadLocal<Map<String, Object>> LOCAL = new ThreadLocal<Map<String, Object>>() {
        protected Map<String, Object> initialValue() {
            return new LinkedHashMap<String, Object>();
        }
    };

    public static <T> T get(String key) {
        return (T) LOCAL.get().get(key);
    }

    public static void put(String key, Object value) {
        LOCAL.get().put(key, value);
    }

    public static List<Url> getUrls() {
        List<Url> list = (List<Url>) LOCAL.get().get(URLS);
        if (list == null) {
            list = new LinkedList<Url>();
            LOCAL.get().put(URLS, list);
        }
        return list;
    }
    
    public static void putUrls(List<Url> urls) {
        List<Url> list = (List<Url>) LOCAL.get().get(URLS);
        if (list == null) {
            list = new LinkedList<Url>();
            LOCAL.get().put(URLS, list);
        }
        list.addAll(urls);
    }

    public static void clear() {
        LOCAL.get().clear();
    }
}