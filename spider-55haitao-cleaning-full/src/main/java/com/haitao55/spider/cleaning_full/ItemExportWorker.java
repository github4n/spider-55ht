package com.haitao55.spider.cleaning_full;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.haitao55.spider.common.dao.impl.mongo.ItemDAOImpl;
import com.haitao55.spider.common.dos.ItemDO;
import com.haitao55.spider.common.gson.bean.CrawlerJSONResult;
import com.haitao55.spider.common.gson.bean.LSelectionList;
import com.haitao55.spider.common.gson.bean.ProdUrl;
import com.haitao55.spider.common.gson.bean.Stock;
import com.haitao55.spider.service.CounterService;
import com.haitao55.spider.util.Constants;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import com.mongodb.Mongo;

/**
 * 
 * 功能：以一个商品数据库为单位,导出商品数据库中的(唯一的一个商品集合中的)每一个最新商品
 * 
 * @author denghuan
 * @time 2017年11月8日 下午5:06:45
 * @version 1.0
 */
public class ItemExportWorker implements Runnable {
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CLEANING_FULL);
	private static final String ITEM_DATABASE_NAME_PREFIX = "spider_items_";
	private static final String ITEM_COLLECTION_NAME_PREFIX = "current_item";
	private static final String ITEM_COLLECTION_FIELD_DOCID = "docId";
	private static final String ITEM_COLLECTION_FIELD_STATUS = "OFFLINE";
	private static final int ITEM_STOCK_STATUS = 0;

	private Mongo mongo;
	private String databaseName;
	private Set<String> ignoringTaskIds;
	
	private CountDownLatch countDownLatchStart;
	private CountDownLatch countDownLatchEnd;
	
	private CounterService counterService;

	public ItemExportWorker(Mongo mongo, String databaseName, Set<String> ignoringTaskIds,CountDownLatch countDownLatchStart, 
			CountDownLatch countDownLatchEnd,CounterService counterService) {
		this.mongo = mongo;
		this.databaseName = databaseName;
		this.ignoringTaskIds = ignoringTaskIds;
		this.countDownLatchStart = countDownLatchStart;
		this.countDownLatchEnd = countDownLatchEnd;
		this.counterService = counterService;
	}

	@Override
	public void run() {
		try {
			this.countDownLatchStart.await();

			if (!StringUtils.startsWith(this.databaseName, ITEM_DATABASE_NAME_PREFIX)) {
				logger.warn("item database name is not valid, databaseName:{}", databaseName);
				return;
			}

			DB db = mongo.getDB(databaseName);
			Set<String> collectionNames = db.getCollectionNames();

			if (CollectionUtils.isEmpty(collectionNames)) {
				logger.error("got none collection-names from a db: {}", databaseName);
				return;
			}

			String collectionName = getCollectionName(collectionNames);

			if (StringUtils.isBlank(collectionName)) {
				logger.error("collectionName is empty collectionName: {}", databaseName);
				return;
			}

			this.exportSingleCollection(db, collectionName);
		} catch (InterruptedException e) {
			logger.error("Error while countDownLatchStart.await()", e);
		} finally {
			this.countDownLatchEnd.countDown();
		}
	}

	private void exportSingleCollection(DB db, String collectionName) {
		if (!StringUtils.startsWith(collectionName, ITEM_COLLECTION_NAME_PREFIX)) {
			logger.error("item collection name is invalid, collectionName: {}", collectionName);
			return;
		}

		if (CollectionUtils.isNotEmpty(this.ignoringTaskIds)) {
			for (String ignoringTaskId : this.ignoringTaskIds) {
				if (StringUtils.endsWith(collectionName, ignoringTaskId)) {
					logger.warn("collection-name is ignored, :collectionName {}", collectionName);
					return;
				}
			}
		}

		this.doExportSingleCollection(db, collectionName);
	}

	private void doExportSingleCollection(DB db, String collectionName) {
		long startTime = System.currentTimeMillis();
		try {
			DBCollection dbCollection = db.getCollection(collectionName);

			Set<String> docIds = new HashSet<String>();
			DBCursor docDbCursor = dbCollection.find();

			while (docDbCursor.hasNext()) {
				DBObject rst = docDbCursor.next();
				String docId = Objects.toString(rst.get(ITEM_COLLECTION_FIELD_DOCID));
				docIds.add(docId);
			}

			if (CollectionUtils.isEmpty(docIds)) {
				logger.warn("got none docIds from collection: {}", collectionName);
				return;
			}
			logger.info("from collectionName: {}, got docIds.size(): {}", collectionName, docIds.size());

			for (Object docId : docIds) {

				DBObject q = new BasicDBObject();
				q.put(ITEM_COLLECTION_FIELD_DOCID, docId);

				DBObject dbObject = dbCollection.findOne(q);

				if (!Objects.isNull(dbObject)) {
					ItemDO itemDO = ItemDAOImpl.convertDBObject2ItemDO(dbObject);
					String status = itemDO.getStatus();
					CrawlerJSONResult rs = CrawlerJSONResult.buildFrom(itemDO.getValue());
					if (StringUtils.isNotBlank(status) && !ITEM_COLLECTION_FIELD_STATUS.equals(status)) {
						Stock stock = rs.getRetbody().getStock();
						String host = rs.getRetbody().getSite().getHost();
						if (!Objects.isNull(stock)) {
							if (ITEM_STOCK_STATUS != stock.getStatus()) {
								// 写queue
								this.writeLastedItem(itemDO);
								// 在线商品数据统计
								this.itemsOnlineStatistics(host);

								// 统计商品SKU数量
								itemsOnlineSkuSizeStatistics(host, rs);
							} else {
								// stock为0下架统计数量
								this.itemStockZEROStatistics(host);
							}
						}
					} else {
						ProdUrl prodUrl = rs.getRetbody().getProdUrl();
						if (!Objects.isNull(prodUrl)) {
							String url = prodUrl.getUrl();
							if (StringUtils.isNotBlank(url)) {
								String domain = this.getHostName(url);
								// OFFLINE domain下架商品数量统计
								this.itemOfflineStatistics(domain);
							}
						}
						// 统计全部下架数量
						this.allItemOfflineStatistics();
					}
					// 全部商品统计
					this.allItemTotalStatistics();
				}
			}
		} catch (Exception e) {
			logger.error("Error while write item error : {} ,collectionName : {}", e, collectionName);
		}
		logger.info("collectionName is : {}, endTime : {}", collectionName,(System.currentTimeMillis()-startTime)/1000 + "s");
	}
	
	private void writeLastedItem(ItemDO itemDO) {
		String value = itemDO.getValue();
		try {
			BlockingQueue<String> queue = ItemBolckingQueue.getInstance();
			queue.put(value);
		} catch (Exception e) {
			logger.error("Error while offer queue error, ", e);
		}
	}
	
	private String getCollectionName(Set<String> collectionNames){
		String collectionName = StringUtils.EMPTY;
		Iterator<String> it = collectionNames.iterator();
		while(it.hasNext()){
			String cName = it.next();
			if(StringUtils.isNotBlank(cName) && 
					StringUtils.containsIgnoreCase(cName, ITEM_COLLECTION_NAME_PREFIX)){
				collectionName = cName;
			}
		}
		return collectionName;
	} 
	
	private String getHostName(String url){
		String temp = StringUtils.substringAfter(url, "//");
		String domain = StringUtils.substringBefore(temp, "/");
		if (StringUtils.isNotBlank(domain)) {
			return domain;
		}
		return null;
	}
	
	/**
	 * 统计商品sku数量
	 * @param host
	 * @param rs
	 */
	private void itemsOnlineSkuSizeStatistics(String host,CrawlerJSONResult rs){
		try {
			List<LSelectionList> l_selection_list = rs.getRetbody().getSku().getL_selection_list();
			if (CollectionUtils.isEmpty(l_selection_list)) {
				counterService.addAndGet(Constants.ITEM_ALL_ONLINE_SKU_SIZE_FIELD_PREFIX + host, 1);
				counterService.addAndGet(Constants.ITEM_ALL_ONLINE_SKU_SIZE, 1);
				return;
			}
			counterService.addAndGet(Constants.ITEM_ALL_ONLINE_SKU_SIZE_FIELD_PREFIX + host, l_selection_list.size());
			counterService.addAndGet(Constants.ITEM_ALL_ONLINE_SKU_SIZE, l_selection_list.size());
		} catch (Exception e) {
			logger.info("handle Statistics sku_size error ::: execption :{}", e);
		}
	}
	
	private void itemsOnlineStatistics(String host){
		counterService.incField(Constants.CLEANING_FULL_ITEM_ONLINE_FIELD_PREFIX + host);
		counterService.incField(Constants.CLEANING_FULL_ITEM_ALL_ONLINE_FIELD_PREFIX);
	}
	
	private void itemStockZEROStatistics(String host){
		counterService.incField(Constants.CLEANING_FULL_ITEM_STOCK_ZERO_FIELD_PREFIX + host);
		counterService.incField(Constants.CLEANING_FULL_ITEM_ALL_STOCK_ZERO_FIELD_PREFIX);
	}
	
	private void itemOfflineStatistics(String host){
		counterService.incField(Constants.CLEANING_FULL_ITEM_OFFLINE_FIELD_PREFIX + host);
	}
	
	private void allItemOfflineStatistics(){
		counterService.incField(Constants.CLEANING_FULL_ITEM_ALL_OFFLINE_FIELD_PREFIX);
	}
	
	private void allItemTotalStatistics(){
		counterService.incField(Constants.CLEANING_FULL_ITEM_TOTAL_FIELD_PREFIX);
	}
}