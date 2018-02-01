package com.haitao55.spider.common.dao.impl.mongo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.data.mongodb.core.DocumentCallbackHandler;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import com.haitao55.spider.common.dao.UrlDAO;
import com.haitao55.spider.common.dos.TaskDO;
import com.haitao55.spider.common.dos.UrlDO;
import com.haitao55.spider.common.entity.TaskUpdateOnly;
import com.haitao55.spider.common.entity.UrlsType;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBObject;
import com.mongodb.InsertOptions;
import com.mongodb.MongoException;

/**
 * 
 * 功能：用来操作url的DAO接口实现类
 * 
 * @author Arthur.Liu
 * @time 2016年7月27日 上午11:11:19
 * @version 1.0
 */
public class UrlDAOImpl extends BaseMongoDAO implements UrlDAO {
	private static final Logger logger = LoggerFactory.getLogger("system");

	private static final String COLLECTION_NAME_PREFIX = "urls";
	private static final String DATABASE_NAME_PREFIX = "spider_55haitao";

	private static final String EMPTY_STRING = "";
	private static final String ZERO = "";

	private static final String DATABASE_FIELD_NAME_ID = "_id";
	private static final String DATABASE_FIELD_NAME_VALUE = "value";
	private static final String DATABASE_FIELD_NAME_PARENT_URL = "parentUrl";
	private static final String DATABASE_FIELD_NAME_TYPE = "type";
	private static final String DATABASE_FIELD_NAME_GRADE = "grade";
	private static final String DATABASE_FIELD_NAME_STATUS = "status";
	private static final String DATABASE_FIELD_NAME_CREATE_TIME = "create_time";
	private static final String DATABASE_FIELD_NAME_LAST_CRAWLED_TIME = "last_crawled_time";
	private static final String DATABASE_FIELD_NAME_LAST_CRAWLED_IP = "last_crawled_ip";
	private static final String DATABASE_FIELD_NAME_LATELY_FAILED_COUNT = "lately_failed_count";
	private static final String DATABASE_FIELD_NAME_LATELY_ERROR_CODE = "lately_error_code";

	/**
	 * 根据taskId获取mongo db实例
	 */
	private DB getDBViaTaskId() {
		DB db = this.getMongoTemplate().getDb().getMongo().getDB(DATABASE_NAME_PREFIX);
		return db;
	}

	@Override
	public void insertUrls(Long taskId, List<UrlDO> urlDOList) {
		if (urlDOList == null || urlDOList.isEmpty()) {
			return;
		}

		List<DBObject> list = new ArrayList<DBObject>(urlDOList.size());
		for (UrlDO urlDO : urlDOList) {
			DBObject obj = this.convertUrlDO2DBObject(urlDO);
			list.add(obj);
		}

		this.getMongoTemplate().insert(list, this.buildCollectionName(taskId));
	}

	@Override
	public void insertUrlsWhileErrorContinue(Long taskId, List<UrlDO> urlDOList) {
		if (urlDOList == null || urlDOList.isEmpty()) {
			return;
		}

		List<DBObject> list = new ArrayList<DBObject>(urlDOList.size());
		for (UrlDO urlDO : urlDOList) {
			DBObject obj = this.convertUrlDO2DBObject(urlDO);
			list.add(obj);
		}

		InsertOptions io = new InsertOptions();
		io.continueOnError(true);
		this.getMongoTemplate().getDb().getCollection(this.buildCollectionName(taskId)).insert(list, io);

	}

	@Override
	public void updateUrls(Long taskId, List<UrlDO> urlDOList) {
		for (UrlDO urlDO : urlDOList) {
			Query query = (new Query()).addCriteria(Criteria.where(DATABASE_FIELD_NAME_ID).is(urlDO.getId()));
			Update update = this.buildUpdate(urlDO);

			// 很遗憾,没有批量更新接口；所以只能在这个for循环中一个一个的更新
			this.getMongoTemplate().updateFirst(query, update, this.buildCollectionName(taskId));
		}
	}

