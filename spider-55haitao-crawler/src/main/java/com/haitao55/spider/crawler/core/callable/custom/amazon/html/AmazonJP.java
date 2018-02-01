package com.haitao55.spider.crawler.core.callable.custom.amazon.html;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.reflect.TypeToken;
import com.haitao55.spider.common.gson.JsonUtils;
import com.haitao55.spider.common.gson.bean.RetBody;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.callable.context.Keyword;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.utils.Constants;

/**
 * @ClassName: Amazon_JP
 * @Description: 解析html内容并组装json数据
 * @author denghuan
 *
 */
public class AmazonJP extends AbstractSelect {
	
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private final String ITEM_URL_TEMPLATE = "https://www.amazon.co.jp/dp/#itemId#/?th=1&psc=1";
	
	@Override
	public void invoke(Context context) throws Exception {
		String content = this.getInputString(context);
		context.get(Keyword.DOC.getValue());
		Map<String,Url> skuUrls = new HashMap<String,Url>();
		if (StringUtils.isNotBlank(content)) {
			String skuData = StringUtils.substringBetween(content, "window.DetailPage", "</script>");
			if(StringUtils.isNotBlank(skuData)){//有sku的情況
				String asin_variation_values = StringUtils.substringBetween(skuData, "\"asin_variation_values\":", ",\"contextMetaData\"");
				Type typeMapMap = new TypeToken<Map<String,Map<String,String>>>(){}.getType();
				Map<String,Map<String,String>> asin_variation_valuesMapMap =  JsonUtils.json2bean(asin_variation_values, typeMapMap);
				if(asin_variation_valuesMapMap != null && asin_variation_valuesMapMap.size() > 0 ){
					for(Map.Entry<String,Map<String,String>> entry : asin_variation_valuesMapMap.entrySet()){
						String skuId = entry.getKey();
						Url skuUrl = new Url(ITEM_URL_TEMPLATE.replace("#itemId#", skuId));
						skuUrl.setTask(context.getUrl().getTask());
						skuUrls.put(skuId,skuUrl);
					}
				}
			}  else {//無sku的情況
				String skuId = StringUtils.substringBetween(context.getCurrentUrl(), "/dp/", "/");
				Url skuUrl = new Url(ITEM_URL_TEMPLATE.replace("#itemId#", skuId));
				skuUrl.setTask(context.getUrl().getTask());
				skuUrls.put(skuId, skuUrl);
			}
			
		}
		Map<String,SkuBean> skuResult = new AmazonPriceStockHandler().process(skuUrls);
		if(skuResult == null || skuResult.size() == 0 ){//無自營商品 或者失敗
			logger.info("Amazon_JP sku price and stock is invalid,url {} skuId {}",context.getCurrentUrl());
			return;//無價格庫存 丟棄
		}
		AmazonJPParser defaultAmazon = new AmazonJPParser(skuResult);
		RetBody ret = defaultAmazon.retboby(context);
		setOutput(context, ret.parseTo());
	}
	public static void main(String[] args) {
//		Map<String,Url> skuUrls = new HashMap<String,Url>();
//		Url skuUrl =  new Url("https://www.amazon.com/dp/B00OL1469U/");
//		skuUrls.put("B015WDQ2T2",skuUrl);
//		Map<String,SkuBean>  skuResult = handler.process(skuUrls);
		//_AmazonJP defaultAmazon = new _AmazonJP(skuResult);
	}
}
