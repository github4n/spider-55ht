package com.haitao55.spider.crawler.core.callable.custom.c21stores;

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
import com.haitao55.spider.common.utils.Currency;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.utils.Constants;
import com.haitao55.spider.crawler.utils.Native2AsciiUtils;

public class C21stores extends AbstractSelect {
	private final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private String domain="www.c21stores.com";
	private String SEX_WOMEN="women";
	private String SEX_WOMEN2="Ladies";
	private String SEX_WOMEN3="Girls";
	private String SEX_MEN="men";
	private String SEX_MEN2="boys";
	private String priceSplit="–";
	private String priceSplit2="-";
	private String SOLD_OUT="SOLD OUT";
	@Override
	public void invoke(Context context) throws Exception {
		RetBody rebody=new RetBody();
		String content = super.getInputString(context);
		if(StringUtils.isNotBlank(content)){
			Document doc=context.getCurrentDoc().getDoc();
			String url=context.getCurrentUrl();
			Sku sku=new Sku();
			List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
			List<LStyleList> l_style_list = new ArrayList<LStyleList>();
			String brand=StringUtils.EMPTY;
			String title=StringUtils.EMPTY;
			String default_color=StringUtils.EMPTY;
			String productId=StringUtils.EMPTY;
			Map<String,Url> urls=new HashMap<String,Url>();
			List<String> colorList=new ArrayList<String>();
			List<String> urlParams=new ArrayList<String>();
			Map<String,Map<String,Object>> colorPrice=new HashMap<String,Map<String,Object>>();
			Map<String, List<String>> colorImages=new HashMap<String, List<String>>();
			//小图
			Map<String,String> colorStyleImages=new HashMap<String,String>();
			Map<String,String> colorIdSkuId=new HashMap<String,String>();
			Map<String, Integer> colorIdStock = new HashMap<String, Integer>();
			List<Map<String,Object>> skuList=new ArrayList<Map<String,Object>>();
			
			Elements productIdElements = doc.select("div.product-detail-container.view");
			if(CollectionUtils.isNotEmpty(productIdElements)){
				String str = productIdElements.attr("data-analytics");
				productId=StringUtils.substringBetween(str, "id\":\"", "\",");
			}
			int dismisson=0;
			Elements elements = null;
			//brand
			elements = doc.select("hgroup h2");
			if(CollectionUtils.isNotEmpty(elements)){
				brand=elements.get(0).text();
			}
			//title
			elements = doc.select("hgroup h1");
			if(CollectionUtils.isNotEmpty(elements)){
				title=elements.get(0).text();
			}
			
			elements = doc.select("form.product-details__form.product-details__add-to-cart-form div.color-options ul li.color-options__color.color-options__color--selected img");
			if(CollectionUtils.isNotEmpty(elements)){
				default_color=elements.get(0).attr("title");
			}
			elements = doc.select("form.product-details__form.product-details__add-to-cart-form div.color-options ul li img");
			if(CollectionUtils.isNotEmpty(elements)){
				for (Element element : elements) {
					dismisson++;
					String colorName = element.attr("title");
					String style_img=element.attr("src");
					colorStyleImages.put(colorName, style_img);//封装color  对应小图图片
					if(StringUtils.isNotBlank(colorName)){
						colorList.add(colorName);
						colorName=replaceColorName(colorName);
						urlParams.add(colorName);
					}
				}
			}
			//多个color
			if(dismisson>1){
				for (String color : urlParams) {//多个color
					String address=url.replaceAll("color=.*", "color="+color);
					Url skuUrl=new Url(address);
					skuUrl.setTask(context.getUrl().getTask());
					urls.put(color, skuUrl);
				}
				new C21storesHandler().process(urls,colorImages,colorPrice,colorIdSkuId,skuList,colorList);
			}else if(dismisson==1){//one color
				Elements imageElements = doc.select("img.product-details__alternate-image-button-image");
				if(CollectionUtils.isEmpty(imageElements)){
					imageElements=doc.select("a.product-details__primary-image-button img");
				}
				if(CollectionUtils.isNotEmpty(imageElements)){
					C21storesUtils.colorImagesPackage(colorImages,imageElements,default_color);
				}
				C21storesUtils.colorPricePackage(colorPrice,doc,default_color);
				C21storesUtils.colorIdSkuIdPackage(colorIdSkuId,doc,default_color,colorList);
				elements = doc.select("div.size-options ul li");
				if(CollectionUtils.isNotEmpty(elements)){
					for (Element element : elements) {
						C21storesUtils.skuPackage(element,skuList,default_color);
					}
				}else{
					default_color=StringUtils.EMPTY;	
				}
			}
			else{//no color
			elements = doc.select("div.size-options ul li");//size
			if(CollectionUtils.isEmpty(elements)){
				//单品
				default_color=StringUtils.EMPTY;
			}
		}
			
			//封装返回详情数据
			if(skuList.size()>0){//多个sku
				for (Map<String,Object> skuMap : skuList) {
					LSelectionList lselectlist=new LSelectionList();
					String skuId = (String)skuMap.get("skuId");
					String size = (String)skuMap.get("size");
					String color = (String)skuMap.get("color");
					Integer stock_status = (Integer)skuMap.get("stock_status");
					Integer stock_number = (Integer)skuMap.get("stock_number");
					Map<String, Object> priceMap = colorPrice.get(color);
					Float orign_price=(Float)priceMap.get("orign_price");
					Float sale_price=(Float)priceMap.get("sale_price");
					String price_unit = (String)priceMap.get("price_unit");
					
					//spu   stock
					Integer st = colorIdStock.get(color);
					if (st == null) {
						colorIdStock.put(color, stock_number);
					} else {
						colorIdStock.put(color, stock_number + st);
					}
					
					lselectlist.setGoods_id(skuId);
					lselectlist.setStyle_id(color);
					lselectlist.setOrig_price(orign_price);
					lselectlist.setSale_price(sale_price);
					lselectlist.setPrice_unit(price_unit);
					lselectlist.setStock_number(stock_number);
					lselectlist.setStock_status(stock_status);
					List<Selection> selections = new ArrayList<Selection>();
					if(StringUtils.isNotBlank(size)){
						Selection selection=new Selection();
						selection.setSelect_id(0);
						selection.setSelect_name("Size");
						selection.setSelect_value(size);
						selections.add(selection);
					}
					lselectlist.setSelections(selections);
					l_selection_list.add(lselectlist);
				}
				// style list
				for (String colorid : colorList) {
					List<Image> picsPerSku = new ArrayList<Image>();
					String skuId = colorIdSkuId.get(colorid);
					List<String> list = colorImages.get(colorid);
					if(null!=list&&list.size()>0){
						for (String imageUrl : list) {
							Image image=new Image(imageUrl);
							picsPerSku.add(image);
						}
					}
					context.getUrl().getImages().put(skuId + "", picsPerSku);// picture
					
					LStyleList style = new LStyleList();
					style.setStyle_switch_img(colorStyleImages.get(colorid));
					style.setStyle_id(colorid);
					style.setStyle_cate_id(0l);
					style.setStyle_cate_name("color");
					style.setStyle_name(colorid);
					style.setGood_id(skuId+"");
					if (colorid.equals(default_color + "")) {
						style.setDisplay(true);
					}
					l_style_list.add(style);
				}
			}
			//单品
			else{
				Elements imageElements = doc.select("div.product-details__alternate-image img");
				List<Image> picsPerSku = new ArrayList<Image>();
				if(CollectionUtils.isNotEmpty(imageElements)){
					for (Element element : imageElements) {
						String imageUrl = element.attr("src");
						Image image=new Image(imageUrl);
						picsPerSku.add(image);
					}
				}
				if(picsPerSku.size()==0){
					imageElements=doc.select("a.product-details__primary-image-button img");
					if(CollectionUtils.isNotEmpty(imageElements)){
						String imageUrl = imageElements.attr("src");
						Image image=new Image(imageUrl);
						picsPerSku.add(image);
					}
				}
				
				context.getUrl().getImages().put(productId + "", picsPerSku);
			}
			Integer stock_status=0;
			if(StringUtils.isNotBlank(default_color)){
				Map<String, Object> priceMap = colorPrice.get(default_color);
				Float orign_price=(Float)priceMap.get("orign_price");
				Float sale_price=(Float)priceMap.get("sale_price");
				String price_unit = (String)priceMap.get("price_unit");
				Integer save = (Integer)priceMap.get("save");
				rebody.setPrice(new Price(orign_price, save, sale_price, price_unit));
				
				Integer stock_number = 0;
				for (Map.Entry<String, Integer>  entry: colorIdStock.entrySet()) {
					stock_number+=entry.getValue();
				}
				if(stock_number>0){
					stock_status=1;
				}
				
			}else{
				//单品, 解析dom树,封装单品价格
				itemPrice(doc,rebody,context);
				
				//单品 stock  
				Elements buttonElements = doc.select("span.product-details__badge.product-details__badge--sold-out");
				if(CollectionUtils.isNotEmpty(buttonElements)){
					String text = buttonElements.get(0).text();
					if(StringUtils.containsIgnoreCase(SOLD_OUT, text)){
						stock_status=0;
					}
				}
				buttonElements = doc.select("button.button.button--large.button--primary.button--extended.button--with-arrow");
				if(CollectionUtils.isNotEmpty(buttonElements)){
					String buttonValue = buttonElements.attr("value");
					if("add_to_cart".equals(buttonValue)){
						stock_status=1;
					}
				}
			}
			
			
			//stock
			rebody.setStock(new Stock(stock_status));
			
			//brand
			rebody.setBrand(new Brand(brand, "","",""));
			
			//title
			rebody.setTitle(new Title(title, "","",""));
			
			
			// full doc info
			String docid = SpiderStringUtil.md5Encode(url);
			String url_no = SpiderStringUtil.md5Encode(url);
			rebody.setDOCID(docid);
			rebody.setSite(new Site(domain));
			rebody.setProdUrl(new ProdUrl(url, System.currentTimeMillis(), url_no));
			
			
			List<String> cats = new ArrayList<String>();
			List<String> breads = new ArrayList<String>();
			Elements cateElements = doc.select("div.breadcrumbs span:not(:first-child)");
			if (CollectionUtils.isNotEmpty(cateElements)) {
				for (Element element : cateElements) {
					String cat = Native2AsciiUtils.ascii2Native(element.text());
					if(StringUtils.isNotBlank(cat)){
						cats.add(cat);
						breads.add(cat);
					}
				}
			}
			rebody.setCategory(cats);
			// BreadCrumb
			breads.add(brand);
			rebody.setBreadCrumb(breads);
			// description
			Map<String, Object> featureMap = new HashMap<String, Object>();
			Map<String, Object> descMap = new HashMap<String, Object>();
			Document document = Jsoup.parse(content);
			Elements es = document.select("div.product-details__description-body > ul > li");
			StringBuilder sb = new StringBuilder();
			if (es != null && es.size() > 0) {
				int count = 1;
				for (Element e : es) {
					featureMap.put("feature-" + count, e.text());
					count++;
					sb.append(e.text());
				}
			}
			rebody.setFeatureList(featureMap);
			descMap.put("en", sb.toString());
			rebody.setDescription(descMap);
			
			String  gender=StringUtils.EMPTY;
			Map<String, Object> propMap = new HashMap<String, Object>();
			gender=getSex(breads.toString());
			gender=getSex(sb.toString());
			propMap.put("s_gender", gender);
			es = document.select("div.product-details__sub-section-content > p");
			if (es != null && es.size() > 0) {
				for (Element e : es) {
					String key = StringUtils.trim(StringUtils.substringBefore(e.text(), ":"));
					String value = StringUtils.trim(StringUtils.substringAfter(e.text(), ":"));
					if (StringUtils.isNotBlank(key) && StringUtils.isNotBlank(value)) {
						propMap.put(key, value);
					}
				}
			}
			rebody.setProperties(propMap);
			
			sku.setL_selection_list(l_selection_list);
			sku.setL_style_list(l_style_list);
			rebody.setSku(sku);
		}
		setOutput(context, rebody);
	}
	
