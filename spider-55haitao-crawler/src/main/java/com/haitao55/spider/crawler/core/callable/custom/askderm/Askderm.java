package com.haitao55.spider.crawler.core.callable.custom.askderm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.haitao55.spider.common.gson.bean.Brand;
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
import com.haitao55.spider.common.http.Crawler;
import com.haitao55.spider.common.http.HttpException;
import com.haitao55.spider.common.utils.CurlCrawlerUtil;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.common.cache.ProxyCache;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.core.model.Proxy;
import com.haitao55.spider.crawler.utils.Constants;
import com.haitao55.spider.crawler.utils.JsoupUtils;

/**
 * 
* @ClassName: Askderm
* @Description: askderm详情页吗解析
* @author songsong.xu
* @date 2017年6月6日
*
 */
public class Askderm extends AbstractSelect {
	
    private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
    private static final String DOMAIN = "www.askderm.com";

	public void invoke(Context context) throws ClientProtocolException, HttpException, IOException {
		String url = context.getCurrentUrl();
		String proxyRegionId = context.getUrl().getTask().getProxyRegionId();
        String content = StringUtils.EMPTY;
        if (proxyRegionId != null) {
            Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
            String ip = proxy.getIp();
            int port = proxy.getPort();
            //content = Crawler.create().timeOut(30000).retry(2).proxy(true).proxyAddress(ip).proxyPort(port).url(url).header(getHeaders()).resultAsString();
            content = CurlCrawlerUtil.get(url,20,ip,port);
            logger.info("use proxy ip {}, port {},url {}",ip,port,url);
        } else {
            //content = Crawler.create().timeOut(30000).url(url).retry(2).header(getHeaders()).resultAsString();
        	 content = CurlCrawlerUtil.get(url);
        	logger.info("do not use proxy url {}",url);
        }
		
	    //String content = Crawler.create().method("get").timeOut(30000).url(url).proxy(true).proxyAddress("159.203.15.211").proxyPort(3128).retry(3).resultAsString();
		Document doc = JsoupUtils.parse(content);
		// product data
		String productData = StringUtils.substringBetween(content, "new Shopify.OptionSelectors", "onVariantSelected");
		if (StringUtils.isNotBlank(productData)) {
			RetBody retBody = new RetBody();
			Sku sku = new Sku();
			// productId
			String productId = StringUtils.substringBetween(content, "productId\":", ",");
			// spu stock status
			int spuStockStatus = 0;
			// default sku
			String defaultSku = StringUtils.substringBetween(content, "sku\":\"", "\",");
			// brand
			String brand = StringUtils.substringBetween(content, "brand\":\"", "\",");
			// unit
			String unit = StringUtils.substringBetween(content, "currency\":\"", "\",");
			// title
			String title = StringUtils.substringBetween(content, ",\"name\":\"", "\",");

			// sku jsonarray
			JSONArray skuJSONArray = new JSONArray();
			// images 用作单属性商品图片展示
			List<Image> images = new ArrayList<Image>();

			// 处理productdata 反序列化为jsonobject
			productData = StringUtils.substringBetween(productData, "product: ", "3e\"},");
			productData = productData + "3e\"}";

			// productJSONObject
			JSONObject productDataJSONObject = JSONObject.parseObject(productData);

			if (MapUtils.isNotEmpty(productDataJSONObject)) {
				skuJSONArrayPackage(productDataJSONObject, skuJSONArray, images);
			}

			// sku
			List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
			List<LStyleList> l_style_list = new ArrayList<LStyleList>();
			// style
			Set<String> styleSet = new HashSet<String>();
			if (CollectionUtils.isNotEmpty(skuJSONArray)) {
				for (Object object : skuJSONArray) {
					JSONObject skuJSONObject = (JSONObject) object;
					// selectlist
					LSelectionList lselectlist = new LSelectionList();

					// skuId
					String skuId = skuJSONObject.getString("skuId");
					// label
					String color = skuJSONObject.getString("color");
					if (StringUtils.containsIgnoreCase(color, "default")) {
						color = "default";
					}

					// stock
					int stock_status = skuJSONObject.getIntValue("stock_status");
					int stock_number = 0;
					if (stock_status > 0) {
						spuStockStatus = 1;
					}

					// style_id
					String style_id = color;

					// selections
					List<Selection> selections = new ArrayList<Selection>();

					// orign_price
					float orign_price = skuJSONObject.getFloatValue("orignPrice");
					float sale_price = skuJSONObject.getFloatValue("salePrice");
					// lselectlist
					lselectlist.setGoods_id(skuId);
					lselectlist.setOrig_price(orign_price);
					lselectlist.setSale_price(sale_price);
					lselectlist.setPrice_unit(unit);
					lselectlist.setStock_status(stock_status);
					lselectlist.setStock_number(stock_number);
					lselectlist.setStyle_id(style_id);
					lselectlist.setSelections(selections);

					// l_selection_list
					l_selection_list.add(lselectlist);
					if (StringUtils.equals(defaultSku, skuId)) {
						int save = (int) ((1 - (sale_price / orign_price)) * 100);
						retBody.setPrice(new Price(orign_price, save, sale_price, unit));
					}

					// style
					if (!styleSet.contains(color)) {
						// stylelist
						LStyleList lStyleList = new LStyleList();
						if (StringUtils.containsIgnoreCase(defaultSku, skuId)) {
							lStyleList.setDisplay(true);
						}

						// imageUrl
						String imageUrl = skuJSONObject.getString("imageUrl");
						List<Image> skuImages = new ArrayList<Image>();
						if (StringUtils.isNotBlank(imageUrl)) {
							skuImages.add(new Image(imageUrl));
						} else {
							skuImages = images;
						}
						
						context.getUrl().getImages().put(skuId, skuImages);
						// switch_img
						String switch_img = StringUtils.EMPTY;

						// stylelist
						lStyleList.setGood_id(skuId);
						lStyleList.setStyle_switch_img(switch_img);
						lStyleList.setStyle_cate_id(0);
						lStyleList.setStyle_id(style_id);
						lStyleList.setStyle_cate_name("Color");
						lStyleList.setStyle_name(style_id);

						l_style_list.add(lStyleList);

						styleSet.add(color);
					}
				}
			}

			// sku
			sku.setL_selection_list(l_selection_list);
			sku.setL_style_list(l_style_list);

			retBody.setSku(sku);

			// stock
			retBody.setStock(new Stock(spuStockStatus));

			// full doc info
			String docid = SpiderStringUtil.md5Encode(DOMAIN + productId);
			String url_no = SpiderStringUtil.md5Encode(url);
			retBody.setDOCID(docid);
			retBody.setSite(new Site(DOMAIN));
			retBody.setProdUrl(new ProdUrl(url, System.currentTimeMillis(), url_no));
			// brand
			retBody.setBrand(new Brand(brand, "", "", ""));
			// title
			retBody.setTitle(new Title(title, "", "", ""));
			category_package(brand, title, doc, retBody);
			// properties
			Map<String, Object> propMap = new HashMap<String, Object>();
			propMap.put("s_gender", "");
			retBody.setProperties(propMap);
			// description
			desc_package(doc, retBody);

			setOutput(context, retBody);
			//System.out.println(retBody.parseTo() ); 
		}

	}

