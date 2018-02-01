package com.haitao55.spider.crawler.core.callable.base;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.core.model.UrlStatus;
import com.haitao55.spider.crawler.core.model.UrlType;

/**
 * 
 * 功能：根据css选择器进行页面urls选择的抽象类
 * 
 * @author Arthur.Liu
 * @time 2016年9月6日 下午3:39:23
 * @version 1.0
 */
public abstract class AbstractSelectUrls extends AbstractSelect {
	/** 用来做替换的正则表达式 */
	private String regex;
	/** 用来做替换的替换字符串 */
	private String replacement;

	protected List<String> reformStrings(List<String> strings) {
		List<String> result = new ArrayList<String>();

		if (CollectionUtils.isNotEmpty(strings) && StringUtils.isNotBlank(regex)) {
			for (String string : strings) {
				String reformedString = StringUtils.replacePattern(string, regex, replacement);
				result.add(reformedString);
			}
		}

		return result;
	}
	
	protected Set<Url> buildNewUrls(List<String> newUrlValues, Context context, String type,int grade) {
		Set<Url> result = new HashSet<Url>();

		if (newUrlValues == null || newUrlValues.isEmpty()) {
			return result;
		}

		for (String urlValue : newUrlValues) {
			if(StringUtils.isBlank(urlValue))
				continue;
			Url url = new Url();
			url.setId(SpiderStringUtil.md5Encode(urlValue));
			url.setValue(urlValue);
			url.setParentUrl(context.getCurrentUrl().toString());
			url.setUrlType(UrlType.codeOf(type));
			url.setUrlStatus(UrlStatus.NEWCOME);
			url.setTaskId(context.getUrl().getTaskId());
			url.setLastCrawledTime(0);
			url.setLastCrawledIP("");
			url.setLatelyFailedCount(0);
			url.setLatelyErrorCode("");
			url.setCreateTime(System.currentTimeMillis());
			url.setGrade(grade);
			result.add(url);
		}

		return result;
	}

	public String getRegex() {
		return regex;
	}

	public void setRegex(String regex) {
		this.regex = regex;
	}

	public String getReplacement() {
		return replacement;
	}

	public void setReplacement(String replacement) {
		this.replacement = replacement;
	}
}