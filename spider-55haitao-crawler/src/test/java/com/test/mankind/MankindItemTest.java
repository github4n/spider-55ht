package com.test.mankind;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringEscapeUtils;
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
import com.haitao55.spider.common.gson.bean.Picture;
import com.haitao55.spider.common.gson.bean.Price;
import com.haitao55.spider.common.gson.bean.ProdUrl;
import com.haitao55.spider.common.gson.bean.RetBody;
import com.haitao55.spider.common.gson.bean.Selection;
import com.haitao55.spider.common.gson.bean.Site;
import com.haitao55.spider.common.gson.bean.Sku;
import com.haitao55.spider.common.gson.bean.Stock;
import com.haitao55.spider.common.gson.bean.Title;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.utils.Constants;
import com.haitao55.spider.crawler.utils.HttpUtils;
import com.haitao55.spider.crawler.utils.Native2AsciiUtils;

/** 
 * @Description: 
 * @author: zhoushuo
 * @date: 2016年11月23日 下午2:20:30  
 */
public class MankindItemTest extends AbstractSelect{
	
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private static final String domain = "www.mankind.co.uk";

	private static final String CSS_BREADS = "li.breadcrumbs_item>a";
	private static final String CSS_DESCRIPTION = "div.product-info p";
	private static final String CSS_DETAIL = "div.product-more-details tr";
	private static final String CSS_IMAGE_LIST = "ul.list-menu>li.list-item>a";
	private static final String CSS_IMAGE = "div.main-product-image>a";
	private static final String CSS_INVENTORY = "span.product-stock-message";
	
	public static void main(String[] args) throws Exception {
		MankindItemTest mit = new MankindItemTest();
		Context context = new Context();
		context.setCurrentUrl("http://www.mankind.co.uk/kerastase-cristalliste-fine-hair-duo/10811005.html");
//		context.setCurrentUrl("http://www.mankind.co.uk/decleor-excellence-de-l-age-regenerating-eye-lip-cream-15ml/10360092.html");
//		context.setCurrentUrl("http://www.mankind.co.uk/zirh-botanical-pre-shave-oil-30ml/10366265.html");
		context.setCurrentUrl("http://www.mankind.co.uk/men-rock-awakening-beard-care-kit-beardy-beloved-beard-shampoo-beard-balm-moustache-wax-beard-comb-gift-box/11169750.html");
		context.setCurrentUrl("http://www.mankind.co.uk/roger-gallet-gingembre-rouge-fresh-fragrant-water-spray-50ml/11259351.html");
		mit.invoke(context);
	}

