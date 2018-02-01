package com.haitao55.spider.cleaning.service.impl;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.haitao55.spider.cleaning.service.ICleaningService;
import com.haitao55.spider.cleaning.utils.CleaningAfterBolckingQueue;
import com.haitao55.spider.cleaning.utils.KafkaItemBolckingQueue;
import com.haitao55.spider.common.dao.CtorItemDAO;
import com.haitao55.spider.common.dao.CurrentItemDAO;
import com.haitao55.spider.common.dao.HaiTunCunItemDAO;
import com.haitao55.spider.common.dao.ItemDAO;
import com.haitao55.spider.common.dao.TaoBaoItemDAO;
import com.haitao55.spider.common.dos.CtorItemDO;
import com.haitao55.spider.common.dos.ItemDO;
import com.haitao55.spider.common.gson.bean.CrawlerJSONResult;
import com.haitao55.spider.common.gson.bean.HaiTunCunRetBody;
import com.haitao55.spider.common.gson.bean.ProdUrl;
import com.haitao55.spider.common.gson.bean.RetBody;
import com.haitao55.spider.common.gson.bean.Stock;
import com.haitao55.spider.common.gson.bean.competitor.CtorRetBody;
import com.haitao55.spider.common.gson.bean.taobao.TBMerchantBody;
import com.haitao55.spider.common.gson.bean.taobao.TBRetBody;
import com.haitao55.spider.common.gson.bean.taobao.TBType;
import com.haitao55.spider.common.kafka.SpiderKafkaProducer;
import com.haitao55.spider.common.kafka.SpiderKafkaResult;
import com.haitao55.spider.common.utils.ItemEnum;
import com.haitao55.spider.common.utils.SpiderStringUtil;

/**
 * 后处理数据清洗-service
 * @author denghuan
 *
 */

@Service("cleaningService")
public class CleaningServiceImpl implements ICleaningService {
	private static final Logger logger = LoggerFactory.getLogger(CleaningServiceImpl.class);
 
	private ItemDAO itemDAO;

	private CurrentItemDAO currentItemDAO;
	
	private CtorItemDAO ctorItemDAO; //竞品业务DAO
	
	private HaiTunCunItemDAO haiTunCunItemDAO; //海豚村
	
	private TaoBaoItemDAO taoBaoItemDAO;
	
	private SpiderKafkaProducer spiderKafkaProducer;

	private String topic;// kafka topic
	
