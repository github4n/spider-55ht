package com.haitao55.spider.common.dao;

import java.util.List;

import com.haitao55.spider.common.dos.TaskDO;
import com.haitao55.spider.common.dos.UrlDO;

/**
 * 
 * 功能：用来操作url的DAO接口
 * 
 * @author Arthur.Liu
 * @time 2016年7月27日 上午11:05:36
 * @version 1.0
 */
public interface UrlDAO {
	/**
	 * URL校验时需要取第三个“/”之前的域名转换成小写，
	 * 因为“http://”或者"https://"最多占据到索引7，所以设置从索引8的位置往后查找"/"
	 */
	public static final int URL_SEPARATE_INDEX = 8;
	/**
	 * 批量查询url,不会改变表中数据
	 * 
	 * @param taskId
	 *            任务ID
	 * @param status
	 *            url状态
	 * @param limit
	 *            查询数量限制
	 * @return
	 */
	public List<UrlDO> queryUrlsWithoutUpdate(Long taskId, String status, int limit);

	/**
	 * 根据状态批量查询urls,同时改变表中已查询出的urls的状态值
	 * 
	 * @param taskId
	 *            任务ID
	 * @param status
	 *            根据什么状态做查询
	 * @param newStatus
	 *            查询出来后更改成什么状态
	 * @param limit
	 *            查询数量上限
	 * @return
	 */
	public List<UrlDO> queryUrlsByStatusWithUpdateStatus(Long taskId, String status, String newStatus, int limit);

	/**
	 * 批量插入url
	 * 
	 * @param taskId
	 *            任务ID
	 * @param urlList
	 *            待插入url列表
	 */
	public void insertUrls(Long taskId, List<UrlDO> urlDOList);
	
	/**
	 * 批量插入url，遇到错误继续
	 * @param taskId
	 * @param urlDOList
	 */
	public void insertUrlsWhileErrorContinue(Long taskId, List<UrlDO> urlDOList);

	/**
	 * 批量更新url
	 * 
	 * @param taskId
	 *            任务ID
	 * @param urlList
	 *            待更新url列表
	 */
	public void updateUrls(Long taskId, List<UrlDO> urlDOList);

	/**
	 * 更新或新增urls
	 * 
	 * @param taskId
	 *            任务ID
	 * @param urlDOList
	 *            UrlDO列表
	 */
	public void upsertUrls(Long taskId, List<UrlDO> urlDOList);

	/**
	 * 批量删除url
	 * 
	 * @param taskId
	 *            任务ID
	 * @param urlList
	 *            待删除url列表
	 */
	public void deleteUrls(Long taskId, List<String> urlIdList);

	/**
	 * 改变整个collection中所有urls的状态值
	 * 
	 * @param taskId
	 *            任务ID
	 * @param newStatus
	 *            要改变成的新状态
	 */
	public void updateStatus(Long taskId, String newStatus);
	
	/**
	 * 根据taskId 查询urls记录
	 * @param taskDO
	 * @return
	 */
	public List<UrlDO> queryUrlsByTaskId(TaskDO taskDO);
	/**
	 * 根据taskId,updateOnly状态 查询urls记录
	 * @param taskId
	 * @param updateOnly
	 * @param value
	 * @param value2
	 * @param limit
	 * @return
	 */
	public List<UrlDO> queryUrlsByStatusWithUpdateStatus(Long taskId, String updateOnly, String status, String newStatus,
			int limit);
	/**
	 * 根据task_Id  获取对应url总数
	 * @param taskId
	 * @return
	 */
	public int queryUrlsCountByTaskId(String taskId);
	/**
	 * 根据task_Id 获取对应的item类型的url总数
	 * @param taskId
	 * @return
	 */
	public int queryItemUrlsCountByTaskId(String taskId);
	
	/**
	 * 根据docId 查找url
	 * @param taskId
	 * @param docId
	 * @return
	 */
	public UrlDO queryUrlByDocId(Long taskId,String docId);
}