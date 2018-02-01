package com.haitao55.spider.common.dao.impl.mysql;

import java.util.List;

import com.haitao55.spider.common.dos.DictionaryDO;
import com.haitao55.spider.common.util.MyMapper;
/**
 * 
* Title: 系统字典项
* Description:
* Company: 55海淘
* @author zhaoxl 
* @date 2016年9月1日 上午10:18:22
* @version 1.0
 */
public interface DictionaryMapper extends MyMapper<DictionaryDO> {
	/***
	 * 获取所有字典项
	 * @return
	 */
	List<DictionaryDO> getDictionaries();
	/**
	 * 修改数据字典
	 * 根据type修改
	 * @param dictionaryDO
	 */
	void updateDictionary(DictionaryDO dictionaryDO);
	/**
	 * 删除数据字典项 按照type  删除大项
	 * @param dictionaryView
	 */
	void deleteDictionary(DictionaryDO dictionaryDO);
	/**
	 * 查询字典项下所有字典数据
	 * @param dictionaryDO
	 * @param page
	 * @param pageSize
	 * @return
	 */
	List<DictionaryDO> getDictionaryDetails(DictionaryDO dictionaryDO);

}
