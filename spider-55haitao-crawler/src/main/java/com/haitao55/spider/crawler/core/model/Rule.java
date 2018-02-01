package com.haitao55.spider.crawler.core.model;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;


import com.haitao55.spider.crawler.common.cache.Cache;
import com.haitao55.spider.crawler.common.cache.SimpleCache;
import com.haitao55.spider.crawler.core.callable.base.Callable;

/**
 * 
 * 功能：用于保存页面的解析规则，一个Rule实例对应一种url的匹配规则
 *
 * @author Arthur.Liu
 * @time Jul 21, 2015 10:46:10 AM
 * @version 1.0
 *
 */
public class Rule {

    private String regex;
    
    private int grade;

    private List<Callable> calls = new ArrayList<Callable>();

    // 缓存，默认1天后失效
    private static final Cache<String, Pattern> cache = new SimpleCache<String, Pattern>();
    private static final Lock lock = new ReentrantLock();

    public List<Callable> getCalls() {
        return calls;
    }

    public void setCalls(List<Callable> calls) {
        this.calls = calls;
    }

    // xmlParseService依赖此方法
    public void addCallable(Callable call) {
        this.calls.add(call);
    }

    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }
    public int getGrade() {
		return grade;
	}

	public void setGrade(int grade) {
		this.grade = grade;
	}

	public boolean matches(String url) {
        Pattern pattern = cache.get(regex);
        if (pattern == null) {
            try {
                lock.lock();
                pattern = cache.get(regex);
                if (pattern == null) {
                    pattern = Pattern.compile(regex);
                    cache.put(regex, pattern);
                }
            } finally {
                lock.unlock();
            }
        }
        return pattern.matcher(url).matches();
    }
	
	public boolean matches(int grade) {
        return (getGrade() == grade);
    }
	
	

}
