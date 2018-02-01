package com.test.pixiemarket;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.haitao55.spider.common.gson.bean.Brand;
import com.haitao55.spider.common.gson.bean.LImageList;
import com.haitao55.spider.common.gson.bean.LSelectionList;
import com.haitao55.spider.common.gson.bean.LStyleList;
import com.haitao55.spider.common.gson.bean.Price;
import com.haitao55.spider.common.gson.bean.ProdUrl;
import com.haitao55.spider.common.gson.bean.RetBody;
import com.haitao55.spider.common.gson.bean.Selection;
import com.haitao55.spider.common.gson.bean.Site;
import com.haitao55.spider.common.gson.bean.Sku;
import com.haitao55.spider.common.gson.bean.Stock;
import com.haitao55.spider.common.gson.bean.Title;
import com.haitao55.spider.common.utils.CurlCrawlerUtil;
import com.haitao55.spider.common.utils.DetailUrlCleaningTool;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.callable.custom.mankind.CrawlerUtils;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.utils.Constants;
import com.haitao55.spider.crawler.utils.HttpUtils;

/** 
 * @Description: 
 * @author: zhoushuo
 * @date: 2017年4月19日 下午4:17:06  
 */
public class PixiemarketItemTest extends AbstractSelect {
	
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private static final String domain = "www.pixiemarket.com";

	private static final String CSS_TITLE = "div.product-name>span.h1";
	private static final String CSS_BREADS = "div.breadcrumbs li a";
	private static final String CSS_DESCRIPTION = "div.tab-content div.std p";
	private static final String CSS_DETAIL = "not require";
	private static final String CSS_INVENTORY = "div.extra-info p.in-stock span.value";
	private static final String CSS_IMAGES = "div.more-views ul.product-image-thumbs li a img";
	
	// 女性的性别标识关键词
	private static final String[] FEMALE_KEY_WORD = { "Women's", "Girls", "Women's Sale", "Women's Final Sale" };
	// 男性的性别标识关键词
	private static final String[] MALE_KEY_WORD = { "Men's", "Boys", "Men's Sale", "Men's Final Sale" };

	public static void main(String[] args) throws Exception {
		PixiemarketItemTest sct = new PixiemarketItemTest();
		Context context = new Context();
		context.setCurrentUrl("https://www.pixiemarket.com/dresses/marigold-velvet-tie-midi-dress.html");
		context.setCurrentUrl("https://www.pixiemarket.com/collections/tops/products/tess-fishnet-off-the-shoulder-sweatshirt");
//		context.setCurrentUrl("http://www.pixiemarket.com/bottoms/sydney-black-snap-button-side-pants-15-off.html");
		sct.invoke(context);
	}

