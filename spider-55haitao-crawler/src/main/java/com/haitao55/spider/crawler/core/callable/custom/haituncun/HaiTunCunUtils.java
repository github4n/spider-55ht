package com.haitao55.spider.crawler.core.callable.custom.haituncun;

import java.io.FileReader;
import java.io.IOException;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.common.gson.JsonUtils;
import com.haitao55.spider.common.gson.bean.CrawlerJSONResult;
import com.haitao55.spider.common.gson.bean.HaiTunCunRetBody;
import com.haitao55.spider.common.kafka.SpiderKafkaProducer;
import com.haitao55.spider.common.kafka.SpiderKafkaResult;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.core.model.DocType;
import com.haitao55.spider.crawler.service.impl.OutputServiceKafka;

/** 
 * @Description: 海豚村数据获取和解析工具类
 * @author: zhoushuo
 * @date: 2017年2月28日 上午10:59:53  
 */
public class HaiTunCunUtils {
	
	private static Logger logger = LoggerFactory.getLogger(HaiTunCunUtils.class);
	
	private static String url = "http://23.91.97.48:8001/o_exp/index?user=emarsys&pwd=30318e6405fb67b52692f8c2c81fcbd3";
	
	//CSV文件头
    private static final String [] FILE_HEADER = {"item","available","title","link","image","c_image_400","category","price","msrp","brand","description"};

	public static String downloadFileAndReturnFilePath(String url){
		return HttpDownload.download(url);
	}
	
