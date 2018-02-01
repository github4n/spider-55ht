package com.haitao55.spider.crawler.core.callable.custom.weibo;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.HttpClients;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.common.gson.bean.weibo.WeiBoBody;
import com.haitao55.spider.common.http.Crawler;
import com.haitao55.spider.common.http.HttpException;
import com.haitao55.spider.common.http.HttpMethod;
import com.haitao55.spider.common.utils.CurlCrawlerUtil;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.utils.Constants;
import com.haitao55.spider.crawler.utils.Native2AsciiUtils;


/**
 * 
 * 新浪微博抓取
 * 
 */

public class WeiBo extends AbstractSelect{
	
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	
	private static String WEIBO_COMMENT_API = "http://s.weibo.com/ajax/comment/small?mid=";
	private static String WEIBO_DETAIL_API = "http://weibo.com/aj/user/newcard?id=";
	
	@Override
	public void invoke(Context context) throws Exception {
		String url = context.getCurrentUrl();
		List<String> weiBoList = new ArrayList<>();
		
		for (int i = 0; i < 3; i++) {
			try {
				List<Map<String, Object>> heanderList = getHeaders(url);

				Map<String, Object> headerMap = random(heanderList); // 随机取一个header

				String content = crawlerUrl(url, headerMap);

				if (StringUtils.isNotBlank(content)) {
					String weiboContent = StringUtils.substringBetween(content, "pl_weibo_direct\",\"js\":",
							"</script>");
					weiboContent = Native2AsciiUtils.ascii2Native(weiboContent);
					weiboContent = this.replaceAll(weiboContent);
					Document doc = Jsoup.parse(weiboContent);

					String notFound = doc.select(".noresult_tit").text();
					if (StringUtils.isNotBlank(notFound) && StringUtils.containsIgnoreCase(notFound, "抱歉")) {
						return;
					}

					Map<String, Boolean> blogNameStatusMap = new HashMap<>();

					Elements es = doc.select(".search_feed .WB_cardwrap");
					for (Element e : es) {

						String mid = StringUtils.substringBetween(e.toString(), "mid=\"", "\"");
						String ouid = StringUtils.substringBetween(e.toString(), "\"ouid=", "\"");
						String blogName = e.select(".feed_content a.W_fb").attr("nick-name");
						String blogUrl = e.select(".feed_content a").attr("href");
						if (StringUtils.isBlank(mid) || StringUtils.isBlank(blogName) || StringUtils.isBlank(ouid)
								|| StringUtils.isBlank(blogUrl)) {
							logger.error(
									"WeiBo crawler blogName || blogUrl || ouid || mid IsBlank :: url : {} , blogUrl : {}, "
											+ "blogName :{},mid :{} ,ouid :{}",
									url, blogUrl, blogName, mid, ouid);
							continue;
						}

						if (StringUtils.containsIgnoreCase(blogUrl, "?")) {
							blogUrl = StringUtils.substringBefore(blogUrl, "?");
						}

						String styleMeUrl = StringUtils.EMPTY;

						boolean isRstyleMeExist = false;
						Elements linkEs = e.select(".feed_content a.W_btn_c6");

						if (null == blogNameStatusMap.get(blogName)) {
							for (Element le : linkEs) {
								String shortLinkUrl = le.attr("href");
								if (StringUtils.isNotBlank(shortLinkUrl)) {
									String redirectUrl = getRedirectUrl(shortLinkUrl);
									if (StringUtils.containsIgnoreCase(redirectUrl, "rstyle.me")) {
										isRstyleMeExist = true;
										styleMeUrl = redirectUrl;
										break;
									}
								}
							}

							if (!isRstyleMeExist) {
								// 查看评论是否有rstyle.me网页链接
								if (StringUtils.isNotBlank(mid)) {
									String styleMeApi = WEIBO_COMMENT_API + mid;
									//String commentContent = crawlerUrl(styleMeApi, headerMap);
									String commentContent = CurlCrawlerUtil.get(styleMeUrl);
									if (StringUtils.isNotBlank(commentContent)) {
										commentContent = this.replaceAll(commentContent);
										Document commentDoc = Jsoup.parse(commentContent);
										Elements commentEs = commentDoc.select(".list_box .list_ul .list_li");
										for (Element ce : commentEs) {
											String shortLinkUrl = ce.select("a.feed_list_url").attr("href");
											if (StringUtils.isBlank(shortLinkUrl)) {
												shortLinkUrl = ce.select("a.W_btn_c6").attr("href");
											}

											if (StringUtils.isNotBlank(shortLinkUrl)) {
												logger.info(
														"Crawler WeiBo Comment Info ::: url : {} , blogName :{}, styleMeApi :{}, shortLinkUrl : {}",
														url, blogName, styleMeApi, shortLinkUrl);

												String redirectUrl = getRedirectUrl(shortLinkUrl);
												if (StringUtils.containsIgnoreCase(redirectUrl, "rstyle.me")) {
													isRstyleMeExist = true;
													styleMeUrl = redirectUrl;
													break;
												}
											}
										}
									}
								}
							}
						}

						if (null == blogNameStatusMap.get(blogName) || blogNameStatusMap.get(blogName) == false) {
							blogNameStatusMap.put(blogName, isRstyleMeExist);
						}

						WeiBoBody weiBoProperty = new WeiBoBody();
						String docId = SpiderStringUtil.md5Encode(blogUrl);
						weiBoProperty.setDOCID(docId);
						weiBoProperty.setUrl(blogUrl);
						weiBoProperty.setBlogName(blogName);
						if (StringUtils.isNotBlank(styleMeUrl)) {
							weiBoProperty.setContainStyleMeUrl(styleMeUrl);
						}
						//String detailContent = CurlCrawlerUtil.get(WEIBO_DETAIL_API + ouid);
						String detailContent = crawlerUrl(WEIBO_DETAIL_API + ouid, headerMap);
						String followCount = StringUtils.EMPTY;
						String fansCount = StringUtils.EMPTY;
						String wbCount = StringUtils.EMPTY;
						String description = StringUtils.EMPTY;
						if (StringUtils.isNotBlank(detailContent)) {
							detailContent = Native2AsciiUtils.ascii2Native(detailContent);
							detailContent = this.replaceAll(detailContent);
							followCount = StringUtils.substringBetween(detailContent, ">关注</a>", "</li>");
							fansCount = StringUtils.substringBetween(detailContent, "粉丝</a>", "</li>");
							wbCount = StringUtils.substringBetween(detailContent, "微博</a>", "</li>");
							description = StringUtils.substringBetween(detailContent, "dd title=\"", "</dd>");
						}

						if (StringUtils.isBlank(fansCount) && StringUtils.isBlank(wbCount)) {
							continue;
						}

						weiBoProperty.setFansCount(fansCount);

						if (StringUtils.isNotBlank(followCount)) {
							weiBoProperty.setFollowCount(followCount);
						}

						if (StringUtils.isNotBlank(wbCount)) {
							weiBoProperty.setWbCount(wbCount);
						}
						if (StringUtils.isNotBlank(description)) {
							weiBoProperty.setDescription(description);
						}

						weiBoList.add(weiBoProperty.parseTo());

						logger.info(
								"Crawler WeiBo Attr Info :::: docId :{},blogUrl :{}, blogName :{} ,isRstyleMeExist :{}, fansCount :{},followCount :{},wbCount : {}",
								docId, blogUrl, blogName, isRstyleMeExist, fansCount, followCount, wbCount);

					}
				}

				if (CollectionUtils.isNotEmpty(weiBoList)) {
					break;
				}
			} catch (Exception e1) {
				e1.printStackTrace();
				logger.error("Crawler WeiBo Execption :::" + e1.getMessage());
			}
		}
		setOutput(context, weiBoList);
	}
	