	/**
	 * 增量各个业务业务逻辑
	 * @param crawlerResultFromKafka
	 */
	private void checkItemDataUpdate(CrawlerJSONResult crawlerResultFromKafka) {
		try {
			RetBody retbody = crawlerResultFromKafka.getRetbody();
			if(null != retbody){
				String docType = crawlerResultFromKafka.getDocType();
				String url = retbody.getProdUrl().getUrl();
				String taskId = crawlerResultFromKafka.getTaskId();
				
				if (docType.equals(ItemEnum.DocType.DELETE.toString())) {
					ItemDO itemDO = currentItemDAO.queryMd5UrlLastItem(Long.parseLong(taskId), SpiderStringUtil.md5Encode(url));// 查找是否有记录
					
					if (!Objects.isNull(itemDO) && itemDO.getStatus().equals(ItemEnum.Status.ONLINE.toString())) {
						String mongoItem = itemDO.getValue();
						CrawlerJSONResult crawlerResultFromMongo = CrawlerJSONResult.buildFrom(mongoItem);
						crawlerResultFromMongo.getRetbody().getStock().setStatus(0);
						crawlerResultFromMongo.setTaskId(null);
						crawlerResultFromMongo.setDocType(null);
						String lastOnlineItemValue = crawlerResultFromMongo.parseTo();
						this.writeKafka(topic, lastOnlineItemValue, url);
						
						this.writeMongo(crawlerResultFromMongo.getRetbody().getDOCID(), crawlerResultFromKafka.parseTo(),
								url, taskId, ItemEnum.Status.OFFLINE.toString(),itemDO);
					}
				} else if (docType.equals(ItemEnum.DocType.INSERT.toString())) {
					String docId = retbody.getDOCID();
					RetBody retbodyFromKafka = retbody;
					
					ItemDO itemDO = currentItemDAO.queryLastItem(Long.parseLong(taskId), docId);
					if (Objects.isNull(itemDO)) {// mongodb中还没有历史商品数据
						this.writeItem(taskId, docId, url, ItemEnum.Status.ONLINE.toString(), crawlerResultFromKafka,itemDO);
					} else {// mongodb中已经有了历史商品数据；
						if (itemDO.getStatus().equals(ItemEnum.Status.OFFLINE.toString())) {
							this.writeItem(taskId, docId, url, ItemEnum.Status.ONLINE.toString(), crawlerResultFromKafka,itemDO);
						} else if (itemDO.getStatus().equals(ItemEnum.Status.ONLINE.toString())) {
							String itemJsonFromMongo = itemDO.getValue();
							CrawlerJSONResult crawlerFromMongo = CrawlerJSONResult.buildFrom(itemJsonFromMongo);
							RetBody retBodyFromMongo = crawlerFromMongo.getRetbody();
							
							boolean isSame = retbodyFromKafka.equals(retBodyFromMongo); // 调用equals方法进行kafka与mongodb的商品数据进行对比
							logger.info("kafka-mongodb compare ->taskId: {},docId: {},url :{}, isSame:{} ", taskId, docId, url,isSame);
							if (!isSame) {
								this.writeItem(taskId, docId, url, ItemEnum.Status.ONLINE.toString(),
										crawlerResultFromKafka,itemDO);
							}else{
								//针对第五大道图片更新做调整
								if(StringUtils.containsIgnoreCase(url, "saksoff5th.com") ||
										StringUtils.containsIgnoreCase(url, "saksfifthavenue.com")){
									this.writeItem(taskId, docId, url, ItemEnum.Status.ONLINE.toString(),
											crawlerResultFromKafka,itemDO);
									logger.info("saks Image update -> taskId :{} ,docId :{} ,url :{}",taskId,docId,url);
								}
								
								//query item urlMD5 isexist
								if (StringUtils.containsIgnoreCase(url, "6pm.com")
										|| StringUtils.containsIgnoreCase(url, "zappos.com")
										|| StringUtils.containsIgnoreCase(url, "victoriassecret.com")) {
									this.checkUrlExistsInHistory(taskId, docId, url, ItemEnum.Status.ONLINE.toString(),
											crawlerResultFromKafka,itemDO);
								}
							}
						}
					}
				}
			}
			//竞品body
			CtorRetBody ctorRetbody = crawlerResultFromKafka.getCtorRetbody();
			if(null != ctorRetbody){
				String url = ctorRetbody.getCtorProdUrl().getUrl();
				String taskId = crawlerResultFromKafka.getTaskId();
				String docId = ctorRetbody.getDOCID();
				String itemJsonMore = crawlerResultFromKafka.parseTo();
				String update_time = ctorRetbody.getCtorProdUrl().getUpdate_time();
				writeCtorMongo(docId, itemJsonMore, url, update_time, taskId, ItemEnum.Status.ONLINE.toString());
			}
			//海豚村body
			HaiTunCunRetBody htcRetbody = crawlerResultFromKafka.getHtcRetBody();
			if(null != htcRetbody){
				String docId = htcRetbody.getDocId();
				String itemJsonMore = crawlerResultFromKafka.parseTo();
				writeHTCMongo(docId, itemJsonMore, ItemEnum.Status.ONLINE.toString());
			}
			//淘宝全球购body
			TBRetBody tbRetBody = crawlerResultFromKafka.getTbRetBody();
			if(null != tbRetBody){
				String docId = tbRetBody.getDOCID();
				boolean  isexist = false;
				TBType type = tbRetBody.getType();
				if(!Objects.isNull(type)){
					isexist = true;
					crawlerResultFromKafka.getTbRetBody().setType(null);//将Type设置为空,不传入mongo
				}
				String itemJsonMore = crawlerResultFromKafka.parseTo();
				String url = tbRetBody.getProductUrl();
				
				writeTaoBaoMongo(docId, itemJsonMore,url, ItemEnum.Status.ONLINE.toString(),isexist);//淘宝全球购
			}
			
			TBMerchantBody tbMreBody = crawlerResultFromKafka.getTbMerchantBody();
			if(null != tbMreBody){
				String docId = tbMreBody.getDOCID();
				String url = tbMreBody.getUrl();
				String itemJsonMore = crawlerResultFromKafka.parseTo();
				writeTaoBaoMerchantMongo(docId,itemJsonMore,url,ItemEnum.Status.ONLINE.toString());
			}
			
		} catch (Exception e) {
			logger.error("handle cralwerResult Excepiton ::", e);
		}
	}

