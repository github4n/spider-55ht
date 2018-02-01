package com.haitao55.spider.crawler.core.pipeline;

import java.util.LinkedList;
import java.util.List;

import com.haitao55.spider.crawler.core.pipeline.valve.Valve;

/**
 * 
 * 功能：Pipeline接口的标准实现
 * 
 * @author Arthur.Liu
 * @time 2016年6月1日 上午10:02:18
 * @version 1.0
 */
public class StandardPipeline implements Pipeline {

    private final LinkedList<Valve> valves = new LinkedList<Valve>();

    @Override
    public void addValve(Valve valve) {
        this.valves.addLast(valve);
    }

    @Override
    public void removeValve(Valve valve) {
        this.valves.remove(valve);
    }

    @Override
    public void setValves(List<Valve> valves) {
        this.valves.clear();
        this.valves.addAll(valves);
    }

    @Override
    public List<Valve> getValves() {
        return this.valves;
    }

    @Override
    public Valve getFirstValve() {
        return this.valves.getFirst();
    }
}