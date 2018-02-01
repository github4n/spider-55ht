package com.haitao55.spider.crawler.core.callable.custom.puma;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.stream.Stream;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.haitao55.spider.common.http.Crawler;
import com.haitao55.spider.common.http.HttpException;
import com.haitao55.spider.common.http.HttpMethod;
import com.haitao55.spider.crawler.common.cache.ProxyCache;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.core.model.Proxy;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.utils.JsoupUtils;


public class PumaCallaber implements Callable<JSONObject> {
	private static final Logger logger = LoggerFactory.getLogger(PumaCallaber.class);
	
	private JSONObject paramJsonObject;
	private Url url;

	public PumaCallaber(JSONObject paramJsonObject , Url url) {
		super();
		this.paramJsonObject = paramJsonObject;
		this.url = url;
	}

	public PumaCallaber() {
		super();
	}


	@Override
	public JSONObject call(){
		JSONObject jsonObject = new JSONObject();
		
		String address = paramJsonObject.getString("url");
		try {
			String content = crawler_result(url,address);
			
			Document doc = JsoupUtils.parse(content);

			String colorVal = doc.select(".selected-value span").text();
			
			//skuId
			String skuId = StringUtils.substringBetween(content, "productID\">", "</");
			//stock status flag
			int stock_status = 0;
			String stock_status_flag = doc.select(".in-stock-msg").text();
			if(StringUtils.equalsIgnoreCase(stock_status_flag, "In Stock")){
				stock_status = 1;
			}
			
			String salePrice = doc.select(".price-sales").text();
			String origPrice = doc.select(".price-standard").text().trim();
			
			if(StringUtils.isNotBlank(salePrice)){
				salePrice = salePrice.replaceAll("[$,]", "");
			}
			if(StringUtils.isBlank(origPrice)){
				origPrice = salePrice;
			}
			origPrice = origPrice.replaceAll("[$,]", "");
			
			//images
			Elements elements = doc.select(".mainimage");
			List<Image> images = new ArrayList<Image>();
			if(CollectionUtils.isNotEmpty(elements)){
				for (Element element : elements) {
					String image_url = element.attr("src");
					if(StringUtils.isNotBlank(image_url)){
						images.add(new Image(image_url));
					}
				}
			}
			
			Elements esImgs = doc.select(".slick-carousel img");
			if(CollectionUtils.isNotEmpty(esImgs)){
				for (Element element : esImgs) {
					String image_url = element.attr("data-original");
					if(StringUtils.isNotBlank(image_url)){
						images.add(new Image(image_url));
					}
				}
			}
			
			List<String> sizeList = Lists.newArrayList();
			Elements es = doc.select("ul.size-swatches li.emptyswatch");
			for(Element e : es){
				String value = e.text();
				if(StringUtils.isNotBlank(value)){
					sizeList.add(value);
				}
			}
			
			//return jsonObject
			jsonObject.put("color", colorVal);
			jsonObject.put("skuId", skuId);
			jsonObject.put("stock_status", stock_status);
			jsonObject.put("sale_price", salePrice);
			jsonObject.put("orign_price", origPrice);
			jsonObject.put("images", images);
			jsonObject.put("sizes", sizeList);
			
		} catch (HttpException | IOException e) {
			logger.error("PumaCallaber request url error , url: {} ,  exception:{} ",address,e);
		}
		
		return jsonObject;
		
	}

	/**
	 * 线上爬取
	 * @param url
	 * @param path
	 * @return
	 * @throws ClientProtocolException
	 * @throws HttpException
	 * @throws IOException
	 */
	private String crawler_result(Url url, String path) throws ClientProtocolException, HttpException, IOException {
		String content = StringUtils.EMPTY;
		String proxyRegionId = url.getTask().getProxyRegionId();
		if(StringUtils.isBlank(proxyRegionId)){
			content = Crawler.create().timeOut(30000).url(path).header(getHeaders()).method(HttpMethod.GET.getValue())
					.resultAsString();
		}else{
			Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
			String proxyAddress=proxy.getIp();
			int proxyPort=proxy.getPort();
			content = Crawler.create().timeOut(30000).url(path).header(getHeaders()).method(HttpMethod.GET.getValue())
					.proxy(true).proxyAddress(proxyAddress).proxyPort(proxyPort).resultAsString();
		}
		 return content;
	}
	
