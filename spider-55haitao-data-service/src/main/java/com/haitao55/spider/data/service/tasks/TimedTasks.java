package com.haitao55.spider.data.service.tasks;

import java.text.SimpleDateFormat;
import java.util.List;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import com.haitao55.spider.data.service.service.TaskService;
import com.haitao55.spider.data.service.utils.WebSiteTaskCache;
import com.haitao55.spider.data.service.view.TaskView;

/**
 * 
 * 功能：定时器任务类
 * 
 * @author denghuan
 * @time 2017年4月19日 下午9:39:44
 * @version 1.0
 */
public class TimedTasks {
	private static final Logger logger = LoggerFactory.getLogger(TimedTasks.class);

	private static final String TIME_FORMAT_STRING = "yyyy-MM-dd HH:mm:ss.SSS";

	@Autowired
	private TaskService taskService;

	/**
	 * 定时从mysql数据库中加载爬虫任务配置信息
	 */
	public void reloadTasks() {
		StringBuilder logString = new StringBuilder();
		logString.append("WebSitTimedTasks.reloadTasks() start::");
		logString.append((new SimpleDateFormat(TIME_FORMAT_STRING)).format(System.currentTimeMillis()));
		logString.append(System.getProperty("line.separator"));

		List<TaskView> taskViewList = this.taskService.queryAllTasks(1, 2000);

		if (CollectionUtils.isEmpty(taskViewList)) {
			logger.warn("data-service TimedTasks.reloadTask() got none tasks!");
			return;
		} else {
			logger.info("data-service TimedTasks.reloadTask() got {} tasks!", taskViewList.size());
		}

		WebSiteTaskCache taskCache = WebSiteTaskCache.getInstance();
		for (TaskView taskView : taskViewList) {
			if(!taskCache.containsKey(taskView.getDomain())){
				taskCache.put(taskView.getDomain(), taskView.getId().toString());
			}
		}

		logString.append("data-service TimedTasks.reloadTasks() end:");
		logString.append((new SimpleDateFormat(TIME_FORMAT_STRING)).format(System.currentTimeMillis()));
		logString.append(System.getProperty("line.separator"));
		logString.append("data-service TimedTasks.size():").append(taskCache.size());
		logger.info(logString.toString());
	}

}