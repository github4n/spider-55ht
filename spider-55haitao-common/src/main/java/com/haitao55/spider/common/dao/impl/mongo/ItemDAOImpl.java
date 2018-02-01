package com.haitao55.spider.common.dao.impl.mongo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.haitao55.spider.common.dao.ItemDAO;
import com.haitao55.spider.common.dos.ItemDO;
import com.haitao55.spider.common.utils.ItemDatabaseIndexCache;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

/**
 * 
 * 功能：用来操作item的DAO接口实现类
 * 
 * @author Arthur.Liu
 * @time 2016年7月27日 上午11:15:22
 * @version 1.0
 */
@Repository("itemDAO")
public class ItemDAOImpl extends BaseMongoDAO implements ItemDAO {
	private static final String DATABASE_NAME_PREFIX = "spider_items_";
	private static final String COLLECTION_NAME_PREFIX = "item";

	private static final String EMPTY_STRING = "";
	private static final String ZERO = "";
	private static final String DATABASE_FIELD_NAME_ID = "_id";
	private static final String DATABASE_FIELD_NAME_VALUE = "value";
	private static final String DATABASE_FIELD_NAME_DOCID = "docId";
	private static final String DATABASE_FIELD_NAME_CREATE_TIME = "createTime";
	private static final String DATABASE_FIELD_NAME_URL_MD5 = "urlMD5";
	private static final String DATABASE_FIELD_NAME_STATUS = "status";
	private static final String DATABASE_FIELD_NAME_URLS = "urls";
	private static final String DATABASE_FIELD_NAME_MD5_URLS = "md5Urls";
	
	private static final String DATABASE_FIELD_NAME_STATUS_VALUE_ONLINE = "online";

	@Override
	public void insertItems(Long taskId, List<ItemDO> itemDOList) {
		if (itemDOList == null || itemDOList.isEmpty()) {
			return;
		}

		List<DBObject> list = new ArrayList<DBObject>(itemDOList.size());
		for (ItemDO itemDO : itemDOList) {
			DBObject obj = this.convertItemDO2DBObject(itemDO);
			list.add(obj);
		}

		this.getDBViaTaskId(taskId).getCollection(this.buildCollectionName(taskId)).insert(list);
	}

	/**
	 * 根据taskId获取mongo db实例
	 */
	private DB getDBViaTaskId(Long taskId) {
		DB db = this.getMongoTemplate().getDb().getMongo().getDB(DATABASE_NAME_PREFIX + taskId);
		return db;
	}
	

	
	@Override
	public void updateItems(Long taskId, List<ItemDO> itemDOList) {
		for (ItemDO itemDO : itemDOList) {
			Query query = (new Query()).addCriteria(Criteria.where(DATABASE_FIELD_NAME_ID).is(itemDO.getId()));
			Update update = this.buildUpdate(itemDO);

			// 很遗憾,没有批量更新接口；所以只能在这个for循环中一个一个的更新
			this.getMongoTemplate().updateFirst(query, update, this.buildCollectionName(taskId));
		}
	}

	@Override
	public List<ItemDO> queryItems(Long taskId, int limit) {
		List<ItemDO> result = new ArrayList<ItemDO>();
		DBCursor dbCursor = this.getDBViaTaskId(taskId).getCollection(this.buildCollectionName(taskId)).find().limit(limit);
		while(dbCursor.hasNext()){
			DBObject rst = dbCursor.next();
			ItemDO itemDO = ItemDAOImpl.convertDBObject2ItemDO(rst);
			result.add(itemDO);
		}

		return result;
	}

