package com.test.utils;

import java.util.HashSet;
import java.util.Set;

/** 
 * @Description: 
 * @author: zhoushuo
 * @date: 2016年11月16日 下午5:30:23  
 */

//枚举类型实现单利模式，即便是反射机制也无法破解，线程安全
public enum Singleton {
	INSTANCE;
	private Set<String> set = null;
	private Singleton(){
		set = new HashSet<>();
	}
	
	public Set<String> getSet(){
		return set;
	}
}