	@Override
	public void deleteUrls(Long taskId, List<String> urlIdList) {
		Query query = (new Query()).addCriteria(Criteria.where(DATABASE_FIELD_NAME_ID).in(urlIdList));
		this.getMongoTemplate().remove(query, this.buildCollectionName(taskId));
	}

	@Override
	public void updateStatus(Long taskId, String newStatus) {
		Query query = (new Query()).addCriteria(Criteria.where(DATABASE_FIELD_NAME_ID).exists(true));
		Update update = (new Update()).set(DATABASE_FIELD_NAME_STATUS, newStatus);

		this.getMongoTemplate().updateMulti(query, update, this.buildCollectionName(taskId));
	}

	@Override
	public List<UrlDO> queryUrlsWithoutUpdate(Long taskId, String status, int limit) {
		List<UrlDO> result = new ArrayList<UrlDO>();

		Query query = (new Query()).addCriteria(Criteria.where(DATABASE_FIELD_NAME_STATUS).is(status)).limit(limit);
		this.getMongoTemplate().executeQuery(query, this.buildCollectionName(taskId), new DocumentCallbackHandler() {
			@Override
			public void processDocument(DBObject dbObject) throws MongoException, DataAccessException {
				UrlDO urlDO = UrlDAOImpl.this.convertDBObject2UrlDO(dbObject);
				result.add(urlDO);
			}
		});

		return result;
	}

	@Override
	public List<UrlDO> queryUrlsByStatusWithUpdateStatus(Long taskId, String status, String newStatus, int limit) {
		List<UrlDO> result = new ArrayList<UrlDO>();

		// 查询出一批url
		Query query = (new Query()).addCriteria(Criteria.where(DATABASE_FIELD_NAME_STATUS).is(status)).limit(limit);
		this.getMongoTemplate().executeQuery(query, this.buildCollectionName(taskId), new DocumentCallbackHandler() {
			@Override
			public void processDocument(DBObject dbObject) throws MongoException, DataAccessException {
				UrlDO urlDO = UrlDAOImpl.this.convertDBObject2UrlDO(dbObject);
				result.add(urlDO);
			}
		});

		// 更改刚查询出来的一批url在数据库中的状态
		@SuppressWarnings("serial")
		Query q = (new Query())
				.addCriteria(Criteria.where(DATABASE_FIELD_NAME_ID).in(new ArrayList<String>(result.size()) {
					{
						for (UrlDO urlDO : result) {
							add(urlDO.getId());
						}
					}
				}));
		Update u = new Update().set(DATABASE_FIELD_NAME_STATUS, newStatus);
		this.getMongoTemplate().updateMulti(q, u, this.buildCollectionName(taskId));

		// 返回查询出的一批url
		return result;
	}

	@Override
	public void upsertUrls(Long taskId, List<UrlDO> urlDOList) {
		urlDOList = insertUrlsIfNotExist(taskId, urlDOList);
		for (UrlDO urlDO : urlDOList) {
			Query query = (new Query()).addCriteria(Criteria.where(DATABASE_FIELD_NAME_ID).is(urlDO.getId()));
			Update update = Update.fromDBObject(this.convertUrlDO2DBObject(urlDO), EMPTY_STRING);

			this.getMongoTemplate().upsert(query, update, this.buildCollectionName(taskId));
		}
	}

	private List<UrlDO> insertUrlsIfNotExist(Long taskId, List<UrlDO> urlDOList) {
		List<String> ids = new ArrayList<String>(urlDOList.size());
		for (UrlDO urlDO : urlDOList) {
			ids.add(urlDO.getId());
		}

		Query query = (new Query()).addCriteria(Criteria.where(DATABASE_FIELD_NAME_ID).in(ids));
		List<UrlDO> existDOs = new ArrayList<UrlDO>();
		this.getMongoTemplate().executeQuery(query, this.buildCollectionName(taskId), new DocumentCallbackHandler() {
			@Override
			public void processDocument(DBObject dbObject) throws MongoException, DataAccessException {
				existDOs.add(UrlDAOImpl.this.convertDBObject2UrlDO(dbObject));
			}
		});

		urlDOList.removeAll(existDOs);

		return urlDOList;
	}

