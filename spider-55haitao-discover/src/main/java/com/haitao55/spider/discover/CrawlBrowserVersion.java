package com.haitao55.spider.discover;

import com.gargoylesoftware.htmlunit.BrowserVersion;

/**
 * 
 * 功能：一个枚举，表示在抓取过程中使用的浏览器版本
 * 
 * @author Arthur.Liu
 * @time 2016年6月2日 下午2:15:11
 * @version 1.0
 */
public enum CrawlBrowserVersion {
    FIREFOX_24("ff24", BrowserVersion.FIREFOX_24), CHROME("chrome", BrowserVersion.CHROME), IE11("ie11",
            BrowserVersion.INTERNET_EXPLORER_11);

    public static CrawlBrowserVersion getCrawlBrowser(final String stringValue) {
        final CrawlBrowserVersion[] bs = CrawlBrowserVersion.values();
        for (final CrawlBrowserVersion b : bs) {
            if (b.stringValue.equalsIgnoreCase(stringValue)) {
                return b;
            }
        }
        return getDefault();
    }

    public static CrawlBrowserVersion getDefault() {
        return CrawlBrowserVersion.CHROME;
    }

    private final String stringValue;
    private final BrowserVersion browserVersion;

    private CrawlBrowserVersion(final String stringValue, final BrowserVersion browserVersion) {
        this.stringValue = stringValue;
        this.browserVersion = browserVersion;
    }

    public String getStringValue() {
        return stringValue;
    }

    public BrowserVersion getBrowserVersion() {
        return browserVersion;
    }
}