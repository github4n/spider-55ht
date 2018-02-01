package com.haitao55.spider.crawler.core.callable.custom.amazon_jp;
import java.util.Map;
import java.util.Random;

import org.apache.commons.collections.MapUtils;
import org.apache.http.HttpHost;
import org.apache.http.client.fluent.Executor;
import org.apache.http.client.fluent.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.crawler.utils.Constants;

public class LumHttpClient {
    private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	public String session_id = Integer.toString(new Random().nextInt(Integer.MAX_VALUE));

	public LumHttpClient() {
		
	}

	public String request(String url,Map<String,Object> headers){
	    for(int i =0 ; i < 3; i++){
	    	try{
		    	HttpHost proxy = new HttpHost("10.128.0.2", 24000);
			    Request req = Request.Get(url);
			    if(MapUtils.isNotEmpty(headers)){
			        headers.forEach( (k,v) -> {
			        	if(v == null){
			        		return ;
			        	}
			        	req.addHeader(k, v.toString());
				    });
			    }
			    req.userAgent("Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/55.0.2883.75 Safari/537.36");
			    logger.info(" url {} ,retry count {} ");
			    return Executor.newInstance().execute(req.viaProxy(proxy)).returnContent().asString();
		    }catch(Exception e){
		    	e.printStackTrace();
		    }
	    }
        return null;
	}

	
	public static void main(String[] args) {
		String url = "https://www.amazon.co.jp/dp/B00YQU3HSW/";
		LumHttpClient client = new LumHttpClient();
		long start = System.currentTimeMillis();
		String result = client.request(url,null);
		long end = System.currentTimeMillis();
		System.out.println(end - start);
	}
}
