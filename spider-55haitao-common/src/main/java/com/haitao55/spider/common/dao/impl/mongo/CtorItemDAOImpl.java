package com.haitao55.spider.common.dao.impl.mongo;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Repository;

import com.haitao55.spider.common.dao.CtorItemDAO;
import com.haitao55.spider.common.dos.CtorItemDO;
import com.haitao55.spider.common.utils.SpiderDateTimeUtil;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;

/**
 * 
 * 功能：用来操作currenItem的DAO接口实现类
 * 
 * @author denghuan
 * @time 2017年2月16日 上午11:15:22
 * @version 1.0
 */
@Repository("ctorItemDAO")
public class CtorItemDAOImpl extends BaseMongoDAO implements CtorItemDAO {
	private static final String DATABASE_NAME_PREFIX = "spider_competition_items";
	private static final String COLLECTION_NAME_PREFIX = "competition_item";

	private static final String EMPTY_STRING = "";
	private static final String ZERO = "";
	private static final String DATABASE_FIELD_NAME_ID = "_id";
	private static final String DATABASE_FIELD_NAME_VALUE = "value";
	private static final String DATABASE_FIELD_NAME_DOCID = "docId";
	private static final String DATABASE_FIELD_NAME_CREATE_TIME = "createTime";
	private static final String DATABASE_FIELD_NAME_UPDATE_TIME = "updateTime";
	private static final String DATABASE_FIELD_NAME_URL_MD5 = "urlMD5";
	private static final String DATABASE_FIELD_NAME_STATUS = "status";
	
	//private static final String DATABASE_FIELD_NAME_STATUS_VALUE_ONLINE = "online";


	/**
	 * 根据taskId获取mongo db实例
	 */
	private DB getDBViaTaskId(Long taskId) {
		DB db = this.getMongoTemplate().getDb().getMongo().getDB(DATABASE_NAME_PREFIX);
		return db;
	}
	
	private DBObject convertItemDO2DBObject(CtorItemDO itemDO) {
		DBObject dbObject = new BasicDBObject();

		// 不设值,让mongo自动帮忙生成"_id"字段值
		// dbObject.put(DATABASE_FIELD_NAME_ID, itemDO.getId());
		dbObject.put(DATABASE_FIELD_NAME_DOCID, itemDO.getDocId());
		dbObject.put(DATABASE_FIELD_NAME_URL_MD5, itemDO.getUrlMD5());
		dbObject.put(DATABASE_FIELD_NAME_CREATE_TIME, itemDO.getCreateTime());
		dbObject.put(DATABASE_FIELD_NAME_UPDATE_TIME, itemDO.getUpdateTime());
		dbObject.put(DATABASE_FIELD_NAME_VALUE, itemDO.getValue());
		dbObject.put(DATABASE_FIELD_NAME_STATUS, itemDO.getStatus());
		return dbObject;
	}

	public static CtorItemDO convertDBObject2ItemDO(DBObject dbObject) {
		CtorItemDO itemDO = new CtorItemDO();

		itemDO.setId(Objects.toString(dbObject.get(DATABASE_FIELD_NAME_ID), EMPTY_STRING));
		itemDO.setDocId(Objects.toString(dbObject.get(DATABASE_FIELD_NAME_DOCID), EMPTY_STRING));
		itemDO.setUrlMD5(Objects.toString(dbObject.get(DATABASE_FIELD_NAME_URL_MD5), EMPTY_STRING));
		itemDO.setCreateTime(Long.parseLong(Objects.toString(dbObject.get(DATABASE_FIELD_NAME_CREATE_TIME), ZERO)));
		itemDO.setUpdateTime(Long.parseLong(Objects.toString(dbObject.get(DATABASE_FIELD_NAME_UPDATE_TIME), ZERO)));
		itemDO.setValue(Objects.toString(dbObject.get(DATABASE_FIELD_NAME_VALUE), EMPTY_STRING));
		itemDO.setStatus(Objects.toString(dbObject.get(DATABASE_FIELD_NAME_STATUS), EMPTY_STRING));

		return itemDO;
	}


	private  String buildCollectionName(Long taskId) {
		String collectionName = (new StringBuilder(COLLECTION_NAME_PREFIX)).append(taskId).toString();
		return collectionName;
	}

	@Override
	public void upsertItem(Long taskId, CtorItemDO itemDO) {
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
	}

	@Override
	public List<CtorItemDO> queryCompartiotorItemsByTaskId(long taskId, int currentDay,int currentHour) {
		List<CtorItemDO> result = new ArrayList<CtorItemDO>();
		
		long currentDate = System.currentTimeMillis();
		
		String currentTime = StringUtils.EMPTY;
		long startDate = 0;
		
		try {
			if(currentDay != 0 && currentHour == 0){
				currentTime = SpiderDateTimeUtil.dateFormat(new Date().getTime(), SpiderDateTimeUtil.FORMAT_SHORT_DATE);
				startDate = SpiderDateTimeUtil.dateFormat(currentDay-1,currentTime,SpiderDateTimeUtil.FORMAT_SHORT_DATE);
			}else if(currentDay == 0 && currentHour != 0){
				currentTime = SpiderDateTimeUtil.dateFormat(new Date().getTime(), SpiderDateTimeUtil.FORMAT_LONG_DATE);
				startDate = SpiderDateTimeUtil.dateHourFormat(currentHour,currentTime,SpiderDateTimeUtil.FORMAT_LONG_DATE);
			}else{//默认6个小时的数据
				currentTime = SpiderDateTimeUtil.dateFormat(new Date().getTime(), SpiderDateTimeUtil.FORMAT_LONG_DATE);
				startDate = SpiderDateTimeUtil.dateHourFormat(6,currentTime,SpiderDateTimeUtil.FORMAT_LONG_DATE);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		DBObject q = new BasicDBObject();
		q.put("updateTime", new BasicDBObject("$lte", currentDate).append("$gte", startDate));
		
		DBCursor dbCursor = this.getDBViaTaskId(taskId).getCollection(this.buildCollectionName(taskId)).find(q);
		while(dbCursor.hasNext()){
			DBObject rst = dbCursor.next();
			CtorItemDO itemDO = convertDBObject2ItemDO(rst);
			result.add(itemDO);
		}
		return result;
	}
}