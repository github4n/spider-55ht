package com.haitao55.spider.crawler.core.callable.custom.peterthomasroth;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

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
import com.haitao55.spider.common.http.HttpException;
import com.haitao55.spider.common.utils.Currency;
import com.haitao55.spider.common.utils.LuminatiHttpClient;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.callable.custom.ssense.Ssense;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.utils.JsoupUtils;

/**
 * peterthomasroth 网站收录 Title: Description: Company: 55海淘
 * 
 * @author zhaoxl
 * @date 2017年4月28日 下午2:44:14
 * @version 1.0
 */
public class Peterthomasroth extends AbstractSelect {
	private static final String DOMAIN = "www.peterthomasroth.com";

	private static Map<String,Object> getHeaders(){
		final Map<String, Object> headers = new HashMap<String, Object>();
		headers.put("User-Agent",
				"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/62.0.3202.89 Chrome/62.0.3202.89 Safari/537.36");
		headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8");
		headers.put("Upgrade-Insecure-Requests", "1");
		headers.put("Host", "www.peterthomasroth.com");
		headers.put("Accept-Encoding", "gzip, deflate, br");
		headers.put("Cookie", "BVBRANDID=344c8585-5818-4380-8d6c-a90eaa2f0266; ASP.NET_SessionId=n1b3uqbjgggjn2ckohhnvox1; HASH_ASP.NET_SessionId=6E9562B8198653FFF2AAFE44DB7057DDEE4ABF20; BVImplmain_site=13934; _isuid=9C7F593B-1C92-4DD8-93A8-35C60EDAE3BA; UrlParam=WebKey=1594784.98977105&CustCode=65492; AppriseCustPortal=0ADDD9CBABA242B012C45F4AE4465C633169667235A638B76143DD685E0D3A80514D5CCFB1674D288716960E563ABB8B53E070544AFDC36D061C4B1066DBA6965FA7DA8E4FE51DD677436122F55A411514ADE162CAA1A7AAF601EB0CDBDEE05AB8803FCBCB44A509D382E0193D98AE5EACD1089AF8CD2DD75976442CDF7DE60F; HASH_UrlParam=228744D1C94A048F05C895749AC6F475FA3F22B0; HASH_AppriseCustPortal=EF918790A688382324C465A3403D874C4A52840E; u-upsellitc3006=seenChat; u-upsellit10048=seenChat; _ga=GA1.2.2134388855.1506482123; _gid=GA1.2.694635695.1511438516; __AnonRecentlyViewedProducts=1501247 1301015 3901001; HASH___AnonRecentlyViewedProducts=A880B995F154FD4910BF8B20940741A2AD8ACA11; stc112089=env:1511442012%7C20171224130012%7C20171123140341%7C9%7C1020186:20181123133341|uid:1506482124607.28217345.407127857.112089.1737014876:20181123133341|srchist:1020187%3A1510113340%3A20171209035540%7C1020186%3A1511442012%3A20171224130012:20181123133341|tsa:1511442012564.1817315839.6328993.371326982830801.2:20171123140341; com.silverpop.iMAWebCookie=3b88ae71-a7ee-1d4b-7aee-e321857b5904");
		return headers;
	}
	
