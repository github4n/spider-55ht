package com.haitao55.spider.crawler.core.callable;

import com.haitao55.spider.crawler.core.callable.base.AbstractCallable;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.utils.AddressUtils;

/**
 * 
 * 功能：获取当前机器IP地址,在context中输出成指定名称的值
 * 
 * @author Arthur.Liu
 * @time 2016年8月5日 下午5:06:56
 * @version 1.0
 */
public class Ip extends AbstractCallable {

	@Override
	public void invoke(Context context) throws Exception {
		String value = AddressUtils.getIP();
		setOutput(context, value);
	}
}