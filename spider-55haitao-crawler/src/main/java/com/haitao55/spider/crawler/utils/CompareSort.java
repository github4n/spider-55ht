package com.haitao55.spider.crawler.utils;


import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.haitao55.spider.common.gson.bean.LSelectionList;

/**
 * 比较  排序
 * @author denghuan
 *
 * @param <E>
 */
public class CompareSort<E> {
    public void sort(List<E> list, final String method, final String sort) {
        Collections.sort(list, new Comparator<Object>() {
        	@SuppressWarnings("unchecked")
			public int compare(Object a, Object b) {
                int ret = 0;
                try {
                    Method m1 = ((E) a).getClass().getMethod(method);
                    Method m2 = ((E) b).getClass().getMethod(method);
                    if (sort != null && "desc".equals(sort))// 倒序
                        ret = m2.invoke(((E) b)).toString()
                                .compareTo(m1.invoke(((E) a)).toString());
                    else
                        // 正序
                        ret = m1.invoke(((E) a)).toString()
                                .compareTo(m2.invoke(((E) b)).toString());
                } catch (Exception e) {
                	e.printStackTrace();
                }
                return ret;
            }
        });
    }
    
    public void sort(List<LSelectionList> list){
    	Collections.sort(list,new Comparator<LSelectionList>(){  
 			@Override
 			public int compare(LSelectionList o1, LSelectionList o2) {
 				return new BigDecimal(o1.getSale_price()).compareTo(new BigDecimal(o2.getSale_price()));
 			}  
    	});  
    }
}
