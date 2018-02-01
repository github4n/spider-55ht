package com.haitao55.spider.common.utils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.haitao55.spider.common.gson.bean.LSelectionList;
import com.haitao55.spider.common.gson.bean.Selection;

/**
 * 用于处理爬虫封装 json util Title: Description: Company: 55海淘
 * 
 * @author zhaoxl
 * @date 2017年3月24日 上午11:34:14
 * @version 1.0
 */
public class CrawlerJSONResultUtils {
	/**
	 * 获取 skuId 根据styleId与selections列表的value集合，经过md5编码得到skuId
	 * 
	 * @param lSelectionList
	 * @return
	 */
	public static String buildSkuid(LSelectionList lSelectionList) {
		List<String> list = new ArrayList<String>();

		String styleId = lSelectionList.getStyle_id();
		list.add(styleId);

		List<Selection> selections = lSelectionList.getSelections();
		if (CollectionUtils.isNotEmpty(selections)) {
			for (Selection selection : selections) {
				String selectValue = selection.getSelect_value();
				list.add(selectValue);
			}
		}

		String[] strArr = list.toArray(new String[list.size()]);
		String skuId = buildSkuid(strArr);

		return skuId;
	}

	/**
	 * 根据属性值数组，生成skuId；这里的生成skuId的策略逻辑，一旦确定，以后便不能再改变了，商品数据中会一直使用
	 * 
	 * @param args
	 * @return
	 */
	public static String buildSkuid(String... args) {
		Set<String> treeSet = new TreeSet<String>();// 这里使用TreeSet是为了保证对String对象进行排序

		if (ArrayUtils.isNotEmpty(args)) {
			for (String arg : args) {
				treeSet.add(arg);
			}
		}

		StringBuilder sb = new StringBuilder();

		if (CollectionUtils.isNotEmpty(treeSet)) {
			for (String s : treeSet) {
				sb.append(StringUtils.trim(s));
			}
		}

		return SpiderStringUtil.md5Encode(sb.toString());
	}
}