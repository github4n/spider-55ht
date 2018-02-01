package com.haitao55.spider.crawler.core.callable;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.crawler.core.callable.base.AbstractCallable;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.utils.Constants;

/**
 * 
 * 功能：使用分隔符,对输入数据做分割
 * 
 * @author Arthur.Liu
 * @time 2016年8月5日 下午12:09:26
 * @version 1.0
 */
@Deprecated
public class Split extends AbstractCallable {

	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);

	private static final String RULE_FIELDS_SEPARATOR = ";";

	private String separator = "#,#";// default @see MultiSelect.java
	private String names;

	@Override
	public void invoke(Context context) throws Exception {
		String input = getInputString(context);

		if (StringUtils.isBlank(input) || !StringUtils.contains(input, separator)) {
			logger.warn("error split input:input={}", input);
			return;
		}

		if (StringUtils.isBlank(names) || !StringUtils.contains(names, RULE_FIELDS_SEPARATOR)) {
			logger.warn("error split names:names={}", names);
			return;
		}

		String[] nameArray = StringUtils.splitByWholeSeparator(names, RULE_FIELDS_SEPARATOR);
		String[] valueArray = StringUtils.splitByWholeSeparator(input, separator);

		if (nameArray.length != valueArray.length) {
			logger.warn("error split values:names={},input={}", names, input);
			return;
		}

		for (int i = 0; i < nameArray.length; i++) {
			context.put(nameArray[i], valueArray[i]);
		}
	}

	public String getSeparator() {
		return separator;
	}

	public void setSeparator(String separator) {
		this.separator = separator;
	}

	public String getNames() {
		return names;
	}

	public void setNames(String names) {
		this.names = names;
	}
}