    /**
     * @param fileName
     */
    public static void readCsvFileAndSendMsg(String fileName, OutputServiceKafka outputServiceKafka, Long taskId) {
        FileReader fileReader = null;
        CSVParser csvFileParser = null;
        //创建CSVFormat（header mapping）
        CSVFormat csvFileFormat = CSVFormat.DEFAULT.withHeader(FILE_HEADER);
        try {
            //初始化FileReader object
            fileReader = new FileReader(fileName);
            //初始化 CSVParser object
            csvFileParser = new CSVParser(fileReader, csvFileFormat);
            //使用迭代器方式获取文件内容，避免文件过大时全部加载到内存出现内存溢出问题
            long start = System.currentTimeMillis();
            Iterator<CSVRecord> it = csvFileParser.iterator();
            long end = System.currentTimeMillis();
            logger.info("iterator耗时："+(end - start));
            it.next();//去除标题栏
            long failCount = 0l;
            while(it.hasNext()){
            	CSVRecord record = it.next();
            	String goodId = record.get(FILE_HEADER[0]);
                if(StringUtils.isBlank(goodId)){
                	logger.error("item字段为空,不入库!记录为：{}",JsonUtils.bean2json(record));
                	failCount++;
                	continue;
                }
                if(!checkMsg(record)){
                	logger.error("数据有误,不入库!记录为：{}",JsonUtils.bean2json(record));
                	failCount++;
                	continue;
                }
                HaiTunCunRetBody retBody = new HaiTunCunRetBody();
                retBody.setDocId(SpiderStringUtil.md5Encode(goodId));
                retBody.setItem(goodId);
                retBody.setAvailable(new Boolean(record.get(FILE_HEADER[1])));
                retBody.setTitle(record.get(FILE_HEADER[2]));
                retBody.setLink(record.get(FILE_HEADER[3]));
                retBody.setImage(record.get(FILE_HEADER[4]));
                retBody.setC_image_400(record.get(FILE_HEADER[5]));
                retBody.setCategory(record.get(FILE_HEADER[6]));
                retBody.setPrice(new Double(record.get(FILE_HEADER[7])));
                retBody.setMsrp(new Double(record.get(FILE_HEADER[8])));
                retBody.setBrand(record.get(FILE_HEADER[9]));
                retBody.setDescription(record.get(FILE_HEADER[10]));
                CrawlerJSONResult jsonResult = new CrawlerJSONResult("OK", 0, retBody, taskId+"",DocType.INSERT.toString());
    			String msg = jsonResult.parseTo();
    			SpiderKafkaProducer producer = outputServiceKafka.getProducer();
    			String topic = outputServiceKafka.getTopic();
    			if(StringUtils.isNotBlank(msg)){
    				for(int j =0 ; j < 20; j++){
    					try {
    						SpiderKafkaResult result = producer.sendbyCallBack(topic, msg);
    						if(result != null){
    							logger.info("send a message offset :{}, message:{}", result.getOffset(), msg);
    							break;
    						}
    					} catch (Exception e) {
    						if(e instanceof ConnectException
    								|| e instanceof NoRouteToHostException
    								|| e instanceof SocketTimeoutException
    								|| e instanceof UnknownHostException
    								|| e instanceof SocketException){
    							try {
    								Thread.sleep(10000);
    							} catch (InterruptedException e1) {
    								e1.printStackTrace();
    							}
    							continue;
    						}
    						logger.error("haituncun send message to topic {},exception {}", topic, e);
    					}
    					logger.info("send a message failed, msg:{}", msg);
    				}
    				
    			} else {
    				logger.warn("Haituncun send message to topic {},msg is null", topic);
    			}
            }
            logger.info("由于数据校验不通过，过滤了{}条数据！", failCount);
            //CSV文件records
/*            List<CSVRecord> csvRecords = csvFileParser.getRecords(); 
            for (int i = 1; i < csvRecords.size(); i++) {
                CSVRecord record = csvRecords.get(i);
                String goodId = record.get(FILE_HEADER[0]);
                if(StringUtils.isBlank(goodId)){
                	logger.error("第{}条记录item字段为空,不入库!",i);
                	continue;
                }
                if(!checkMsg(record)){
                	logger.error("第{}条记录数据有误,不入库!",i);
                	continue;
                }
                HaiTunCunRetBody retBody = new HaiTunCunRetBody();
                retBody.setDocId(SpiderStringUtil.md5Encode(goodId));
                retBody.setItem(goodId);
                retBody.setAvailable(new Boolean(record.get(FILE_HEADER[1])));
                retBody.setTitle(record.get(FILE_HEADER[2]));
                retBody.setLink(record.get(FILE_HEADER[3]));
                retBody.setImage(record.get(FILE_HEADER[4]));
                retBody.setC_image_400(record.get(FILE_HEADER[5]));
                retBody.setCategory(record.get(FILE_HEADER[6]));
                retBody.setPrice(new Double(record.get(FILE_HEADER[7])));
                retBody.setMsrp(new Double(record.get(FILE_HEADER[8])));
                retBody.setBrand(record.get(FILE_HEADER[9]));
                retBody.setDescription(record.get(FILE_HEADER[10]));
                CrawlerJSONResult jsonResult = new CrawlerJSONResult("OK", 0, retBody, taskId+"",DocType.INSERT.toString());
    			String msg = jsonResult.parseTo();
    			SpiderKafkaProducer producer = outputServiceKafka.getProducer();
    			String topic = outputServiceKafka.getTopic();
    			if(StringUtils.isNotBlank(msg)){
    				for(int j =0 ; j < 20; j++){
    					try {
    						SpiderKafkaResult result = producer.sendbyCallBack(topic, msg);
    						if(result != null){
    							logger.info("send a message offset :{}, message:{}", result.getOffset(), msg);
    							break;
    						}
    					} catch (Exception e) {
    						if(e instanceof ConnectException
    								|| e instanceof NoRouteToHostException
    								|| e instanceof SocketTimeoutException
    								|| e instanceof UnknownHostException
    								|| e instanceof SocketException){
    							try {
    								Thread.sleep(10000);
    							} catch (InterruptedException e1) {
    								e1.printStackTrace();
    							}
    							continue;
    						}
    						logger.error("haituncun send message to topic {},exception {}", topic, e);
    					}
    					logger.info("send a message failed, msg:{}", msg);
    				}
    				
    			} else {
    				logger.warn("Haituncun send message to topic {},msg is null", topic);
    			}
            }*/
        } 
        catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fileReader.close();
                csvFileParser.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
    
    private static boolean checkMsg(CSVRecord record){
    	if(StringUtils.isBlank(record.get(FILE_HEADER[1]))){
    		logger.error("available is null");
        	return false;
        }
    	if(StringUtils.isBlank(record.get(FILE_HEADER[2]))){
    		logger.error("title is null");
        	return false;
        }
    	if(StringUtils.isBlank(record.get(FILE_HEADER[3]))){
    		logger.error("link is null");
        	return false;
        }
    	if(StringUtils.isBlank(record.get(FILE_HEADER[4]))){
    		logger.error("image is null");
        	return false;
        }
    	if(StringUtils.isBlank(record.get(FILE_HEADER[5]))){
    		logger.error("c_image_400 is null");
        	return false;
        }
    	if(StringUtils.isBlank(record.get(FILE_HEADER[7]))){
    		logger.error("price is null");
        	return false;
        }
    	if(StringUtils.isBlank(record.get(FILE_HEADER[8]))){
    		logger.error("msrp is null");
        	return false;
        }
    	return true;
    }
	
	public static void main(String[] args) throws IOException {
		HttpDownload.download(url);
	}
}
