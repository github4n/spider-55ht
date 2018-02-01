package com.haitao55.spider.crawler.tasks;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.crawler.core.pipeline.Pipeline;
import com.haitao55.spider.crawler.core.pipeline.valve.Valve;
import com.haitao55.spider.crawler.utils.Constants;

/**
 * 
 * 功能：工作线程；根据机器硬件资源情况，可以启动适当数量的本类实例
 * 
 * @author Arthur.Liu
 * @time 2016年8月2日 下午1:57:22
 * @version 1.0
 */
public class Worker implements DaemonTask {
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_SYSTEM);

	private Pipeline pipeline;

	@Override
	public void run() {
		List<Valve> valves = this.pipeline.getValves();
		boolean running = true;
		while (running) {
			try {
				for (Valve valve : valves) {
					valve.invoke();
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			} finally {

			}
		}
	}

	public Pipeline getPipeline() {
		return pipeline;
	}

	public void setPipeline(Pipeline pipeline) {
		this.pipeline = pipeline;
	}
}