	private String crawlerUrl(String url,Map<String,Object> headerMap) throws ClientProtocolException, HttpException, IOException{

		String content = Crawler.create().timeOut(30000)
				.header(headerMap).url(url)
				.method(HttpMethod.GET.getValue())
				.resultAsString();

		return content;
	}
	
	/**
	 * 随机取header
	 */
	private Map<String,Object> random(List<Map<String,Object>> headerList){
		int max = headerList.size();
		int min = 0;
		Random random = new Random();
		int s = random.nextInt(max) % (max - min + 1) + min;
		Map<String, Object> headerMap = headerList.get(s);
		return headerMap;
	}
	
	private String replaceAll(String content){
		return content.replaceAll("[\\\\]", "");
	}
	
	/**
	 * 俩个帐号Cookie,其实可以放在配置文件中,赶时间开发,后期再优化
	 * 
	 */
	private static List<Map<String,Object>> getHeaders(String url){
		List<Map<String, Object>> list = new ArrayList<>();
		
		/**
		 * 孙帐号 cookie
		 */
		final Map<String, Object> headers = new HashMap<String, Object>();
		headers.put("User-Agent",
				"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/58.0.3029.81 Chrome/58.0.3029.81 Safari/537.36");
		headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		headers.put("Upgrade-Insecure-Requests", "1");
		if (url.contains("s.weibo.com")) {
			headers.put("Host", "s.weibo.com");
			headers.put("Cookie",
					"SINAGLOBAL=547550869372.2042.1477047012758; _s_tentry=www.51testing.com; Apache=3497548739073.28.1501669165390; ULV=1501669165478:4:1:1:3497548739073.28.1501669165390:1479291915565; login_sid_t=7ef8d5e17e1f95a1150c1b4dcf86521c; SWB=usrmdinst_12; un=gechongy@sina.com; UOR=www.sephora.cn,widget.weibo.com,login.sina.com.cn; WBtopGlobal_register_version=461ddecc2bfe50a1; SCF=AhCxaWsoWs2nUMZknIZ2sBNoDkKpAR5BDol2KcV_BnrnSUTPkAKh8AScfPK7n6q67s6I7DxMPek3sukaZaqPVJU.; SUB=_2A250ntVrDeRhGeNJ6VQQ8SnFzDWIHXVX6kGjrDV8PUNbmtBeLWfRkW8knqYYNTVz36RfNx3hYrTKTq-Q0A..; SUBP=0033WrSXqPxfM725Ws9jqgMF55529P9D9WWiYcamEH10PR3wch.T7Edz5JpX5K2hUgL.Fo-NeoqpeKM4S0.2dJLoIpUTqg4.wCH8SCHWxC-RSEH8SEHFxb-4S5tt; SUHB=00xpPWqx7tsG-2; ALF=1503911867; SSOLoginState=1503307067; un=17701858087; WBStorage=0c663978e8e51f06|undefined");
		} else {
			headers.put("Host", "weibo.com");
			headers.put("Cookie",
					"SINAGLOBAL=547550869372.2042.1477047012758; _s_tentry=www.51testing.com; Apache=3497548739073.28.1501669165390; ULV=1501669165478:4:1:1:3497548739073.28.1501669165390:1479291915565; YF-Ugrow-G0=8751d9166f7676afdce9885c6d31cd61; login_sid_t=7ef8d5e17e1f95a1150c1b4dcf86521c; YF-V5-G0=a9b587b1791ab233f24db4e09dad383c; YF-Page-G0=23b9d9eac864b0d725a27007679967df; WBtopGlobal_register_version=461ddecc2bfe50a1; un=gechongy@sina.com; UOR=www.sephora.cn,widget.weibo.com,login.sina.com.cn; SCF=AhCxaWsoWs2nUMZknIZ2sBNoDkKpAR5BDol2KcV_BnrnSUTPkAKh8AScfPK7n6q67s6I7DxMPek3sukaZaqPVJU.; SUB=_2A250ntVrDeRhGeNJ6VQQ8SnFzDWIHXVX6kGjrDV8PUNbmtBeLWfRkW8knqYYNTVz36RfNx3hYrTKTq-Q0A..; SUBP=0033WrSXqPxfM725Ws9jqgMF55529P9D9WWiYcamEH10PR3wch.T7Edz5JpX5K2hUgL.Fo-NeoqpeKM4S0.2dJLoIpUTqg4.wCH8SCHWxC-RSEH8SEHFxb-4S5tt; SUHB=00xpPWqx7tsG-2; ALF=1503911867; SSOLoginState=1503307067");
		}
		list.add(headers);
		/**
		 * 葛老板帐号 cookie
		 */
//		final Map<String, Object> headers2 = new HashMap<String, Object>();
//		headers2.put("User-Agent",
//				"Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/58.0.3029.81 Chrome/58.0.3029.81 Safari/537.36");
//		headers2.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
//		headers2.put("Upgrade-Insecure-Requests", "1");
//		if (url.contains("s.weibo.com")) {
//			headers2.put("Host", "s.weibo.com");
//			headers2.put("Cookie",
//					"SINAGLOBAL=547550869372.2042.1477047012758; _s_tentry=www.51testing.com; Apache=3497548739073.28.1501669165390; ULV=1501669165478:4:1:1:3497548739073.28.1501669165390:1479291915565; login_sid_t=7ef8d5e17e1f95a1150c1b4dcf86521c; SWB=usrmdinst_12; WBtopGlobal_register_version=461ddecc2bfe50a1; UOR=www.sephora.cn,widget.weibo.com,login.sina.com.cn; SSOLoginState=1503297943; SCF=AhCxaWsoWs2nUMZknIZ2sBNoDkKpAR5BDol2KcV_Bnrn1LMp3JkGaZ3Pwp9vEO2PImh_POHLLPEvjv72VErJaCo.; SUB=_2A250nvHHDeThGeRO6lsX9SrLyTyIHXVX6mQPrDV8PUNbmtBeLXPAkW9FgeSdMNFAxCH9Vo6FNdqROkOrhQ..; SUBP=0033WrSXqPxfM725Ws9jqgMF55529P9D9W5y6Cs.snFfTn5c.Q1.aRKo5JpX5K2hUgL.Foz7eK.cSKBNeo52dJLoIEBLxKqL1K-LBo5LxKqL12BLB--LxKML1KBLBo-LxK-L1K-L1hqt; SUHB=0ft4A-Q4iflzQP; ALF=1534833942; un=gechongy@sina.com; wvr=6; WBStorage=0c663978e8e51f06|undefined");
//		} else {
//			headers2.put("Host", "weibo.com");
//			headers2.put("Cookie",
//					"SINAGLOBAL=547550869372.2042.1477047012758; _s_tentry=www.51testing.com; Apache=3497548739073.28.1501669165390; ULV=1501669165478:4:1:1:3497548739073.28.1501669165390:1479291915565; YF-Ugrow-G0=8751d9166f7676afdce9885c6d31cd61; login_sid_t=7ef8d5e17e1f95a1150c1b4dcf86521c; YF-V5-G0=a9b587b1791ab233f24db4e09dad383c; YF-Page-G0=23b9d9eac864b0d725a27007679967df; WBtopGlobal_register_version=461ddecc2bfe50a1; UOR=www.sephora.cn,widget.weibo.com,login.sina.com.cn; SSOLoginState=1503297943; un=gechongy@sina.com; wvr=6; SCF=AhCxaWsoWs2nUMZknIZ2sBNoDkKpAR5BDol2KcV_BnrnIPg4v6HWYM_5_0JVOOf_FxBXpDFywQsGpajAOtlUsKA.; SUB=_2A250ntRvDeThGeRO6lsX9SrLyTyIHXVX6kKnrDV8PUNbmtBeLWr_kW8rzivs7kC8CZ8yCTizC15PGxEF1Q..; SUBP=0033WrSXqPxfM725Ws9jqgMF55529P9D9W5y6Cs.snFfTn5c.Q1.aRKo5JpX5KzhUgL.Foz7eK.cSKBNeo52dJLoIEBLxKqL1K-LBo5LxKqL12BLB--LxKML1KBLBo-LxK-L1K-L1hqt; SUHB=04W_W8hXw561Bo; ALF=1534842815");
//		}
//		list.add(headers2);
		 
		return list;
	}
	
	private static String getRedirectUrl(String path) throws Exception {
		HttpURLConnection conn = (HttpURLConnection) new URL(path).openConnection();
		conn.setInstanceFollowRedirects(false);
		conn.setConnectTimeout(15000);
		return conn.getHeaderField("Location");
	}
	public static void main(String[] args) throws Exception {
		try {
			System.out.println(getRedirectUrl("http://weibo.com/aj/user/newcard?id=1769684987"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
