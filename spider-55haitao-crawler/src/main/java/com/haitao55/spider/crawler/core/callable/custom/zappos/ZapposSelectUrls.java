package com.haitao55.spider.crawler.core.callable.custom.zappos;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.haitao55.spider.crawler.core.callable.SelectUrls;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.utils.Constants;

/**
 * 
  * @ClassName: ZapposSelectUrls
  * @Description: Zappos的列表页面处理器
  * @author denghuan
  * @date 2016年10月21日 下午6:29:50
  *
 */
public class ZapposSelectUrls extends SelectUrls {
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private static final String BASEURL = "http://www.zappos.com";

	@Override
	public void invoke(Context context) throws Exception {
		String content = super.getInputString(context);
		Document docment = Jsoup.parse(content);
		List<String> newUrlValues = new ArrayList<String>();
		Elements es = docment.select("#resultWrap div#searchResults a");
		if(es != null && es.size() > 0){
			for(Element e : es){
				String url = e.attr("href");
				newUrlValues.add(BASEURL+url);
			}
		}
		Set<Url> newUrls = this.buildNewUrls(newUrlValues, context, type, grade);
		context.getUrl().getNewUrls().addAll(newUrls);
		logger.info("zappos list url:{} ,get items :{}", context.getUrl().getValue(), newUrlValues.size());
		
	}

}