	/**
	 * 淘宝数据写入mongo
	 * @param docId
	 * @param value
	 * @param url
	 * @param taskId
	 * @param itemStatus
	 * @param type
	 */
	private void writeTaoBaoMerchantMongo(String docId, String value,String url ,String itemStatus) {
		try {
			ItemDO itemDO = new ItemDO();
			itemDO.setDocId(docId);
			itemDO.setUrlMD5(SpiderStringUtil.md5Encode(url));
			itemDO.setCreateTime(new Date().getTime());
			itemDO.setValue(value);
			itemDO.setStatus(itemStatus);
			taoBaoItemDAO.upsertTaoBaoMerchantItem(itemDO);
		} catch (Exception e) {
			logger.error("insert to mongodb Exception...", e);
		}
	}
	
	
	private void writeItem(String taskId, String docId, String url, String itemStatus,
			CrawlerJSONResult crawlerResult, ItemDO itemDO) {
		String itemJsonMore = crawlerResult.parseTo();

		crawlerResult.setTaskId(null);// 传递给搜索引擎的数据中，不希望有taskId字段值
		crawlerResult.setDocType(null);
		String itemJsonLess = crawlerResult.parseTo();

		// 1.将最新的商品数据发送到kafka服务器,供搜索引擎后续继续消费使用
		writeKafka(topic, itemJsonLess, url);

		// 2.将最新的商品数据写入mongodb数据库中
		writeMongo(docId, itemJsonMore, url, taskId, itemStatus,itemDO);

		// 3.将清洗后数据写入queue
		cleaningAfterDataToQueue(itemJsonMore);
	}

	private void writeKafka(String topic, String itemJson, String url) {
		try {
			SpiderKafkaResult result = spiderKafkaProducer.send(topic, itemJson);
			logger.info("send a message offset :{}", result.getOffset());
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("url:{},send message to topic {},exception {}", url, topic, e);
		}
	}
	
	/**
	 * 查询历史商品库商品数据是否存在,不存在做插入操作
	 * @param taskId
	 * @param docId
	 * @param url
	 * @param itemStatus
	 * @param crawlerResult
	 */
	
	private void checkUrlExistsInHistory(String taskId, String docId, String url, String itemStatus,
			CrawlerJSONResult crawlerResult,ItemDO itemDO){
		try {
			long start = System.currentTimeMillis();
			boolean isExist = itemDAO.queryMd5UrlIsexist(Long.parseLong(taskId), SpiderStringUtil.md5Encode(url));
			logger.info("checkUrlExistsInHistory ---> taskId : {} , docId : {} ,md5URL : {} , url : {} , isexist : {} , spent-time : {}",taskId ,docId ,SpiderStringUtil.md5Encode(url) , url,isExist,System.currentTimeMillis() - start);
			
			if(!isExist){
				String value = crawlerResult.parseTo();
				this.writeMongo(docId,value,url,taskId,itemStatus,itemDO);
			}
		} catch (Exception e) {
			logger.error("handle checkUrlExistsInHistory Error ::::", e);
		}
	}
	
