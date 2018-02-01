package com.haitao55.spider.common.dao.impl.mongo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Repository;
import com.haitao55.spider.common.dao.ImageDAO;
import com.haitao55.spider.common.dos.ImageDO;
import com.haitao55.spider.common.utils.ItemDatabaseIndexCache;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;


@Repository("imageDAO")
public class ImageDAOImpl extends BaseMongoDAO implements ImageDAO {
	private static final String DATABASE_NAME_PREFIX = "spider_images_";
	private static final String COLLECTION_NAME_PREFIX = "images";

	private static final String EMPTY_STRING = "";
	private static final String ZERO = "";
	private static final String DATABASE_FIELD_NAME_ID = "_id";
	private static final String DATABASE_FIELD_NAME_DOCID = "doc_id";
	private static final String DATABASE_FIELD_NAME_SRC = "src";
	private static final String DATABASE_FIELD_NAME_SRC_KEY = "src_key";//src md5
	private static final String DATABASE_FIELD_NAME_CDN = "cdn";
	private static final String DATABASE_FIELD_NAME_CDN_KEY = "cdn_key";
	private static final String DATABASE_FIELD_NAME_CREATE_TIME = "create_time";
	private static final String DATABASE_FIELD_NAME_UPDATE_TIME = "update_time";
	private static final String DATABASE_FIELD_NAME_STATUS = "status";//0 未下载,1 已下载,2 删除

	@Override
	public void insertImages(Long taskId, List<ImageDO> imageDOList) {
		if (imageDOList == null || imageDOList.isEmpty()) {
			return;
		}

		List<DBObject> list = new ArrayList<DBObject>(imageDOList.size());
		for (ImageDO imageDO : imageDOList) {
			DBObject obj = this.convertImageDO2DBObject(imageDO);
			list.add(obj);
		}
		
		String collectionName = this.buildCollectionName(taskId);
		
		DB db = this.getDBViaTaskId(taskId);
		
		db.getCollection(collectionName).insert(list);
		
		if(ItemDatabaseIndexCache.getInstance().get(collectionName) == null || 
				!ItemDatabaseIndexCache.getInstance().get(collectionName)){
			checkIndexIsExists(collectionName,db);
		}
		
	}
	
	/**
	 * 判断索引是否存在,不存在创建hash索引
	 * @param collectionName
	 * @param db
	 */
	@SuppressWarnings("deprecation")
	public void checkIndexIsExists(String collectionName,DB db){
		DBCollection coll = db.getCollection(collectionName);
		List<DBObject> indexs = coll.getIndexInfo();
		boolean isIndex = false;
		for(int i = 0;i < indexs.size(); i++){
			String index = indexs.get(i).toString();
			if(StringUtils.containsIgnoreCase(index, DATABASE_FIELD_NAME_SRC_KEY)){
				ItemDatabaseIndexCache.getInstance().put(collectionName, true);
				isIndex = true;
				break;
			}	
		}
		
		if(!isIndex){
			 DBObject dbObjKeys = new BasicDBObject();
			 dbObjKeys.put(DATABASE_FIELD_NAME_SRC_KEY, "hashed");
			 DBObject optionsDbObj = new BasicDBObject();
			 optionsDbObj.put("sparse", true);
			 optionsDbObj.put("dropDups", true);
	         coll.ensureIndex(dbObjKeys,optionsDbObj);
			ItemDatabaseIndexCache.getInstance().put(collectionName, true);//标识是否创建过索引
		}
	}
	

	/**
	 * 根据taskId获取mongo db实例
	 */
	private DB getDBViaTaskId(Long taskId) {
		DB db = this.getMongoTemplate().getDb().getMongo().getDB(DATABASE_NAME_PREFIX + taskId);
		return db;
	}
	

	
	@Override
	public void updateImages(Long taskId, List<ImageDO> imageDOList) {
		for (ImageDO imageDO : imageDOList) {
			Query query = (new Query()).addCriteria(Criteria.where(DATABASE_FIELD_NAME_ID).is(imageDO.getId()));
			Update update = this.buildUpdate(imageDO);

			// 很遗憾,没有批量更新接口；所以只能在这个for循环中一个一个的更新
			this.getMongoTemplate().updateFirst(query, update, this.buildCollectionName(taskId));
		}
	}

