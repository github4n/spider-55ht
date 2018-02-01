package com.test.kirnazabete;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.common.gson.bean.Brand;
import com.haitao55.spider.common.gson.bean.ProdUrl;
import com.haitao55.spider.common.gson.bean.RetBody;
import com.haitao55.spider.common.gson.bean.Site;
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
 * @date: 2017年3月13日 下午3:29:15  
 */
public class KirnazabeteTest  extends AbstractSelect {
	
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private static final String domain = "www.kirnazabete.com";

	private static final String CSS_TITLE = "div.product-name h2";
	private static final String CSS_BREADS = "nav.breadcrumbs li";
	private static final String CSS_DESCRIPTION = "div#details div.details-tab-details p";
	private static final String CSS_DETAIL = "div#details div.details-tab-details li";
	private static final String CSS_DEFAULT_COLOR = "div.product-variations ul.all-attributes li ul.color li.selected a";

	@Override
	public void invoke(Context context) throws Exception {
		String currentUrl = context.getCurrentUrl();
		currentUrl = DetailUrlCleaningTool.getInstance().cleanDetailUrl(currentUrl);
		// String content = super.getInputString(context);
		String content = HttpUtils.get(currentUrl, 30000, 1, null);
		RetBody retbody = new RetBody();
		if(StringUtils.isNotBlank(content)){
			Document document = Jsoup.parse(content, currentUrl);
			String title = CrawlerUtils.setTitle(document, CSS_TITLE, currentUrl, logger);
			String productID = CrawlerUtils.getProductId(currentUrl);
			if (StringUtils.isBlank(productID)) {
				logger.error("get productID error,and url is {}",currentUrl);
				return;
			}
			String docid = SpiderStringUtil.md5Encode(domain + productID);
			String url_no = SpiderStringUtil.md5Encode(currentUrl);
			
			// 设置面包屑和类别
			List<String> breads = new ArrayList<String>();
			List<String> categories = new ArrayList<String>();
			Elements ebread = document.select(CSS_BREADS);
			if(CollectionUtils.isNotEmpty(ebread)){
				
			}
			for (Element e : ebread) {
				String breadbrums = e.text().replace("·", "");
				breads.add(e.text());
				categories.add(e.text());
			}

			// 设置retbody
			retbody.setDOCID(docid);
			retbody.setSite(new Site(domain));
			retbody.setProdUrl(new ProdUrl(currentUrl, System.currentTimeMillis(), url_no));
			retbody.setTitle(new Title(title, "", "", ""));
			retbody.setBrand(new Brand("Cole Haan", "", "", ""));
			retbody.setBreadCrumb(breads);
			retbody.setCategory(categories);
			System.err.println(retbody.parseTo());
		}
	}
	
	public static void main(String[] args) throws Exception {
		KirnazabeteTest kt = new KirnazabeteTest();
		Context context = new Context();
		context.setCurrentUrl("http://www.kirnazabete.com/kzloves/march-must-haves/embroidered-denim-shorts");
		context.setCurrentUrl("http://www.kirnazabete.com/shoes/pumps/metallic-gold-sandal");
		kt.invoke(context);
	}
}
