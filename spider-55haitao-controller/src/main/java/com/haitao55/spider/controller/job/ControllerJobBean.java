package com.haitao55.spider.controller.job;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap.KeySetView;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import com.haitao55.spider.common.dao.ItemDAO;
import com.haitao55.spider.common.dao.StatisticsDao;
import com.haitao55.spider.common.dao.TaskDAO;
import com.haitao55.spider.common.dao.UrlDAO;
import com.haitao55.spider.common.dos.StatisticsDO;
import com.haitao55.spider.common.dos.TaskDO;
import com.haitao55.spider.common.dos.UrlDO;
import com.haitao55.spider.common.entity.TaskStatus;
import com.haitao55.spider.common.entity.TaskType;
import com.haitao55.spider.common.entity.UrlsStatus;
import com.haitao55.spider.common.entity.UrlsType;
import com.haitao55.spider.common.service.MonitorService;
import com.haitao55.spider.common.thrift.TaskModel;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.controller.cache.HotTasksCache;
import com.haitao55.spider.controller.cache.StatisticsCache;
import com.haitao55.spider.controller.cache.TaskRatioCriteria;
import com.haitao55.spider.controller.cache.TaskRatioCriteriaCache;
import com.haitao55.spider.controller.utils.MonitorConstants;

/**
 * 
 * 功能：这是一个全局功能类；类中定义的方法将作为定时启动的任务来执行
 * 
 * @author Arthur.Liu
 * @time 2016年7月29日 下午4:29:17
 * @version 1.0
 */
public class ControllerJobBean {
	private static final Logger logger = LoggerFactory.getLogger("system");

	private TaskDAO taskDAO;

	private UrlDAO urlDAO;

	private ItemDAO itemDAO;

	@Autowired
	private StatisticsDao statisticsDao;

	private MonitorService monitorService;

	public TaskDAO getTaskDAO() {
		return taskDAO;
	}

	public void setTaskDAO(TaskDAO taskDAO) {
		this.taskDAO = taskDAO;
	}

	public UrlDAO getUrlDAO() {
		return urlDAO;
	}

	public void setUrlDAO(UrlDAO urlDAO) {
		this.urlDAO = urlDAO;
	}

	public ItemDAO getItemDAO() {
		return itemDAO;
	}

	public void setItemDAO(ItemDAO itemDAO) {
		this.itemDAO = itemDAO;
	}

	public MonitorService getMonitorService() {
		return monitorService;
	}

	public void setMonitorService(MonitorService monitorService) {
		this.monitorService = monitorService;
	}

	/**
	 * 定时任务:清除"控制'抓取速率'的"缓存
	 */
	public void cleanTaskRatioCache() {
		logger.info("clean-task-ratio-cache start....");

		TaskRatioCriteriaCache cache = TaskRatioCriteriaCache.getInstance();
		for (Entry<Long, TaskRatioCriteria> entry : cache.entrySet()) {
			TaskRatioCriteria rc = entry.getValue();
			rc.cleanCurrentValue();
		}

		logger.info("clean-task-ratio-cache successfully!");
	}

