package com.haitao55.spider.crawler.core.callable.custom.amazon.html;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.core.callable.SelectUrls;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.utils.Constants;
import com.haitao55.spider.crawler.utils.HttpUtils;

/**
 * 
  * @ClassName: AmazonSelectUrls
  * @Description: 亞馬遜的列表頁面選取item
  * @author songsong.xu
  * @date 2016年10月12日 下午8:43:46
  *
 */
public class AmazonSelectUrls extends SelectUrls {
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private String ITEM_URL = "https://www.#domain#/dp/#itemId#/";

	@Override
	public void invoke(Context context) throws Exception {

		String content = super.getInputString(context);
		String currUrl = context.getUrl().getValue();
		String domain = SpiderStringUtil.getAmazonDomain(currUrl);
		if(StringUtils.isBlank(domain)){
			logger.error("Error while fetching domain from url {}",currUrl);
			return;//無域名無法爬取
		}
		String itemUrlTmp = ITEM_URL.replace("#domain#", domain);
		List<String> newUrlValues = new ArrayList<String>();
		//one links
		Pattern p = Pattern.compile("http(.*?)/mn/search/ajax(.*?)section=BTF(.*?)");
		Matcher m = p.matcher(currUrl);
		if(m.find()){
			p = Pattern.compile("<li[^>]*id=[^>]*result_[^>]*data-asin=\\\\\"(.*?)\\\\\"(.*?)</div></li>");
			m = p.matcher(content);
			while(m.find()){
				String itemId = m.group(1);
				String itemUrl = itemUrlTmp.replace("#itemId#", itemId);
				newUrlValues.add(itemUrl);
			}
		
		}
		
		//other links
		Pattern otherp = Pattern.compile("http(.*?)&pg=(.*?)&ajax=1(.*?)");
		Matcher otherm = otherp.matcher(currUrl);
		if(otherm.find()){
			otherp = Pattern.compile("<div[^>]*zg_title[^>]*>[^<]*<a[^>]*/dp/(.*?)/[^>]*>");
			otherm = otherp.matcher(content);
			while(otherm.find()){
				String itemId = otherm.group(1);
				String itemUrl = ITEM_URL.replace("#domain#", domain).replace("#itemId#", itemId);
				newUrlValues.add(itemUrl);
			}
		}
		Set<Url> newUrls = this.buildNewUrls(newUrlValues, context, type, grade);
		context.getUrl().getNewUrls().addAll(newUrls);
		logger.info("amazon list url:{} ,get items :{}", context.getUrl().getValue(), newUrlValues.size());
		
	}

	public static void main(String[] args) throws UnsupportedEncodingException {
		String url = "https://www.amazon.com/mn/search/ajax/?rh=n%3A165796011%2Cn%3A!165797011%2Cn%3A166764011%2Cn%3A2237474011&page=33&ie=UTF8&qid=1444994300&fromHash=&fromRH=n%3A165796011%2Cn%3A!165797011%2Cn%3A166764011%2Cn%3A2237474011&section=BTF&fromApp=gp%2Fsearch&fromPage=results&fromPageConstruction=auisearch&version=2";
		//https://www.amazon.com/Best-Sellers-Electronics-Tablet-Accessories/zgbs/electronics/2348628011/ref=zg_bs_2348628011_pg_2?_encoding=UTF8&pg=2&ajax=1&isAboveTheFold=0
		String content = HttpUtils.get(url);
		//Pattern p = Pattern.compile("<li[^>]*id=[^>]*result_[^>]*data-asin=\\\\\"(.*?)\\\\\"(.*?)</div></li>");
		Pattern p = Pattern.compile("<li[^>]*id=[^>]*result_[^>]*data-asin=\\\\\"(.*?)\\\\\"(.*?)</div></li>");
		Matcher m = p.matcher(content);
		while(m.find()){
			String itemId = m.group(1);
			System.out.println(itemId);
		}
		/*url = "/https://www.amazon.com/Best-Sellers-Electronics-Tablet-Accessories/zgbs/electronics/2348628011/ref=zg_bs_2348628011_pg_2?_encoding=UTF8&pg=2&ajax=1&isAboveTheFold=0";
		Pattern pattern = Pattern.compile("http(.*?)&pg=(.*?)&ajax=1(.*?)");
		Matcher matcher = pattern.matcher(url);
		if(matcher.find()){
			System.out.println("==================");
		}*/
	}

}