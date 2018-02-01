package com.haitao55.spider.crawler.core.callable.custom.amazon_jp;

import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.common.gson.bean.RetBody;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.callable.custom.amazon.api.AWSKey;
import com.haitao55.spider.crawler.core.callable.custom.amazon.api.ParentResult;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.utils.Constants;


public class AmazonJPAPI extends AbstractSelect{
	
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private static final String defaultKeyId = "AKIAJTNFWFMZUENHJ2ZQ";
	private static final String defaultSecretKey = "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";
	private static final String defaultAssociateTag = "55haitao";
	

	@Override
	public void invoke(Context context) throws Exception {
		//assemble a request
		ParentResult result = null;
		for(int i =0 ; i < 5; i++){
			try{
			    AWSKey key  = new AWSKey(defaultKeyId, defaultSecretKey, defaultAssociateTag);
	            result = AmazonJPUtils.getParentAsin(context, key, 30000);
	            if(result != null){
	                break;
	            }
			}catch(Throwable e){
			    e.printStackTrace();
			    TimeUnit.SECONDS.sleep(2);
			}
		}
		RetBody rebody = null;
		if(result != null && StringUtils.isNotBlank(result.getParentAsin())){
			for(int i =0 ; i < 5; i++){
				try{
				    AWSKey key  = new AWSKey(defaultKeyId, defaultSecretKey, defaultAssociateTag);
	                rebody = AmazonJPUtils.getRebody(result, key, 30000);
	                if(rebody != null){
	                    break;
	                }
				}catch(Throwable e){
				    e.printStackTrace();
				    TimeUnit.SECONDS.sleep(2);
				}
			}
			
		}
		if(rebody != null){
			setOutput(context, rebody);
			logger.info("amazon-jp-api rebody {}",rebody==null?null:rebody.parseTo());
			System.out.println(rebody.parseTo());
		}
	}
	
	public static void main(String[] args) throws Exception {
		AmazonJPAPI api = new AmazonJPAPI();
		Context context = new Context();
		//https://www.amazon.com/dp/B004X4Y9MO/
		context.setUrl(new Url("https://www.amazon.co.jp/dp/B0197V54TO/"));
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
