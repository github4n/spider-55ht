package com.haitao55.spider.common.dao.impl.mongo;

import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;

import com.mongodb.DBObject;
import com.mongodb.WriteResult;

/**
 * 
 * 功能：数据库操作的MongoDAO基类
 * 
 * @author Arthur.Liu
 * @time 2016年6月7日 下午5:02:04
 * @version 1.0
 */
public class BaseMongoDAO {
	private MongoTemplate mongoTemplate;
	
    public MongoTemplate getMongoTemplate() {
		return mongoTemplate;
	}

	public void setMongoTemplate(MongoTemplate mongoTemplate) {
		this.mongoTemplate = mongoTemplate;
	}

	public WriteResult insert(String collection, DBObject... datas) {
        WriteResult result = this.getMongoTemplate().getCollection(collection).insert(datas);
        return result;
    }

    public WriteResult update(String collection, Query q, Update u) {
        WriteResult result = this.getMongoTemplate().updateFirst(q, u, collection);
        return result;
    }

    public WriteResult delete(String collection, DBObject data) {
        WriteResult result = this.getMongoTemplate().getCollection(collection).remove(data);
        return result;
    }
}