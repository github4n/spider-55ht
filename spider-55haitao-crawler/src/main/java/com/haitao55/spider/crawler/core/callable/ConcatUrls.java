package com.haitao55.spider.crawler.core.callable;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.haitao55.spider.crawler.core.callable.base.AbstractCallable;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Url;

public class ConcatUrls extends AbstractCallable {
	// 前缀
	private String prefix;

	// 后缀
	private String suffix;

	//contanis 包含规则，用于拼接ｕｒl之前判断
	private String contains;
	
	public String getPrefix() {
		return prefix;
	}

	public void setPrefix(String prefix) {
		this.prefix = prefix;
	}

	public String getSuffix() {
		return suffix;
	}

	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}
	
	
	public String getContains() {
		return contains;
	}

	public void setContains(String contains) {
		this.contains = contains;
	}
	
	@Override
	public void invoke(Context context) throws Exception {
		//一般批量处理urls
		Set<Url> urls = context.getUrl().getNewUrls();
		Set<Url> newUrls=new HashSet<Url>();
		for (Url url : urls) {
			if(!StringUtils.containsIgnoreCase(url.getValue(), contains)){
				String newurl=(prefix == null ? StringUtils.EMPTY : prefix) + url.getValue()
				+ (suffix == null ? StringUtils.EMPTY : suffix);
				url.setValue(newurl);
			}
			newUrls.add(url);
		}
		
		//替换urls
		context.getUrl().getNewUrls().clear();
		context.getUrl().getNewUrls().addAll(newUrls);
	}

}
