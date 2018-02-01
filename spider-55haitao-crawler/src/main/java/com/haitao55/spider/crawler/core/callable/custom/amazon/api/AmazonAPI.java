package com.haitao55.spider.crawler.core.callable.custom.amazon.api;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.common.gson.bean.RetBody;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.utils.Constants;


public class AmazonAPI extends AbstractSelect{
	
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private static AmazonAWSKeyPool awsKeyPool;
	//private static final String defaultKeyId = "AKIAI77X7X5JVEZ52ZCA";
	//private static final String defaultSecretKey = "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";
	//private static final String defaultAssociateTag = "55haitao";
	
	
	
	static {
		try {
			awsKeyPool = new AmazonAWSKeyPool();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void invoke(Context context) throws Exception {
		//assemble a request
		ParentResult result = null;
		for(int i =0 ; i < 5; i++){
			try{
			    AWSKey key = awsKeyPool.pollKey();
	            /*if(i < 2){
	                key = awsKeyPool.pollKey();
	            } else {
	                key = new AWSKey(defaultKeyId, defaultSecretKey, defaultAssociateTag);
	            }*/
	            result = AmazonUtils.getParentAsin(context, key, 30000);
	            if(result != null){
	                break;
	            }
			}catch(Throwable e){
			    e.printStackTrace();
			}
		}
		RetBody rebody = null;
		if(result != null && StringUtils.isNotBlank(result.getParentAsin())){
			for(int i =0 ; i < 5; i++){
				try{
				    AWSKey key = awsKeyPool.pollKey();
	                /*if(i < 2){
	                    key = awsKeyPool.pollKey();
	                } else {
	                    key = new AWSKey(defaultKeyId, defaultSecretKey, defaultAssociateTag);
	                }*/
	                rebody = AmazonUtils.getRebody(result, key, 30000);
	                if(rebody != null){
	                    break;
	                }
				}catch(Throwable e){
				    e.printStackTrace();
				}
			}
			
		}
		logger.info("amazon-api rebody {}",rebody==null?null:rebody.parseTo());
		if(rebody != null){
			setAmazonOutput(context, rebody);
			//System.out.println(rebody.parseTo());
		}
	}
	
	public static void main(String[] args) throws Exception {
		AmazonAPI api = new AmazonAPI();
		Context context = new Context();
		//https://www.amazon.com/dp/B004X4Y9MO/
		context.setUrl(new Url("https://www.amazon.com/dp/B06WVGWFKB/"));
		api.invoke(context);
		/*String content = HttpUtils.get("https://www.amazon.de/dp/B01IHS4IIQ/?th=1&psc=1");
		Document document = Jsoup.parse(content);
		//div#detailBullets_feature_div > ul > li > span
		Elements es = document.select("div#detailBullets_feature_div > ul > li > span");
		if(es != null && es.size() > 0){
			for(Element e : es){
				System.out.println(e.text());
			}
		}*/

	}
	

}
