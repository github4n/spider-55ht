package com.haitao55.spider.common.http;

import org.apache.http.cookie.ClientCookie;
import org.apache.http.cookie.Cookie;
import org.apache.http.cookie.CookieOrigin;
import org.apache.http.cookie.MalformedCookieException;
import org.apache.http.cookie.SetCookie;
import org.apache.http.impl.cookie.BasicExpiresHandler;
import org.apache.http.impl.cookie.BrowserCompatSpec;

public class HtCookieSpec extends BrowserCompatSpec {

	public static final String[] DATE_PATTERNS = new String[] {
			"EEE, dd MMM yyyy HH:mm:ss zzz", "EEE, dd-MMM-yy HH:mm:ss zzz",
			"EEE MMM d HH:mm:ss yyyy", "EEE, dd-MMM-yyyy HH:mm:ss z",
			"EEE, dd-MMM-yyyy HH-mm-ss z", "EEE, dd MMM yy HH:mm:ss z",
			"EEE dd-MMM-yyyy HH:mm:ss z", "EEE dd MMM yyyy HH:mm:ss z",
			"EEE dd-MMM-yyyy HH-mm-ss z", "EEE dd-MMM-yy HH:mm:ss z",
			"EEE dd MMM yy HH:mm:ss z", "EEE,dd-MMM-yy HH:mm:ss z",
			"EEE,dd-MMM-yyyy HH:mm:ss z", "EEE, dd-MM-yyyy HH:mm:ss z",
			"E, dd-MMM-yyyy HH:mm:ss zzz", "EEEE, dd-MMM-yy HH:mm:ss zzz" };

	public HtCookieSpec() {
		super();
		registerAttribHandler(ClientCookie.EXPIRES_ATTR,
				new BasicExpiresHandler(DATE_PATTERNS) {
					@Override
					public void parse(SetCookie cookie, String value)
							throws MalformedCookieException {
						value = value.replaceAll("\"", "");
						super.parse(cookie, value);
					}
				});
	}

	@Override
	public void validate(Cookie cookie, CookieOrigin origin)
			throws MalformedCookieException {
		if (cookie == null) {
			throw new IllegalArgumentException("Cookie may not be null");
		}
		if (origin == null) {
			throw new IllegalArgumentException("Cookie origin may not be null");
		}
	}

}
