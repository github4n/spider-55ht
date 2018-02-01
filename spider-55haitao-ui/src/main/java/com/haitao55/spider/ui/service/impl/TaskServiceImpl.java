package com.haitao55.spider.ui.service.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.haitao55.spider.common.dao.StatisticsDao;
import com.haitao55.spider.common.dao.TaskDAO;
import com.haitao55.spider.common.dao.UrlDAO;
import com.haitao55.spider.common.dos.TaskDO;
import com.haitao55.spider.common.dos.UrlDO;
import com.haitao55.spider.common.entity.TaskStatus;
import com.haitao55.spider.common.entity.TaskType;
import com.haitao55.spider.common.entity.TaskUpdateOnly;
import com.haitao55.spider.common.entity.UrlsStatus;
import com.haitao55.spider.common.entity.UrlsType;
import com.haitao55.spider.common.service.impl.BaseService;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.ui.common.util.ConvertPageInstance;
import com.haitao55.spider.ui.service.TaskService;
import com.haitao55.spider.ui.view.TaskView;

import scala.collection.parallel.Tasks;

/**
 * 
 * 功能：任务管理service接口实现类
 * 
 * @author Arthur.Liu
 * @time 2016年8月10日 上午11:03:00
 * @version 1.0
 */
@Service("taskService")
public class TaskServiceImpl extends BaseService<TaskDO> implements TaskService {
	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(TaskServiceImpl.class);

	private TaskDAO taskDAO;
	private UrlDAO urlDAO;
	
	private static final int BATCH_SIZE = 1000;

	@Autowired
	private StatisticsDao statisticsDao;

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

	@Override
	public void createTask(TaskView taskView) {
		TaskDO taskDO = this.convertView2DO(taskView);
		this.taskDAO.insert(taskDO);
	}

	private TaskDO convertView2DO(TaskView taskView) {
		TaskDO taskDO = new TaskDO();
		taskDO.setId(taskView.getId());
		taskDO.setName(taskView.getName());
		taskDO.setDescription(taskView.getDescription());
		taskDO.setDomain(taskView.getDomain());
		taskDO.setInitUrl(taskView.getInitUrl());
		taskDO.setType(TaskType.codeOf(taskView.getType()).getValue());
		taskDO.setStatus(TaskStatus.codeOf(taskView.getStatus()).getValue());
		taskDO.setPeriod(taskView.getPeriod());
		taskDO.setWinStart(taskView.getWinStart());
		taskDO.setWinEnd(taskView.getWinEnd());
		taskDO.setCreateTime(taskView.getCreateTime());
		taskDO.setUpdateTime(taskView.getUpdateTime());
		taskDO.setMaster(taskView.getMaster());
		taskDO.setUpdateOnly(TaskUpdateOnly.codeOf(taskView.getUpdateOnly()).getValue());
		taskDO.setConfig(taskView.getConfig().trim());
		taskDO.setRatio(taskView.getRatio());
		taskDO.setWeight(taskView.getWeight());
		taskDO.setSiteRegion(taskView.getSiteRegion());
		taskDO.setProxyRegionId(taskView.getProxyRegionId());
		taskDO.setPretreatConfig(taskView.getPretreatConfig());
		
		if(TaskType.MANUAL.getValue().equals(taskDO.getType()) && TaskStatus.HANGUP.getValue().equals(taskDO.getStatus()))
			taskDO.setStatus(TaskStatus.PAUSE.getValue());
		
		return taskDO;
	}