	@Override
	public ItemDO queryLastItem(Long taskId, String docId) {
		List<ItemDO> result = new ArrayList<ItemDO>();
		DBObject q = new BasicDBObject();
		q.put(DATABASE_FIELD_NAME_DOCID, docId);
		DBObject s = new BasicDBObject();
		s.put(DATABASE_FIELD_NAME_CREATE_TIME, -1);
		
		DBCursor dbCursor = this.getDBViaTaskId(taskId).getCollection(this.buildCollectionName(taskId)).find(q).sort(s).limit(1);
		while(dbCursor.hasNext()){
			DBObject rst = dbCursor.next();
			ItemDO itemDO = ItemDAOImpl.convertDBObject2ItemDO(rst);
			result.add(itemDO);
		}
		
		if (CollectionUtils.isNotEmpty(result)) {
			return result.get(0);
		}
		return null;
	}

	@Override
	public ItemDO queryMd5UrlLastItem(Long taskId, String md5Url) {
		List<ItemDO> result = new ArrayList<ItemDO>();
		
		DBObject q = new BasicDBObject();
		q.put(DATABASE_FIELD_NAME_URL_MD5, md5Url);
		DBObject s = new BasicDBObject();
		s.put(DATABASE_FIELD_NAME_CREATE_TIME, -1);
		
		DBCursor dbCursor = this.getDBViaTaskId(taskId).getCollection(this.buildCollectionName(taskId)).find(q).sort(s).limit(1);
		while(dbCursor.hasNext()){
			DBObject rst = dbCursor.next();
			ItemDO itemDO = ItemDAOImpl.convertDBObject2ItemDO(rst);
			result.add(itemDO);
		}
		
		if (CollectionUtils.isNotEmpty(result)) {
			return result.get(0);
		}
		return null;
	}

	@Override
	public List<ItemDO> queryMd5UrlList(Long taskId, String md5Url,int count,int limit) {
		List<ItemDO> result = new ArrayList<ItemDO>();
		DBObject q = new BasicDBObject();
		q.put(DATABASE_FIELD_NAME_URL_MD5, md5Url);
		DBObject s = new BasicDBObject();
		s.put(DATABASE_FIELD_NAME_CREATE_TIME, -1);
		DBCursor dbCursor = this.getDBViaTaskId(taskId).getCollection(this.buildCollectionName(taskId)).find(q).skip(count).limit(limit).sort(s);
		while(dbCursor.hasNext()){
			DBObject rst = dbCursor.next();
			ItemDO itemDO = ItemDAOImpl.convertDBObject2ItemDO(rst);
			result.add(itemDO);
		}
		return result;
	}
	
	@Override
	public long count(Long taskId) {
		DB db = this.getDBViaTaskId(taskId);
		long count = db.getCollection(this.buildCollectionName(taskId)).count();
		return count;
	}
	
	@Override
	public long countItems(Long taskId) {
		DB db = this.getDBViaTaskId(taskId);
		long count = db.getCollection(this.buildCollectionName(taskId)).distinct(DATABASE_FIELD_NAME_DOCID).size();
		return count;
	}

	@Override
	public long countOnlineItems(Long taskId) {// FIXME::这个方法这里这样写有问题,这样是不可以的
		DBObject query = new BasicDBObject();
		query.put(DATABASE_FIELD_NAME_STATUS, DATABASE_FIELD_NAME_STATUS_VALUE_ONLINE);
		
		DB db = this.getDBViaTaskId(taskId);
		long count = db.getCollection(this.buildCollectionName(taskId)).distinct(DATABASE_FIELD_NAME_DOCID, query).size();
		return count;
	}

	public long countUrlMD5Items(Long taskId,String urlMD5){
		DBObject query = new BasicDBObject();
		query.put(DATABASE_FIELD_NAME_URL_MD5, urlMD5);
		
		DB db = this.getDBViaTaskId(taskId);
		return  db.getCollection(this.buildCollectionName(taskId)).find(query).count();
	}
	
	
	@Override
	public void upsertItems(Long taskId, List<ItemDO> itemDOList) {
		for (ItemDO itemDO : itemDOList) {
			Query query = (new Query()).addCriteria(Criteria.where(DATABASE_FIELD_NAME_ID).is(itemDO.getId()));
			Update update = Update.fromDBObject(this.convertItemDO2DBObject(itemDO), EMPTY_STRING);
			this.getMongoTemplate().upsert(query, update, this.buildCollectionName(taskId));
		}
	}