	private DBObject convertUrlDO2DBObject(UrlDO urlDO) {
		DBObject obj = new BasicDBObject();

		obj.put(DATABASE_FIELD_NAME_ID, urlDO.getId());
		obj.put(DATABASE_FIELD_NAME_VALUE, urlDO.getValue());
		obj.put(DATABASE_FIELD_NAME_PARENT_URL, urlDO.getParentUrl());
		obj.put(DATABASE_FIELD_NAME_TYPE, urlDO.getType());
		obj.put(DATABASE_FIELD_NAME_GRADE, urlDO.getGrade());
		obj.put(DATABASE_FIELD_NAME_STATUS, urlDO.getStatus());
		obj.put(DATABASE_FIELD_NAME_CREATE_TIME, urlDO.getCreateTime());
		obj.put(DATABASE_FIELD_NAME_LAST_CRAWLED_TIME, urlDO.getLastCrawledTime());
		obj.put(DATABASE_FIELD_NAME_LAST_CRAWLED_IP, urlDO.getLastCrawledIp());
		obj.put(DATABASE_FIELD_NAME_LATELY_FAILED_COUNT, urlDO.getLatelyFailedCount());
		obj.put(DATABASE_FIELD_NAME_LATELY_ERROR_CODE, urlDO.getLatelyErrorCode());

		return obj;
	}

	private UrlDO convertDBObject2UrlDO(DBObject obj) {
		UrlDO urlDO = new UrlDO();
		try {

			urlDO.setId(Objects.toString(obj.get(DATABASE_FIELD_NAME_ID), EMPTY_STRING));
			urlDO.setValue(Objects.toString(obj.get(DATABASE_FIELD_NAME_VALUE), EMPTY_STRING));
			urlDO.setParentUrl(Objects.toString(obj.get(DATABASE_FIELD_NAME_PARENT_URL), EMPTY_STRING));
			urlDO.setType(Objects.toString(obj.get(DATABASE_FIELD_NAME_TYPE), EMPTY_STRING));
			urlDO.setGrade(Integer.parseInt(Objects.toString(obj.get(DATABASE_FIELD_NAME_GRADE))));
			urlDO.setStatus(Objects.toString(obj.get(DATABASE_FIELD_NAME_STATUS), EMPTY_STRING));
			urlDO.setCreateTime(Long.parseLong(Objects.toString(obj.get(DATABASE_FIELD_NAME_CREATE_TIME), ZERO)));
			urlDO.setLastCrawledTime(Long.parseLong(Objects.toString(obj.get(DATABASE_FIELD_NAME_CREATE_TIME), ZERO)));
			urlDO.setLastCrawledIp(Objects.toString(obj.get(DATABASE_FIELD_NAME_LAST_CRAWLED_IP), EMPTY_STRING));
			urlDO.setLatelyFailedCount(
					Integer.parseInt(Objects.toString(obj.get(DATABASE_FIELD_NAME_LATELY_FAILED_COUNT), ZERO)));
			urlDO.setLatelyErrorCode(Objects.toString(obj.get(DATABASE_FIELD_NAME_LATELY_ERROR_CODE), EMPTY_STRING));
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("url转换异常", e);
		}

		return urlDO;
	}

	private Update buildUpdate(UrlDO urlDO) {
		Update update = (new Update()).set(DATABASE_FIELD_NAME_VALUE, urlDO.getValue())
				.set(DATABASE_FIELD_NAME_PARENT_URL, urlDO.getParentUrl())
				.set(DATABASE_FIELD_NAME_TYPE, urlDO.getType()).set(DATABASE_FIELD_NAME_GRADE, urlDO.getGrade())
				.set(DATABASE_FIELD_NAME_STATUS, urlDO.getStatus())
				.set(DATABASE_FIELD_NAME_CREATE_TIME, urlDO.getCreateTime())
				.set(DATABASE_FIELD_NAME_LAST_CRAWLED_TIME, urlDO.getLastCrawledTime())
				.set(DATABASE_FIELD_NAME_LAST_CRAWLED_IP, urlDO.getLastCrawledIp())
				.set(DATABASE_FIELD_NAME_LATELY_FAILED_COUNT, urlDO.getLatelyFailedCount())
				.set(DATABASE_FIELD_NAME_LATELY_ERROR_CODE, urlDO.getLatelyErrorCode());

		return update;
	}