	public void invoke(Context context) throws ClientProtocolException, HttpException, IOException {
		String url = context.getCurrentUrl().toString();
		String content = StringUtils.EMPTY;
		boolean isRun = context.isRunInRealTime();
		if(isRun){
			LuminatiHttpClient luminatiHttpClient = new LuminatiHttpClient("US", false);
			content = luminatiHttpClient.request(context.getCurrentUrl(), getHeaders());
		}else{
			content = this.getInputString(context);
		}
		Document doc = JsoupUtils.parse(content);

		RetBody retBody = new RetBody();
		Sku sku = new Sku();

		// productId
		String productId = StringUtils.substringBetween(content, "productId\": \"", "\"");

		// spustockstatus
		int spuStockStatus = 1;
		Elements stockElements = doc.select("img.OutOfStockImg");
		if (CollectionUtils.isNotEmpty(stockElements)) {// 存在无货标志
			spuStockStatus = 0;
		}
		// size 存在则封装sku
		String size = StringUtils.EMPTY;
		String skuId = StringUtils.EMPTY;
		Elements sizeElements = doc.select("span.ProductSize");
		if (CollectionUtils.isNotEmpty(sizeElements)) {
			size = sizeElements.text();
		}
		Elements skuIdElements = doc.select("span.ProductRelCode");
		if (CollectionUtils.isNotEmpty(skuIdElements)) {
			skuId = StringUtils.replacePattern(skuIdElements.text(), "[- ]", "");
		}

		// price
		String orignPrice = StringUtils.EMPTY;
		String salePrice = StringUtils.EMPTY;
		Elements orignElements = doc.select("span.ProductListPrice");
		Elements saleElements = doc.select("span.ProductPrice");
		if (CollectionUtils.isNotEmpty(orignElements)) {
			orignPrice = orignElements.text();
		}
		if (CollectionUtils.isNotEmpty(saleElements)) {
			salePrice = saleElements.text();
		} else {
			saleElements = doc.select("span.ProductPrice");
			salePrice = saleElements.text();
		}
		if (StringUtils.isBlank(orignPrice)) {
			orignPrice = salePrice;
		}
		// unit
		String unit = getCurrencyValue(salePrice);
		if (StringUtils.isBlank(unit)) {
			unit = "USD";
		}
		salePrice = salePrice.replaceAll("[$, ]", "");
		orignPrice = orignPrice.replaceAll("[$, ]", "");

		// brand
		String brand = "peterthomasroth";

		// title
		String title = StringUtils.EMPTY;
		Elements titleElements = doc.select("div.ProductName");
		if (CollectionUtils.isNotEmpty(titleElements)) {
			title = titleElements.text();
		}

		// images
		List<Image> images = new ArrayList<Image>();
		Elements imageElements = doc.select("div.ProductImages a img");
		if (CollectionUtils.isNotEmpty(imageElements)) {
			for (Element element : imageElements) {
				String imageUrl = element.attr("src");
				if (StringUtils.isNotBlank(imageUrl)) {
					images.add(new Image(imageUrl));
				}
			}
		}
		Elements otherImageElements = doc.select("div.ItemContainer.ImageItemHolder div a:has(img)");
		if (CollectionUtils.isNotEmpty(otherImageElements)) {
			for (Element element : otherImageElements) {
				String imageUrl = element.attr("href");
				if (StringUtils.isNotBlank(imageUrl)) {
					images.add(new Image(imageUrl));
				}
			}
		}

		List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
		List<LStyleList> l_style_list = new ArrayList<LStyleList>();
		if (StringUtils.isNotBlank(size)) {
			// 封装存在size的sku 便于展示表明商品属性
			// sku

			// selectlist
			LSelectionList lselectlist = new LSelectionList();

			if (StringUtils.isBlank(skuId)) {
				skuId = productId;
			}
			// color
			String color = "default";

			// stock
			int stock_status = spuStockStatus;

			// style_id
			String style_id = color;

			// selections
			List<Selection> selections = new ArrayList<Selection>();
			Selection selection = new Selection();
			selection.setSelect_id(0);
			selection.setSelect_name("Size");
			selection.setSelect_value(size);
			selections.add(selection);

			// lselectlist
			lselectlist.setGoods_id(skuId);
			lselectlist.setOrig_price(Float.parseFloat(orignPrice));
			lselectlist.setSale_price(Float.parseFloat(salePrice));
			lselectlist.setPrice_unit(unit);
			lselectlist.setStock_status(stock_status);
			lselectlist.setStock_number(0);
			lselectlist.setStyle_id(style_id);
			lselectlist.setSelections(selections);

			// l_selection_list
			l_selection_list.add(lselectlist);
			int save = (int) ((1 - (Float.parseFloat(salePrice) / Float.parseFloat(orignPrice))) * 100);
			retBody.setPrice(new Price(Float.parseFloat(orignPrice), save, Float.parseFloat(salePrice), unit));

			// stylelist
			LStyleList lStyleList = new LStyleList();
			// images
			lStyleList.setDisplay(true);
			context.getUrl().getImages().put(skuId, images);

			String switch_img = StringUtils.EMPTY;

			// stylelist
			lStyleList.setGood_id(skuId);
			lStyleList.setStyle_switch_img(switch_img);
			lStyleList.setStyle_cate_id(0);
			lStyleList.setStyle_id(style_id);
			lStyleList.setStyle_cate_name("Color");
			lStyleList.setStyle_name(style_id);

			l_style_list.add(lStyleList);
			// sku
		
		}else{
			int save = (int) ((1 - (Float.parseFloat(salePrice) / Float.parseFloat(orignPrice))) * 100);
			retBody.setPrice(new Price(Float.parseFloat(orignPrice), save, Float.parseFloat(salePrice), unit));
			context.getUrl().getImages().put(productId, images);
		}
		
		sku.setL_selection_list(l_selection_list);
		sku.setL_style_list(l_style_list);
		
		retBody.setSku(sku);

		// stock
		retBody.setStock(new Stock(spuStockStatus));

		// title
		retBody.setTitle(new Title(title, "", "", ""));

		// brand
		retBody.setBrand(new Brand(brand, "", "", ""));

		// full doc info
		String docid = SpiderStringUtil.md5Encode(productId.concat(DOMAIN));
		String url_no = SpiderStringUtil.md5Encode(url);
		retBody.setDOCID(docid);
		retBody.setSite(new Site(DOMAIN));
		retBody.setProdUrl(new ProdUrl(url, System.currentTimeMillis(), url_no));

		// category breadcrumb
		List<String> cats = new ArrayList<String>();
		List<String> breads = new ArrayList<String>();
		Elements categoryElements = doc.select("div#NavigationURL a:not(:first-child)");
		if (CollectionUtils.isNotEmpty(categoryElements)) {
			List<String> categoryList = new ArrayList<String>();
			for (Element element : categoryElements) {
				String text = element.text();
				if (StringUtils.isNotBlank(text)) {
					categoryList.add(StringUtils.trim(text));
				}
			}
			cats.addAll(categoryList);
			breads.addAll(categoryList);
		}
		breads.add(title);
		cats.add(title);
		breads.add(brand);
		retBody.setCategory(cats);
		retBody.setBreadCrumb(breads);
		// description
		desc_package(doc, retBody);

		// properties
		Map<String, Object> propMap = new HashMap<String, Object>();
		propMap.put("s_gender", "");
		retBody.setProperties(propMap);
		setOutput(context, retBody);
	}

	/***
	 * 描述 封装
	 * 
	 * @param desc
	 * @param retBody
	 */
	private static void desc_package(Document doc, RetBody retBody) {
		Map<String, Object> featureMap = new HashMap<String, Object>();
		Map<String, Object> descMap = new HashMap<String, Object>();
		// desc trans doc
		Elements es = doc.select("div.ProducLongDesc");
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

	private static String getCurrencyValue(String val) {
		String currency = StringUtils.substring(val, 0, 1);
		String unit = StringUtils.EMPTY;
		unit = Currency.codeOf(currency).name();
		return unit;
	}
	public static void main(String[] args) throws ClientProtocolException, HttpException, IOException {
		
		Peterthomasroth shan = new Peterthomasroth();
		Context context = new Context();
		context.setUrl(new Url(
				"https://www.peterthomasroth.com/product/un-wrinkle/un-wrinkle-face-serum/1501247/each"));
		context.setCurrentUrl(
				"https://www.peterthomasroth.com/product/un-wrinkle/un-wrinkle-face-serum/1501247/each");
		// http://www.yslbeautyus.com/forever-light-creator-cc-primer/890YSL.html
		shan.invoke(context);
		
	}
}
