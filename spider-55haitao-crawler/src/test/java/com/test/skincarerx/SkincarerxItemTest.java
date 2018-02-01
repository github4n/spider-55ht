package com.test.skincarerx;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.common.gson.bean.Brand;
import com.haitao55.spider.common.gson.bean.LImageList;
import com.haitao55.spider.common.gson.bean.LSelectionList;
import com.haitao55.spider.common.gson.bean.LStyleList;
import com.haitao55.spider.common.gson.bean.Price;
import com.haitao55.spider.common.gson.bean.ProdUrl;
import com.haitao55.spider.common.gson.bean.RetBody;
import com.haitao55.spider.common.gson.bean.Site;
import com.haitao55.spider.common.gson.bean.Sku;
import com.haitao55.spider.common.gson.bean.Stock;
import com.haitao55.spider.common.gson.bean.Title;
import com.haitao55.spider.common.utils.DetailUrlCleaningTool;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.callable.custom.mankind.CrawlerUtils;
import com.haitao55.spider.crawler.utils.Constants;
import com.haitao55.spider.crawler.utils.HttpUtils;

/** 
 * @Description: 
 * @author: zhoushuo
 * @date: 2017年4月18日 下午5:10:16  
 */
public class SkincarerxItemTest extends AbstractSelect{
	
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private static final String domain = "www.skincarerx.com";

	private static final String CSS_BREADS = "ul.breadcrumbs_container li a";
	private static final String CSS_DESCRIPTION = "div.product-info p";
	private static final String CSS_DETAIL = "div#technicaldetails div.product-more-details table tr";
	private static final String CSS_INVENTORY = "span.product-stock-message";
	private static final String CSS_IMAGES = "ul.product-large-view-thumbs>li.n-unit>a";
	
	// 女性的性别标识关键词
	private static final String[] FEMALE_KEY_WORD = { "Women's", "Girls", "Women's Sale", "Women's Final Sale" };
	// 男性的性别标识关键词
	private static final String[] MALE_KEY_WORD = { "Men's", "Boys", "Men's Sale", "Men's Final Sale" };

	public static void main(String[] args) throws Exception {
		SkincarerxItemTest sct = new SkincarerxItemTest();
		Context context = new Context();
		context.setCurrentUrl("https://www.skincarerx.com/ren-relax-with-rose-set-worth-165/11349516.html");
		sct.invoke(context);
	}

	@Override
	public void invoke(Context context) throws Exception {
		String currentUrl = context.getCurrentUrl();
		currentUrl = DetailUrlCleaningTool.getInstance().cleanDetailUrl(currentUrl);
		// String content = super.getInputString(context);
		String content = HttpUtils.get(currentUrl, 30000, 1, null);
		RetBody retbody = new RetBody();
		if (StringUtils.isNotBlank(content)) {
			Document document = Jsoup.parse(content, currentUrl);
			
			String keywords = StringUtils.substringBetween(content, "productID", "productVariationPrice");
			String productID = StringUtils.substringBefore(keywords, ",").replace(":", "").replace("\"", "").replace(" ", "");
			String title = StringUtils.trim(StringUtils.substringBetween(keywords, "productTitle", ",").replace(":", "").replace("\"", ""));
			String productPriceStr = StringUtils.trim(StringUtils.substringBetween(keywords, "productPrice", ",").replace(":", "").replace("\"", "").replace("&#36;", "$"));
			String brand = StringUtils.trim(StringUtils.substringBetween(keywords, "productBrand", ",").replace(":", "").replace("\"", ""));
			String origPriceStr = StringUtils.substringBetween(content, "RRP: <span class=\"strike\">", "</span></p>").replace("&#36;", "$");
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

			float spu_orig_price = CrawlerUtils.getPrice(origPriceStr, currentUrl, logger);
			float spu_sale_price = CrawlerUtils.getPrice(productPriceStr, currentUrl, logger);
			int save = Math.round((1 - spu_sale_price / spu_orig_price) * 100);
			String spu_Unit = CrawlerUtils.getUnit(productPriceStr, currentUrl, logger);
			int spu_stock_status = 0;
			
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
					imgs.add(StringUtils.trimToEmpty(e.absUrl("href")));
				}
			}

			List<LStyleList> l_style_list = new ArrayList<>();
			List<LSelectionList> l_selection_list = new ArrayList<>();
			
			LImageList image_list = CrawlerUtils.getImageList(imgs);

			// 设置retbody
			retbody.setDOCID(docid);
			retbody.setSite(new Site(domain));
			retbody.setProdUrl(new ProdUrl(currentUrl, System.currentTimeMillis(), url_no));
			retbody.setTitle(new Title(title, "", "", ""));
			retbody.setBrand(new Brand(brand, "", "", ""));
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
