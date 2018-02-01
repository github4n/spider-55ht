package com.haitao55.spider.crawler.core.callable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.crawler.core.callable.base.AbstractSelectUrls;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.core.model.UrlType;
import com.haitao55.spider.crawler.utils.Constants;
import com.haitao55.spider.crawler.utils.HttpUtils;
import com.haitao55.spider.crawler.utils.JsoupUtils;

/**
 * 
 * @ClassName: SelectAllPages
 * @Description: 从初始页面上获取列表页面作为种子，并深入迭代所有的列表页面
 * @author songsong.xu
 * @date 2016年9月21日 下午5:11:14
 *
 */
public class SelectAllPages extends AbstractSelectUrls {
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private static final String ATTR_HREF = "href";
	private static final String DEFAULT_NEW_URLS_TYPE = UrlType.LINK.getValue();
	private static final int TIMEOUT = 30 * 1000;
	private static final int RETRY_TIMES = 3;
	
	// css选择器
	private String css;
	// 使用css选择器进行选择后,取元素的什么属性值；默认是href
	private String attr = ATTR_HREF;
	// 迭代出来的newurls的类型,链接类型还是商品类型,默认是链接类型
	private String type = DEFAULT_NEW_URLS_TYPE;

	private static final String DEFAULT_ATTR_ABS_HREF = "abs:href";
	private static final String DEFAULT_ATTR_TEXT = "text";
	private static final String DEFAULT_START_PAGE_INDEX = "1";
	private static final String PLACEHOLDER = "\\{\\}";
	private static final String ITEM_REG = "^\\d+$";

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
	private String attrItemsPerPage;
	private int step = 1;

	private int grade;

	@Override
	public void invoke(Context context) throws Exception {

		Document doc = super.getDocument(context);
		// body > div.header > div.header-nav.refresh-container > ul > li >
		// div.header-dropdown > div.refresh-container > div.header-sub-nav >
		// div.lists > div > ul > li > h5 > a
		Elements elements = doc.select(css);
		if (CollectionUtils.isNotEmpty(elements)) {
			List<String> firstUrlValues = JsoupUtils.attrs(elements, attr);

			// 只有在newUrlValues不为空且regex不为空时,才改装newUrlValues
			if (CollectionUtils.isNotEmpty(firstUrlValues) && StringUtils.isNotBlank(getRegex())) {
				firstUrlValues = this.reformStrings(firstUrlValues);
			}

			Set<Url> newUrls = this.buildNewUrls(firstUrlValues, context, type, grade);

			context.getUrl().getNewUrls().addAll(newUrls);

			for (Url url : newUrls) {
				try{
					url.setTask(context.getUrl().getTask());
					String content = HttpUtils.get(url, TIMEOUT, RETRY_TIMES);
					Document d = getDocument(url.getValue(), content);

					int totalPagesInt = 1;
					if (StringUtils.isNotBlank(this.cssTotalPages)) {
						String totalPages = this.selectTotalPages(url, d);
						totalPages = this.handleTotalPages(totalPages);
						if (StringUtils.isNotBlank(totalPages)) {
							totalPagesInt = Integer.parseInt(totalPages);
						}
					}
					if (1 == totalPagesInt) {
						if (StringUtils.isNotBlank(this.cssTotalItems) && StringUtils.isNotBlank(this.itemsPerPage)) {
							String totalItems = this.selectTotalItems(url, d);
							if(!this.itemsPerPage.matches(ITEM_REG))
								this.itemsPerPage = this.selectPageSize(url, d);
							if (StringUtils.isNotBlank(totalItems)) {
								totalPagesInt = (Integer.parseInt(totalItems) / Integer.parseInt(this.itemsPerPage)) + 1;
							}
						}
					}
					// 如果只有一页的情况
					if (totalPagesInt == 1) {
						continue;
					}

					String templateUrl = this.selectTemplateUrl(url, d);
					if (StringUtils.isNotBlank(templateUrl)) {
						templateUrl = templateUrl.replaceAll(replaceRegex, replaceFormat);
					}

					List<String> newUrlValues = new ArrayList<String>();
					for (int i = Integer.parseInt(startIndex); i <= totalPagesInt; i++) {
						String pageUrl = templateUrl.replaceAll(PLACEHOLDER, String.valueOf(i * step));
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

					Thread.sleep(500);
				}catch(Throwable e){
					e.printStackTrace();
				}
			}
		} else

		{
			logger.warn("css got no elements,url:{} ,css:{}", context.getUrl().getValue(), css);
		}

	}

	private Document getDocument(String url, String content) {
		return Jsoup.parse(content, url);
	}

	private String selectTotalPages(Url url, Document doc) {
		String totalPages = "";
		Elements elementsTotalPages = doc.select(this.cssTotalPages);
		if (CollectionUtils.isNotEmpty(elementsTotalPages)) {
			if (DEFAULT_ATTR_TEXT.equals(attrTotalPages)) {
				totalPages = elementsTotalPages.get(0).text();
			} else {
				totalPages = elementsTotalPages.get(0).attr(attrTotalPages);
			}
		} else {
			logger.warn("css got no elements,url:{} ,css:{}", url.getValue(), this.cssTotalPages);
		}

		return totalPages;
	}

	private String selectTotalItems(Url url, Document doc) {
		String totalItems = "";

		Elements elementsTotalItems = doc.select(this.cssTotalItems);
		if (CollectionUtils.isNotEmpty(elementsTotalItems)) {
			if (DEFAULT_ATTR_TEXT.equals(attrTotalItems)) {
				totalItems = elementsTotalItems.get(0).text();
			} else {
				totalItems = elementsTotalItems.get(0).attr(attrTotalItems);
			}
		} else {
			logger.warn("css got no elements,url:{} ,css:{}", url.getValue(), this.cssTotalItems);
		}

		return totalItems;
	}
	
	private String selectPageSize(Url url, Document doc) {
		String pageSize = "";

		Elements pageSizeElements = doc.select(this.itemsPerPage);
		if (CollectionUtils.isNotEmpty(pageSizeElements)) {
			if (DEFAULT_ATTR_TEXT.equals(attrItemsPerPage)) {
				pageSize = pageSizeElements.get(0).text();
			} else {
				pageSize = pageSizeElements.get(0).attr(attrItemsPerPage);
			}
		} else {
			logger.warn("css got no elements,url:{} ,css:{}", url.getValue(), this.itemsPerPage);
		}

		return pageSize;
	}

	private String selectTemplateUrl(Url url, Document doc) {
		String templateUrl = "";
		Elements elementsTemplateUrl = doc.select(this.cssTemplateUrl);
		if (CollectionUtils.isNotEmpty(elementsTemplateUrl)) {
			templateUrl = elementsTemplateUrl.get(0).attr(attrTemplateUrl);
		} else {
			logger.warn("css got no elements,url:{} ,css:{}", url.getValue(), this.cssTemplateUrl);
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

	public String getCss() {
		return css;
	}

	public void setCss(String css) {
		this.css = css;
	}

	public String getAttr() {
		return attr;
	}

	public void setAttr(String attr) {
		this.attr = attr;
	}

	public int getStep() {
		return step;
	}

	public void setStep(int step) {
		this.step = step;
	}

	public String getAttrItemsPerPage() {
		return attrItemsPerPage;
	}

	public void setAttrItemsPerPage(String attrItemsPerPage) {
		this.attrItemsPerPage = attrItemsPerPage;
	}

}