package com.haitao55.spider.crawler.core.callable.custom.compartiotor.rebatesme;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
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
 * rebatesme 详情 Title: Description: Company: 55海淘
 * 
 * @author zhaoxl
 * @date 2017年4月18日 下午2:09:22
 * @version 1.0
 */
public class Rebatesme extends AbstractSelect {
	private static final String URL_PREFFIX = "http://www.rebatesme.com";
	private static final String DOMAIN = "www.rebatesme.com";
	private String tag;

	public void invoke(Context context) throws ClientProtocolException, HttpException, IOException {
		String url = context.getCurrentUrl().toString();
		String content = HttpUtils.crawler_package(context, getHeaders());
		// String content = HttpUtils.get(url);
		Document doc = JsoupUtils.parse(content);

		if (StringUtils.contains(content, "已过期")) {
			throw new ParseException(CrawlerExceptionCode.OFFLINE,
					"rebatesme.com itemUrl:" + context.getUrl().toString() + " not found..");
		}

		CtorRetBody ctorRetBody = new CtorRetBody();

		String deputy_title = StringUtils.EMPTY;
		// title
		String main_title = StringUtils.EMPTY;
		Elements mainTitleElements = doc.select("div.deals-detail-left div.deals-detail-left-title h1");
		if (CollectionUtils.isNotEmpty(mainTitleElements)) {
			main_title = mainTitleElements.text();
		}

		// 优惠码
		List<String> promoCodes = new ArrayList<String>();
		Elements promoCodeElements = doc.select("div.deals-detail-left-code span.copycoupon");
		if (CollectionUtils.isNotEmpty(promoCodeElements)) {
			for (Element element : promoCodeElements) {
				promoCodes.add(element.attr("data-code"));
			}
		}

		// 购买链接
		String buy_url = StringUtils.EMPTY;
		Elements buyUrlElements = doc.select("div.deals-detail-right div:nth-child(2)");
		if (CollectionUtils.isNotEmpty(buyUrlElements)) {
			buy_url = buyUrlElements.attr("onclick");
			if (StringUtils.isNotBlank(buy_url)) {
				buy_url = StringUtils.substringBetween(buy_url, "'", "'");
			}
			if (!StringUtils.contains(buy_url, "http")) {
				buy_url = URL_PREFFIX + buy_url;
			}
		}

		// 来源链接
		String parent_url = StringUtils.EMPTY;

		long now = System.currentTimeMillis();
		Date date = new Date(now);
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String discovery_time = format.format(date);

		// 更新时间 update_time
		String update_time = StringUtils.EMPTY;
		Elements updateTimeElments = doc.select("div.deals-detail-right div:nth-child(1)");
		if (CollectionUtils.isNotEmpty(updateTimeElments)) {
			String update_time_text = updateTimeElments.text();
			update_time = update_time_text;
		}

		// end_time
		String end_time = StringUtils.EMPTY;
		Elements endTimeElements = doc.select("div.time.deals-end-time");
		if (CollectionUtils.isNotEmpty(endTimeElements)) {
			end_time = getEndTime(doc, endTimeElements);
		}
		if (StringUtils.isBlank(end_time)) {
			end_time = StringUtils.substringBetween(content, "北京时间", "。");
		}

		if (StringUtils.isBlank(end_time)) {
			end_time = StringUtils.substringBetween(content, "截止至", "。");
		}

		// image
		List<Picture> l_image_list = new ArrayList<Picture>();
		Elements imageElements = doc.select("div.deals-detail-right a>img");
		if (CollectionUtils.isNotEmpty(imageElements)) {
			l_image_list.add(new Picture(imageElements.attr("src")));
		}

		// 分类
		List<String> cats = new ArrayList<String>();
		Elements categoryElements = doc.select("div.site a");
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
		Elements mallElements = doc.select("div.site a");
		if (CollectionUtils.isNotEmpty(mallElements)) {
			mall = mallElements.get(mallElements.size() - 1).text();
		}
		if (StringUtils.isBlank(mall)) {
			mall = StringUtils.substringBefore(main_title, "：");
		}

		// 内容
		Map<String, Object> featureMap = new HashMap<String, Object>();
		Elements contentElements = doc.select("div.deals-detail-left");
		StringBuilder sb = new StringBuilder();
		if (contentElements != null && contentElements.size() > 0) {
			int count = 1;
			for (Element e : contentElements) {
				String text = e.text();
				if (StringUtils.isNotBlank(text)) {
					text = StringUtils.substringAfter(text, "复制");
				}
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

	/**
	 * 获取结束时间 转换为正规date格式
	 * 
	 * @param doc
	 * 
	 * @param endTimeElements
	 * @return
	 */
	private static String getEndTime(Document doc, Elements endTimeElements) {
		String endTime = StringUtils.EMPTY;
		Date date = new Date();
		Calendar calendar = new GregorianCalendar();
		calendar.setTime(date);
		if (CollectionUtils.isNotEmpty(endTimeElements)) {
			endTime = endTimeElements.text();
			if (StringUtils.isNotBlank(endTime)) {
				endTime = StringUtils.substringAfter(endTime, "距结束");
				if (StringUtils.contains(endTime, "小时")) {
					endTime = StringUtils.substringBefore(endTime, "小时");
					calendar.add(Calendar.HOUR, Integer.parseInt(endTime));
				}
				if (StringUtils.contains(endTime, "天")) {
					endTime = StringUtils.substringBefore(endTime, "天");
					calendar.add(Calendar.DATE, Integer.parseInt(endTime));
				}

			} else {
				endTimeElements = doc.select("div.time.deals-end-time span.endtime");
				if (CollectionUtils.isNotEmpty(endTimeElements)) {
					String attr = endTimeElements.attr("data-value");
					int max_length = 13;
					if (attr.length() < 13) {
						int length = max_length - attr.length();
						for (int i = 0; i < length; i++) {
							attr = attr + "0";
						}
					}
					Date date2 = new Date(Long.parseLong(attr));
					DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					endTime = format.format(date2);
				}
				return endTime;
			}
		}
		Date time = calendar.getTime();
		DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String discovery_time = format.format(time);
		return discovery_time;
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