	@Override
	public List<ImageDO> queryImages(Long taskId, int limit) {
		List<ImageDO> result = new ArrayList<ImageDO>();

		DBCursor dbCursor = this.getDBViaTaskId(taskId).getCollection(this.buildCollectionName(taskId)).find().limit(limit);
		while(dbCursor.hasNext()){
			DBObject rst = dbCursor.next();
			ImageDO imageDO = ImageDAOImpl.convertDBObject2ImageDO(rst);
			result.add(imageDO);
		}

		return result;
	}

	@Override
	public long count(Long taskId) {
		DB db = this.getDBViaTaskId(taskId);
		long count = db.getCollection(this.buildCollectionName(taskId)).count();
		/**
		 * spring api实现方式
		 */
		/*Query query = (new Query()).addCriteria(Criteria.where(DATABASE_FIELD_NAME_ID).exists(true));
		long count = this.getMongoTemplate().count(query, this.buildCollectionName(taskId));*/
		return count;
	}
	
	@Override
	public long countImages(Long taskId) {
		DB db = this.getDBViaTaskId(taskId);
		long count = db.getCollection(this.buildCollectionName(taskId)).distinct(DATABASE_FIELD_NAME_DOCID).size();
		return count;
	}

	@Override
	public long countImagesBy(Long taskId,ImageDO imageDO) {// FIXME::这个方法这里这样写有问题,这样是不可以的
		DBObject query = new BasicDBObject();
		if(StringUtils.isNotBlank(imageDO.getSrc())){
			query.put(DATABASE_FIELD_NAME_SRC, imageDO.getSrc());
		}
		if(StringUtils.isNotBlank(imageDO.getSrc_key())){
			query.put(DATABASE_FIELD_NAME_SRC_KEY, imageDO.getSrc_key());
		}
		if(StringUtils.isNotBlank(imageDO.getCdn())){
			query.put(DATABASE_FIELD_NAME_CDN, imageDO.getCdn());
		}
		if(StringUtils.isNotBlank(imageDO.getCdn_key())){
			query.put(DATABASE_FIELD_NAME_CDN_KEY, imageDO.getCdn_key());
		}
		if(StringUtils.isNotBlank(imageDO.getStatus())){
			query.put(DATABASE_FIELD_NAME_STATUS, imageDO.getStatus());
		}
		DB db = this.getDBViaTaskId(taskId);
		long count = db.getCollection(this.buildCollectionName(taskId)).distinct(DATABASE_FIELD_NAME_DOCID, query).size();
		return count;
	}

	@Override
	public void upsertImages(Long taskId, List<ImageDO> imageDOList) {
		for (ImageDO imageDO : imageDOList) {
			Query query = (new Query()).addCriteria(Criteria.where(DATABASE_FIELD_NAME_ID).is(imageDO.getId()));
			Update update = Update.fromDBObject(this.convertImageDO2DBObject(imageDO), EMPTY_STRING);
			this.getMongoTemplate().upsert(query, update, this.buildCollectionName(taskId));
		}
	}

	private DBObject convertImageDO2DBObject(ImageDO imageDO) {
		DBObject dbObject = new BasicDBObject();

		// 不设值,让mongo自动帮忙生成"_id"字段值
		// dbObject.put(DATABASE_FIELD_NAME_ID, itemDO.getId());
		dbObject.put(DATABASE_FIELD_NAME_DOCID, imageDO.getDoc_id());
		dbObject.put(DATABASE_FIELD_NAME_SRC, imageDO.getSrc());
		dbObject.put(DATABASE_FIELD_NAME_SRC_KEY, imageDO.getSrc_key());
		dbObject.put(DATABASE_FIELD_NAME_CDN, imageDO.getCdn());
		dbObject.put(DATABASE_FIELD_NAME_CDN_KEY, imageDO.getCdn_key());
		dbObject.put(DATABASE_FIELD_NAME_CREATE_TIME, imageDO.getCreate_time());
		//dbObject.put(DATABASE_FIELD_NAME_UPDATE_TIME, imageDO.getUpdate_time());
		dbObject.put(DATABASE_FIELD_NAME_STATUS, imageDO.getStatus());
		return dbObject;
	}