	@Override
	public void invoke(Context context) throws Exception {
		// String content = super.getInputString(context);
		String content = HttpUtils.get(context.getCurrentUrl(), 30000, 1, null);
		RetBody retbody = new RetBody();
		if (StringUtils.isNotBlank(content)) {
			Document document = Jsoup.parse(content, context.getCurrentUrl());
			
			String key_words = StringUtils.substringBetween(content, "var siteObj", "</script>");
			String title = CrawlerUtils.setTitle(StringUtils.substringBetween(key_words, "productTitle: \"", "\","), context.getCurrentUrl(), logger);
			String productID = StringUtils.substringBetween(key_words, "productID: \"", "\",");
			if(StringUtils.isBlank(productID))
				productID = UUID.randomUUID().toString();
			String priceStr = StringUtils.substringBetween(key_words, "productPrice: \"", "\",");
			String originalPriceStr = StringUtils.substringBetween(key_words, "<p class=\"rrp\">RRP: <span class=\"strike\">", "</span></p>");
			if(StringUtils.isNotBlank(priceStr))
				priceStr = priceStr.replace("&#163;", "£").replace(" ", "");
			Map<String,Object> priceMap = CrawlerUtils.formatPrice(priceStr, context.getCurrentUrl(), logger);
			float price = (float) priceMap.get(CrawlerUtils.KEY_PRICE);
			float original_price = 0f;
			String unit = priceMap.get(CrawlerUtils.KEY_UNIT).toString();
			if(StringUtils.isBlank(originalPriceStr)){
				original_price = price;
			} else {
				originalPriceStr = originalPriceStr.replace("&#163;", "£").replace(" ", "");
				original_price = (float)CrawlerUtils.formatPrice(originalPriceStr, context.getCurrentUrl(), logger).get(CrawlerUtils.KEY_PRICE);
			}
			if(original_price < price)
				original_price = price;
			int save = 0;
			if (original_price != 0)
				save = Math.round((1 - price / original_price) * 100);

			String brand = StringUtils.substringBetween(key_words, "productBrand: \"", "\",");
			brand = StringEscapeUtils.unescapeHtml4(brand);
			
			String docid = SpiderStringUtil.md5Encode(domain + productID);
			String url_no = SpiderStringUtil.md5Encode(context.getCurrentUrl());
			
			//设置面包屑和类别
			List<String> breads = new ArrayList<String>();
			List<String> categories = new ArrayList<String>();
			CrawlerUtils.setBreadAndCategory(breads, categories, document, CSS_BREADS, title);
			
			// description and feature
			Map<String, Object> featureMap = new HashMap<String, Object>();
			Map<String, Object> descMap = new HashMap<String, Object>();
			CrawlerUtils.setDescription(featureMap, descMap, document, CSS_DESCRIPTION, CSS_DETAIL);
			
			// 设置spu图片
			List<String> imgs = new ArrayList<>();
			List<Picture> pl = new ArrayList<>();
			Elements ePictures = document.select(CSS_IMAGE_LIST);
			if(CollectionUtils.isEmpty(ePictures))
				ePictures = document.select(CSS_IMAGE);
			for (Element e : ePictures) {
				String img = StringUtils.trim(e.absUrl("href"));
				if(StringUtils.isNotBlank(img)){
					pl.add(new Picture(img, ""));
					imgs.add(img);
				}
			}
			LImageList image_list = new LImageList(pl);
//			context.getUrl().getImages().put(productID, CrawlerUtils.convertToImageList(imgs));

			//设置库存状态
			int stock = 0;
			Elements es = document.select(CSS_INVENTORY);
			if(CollectionUtils.isNotEmpty(es)){
				for(Element e : es){
					if("In stock".equals(StringUtils.trim(e.ownText())))
						stock = 1;
				}
			}
			
			// 设置sku
			List<LStyleList> l_style_list = new ArrayList<>();
			List<LSelectionList> l_selection_list = new ArrayList<>();
			
			Map<String, Object> properties = new HashMap<>();
			if(title.contains("Women") || title.contains("Girl")){
				properties.put("s_gender", "women");
			} else if(title.contains(" Men ") || title.contains(" Boy ")){
				properties.put("s_gender", "men");
			}
			if (properties.get("s_gender") == null) {
				if (brand.contains("Men") || brand.contains("Boy"))
					properties.put("s_gender", "men");
				else if (brand.contains("Girl"))
					properties.put("s_gender", "women");
				else
					properties.put("s_gender", "");
			}
			
			// 设置retbody
			retbody.setDOCID(docid);
			retbody.setSite(new Site(domain));
			retbody.setProdUrl(new ProdUrl(context.getCurrentUrl(), System.currentTimeMillis(), url_no));
			retbody.setTitle(new Title(title.trim(), "", "", ""));
			retbody.setBrand(new Brand(brand, "", "", ""));
			retbody.setBreadCrumb(breads);
			retbody.setCategory(categories);
			retbody.setFeatureList(featureMap);
			retbody.setDescription(descMap);
			retbody.setImage(image_list);
			retbody.setPrice(new Price(original_price, save, price, unit));
			retbody.setStock(new Stock(stock));
			retbody.setSku(new Sku(l_selection_list, l_style_list));
			retbody.setProperties(properties);
		}
		System.err.println(retbody.parseTo());
		System.out.println();
		System.out.println(StringEscapeUtils.unescapeHtml4("Roger&amp;Gallet"));
	}

}
