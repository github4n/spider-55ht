package com.haitao55.spider.crawler.core.callable.custom.kipling;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.alibaba.fastjson.JSONObject;
import com.haitao55.spider.common.http.Crawler;
import com.haitao55.spider.common.http.HttpException;
import com.haitao55.spider.common.http.HttpMethod;
import com.haitao55.spider.common.utils.Currency;
import com.haitao55.spider.crawler.common.cache.ProxyCache;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.core.model.Proxy;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.utils.JsoupUtils;

public class KiplingCallaber implements Callable<JSONObject> {
	
	private static final String stock_flag = "In Stock";
	private static final String color_flag = "dwvar_{}_color=";
	private String param;
	private String productId;
	private Context context;


	public String getParam() {
		return param;
	}

	public void setParam(String param) {
		this.param = param;
	}


	public String getProductId() {
		return productId;
	}

	public void setProductId(String productId) {
		this.productId = productId;
	}
	
	public Context getContext() {
		return context;
	}

	public void setContext(Context context) {
		this.context = context;
	}

	public KiplingCallaber(String param, String productId, Context context) {
		this.param = param;
		this.productId = productId;
		this.context = context;
	}

	@Override
	public JSONObject call() throws Exception {
		//pics
		JSONObject json = new JSONObject();
		String content = crawler_package(param);
		Document doc = JsoupUtils.parse(content);
		
		List<Image> pics =  new ArrayList<Image>();
		
		//sku color id
		String sku_color_id = StringUtils.EMPTY;
		
		String color_flag_temp = StringUtils.replacePattern(color_flag, "\\{\\}", productId);
		//default colorid
		String colorId = StringUtils.substringAfter(param, color_flag_temp);
		
		Elements colorElemets = doc.select("ul.swatches.Color li a");
		if(CollectionUtils.isNotEmpty(colorElemets)){
			for (Element element : colorElemets) {
				String skuColorId = element.attr("data-colorid");
				//匹配当前颜色对应的属性值
				if(StringUtils.containsIgnoreCase(colorId, skuColorId)){
					sku_color_id = skuColorId;
					String color = element.attr("title");
					String switch_img = element.attr("style");
					switch_img = StringUtils.substringBetween(switch_img, "background: url(", ")");
					
					//json put
					json.put("color", color);
					json.put("colorId", skuColorId);
					json.put("switch_img", switch_img);
				}
			}
		}
		
		String salePrice = StringUtils.EMPTY;
		String origPrice = StringUtils.EMPTY;
		String save = StringUtils.EMPTY;
		
		Elements elements = doc.select("span.price-sales");
		if (CollectionUtils.isNotEmpty(elements)) {
			salePrice = elements.get(0).text();
		}
		elements = doc.select("span.price-standard");
		if (CollectionUtils.isNotEmpty(elements)) {
			origPrice = elements.get(0).text();
		}

		String unit = getCurrencyValue(salePrice);// 得到货币代码
		salePrice = salePrice.replaceAll("[$,]", "");
		origPrice = origPrice.replaceAll("[$,]", "");

		if (StringUtils.isBlank(origPrice)) {
			origPrice = salePrice;
		}
		if (StringUtils.isBlank(salePrice)) {
			salePrice = origPrice;
		}
		if (StringUtils.isBlank(origPrice)
				|| Float.valueOf(origPrice) < Float.valueOf(salePrice)) {
			origPrice = salePrice;
		}
		if (StringUtils.isBlank(save)) {
			save = Math.round((1 - Float.valueOf(salePrice) / Float.valueOf(origPrice)) * 100)
					+ "";// discount
		}
		
		int stock_status = 1;
		Elements stockElements = doc.select("p.in-stock-msg");
		if(!StringUtils.containsIgnoreCase(stockElements.text(), stock_flag)){
			stock_status = 0;
		}
		
		//json put
		json.put("sale_price", salePrice);
		json.put("orign_price", origPrice);
		json.put("save", save);
		json.put("unit", unit);
		json.put("stock_status", stock_status);
		
		images_package(pics,sku_color_id,productId,context);
		
		//images
		json.put(sku_color_id, pics);
		
		return json;
	}

	/**
	 * 封装图片地址
	 * @param pics
	 * @param image_preffix
	 * @param sku_color_id
	 * @param productId 
	 */
	private void images_package(List<Image> pics, String sku_color_id, String productId
			,Context context) {
		String[] params = new String[]{"A","B","C","D"};
		String image_suffix ="http://s7d9.scene7.com/is/image/KiplingBrand/xxlarge/";
		for (String attr : params) {
			String imageUrl = image_suffix+productId+"_"+sku_color_id+"_"+attr+".jpg";
			boolean flag = image_exists(imageUrl,context);
			if(flag){
				pics.add(new Image(imageUrl));
			}
		}
	}
	
	/**
	 * 验证图片是否存在
	 * @param image_url
	 * @param context
	 * @return
	 */
	private boolean image_exists(String image_url, Context context) {
		try {
			Url url = context.getUrl();
			String proxyRegionId = url.getTask().getProxyRegionId();
			if(StringUtils.isBlank(proxyRegionId)){
				Crawler.create().timeOut(15000).url(image_url).proxy(false).resultAsString();
			}else{
				Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
				String ip = proxy.getIp();
				int port = proxy.getPort();
				Crawler.create().timeOut(20000).url(image_url).proxy(true).proxyAddress(ip)
				.proxyPort(port).resultAsString();
			}
			return true;
		} catch (ClientProtocolException e) {
		} catch (HttpException e) {
			if(200!=e.getStatus()){
				return false;
			}
		} catch (IOException e) {
		}
		return false;
	}
	
	

	/**
	 * get 货币
	 * 
	 * @param val
	 * @return
	 */
	private static String getCurrencyValue(String val) {
		String currency = StringUtils.substring(val, 0, 1);
		String unit = StringUtils.EMPTY;
		unit = Currency.codeOf(currency).name();
		return unit;

	}
	
	private String crawler_package(String url) throws ClientProtocolException, HttpException, IOException {
		String content = StringUtils.EMPTY;
		String proxyRegionId = context.getUrl().getTask().getProxyRegionId();
		if(StringUtils.isBlank(proxyRegionId)){
			content = Crawler.create().timeOut(15000).url(url).method(HttpMethod.GET.getValue())
					.resultAsString();
		}else{
			Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
			String proxyAddress=proxy.getIp();
			int proxyPort=proxy.getPort();
			content = Crawler.create().timeOut(15000).url(url).method(HttpMethod.GET.getValue())
					.proxy(true).proxyAddress(proxyAddress).proxyPort(proxyPort).resultAsString();
		}
		return content;
	}
}
