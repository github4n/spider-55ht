package com.haitao55.spider.ui.action;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import com.github.pagehelper.PageInfo;
import com.haitao55.spider.common.entity.TaskStatus;
import com.haitao55.spider.common.entity.TaskType;
import com.haitao55.spider.common.entity.TaskUpdateOnly;
import com.haitao55.spider.ui.service.ProxyService;
import com.haitao55.spider.ui.service.StatisticsService;
import com.haitao55.spider.ui.service.TaskService;
import com.haitao55.spider.ui.view.ProxyView;
import com.haitao55.spider.ui.view.TaskView;
import com.haitao55.spider.ui.view.TaskView.PeriodView;
import com.haitao55.spider.ui.view.TaskView.TimeWindowView;

/**
 * 
 * 功能：任务管理的Action
 * 
 * @author Arthur.Liu
 * @time 2016年8月11日 下午4:05:20
 * @version 1.0
 */
@Controller
@RequestMapping("/task")
public class TaskAction extends BaseAction {

	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(TaskAction.class);

	@Autowired
	private TaskService taskService;

	@Autowired
	private StatisticsService statisticsService;

	@Autowired
	private ProxyService proxyService;

	@RequestMapping("getAllTasks")
	public String getAllTasks(HttpServletRequest request, HttpServletResponse response, Model model) {
		int page = getPage(request);
		int pageSize = getPageSize(request);
		List<TaskView> taskViewList = this.taskService.queryAllTasks(page, pageSize);

		model.addAttribute("pageInfo", new PageInfo<TaskView>(taskViewList));

		return "task/task-home";
	}

	@RequestMapping("gotoCreateTaskPage")
	public String gotoCreateTaskPage(HttpServletRequest request, HttpServletResponse response, Model model) {
		List<ProxyView> list = proxyService.queryDistinctRegions();
		model.addAttribute("proxyRegionList", list);
		return "task/task-create";
	}

	@RequestMapping("createTask")
	public String createTask(HttpServletRequest request, HttpServletResponse response, Model model, TaskView taskView) {
		taskView.setId(System.currentTimeMillis());
		taskView.setStatus(TaskStatus.INIT.toString());
		taskView.setCreateTime(System.currentTimeMillis());
		taskView.setUpdateTime(System.currentTimeMillis());
		taskView.setUpdateOnly(TaskUpdateOnly.NO.toString());
		this.clearTimeWindowFieldsOfManualTask(taskView);

		this.taskService.createTask(taskView);

		return "redirect:getAllTasks.action";
	}

	private void clearTimeWindowFieldsOfManualTask(TaskView taskView) {
		if (TaskType.MANUAL.getValue().equals(taskView.getType())) {// 手动任务
			taskView.setPeriod(0l);
			taskView.setWinStart(0l);
			taskView.setWinEnd(0l);
		}
	}

	@RequestMapping("gotoEditTaskPage")
	public String gotoEditTaskPage(HttpServletRequest request, HttpServletResponse response, Model model,
			TaskView taskView) {
		String taskId = request.getParameter("taskId");

		if (StringUtils.isBlank(taskId)) {
			return "login";
		}

		TaskView view = this.taskService.queryTaskById(taskId);
		if (TaskType.AUTOMATIC.getValue().equals(view.getType())) {// 只有任务类型是"自动类型"时才设置有效时间窗口
			this.fillTimeWindow(view);
		}
		List<ProxyView> list = proxyService.queryDistinctRegions();
		model.addAttribute("proxyRegionList", list);

		model.addAttribute("taskView", view);

		return "task/task-edit";
	}

	@RequestMapping("editTask")
	public String editTask(HttpServletRequest request, HttpServletResponse response, Model model, TaskView taskView) {
		this.clearTimeWindowFieldsOfManualTask(taskView);
		this.taskService.editTask(taskView);
		return "redirect:getAllTasks.action";
	}

	@RequestMapping("deleteTask")
	public String deleteTask(HttpServletRequest request, HttpServletResponse response, Model model, TaskView taskView) {
		this.taskService.deleteTask(taskView);
		return "redirect:getAllTasks.action";
	}

	@RequestMapping("viewTask")
	public String viewTask(Model model, String id) {
		TaskView taskView = this.taskService.queryTaskById(id);
		model.addAttribute("taskView", taskView);
		if (TaskType.AUTOMATIC.getValue().equals(taskView.getType())) {// 只有任务类型是"自动类型"时才设置有效时间窗口
			this.fillTimeWindow(taskView);
		}
		List<ProxyView> list = proxyService.queryDistinctRegions();
		model.addAttribute("proxyRegionList", list);
		return "task/task-view";
	}

	@RequestMapping("findTaskById")
	@ResponseBody
	public TaskView findTaskById(String taskId) {
		return taskService.queryTaskById(taskId);
	}

