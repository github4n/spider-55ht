package com.haitao55.spider.crawler.core.callable.custom.colehaan;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.common.utils.DetailUrlCleaningTool;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelectUrls;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.callable.custom.mankind.CrawlerUtils;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.core.model.UrlType;
import com.haitao55.spider.crawler.utils.Constants;

/** 
 * @Description: 
 * @author: zhoushuo
 * @date: 2016年12月30日 下午2:14:56  
 */
public class ColeHaanSelectUrls  extends AbstractSelectUrls {
	
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	public int grade;
	public String type = UrlType.LINK.getValue();

	@Override
	public void invoke(Context context) throws Exception {
		String currentUrl = context.getCurrentUrl();
		currentUrl = DetailUrlCleaningTool.getInstance().cleanDetailUrl(currentUrl);
		String content = super.getInputString(context);
		String totalItemsStr = StringUtils.substringBetween(content, "var iScrollCount", ";");
		totalItemsStr = StringUtils.trim(CrawlerUtils.getNumberFromString(totalItemsStr));
		int totalItems = 0;
		if(StringUtils.isNotBlank(totalItemsStr)){
			try {
				totalItems = Integer.valueOf(totalItemsStr);
			} catch (NumberFormatException e) {
				logger.error("get totalItems error from url {}",currentUrl);
				return;
			}
		}
		if(totalItems <= 50) //商品个数小于等于50无需分页
			return;
		int totalPages = (totalItems-1)/50 +1;
		List<String> newUrlValues = new ArrayList<>();
		for(int i=1; i < totalPages; i++){
			String newUrl = currentUrl+"?sz=50&start="+50*i+"&format=page-element&hitcount="+50*i;
			newUrlValues.add(newUrl);
		}
		String lastUrl = currentUrl+"?sz=50&start="+(totalItems-1)+"&format=page-element&hitcount="+totalItems;
		newUrlValues.add(lastUrl);
		
		Set<Url> newUrls = this.buildNewUrls(newUrlValues, context, type, grade);

		// 新迭代出来的urls,放置在Url对象的newUrls中；而不是放置在context中
		context.getUrl().getNewUrls().addAll(newUrls);
	}
	
	public int getGrade() {
		return grade;
	}

	public String getType() {
		return type;
	}

	public void setGrade(int grade) {
		this.grade = grade;
	}

	public void setType(String type) {
		this.type = type;
	}

}
