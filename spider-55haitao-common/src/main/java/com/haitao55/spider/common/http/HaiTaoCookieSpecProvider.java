package com.haitao55.spider.common.http;

import org.apache.http.cookie.CookieSpec;
import org.apache.http.cookie.CookieSpecProvider;
import org.apache.http.protocol.HttpContext;

public class HaiTaoCookieSpecProvider implements CookieSpecProvider{

	@Override
	public CookieSpec create(HttpContext context) {
		return new HtCookieSpec();
	}

}
