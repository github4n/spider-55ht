package com.haitao55.spider.crawler.core.callable.custom.barneys;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.common.http.Crawler;
import com.haitao55.spider.common.http.HttpException;
import com.haitao55.spider.crawler.core.callable.SelectUrls;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.core.model.UrlType;
import com.haitao55.spider.crawler.exception.CrawlerException.CrawlerExceptionCode;
import com.haitao55.spider.crawler.exception.ParseException;
import com.haitao55.spider.crawler.utils.Constants;
import com.haitao55.spider.crawler.utils.JsoupUtils;

/**
  * @ClassName: BarneysSelectAllPages
  * @Description: category page parser
  * @author songsong.xu
  * @date 2016年12月1日 下午3:06:03
  *
 */
public class BarneysSelectAllPages extends SelectUrls{
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private int grade;
	
	@Override
	public void invoke(Context context) throws Exception {
		String content = super.getInputString(context);
		Document d = JsoupUtils.parse(content);
		//#topnav-level-1 > li > div.flyout > div.flyout-inner > div.flyout-inner-left.featured-margin-right > ul > li > a
		Elements es = d.select("#topnav-level-1 > li > div.flyout > div.flyout-inner > div.flyout-inner-left.featured-margin-right > ul > li > a");
		List<String> newUrlValues = new ArrayList<String>();
		if(es != null && es.size() > 0 ){
			for(Element ele : es){
				String link = JsoupUtils.attr(ele, "href");
				if(StringUtils.isBlank(link) || !StringUtils.startsWith(link, "http://")){
					continue;
				}
				if(StringUtils.contains(link, "?")){
					link = StringUtils.substringBefore(link, "?");
				}
				newUrlValues.add(link+"?sz=999&viewall=1&format=ajax");
			}
			logger.info("fetch {} categories url from yslbeautyus.com's init url",newUrlValues.size());
			
		} else {
			logger.error("Error while fetching categories url https://www.yslbeautyus.com");
			throw new ParseException(CrawlerExceptionCode.PARSE_ERROR,"yslbeautyus.com itemUrl:"+context.getUrl().toString()+" categories element size is 0");
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
		String u = "https://www.barneys.com";
		Map<String,Object> headers = new HashMap<String,Object>();
		headers.put("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/54.0.2840.59 Safari/537.36");
		headers.put("Upgrade-Insecure-Requests", "1");
		headers.put("Retry-After", 120);
		headers.put("Cookie", "__psrw=cd9c8e53-14fb-4509-b0d1-49b0b7a6f030; AMCVS_94AA483F53B6BF2B0A490D44%40AdobeOrg=1; AMCV_94AA483F53B6BF2B0A490D44%40AdobeOrg=-1758798782%7CMCIDTS%7C17137%7CMCMID%7C50007579796145131349211327837566128193%7CMCAID%7CNONE%7CMCOPTOUT-1480567188s%7CNONE%7CMCAAMLH-1480669297%7C9%7CMCAAMB-1481164788%7Chmk_Lq6TPIBMW925SPhw3Q; fsr.r=%7B%22d%22%3A360%2C%22i%22%3A%22d1159f3-80019447-dd96-d586-d3666%22%2C%22e%22%3A1481165207762%7D; _sp_id.9d59=50254da3-68a9-401d-ac6a-7844226300c9.1480064666.3.1480573704.1480560390.bc8b8646-6372-47ef-8528-53ba83635578; productFindingMethod=Browse; JSESSIONID=4RG5Ombv7Eyjmdoo1zY6UTlxHY-mr-tsqiy1J4S5kMNuNpOizthc!248012704!813362-prodapp3!20580!-1; mmcore.tst=0.514; _gat_fe821b5d299b68da6915087bb7cc0516=1; _ga=GA1.2.1814680223.1480064478; _gat_BNY=1; stc112112=env:1480576363%7C20170101071243%7C20161201075251%7C4%7C1020409:20171201072251|uid:1480064478198.1014142975.0414495.112112.1195637724:20171201072251|srchist:1020408%3A1%3A20161226090118%7C1020410%3A1480064817%3A20161226090657%7C1020408%3A1480559988%3A20170101023948%7C1020410%3A1480560387%3A20170101024627%7C1020409%3A1480572314%3A20170101060514%7C1020410%3A1480572893%3A20170101061453%7C1020409%3A1480576363%3A20170101071243:20171201072251|tsa:1480576363394.2029233747.3248067.3048454481979159.:20161201075251; dw=1; dslv=1480576972043; dslv_s=Less%20than%201%20day; s_cc=true; numberOfItems=; mmapi.store.p.0=%7B%22mmparams.d%22%3A%7B%7D%2C%22mmparams.p%22%3A%7B%22mmid%22%3A%221512112971621%7C%5C%222037552153%7CLQAAAAqj8TZEIA4AAA%3D%3D%5C%22%22%2C%22pd%22%3A%221512112971623%7C%5C%22-1340837606%7CLQAAAAoBQqPxNkQgDhm%2FHHEEAOJ3Pdy6GdRIDwAAAHrxG5wRFdRIAAAAAP%2F%2F%2F%2F%2F%2F%2F%2F%2F%2FAAZEaXJlY3QBIA4EAAAAAAAAAAAAAP%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2F%2FwIAP1IAAAB%2FraVSIA4A%2F%2F%2F%2F%2FwEgDiYO%2F%2F8tAAABAAAAAAFxyAAA3z0BAAC0dgAAAEDp5RogDgD%2F%2F%2F%2F%2FASAOJg7%2F%2Fy0AAAEAAAAAAa4aAQDetAEAAAAAAAFF%5C%22%22%2C%22srv%22%3A%221512112971625%7C%5C%22lvsvwcgus01%5C%22%22%7D%2C%22mmengine%22%3A%7B%7D%7D; mmapi.store.s.0=%7B%22mmparams.d%22%3A%7B%7D%2C%22mmparams.p%22%3A%7B%7D%2C%22mmengine%22%3A%7B%7D%7D; rr_rcs=eF4FwTsSgCAMBcCGyrs8J5AP4QZeg4DMWNip53c3bff3XHMXIWRx0mqtFiJDIyCndxzBrY9hBR5qkNMDsozRWXWpTp8SP2svEcI; s_pvpg=Homepage%3A%20Barneys%20New%20York; userPrefLanguage=en_US; fsr.s=%7B%22v2%22%3A-2%2C%22v1%22%3A1%2C%22rid%22%3A%22d1159f3-80019447-dd96-d586-d3666%22%2C%22ru%22%3A%22https%3A%2F%2Fwww.google.com.hk%2F%22%2C%22r%22%3A%22www.google.com.hk%22%2C%22st%22%3A%22%22%2C%22cp%22%3A%7B%22SignedIn%22%3A%22NO%22%2C%22numberOfItems%22%3A%220%22%7D%2C%22to%22%3A4%2C%22c%22%3A%22http%3A%2F%2Fwww.barneys.com%2F%22%2C%22pv%22%3A37%2C%22lc%22%3A%7B%22d1%22%3A%7B%22v%22%3A37%2C%22s%22%3Atrue%7D%7D%2C%22cd%22%3A1%2C%22sd%22%3A1%2C%22f%22%3A1480576971151%2C%22l%22%3A%22en%22%2C%22i%22%3A-1%7D; s_ppvl=Product%253A%2520Dries%2520Van%2520Noten%253A%2520Dorindo%2520Sequined%2520Shift%2520Dress%253A%2520504600924%2C12%2C12%2C263%2C1301%2C263%2C1366%2C768%2C1%2CP; RT=\"sl=3&ss=1480576373152&tt=38294&obo=0&sh=1480577002088%3D3%3A0%3A38294%2C1480576630574%3D2%3A0%3A6783%2C1480576376766%3D1%3A0%3A3610&dm=barneys.com&si=0f482e11-b93c-496f-a733-03ddda4d68be&bcn=%2F%2F36fb6d09.mpstat.us%2F&ld=1480577002088&r=http%3A%2F%2Fwww.barneys.com%2F&ul=1480577019229\"; s_ppv=Homepage%253A%2520Barneys%2520New%2520York%2C43%2C43%2C1108%2C1301%2C154%2C1366%2C768%2C1%2CP");
		String content = Crawler.create().method("get").timeOut(60000).url(u)/*.proxy(true).proxyAddress("104.196.182.96").proxyPort(3128)*/.retry(3).header(headers).resultAsString();
		System.out.println(content);
		Document d = JsoupUtils.parse(content);
		//#topnav-level-1 > li > div.flyout > div.flyout-inner > div.flyout-inner-left.featured-margin-right > ul > li > a
		Elements es = d.select("#topnav-level-1 > li");
		//> div.flyout > div.flyout-inner > div.flyout-inner-left.featured-margin-right > ul > li > a
		System.out.println(es.html());
		for(Element e : es){
			System.out.println(JsoupUtils.attr(e, "href"));
		}
	}

}