	private DBObject convertItemDO2DBObject(ItemDO itemDO) {
		DBObject dbObject = new BasicDBObject();

		// 不设值,让mongo自动帮忙生成"_id"字段值
		// dbObject.put(DATABASE_FIELD_NAME_ID, itemDO.getId());
		dbObject.put(DATABASE_FIELD_NAME_DOCID, itemDO.getDocId());
		dbObject.put(DATABASE_FIELD_NAME_URL_MD5, itemDO.getUrlMD5());
		dbObject.put(DATABASE_FIELD_NAME_CREATE_TIME, itemDO.getCreateTime());
		dbObject.put(DATABASE_FIELD_NAME_VALUE, itemDO.getValue());
		dbObject.put(DATABASE_FIELD_NAME_STATUS, itemDO.getStatus());
		dbObject.put(DATABASE_FIELD_NAME_URLS, itemDO.getUrls());
		dbObject.put(DATABASE_FIELD_NAME_MD5_URLS, itemDO.getMd5Urls());
		return dbObject;
	}

	public static ItemDO convertDBObject2ItemDO(DBObject dbObject) {
		ItemDO itemDO = new ItemDO();

		itemDO.setId(Objects.toString(dbObject.get(DATABASE_FIELD_NAME_ID), EMPTY_STRING));
		itemDO.setDocId(Objects.toString(dbObject.get(DATABASE_FIELD_NAME_DOCID), EMPTY_STRING));
		itemDO.setUrlMD5(Objects.toString(dbObject.get(DATABASE_FIELD_NAME_URL_MD5), EMPTY_STRING));
		itemDO.setCreateTime(Long.parseLong(Objects.toString(dbObject.get(DATABASE_FIELD_NAME_CREATE_TIME), ZERO)));
		itemDO.setValue(Objects.toString(dbObject.get(DATABASE_FIELD_NAME_VALUE), EMPTY_STRING));
		itemDO.setStatus(Objects.toString(dbObject.get(DATABASE_FIELD_NAME_STATUS), EMPTY_STRING));
		String urlStr = Objects.toString(dbObject.get(DATABASE_FIELD_NAME_URLS),EMPTY_STRING);
		String[] urls = handleUrls(urlStr);
		if(urls != null && urls.length > 0){
			itemDO.setUrls(urls);
		}
		String md5UrlsStr = Objects.toString(dbObject.get(DATABASE_FIELD_NAME_MD5_URLS),EMPTY_STRING);
		String[] md5Urls = handleUrls(md5UrlsStr);
		if(md5Urls != null && md5Urls.length > 0){
			itemDO.setMd5Urls(md5Urls);
		}
		
		return itemDO;
	}

	private static String[] handleUrls(String urls){
		if(StringUtils.isNotBlank(urls)){
			JSONArray jsonArray = JSONObject.parseArray(urls);
			String[] mongoUrls = new String[jsonArray.size()];
			for(int i = 0; i < jsonArray.size(); i++){
				String url = jsonArray.getString(i);
				if(StringUtils.isNotBlank(url)){
					mongoUrls[i] = url;
				}
			}
			return mongoUrls;
		}
		return null;
	}
	
	private Update buildUpdate(ItemDO itemDO) {
		Update update = (new Update()).set(DATABASE_FIELD_NAME_VALUE, itemDO.getValue())
				.set(DATABASE_FIELD_NAME_DOCID, itemDO.getDocId()).set(DATABASE_FIELD_NAME_URL_MD5, itemDO.getUrlMD5())
				.set(DATABASE_FIELD_NAME_CREATE_TIME, itemDO.getCreateTime());
		return update;
	}

