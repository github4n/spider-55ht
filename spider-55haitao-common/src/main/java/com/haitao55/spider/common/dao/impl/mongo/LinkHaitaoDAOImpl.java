package com.haitao55.spider.common.dao.impl.mongo;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import com.haitao55.spider.common.dao.LinkHaiTaoDAO;
import com.haitao55.spider.common.dos.LinksDO;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;

/**
 * 
  * @ClassName: LinkHaitaoDAOImpl
  * @Description: LinkHaitao links
  * @author songsong.xu
  * @date 2017年4月6日 上午10:23:38
  *
 */
public class LinkHaitaoDAOImpl extends BaseMongoDAO implements LinkHaiTaoDAO {
    
    private static final Logger logger = LoggerFactory.getLogger(LinkHaitaoDAOImpl.class);
	private static final String DATABASE_NAME_PREFIX = "spider_linkhaitao";
	private static final String LINKHAITAO_COLLECTION_NAME_PREFIX = "links";

	private static final String EMPTY_STRING = "";
	private static final String ZERO = "0";
	private static final String DATABASE_FIELD_NAME_ID = "_id";
	private static final String DATABASE_FIELD_NAME_ORIGNAL_DOC_ID = "orignal_doc_id";
	private static final String DATABASE_FIELD_NAME_ORIGNAL_URL = "orignal_url";
	private static final String DATABASE_FIELD_NAME_TARGET_URL = "target_url";
	private static final String DATABASE_FIELD_NAME_RESULT_URL = "result_url";
	private static final String DATABASE_FIELD_NAME_EXPECT_PERCENT = "expect_percent";
	private static final String DATABASE_FIELD_NAME_RESULT_PERCENT = "result_percent";
	private static final String DATABASE_FIELD_NAME_CREATE_TIME = "create_time";
	private static final String DATABASE_FIELD_NAME_UPDATE_TIME = "update_time";
	private static final String DATABASE_FIELD_NAME_STATUS = "status";
	
	private DB getDBViaTaskId() {
		DB db = this.getMongoTemplate().getDb().getMongo().getDB(DATABASE_NAME_PREFIX);
		return db;
	}
	
	private  DBObject convertLinksDO2DBObject(LinksDO linksDO) {
		DBObject dbObject = new BasicDBObject();
		// dbObject.put(DATABASE_FIELD_NAME_ID, itemDO.getId());
		dbObject.put(DATABASE_FIELD_NAME_ORIGNAL_DOC_ID, linksDO.getOrignal_doc_id());
		dbObject.put(DATABASE_FIELD_NAME_ORIGNAL_URL, linksDO.getOrignal_url());
		dbObject.put(DATABASE_FIELD_NAME_TARGET_URL, linksDO.getTarget_url());
		dbObject.put(DATABASE_FIELD_NAME_RESULT_URL, linksDO.getResult_url());
		dbObject.put(DATABASE_FIELD_NAME_EXPECT_PERCENT, linksDO.getExpect_percent());
		dbObject.put(DATABASE_FIELD_NAME_RESULT_PERCENT, linksDO.getResult_percent());
		if(linksDO.getCreate_time() > 0){
		    dbObject.put(DATABASE_FIELD_NAME_CREATE_TIME, linksDO.getCreate_time());
		}
		dbObject.put(DATABASE_FIELD_NAME_UPDATE_TIME, linksDO.getUpdate_time());
		dbObject.put(DATABASE_FIELD_NAME_STATUS, linksDO.getStatus());
		return dbObject;
	}

	public static LinksDO convertDBObject2LinksDO(DBObject dbObject) {
		LinksDO linksDO = new LinksDO();
		linksDO.setId(Objects.toString(dbObject.get(DATABASE_FIELD_NAME_ID), EMPTY_STRING));
		linksDO.setOrignal_doc_id(Objects.toString(dbObject.get(DATABASE_FIELD_NAME_ORIGNAL_DOC_ID), EMPTY_STRING));
		linksDO.setOrignal_url(Objects.toString(dbObject.get(DATABASE_FIELD_NAME_ORIGNAL_URL), EMPTY_STRING));
		linksDO.setTarget_url(Objects.toString(dbObject.get(DATABASE_FIELD_NAME_TARGET_URL), EMPTY_STRING));
		linksDO.setResult_url(Objects.toString(dbObject.get(DATABASE_FIELD_NAME_RESULT_URL), EMPTY_STRING));
		linksDO.setExpect_percent(Double.parseDouble(Objects.toString(dbObject.get(DATABASE_FIELD_NAME_EXPECT_PERCENT), ZERO)));
		linksDO.setResult_percent(Double.parseDouble(Objects.toString(dbObject.get(DATABASE_FIELD_NAME_RESULT_PERCENT), ZERO)));
		linksDO.setCreate_time(Long.valueOf(Objects.toString(dbObject.get(DATABASE_FIELD_NAME_CREATE_TIME), ZERO)));
		linksDO.setUpdate_time(Long.valueOf(Objects.toString(dbObject.get(DATABASE_FIELD_NAME_UPDATE_TIME), ZERO)));
		linksDO.setStatus(Objects.toString(dbObject.get(DATABASE_FIELD_NAME_STATUS), EMPTY_STRING));
		return linksDO;
	}

