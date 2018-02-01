package com.haitao55.spider.common.dao.impl.mongo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Repository;
import com.haitao55.spider.common.dao.TaoBaoItemDAO;
import com.haitao55.spider.common.dos.ItemDO;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCursor;	
import com.mongodb.DBObject;

/**
 * 
 * 功能：用来操作TaoBaoItemDAO的DAO接口实现类
 * 
 * @author denghuan
 * @time 2017年3月10日 上午11:15:22
 * @version 1.0
 */
@Repository("taoBaoItemDAO")
public class TaoBaoItemDAOImpl extends BaseMongoDAO implements TaoBaoItemDAO {
	private static final String DATABASE_NAME_PREFIX = "spider_taobao_items";
	private static final String TAOBAO_DAIGOU_COLLECTION_NAME_PREFIX = "taobao_daigou_item";//代购
	private static final String TAOBAO_ZHIGOU_COLLECTION_NAME_PREFIX = "taobao_zhigou_item";//直购
	private static final String TAOBAO_MEACHANT_COLLECTION_NAME_PREFIX = "taobao_merchant_item";//商家

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
	private  String buildTaoBaoDaiGouCollectionName() {
		String collectionName = (new StringBuilder(TAOBAO_DAIGOU_COLLECTION_NAME_PREFIX)).toString();
		return collectionName;
	}
	
	/**
	 * 淘宝直购collection
	 * @return
	 */
	private  String buildTaoBaoZhiGouCollectionName() {
		String collectionName = (new StringBuilder(TAOBAO_ZHIGOU_COLLECTION_NAME_PREFIX)).toString();
		return collectionName;
	}
	
	/**
	 * 淘宝Merchantcollection
	 * @return
	 */
	private  String buildTaoBaoMerchantCollectionName() {
		String collectionName = (new StringBuilder(TAOBAO_MEACHANT_COLLECTION_NAME_PREFIX)).toString();
		return collectionName;
	}
	
	
	@Override
	public void upsertTaoBaoMerchantItem(ItemDO itemDO) {
		if (Objects.isNull(itemDO)) {
			return;
		}
		
		DBObject obj = this.convertItemDO2DBObject(itemDO);
		
		DB db = this.getDBViaTaskId();
		
		String collectionName = this.buildTaoBaoMerchantCollectionName();
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
	public void upsertTaoBaoDgItem(ItemDO itemDO) {
		if (Objects.isNull(itemDO)) {
			return;
		}
		
		DBObject obj = this.convertItemDO2DBObject(itemDO);
		
		DB db = this.getDBViaTaskId();
		
		String collectionName = this.buildTaoBaoDaiGouCollectionName();
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
	public List<ItemDO> queryTaoBaoDgItems(int page, int pageSize) {
		List<ItemDO> result = new ArrayList<ItemDO>();
		DBObject s = new BasicDBObject();
		s.put(DATABASE_FIELD_NAME_CREATE_TIME, -1);
		DBCursor dbCursor = this.getDBViaTaskId().getCollection(this.buildTaoBaoDaiGouCollectionName()).find().skip(page).limit(pageSize).sort(s);
		while(dbCursor.hasNext()){
			DBObject rst = dbCursor.next();
			ItemDO itemDO = convertDBObject2ItemDO(rst);
			result.add(itemDO);
		}
		return result;
		
	}
	
	@Override
	public Long countTaoBaoDgItems(){
		DB db = this.getDBViaTaskId();
		return  db.getCollection(this.buildTaoBaoDaiGouCollectionName()).count();
	}

	/**
	 * 以上属于淘宝代购业务方法.........
	 * 
	 * 以下业务属于淘宝直购业务方法
	 *          请知晓 
	 *  			
	 */
	
	@Override
	public void upsertTaoBaoZgItem(ItemDO itemDO) {
		if (Objects.isNull(itemDO)) {
			return;
		}
		
		DBObject obj = this.convertItemDO2DBObject(itemDO);
		
		DB db = this.getDBViaTaskId();
		
		String collectionName = this.buildTaoBaoZhiGouCollectionName();
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
	public List<ItemDO> queryTaoBaoZgItems(int page, int pageSize) {
		List<ItemDO> result = new ArrayList<ItemDO>();
		DBObject s = new BasicDBObject();
		s.put(DATABASE_FIELD_NAME_CREATE_TIME, -1);
		DBCursor dbCursor = this.getDBViaTaskId().getCollection(this.buildTaoBaoZhiGouCollectionName()).find().skip(page).limit(pageSize).sort(s);
		while(dbCursor.hasNext()){
			DBObject rst = dbCursor.next();
			ItemDO itemDO = convertDBObject2ItemDO(rst);
			result.add(itemDO);
		}
		return result;
		
	}
	
	@Override
	public Long countTaoBaoZgItems(){
		DB db = this.getDBViaTaskId();
		return  db.getCollection(this.buildTaoBaoZhiGouCollectionName()).count();
	}
	
}