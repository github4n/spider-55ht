package com.haitao55.spider.common.dao.impl.mongo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.springframework.stereotype.Repository;

import com.haitao55.spider.common.dao.WeiBoItemDAO;
import com.haitao55.spider.common.dos.ItemDO;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

/**
 * 
 * 功能：用来操作weiBoItemDAO的DAO接口实现类
 * 
 * @author denghuan
 * @time 2017年8月21日 上午11:15:22
 * @version 1.0
 */
@Repository("weiBoItemDAO")
public class WeiBoItemDAOImpl extends BaseMongoDAO implements WeiBoItemDAO {
	private static final String DATABASE_NAME_PREFIX = "spider_weibo_items";
	private static final String WEIBO_ITEM_COLLECTION_NAME_PREFIX = "weibo_item";//

	private static final String EMPTY_STRING = "";
	private static final String ZERO = "";
	private static final String DATABASE_FIELD_NAME_ID = "_id";
	private static final String DATABASE_FIELD_NAME_VALUE = "value";
	private static final String DATABASE_FIELD_NAME_DOCID = "docId";
	private static final String DATABASE_FIELD_NAME_CREATE_TIME = "createTime";
	private static final String DATABASE_FIELD_NAME_URL_MD5 = "urlMD5";
	private static final String DATABASE_FIELD_NAME_STATUS = "status";
	
	/**
	 * 根据taskId获取mongo db实例
	 */
	private DB getDBViaTaskId() {
		DB db = this.getMongoTemplate().getDb().getMongo().getDB(DATABASE_NAME_PREFIX);
		return db;
	}
	
	private  DBObject convertItemDO2DBObject(ItemDO itemDO) {
		DBObject dbObject = new BasicDBObject();

		// 不设值,让mongo自动帮忙生成"_id"字段值
		// dbObject.put(DATABASE_FIELD_NAME_ID, itemDO.getId());
		dbObject.put(DATABASE_FIELD_NAME_DOCID, itemDO.getDocId());
		dbObject.put(DATABASE_FIELD_NAME_URL_MD5, itemDO.getUrlMD5());
		dbObject.put(DATABASE_FIELD_NAME_CREATE_TIME, itemDO.getCreateTime());
		dbObject.put(DATABASE_FIELD_NAME_VALUE, itemDO.getValue());
		dbObject.put(DATABASE_FIELD_NAME_STATUS, itemDO.getStatus());
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

		return itemDO;
	}

	/**
	 * 淘宝代购collection
	 * @return
	 */
	private  String buildWeiBoCollectionName(Long taskId) {
		String collectionName = (new StringBuilder(WEIBO_ITEM_COLLECTION_NAME_PREFIX)).append(taskId).toString();
		return collectionName;
	}
	
	@Override
	public void upsertWeiBoItem(ItemDO itemDO,Long taskId) {
		if (Objects.isNull(itemDO)) {
			return;
		}
		
		DBObject obj = this.convertItemDO2DBObject(itemDO);
		
		DB db = this.getDBViaTaskId();
		
		String collectionName = this.buildWeiBoCollectionName(taskId);
		/**
		 * 第一个参数是docId做为查询条件,第二个参数做为修改对象值
		 * 第三个参数表示如果需要更新的对象数据库不存在，则直接执行插入操作，第四个参数是控制更新多条
		 */
		 BasicDBObject query = new BasicDBObject();
		 query.put(DATABASE_FIELD_NAME_DOCID, itemDO.getDocId());
		 BasicDBObject doc = new BasicDBObject();  
	     doc.put("$set", obj);
	     
	     db.getCollection(collectionName).update(query, doc, true, false);
	}

	@Override
	public List<ItemDO> queryWeiBoItems(int page, int pageSize,Long taskId) {
		List<ItemDO> result = new ArrayList<ItemDO>();
		DBObject s = new BasicDBObject();
		s.put(DATABASE_FIELD_NAME_CREATE_TIME, -1);
		DBCursor dbCursor = this.getDBViaTaskId().getCollection(this.buildWeiBoCollectionName(taskId)).find().skip(page).limit(pageSize).sort(s);
		while(dbCursor.hasNext()){
			DBObject rst = dbCursor.next();
			ItemDO itemDO = convertDBObject2ItemDO(rst);
			result.add(itemDO);
		}
		return result;
	}
}