	public static ImageDO convertDBObject2ImageDO(DBObject dbObject) {
		ImageDO imageDO = new ImageDO();

		imageDO.setId(Objects.toString(dbObject.get(DATABASE_FIELD_NAME_ID), EMPTY_STRING));
		imageDO.setDoc_id(Objects.toString(dbObject.get(DATABASE_FIELD_NAME_DOCID), EMPTY_STRING));
		imageDO.setSrc(Objects.toString(dbObject.get(DATABASE_FIELD_NAME_SRC), EMPTY_STRING));
		imageDO.setSrc_key(Objects.toString(dbObject.get(DATABASE_FIELD_NAME_SRC_KEY), EMPTY_STRING));
		imageDO.setCdn(Objects.toString(dbObject.get(DATABASE_FIELD_NAME_CDN), EMPTY_STRING));
		imageDO.setCdn_key(Objects.toString(dbObject.get(DATABASE_FIELD_NAME_CDN_KEY), EMPTY_STRING));
		imageDO.setCreate_time(Long.parseLong(Objects.toString(dbObject.get(DATABASE_FIELD_NAME_CREATE_TIME), ZERO)));
		imageDO.setUpdate_time(Long.parseLong(Objects.toString(dbObject.get(DATABASE_FIELD_NAME_UPDATE_TIME), ZERO)));;
		imageDO.setStatus(Objects.toString(dbObject.get(DATABASE_FIELD_NAME_STATUS), EMPTY_STRING));

		return imageDO;
	}

	private Update buildUpdate(ImageDO imageDO) {
		Update update = new Update();
		update.set(DATABASE_FIELD_NAME_UPDATE_TIME, imageDO.getUpdate_time());
		if(StringUtils.isNotBlank(imageDO.getSrc())){
			update.set(DATABASE_FIELD_NAME_SRC, imageDO.getSrc());
		}
		if(StringUtils.isNotBlank(imageDO.getSrc_key())){
			update.set(DATABASE_FIELD_NAME_SRC_KEY, imageDO.getSrc_key());
		}
		if(StringUtils.isNotBlank(imageDO.getCdn())){
			update.set(DATABASE_FIELD_NAME_CDN, imageDO.getCdn());
		}
		if(StringUtils.isNotBlank(imageDO.getCdn_key())){
			update.set(DATABASE_FIELD_NAME_CDN_KEY, imageDO.getCdn_key());
		}
		if(imageDO.getUpdate_time() > 0){
			update.set(DATABASE_FIELD_NAME_UPDATE_TIME, imageDO.getUpdate_time());
		}
		return update;
	}

	private String buildCollectionName(Long taskId) {
		String collectionName = (new StringBuilder(COLLECTION_NAME_PREFIX)).append(taskId).toString();
		return collectionName;
	}

	@Override
	public void insertImage(Long taskId, ImageDO imageDO) {
		DB db = this.getDBViaTaskId(taskId);

		if (Objects.isNull(imageDO)) {
			return;
		}
		DBObject obj = this.convertImageDO2DBObject(imageDO);
		db.getCollection(this.buildCollectionName(taskId)).insert(obj);
		//this.getMongoTemplate().insert(obj, this.buildCollectionName(taskId));
	}

	@Override
	public boolean checkCollecionIsExists(Long taskId) {
		DB db = this.getDBViaTaskId(taskId);
		boolean  flag = db.collectionExists(this.buildCollectionName(taskId));
		return flag;
	}
}