	/**
	 * skuJSONArray 封装
	 * 
	 * @param productDataJSONObject
	 * @param skuJSONArray
	 * @param images
	 */
	private static void skuJSONArrayPackage(JSONObject productDataJSONObject, JSONArray skuJSONArray,
			List<Image> images) {
		JSONArray jsonArray = productDataJSONObject.getJSONArray("variants");
		if (CollectionUtils.isNotEmpty(jsonArray)) {
			if (1 == jsonArray.size()) {
				imagesPackage(productDataJSONObject, images);
			}
			for (Object object : jsonArray) {
				JSONObject jsonObject = (JSONObject) object;
				String skuId = jsonObject.getString("sku");
				String color = jsonObject.getString("option1");
				// stock
				int stockStatus = 0;
				int inventoryQuantity = jsonObject.getIntValue("inventory_quantity");
				if (inventoryQuantity > 0) {
					stockStatus = 1;
				}
				float salePrice = (jsonObject.getFloatValue("price")) * 0.01f;
				float orignPrice = 0f;
				String orignPriceStr = jsonObject.getString("compare_at_price");
				if (StringUtils.isBlank(orignPriceStr)) {
					orignPrice = salePrice;
				} else {
					orignPrice = Float.parseFloat(orignPriceStr) * 0.01f;
				}
				JSONObject imageJSONObject = jsonObject.getJSONObject("featured_image");
				String imageUrl = StringUtils.EMPTY;
				if (MapUtils.isNotEmpty(imageJSONObject)) {
					imageUrl = imageJSONObject.getString("src");
				}

				JSONObject skuJSONObject = new JSONObject();
				skuJSONObject.put("skuId", skuId);
				skuJSONObject.put("color", color);
				skuJSONObject.put("stock_status", stockStatus);
				skuJSONObject.put("salePrice", salePrice);
				skuJSONObject.put("orignPrice", orignPrice);
				skuJSONObject.put("imageUrl", imageUrl);
				skuJSONArray.add(skuJSONObject);

			}

		}
	}