	@Override
	public void invoke(Context context) throws Exception {
		String currentUrl = context.getCurrentUrl();
		currentUrl = DetailUrlCleaningTool.getInstance().cleanDetailUrl(currentUrl);
		// String content = super.getInputString(context);
		String content = CurlCrawlerUtil.get(currentUrl);//HttpUtils.get(currentUrl, 30000, 1, null);
		RetBody retbody = new RetBody();
		if (StringUtils.isNotBlank(content)) {
			Document document = Jsoup.parse(content, currentUrl);
			
			String title = CrawlerUtils.setTitle(document, CSS_TITLE, currentUrl, logger);
			String productID = CrawlerUtils.getProductId(currentUrl);
			if (StringUtils.isBlank(productID)) {
				logger.error("get productID error");
				return;
			}
			if (StringUtils.isBlank(title)) {
				logger.error("get title error");
				return;
			}
			String docid = SpiderStringUtil.md5Encode(domain + productID);
			String url_no = SpiderStringUtil.md5Encode(currentUrl);

			// 设置面包屑和类别
			List<String> breads = new ArrayList<String>();
			List<String> categories = new ArrayList<String>();
			CrawlerUtils.setBreadAndCategory(breads, categories, document, CSS_BREADS, title);

			// description and feature
			Map<String, Object> featureMap = new HashMap<String, Object>();
			Map<String, Object> descMap = new HashMap<String, Object>();
			CrawlerUtils.setDescription(featureMap, descMap, document, CSS_DESCRIPTION, CSS_DETAIL);

			float spu_orig_price = 0l;
			float spu_sale_price = 0l;
			String spu_Unit = "";
			int spu_stock_status = 0;
			
			Elements tempNowPrice = document.select("div.product-shop div.price-info div.price-box span.regular-price span.price");
			if(CollectionUtils.isEmpty(tempNowPrice))
				tempNowPrice = document.select("div.product-shop div.price-info div.price-box p.special-price span.price");
			if(CollectionUtils.isNotEmpty(tempNowPrice)){
				spu_sale_price = CrawlerUtils.getPrice(tempNowPrice.get(0).text().trim(), currentUrl, logger);
				spu_Unit = CrawlerUtils.getUnit(tempNowPrice.get(0).text().trim(), currentUrl, logger);
			}
			Elements tempOldPrice = document.select("div.product-shop div.price-info div.price-box p.old-price span.price");
			if(CollectionUtils.isNotEmpty(tempOldPrice)){
				spu_orig_price = CrawlerUtils.getPrice(tempOldPrice.get(0).text().trim(), currentUrl, logger);
			} else {
				spu_orig_price = spu_sale_price;
			}
			int save = Math.round((1 - spu_sale_price / spu_orig_price) * 100);
			
			//设置spu库存
			Elements es = document.select(CSS_INVENTORY);
			if(CollectionUtils.isNotEmpty(es)){
				String inventoryMsg = StringUtils.trim(es.get(0).text());
				if(StringUtils.containsIgnoreCase(inventoryMsg, "In stock"))
					spu_stock_status = 1;
			}

			Map<String, Object> properties = new HashMap<>();
			// 设置性别
			CrawlerUtils.setGender(properties, categories, MALE_KEY_WORD, FEMALE_KEY_WORD);

			// 设置图片
			List<String> imgs = new ArrayList<>();
			Elements epictures = document.select(CSS_IMAGES);
			if(CollectionUtils.isNotEmpty(epictures)){
				for(Element e : epictures) {
					imgs.add(StringUtils.trimToEmpty(e.absUrl("src")));
				}
			}

			List<LStyleList> l_style_list = new ArrayList<>();
			List<LSelectionList> l_selection_list = new ArrayList<>();
	
			String keywords = StringUtils.substringBetween(content, "var spConfig = new Product.Config(", ",\"chooseText");
			if(StringUtils.isNotBlank(keywords)){
				JSONObject Json = JSONObject.parseObject(keywords+"}");
				System.out.println(Json.toJSONString());
				productID = Json.getString("productId");
				docid = SpiderStringUtil.md5Encode(domain + productID);
				
				JSONObject skusJson = Json.getJSONObject("attributes").getJSONObject("965");
				if(skusJson == null) {
					logger.error("get size error ang url is {}", currentUrl);
					return;
				}
				String attrbute = skusJson.getString("code");
				JSONArray skusArray = skusJson.getJSONArray("options");
				if(skusArray != null && skusArray.size() != 0){
					int count = 0;
					int len = skusArray.size();
					for(int i=0; i<len; i++){
						int sku_stock_status = 0;
						int sku_stock_number = 0;
						String style_switch_img = "";
						String size = skusArray.getJSONObject(i).getString("label");
						String skuid = skusArray.getJSONObject(i).getString("id");
						
						if(spu_stock_status == 1)
							sku_stock_status = 1;
						
						LSelectionList lsl = new LSelectionList();
						List<Selection> selections = new ArrayList<>();
						Selection selection = new Selection(0, size, attrbute);
						selections.add(selection);
						lsl.setGoods_id(skuid);
						lsl.setPrice_unit(spu_Unit);
						lsl.setSale_price(spu_sale_price);
						lsl.setOrig_price(spu_orig_price);
						if(spu_orig_price < spu_sale_price)
							lsl.setOrig_price(spu_sale_price);
						lsl.setStyle_id("default");
						lsl.setStock_number(sku_stock_number);
						lsl.setStock_status(sku_stock_status);
						lsl.setSelections(selections);
						l_selection_list.add(lsl);
						
						LStyleList ll = new LStyleList();
						if(count == 0){
							ll.setStyle_switch_img(style_switch_img);
							ll.setGood_id(skuid);
							ll.setStyle_id("default");
							ll.setStyle_cate_id(0);
							ll.setStyle_cate_name("color");
							ll.setStyle_name("default");
							ll.setDisplay(true);
							l_style_list.add(ll);
						}
						count++;
					}
				}
			}
			
			LImageList image_list = CrawlerUtils.getImageList(imgs);
			context.getUrl().getImages().put(productID, CrawlerUtils.convertToImageList(imgs));

			// 设置retbody
			retbody.setDOCID(docid);
			retbody.setSite(new Site(domain));
			retbody.setProdUrl(new ProdUrl(currentUrl, System.currentTimeMillis(), url_no));
			retbody.setTitle(new Title(title, "", "", ""));
			retbody.setBrand(new Brand("", "", "", ""));
			retbody.setBreadCrumb(breads);
			retbody.setCategory(categories);
			retbody.setFeatureList(featureMap);
			retbody.setDescription(descMap);
			retbody.setImage(image_list);
			retbody.setPrice(new Price(spu_orig_price, save, spu_sale_price,
			 spu_Unit));
			retbody.setStock(new Stock(spu_stock_status));
			retbody.setSku(new Sku(l_selection_list, l_style_list));
			retbody.setProperties(properties);
		}
		System.err.println(retbody.parseTo());
		
	}
}
