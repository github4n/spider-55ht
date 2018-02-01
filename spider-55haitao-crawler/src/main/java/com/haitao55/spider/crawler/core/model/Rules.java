package com.haitao55.spider.crawler.core.model;

import java.util.ArrayList;

/**
 * 
 * 功能：用于保存页面的解析规则，一个Rules实例对应一个任务的规则配置文档
 * 
 * @author Arthur.Liu
 * @time 2016年5月31日 下午4:14:15
 * @version 1.0
 */
public class Rules extends ArrayList<Rule> {

    private static final long serialVersionUID = 1L;

    public void addRule(Rule rule) {
        add(rule);
    }
}