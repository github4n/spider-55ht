package com.haitao55.spider.crawler.core.callable.custom.amazon.html;

import java.util.List;
import java.util.Map;

import com.haitao55.spider.common.gson.bean.Brand;
import com.haitao55.spider.common.gson.bean.LImageList;
import com.haitao55.spider.common.gson.bean.Price;
import com.haitao55.spider.common.gson.bean.ProdUrl;
import com.haitao55.spider.common.gson.bean.Site;
import com.haitao55.spider.common.gson.bean.Sku;
import com.haitao55.spider.common.gson.bean.Stock;
import com.haitao55.spider.common.gson.bean.Title;
import com.haitao55.spider.crawler.core.callable.context.Context;

public interface ItemParser {
	
	public String docID(Context context);
	
	public Site site(Context context);
	
	public ProdUrl prodUrl(Context context);
	
	public Title title(Context context);
	
	public Price price(Context context);
	
	public Stock stock(Context context);
	
	public Brand brand(Context context);
	
	public List<String> breadCrumb(Context context);
	
	public List<String> category(Context context);
	
	public LImageList image(Context context);
	
	public Map<String, Object> properties(Context context);
	
	public Map<String, Object> featureList(Context context);
	
	public Map<String, Object> description(Context context);
	
	public Sku sku(Context context);

}
