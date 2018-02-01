package com.haitao55.spider.crawler.core.callable;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jayway.jsonpath.JsonPath;

import com.haitao55.spider.crawler.core.callable.base.AbstractCallable;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.exception.ParseException;
import com.haitao55.spider.crawler.utils.Constants;

/**
 * 
 * 功能：根据json path解析内容
 * 
 * @author Arthur.Liu
 * @time 2016年8月8日 上午11:41:39
 * @version 1.0
 */
public class JSONPath extends AbstractCallable {
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_PARSER);

	private String path;

	public void invoke(Context context) throws Exception {
		String json = getInputString(context);
		if (StringUtils.isBlank(json)) {
			return;
		}
		Object value = null;
		try {
			value = JsonPath.read(json, getPath());
		} catch (ParseException pe) {
			logger.error("jsonpath got error,json:{};pe:{}", json, pe);
			throw pe;
		}
		setOutput(context, value);
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this);
	}
}