	/**
	 * 定时任务:加载"热门"任务的任务定义数据进内存,包括"运行中"状态和"丢弃中"状态的任务
	 */
	public void reloadHotTasks() {
		logger.info("load-hot-tasks start....");

		List<TaskDO> activeTaskList = this.taskDAO.queryByStatus(TaskStatus.ACTIVE.getValue());
		List<TaskDO> discardingTaskList = this.taskDAO.queryByStatus(TaskStatus.DISCARDING.getValue());

		Map<Long, TaskModel> newMap = new HashMap<Long, TaskModel>();

		for (TaskDO activeTaskDO : activeTaskList) {
			TaskModel taskModel = this.convertTaskDO2TaskModel(activeTaskDO);
			newMap.put(taskModel.getId(), taskModel);
		}

		for (TaskDO discardingTaskDO : discardingTaskList) {
			TaskModel taskModel = this.convertTaskDO2TaskModel(discardingTaskDO);
			newMap.put(taskModel.getId(), taskModel);
		}

		// 温和的替换；如果整体暴力的替换,将会出现特定时刻HotTasksCache内容为空的情况
		KeySetView<Long, TaskModel> oldHotTaskIds = HotTasksCache.getInstance().keySet();
		for (Long oldHotTaskId : oldHotTaskIds) {
			if (!newMap.containsKey(oldHotTaskId)) {
				// HotTasksCache is a ConcurrentHashMap,so it is OK here
				HotTasksCache.getInstance().remove(oldHotTaskId);
			}
		}
		HotTasksCache.getInstance().putAll(newMap);

		reloadTaskRatioCriteriaCache();

		logger.info("load-hot-tasks successfully!");
		this.monitorService.incField(MonitorConstants.MONITOR_FIELD_HOT_TASKS_RELOAD_TIME);
	}

	private TaskModel convertTaskDO2TaskModel(TaskDO taskDO) {
		TaskModel taskModel = new TaskModel();
		taskModel.setId(taskDO.getId());
		taskModel.setName(taskDO.getName());
		// 这样转来转去不是故意找麻烦,也是一个检验数据有效性的过程
		taskModel.setStatus(TaskStatus.codeOf(taskDO.getStatus()).getValue());
		taskModel.setConfig(taskDO.getConfig());
		taskModel.setSiteRegion(taskDO.getSiteRegion());
		taskModel.setLimit(taskDO.getRatio());
		taskModel.setWeight(taskDO.getWeight());
		taskModel.setProxyRegionId(taskDO.getProxyRegionId());

		return taskModel;
	}

	/**
	 * 定时任务:如果当前时间已经进入某个"自动"类型任务的时间窗口,则启动该任务(H->A)
	 */
	@SuppressWarnings("serial")
	public void startupAutomaticTask() {
		logger.info("startup-automatic-task start....");

		List<TaskDO> automaticTaskDOList = this.taskDAO.queryByType(TaskType.AUTOMATIC.getValue());
		for (TaskDO taskDO : automaticTaskDOList) {
			if (!TaskStatus.INIT.getValue().equals(taskDO.getStatus())
					&& !TaskStatus.HANGUP.getValue().equals(taskDO.getStatus())
					&& !TaskStatus.FINISH.getValue().equals(taskDO.getStatus())) {
				continue;
			}
			// init状态 添加initurl 到mongo 为INIT状态
			if (TaskStatus.INIT.getValue().equals(taskDO.getStatus())) {
				List<UrlDO> urlDOList = new ArrayList<UrlDO>() {
					{
						add(new UrlDO() {
							{
								setId(SpiderStringUtil.md5Encode(taskDO.getInitUrl()));
								setValue(taskDO.getInitUrl());// 这一句很关键
								setType(UrlsType.LINK.getValue());
								setStatus(UrlsStatus.INIT.getValue());
								setCreateTime(System.currentTimeMillis());
							}
						});
					}
				};
				urlDAO.insertUrls(taskDO.getId(), urlDOList);
			}
			long winStart = taskDO.getWinStart();
			long winEnd = taskDO.getWinEnd();
			long current = System.currentTimeMillis();

			if (winStart < current && current < winEnd) {// 当前时间已经进入任务的执行时间窗口
				this.startupAutomaticTaskAsynchronously(String.valueOf(taskDO.getId()),
						TaskStatus.codeOf(taskDO.getStatus()));
			}else if(winEnd < current){
				long period = taskDO.getPeriod();
				while (winEnd < current) {// 如果Controller进程停止太久,整个时间窗口将会落后于当前时间很远一段距离
					winStart += period;
					winEnd += period;
					sleep(50);//sleep and wait for other threads
				}

				taskDO.setWinStart(winStart);
				taskDO.setWinEnd(winEnd);
//				if (TaskStatus.HANGUP.getValue().equals(taskDO.getStatus()) || TaskStatus.FINISH.getValue().equals(taskDO.getStatus())){
//					taskDO.setStatus(TaskStatus.ACTIVE.getValue());
//				}
				//只修改时间偏移量
				this.taskDAO.update(taskDO);
				
				this.startupAutomaticTaskAsynchronously(String.valueOf(taskDO.getId()),
						TaskStatus.codeOf(taskDO.getStatus()));
			}
			
		}

		logger.info("startup-automatic-task start successfully!");
	}

