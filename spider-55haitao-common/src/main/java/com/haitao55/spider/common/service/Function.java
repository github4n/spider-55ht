package com.haitao55.spider.common.service;
/**
 * 
* Title:
* Description:
* Company: 55海淘
* @author zhaoxl 
* @date 2016年9月19日 下午8:22:49
* @version 1.0
 */
public interface Function<E, T> {

    public T execute(E e);

}