	private TaskView convertDO2View(TaskDO taskDO) {
		TaskView taskView = new TaskView();
		taskView.setId(taskDO.getId());
		taskView.setName(taskDO.getName());
		taskView.setDescription(taskDO.getDescription());
		taskView.setDomain(taskDO.getDomain());
		taskView.setInitUrl(taskDO.getInitUrl());
		taskView.setType(taskDO.getType().toString());
		taskView.setStatus(taskDO.getStatus().toString());
		taskView.setPeriod(taskDO.getPeriod());
		taskView.setWinStart(taskDO.getWinStart());
		taskView.setWinEnd(taskDO.getWinEnd());
		taskView.setCreateTime(taskDO.getCreateTime());
		taskView.setUpdateTime(taskDO.getUpdateTime());
		taskView.setMaster(taskDO.getMaster());
		taskView.setUpdateOnly(taskDO.getUpdateOnly().toString());
		taskView.setConfig(taskDO.getConfig());
		taskView.setRatio(taskDO.getRatio());
		taskView.setWeight(taskDO.getWeight());
		taskView.setSiteRegion(taskDO.getSiteRegion());
		taskView.setProxyRegionId(taskDO.getProxyRegionId());
		taskView.setPretreatConfig(taskDO.getPretreatConfig());
		return taskView;
	}

	@Override
	public List<TaskView> queryAllTasks(int page, int pageSize) {
		PageHelper.startPage(page, pageSize);
		List<TaskDO> allTaskDOs = this.taskDAO.getAllTasks();
		Page<TaskDO> p = (Page<TaskDO>) allTaskDOs;
		Page<TaskView> pv = new Page<TaskView>();
		ConvertPageInstance.convert(p, pv);
		if (allTaskDOs != null) {
			for (TaskDO taskDO : allTaskDOs) {
				TaskView taskView = this.convertDO2View(taskDO);
				pv.add(taskView);
			}
		}

		return pv;
	}

	@Override
	public TaskView queryTaskById(String taskId) {
		TaskDO taskDO = this.taskDAO.getTaskById(taskId);
		TaskView taskView = this.convertDO2View(taskDO);

		return taskView;
	}

	@Override
	public void editTask(TaskView taskView) {
		TaskDO taskDO = this.convertView2DO(taskView);
		this.updateNotNull(taskDO);
	}

	@Override
	public void deleteTask(TaskView taskView) {
		TaskDO taskDO = new TaskDO();
		taskDO.setId(taskView.getId());
		this.delete(taskDO);
		this.statisticsDao.deleteByTaskId(taskView.getId().toString());
	}

	@Override
	@SuppressWarnings("serial")
	public void startupTask(String taskId) {
		TaskDO taskDO = this.taskDAO.getTaskById(taskId);
		List<UrlDO> urlList = new ArrayList<UrlDO>() {
			{
				add(new UrlDO() {
					{
						setId(SpiderStringUtil.md5Encode(taskDO.getInitUrl()));
						setValue(taskDO.getInitUrl());// 这一句很关键
						setGrade(0);// 初始Url指定为第0级别
						setType(UrlsType.LINK.getValue());
						setStatus(UrlsStatus.INIT.getValue());
						setCreateTime(System.currentTimeMillis());
					}
				});
			}
		};
		this.urlDAO.insertUrls(taskDO.getId(), urlList);

		// 2.改变关系型数据库中任务记录的状态字段值
		this.updateTaskStatusSynchronously(taskId, TaskStatus.STARTING.getValue());
		// 3.改变urls数据库中urls的状态字段值,然后改变关系型数据库中任务记录的状态字段值
		this.updateUrlsStatusAndTaskStatusAsynchronously(taskId, UrlsStatus.RELIVE, TaskStatus.ACTIVE);
	}

	@Override
	public void pauseTask(String taskId) {
		this.updateTaskStatusSynchronously(taskId, TaskStatus.PAUSE.getValue());
	}

	@Override
	public void recoverTask(String taskId) {
		this.updateTaskStatusSynchronously(taskId, TaskStatus.ACTIVE.getValue());
	}

	@Override
	public void restartTask(String taskId) {
		this.updateTaskStatusSynchronously(taskId, TaskStatus.STARTING.getValue());
		this.updateUrlsStatusAndTaskStatusAsynchronously(taskId, UrlsStatus.RELIVE, TaskStatus.ACTIVE);
	}

