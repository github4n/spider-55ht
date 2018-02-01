package com.haitao55.spider.crawler.core.pipeline;

import java.util.List;

import com.haitao55.spider.crawler.core.pipeline.valve.Valve;

/**
 * 
 * 功能：Pipeline 接口定义
 * 
 * @author Arthur.Liu
 * @time 2016年6月1日 上午9:57:35
 * @version 1.0
 */
public interface Pipeline {

    public void addValve(Valve valve);

    public void removeValve(Valve valve);

    public void setValves(List<Valve> valves);

    public List<Valve> getValves();

    public Valve getFirstValve();
}