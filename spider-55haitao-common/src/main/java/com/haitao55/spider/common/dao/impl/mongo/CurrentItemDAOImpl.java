package com.haitao55.spider.common.dao.impl.mongo;

import java.util.List;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.haitao55.spider.common.dao.CurrentItemDAO;
import com.haitao55.spider.common.dos.ItemDO;
import com.haitao55.spider.common.utils.ItemDatabaseIndexCache;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;

/**
 * 
 * 功能：用来操作currenItem的DAO接口实现类
 * 
 * @author denghuan
 * @time 2017年2月16日 上午11:15:22
 * @version 1.0
 */
@Repository("currentItemDAO")
public class CurrentItemDAOImpl extends BaseMongoDAO implements CurrentItemDAO {
	private static final String DATABASE_NAME_PREFIX = "spider_items_";
	private static final String COLLECTION_NAME_PREFIX = "current_item";

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
	
	//private static final String DATABASE_FIELD_NAME_STATUS_VALUE_ONLINE = "online";


	/**
	 * 根据taskId获取mongo db实例
	 */
	private DB getDBViaTaskId(Long taskId) {
		DB db = this.getMongoTemplate().getDb().getMongo().getDB(DATABASE_NAME_PREFIX + taskId);
		return db;
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
			for(int i = 0; i < jsonArray.size();i++){
				String url = jsonArray.getString(i);
				if(StringUtils.isNotBlank(url)){
					mongoUrls[i] = url;
				}
			}
			return mongoUrls;
		}
		return null;
	}


	private  String buildCollectionName(Long taskId) {
		String collectionName = (new StringBuilder(COLLECTION_NAME_PREFIX)).append(taskId).toString();
		return collectionName;
	}

	@Override
	public void upsertCurrentItems(Long taskId, ItemDO itemDO) {
		if (Objects.isNull(itemDO)) {
			return;
		}
		
		DBObject obj = this.convertItemDO2DBObject(itemDO);
		
		DB db = this.getDBViaTaskId(taskId);
		
		String collectionName = this.buildCollectionName(taskId);
		/**
		 * 第一个参数是docId做为查询条件,第二个参数做为修改对象值
		 * 第三个参数表示如果需要更新的对象数据库不存在，则直接执行插入操作，第四个参数是控制更新多条
		 */
		 BasicDBObject query = new BasicDBObject();
		 query.put(DATABASE_FIELD_NAME_DOCID, itemDO.getDocId());
		 BasicDBObject doc = new BasicDBObject();  
	     doc.put("$set", obj);
	     
	     db.getCollection(collectionName).update(query, doc, true, false);
	     
	     if(ItemDatabaseIndexCache.getInstance().get(collectionName) == null || !ItemDatabaseIndexCache.getInstance().get(collectionName)){
				checkIndexIsExists(collectionName,db);
		 }
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
	public ItemDO queryMd5UrlLastItem(Long taskId, String md5Url) {
		DBObject q = new BasicDBObject();
		q.put(DATABASE_FIELD_NAME_URL_MD5, md5Url);
		
		DBObject dbObject = this.getDBViaTaskId(taskId).getCollection(this.buildCollectionName(taskId)).findOne(q);
		if(!Objects.isNull(dbObject)){
			ItemDO itemDO = convertDBObject2ItemDO(dbObject);
			return itemDO;
		}
		return null;
	}
	
	@Override
	public ItemDO queryLastItem(Long taskId, String docId) {
		DBObject q = new BasicDBObject();
		q.put(DATABASE_FIELD_NAME_DOCID, docId);
		
		DBObject dbObject = this.getDBViaTaskId(taskId).getCollection(this.buildCollectionName(taskId)).findOne(q);
		if(!Objects.isNull(dbObject)){
			ItemDO itemDO = convertDBObject2ItemDO(dbObject);
			return itemDO;
		}
		return null;
	}

	@Override
	public ItemDO queryCurrentMD5Urls(Long taskId, String md5Urls) {
		DBObject q = new BasicDBObject();
		q.put(DATABASE_FIELD_NAME_MD5_URLS, md5Urls);
		
		DBObject dbObject = this.getDBViaTaskId(taskId).getCollection(this.buildCollectionName(taskId)).findOne(q);
		if(!Objects.isNull(dbObject)){
			ItemDO itemDO = convertDBObject2ItemDO(dbObject);
			return itemDO;
		}
		return null;
	}	
}