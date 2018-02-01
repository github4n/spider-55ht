package com.haitao55.spider.crawler.core.callable.custom.nautica;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.crawler.core.callable.base.AbstractSelectUrls;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.core.model.UrlType;
import com.haitao55.spider.crawler.utils.Constants;

/**
 * feelunique pages Title: Description: Company: 55海淘
 * 
 * @author zhaoxl
 * @date 2016年11月28日 下午5:29:07
 * @version 1.0
 */
public class SelectNauticaPages extends AbstractSelectUrls {

	private static final String URL_SUFFIX = "trigger=loadmore&format=ajax&start={}&uuid=&display=()&sz=[]";

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
		String url = context.getCurrentUrl().toString();
		String content = this.getInputString(context);
		int totalPagesInt = 1;
		String totalItems = StringUtils.EMPTY;
		if (1 == totalPagesInt) {
			totalItems = StringUtils.substringBetween(content, "itemTotal", ",");
			if (StringUtils.isNotBlank(totalItems)) {
				totalItems = StringUtils.replacePattern(totalItems, "[ =	]", "");
			}
			if (StringUtils.isNotBlank(totalItems)) {
				totalPagesInt = (Integer.parseInt(totalItems) / Integer.parseInt(this.itemsPerPage)) + 1;
			}
		}
		// 如果只有一页的情况
		if (totalPagesInt == 1) {
			return;
		}

		String templateUrl = StringUtils.EMPTY;

		String urlSuffix = StringUtils.replacePattern(URL_SUFFIX, "\\(\\)", totalItems);
		urlSuffix = StringUtils.replacePattern(urlSuffix, "\\[\\]", itemsPerPage);

		if (StringUtils.contains(url, "?")) {
			templateUrl = url + "&" + urlSuffix;
		} else {
			templateUrl = url + "?" + urlSuffix;
		}

		List<String> newUrlValues = new ArrayList<String>();
		for (int i = Integer.parseInt(startIndex); i <= totalPagesInt; i++) {
			String pageUrl = templateUrl.replaceAll(PLACEHOLDER,
					String.valueOf((i - 1) * Integer.parseInt(itemsPerPage)));
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
