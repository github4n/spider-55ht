package com.haitao55.spider.crawler.core.callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.utils.Constants;

/**
 * 
 * 功能：字符窜加密
 * 
 * @author Arthur.Liu
 * @time 2016年8月8日 上午11:34:41
 * @version 1.0
 */
public class Md5 extends AbstractSelect {
	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);

	@Override
	public void invoke(Context context) throws Exception {
		String input = getInputString(context);
		String value = SpiderStringUtil.md5Encode(input);
		setOutput(context, value);
	}
}