	private void writeMongo(String docId, String value, String url, String taskId, String itemStatus,ItemDO currentItemDO) {
		try {
			ItemDO itemDO = new ItemDO();
			
			handleUrls(currentItemDO,url,itemDO);//处理urls和MD5Urls业务逻辑
			
			itemDO.setDocId(docId);
			itemDO.setUrlMD5(SpiderStringUtil.md5Encode(url));
			itemDO.setCreateTime(new Date().getTime());
			itemDO.setValue(value);
			itemDO.setStatus(itemStatus);
		
			currentItemDAO.upsertCurrentItems(Long.parseLong(taskId), itemDO);//先写currentItem表
			itemDAO.insertItemEnsureIndex(Long.parseLong(taskId), itemDO);
		} catch (Exception e) {
			logger.error("handle writeMongo Error:::", e);
		}
	}
	
	private void handleUrls(ItemDO currentItemDO,String currrentUrl,ItemDO targetItemDO){
		 long startTime = System.currentTimeMillis();
	     String currrentUrlMd5 = SpiderStringUtil.md5Encode(currrentUrl);
	     if(!Objects.isNull(currentItemDO)){
	         String[] md5Urls = currentItemDO.getMd5Urls();
		     String[] urls = currentItemDO.getUrls();
		     boolean urlsMd5Flag = false;
		     if((md5Urls != null && md5Urls.length > 0) && 
		    		 (urls != null && urls.length > 0)){
		    	 
		    	 List<String> urlsMd5List = new ArrayList<>();
		  	     List<String> urlsList = new ArrayList<>();
		    	 
		    	 for(String urlMd5 : md5Urls){
		    		 if(StringUtils.equals(urlMd5, currrentUrlMd5)){
		    			 urlsMd5Flag = true;
		    			 break;
		    		 }
		    	 }
		  	     
		    	 for(String urlMd5 : md5Urls){
		    		 if(StringUtils.isNotBlank(urlMd5)){
		    			 urlsMd5List.add(urlMd5);
		    		 }
		    	 }
		    	 
		    	 for(String url : urls){
		    		 if(StringUtils.isNotBlank(url)){
		    			 urlsList.add(url);
		    		 }
		    	 }
		    	 
		    	 if(!urlsMd5Flag){
		    		 urlsMd5List.add(currrentUrlMd5);
		    		 urlsList.add(currrentUrl);
		    	 }
		    	 
		    	 String[] handleAfterUrlsMd5 = urlsMd5List.toArray(new String[urlsMd5List.size()]);
		    	 String[] handleAfterUrls = urlsList.toArray(new String[urlsList.size()]);
		    	 
		    	 targetItemDO.setMd5Urls(handleAfterUrlsMd5);
		    	 targetItemDO.setUrls(handleAfterUrls);
		    	 
		    	 logger.info("handle  urls_or_MD5Urls:: mongo_MD5_Urls: {} , handle_after_MD5_urls: {} , mongo_urls :"
		    	 		+ "{} , handle_after_urls: {} , total_Time: {}",md5Urls,handleAfterUrlsMd5,urls,handleAfterUrls,System.currentTimeMillis()-startTime);
		     }else{
		    	 setUrlsAndUrlsMD5(targetItemDO,currrentUrl,currrentUrlMd5);
		     }
	     }else{
	    	 setUrlsAndUrlsMD5(targetItemDO,currrentUrl,currrentUrlMd5);
	     }
	}
	
