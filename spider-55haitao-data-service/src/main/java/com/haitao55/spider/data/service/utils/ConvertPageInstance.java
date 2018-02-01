package com.haitao55.spider.data.service.utils;

import com.github.pagehelper.Page;

public class ConvertPageInstance {

	public static  <S, T> void  convert(Page<S> p, Page<T> pv) {
		pv.setCount(p.isCount());
		pv.setCountSignal(p.getCountSignal());
		pv.setEndRow(p.getEndRow());
		pv.setOrderBy(p.getOrderBy());
		pv.setOrderByOnly(p.isOrderByOnly());
		pv.setPageNum(p.getPageNum());
		pv.setPages(p.getPages());
		pv.setPageSize(p.getPageSize());
		pv.setPageSizeZero(p.getPageSizeZero());
		pv.setReasonable(p.getReasonable());
		pv.setStartRow(p.getStartRow());
		pv.setTotal(p.getTotal());
	}
}
