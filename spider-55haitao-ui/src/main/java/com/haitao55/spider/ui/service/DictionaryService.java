package com.haitao55.spider.ui.service;

import java.util.List;

import com.haitao55.spider.common.dos.DictionaryDO;
import com.haitao55.spider.common.service.IService;
import com.haitao55.spider.ui.view.DictionaryView;

public interface DictionaryService extends IService<DictionaryDO>{
	
	/**
	 * 获取所有数据字典项
	 * @param dictionaryView
	 * @param page
	 * @param pageSize
	 * @return
	 */
	List<DictionaryView> getDictionaries(DictionaryView dictionaryView, int page, int pageSize);
	/**
	 * 添加字典数据
	 * @param dictionaryView
	 */
	void insertDictionary(DictionaryView dictionaryView);
	/**
	 * 查询字典
	 * @param dictionaryView
	 * @return
	 */
	DictionaryView selectDictionaryByMapper(DictionaryView dictionaryView);
	/**
	 * 修改数据字典数据    
	 * 根据type修改
	 * @param dictionaryView
	 */
	void updateDictionary(DictionaryView dictionaryView);
	/**
	 * 删除数据字典项 按照type  删除大项
	 * @param dictionaryView
	 */
	void deleteDictionary(DictionaryView dictionaryView);
	/**
	 * 查询字典项下所有字典数据
	 * @param dictionaryView
	 * @param page
	 * @param pageSize
	 * @return
	 */
	List<DictionaryView> getDictionaryDetails(DictionaryView dictionaryView, Integer page, Integer pageSize);
	/**
	 * 修改字典项 字典数据
	 * @param dictionaryView
	 */
	void updateDictionaryDetail(DictionaryView dictionaryView);
	/**
	 * 删除字典项  字典数据
	 * @param dictionaryView
	 */
	void doDeleteDictionaryDetail(DictionaryView dictionaryView);
	/**
	 * 获取字典项数据,不分页
	 * @param dictionaryView
	 * @return
	 */
	List<DictionaryView> getDictionaryDetailsNoPage(DictionaryView dictionaryView);
	
}