	/**
	 * <p>
	 * 定时任务:挂起已经执行完时间窗口的自动任务,并将时间窗口右移一个(或多个)时间周期(A->H);
	 * </p>
	 * <p>
	 * 挂起任务时,保证将任务的时间窗口右边界右移到当前时间的右侧；
	 * </p>
	 */
	public void hangupAutomaticTask() {
		logger.info("hangup-automatic-task....");

		List<TaskDO> automaticTaskDOList = this.taskDAO.queryByType(TaskType.AUTOMATIC.getValue());
		for (TaskDO taskDO : automaticTaskDOList) {
			if (!TaskStatus.ACTIVE.getValue().equals(taskDO.getStatus())
					&& !TaskStatus.FINISH.getValue().equals(taskDO.getStatus())) {// 只有运行中的任务,才可以被"挂起"
				continue;
			}

			long winStart = taskDO.getWinStart();
			long winEnd = taskDO.getWinEnd();
			long period = taskDO.getPeriod();
			long current = System.currentTimeMillis();

			if (winEnd < current) {
				while (winEnd < current) {// 如果Controller进程停止太久,整个时间窗口将会落后于当前时间很远一段距离
					winStart += period;
					winEnd += period;
					sleep(50);//sleep and wait for other threads
				}

				taskDO.setWinStart(winStart);
				taskDO.setWinEnd(winEnd);
				if (TaskStatus.ACTIVE.getValue().equals(taskDO.getStatus())){
					taskDO.setStatus(TaskStatus.HANGUP.getValue());
				}

				this.taskDAO.update(taskDO);
			}
		}

		logger.info("hangup-automatic-task successfully!");
	}

	private void startupAutomaticTaskAsynchronously(String taskId, TaskStatus srcTaskStatus) {
		ExecutorService executorService = Executors.newSingleThreadExecutor();
		executorService.submit(new Runnable() {
			@Override
			public void run() {
				// 1.修改任务状态为"启动中"
				ControllerJobBean.this.updateTaskStatus(taskId, TaskStatus.STARTING.getValue());

				// 2.修改urls状态为"重生"
				if (TaskStatus.INIT.equals(srcTaskStatus) || TaskStatus.FINISH.equals(srcTaskStatus)) {
					ControllerJobBean.this.urlDAO.updateStatus(Long.valueOf(taskId), UrlsStatus.RELIVE.getValue());
					// 统计表插入任务记录 task_id,start_time,total_count
					int totalCount = urlDAO.queryUrlsCountByTaskId(taskId);
					statisticsDao.insert(new StatisticsDO() {
						{
							setTaskId(Long.parseLong(taskId));
							setStartTime(System.currentTimeMillis());
							setTotalCount(totalCount);
							setFailedCount(0);
							setHandleCount(0);
							setOfflineCount(0);
							setSuccessCount(0);
						}
					});
				}

				// 3.修改任务状态为"运行中"
				ControllerJobBean.this.updateTaskStatus(taskId, TaskStatus.ACTIVE.getValue());
			}
		});

		executorService.shutdown();
	}

