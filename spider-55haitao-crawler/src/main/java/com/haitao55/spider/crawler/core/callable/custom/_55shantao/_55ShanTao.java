package com.haitao55.spider.crawler.core.callable.custom._55shantao;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.common.gson.bean.Brand;
import com.haitao55.spider.common.gson.bean.LSelectionList;
import com.haitao55.spider.common.gson.bean.LStyleList;
import com.haitao55.spider.common.gson.bean.Price;
import com.haitao55.spider.common.gson.bean.ProdUrl;
import com.haitao55.spider.common.gson.bean.RetBody;
import com.haitao55.spider.common.gson.bean.Site;
import com.haitao55.spider.common.gson.bean.Sku;
import com.haitao55.spider.common.gson.bean.Stock;
import com.haitao55.spider.common.gson.bean.Title;
import com.haitao55.spider.common.utils.Currency;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.exception.CrawlerException.CrawlerExceptionCode;
import com.haitao55.spider.crawler.exception.ParseException;
import com.haitao55.spider.crawler.utils.Constants;
import com.haitao55.spider.crawler.utils.JsoupUtils;

/**
 * 
  * @ClassName: _55ShanTao
  * @Description: 自营商品爬取
  * @author songsong.xu
  * @date 2016年11月16日 下午2:59:05
  *
 */
public class _55ShanTao extends AbstractSelect {
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private static final String domain = "www.55shantao.com";