	/**
	 * 根据任务ID构造集合名称
	 * 
	 * @param taskId
	 *            任务ID
	 * @return 集合名称
	 */
	private String buildCollectionName(Long taskId) {
		String collectionName = (new StringBuilder(COLLECTION_NAME_PREFIX)).append(taskId).toString();
		return collectionName;
	}

	@Override
	public List<UrlDO> queryUrlsByTaskId(TaskDO taskDO) {
		List<UrlDO> result = new ArrayList<UrlDO>();
		Query query = new Query();
		this.getMongoTemplate().executeQuery(query, this.buildCollectionName(taskDO.getId()),
				new DocumentCallbackHandler() {
					@Override
					public void processDocument(DBObject dbObject) throws MongoException, DataAccessException {
						UrlDO urlDO = UrlDAOImpl.this.convertDBObject2UrlDO(dbObject);
						result.add(urlDO);
					}
				});

		return result;
	}

	@Override
	public List<UrlDO> queryUrlsByStatusWithUpdateStatus(Long taskId, String updateOnly, String status,
			String newStatus, int limit) {
		List<UrlDO> result = new ArrayList<UrlDO>();
		Query query = null;
		if (TaskUpdateOnly.YES.getValue().equals(updateOnly)) {
			// 查询出一批url
			query = (new Query()).addCriteria(Criteria.where(DATABASE_FIELD_NAME_STATUS).is(status))
					.addCriteria(Criteria.where(DATABASE_FIELD_NAME_TYPE).is(UrlsType.ITEM.getValue())).limit(limit);
		} else {
			// 查询出一批url
			query = (new Query()).addCriteria(Criteria.where(DATABASE_FIELD_NAME_STATUS).is(status)).limit(limit);
		}
		this.getMongoTemplate().executeQuery(query, this.buildCollectionName(taskId), new DocumentCallbackHandler() {
			@Override
			public void processDocument(DBObject dbObject) throws MongoException, DataAccessException {
				UrlDO urlDO = UrlDAOImpl.this.convertDBObject2UrlDO(dbObject);
				result.add(urlDO);
			}
		});

		// 更改刚查询出来的一批url在数据库中的状态
		@SuppressWarnings("serial")
		Query q = (new Query())
				.addCriteria(Criteria.where(DATABASE_FIELD_NAME_ID).in(new ArrayList<String>(result.size()) {
					{
						for (UrlDO urlDO : result) {
							add(urlDO.getId());
						}
					}
				}));
		Update u = new Update().set(DATABASE_FIELD_NAME_STATUS, newStatus);
		this.getMongoTemplate().updateMulti(q, u, this.buildCollectionName(taskId));

		// 返回查询出的一批url
		return result;
	}

	@Override
	public int queryUrlsCountByTaskId(String taskId) {
		Query query = new Query();
		long count = this.getMongoTemplate().count(query, this.buildCollectionName(Long.parseLong(taskId)));
		return new Long(count).intValue();
	}

	@Override
	public int queryItemUrlsCountByTaskId(String taskId) {
		Query query = new Query().addCriteria(Criteria.where(DATABASE_FIELD_NAME_TYPE).is(UrlsType.ITEM.getValue()));
		long count = this.getMongoTemplate().count(query, this.buildCollectionName(Long.parseLong(taskId)));
		return new Long(count).intValue();
	}

	@Override
	public UrlDO queryUrlByDocId(Long taskId, String docId) {
		DBObject q = new BasicDBObject();
		q.put(DATABASE_FIELD_NAME_ID, docId);

		DBObject dbObject = this.getDBViaTaskId().getCollection(this.buildCollectionName(taskId)).findOne(q);
		if (!Objects.isNull(dbObject)) {
			UrlDO urlDO = this.convertDBObject2UrlDO(dbObject);
			return urlDO;
		}
		return null;
	}
}