	private void setUrlsAndUrlsMD5(ItemDO targetItemDO,String currrentUrl,String currrentUrlMd5){
		targetItemDO.setMd5Urls(new String[]{currrentUrlMd5});
		targetItemDO.setUrls(new String[]{currrentUrl});
	}
	
	
	/**
	 * 竞品数据写入mongo
	 * @param docId
	 * @param value
	 * @param url
	 * @param update_time 
	 * @param taskId
	 * @param itemStatus
	 */
	private void writeCtorMongo(String docId, String value, String url, String update_time , String taskId, String itemStatus) {
		try {
			CtorItemDO itemDO = new CtorItemDO();
			Date updateDate = compartiotor_update_time_package(update_time);
			itemDO.setDocId(docId);
			itemDO.setUrlMD5(SpiderStringUtil.md5Encode(url));
			itemDO.setCreateTime(new Date().getTime());
			if(null != updateDate){
				itemDO.setUpdateTime(updateDate.getTime());
			}
			itemDO.setValue(value);
			itemDO.setStatus(itemStatus);
			ctorItemDAO.upsertItem(Long.parseLong(taskId), itemDO);
		} catch (Exception e) {
			logger.error("insert to mongodb Exception...", e);
		}
	}

	/**
	 * 海豚村数据写入mongo
	 * @param docId
	 * @param value
	 * @param taskId
	 * @param itemStatus
	 */
	private void writeHTCMongo(String docId, String value,String itemStatus) {
		try {
			ItemDO itemDO = new ItemDO();
			itemDO.setDocId(docId);
			itemDO.setCreateTime(new Date().getTime());
			itemDO.setValue(value);
			itemDO.setStatus(itemStatus);
			haiTunCunItemDAO.upsertItem(itemDO);
		} catch (Exception e) {
			logger.error("insert to mongodb Exception...", e);
		}
	}
	
	/**
	 * 淘宝数据写入mongo
	 * @param docId
	 * @param value
	 * @param url
	 * @param taskId
	 * @param itemStatus
	 * @param type
	 */
	private void writeTaoBaoMongo(String docId, String value,String url ,String itemStatus,boolean isexist) {
		try {
			ItemDO itemDO = new ItemDO();
			itemDO.setDocId(docId);
			itemDO.setUrlMD5(SpiderStringUtil.md5Encode(url));
			itemDO.setCreateTime(new Date().getTime());
			itemDO.setValue(value);
			itemDO.setStatus(itemStatus);
			if(isexist){//通过isexist标识,做淘宝代购/直购业务操作
				taoBaoItemDAO.upsertTaoBaoZgItem(itemDO);//直购
			}else{
				taoBaoItemDAO.upsertTaoBaoDgItem(itemDO);//代购
			}
		} catch (Exception e) {
			logger.error("insert to mongodb Exception...", e);
		}
	}
	
	
	/**
	 * format compartiotor update time
	 * @param update_time
	 * @return
	 * @throws ParseException 
	 */
	private Date compartiotor_update_time_package(String update_time) throws ParseException {
		Date updateDate = null; 
		DateFormat format = null;
		if(StringUtils.isNotBlank(update_time)){
			try {
				format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				updateDate = format.parse(update_time);
			} catch (java.text.ParseException e) {
				format = new SimpleDateFormat("MM/dd/yyyy");
				try {
					updateDate = format.parse(update_time);
				}catch (java.text.ParseException e2){
					format = new SimpleDateFormat("yyyy-MM-dd HH:mm");
					updateDate = format.parse(update_time);
				}
			}
		}
		return updateDate;
	}

	/**
	 * 清洗完后的item数据,写到queue中
	 * @param item
	 */
	private void cleaningAfterDataToQueue(String item) {
		try {
			BlockingQueue<String> queue = CleaningAfterBolckingQueue.getInstance();
			queue.put(item);
		} catch (Exception e) {
			logger.error("Error while offer queue error", e);
		}
	}
	
	/**
	 * kafka的数据,写到queue中
	 * @param item
	 */
	private void kafkaDataToWriteQueue(String item) {
		try {
			BlockingQueue<String> queue = KafkaItemBolckingQueue.getInstance();
			queue.put(item);
		} catch (Exception e) {
			logger.error("Error while offer queue error", e);
		}
	}
	
