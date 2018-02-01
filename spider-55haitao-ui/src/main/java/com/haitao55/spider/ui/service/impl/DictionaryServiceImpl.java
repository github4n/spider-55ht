package com.haitao55.spider.ui.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.haitao55.spider.common.dao.DictionaryDAO;
import com.haitao55.spider.common.dos.DictionaryDO;
import com.haitao55.spider.common.service.impl.BaseService;
import com.haitao55.spider.ui.common.util.ConvertPageInstance;
import com.haitao55.spider.ui.service.DictionaryService;
import com.haitao55.spider.ui.view.DictionaryView;

@Service("dictionaryService")
public class DictionaryServiceImpl extends BaseService<DictionaryDO> implements DictionaryService {
	@Autowired
	private DictionaryDAO dictionaryDAO;

	@Override
	public List<DictionaryView> getDictionaries(DictionaryView dictionaryView, int page, int pageSize) {
		PageHelper.startPage(page, pageSize);
		List<DictionaryDO> list=dictionaryDAO.getDictionaries();
		Page<DictionaryDO> p=(Page<DictionaryDO>)list;
		Page<DictionaryView> pv=new Page<DictionaryView>();
		ConvertPageInstance.convert(p,pv);
		if (list != null) {
			for (DictionaryDO dictionaryDO : list) {
				DictionaryView view = this.convertDO2View(dictionaryDO);
				pv.add(view);
			}
		}
		return pv;
	}

	private DictionaryView convertDO2View(DictionaryDO dictionaryDO) {
		String[] split = dictionaryDO.getValue().split("=");
		String key=split[0];
		String value=split[1];
		DictionaryView dictionaryView=new DictionaryView(dictionaryDO.getId(), dictionaryDO.getType(), key,value);
		dictionaryView.setName(dictionaryDO.getName());
		return dictionaryView;
	}
	
	private DictionaryDO convertView2DO(DictionaryView dictionaryView) {
		String key=dictionaryView.getKey()==null?"":dictionaryView.getKey();
		String value=dictionaryView.getValue()==null?"":dictionaryView.getValue();
		DictionaryDO dictionaryDO=new DictionaryDO(dictionaryView.getId(), dictionaryView.getType() );
		dictionaryDO.setName(dictionaryView.getName());
		if(StringUtils.isNotBlank(key)&&StringUtils.isNotBlank(value)){
			dictionaryDO.setValue(key.concat("=").concat(value));
		}
		if(StringUtils.isNotBlank(dictionaryView.getTypeParam())){
			dictionaryDO.setTypeParam(dictionaryView.getTypeParam());
		}
		if(StringUtils.isNotBlank(dictionaryView.getNameParam())){
			dictionaryDO.setNameParam(dictionaryView.getNameParam());
		}
		return dictionaryDO;
	}
	@Override
	public void insertDictionary(DictionaryView dictionaryView) {
		DictionaryDO dictionaryDO = convertView2DO(dictionaryView);
		this.save(dictionaryDO);
	}

	@Override
	public DictionaryView selectDictionaryByMapper(DictionaryView dictionaryView) {
		DictionaryDO dictionaryDO = convertView2DO(dictionaryView);
		DictionaryDO selectByKey = this.selectByKey(dictionaryDO);
		DictionaryView view = convertDO2View(selectByKey);
		return view;
	}

	@Override
	public void updateDictionary(DictionaryView dictionaryView) {
		DictionaryDO dictionaryDO = convertView2DO(dictionaryView);
		dictionaryDAO.updateDictionary(dictionaryDO);
	}

	@Override
	public void deleteDictionary(DictionaryView dictionaryView) {
		DictionaryDO dictionaryDO = convertView2DO(dictionaryView);
		dictionaryDAO.deleteDictionary(dictionaryDO);
	}

	@Override
	public List<DictionaryView> getDictionaryDetails(DictionaryView dictionaryView, Integer page, Integer pageSize) {
		DictionaryDO dictionaryDO = convertView2DO(dictionaryView);
		PageHelper.startPage(page, pageSize);
		List<DictionaryDO> list=dictionaryDAO.getDictionaryDetails(dictionaryDO);
		Page<DictionaryDO> p=(Page<DictionaryDO>)list;
		Page<DictionaryView> pv=new Page<DictionaryView>();
		ConvertPageInstance.convert(p,pv);
		if (list != null) {
			for (DictionaryDO dictionary : list) {
				DictionaryView view = this.convertDO2View(dictionary);
				pv.add(view);
			}
		}
		return pv;
	}

	@Override
	public void updateDictionaryDetail(DictionaryView dictionaryView) {
		DictionaryDO dictionaryDO = convertView2DO(dictionaryView);
		this.updateNotNull(dictionaryDO);
	}

	@Override
	public void doDeleteDictionaryDetail(DictionaryView dictionaryView) {
		DictionaryDO dictionaryDO = convertView2DO(dictionaryView);
		this.delete(dictionaryDO);
	}

	@Override
	public List<DictionaryView> getDictionaryDetailsNoPage(DictionaryView dictionaryView) {
		List<DictionaryView> dictionaryViewList=new ArrayList<DictionaryView>();
		DictionaryDO dictionary = convertView2DO(dictionaryView);
		List<DictionaryDO> list=dictionaryDAO.getDictionaryDetails(dictionary);
		if (list != null) {
			for (DictionaryDO dictionaryDO : list) {
				DictionaryView view = this.convertDO2View(dictionaryDO);
				dictionaryViewList.add(view);
			}
		}
		return dictionaryViewList;
	}


}