	private String getSex(String cat) {
		String gender = StringUtils.EMPTY;
		if(StringUtils.containsIgnoreCase(cat, SEX_WOMEN)){
			gender = "women";
		} else if(StringUtils.containsIgnoreCase(cat, SEX_WOMEN2)) {
			gender = "women";
		} else if(StringUtils.containsIgnoreCase(cat, SEX_WOMEN3)) {
			gender = "women";
		} else if(StringUtils.containsIgnoreCase(cat, SEX_MEN)) {
			gender = "men";
		} else if(StringUtils.containsIgnoreCase(cat, SEX_MEN2)) {
			gender = "men";
		} 
		return gender;
	}
	
	/**
	 * 单品详情,价格封装
	 * @param doc
	 * @param rebody
	 * @param context 
	 */
	private void itemPrice(Document doc, RetBody rebody, Context context) {
		boolean priceboo=false;
		String salePrice=StringUtils.EMPTY;
		String orignPrice=StringUtils.EMPTY;
		String save=StringUtils.EMPTY;
		try {
			Elements salePriceElement = doc.select("p[class=product-details__price product-details__price--sell]");
			Elements orignPriceElement = doc.select("p[class=product-details__price product-details__price--msrp]");
			Elements saveElement = doc.select("p[class=product-details__price product-details__price--percent-saved]");
			if(CollectionUtils.isNotEmpty(salePriceElement)){
				salePrice=salePriceElement.get(0).text();
			}
			if(CollectionUtils.isNotEmpty(orignPriceElement)){
				orignPrice=orignPriceElement.get(0).text();
			}
			if(CollectionUtils.isNotEmpty(saveElement)){
				save=saveElement.get(0).text();
			}
			if(StringUtils.isBlank(orignPrice)){
				orignPrice=salePrice;
			}
			String currency=salePrice.substring(0, 1);
			String price_unit = Currency.codeOf(currency).name();
			save=save.replaceAll("[Save,%]", "").trim();
			
			salePrice = salePrice.replaceAll("[$,]", "");
			orignPrice = orignPrice.replaceAll("[$,]", "");
			//price a-b
			if(StringUtils.contains(salePrice, priceSplit)){// saleprice  a–b   choise b
				salePrice=salePrice.split(priceSplit)[1].trim();
				priceboo=true;
			}
			if(StringUtils.contains(orignPrice, priceSplit)){// orignPrice  a–b   choise a
				orignPrice=orignPrice.split(priceSplit)[0].trim();
				priceboo=true;
			}
			
			if(StringUtils.contains(salePrice, priceSplit2)){// saleprice  a-b   choise b
				salePrice=salePrice.split(priceSplit2)[1].trim();
				priceboo=true;
			}
			if(StringUtils.contains(orignPrice, priceSplit2)){// orignPrice  a-b   choise a
				orignPrice=orignPrice.split(priceSplit2)[0].trim();
				priceboo=true;
			}
			
			Float orign_price=Float.parseFloat(orignPrice);
			Float sale_price=Float.parseFloat(salePrice);
			if(priceboo){
				save =String.valueOf(Math.round((1 - sale_price / orign_price) * 100));// discount
			}
			if(orign_price<sale_price){
				orign_price=sale_price;
			}
			rebody.setPrice(new Price(orign_price, StringUtils.isBlank(save)?0:Integer.parseInt(save), sale_price, price_unit));
		}catch(Exception e){
			logger.error(" item price analysis error url:{}",context.getCurrentUrl());
		}
	}

	/**
	 * replace
	 * @param colorName
	 * @return
	 */
	private String replaceColorName(String colorName) {
		String color=StringUtils.EMPTY;
		color=colorName.replaceAll(" ", "+");
		return color;
	}
}