	public ItemDAO getItemDAO() {
		return itemDAO;
	}

	public void setItemDAO(ItemDAO itemDAO) {
		this.itemDAO = itemDAO;
	}
	
	public CurrentItemDAO getCurrentItemDAO() {
		return currentItemDAO;
	}

	public void setCurrentItemDAO(CurrentItemDAO currentItemDAO) {
		this.currentItemDAO = currentItemDAO;
	}
	
	public CtorItemDAO getCtorItemDAO() {
		return ctorItemDAO;
	}

	public void setCtorItemDAO(CtorItemDAO ctorItemDAO) {
		this.ctorItemDAO = ctorItemDAO;
	}

	public SpiderKafkaProducer getSpiderKafkaProducer() {
		return spiderKafkaProducer;
	}

	public void setSpiderKafkaProducer(SpiderKafkaProducer spiderKafkaProducer) {
		this.spiderKafkaProducer = spiderKafkaProducer;
	}

	public String getTopic() {
		return topic;
	}

	public void setTopic(String topic) {
		this.topic = topic;
	}
	
	public HaiTunCunItemDAO getHaiTunCunItemDAO() {
		return haiTunCunItemDAO;
	}

	public void setHaiTunCunItemDAO(HaiTunCunItemDAO haiTunCunItemDAO) {
		this.haiTunCunItemDAO = haiTunCunItemDAO;
	}

	public TaoBaoItemDAO getTaoBaoItemDAO() {
		return taoBaoItemDAO;
	}

	public void setTaoBaoItemDAO(TaoBaoItemDAO taoBaoItemDAO) {
		this.taoBaoItemDAO = taoBaoItemDAO;
	}
	
	private CrawlerJSONResult itemDeleteToJson(CrawlerJSONResult cr) {
		ProdUrl prodUrl = cr.getRetbody().getProdUrl();
		if(!Objects.isNull(prodUrl)){
			RetBody body = new RetBody();
			body.setProdUrl(new ProdUrl(prodUrl.getUrl()));
			body.setStock(new Stock(0));
			CrawlerJSONResult jsonResult = new CrawlerJSONResult("OK", 0, body, cr.getTaskId(),
					ItemEnum.DocType.DELETE.toString());
			return jsonResult;
		}
		return null;
	}
	

	private boolean checkItemDataSelf(CrawlerJSONResult cr) {
		try {
			if (Objects.isNull(cr)) {
				logger.error("crawlerResult is null");
				return false;
			}

			if (!cr.isValid()) {
				logger.error("crawlerResult is not valid");
				return false;
			}

			return true;
		} catch (Exception e) {
			logger.error("Error CrawlerJSONResult, docid: {}; exception: {}", cr.getRetbody().getDOCID(), e);
			return false;
		}
	}

	@Override
	public void handleItem(String item) {
		// 1.将商品数据由json字符串形式转换成对象形式
		CrawlerJSONResult crawlerResult = CrawlerJSONResult.buildFrom(item);

		RetBody retbody = crawlerResult.getRetbody();

		// 2.将kafka商品数据写到queue
		// 3.检验商品数据本身是否有效
		//增加竞品商品数据 不参与check
		if(null!=retbody){
			kafkaDataToWriteQueue(item);
			boolean selfCorrect = this.checkItemDataSelf(crawlerResult);
			if (!selfCorrect) {
				 logger.error("item-data is not self-correct -> is delete, item: {}", item);
				 CrawlerJSONResult newCr = this.itemDeleteToJson(crawlerResult);
				 if(!Objects.isNull(newCr)){
					 this.checkItemDataUpdate(newCr);//封装delete类型
				 }
				 return;
			}
		}

		this.checkItemDataUpdate(crawlerResult);
	}
}