	private void updateTaskStatus(String taskId, String newStatus) {
		@SuppressWarnings("serial")
		Map<String, String> params = new HashMap<String, String>() {
			{
				put(TaskDAO.UPDATE_STATUS_PARAMS_TASKID, taskId);
				put(TaskDAO.UPDATE_STATUS_PARAMS_NEWSTATUS, newStatus);
			}
		};

		this.taskDAO.updateStatus(params);
	}

	/**
	 * 定时刷新速率控制容器
	 */
	private void reloadTaskRatioCriteriaCache() {
		logger.info("reloadTaskRatioCriteriaCache task statring ... ");
		HotTasksCache hotTasksCache = HotTasksCache.getInstance();
		TaskRatioCriteriaCache ratioCache = TaskRatioCriteriaCache.getInstance();

		for (Entry<Long, TaskRatioCriteria> entry : ratioCache.entrySet()) {
			Long taskId = entry.getKey();
			if (!hotTasksCache.contains(taskId)) {
				ratioCache.remove(taskId);
			}
		}

		for (Entry<Long, TaskModel> entry : hotTasksCache.entrySet()) {
			Long taskId = entry.getKey();
			TaskRatioCriteria ratio = ratioCache.get(taskId);

			if (ratio == null) {
				ratio = new TaskRatioCriteria(taskId, entry.getValue().getWeight(), entry.getValue().getLimit());
				ratioCache.put(taskId, ratio);
			} else {
				ratio.setWeight(entry.getValue().getWeight());
				ratio.setLimit(entry.getValue().getLimit());
			}
		}
		logger.info("reloadTaskRatioCriteriaCache successfully!");
	}

	/**
	 * 手动任务 丢弃中的任务,检测mongo中是否对应任务url数据,如果mongo中已删除,则将任务状态置为void
	 */
	public void updateManualDiscardingToVoid() {
		logger.info("updateManualDiscardingToVoid task statring ... ");
		List<TaskDO> discardingTaskList = this.taskDAO.queryByStatus(TaskStatus.DISCARDING.getValue());
		List<UrlDO> urls = null;
		Map<String, String> params = new HashMap<String, String>();
		for (TaskDO taskDO : discardingTaskList) {
			urls = this.urlDAO.queryUrlsByTaskId(taskDO);
			if (null == urls || urls.size() == 0) {
				params.put(TaskDAO.UPDATE_STATUS_PARAMS_TASKID, String.valueOf(taskDO.getId()));
				params.put(TaskDAO.UPDATE_STATUS_PARAMS_NEWSTATUS, TaskStatus.VOID.getValue());
				taskDAO.updateStatus(params);
			}
		}
		logger.info("updateManualDiscardingToVoid successfully!");
	}

	/**
	 * 修改统计表中 对应 count数据
	 */
	public void updateStatisticsFigure() {
		logger.info("updateStatisticsFigure task statring ... ");
		StatisticsCache cache = StatisticsCache.getInstance();
		for (Entry<Long, StatisticsDO> entry : cache.entrySet()) {
			Long key = entry.getKey();
			StatisticsDO value = entry.getValue();
			value.setTaskId(key);

			if (null == value.getFailedCount() && null == value.getSuccessCount() && null == value.getHandleCount()
					&& null == value.getOfflineCount()) {
				continue;
			}
			if ((null != value.getFailedCount() && 0 == value.getFailedCount().intValue())
					&& (null != value.getSuccessCount() && 0 == value.getSuccessCount().intValue())
					&& (null != value.getHandleCount() && 0 == value.getHandleCount().intValue())
					&& (null != value.getOfflineCount() && 0 == value.getOfflineCount().intValue())) {
				continue;
			}
			this.statisticsDao.update(value);

			// 写完之后 对应数值清零
			value.setSuccessCount(0);
			value.setFailedCount(0);
			value.setHandleCount(0);
			value.setOfflineCount(0);
		}
		logger.info("updateStatisticsFigure successfully!");
	}
	
  private void sleep(long time) {
    try {
      Thread.sleep(time);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

}