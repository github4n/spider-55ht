package com.haitao55.spider.crawler.core.callable.custom.sportdirect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
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
import com.haitao55.spider.common.utils.DetailUrlCleaningTool;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.callable.custom.mankind.CrawlerUtils;
import com.haitao55.spider.crawler.utils.Constants;
import com.mashape.unirest.http.Unirest;

/**
 * @Description:
 * @author: zhoushuo
 * @date: 2017年5月24日 下午5:57:20
 */
public class Sportdirect extends AbstractSelect {

	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private static final String domain = "www.sportsdirect.com";

	@Override
	public void invoke(Context context) throws Exception {
		try {
			String currentUrl = context.getCurrentUrl();
			currentUrl = DetailUrlCleaningTool.getInstance().cleanDetailUrl(currentUrl);
			Map<String, String> header = new HashMap<>();
			header.put("Cookie", ".ASPXANONYMOUS=AMaBGU0K0wEkAAAAMTgwNWQxNjQtZDFhYS00MDBjLWExM2MtZDZmODkzMzUwZjgy0; SportsDirect_AuthenticationCookie=89c70434-db35-4447-aa42-441384b6d9d5; acceptedCookies=true; SportsDirect_ClearedCookies=1; AddForcedProductItemToBasketCookie=YES; CountryRedirectCheckIsDone=true; ASP.NET_SessionId=h2aragaz23vib5dgc42gjo20; AdvertCookie=true; ChosenSite=us; SportsDirect_AnonymousUserCurrency=GBP; _uetsid=_uet539e1e0a; _ga=GA1.2.910959560.1495540440; _gid=GA1.2.1358424910.1495876265; um_IsMobile=False; language=en-GB; X-SD-URep=e16664ac-a93b-4017-b194-4031874d6fbb; TS01a19d95=01e4dc9a769ea0e2a00e9864a1d0903be979d913bfa4f3f5d67fabf76dc51d39fdcd1b5536eb05a5a580b4e1b1a0b65d2e8bcc114275e9f55cdf4c1685baeb88d8a74a506f7efb38803c5bd1e6e4141abaff74837ecc25b7850a2e547f22f1203f63126494434fea6de8fc9d609950e317be5882dcaa07e63a8e4399cab43829db5e98bee058fd83f406d744d2f373ccef645ad2ae8d0533a7bc4fea11074020d3d88af54d359269d28be37850a87046a2d077ff6ef080addd0a5620adfe990531303bb39d; ak_bmsc=857E7DAEFD9EC5008D8C2AB93DD7B58B687606379D340000D1302959DDF1D236~pld8WvxUQCP2KvqmezjJWtm2XLAjpyK3aRfj61FUzHf8xMWxmqXjug0hZB31iE8bf/73RddQ1cVOC0gd7aDpFDKgVrxi8Wtbt4Ew7/NYMXRLY458kk3fuGs1lxKkdifd7IVFLICmGFA15bXlqJNtDjIKaMRAmUL05frDr5SySUgkifEMsGKILgbnuObFrbeZdFttRebjqmOabmx9JVbKGmDw==");
			String content = Unirest.get(currentUrl).headers(header).asString().getBody();
			RetBody retbody = new RetBody();
			if (StringUtils.isNotBlank(content)) {
				Document document = Jsoup.parse(content, currentUrl);
				JSONObject keywordJson = JSONObject
						.parseObject(StringUtils.substringBetween(content, "dataLayer.push(", ");"));
				String productID = keywordJson.getString("productId");
				String brand = keywordJson.getString("productBrand");
				String gender = StringUtils.EMPTY;
				gender = keywordJson.getString("productGender");
				String title = keywordJson.getString("productName");

				if (StringUtils.isBlank(productID)) {
					logger.error("get productID error, url {}", currentUrl);
					return;
				}
				if (StringUtils.isBlank(title)) {
					logger.error("get title error, url {}", currentUrl);
					return;
				}
				String docid = SpiderStringUtil.md5Encode(domain + productID);
				String url_no = SpiderStringUtil.md5Encode(currentUrl);

				// 设置面包屑和类别
				String breadsStr = document.select("span#dnn_dnnBreadcrumb_siteMap").text();
				List<String> breads = Arrays.asList(breadsStr.split("/"));
				List<String> categories = breads;

				// description and feature
				Map<String, Object> featureMap = new HashMap<String, Object>();
				Map<String, Object> descMap = new HashMap<String, Object>();
				this.dealWithfeatureAndDescription(document, featureMap, descMap);

				// 设置性别
				Map<String, Object> properties = new HashMap<>();
				if (!("Men".equalsIgnoreCase(gender) || "Women".equalsIgnoreCase(gender)))
					gender = "";
				properties.put("s_gender", gender);

				// 获取sku和颜色列表
				List<SportDirectSku> skus = new ArrayList<>();
				List<Style> styles = new ArrayList<>();
				this.dealWithSkus(document, currentUrl, skus, styles);

				// 封装StyleList和SelectionList
				List<LStyleList> l_style_list = new ArrayList<>();
				if (CollectionUtils.isEmpty(styles)) {
					logger.error("styles is empty and url is {}", context.getCurrentUrl());
					return;
				}
				
				String defaultColor = StringUtils.trimToEmpty(
						document.select("span#dnn_ctr103511_ViewTemplate_ctl00_ctl09_colourName").text());
				logger.info("=======defaultColor========{}-----url:{}",defaultColor,currentUrl);
				LImageList image_list = null;
				for (Style s : styles) {
					LStyleList style = new LStyleList();
					if (defaultColor.equalsIgnoreCase(s.getColor())) {
						style.setDisplay(true);
						image_list = CrawlerUtils.getImageList(s.getImgs());
					} else
						style.setDisplay(false);
					style.setGood_id(s.getGoodId());
					style.setStyle_cate_id(0);
					style.setStyle_cate_name("color");
					style.setStyle_id(s.getColor());
					style.setStyle_name(s.getColor());
					style.setStyle_switch_img(s.getSwitch_img());
					context.getUrl().getImages().put(s.getGoodId(), CrawlerUtils.convertToImageList(s.getImgs()));
					l_style_list.add(style);
				}

				float spu_orig_price = 0l;
				float spu_sale_price = 0l;
				int save = 0;
				String spu_Unit = StringUtils.EMPTY;
				int spu_stock_status = 0;

				List<LSelectionList> l_selection_list = new ArrayList<>();
				if (CollectionUtils.isEmpty(skus)) {
					logger.error("skuList is empty and url is {}", context.getCurrentUrl());
					return;
				}
				int count = 0;
				for (SportDirectSku sku : skus) {
					List<Selection> slist = new ArrayList<>();
					Selection sec = new Selection(0, sku.getSize(), "Size");
					slist.add(sec);
					LSelectionList selection = new LSelectionList();
					selection.setStyle_id(sku.getColor());
					selection.setGoods_id(sku.getSkuid());
					selection.setPrice_unit(sku.getUnit());
					selection.setSale_price(sku.getSalePrice());
					if (sku.getOrigPrice() < selection.getSale_price())
						selection.setOrig_price(selection.getSale_price());
					else
						selection.setOrig_price(sku.getOrigPrice());
					selection.setStock_number(sku.getStock());
					selection.setStock_status(sku.getStatus());
					selection.setSelections(slist);
					l_selection_list.add(selection);

					// 设置spu价格和库存
					if (defaultColor.equalsIgnoreCase(sku.getColor()) && count == 0) {
						spu_orig_price = sku.getOrigPrice();
						spu_sale_price = sku.getSalePrice();
						spu_stock_status = 1;
						spu_Unit = sku.getUnit();
						if (spu_orig_price != 0)
							save = Math.round((1 - spu_sale_price / spu_orig_price) * 100);
						count++;
					}
				}

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
				retbody.setPrice(new Price(spu_orig_price, save, spu_sale_price, spu_Unit));
				retbody.setStock(new Stock(spu_stock_status));
				retbody.setSku(new Sku(l_selection_list, l_style_list));
				retbody.setProperties(properties);
			}
			setOutput(context, retbody);
		} catch (Exception e) {
			logger.error("url is {}, reason {}", context.getCurrentUrl(), e.getMessage());
			e.printStackTrace();
		}
	}

