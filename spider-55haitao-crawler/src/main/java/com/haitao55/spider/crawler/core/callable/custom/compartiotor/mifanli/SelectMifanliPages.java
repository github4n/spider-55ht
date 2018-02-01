package com.haitao55.spider.crawler.core.callable.custom.compartiotor.mifanli;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.crawler.core.callable.SelectUrls;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelectUrls;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.core.model.UrlType;
import com.haitao55.spider.crawler.utils.Constants;

/**
 * 
 * 功能：通过css选择器,提取和计算页面上的翻页urls超级链接
 * 
 * @author Arthur.Liu
 * @time 2016年8月22日 下午3:02:22
 * @version 1.0
 * @see Select
 * @see SelectUrls
 */
public class SelectMifanliPages extends AbstractSelectUrls {
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);

	private static final String DEFAULT_ATTR_ABS_HREF = "abs:href";
	private static final String DEFAULT_ATTR_TEXT = "text";
	private static final String DEFAULT_START_PAGE_INDEX = "1";
	private static final String PLACEHOLDER = "\\{\\}";

	private String cssTemplateUrl;
	private String attrTemplateUrl = DEFAULT_ATTR_ABS_HREF;
	private String cssTotalPages;
	private String attrTotalPages = DEFAULT_ATTR_TEXT;
	private String replaceRegex;
	private String replaceFormat;
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

		String templateUrl = context.getCurrentUrl().toString()+"{}";

		List<String> newUrlValues = new ArrayList<String>();
		for (int i = Integer.parseInt(startIndex); i <= totalPagesInt; i++) {
			String pageUrl = templateUrl.replaceAll(PLACEHOLDER, String.valueOf(i));
			newUrlValues.add(pageUrl);
		}

		// 只有当newUrlValues和regex都不为空时,才改装newUrlValues
		if (CollectionUtils.isNotEmpty(newUrlValues) && StringUtils.isNotBlank(getRegex())) {
			newUrlValues = this.reformStrings(newUrlValues);
		}

		// 翻页操作,最后取得的urls,肯定是link类型的url
		Set<Url> value = this.buildNewUrls(newUrlValues, context, UrlType.LINK.getValue(),grade);

		// 新迭代出来的urls,放置在Url对象的newUrls中；而不是放置在context中
		context.getUrl().getNewUrls().addAll(value);
	}

	private String selectTotalPages(Context context) {
		String totalPages = "";

		Document doc = super.getDocument(context);
		Elements elementsTotalPages = doc.select(this.cssTotalPages);
		if (CollectionUtils.isNotEmpty(elementsTotalPages)) {
			if (DEFAULT_ATTR_TEXT.equals(attrTotalPages)) {
				totalPages = elementsTotalPages.get(0).text();
			} else {
				totalPages = elementsTotalPages.get(0).attr(attrTotalPages);
			}
			if(StringUtils.isNotBlank(totalPages)){
				totalPages = StringUtils.substringBetween(totalPages, "总", "页");
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

	private String selectTemplateUrl(Context context) {
		String templateUrl = "";

		Document doc = super.getDocument(context);
		Elements elementsTemplateUrl = doc.select(this.cssTemplateUrl);
		if (CollectionUtils.isNotEmpty(elementsTemplateUrl)) {
			templateUrl = elementsTemplateUrl.get(0).attr(attrTemplateUrl);
		} else {
			logger.warn("css got no elements,url:{} ,css:{}", context.getUrl().getValue(), this.cssTemplateUrl);
		}

		return templateUrl;
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

	public String getReplaceRegex() {
		return replaceRegex;
	}

	public void setReplaceRegex(String replaceRegex) {
		this.replaceRegex = replaceRegex;
	}

	public String getReplaceFormat() {
		return replaceFormat;
	}

	public void setReplaceFormat(String replaceFormat) {
		this.replaceFormat = replaceFormat;
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
	
}