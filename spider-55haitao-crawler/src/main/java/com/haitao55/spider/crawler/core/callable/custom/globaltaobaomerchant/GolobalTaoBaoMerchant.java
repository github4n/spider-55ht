package com.haitao55.spider.crawler.core.callable.custom.globaltaobaomerchant;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.common.gson.bean.taobao.TBMerchantBody;
import com.haitao55.spider.common.http.Crawler;
import com.haitao55.spider.common.http.HttpException;
import com.haitao55.spider.common.http.HttpMethod;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.common.cache.ProxyCache;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Proxy;
import com.haitao55.spider.crawler.utils.Constants;

/**
 * 淘宝商家爬取
 * @author denghuan
 *
 */
public class GolobalTaoBaoMerchant extends AbstractSelect{
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);

	private static final String domain = "rate.taobao.com";
	
	@Override
	public void invoke(Context context) throws Exception {
		//String content = this.getInputString(context);
		String url = StringUtils.substringBefore(context.getCurrentUrl(),"?");
		String content = getContent(context,url);
		String shopName = StringUtils.substringBetween(url, "shopName=", "&");
		if(StringUtils.isBlank(shopName)){
			shopName = StringUtils.substringAfter(context.getCurrentUrl(),"shopName=");
		}
		TBMerchantBody tbMerchant = new TBMerchantBody();
		if(StringUtils.isNotBlank(content)){
			Document doc = Jsoup.parse(content);
			
			String shopId = StringUtils.substringBetween(content, "shopID\": \"", "\"");
			String wangWang = doc.select("span.J_WangWang").attr("data-nick");
			String docid = StringUtils.EMPTY;
			if(StringUtils.isNotBlank(shopId)){
				docid = SpiderStringUtil.md5Encode(domain+shopId);
			}else{
				docid = SpiderStringUtil.md5Encode(url);
			}
		
			logger.info("crawler GolobalTaoBaoMerchant Info --->>> shopId :{} , wangWang :{}",shopId,wangWang);
			if(StringUtils.isBlank(shopId) && 
					StringUtils.isNotBlank(wangWang)){
				return;
			}
			
			String sellerCredit = doc.select(".info-block ul.sep li").get(0).text();//卖家信用
			if(StringUtils.isNotBlank(sellerCredit)){
				sellerCredit = pattern(sellerCredit);
			}
			String sellerGrade = doc.select(".info-block ul.sep li a img").get(0).attr("src");
			if(StringUtils.isNotBlank(sellerGrade)){
				sellerGrade = StringUtils.substring(sellerGrade, sellerGrade.lastIndexOf("/")+1,sellerGrade.lastIndexOf("."));
			}
			
			String buyersCredit = doc.select(".info-block ul.sep li").get(1).text();//买家信用
			if(StringUtils.isNotBlank(buyersCredit)){
				buyersCredit = pattern(buyersCredit);
			}
			
			String goodRate = StringUtils.substringBetween(content, "好评率：", "</em>");
			
			String sales = StringUtils.EMPTY;
			Elements  es = doc.select(".menu-list .menu-tab li");
			for(Element e :es){
				String halfYear = e.text();
				if("最近半年".equals(halfYear)){
					String salesVolume =e.attr("rel");
					if(StringUtils.isNotBlank(salesVolume)){
						sales = StringUtils.substringBetween(salesVolume, "[[", ",");
					}
				}
			}
			
			tbMerchant.setDOCID(docid);
			tbMerchant.setUrl(url);
			tbMerchant.setBuyersCredit(buyersCredit);
			tbMerchant.setGoodRate(goodRate);
			tbMerchant.setSalesVolume(sales);
			tbMerchant.setSellerCredit(sellerCredit);
			tbMerchant.setSellerGrade(sellerGrade);
			tbMerchant.setShopName(shopName);
			tbMerchant.setWangWangName(wangWang);
			
			setOutput(context, tbMerchant.parseTo());
		}
	}

	private  String pattern(String sellerCredit){
		Pattern pattern = Pattern.compile("(\\d+)");
		Matcher matcher = pattern.matcher(sellerCredit);
		if(matcher.find()){
			return matcher.group(1);
		}
		return null;
	}
	
	private String getContent(Context context,String url) throws ClientProtocolException, HttpException, IOException{
		String content = StringUtils.EMPTY;
		String proxyRegionId = context.getUrl().getTask().getProxyRegionId();
		if(StringUtils.isBlank(proxyRegionId)){
			content = Crawler.create().timeOut(60000).url(url).header(getHeaders())
					.resultAsString();
		}else{
			Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
			String proxyAddress=proxy.getIp();
			int proxyPort=proxy.getPort();
			content = Crawler.create().timeOut(60000).url(url).header(getHeaders())
					.proxy(true).proxyAddress(proxyAddress).proxyPort(proxyPort).resultAsString();
		}
		return content;
	}
	
	
	public static void main(String[] args) throws ClientProtocolException, HttpException, IOException {
		String html = Crawler.create().timeOut(20000).proxy(true).proxyAddress("114.55.57.110").proxyPort(3128).retry(3).proxy(true).proxyAddress("114.55.57.110").proxyPort(3128).header(getHeaders()).url("https://rate.taobao.com/user-rate-UMmNSMmcSvGvL.htm").method(HttpMethod.GET.getValue()).resultAsString();
		System.out.println(html);
	}
	private static Map<String,Object> getHeaders(){
		final Map<String, Object> headers = new HashMap<String, Object>();
		headers.put("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/58.0.3029.81 Chrome/58.0.3029.81 Safari/537.36");
		 headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		 headers.put("Upgrade-Insecure-Requests", "1");
		 headers.put("method", "GET");
		 headers.put("scheme", "https");
		 //headers.put("path", "/user-rate-UMmcLvCIuvCgT.htm?spm=a1z10.1-c-s.0.0.ZDBHtr");
		// headers.put("referer", "https://hongfen.taobao.com/shop/view_shop.htm?spm=5148.7631246.0.0.L0s3Bm&user_number_id=47622728");
		 headers.put("Cookie", "UM_distinctid=15ab65857760-061c98f667218e-24414032-100200-15ab6585777351; thw=cn; l=AvT0J5ToJNGiB6CWyfLppMi0RLlk-hi3; swfstore=259156; v=0; _tb_token_=e38546eee64d0; uc3=sg2=Aia0IyCbeyanlMvwo267v7MJiaWscD1gMEFDQ2tDaUc%3D&nk2=FOx7MKOqvZsVZw%3D%3D&id2=UNN9c44Ios43wg%3D%3D&vt3=F8dBzWCvztKrtGUUSaU%3D&lg2=WqG3DMC9VAQiUQ%3D%3D; existShop=MTQ5ODE4NjY3Mg%3D%3D; uss=BdWLJ9vkiWU0wCwWz0ARXTbsm2P4bBNxq39EX47Dd0ma1LOdlbhu%2Fh%2Fq; lgc=wuwuhaitao; tracknick=wuwuhaitao; cookie2=1cbe6bfe1c32e3eec465c3c1642d3d09; sg=o3a; cookie1=AC4IrixFUt0fN6Fc%2FuQCUkikQk5WbiKvJpGqwRuk3ZE%3D; unb=3306158653; skt=bd4e9de9b9490b05; t=cb93db966345e39ee319070f6bf3de96; _cc_=U%2BGCWk%2F7og%3D%3D; tg=4; _nk_=wuwuhaitao; _l_g_=Ug%3D%3D; cookie17=UNN9c44Ios43wg%3D%3D; cna=r+iREAyNomYCAcDzdxuepNvX; mt=ci=0_1; uc1=cookie14=UoW%2BsOCLZqoE8g%3D%3D&lng=zh_CN&cookie16=Vq8l%2BKCLySLZMFWHxqs8fwqnEw%3D%3D&existShop=false&cookie21=Vq8l%2BKCLiw%3D%3D&tag=8&cookie15=V32FPkk%2Fw0dUvg%3D%3D&pas=0; x=e%3D1%26p%3D*%26s%3D0%26c%3D0%26f%3D0%26g%3D0%26t%3D0%26__ll%3D-1%26_ato%3D0; isg=AmhoxzKepUfdU4i3IBqQaPK6OV_WgcvJ0ghXqCKZtOPWfQjnyqGcK_6_Aytw; whl=-1%260%260%261498186685799");
		return headers;
	}
}