	private static Map<String,Object> getHeaders(){
		final Map<String, Object> headers = new HashMap<String, Object>();
		headers.put("User-Agent",
				"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/58.0.3029.81 Chrome/58.0.3029.81 Safari/537.36");
		headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		headers.put("Upgrade-Insecure-Requests", "1");
		headers.put("Host", "us.puma.com");
		headers.put("Cookie", "dw=1; dw=1; __cfduid=dd9aeb19ede1a78ab26e812725fb40d4d1501138055; cqcid=ab02eBCq22LWZLtlTfLbCbHTc1; dwanonymous_7254072e2668c23dc3bf6cca213a6657=ab02eBCq22LWZLtlTfLbCbHTc1; AMCVS_C2D31CFE5330AFE50A490D45%40AdobeOrg=1; TruView_visitor=ae697c6c-0b93-42e2-ad81-28d947452af0; TruView_uab=46; TruView_session=7641de39-5d60-4684-87ff-d9e3834ff8a8; pt_s_3cc35021=vt=1502864903926&cad=; pt_3cc35021=uid=bqMktSrlPWspsinhQo1wIA&nid=0&vid=ZdPJTPxH1oj1GPXYHtF3qA&vn=2&pvn=1&sact=1502864905644&to_flag=0&pl=GJNBbH9ugujz46Ywo1oJVQ*pt*1502864903926; TruView_tssession=1502867314006; dw=1; liveagent_oref=; liveagent_sid=5e3cc0b0-3e2c-48b7-9adc-1bd1598adf1c; liveagent_vc=2; liveagent_ptid=5e3cc0b0-3e2c-48b7-9adc-1bd1598adf1c; PopupFlag=Puma True; _blka_v=0ae705f1-a5fd-463a-8194-ec31c4097f00; _blka_uab=83; _CT_RS_=Recording; _blka_lpd=10; _blka_lt=y; _blka_t=y; _blka_pd=2; dwac_bc6ZEiaaieGqMaaaddtsaoIDP5=ZL0ztJXzJVkRLC5CFuP5M-bZmiMOsctHu80%3D|dw-only|||USD|false|US%2FEastern|true; sid=ZL0ztJXzJVkRLC5CFuP5M-bZmiMOsctHu80; dwsid=fD6xrcGWSaJAzu5bWlZFcP5aGDpa1Ke86i9-EnecAxgi6LmQNT058lX_sAYPiCCqEfsMrBsW29S4I-Usp5qblw==; AMCV_C2D31CFE5330AFE50A490D45%40AdobeOrg=-1176276602%7CMCIDTS%7C17429%7CMCMID%7C32783341557131236263529480666784243745%7CMCAAMLH-1506393649%7C9%7CMCAAMB-1506404397%7CNRX38WO0n5BH8Th-nqAG_A%7CMCOPTOUT-1505806797s%7CNONE%7CMCAID%7C2C3F736C0519519B-4000060D6006A946;");
		// headers.put("Cookie", setCookie());
		return headers;
	}
	
	public static void main(String[] args) {
		List<String> list = new ArrayList<>();
		list.add("2");
		list.add("1");
		list.add("4");
		String[] attr = list.toArray(new String[list.size()]);
        Stream.of(attr)  
        .sorted((s1, s2) -> {  
            System.out.printf("sort: %s; %s\n", s1, s2);  
            return s1.compareTo(s2);  
        })  
        .forEach(s -> {
        	System.out.println(s);
        }); 
        
        list.forEach(c ->{
        	System.out.println(c);
        });
	}
	
}
