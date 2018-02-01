package com.haitao55.spider.crawler.core.callable.custom.victoriassecret;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.common.http.HttpException;
import com.haitao55.spider.crawler.core.callable.SelectUrls;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.core.model.UrlType;
import com.haitao55.spider.crawler.utils.Constants;
import com.haitao55.spider.crawler.utils.HttpUtils;
import com.haitao55.spider.crawler.utils.JsoupUtils;

/**
 * 
  * @ClassName: VictoriasSecretSelectAllPages
  * @Description: 维多利亚的秘密 获取所有分类url
  * @author songsong.xu
  * @date 2016年11月7日 下午5:27:07
  *
 */
public class VictoriasSecretSelectAllPages extends SelectUrls{
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private final int pageSize = 180;
	private final String HEAD = "https://www.victoriassecret.com";
	private final String SUFFIX = "/more?increment="+pageSize+"&sortby=REC&location=";
	private int grade;
	
	@Override
	public void invoke(Context context) throws Exception {
		String content = super.getInputString(context);
		Document d = JsoupUtils.parse(content);
		Elements es = d.select("section.menu.secondary.closed> div > div.columns.wide > div > ul > li > span > a");
		List<String> newUrlValues = new ArrayList<String>();
		for(Element ele : es){
			String link = JsoupUtils.attr(ele, "href");
			if(StringUtils.isBlank(link)){
				continue;
			}
			try{
				Url currentUrl = new Url(HEAD+link);
				currentUrl.setTask(context.getUrl().getTask());
				String res = HttpUtils.get(currentUrl, HttpUtils.DEFAULT_TIMEOUT, HttpUtils.DEFAULT_RETRY_TIMES);
				Pattern p = Pattern.compile("<span[^>]*\\\"item-count\\\"[^>]*>(\\d+)[^<]*</span>");
				Matcher m = p.matcher(res);
				int totalCount = 0 ;
				if(m.find()){
					String totalCountStr = m.group(1);
					if(StringUtils.isNotBlank(totalCountStr)){
						totalCount = Integer.valueOf(totalCountStr);
					}
				} 
				logger.info("victoriassecret.com url {} , itemcount {}",link,totalCount);
				int pageCount = (totalCount+pageSize-1)/pageSize;
				if(pageCount > 0){
					for(int page = 0 ; page < pageCount;page++){
						StringBuilder sb = new StringBuilder();
						sb.append(HEAD).append(link).append(SUFFIX).append(page*pageSize);
						newUrlValues.add(sb.toString());
					}
				} else {
					StringBuilder sb = new StringBuilder();
					sb.append(HEAD).append(link).append(SUFFIX).append(0);
					newUrlValues.add(sb.toString());
				}
				logger.info("fetch {} categories url from victoriassecret.com's init url",newUrlValues.size());
			}catch(Throwable e){
				e.printStackTrace();
			}
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

	public static void main(String[] args) throws ClientProtocolException, HttpException, IOException {
		//section.menu.secondary.closed> div > div.columns.wide > div > ul > li > span > a
		String u = "https://www.victoriassecret.com/";
		Map<String,Object> headers = new HashMap<String,Object>();
		headers.put("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.59 Safari/537.36");
		//String content = Crawler.create().method("get").timeOut(30000).url(u).proxy(true).proxyAddress("104.196.182.96").proxyPort(3128).retry(3).header(headers).resultAsString();
		Url url = new Url(u);
		String content = HttpUtils.get(url);
		System.out.println(content);
		/*Document d = JsoupUtils.parse(content);
		//#collection-set > hgroup > span
		Elements es = d.select("section.menu.secondary.closed> div > div.columns.wide > div > ul > li > span > a");
		for(Element e : es){
			System.out.println(e.attr("href"));
		}*/
		
		/*Pattern p = Pattern.compile("<span[^>]*\\\"item-count\\\"[^>]*>(\\d+)[^<]*</span>");
		Matcher m = p.matcher(content);
		if(m.find()){
			System.out.println(m.group(1));
		}*/
		//System.out.println(content);
		
		//System.out.println(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(1478263131315l)));
	}

}
