package com.haitao55.spider.crawler.core.callable.custom.askderm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.crawler.core.callable.SelectUrls;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.core.model.UrlType;
import com.haitao55.spider.crawler.exception.CrawlerException.CrawlerExceptionCode;
import com.haitao55.spider.crawler.exception.ParseException;
import com.haitao55.spider.crawler.utils.Constants;
import com.haitao55.spider.crawler.utils.JsoupUtils;

/**
 * 
* @ClassName: AskdermSelectAllPages
* @Description: askderm所有分类页面爬取
* @author songsong.xu
* @date 2017年6月6日
*
 */
public class AskdermSelectAllPages extends SelectUrls{
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private int grade;
	
	@Override
	public void invoke(Context context) throws Exception {
	    String content = super.getInputString(context);
        //String content = Crawler.create().method("get").timeOut(60000).url(context.getUrl().getValue()).proxy(true).proxyAddress("159.203.15.211").proxyPort(3128).retry(3).resultAsString();
        Document d = JsoupUtils.parse(content);
        //div.pagination > span.page > a
        Elements es = d.select("div.pagination > span.page > a");
        List<String> newUrlValues = new ArrayList<String>();
        if(es != null && es.size() > 0 ){
            List<Integer> pageNumList = new ArrayList<>();
            es.forEach( ele -> {
                pageNumList.add(Integer.valueOf(StringUtils.trim(ele.text())));
            });
            int totalPage = Collections.max(pageNumList);
            for(int i= 1; i <= totalPage; i++){
                StringBuilder sb = new StringBuilder();
                sb.append("https://askderm.com/collections/all?page=")
                  .append(i);
                newUrlValues.add(sb.toString());
            }
            logger.info("fetch {} categories url from askderm.com's init url",newUrlValues.size());
            
        } else {
            logger.error("Error while fetching categories url https://www.askderm.com");
            throw new ParseException(CrawlerExceptionCode.PARSE_ERROR,"askderm.com itemUrl:"+context.getUrl().toString()+" categories element size is 0");
        }
        Set<Url> value = this.buildNewUrls(newUrlValues, context, UrlType.LINK.getValue(),grade);
        context.getUrl().getNewUrls().addAll(value);
	}
	public int getGrade() {
		return grade;
	}
	public void setGrade(int grade) {
		this.grade = grade;
	}

	public static void main(String[] args) throws Exception {
	    String u = "https://askderm.com/collections/all";
		AskdermSelectAllPages ask = new AskdermSelectAllPages();
		Context context = new Context();
		context.setUrl(new Url(u));
		context.setCurrentUrl(u);
		ask.invoke(context);
	}

}
