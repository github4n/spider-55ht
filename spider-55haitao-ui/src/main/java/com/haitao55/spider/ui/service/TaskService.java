package com.haitao55.spider.ui.service;

import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.haitao55.spider.common.dos.TaskDO;
import com.haitao55.spider.common.service.IService;
import com.haitao55.spider.ui.view.TaskView;

/**
 * 
 * 功能：任务管理service接口
 * 
 * @author Arthur.Liu
 * @time 2016年8月10日 上午10:59:56
 * @version 1.0
 */
public interface TaskService extends IService<TaskDO>{
	/**
	 * <p>
	 * 创建一个任务；
	 * </p>
	 * <p>
	 * 创建一个任务时,仅在关系型数据库中的task表中生成一条task记录,并不在noSql数据库中生成urls表和items表;
	 * </p>
	 * <p>
	 * 而是在"启动"任务时才通过新增一条url文档的方式(通过转移task表中init_url字段的值的方式)创建urls表；
	 * </p>
	 * <p>
	 * 创建商品数据集合(items collection)也是放在需要新增第一条文档时才进行
	 * </p>
	 * 
	 * @param taskBean
	 */
	public void createTask(TaskView taskView);

	/**
	 * 查询获取所有任务对象
	 * 
	 * @return
	 */
	public List<TaskView> queryAllTasks(int page,int pageSize);

	/**
	 * 根据任务ID查询任务对象
	 * 
	 * @param taskId
	 * @return
	 */
	public TaskView queryTaskById(String taskId);

	/**
	 * 修改一条任务
	 * 
	 * @param taskView
	 */
	public void editTask(TaskView taskView);

	/**
	 * <p>
	 * 启动任务；
	 * </p>
	 * <p>
	 * 只有在"初始化"状态和"已丢弃"状态下,才可以执行"启动"任务的操作；
	 * </p>
	 * <p>
	 * 启动任务时, 将关系型数据库中task记录的init_url填充到urls表中
	 * </p>
	 * 
	 * @param taskId
	 *            任务ID主键
	 */
	public void startupTask(String taskId);

	/**
	 * 暂停任务
	 * 
	 * @param taskId
	 *            任务ID主键
	 */
	public void pauseTask(String taskId);

	/**
	 * 恢复任务执行状态
	 * 
	 * @param taskId
	 *            任务ID主键
	 */
	public void recoverTask(String taskId);

	/**
	 * 重启任务
	 * 
	 * @param taskId
	 *            任务ID主键
	 */
	public void restartTask(String taskId);

	/**
	 * 丢弃任务
	 * 
	 * @param taskId
	 *            任务ID主键
	 */
	public void discardTask(String taskId);
	/**
	 * 删除任务
	 * @param taskView
	 */
	public void deleteTask(TaskView taskView);
	/**
	 * @Title: importSeeds 
	 * @Description: 导入种子
	 * @param taskId
	 * @param urlType
	 * @param url
	 */
	public Map<String, Object> importSeeds(String taskId, String urlType, String url, int grade, MultipartFile file);

	/**
	 * 一键休眠：暂停所有运行中和挂起的任务
	 */
	public void oneKeyDormancy();

	/**
	 * 一键唤醒：恢复一键休眠暂停的任务
	 */
	public void oneKeyRouse();
}