	private void dealWithfeatureAndDescription(Document document, Map<String, Object> featureMap,
			Map<String, Object> descMap) {
		String desc = StringUtils.EMPTY;
		Elements des = document.getElementsByAttributeValue("itemprop", "description");
		if (CollectionUtils.isNotEmpty(des)) {
			desc = des.get(0).ownText();
		}
		String[] temp = desc.split(">");
		for (int i = 1; i <= temp.length; i++) {
			featureMap.put("feature" + i, temp[i - 1]);
		}
		descMap.put("en", desc);
	}

	private void dealWithSkus(Document document, String currentUrl, List<SportDirectSku> skus, List<Style> styles) {
		Elements elements = document.select("span.ProductDetailsVariants");
		String skuStr = StringUtils.EMPTY;
		if (CollectionUtils.isNotEmpty(elements)) {
			skuStr = elements.get(0).attr("data-variants");
		}
		skuStr = StringUtils.trimToNull(StringEscapeUtils.unescapeHtml(skuStr)); // 防止出现html的转义字符
		JSONArray skuArray = JSONArray.parseArray(skuStr);
		if (skuArray == null) {
			logger.error("can not get sku info for url {}", currentUrl);
			return;
		}
		int len = skuArray.size();
		for (int i = 0; i < len; i++) {
			JSONObject selectionJson = skuArray.getJSONObject(i);
			String colorId = StringUtils.trimToEmpty(selectionJson.getString("ColVarId"));
			String color = StringUtils.trimToEmpty(document.getElementsByAttributeValue("value", colorId).text());

			// 设置styleList
			Style style = new Style();
			style.setColor(color);
			style.setSwitch_img(selectionJson.getJSONObject("ProdImages").getString("ImgUrlThumbNail"));
			JSONArray imgsJson = selectionJson.getJSONObject("ProdImages").getJSONArray("AlternateImages");
			List<String> imgs = new ArrayList<>();
			if (imgsJson != null) {
				for (int j = 0; j < imgsJson.size(); j++) {
					imgs.add(imgsJson.getJSONObject(j).getString("ImgUrlXXLarge"));
				}
			}
			style.setImgs(imgs);
			styles.add(style);

			// 设置skus
			JSONArray sizeJson = selectionJson.getJSONArray("SizeVariants");
			if (sizeJson != null) {
				for (int j = 0; j < sizeJson.size(); j++) {
					JSONObject obj = sizeJson.getJSONObject(j);
					SportDirectSku sku = new SportDirectSku();
					sku.setColor(color);
					sku.setColorId(colorId);
					sku.setSizeId(obj.getString("SizeVarId"));
					sku.setSize(obj.getString("SizeName"));
					String refPrice = obj.getJSONObject("ProdSizePrices").getString("RefPrice");
					String sellPrice = obj.getJSONObject("ProdSizePrices").getString("SellPrice");
					if (StringUtils.isBlank(refPrice))
						refPrice = sellPrice;
					sku.setOrigPrice(CrawlerUtils.getPrice(refPrice, currentUrl, logger));
					sku.setSalePrice(CrawlerUtils.getPrice(sellPrice, currentUrl, logger));
					sku.setUnit(CrawlerUtils.getUnit(sellPrice, currentUrl, logger));
					sku.setSkuid(sku.getColorId() + sku.getSizeId());
					// if("Green".equals(obj.getString("State"))){
					// //经过测试发现State为Red的时候页面也能添加购物车，故此有无库存判断还有待观察
					// sku.setStatus(1);
					// sku.setStock(0);
					// }
					sku.setStatus(1);
					sku.setStock(0);
					if (j == 0)
						style.setGoodId(sku.getSkuid());
					skus.add(sku);
				}
			}
		}
	}
}
