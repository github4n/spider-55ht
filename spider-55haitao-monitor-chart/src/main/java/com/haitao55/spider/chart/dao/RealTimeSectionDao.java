package com.haitao55.spider.chart.dao;

import java.util.List;
import java.util.Map;

import com.haitao55.spider.chart.entity.RealTimeSection;
import com.haitao55.spider.common.util.MyMapper;

/** 
 * @Description: 
 * @author: zhoushuo
 * @date: 2017年5月31日 下午4:45:57  
 */
public interface RealTimeSectionDao extends MyMapper<RealTimeSection>{
	List<RealTimeSection> selectByTime(Map<String, String> params);
}
