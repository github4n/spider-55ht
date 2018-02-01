package com.haitao55.spider.ui.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.haitao55.spider.ui.service.InfluxdbService;

/**
 * 
 * 功能：监控日志收集的Action类
 * 
 * @author Arthur.Liu
 * @time 2016年11月29日 下午4:54:39
 * @version 1.0
 */
@Controller
@RequestMapping("/monitor-collector")
public class MonitorCollectorAction extends BaseAction {
	private static final Logger logger = LoggerFactory.getLogger(MonitorCollectorAction.class);

	@Autowired
	private InfluxdbService influxdbService;

	@RequestMapping("collect-monitor")
	public @ResponseBody void collectMonitorLog(HttpServletRequest request, HttpServletResponse response, Model model,
			@RequestParam("ip") String ip, @RequestParam("module") String module,
			@RequestParam("content") String content, @RequestParam("comefrom") String comeFrom) {

		try {
			logger.info("MonitorCollectorAction.collectMonitorLog() is called, ip:{}, module:{}", ip, module);
			this.influxdbService.writeInfluxdb(ip, module, content, comeFrom);
		} catch (Exception e) {
			logger.error("Error writing influxdb, ", e);
		}
	}
}