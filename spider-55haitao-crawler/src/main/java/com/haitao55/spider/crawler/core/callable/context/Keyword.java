package com.haitao55.spider.crawler.core.callable.context;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * 
 * 功能：定义了callable中出现的关键字<br>
 * 这些关键字在rule文件中有特殊含义<br>
 * 
 * <p>
 * ${url} 当前url<br>
 * ${html} 当前url对应的html<br>
 * ${doc} 当前url对应的document<br>
 * ${newurls} 当前url页面（一般是link页面）的新增urls<br>
 * </p>
 *
 * @author Arthur.Liu
 * @time Jul 22, 2015 1:41:45 PM
 * @version 1.0
 *
 */
public enum Keyword {
    URL("${url}"), HTML("${html}"), DOC("${doc}"), NEWURLS("${newurls}"), SITE(
            "${site}");

    private String value;

    private Keyword(String value) {
        this.value = value;
    }

    public String getValue() {
        return this.value;
    }

    // 用map增强性能
    private static final Map<String, Keyword> map = new HashMap<String, Keyword>();
    static {
        for (Keyword keyword : values()) {
            map.put(keyword.getValue().toLowerCase(), keyword);
        }
    }

    public static Keyword codeOf(String keyword) {
        return map.get(keyword == null ? null : keyword.toLowerCase());
    }

    public static Collection<String> codes() {
        // list返回给调用者之后，可能会被修改里面的元素；为避免这种情况的出现，这里每次都返回一个新list对象
        Set<String> list = new HashSet<String>();
        for (Keyword keyword : values()) {
            list.add(keyword.getValue());
        }
        return list;
    }
};