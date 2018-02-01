package com.haitao55.spider.crawler.core.callable.custom.urbanoutfitters;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.common.http.HttpException;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelectUrls;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.core.model.UrlType;
import com.haitao55.spider.crawler.utils.Constants;
import com.haitao55.spider.crawler.utils.HttpUtils;
import com.haitao55.spider.crawler.utils.JsoupUtils;

/**
 * Urbanoutfitters pages 列表封装 Title: Description: Company: 55海淘
 * 
 * @author zhaoxl
 * @date 2017年4月7日 上午11:47:37
 * @version 1.0
 */
public class SelectUrbanoutfittersPages extends AbstractSelectUrls {
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);

	private static final String DEFAULT_ATTR_ABS_HREF = "abs:href";
	private static final String DEFAULT_ATTR_TEXT = "text";
	private static final String DEFAULT_START_PAGE_INDEX = "1";
	private static final String PLACEHOLDER = "\\{\\}";

	private String cssTemplateUrl;
	private String attrTemplateUrl = DEFAULT_ATTR_ABS_HREF;
	private String cssTotalPages;
	private String attrTotalPages = DEFAULT_ATTR_TEXT;
	private String startIndex = DEFAULT_START_PAGE_INDEX;

	private String cssTotalItems;
	private String attrTotalItems;
	private String itemsPerPage;

	private int grade;

	@Override
	public void invoke(Context context) throws Exception {
		int totalPagesInt = 1;

		if (StringUtils.isNotBlank(this.cssTotalPages)) {
			String totalPages = this.selectTotalPages(context);
			totalPages = this.handleTotalPages(totalPages);
			if (StringUtils.isNotBlank(totalPages)) {
				totalPagesInt = Integer.parseInt(totalPages);
			}
		}
		if (1 == totalPagesInt) {
			if (StringUtils.isNotBlank(this.cssTotalItems) && StringUtils.isNotBlank(this.itemsPerPage)) {
				String totalItems = this.selectTotalItems(context);
				if (StringUtils.isNotBlank(totalItems)) {
					totalPagesInt = (Integer.parseInt(totalItems) / Integer.parseInt(this.itemsPerPage)) + 1;
				}
			}
		}
		// 如果只有一页的情况
		if (totalPagesInt == 1) {
			return;
		}

		String url = context.getCurrentUrl().toString();
		
		String templateUrl = StringUtils.EMPTY;
		if(StringUtils.contains(url, "?")){
			templateUrl = url+"&page={}";
		}else{
			templateUrl = url+"?page={}";
		}

		List<String> newUrlValues = new ArrayList<String>();
		for (int i = Integer.parseInt(startIndex); i <= totalPagesInt; i++) {
			String pageUrl = templateUrl.replaceAll(PLACEHOLDER, String.valueOf((i)));
			newUrlValues.add(pageUrl);
		}

		// 只有当newUrlValues和regex都不为空时,才改装newUrlValues
		if (CollectionUtils.isNotEmpty(newUrlValues) && StringUtils.isNotBlank(getRegex())) {
			newUrlValues = this.reformStrings(newUrlValues);
		}

		// 翻页操作,最后取得的urls,肯定是link类型的url
		Set<Url> value = this.buildNewUrls(newUrlValues, context, UrlType.LINK.getValue(), grade);

		// 新迭代出来的urls,放置在Url对象的newUrls中；而不是放置在context中
		context.getUrl().getNewUrls().addAll(value);
	}

	private String selectTotalPages(Context context) throws ClientProtocolException, HttpException, IOException {
		String totalPages = "";
		String content = HttpUtils.crawler_package(context, getHeaders());
		Document doc = JsoupUtils.parse(content);
		Elements elementsTotalPages = doc.select(this.cssTotalPages);
		if (CollectionUtils.isNotEmpty(elementsTotalPages)) {
			if (DEFAULT_ATTR_TEXT.equals(attrTotalPages)) {
				totalPages = elementsTotalPages.get(0).text();
			} else {
				totalPages = elementsTotalPages.get(0).attr(attrTotalPages);
			}
		} else {
			logger.warn("css got no elements,url:{} ,css:{}", context.getUrl().getValue(), this.cssTotalPages);
		}

		return totalPages;
	}

	private String selectTotalItems(Context context) {
		String totalItems = "";

		Document doc = super.getDocument(context);
		Elements elementsTotalItems = doc.select(this.cssTotalItems);
		if (CollectionUtils.isNotEmpty(elementsTotalItems)) {
			if (DEFAULT_ATTR_TEXT.equals(attrTotalItems)) {
				totalItems = elementsTotalItems.get(0).text();
			} else {
				totalItems = elementsTotalItems.get(0).attr(attrTotalItems);
			}
		} else {
			logger.warn("css got no elements,url:{} ,css:{}", context.getUrl().getValue(), this.cssTotalItems);
		}

		return totalItems;
	}

	/**
	 * 处理totalCount数值，比如“2/11”分隔符形式
	 * 
	 * @param count
	 * @return
	 */
	private String handleTotalPages(String totalPages) {
		if (totalPages.indexOf("/") != -1) {
			String[] c_pages = totalPages.trim().split("/");
			if (c_pages.length == 2) {
				totalPages = c_pages[1].trim();
			}
		}
		totalPages = totalPages.replaceAll("[^\\d]", "");

		return totalPages;
	}

	public String getCssTemplateUrl() {
		return cssTemplateUrl;
	}

	public void setCssTemplateUrl(String cssTemplateUrl) {
		this.cssTemplateUrl = cssTemplateUrl;
	}

	public String getCssTotalPages() {
		return cssTotalPages;
	}

	public void setCssTotalPages(String cssTotalPages) {
		this.cssTotalPages = cssTotalPages;
	}

	public String getAttrTemplateUrl() {
		return attrTemplateUrl;
	}

	public void setAttrTemplateUrl(String attrTemplateUrl) {
		this.attrTemplateUrl = attrTemplateUrl;
	}

	public String getAttrTotalPages() {
		return attrTotalPages;
	}

	public void setAttrTotalPages(String attrTotalPages) {
		this.attrTotalPages = attrTotalPages;
	}

	public String getStartIndex() {
		return startIndex;
	}

	public void setStartIndex(String startIndex) {
		this.startIndex = startIndex;
	}

	public String getCssTotalItems() {
		return cssTotalItems;
	}

	public void setCssTotalItems(String cssTotalItems) {
		this.cssTotalItems = cssTotalItems;
	}

	public String getItemsPerPage() {
		return itemsPerPage;
	}

	public void setItemsPerPage(String itemsPerPage) {
		this.itemsPerPage = itemsPerPage;
	}

	public String getAttrTotalItems() {
		return attrTotalItems;
	}

	public void setAttrTotalItems(String attrTotalItems) {
		this.attrTotalItems = attrTotalItems;
	}

	public int getGrade() {
		return grade;
	}

	public void setGrade(int grade) {
		this.grade = grade;
	}
	private static Map<String, Object> getHeaders() {
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
}