	@Override
	public void discardTask(String taskId) {
		this.updateTaskStatusSynchronously(taskId, TaskStatus.DISCARDING.getValue());
		this.updateUrlsStatusAndTaskStatusAsynchronously(taskId, UrlsStatus.RELIVE, null);
	}

	private void updateTaskStatusSynchronously(String taskId, String newStatus) {
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
	 * 改变urls数据库中urls的状态,完了之后改变task表中task记录的状态
	 * 
	 * @param taskId
	 *            任务ID主键
	 * @param urlsStatus
	 *            要改变成的urls的状态
	 * @param taskStatus
	 *            要改变成的task的状态
	 */
	private void updateUrlsStatusAndTaskStatusAsynchronously(String taskId, UrlsStatus urlsStatus,
			TaskStatus taskStatus) {
		ExecutorService executorService = Executors.newSingleThreadExecutor();
		executorService.submit(new Runnable() {
			@Override
			public void run() {
				TaskServiceImpl.this.urlDAO.updateStatus(Long.valueOf(taskId), urlsStatus.getValue());

				if (taskStatus != null) {// 对于传入taskStatus为null的情况,亦即在更新完urls的状态之后,不需要再更新task的状态
					TaskServiceImpl.this.updateTaskStatusSynchronously(taskId, taskStatus.getValue());
				}
			}
		});

		executorService.shutdown();
	}
	
	/**
	 * 导入种子
	 */
	@Override
	public Map<String,Object> importSeeds(String taskId, String urlType, String url, int grade, MultipartFile file){
		Map<String,Object> map = new HashMap<>();
		Set<String> errorUrls = new HashSet<String>();//存放不合法的URL，去除重复
		Set<String> successUrls = new HashSet<String>();//存放合法的URL，去除重复
		Set<String> batchUrls = new HashSet<String>();//批量插入url，暂定其size为1000时插入一次
		/**
		 * 赋初始值为“”，包括两种情况: 
		 * 1.文本输入没填 
		 * 2.上传文件 
		 * 注意:上传文件即便是有错误URL也不给返回到页面
		 */
		map.put("errorUrls", "");
		/**
		 * 第一种情况：文本输入的方式导入种子
		 */
		int count = 0;//记录批量导入次数
		if(url != null && !"".equals(url)){
			String[] urls = url.split("\r\n");//按行分割，得到URL的数组
			for(String s : urls){
				String tempUrl = pickUrls(successUrls, errorUrls, s);
				if(StringUtils.isNotBlank(tempUrl))
					batchUrls.add(tempUrl);
				if(batchUrls.size() == BATCH_SIZE) {
					this.urlDAO.insertUrlsWhileErrorContinue(Long.valueOf(taskId), initUrls(batchUrls, urlType, grade));
					count++;
					logger.info("第"+count+"次成功导入"+batchUrls.size()+"条数据");
					batchUrls.clear();
				}
			}
			map.put("errorUrls", concatUrls(errorUrls));
		}
		/**
		 * 第二种情况：文件导入的方式上传种子
		 */
		if(file != null && file.getSize()>0){
			File desFile = new File(UUID.randomUUID()+".txt");
			BufferedReader br = null;
			try {
				file.transferTo(desFile);
				br = new BufferedReader(new FileReader(desFile));
				String s = br.readLine();
				while(s != null){
					String tempUrl = pickUrls(successUrls, errorUrls, s);
					if(StringUtils.isNotBlank(tempUrl))
						batchUrls.add(tempUrl);
					if(batchUrls.size() == BATCH_SIZE) {
						this.urlDAO.insertUrlsWhileErrorContinue(Long.valueOf(taskId), initUrls(batchUrls, urlType, grade));
						count++;
						logger.info("第"+count+"次成功导入"+batchUrls.size()+"条数据");
						batchUrls.clear();
					}
					s = br.readLine();
				}
			} catch (IllegalStateException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if(br != null){
					try {
						br.close();
					} catch (IOException e) {
						throw new RuntimeException("BufferedReader关闭失败");
					}
					br = null;
				}
				if(desFile.exists()){
					desFile.delete();
				}
			}
		}
		this.urlDAO.insertUrlsWhileErrorContinue(Long.valueOf(taskId), initUrls(batchUrls, urlType, grade));
		logger.info("导入剩下的记录"+batchUrls.size()+"条.");
		logger.info("总共导入："+successUrls.size());
		map.put("errorCount", errorUrls.size());
		map.put("successCount", successUrls.size());
		return map;
	}
	
	/**
	 * URL合法性校验:
	 * 1.去掉不是以"http://"或者"https://"开头的URL
	 * 2.URL第三个“/”之前如果有大写，转换成小写
	 */
	@SuppressWarnings("static-access")
	private String transformUrl(String url){
		String prefix = "";//将第三个"/"之前的URL转换成小写,该变量用来存放需要转换成小写的URL
		String suffix = "";//存放第三个"/"之后的url
		int index = url.indexOf("/", urlDAO.URL_SEPARATE_INDEX);
		if(index == -1){//说明URL中没有找到第三个"/"，则全部转小写
			prefix = url;
		} else {
			prefix = url.substring(0, index);
			suffix = url.substring(index);//截取后缀
		}
		prefix = prefix.toLowerCase();//前缀转成小写
		return prefix+suffix;
	}
	
	/**
	 * 
	 * @Title: pickUrls 
	 * @Description: 将URL检验后进行分类，合法的URL进行格式化后放入到successUrls中，非法的URL按原来的方式放入errorUrls中，Set集合是为了去除重复元素
	 */
	private String pickUrls(Set<String> successUrls, Set<String> errorUrls, String oldUrl){
		if(StringUtils.isBlank(oldUrl)){
			return null;
		}
		oldUrl = oldUrl.trim();
		String newUrl = transformUrl(oldUrl);
		if(!newUrl.startsWith("http://") && !newUrl.startsWith("https://")){
			errorUrls.add(oldUrl);//URL不合法
			return null;
		} else {
			successUrls.add(newUrl);
			return newUrl;
		}
	}
	
	@SuppressWarnings("serial")
	private List<UrlDO> initUrls(Set<String> urls, String urlType, int grade){
		List<UrlDO> urlDOs = new ArrayList<>();
		for(String url : urls){
			urlDOs.add(new UrlDO(){{
				setId(SpiderStringUtil.md5Encode(url));
				setValue(url);
				setType(urlType);
				setGrade(grade);
				setStatus(UrlsStatus.INIT.getValue());
				setCreateTime(System.currentTimeMillis());
			}});
		}
		return urlDOs;
	}
	
	private String concatUrls(Set<String> urls){
		StringBuilder strBuilder = new StringBuilder("");
		for(String url : urls){
			strBuilder.append(url).append("\n");
		}
		return strBuilder.toString();
	}

	@Override
	public void oneKeyDormancy() {
		this.taskDAO.switchStatus(setParams(TaskStatus.ACTIVE.getValue(),TaskStatus.WAIT.getValue()));
		this.taskDAO.switchStatus(setParams(TaskStatus.HANGUP.getValue(), TaskStatus.MOTIONLESS.getValue()));
	}

	@Override
	public void oneKeyRouse() {
		this.taskDAO.switchStatus(setParams(TaskStatus.WAIT.getValue(), TaskStatus.ACTIVE.getValue()));
		this.taskDAO.switchStatus(setParams(TaskStatus.MOTIONLESS.getValue(), TaskStatus.HANGUP.getValue()));
	}
	
	private Map<String, String> setParams(String oldStatus, String newStatus){
		Map<String, String> params = new HashMap<>();
		params.put(TaskDAO.UPDATE_STATUS_PARAMS_OLDSTATUS, oldStatus);
		params.put(TaskDAO.UPDATE_STATUS_PARAMS_NEWSTATUS, newStatus);
		return params;
	}
}