package com.haitao55.spider.crawler.core.callable.custom.spacenk;

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
 * spacenk page封装 Title: Description: Company: 55海淘
 * 
 * @author zhaoxl
 * @date 2017年3月31日 下午6:04:56
 * @version 1.0
 */
public class SelectSpacenkPages extends AbstractSelectUrls {
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);

	private static final String DEFAULT_START_PAGE_INDEX = "1";
	private static final String PLACEHOLDER = "\\{\\}";

	private String startIndex = DEFAULT_START_PAGE_INDEX;
	private String itemsPerPage;
	private int grade;

	@Override
	public void invoke(Context context) throws Exception {
		int totalPagesInt = 1;
		String content = this.getInputString(context);
		String totalItems = StringUtils.substringBetween(content, "items_count\":", ",");
		if (StringUtils.isNotBlank(totalItems)) {
			totalPagesInt = (Integer.parseInt(totalItems) / Integer.parseInt(this.itemsPerPage)) + 1;
		}
		// 如果只有一页的情况
		if (totalPagesInt == 1) {
			return;
		}
		String url = context.getCurrentUrl().toString();
		String templateUrl = StringUtils.EMPTY;
		if(StringUtils.contains(url, "?")){
			templateUrl = url + "&page={}";
		}else{
			templateUrl = url + "?page={}";
		}
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
		Set<Url> value = this.buildNewUrls(newUrlValues, context, UrlType.LINK.getValue(), grade);

		// 新迭代出来的urls,放置在Url对象的newUrls中；而不是放置在context中
		context.getUrl().getNewUrls().addAll(value);
	}

	public String getStartIndex() {
		return startIndex;
	}

	public void setStartIndex(String startIndex) {
		this.startIndex = startIndex;
	}

	public String getItemsPerPage() {
		return itemsPerPage;
	}

	public void setItemsPerPage(String itemsPerPage) {
		this.itemsPerPage = itemsPerPage;
	}

	public int getGrade() {
		return grade;
	}

	public void setGrade(int grade) {
		this.grade = grade;
	}

}