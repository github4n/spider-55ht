package com.haitao55.spider.crawler.service;

import com.haitao55.spider.crawler.core.model.OutputObject;

/**
 * 
 * 功能：结果数据输出的Service接口；如果需要增加一种输出方式,只需要增加这个接口的一个实现类即可；
 * 
 * @author Arthur.Liu
 * @time 2016年8月5日 下午6:33:41
 * @version 1.0
 */
public interface OutputService {

	/**
	 * 输出结果数据
	 * 
	 * @param oo
	 *            待输出的结果数据的封装对象
	 */
	public void write(OutputObject oo) throws Exception;
}