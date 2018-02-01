package com.haitao55.spider.common.dao;

import java.util.List;

import com.haitao55.spider.common.dos.DictionaryDO;

/**
 * 
* Title: 系统数据字典  dao
* Description:
* Company: 55海淘
* @author zhaoxl 
* @date 2016年9月1日 上午10:15:12
* @version 1.0
 */
public interface DictionaryDAO {
	/***
	 * 获取所有字典项
	 * @return
	 */
	public List<DictionaryDO> getDictionaries();
	/**
	 * 修改数据字典
	 * 根据type修改
	 * @param dictionaryDO
	 */
	public void updateDictionary(DictionaryDO dictionaryDO);
	/**
	 * 删除数据字典项 按照type  删除大项
	 * @param dictionaryView
	 */
	public void deleteDictionary(DictionaryDO dictionaryDO);
	/**
	 * 查询字典项下所有字典数据
	 * @param dictionaryDO
	 * @param page
	 * @param pageSize
	 * @return
	 */
	public List<DictionaryDO> getDictionaryDetails(DictionaryDO dictionaryDO);
	
}