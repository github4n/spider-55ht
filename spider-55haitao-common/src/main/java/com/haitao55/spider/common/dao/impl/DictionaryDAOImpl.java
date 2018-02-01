package com.haitao55.spider.common.dao.impl;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.haitao55.spider.common.dao.DictionaryDAO;
import com.haitao55.spider.common.dao.impl.mysql.DictionaryMapper;
import com.haitao55.spider.common.dos.DictionaryDO;

@Repository("dictionaryDAO")
public class DictionaryDAOImpl implements DictionaryDAO {
	@Autowired
	private DictionaryMapper dictionaryDAOMapper;

	@Override
	public List<DictionaryDO> getDictionaries() {
		return dictionaryDAOMapper.getDictionaries();
	}

	@Override
	public void updateDictionary(DictionaryDO dictionaryDO) {
		dictionaryDAOMapper.updateDictionary(dictionaryDO);
	}

	@Override
	public void deleteDictionary(DictionaryDO dictionaryDO) {
		dictionaryDAOMapper.deleteDictionary(dictionaryDO);
	}

	@Override
	public List<DictionaryDO> getDictionaryDetails(DictionaryDO dictionaryDO) {
		return dictionaryDAOMapper.getDictionaryDetails(dictionaryDO);
	}

}
