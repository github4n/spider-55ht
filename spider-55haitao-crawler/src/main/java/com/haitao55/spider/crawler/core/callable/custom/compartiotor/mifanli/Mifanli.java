package com.haitao55.spider.crawler.core.callable.custom.compartiotor.mifanli;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.haitao55.spider.common.gson.bean.LImageList;
import com.haitao55.spider.common.gson.bean.Picture;
import com.haitao55.spider.common.gson.bean.Site;
import com.haitao55.spider.common.gson.bean.competitor.CtorProdUrl;
import com.haitao55.spider.common.gson.bean.competitor.CtorRetBody;
import com.haitao55.spider.common.gson.bean.competitor.CtorTitle;
import com.haitao55.spider.common.gson.bean.competitor.Mall;
import com.haitao55.spider.common.gson.bean.competitor.PromoCode;
import com.haitao55.spider.common.gson.bean.competitor.Tag;
import com.haitao55.spider.common.http.HttpException;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.exception.CrawlerException.CrawlerExceptionCode;
import com.haitao55.spider.crawler.exception.ParseException;
import com.haitao55.spider.crawler.utils.HttpUtils;
import com.haitao55.spider.crawler.utils.JsoupUtils;
import com.haitao55.spider.crawler.utils.Native2AsciiUtils;

/**
 * mifanli 详情封装 Title: Description: Company: 55海淘
 * 
 * @author zhaoxl
 * @date 2017年4月18日 上午10:34:18
 * @version 1.0
 */
public class Mifanli extends AbstractSelect {
	private static final String URL_PREFFIX = "http://www.mifanli.com";
	private static final String DOMAIN = "www.mifanli.com";
	private String tag;

	public void invoke(Context context) throws ClientProtocolException, HttpException, IOException {
		String url = context.getCurrentUrl().toString();
		String content = HttpUtils.crawler_package(context, getHeaders());
		Document doc = JsoupUtils.parse(content);

		if (StringUtils.contains(content, "已过期")) {
			throw new ParseException(CrawlerExceptionCode.OFFLINE,
					"mifanli.com itemUrl:" + context.getUrl().toString() + " not found..");
		}
		
		CtorRetBody ctorRetBody = new CtorRetBody();

		String deputy_title = StringUtils.EMPTY;
		Elements deputyTitleElements = doc.select("h1 span.red");
		if (CollectionUtils.isNotEmpty(deputyTitleElements)) {
			deputy_title = deputyTitleElements.text();
		}
		// title
		String main_title = StringUtils.EMPTY;
		Elements mainTitleElements = doc.select("div.r h1");
		if (CollectionUtils.isNotEmpty(mainTitleElements)) {
			main_title = mainTitleElements.text();
			main_title = StringUtils.substringBefore(main_title, deputy_title);
		}

		// 优惠码
		List<String> promoCodes = new ArrayList<String>();
		Elements promoCodeElements = doc.select("div.discount_codes span.copy-discount");
		if (CollectionUtils.isNotEmpty(promoCodeElements)) {
			for (Element element : promoCodeElements) {
				promoCodes.add(element.attr("data-clipboard-text"));
			}
		}

		// 购买链接
		String buy_url = StringUtils.EMPTY;
		Elements buyUrlElements = doc.select("div.op a");
		if (CollectionUtils.isNotEmpty(buyUrlElements)) {
			buy_url = buyUrlElements.attr("href");
			if (!StringUtils.contains(buy_url, "http")) {
				buy_url = URL_PREFFIX + buy_url;
			}
		}

		// 来源链接
		String parent_url = context.getUrl().getParentUrl().toString();

		long now = System.currentTimeMillis();
		Date date = new Date(now);
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String discovery_time = format.format(date);

		// 更新时间 update_time
		String update_time = StringUtils.EMPTY;
		Elements updateTimeElments = doc.select("div.s span.pubtime");
		if (CollectionUtils.isNotEmpty(updateTimeElments)) {
			String update_time_text = updateTimeElments.text();
			update_time_text = StringUtils.substringAfter(update_time_text, "：");
			update_time = update_time_text;
		}

		// end_time
		String end_time = StringUtils.EMPTY;
		Elements endTimeElements = doc.select("div.cutoff_time");
		if (CollectionUtils.isNotEmpty(endTimeElements)) {
			end_time = endTimeElements.text();
			end_time = StringUtils.substringAfter(end_time, "促销截止至北京时间");
		}

		// image
		List<Picture> l_image_list = new ArrayList<Picture>();
		Elements imageElements = doc.select("div.thumb>a>img");
		if (CollectionUtils.isNotEmpty(imageElements)) {
			l_image_list.add(new Picture(imageElements.attr("src")));
		}

		// 分类
		List<String> cats = new ArrayList<String>();
		Elements categoryElements = doc.select("span.cate a");
		if (CollectionUtils.isNotEmpty(categoryElements)) {
			for (Element element : categoryElements) {
				String str = StringEscapeUtils.unescapeHtml(Native2AsciiUtils.ascii2Native(element.text()));
				if (StringUtils.isNotBlank(str)) {
					cats.add(str);
				}
			}
		}

		// 商城
		String mall = StringUtils.EMPTY;
		Elements mallElements = doc.select("div.s span.store");
		if (CollectionUtils.isNotEmpty(mallElements)) {
			mall = mallElements.text();
			if (StringUtils.isNotBlank(mall)) {
				mall = StringUtils.substringAfter(mall, "：");
			}
		}

		// 内容
		Map<String, Object> featureMap = new HashMap<String, Object>();
		Elements contentElements = doc
				.select("div.content div.p h3,div.content div.p p,div.content>h3,div.content div.p:last-child");
		StringBuilder sb = new StringBuilder();
		if (contentElements != null && contentElements.size() > 0) {
			int count = 1;
			for (Element e : contentElements) {
				String text = e.text();
				if (StringUtils.isNotBlank(text) && !StringUtils.equals(" ", text)) {
					featureMap.put("feature-" + count, text);
					count++;
					sb.append(text);
				}
			}
		}

		ctorRetBody.setDOCID(SpiderStringUtil.md5Encode(url));

		ctorRetBody.setSite(new Site(DOMAIN));
		ctorRetBody.setTag(new Tag(tag));
		ctorRetBody.setFeatureList(featureMap);

		ctorRetBody.setCtorProdUrl(new CtorProdUrl(url, parent_url, buy_url, discovery_time, update_time, end_time));

		ctorRetBody.setImage(new LImageList(l_image_list));

		ctorRetBody.setPromoCode(new PromoCode(promoCodes));

		ctorRetBody.setCtorTitle(new CtorTitle(main_title, deputy_title));

		ctorRetBody.setCategory(cats);

		ctorRetBody.setMall(new Mall(mall));

		setOutput(context, ctorRetBody.parseTo());

	}

	private Map<String, Object> getHeaders() {
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
				// put("Cookie",
				// "__gads=ID=6317950096b83888:T=1487555926:S=ALNI_MbSXcNaILJorNUxthhWYhDRx6JjtQ;
				// PHPSESSID=66f143f821916269de8a42b564b3b7aa;
				// _ga=GA1.2.421631721.1487555928; _gat=1; rip=D;
				// Hm_lvt_aa1bd5db226a1bae87a0ffc02cee3d7b=1487555927;
				// Hm_lpvt_aa1bd5db226a1bae87a0ffc02cee3d7b=1487675306;
				// OX_plg=pm");
				put("User-Agent",
						"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/55.0.2883.87 Chrome/55.0.2883.87 Safari/537.36");
			}
		};
		return headers;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}
}
