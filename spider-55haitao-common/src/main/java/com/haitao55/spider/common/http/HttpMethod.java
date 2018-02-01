package com.haitao55.spider.common.http;

import org.apache.commons.lang3.StringUtils;

/**
 * 
 * 功能：http所支持的method
 * 
 * @author Arthur.Liu
 * @time 2016年5月31日 下午3:03:21
 * @version 1.0
 */
public enum HttpMethod {
    // http://www.w3.org/Protocols/rfc2616/rfc2616-sec9.html
    HEAD("HEAD"), PUT("PUT"), GET("GET"), POST("POST"), DELETE("DELETE"), TRACE(
            "TRACE"), CONNECT("CONNECT"), OPTIONS("OPTIONS");

    private String method;

    private HttpMethod(String method) {
        this.method = method;
    }

    public static HttpMethod codeOf(String method) {
        method = StringUtils.trim(method);
        for (HttpMethod m : values()) {
            if (StringUtils.equalsIgnoreCase(m.getValue(), method)) {
                return m;
            }
        }
        return null;
    }

    public String getValue() {
        return method;
    }
}