	private void fillTimeWindow(TaskView taskView) {
		// 1.任务周期
		int periodHour = (int) ((taskView.getPeriod() / 1000) / 60) / 60;
		int periodMinute = (int) ((taskView.getPeriod() / 1000) / 60) % 60;
		taskView.setPeriodView(new PeriodView());
		taskView.getPeriodView().setHour(periodHour);
		taskView.getPeriodView().setMinute(periodMinute);

		// 2.时间窗口开始时间
		Calendar winStartCalendar = Calendar.getInstance();
		winStartCalendar.setTimeInMillis(taskView.getWinStart());
		int winStartYear = winStartCalendar.get(Calendar.YEAR);
		int winStartMonth = winStartCalendar.get(Calendar.MONTH) + 1;
		int winStartDay = winStartCalendar.get(Calendar.DAY_OF_MONTH);
		int winStartHour = winStartCalendar.get(Calendar.HOUR_OF_DAY);
		int winStartMinute = winStartCalendar.get(Calendar.MINUTE);
		taskView.setStartTimeWindowView(new TimeWindowView());
		taskView.getStartTimeWindowView().setYear(winStartYear);
		taskView.getStartTimeWindowView().setMonth(winStartMonth);
		taskView.getStartTimeWindowView().setDay(winStartDay);
		taskView.getStartTimeWindowView().setHour(winStartHour);
		taskView.getStartTimeWindowView().setMinute(winStartMinute);

		// 3.时间窗口结束时间
		Calendar winEndCalendar = Calendar.getInstance();
		winEndCalendar.setTimeInMillis(taskView.getWinEnd());
		int winEndYear = winEndCalendar.get(Calendar.YEAR);
		int winEndMonth = winEndCalendar.get(Calendar.MONTH) + 1;
		int winEndDay = winEndCalendar.get(Calendar.DAY_OF_MONTH);
		int winEndHour = winEndCalendar.get(Calendar.HOUR_OF_DAY);
		int winEndMinute = winEndCalendar.get(Calendar.MINUTE);
		taskView.setEndTimeWindowView(new TimeWindowView());
		taskView.getEndTimeWindowView().setYear(winEndYear);
		taskView.getEndTimeWindowView().setMonth(winEndMonth);
		taskView.getEndTimeWindowView().setDay(winEndDay);
		taskView.getEndTimeWindowView().setHour(winEndHour);
		taskView.getEndTimeWindowView().setHour(winEndHour);
		taskView.getEndTimeWindowView().setMinute(winEndMinute);
	}

	@RequestMapping("startupTask")
	public String startupTask(HttpServletRequest request, HttpServletResponse response, Model model,
			TaskView taskView) {
		String taskId = request.getParameter("taskId");
		this.taskService.startupTask(taskId);
		this.statisticsService.createStatistics(taskId);
		return "redirect:getAllTasks.action";
	}

	@RequestMapping("pauseTask")
	public String pauseTask(HttpServletRequest request, HttpServletResponse response, Model model, TaskView taskView) {
		String taskId = request.getParameter("taskId");
		this.taskService.pauseTask(taskId);

		return "redirect:getAllTasks.action";
	}

	@RequestMapping("recoverTask")
	public String recoverTask(HttpServletRequest request, HttpServletResponse response, Model model,
			TaskView taskView) {
		String taskId = request.getParameter("taskId");
		this.taskService.recoverTask(taskId);

		return "redirect:getAllTasks.action";
	}

	@RequestMapping("restartTask")
	public String restartTask(HttpServletRequest request, HttpServletResponse response, Model model,
			TaskView taskView) {
		String taskId = request.getParameter("taskId");
		this.taskService.restartTask(taskId);
		this.statisticsService.createStatistics(taskId);
		return "redirect:getAllTasks.action";
	}

	@RequestMapping("discardTask")
	public String discardTask(HttpServletRequest request, HttpServletResponse response, Model model,
			TaskView taskView) {
		String taskId = request.getParameter("taskId");
		this.taskService.discardTask(taskId);

		return "redirect:getAllTasks.action";
	}

	@RequestMapping("gotoImportSeeds")
	public String gotoImportSeeds(Model model, String taskId) {
		TaskView taskView = this.taskService.queryTaskById(taskId);
		model.addAttribute("taskView", taskView);
		Map<String, Object> map = new HashMap<>();
		map.put("urlType", "LINK");
		map.put("grade", "0");
		map.put("mode", "0");
		map.put("errorUrls", "NULL");
		map.put("errorCount", "NULL");
		map.put("successCount", "NULL");
		model.addAttribute("map", map);
		return "task/task-import";
	}

	@RequestMapping("importSeeds")
	public String importSeeds(String taskId, String urlType, String urls, String grade,
			@RequestParam(value = "file") MultipartFile file, String mode, Model model) {
		TaskView taskView = this.taskService.queryTaskById(taskId);
		model.addAttribute("taskView", taskView);
		Map<String, Object> map = this.taskService.importSeeds(taskId, urlType, urls, Integer.parseInt(grade), file);
		map.put("urlType", urlType);
		map.put("grade", grade);
		map.put("mode", mode);
		model.addAttribute("map", map);
		return "task/task-import";
	}
	
	@RequestMapping("runWait")
	public String runWait(){
		this.taskService.oneKeyDormancy();
		return "redirect:getAllTasks.action";
	}
	
	@RequestMapping("rouse")
	public String rouse(){
		this.taskService.oneKeyRouse();
		return "redirect:getAllTasks.action";
	}
}