package com.haitao55.spider.crawler.core.callable;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.crawler.core.callable.base.Callable;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.exception.CrawlerException.CrawlerExceptionCode;
import com.haitao55.spider.crawler.exception.RequiredException;
import com.haitao55.spider.crawler.utils.Constants;

/**
 * 
 * 功能：判断context中字段是否存在，不存在则抛异常
 * 
 * @author Arthur.Liu
 * @time 2016年8月5日 上午10:15:28
 * @version 1.0
 */
public class Required implements Callable {
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_PARSER);

	private static final String FIELDS_SEPARATOR = ",";
	private Set<String> fields = new HashSet<String>();

	public Set<String> getFields() {
		return fields;
	}

	public void setFields(String fields) {
		String[] array = StringUtils.split(fields, FIELDS_SEPARATOR);
		if (ArrayUtils.isEmpty(array)) {
			return;
		}

		for (String field : array) {
			if (StringUtils.isBlank(field)) {
				continue;
			}
			this.fields.add(StringUtils.trim(field));
		}
	}

	@Override
	public void invoke(Context context) throws Exception {
		for (String field : fields) {
			Object value = context.get(field);
			if (value == null) {
				logger.error("Required field is null! url:{}, field:{}", context.getCurrentUrl(), field);
				throw new RequiredException(CrawlerExceptionCode.PARSE_ERROR_REQUIRED,
						"required filed '" + field + "' is null,url " + context.getCurrentUrl());
			}

			if (value instanceof String) {
				String str = (String) value;
				if (StringUtils.isBlank(str)) {
					logger.error("Required field is blank! url:{}, field:{}", context.getCurrentUrl(), field);
					throw new RequiredException(CrawlerExceptionCode.PARSE_ERROR_REQUIRED,
							"required filed '" + field + "' is blank ,url " + context.getCurrentUrl());
				}
			}
		}
	}

	@Override
	public void init() throws Exception {
		
	}

	@Override
	public void destroy() throws Exception {
		
	}
}