	/**
	 * 封装images
	 * 
	 * @param productDataJSONObject
	 * @param images
	 */
	private static void imagesPackage(JSONObject productDataJSONObject, List<Image> images) {
		JSONArray imagesJSONArray = productDataJSONObject.getJSONArray("images");
		if (CollectionUtils.isNotEmpty(imagesJSONArray)) {
			for (Object object : imagesJSONArray) {
				String imageUrl = (String) object;
				if (!StringUtils.contains(imageUrl, "http")) {
					imageUrl = "http:" + imageUrl;
				}
				images.add(new Image(imageUrl));
			}
		}
	}

	/**
	 * 分类封装
	 * 
	 * @param brand
	 * @param title
	 * @param doc
	 * @param retBody
	 */
	private static void category_package(String brand, String title, Document doc, RetBody retBody) {
		Elements categoryElemets = doc.select("nav.breadcrumb a:not(:first-child)");
		List<String> cats = new ArrayList<String>();
		List<String> breads = new ArrayList<String>();
		if (CollectionUtils.isNotEmpty(categoryElemets)) {
			for (Element element : categoryElemets) {
				String text = element.text();
				breads.add(text);
				cats.add(text);
			}
		}
		breads.add(title);
		cats.add(title);
		breads.add(brand);
		retBody.setCategory(cats);
		retBody.setBreadCrumb(breads);
	}

	/***
	 * 描述 封装
	 * 
	 * @param doc
	 * @param retBody
	 */
	private static void desc_package(Document doc, RetBody retBody) {
		Map<String, Object> featureMap = new HashMap<String, Object>();
		Map<String, Object> descMap = new HashMap<String, Object>();
		// desc trans doc
		Elements es = doc.select("div.product-description.rte p,div.product-description.rte ul li");
		StringBuilder sb = new StringBuilder();
		if (es != null && es.size() > 0) {
			int count = 1;
			for (Element e : es) {
				String text = e.text();
				if (StringUtils.isNotBlank(text)) {
					featureMap.put("feature-" + count, text);
					count++;
					sb.append(text);
				}
			}
		}
		retBody.setFeatureList(featureMap);
		descMap.put("en", sb.toString());
		retBody.setDescription(descMap);
	}

	private static Map<String, Object> getHeaders() {
		final Map<String, Object> headers = new HashMap<String, Object>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 2581158727487646435L;

			{
				put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
				put("Accept-Encoding", "gzip, deflate, sdch");
				put("Accept-Language", "zh-CN,zh;q=0.8");
				put("Cache-Control", "max-age=0");
				put("Connection", "keep-alive");
				put("User-Agent",
						"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/55.0.2883.87 Chrome/55.0.2883.87 Safari/537.36");
			}
		};
		return headers;
	}
	
  public static void main(String[] args) throws ClientProtocolException, HttpException, IOException {
      Askderm ask = new Askderm();
      Context context = new Context();
      context.setCurrentUrl("https://askderm.com/collections/foreo/products/foreo-luna-mini-2");
      ask.invoke(context);
      
      /*String url = "https://askderm.com/products/st-tropez-self-tan-dark-bronzing-mist";
      String content = Crawler.create().method("get").timeOut(30000).url(url).proxy(true).proxyAddress("159.203.15.211").proxyPort(3128).retry(3).resultAsString();
      System.out.println(content);*/
  }
}