	private String buildCollectionName(Long taskId) {
		String collectionName = (new StringBuilder(COLLECTION_NAME_PREFIX)).append(taskId).toString();
		return collectionName;
	}

	@Override
	public void insertItem(Long taskId, ItemDO itemDO) {
		if (Objects.isNull(itemDO)) {
			return;
		}
		
		DBObject obj = this.convertItemDO2DBObject(itemDO);
		
		DB db = this.getDBViaTaskId(taskId);
		String collectionName = this.buildCollectionName(taskId);
	
		db.getCollection(collectionName).insert(obj);
	}

	public void checkIndexIsExists(String collectionName,DB db){
		DBCollection coll = db.getCollection(collectionName);
		List<DBObject> indexs = coll.getIndexInfo();
		boolean docIdIsIndex = false;
		boolean urlMd5IsIndex = false;
		boolean md5UrlsIndex = false;
		for(int i = 0;i < indexs.size(); i++){
			String index = indexs.get(i).toString();
			if(StringUtils.containsIgnoreCase(index, DATABASE_FIELD_NAME_DOCID)){
				ItemDatabaseIndexCache.getInstance().put(collectionName, true);
				docIdIsIndex = true;
			}
			if(StringUtils.containsIgnoreCase(index, DATABASE_FIELD_NAME_URL_MD5)){
				ItemDatabaseIndexCache.getInstance().put(collectionName, true);
				urlMd5IsIndex  = true;
			}
			if(StringUtils.containsIgnoreCase(index, DATABASE_FIELD_NAME_MD5_URLS)){
				ItemDatabaseIndexCache.getInstance().put(collectionName, true);
				md5UrlsIndex  = true;
			}
		}
		
		if(!docIdIsIndex){
			BasicDBObject q = new BasicDBObject();
			q.put(DATABASE_FIELD_NAME_DOCID, 1);
			q.put("background", 1);
			coll.createIndex(q);
			ItemDatabaseIndexCache.getInstance().put(collectionName, true);
		}
		if(!urlMd5IsIndex){
			BasicDBObject q = new BasicDBObject();
			q.put(DATABASE_FIELD_NAME_URL_MD5, 1);
			q.put("background", 1);
			coll.createIndex(q);
			ItemDatabaseIndexCache.getInstance().put(collectionName, true);
		}
		if(!md5UrlsIndex){
			BasicDBObject q = new BasicDBObject();
			q.put(DATABASE_FIELD_NAME_MD5_URLS, 1);
			q.put("background", 1);
			coll.createIndex(q);
			ItemDatabaseIndexCache.getInstance().put(collectionName, true);
		}
	}
	
	
	@Override
	public boolean checkCollecionIsExists(Long taskId) {
		DB db = this.getDBViaTaskId(taskId);
		boolean  flag = db.collectionExists(this.buildCollectionName(taskId));
		return flag;
	}

	@Override
	public void insertItemEnsureIndex(Long taskId, ItemDO itemDO) {
		if (Objects.isNull(itemDO)) {
			return;
		}
		
		DBObject obj = this.convertItemDO2DBObject(itemDO);
		
		DB db = this.getDBViaTaskId(taskId);
		String collectionName = this.buildCollectionName(taskId);
		db.getCollection(collectionName).insert(obj);
		
		if(ItemDatabaseIndexCache.getInstance().get(collectionName) == null || !ItemDatabaseIndexCache.getInstance().get(collectionName)){
			checkIndexIsExists(collectionName,db);
		}
	}

	@Override
	public boolean queryMd5UrlIsexist(Long taskId, String md5Url) {
		DBObject q = new BasicDBObject();
		q.put(DATABASE_FIELD_NAME_URL_MD5, md5Url);
		
		DBObject dbObject = this.getDBViaTaskId(taskId).getCollection(this.buildCollectionName(taskId)).findOne(q);
		if(!Objects.isNull(dbObject)){
			return true;
		}
		return false;
	}
}