	@Override
	public void invoke(Context context) throws Exception {

		String content = super.getInputString(context);
		//String content = Crawler.create().timeOut(10000).retry(3).url(context.getUrl().getValue()).resultAsString();
		Pattern p = Pattern.compile("<a[^>]*class=\"direct-detail-notice-btn direct-detail-buy btn-disabled\">[^<]*商品未上架[^<]*</a>");
		Matcher m = p.matcher(content);
		if(m.find()){
			throw new ParseException(CrawlerExceptionCode.OFFLINE,"55shantao.com itemUrl:"+context.getUrl().toString()+" is offline...");
		}
		p = Pattern.compile("已结束售卖");
		m = p.matcher(content);
		if(m.find()){
			throw new ParseException(CrawlerExceptionCode.OFFLINE,"55shantao.com itemUrl:"+context.getUrl().toString()+" is offline...");
		}
		String url = context.getUrl().toString();
		RetBody rebody = new RetBody();
		if (StringUtils.isNotBlank(content)) {
			Document document = Jsoup.parse(content);
			Sku sku = new Sku();
			String stock = StringUtils.substringBetween(content, "var stock = ", ";");
			String salePrice = StringUtils.substringBetween(content, "var price_sale = ", ";");
			if(StringUtils.isBlank(stock) || StringUtils.isBlank(salePrice)){
				throw new ParseException(CrawlerExceptionCode.PARSE_ERROR,"55shantao.com itemUrl:"+context.getUrl().toString()+" parse error");
			}
			List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
			List<LStyleList> l_style_list = new ArrayList<LStyleList>();
			sku.setL_selection_list(l_selection_list);
			sku.setL_style_list(l_style_list);

			// full doc info
			String docid = SpiderStringUtil.md5Encode(url);
			String url_no = SpiderStringUtil.md5Encode(context.getCurrentUrl());
			rebody.setDOCID(docid);
			rebody.setSite(new Site(domain));
			rebody.setProdUrl(new ProdUrl(context.getCurrentUrl(), System.currentTimeMillis(), url_no));
			
			//title 
			Elements es = document.select("div.direct-detail-goods-box.clearfix > div.direct-detail-goods-info > h1");
			String title = getText(es);
			rebody.setTitle(new Title("", (title== null?"":title), "", ""));

			
			// price 
			es = document.select("div.direct-detail-goods-info > div.direct-detail-price-wrap > p.direct-detail-price.clearfix > strong");
			String unit = StringUtils.EMPTY;
			if(es != null && es.size() > 0){
				unit = Currency.codeOf(es.get(0).ownText()).name();
			}
			es = document.select("div.direct-detail-goods-info > div.direct-detail-price-wrap > p.direct-detail-rel-price.clearfix > span:nth-child(1)");
			String origPrice = JsoupUtils.text(es);
			if(StringUtils.contains(origPrice, "：")){
				origPrice = StringUtils.substringBetween(origPrice, "：", "元");
			}
			es = document.select("div.direct-detail-goods-info > div.direct-detail-price-wrap > p.direct-detail-price.clearfix > strong > b");
			String price = getText(es);
			float sale = 0;
			if(StringUtils.isBlank(price)){
				price = salePrice;
			}
			if(StringUtils.isNotBlank(price)){
				sale = Float.valueOf(price);
			} else {
				logger.error("fetch price from 55shantao with url {}",url);
			}
			float orig = 0;
			if(StringUtils.isNotBlank(origPrice)){
				orig = Float.valueOf(origPrice);
			} else {
				orig = sale;
			}
			int save = Math.round((1 - sale / orig) * 100);// discount
			rebody.setPrice(new Price(orig, save, sale, unit));
			// stock
			
			rebody.setStock(new Stock(2));
			// images l_image_list
			// rebody.setImage(new LImageList(pics));
			// brand
			StringBuilder brand = new StringBuilder();
			es = document.select("div.direct-detail-price-wrap > p > span");
			if(es != null && es.size() > 0){
			    es.forEach( ele -> {
			        String brandText = ele.text();
	                if(StringUtils.contains(brandText, "品牌")){
	                    brand.append(ele.attr("title"));
	                    return;
	                }
			    });
			}
			rebody.setBrand(new Brand((StringUtils.isBlank(brand.toString())?"自营":brand.toString()), "","",""));
			// Category
			List<String> cats = new ArrayList<String>();
			List<String> breads = new ArrayList<String>();
			//
			es = document.select("div.direct-detail-wrap > div > div.direct-nav > p > a");
			if (es != null && es.size() > 0) {
				for (Element ele : es) {
					String text = ele.text();
					if(StringUtils.isNotBlank(text)){
						cats.add(text);
						breads.add(text);
					}
				}
			}
			rebody.setCategory(cats);
			// BreadCrumb
			rebody.setBreadCrumb(breads);
			// description
			Map<String, Object> featureMap = new HashMap<String, Object>();
			Map<String, Object> descMap = new HashMap<String, Object>();
			
			String skuId = System.currentTimeMillis()+"";
			es = document.select("div.direct-detail-pic-slider-main > ul.clearfix > li");
			if(es != null && es.size() > 0){
				List<Image> imageUrls = new ArrayList<Image>();
				for(Element ele : es){
					String imageUrl = JsoupUtils.attr(ele, "data-big-img");
					if(StringUtils.contains(imageUrl, "!")){
						imageUrl = StringUtils.substringBefore(imageUrl, "!");
					}
					Image image = new Image(imageUrl);
					imageUrls.add(image);
				}
				context.getUrl().getImages().put(skuId, imageUrls);
			}
			//div#goodsDes > p > img
			es = document.select("div#goodsDes > p > img");
			if(es != null && es.size() > 0 ){
				List<Image> imageUrls = new ArrayList<Image>();
				for(Element ele : es){
					String src = JsoupUtils.attr(ele, "src");
					if(StringUtils.isBlank(src)){
						continue;
					}
					Image image = new Image(src);
					imageUrls.add(image);
				}
				List<Image> images = context.getUrl().getImages().get(skuId);
				if(images != null){
					images.addAll(imageUrls);
				}
			}
			
			es = document.select("div#goodsDes > h2");
			int count = 1;
			StringBuilder sb = new StringBuilder();
			if(es != null && es.size() > 0){
				for(Element ele : es){
					String text = JsoupUtils.text(ele);
					if(StringUtils.isBlank(text)){
						continue;
					}
					featureMap.put("feature-" + count, text);
					count++;
					sb.append(text);
				}
			}
			rebody.setFeatureList(featureMap);
			descMap.put("en", sb.toString());
			rebody.setDescription(descMap);

			Map<String, Object> propMap = new HashMap<String, Object>();
			propMap.put("s_gender", "all");
			//body > div.direct-detail-wrap > div > div.direct-detail-main.clearfix > div.direct-detail-con > div.direct-detail-goods-detail > div.direct-detail-goods-parameter.block > table > tbody > tr
			es = document.select("div.direct-detail-goods-detail > div.direct-detail-goods-parameter.block > table > tbody > tr");
			if (es != null && es.size() > 0) {
				for (Element e : es) {
					Elements eles = e.select("td");
					if(eles != null && eles.size() > 0 ){
						String key1 = eles.get(0).text();
						String value1 = eles.get(1).text();
						if(StringUtils.isNotBlank(key1) && StringUtils.isNotBlank(value1)){
							propMap.put(key1, value1);
						}
						if(eles.size() > 2){
							String key2 = eles.get(2).text();
							String value2 = eles.get(3).text();
							if(StringUtils.isNotBlank(key2) && StringUtils.isNotBlank(value2)){
								propMap.put(key2, value2);
							}
						}
					}
				}
			}
			rebody.setProperties(propMap);
			rebody.setSku(sku);
		}
		setOutput(context, rebody.parseTo());
		//System.out.println(rebody.parseTo());
	}

	public String getText(Elements es){
		if(es != null && es.size() > 0){
			return es.get(0).text();
		}
		return StringUtils.EMPTY;
	}
	
	public String getAttr(Elements es,String attrKey){
		if(es != null && es.size() > 0){
			return es.get(0).attr(attrKey);
		}
		return StringUtils.EMPTY;
	}

	public static void main(String[] args) throws Exception {
		
		_55ShanTao shan = new _55ShanTao();
		Context context = new Context();
		context.setUrl(new Url("http://www.55shantao.com/product-2408545.html"));
		shan.invoke(context);
		
		//System.out.println(BCConvert.bj2qj("¥").equals("￥"));
		/*String url = "http://www.55shantao.com/product-583039.html";
		
		String html = Crawler.create().timeOut(10000).retry(3).url(url
		// "http://www.6pm.com/ivanka-trump-kayden-4-black-patent"
				).resultAsString();
		Document d = JsoupUtils.parse(html);
		//body > div.direct-detail-wrap > div > div.direct-detail-goods-box.clearfix > div.direct-detail-goods-info > div.direct-detail-price-wrap > p.direct-detail-rel-price.clearfix > span:nth-child(1)
		Elements es = d.select("span#pay_shadow");
		
		System.out.println(JsoupUtils.text(es));*/
		
	}
}