	/**
	 * LINK HAITAO LINKS collection
	 * @return
	 */
	private  String buildCollectionName(Long taskId) {
		String collectionName = new StringBuilder(LINKHAITAO_COLLECTION_NAME_PREFIX).append(taskId).toString();
		return collectionName;
	}

	@Override
	public void insertLinks(Long taskId, List<LinksDO> linksDOList) {
		
		if (linksDOList == null || linksDOList.isEmpty()) {
			return;
		}
		List<DBObject> list = new ArrayList<DBObject>(linksDOList.size());
		for (LinksDO linksDO : linksDOList) {
			DBObject obj = this.convertLinksDO2DBObject(linksDO);
			list.add(obj);
		}
		this.getDBViaTaskId().getCollection(this.buildCollectionName(taskId)).insert(list);
	}
	
	public Set<String> getAllColls(){
	    return this.getDBViaTaskId().getCollectionNames();
	}
	
	public void upsertLinksList(Long taskId, List<LinksDO> itemDOList) {
		
		for (LinksDO linksDO : itemDOList) {
			Query query = (new Query()).addCriteria(Criteria.where(DATABASE_FIELD_NAME_ORIGNAL_DOC_ID).is(linksDO.getOrignal_doc_id()));
			Update update = Update.fromDBObject(this.convertLinksDO2DBObject(linksDO), EMPTY_STRING);
			this.getMongoTemplate().upsert(query, update, this.buildCollectionName(taskId));
		}
	}
	
	public void upsertLinks(Long taskId, LinksDO linksDO) {
		if (Objects.isNull(linksDO)) {
			return;
		}
		DBObject obj = this.convertLinksDO2DBObject(linksDO);
		DBObject dbObject = new BasicDBObject();
		dbObject.put(DATABASE_FIELD_NAME_RESULT_URL, linksDO.getResult_url());
		dbObject.put(DATABASE_FIELD_NAME_RESULT_PERCENT, linksDO.getResult_percent());
		dbObject.put(DATABASE_FIELD_NAME_UPDATE_TIME, linksDO.getUpdate_time());
		dbObject.put(DATABASE_FIELD_NAME_STATUS, linksDO.getStatus());
		BasicDBObject query = new BasicDBObject();
		query.put(DATABASE_FIELD_NAME_ORIGNAL_DOC_ID, linksDO.getOrignal_doc_id());
		BasicDBObject doc = new BasicDBObject();
		doc.put("$set", obj);
		WriteResult wr = this.getDBViaTaskId().getCollection(this.buildCollectionName(taskId)).update(query, doc, true, true, WriteConcern.SAFE);
		logger.info("WriteResult {}",wr.getError());
	}


	@Override
	public List<LinksDO> queryAllLinks(Long taskId,String status, int limit) {
		List<LinksDO> result = new ArrayList<LinksDO>();
		DBObject dbObject = new BasicDBObject();
		if(StringUtils.isNotBlank(status)){
		    dbObject.put("status", status);
		}
		DBCursor dbCursor = this.getDBViaTaskId().getCollection(this.buildCollectionName(taskId)).find(dbObject).limit(limit);
		while(dbCursor.hasNext()){
			DBObject rst = dbCursor.next();
			LinksDO itemDO = convertDBObject2LinksDO(rst);
			result.add(itemDO);
		}

		return result;
	}

	@Override
	public Long countAllLinks(Long taskId) {
		DB db = this.getDBViaTaskId();
		long count = db.getCollection(this.buildCollectionName(taskId)).distinct(DATABASE_FIELD_NAME_ORIGNAL_DOC_ID).size();
		return count;
	}
}