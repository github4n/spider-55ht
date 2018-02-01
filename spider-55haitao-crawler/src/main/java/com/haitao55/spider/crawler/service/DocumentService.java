package com.haitao55.spider.crawler.service;

import java.util.List;

import com.haitao55.spider.crawler.core.model.Url;

/**
 * 
 * 功能：维护Urls的本地Service接口
 * 
 * @author Arthur.Liu
 * @time 2016年8月8日 下午4:19:01
 * @version 1.0
 */
public interface DocumentService {

	/**
	 * 获取一批urls
	 * 
	 * @param limit
	 * @return
	 */
	public List<Url> getDocuments(int limit);

	/**
	 * 根据Url实例状态,更新或删除一批urls
	 * 
	 * @param urls
	 *            待更新或删除的urls实例
	 * @return
	 */
	public void updelDocuments(List<Url> urls);

	/**
	 * 根据Url实例是否已经在系统中存在,新增或更新一批urls
	 * 
	 * @param urls
	 *            待新增或更新的urls实例
	 */
	public void upsertDocuments(List<Url> urls);
}