package com.haitao55.spider.ui.common.util;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.common.http.Crawler;
import com.haitao55.spider.common.http.HttpException;
import com.haitao55.spider.common.utils.Constants;
import com.haitao55.spider.common.utils.DetailUrlCleaningTool;
import com.haitao55.spider.crawler.common.cache.ProxyCache;
import com.haitao55.spider.crawler.core.model.Proxy;
import com.haitao55.spider.crawler.utils.JsoupUtils;

/**
 * 功能：工具类,用来进行items数据抓取 不同网站可能需要特殊处理，故使用这个工具类进行实现
* Title:
* Description:
* Company: 55海淘
* @author zhaoxl 
* @date 2017年1月17日 下午5:40:24
* @version 1.0
 */
public class ItemsCrawlerTool {
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_SYSTEM);
	private static final String ATTR_HREF = "href";

	private static class Holder {
		private static ItemsCrawlerTool instance = new ItemsCrawlerTool();
	}

	public static ItemsCrawlerTool getInstance() {
		return Holder.instance;
	}

	// <Pattern, methodName>
	private Map<Pattern, String> PATTERN_CACHE = new ConcurrentHashMap<Pattern, String>();
	// <domain, Method>
	private Map<String, Method> METHOD_CACHE = new ConcurrentHashMap<String, Method>();

	/**
	 * 方法功能:通过反射调用方法, 达到清洗商品链接格式的目的
	 * 
	 * @param item_css
	 * @param website_preffix 
	 * @param originalUrl 
	 * @return
	 */
	public List<String> itemsCrawler(String item_css, String originalUrl, String website_preffix,String proxyRegionId) {
		String temp = StringUtils.substringAfter(originalUrl, "//");
		String domain = StringUtils.substringBefore(temp, "/");
		Method method = METHOD_CACHE.get(domain);
		
		if (!Objects.isNull(method)) {
			try {
				Object rst = method.invoke(this, item_css , originalUrl,website_preffix,proxyRegionId);
				return (List<String>)rst;
			} catch (Exception e) {
				logger.error("", e);
			}
		}
		logger.info("itemsCrawler domain: {} , MapCache : {}",domain,PATTERN_CACHE);
		for (Entry<Pattern, String> entry : PATTERN_CACHE.entrySet()) {
			Pattern key = entry.getKey();
			String value = entry.getValue();

			if (key.matcher(domain).find()) {
				try {
					Method meth = this.getClass().getDeclaredMethod(value, String.class,String.class,String.class,String.class);

					METHOD_CACHE.put(domain, meth);

					Object rst = meth.invoke(this, item_css , originalUrl,website_preffix,proxyRegionId);
					return (List<String>)rst;
				} catch (Exception e) {
					logger.error("", e);
				}
			}
		}

		return null;
	}

	/**
	 * 测试方法
	 */
	public static void main(String... args) {
		ItemsCrawlerTool tool = ItemsCrawlerTool.getInstance();
		
	}
	// =============================================================================================//
	// ====================================这个文件,只需要修改这块区域之下的内容===========================//
	// =============================================================================================//
	private ItemsCrawlerTool() {// 私有构造方法
		PATTERN_CACHE.put(Pattern.compile(".*zh.ashford.com.*"), "itemsAshford");
		PATTERN_CACHE.put(Pattern.compile(".*www.escentual.com.*"), "itemsEscentual");
		PATTERN_CACHE.put(Pattern.compile(".*shop.nordstrom.com.*"), "itemsNordstrom");
		PATTERN_CACHE.put(Pattern.compile(".*www.lookfantastic.com.*"), "itemsLookfantastic");
		PATTERN_CACHE.put(Pattern.compile(".*www.cosme-de.com.*"), "itemsCosmeDe");
		PATTERN_CACHE.put(Pattern.compile(".*www.rebeccaminkoff.com.*"), "itemsRebeccaminkoff");
		PATTERN_CACHE.put(Pattern.compile(".*www.katespade.com.*"), "itemsKatespade");
		PATTERN_CACHE.put(Pattern.compile(".*www.michaelkors.com.*"), "itemsMichaelkors");
		PATTERN_CACHE.put(Pattern.compile(".*www.saksfifthavenue.com.*"), "itemsSaksfifthavenue");
		PATTERN_CACHE.put(Pattern.compile(".*www.ninewest.com.*"), "itemsNinewest");
		PATTERN_CACHE.put(Pattern.compile(".*www.6pm.com.*"), "items6pm");
		PATTERN_CACHE.put(Pattern.compile(".*www.macys.com.*"), "itemsMacys");
		PATTERN_CACHE.put(Pattern.compile(".*cn.feelunique.com.*"), "itemsFeelunique");
		PATTERN_CACHE.put(Pattern.compile(".*www.skinstore.com.*"), "itemsSkinstore");
		PATTERN_CACHE.put(Pattern.compile(".*www.mankind.co.*"), "itemsMankind");
	}
	
	@SuppressWarnings("unused")
	private List<String> itemsAshford(String item_css, String originalUrl, String website_preffix,String proxyRegionId) {
		return generalItemsCrawler(item_css,originalUrl,website_preffix,proxyRegionId,null);
	}
	@SuppressWarnings("unused")
	private List<String> itemsEscentual(String item_css, String originalUrl, String website_preffix,String proxyRegionId) {
		return generalItemsCrawler(item_css,originalUrl,website_preffix,proxyRegionId,null);
	}
	@SuppressWarnings("unused")
	private List<String> itemsNordstrom(String item_css, String originalUrl, String website_preffix,String proxyRegionId) {
		return generalItemsCrawler(item_css,originalUrl,website_preffix,proxyRegionId,null);
	}
	@SuppressWarnings("unused")
	private List<String> itemsLookfantastic(String item_css, String originalUrl, String website_preffix,String proxyRegionId) {
		return generalItemsCrawler(item_css,originalUrl,website_preffix,proxyRegionId,null);
	}
	@SuppressWarnings("unused")
	private List<String> itemsCosmeDe(String item_css, String originalUrl, String website_preffix,String proxyRegionId) {
		return cosmeDeCrawler(item_css,originalUrl,website_preffix,proxyRegionId);
	}
	@SuppressWarnings("unused")
	private List<String> itemsRebeccaminkoff(String item_css, String originalUrl, String website_preffix,String proxyRegionId) {
		return generalItemsCrawler(item_css,originalUrl,website_preffix,proxyRegionId,null);
	}
	@SuppressWarnings("unused")
	private List<String> itemsKatespade(String item_css, String originalUrl, String website_preffix,String proxyRegionId) {
		return generalItemsCrawler(item_css,originalUrl,website_preffix,proxyRegionId,null);
	}
	@SuppressWarnings("unused")
	private List<String> itemsMichaelkors(String item_css, String originalUrl, String website_preffix,String proxyRegionId) {
		return generalItemsCrawler(item_css,originalUrl,website_preffix,proxyRegionId,null);
	}
	@SuppressWarnings("unused")
	private List<String> itemsSaksfifthavenue(String item_css, String originalUrl, String website_preffix,String proxyRegionId) {
		return generalItemsCrawler(item_css,originalUrl,website_preffix,proxyRegionId,getSaksfifthavenueHeaders());
	}
	@SuppressWarnings("unused")
	private List<String> itemsNinewest(String item_css, String originalUrl, String website_preffix,String proxyRegionId) {
		return generalItemsCrawler(item_css,originalUrl,website_preffix,proxyRegionId,null);
	}
	@SuppressWarnings("unused")
	private List<String> items6pm(String item_css, String originalUrl, String website_preffix,String proxyRegionId) {
		return generalItemsCrawler(item_css,originalUrl,website_preffix,proxyRegionId,null);
	}
	@SuppressWarnings("unused")
	private List<String> itemsMacys(String item_css, String originalUrl, String website_preffix,String proxyRegionId) {
		if(StringUtils.containsIgnoreCase(originalUrl, "|")){
			originalUrl = originalUrl.replace("|", "%7C");
		}
		return generalItemsCrawler(item_css,originalUrl,website_preffix,proxyRegionId,null);
	}
	@SuppressWarnings("unused")
	private List<String> itemsFeelunique(String item_css, String originalUrl, String website_preffix,String proxyRegionId) {
		return generalItemsCrawler(item_css,originalUrl,website_preffix,proxyRegionId,null);
	}
	@SuppressWarnings("unused")
	private List<String> itemsSkinstore(String item_css, String originalUrl, String website_preffix,String proxyRegionId) {
		return generalItemsCrawler(item_css,originalUrl,website_preffix,proxyRegionId,null);
	}
	@SuppressWarnings("unused")
	private List<String> itemsMankind(String item_css, String originalUrl, String website_preffix,String proxyRegionId) {
		return generalItemsCrawler(item_css,originalUrl,website_preffix,proxyRegionId,null);
	}
	
	// =============================================================================================//
	// ====================================这个文件,只需要修改这块区域之上的内容========================//
	// =============================================================================================//
	
	/**
	 * 通用爬取
	 * @param item_css
	 * @param originalUrl
	 * @param website_preffix
	 * @param proxyRegionId 
	 * @param headers 
	 * @return
	 */
	private List<String> generalItemsCrawler(String item_css, String originalUrl, String website_preffix, String proxyRegionId ,Map<String,Object> headers) {
		List<String> newUrlValues = new ArrayList<String>();
		String content = StringUtils.EMPTY;
		try {
			content = crawler_result(proxyRegionId, originalUrl,headers);
		} catch (ClientProtocolException e) {
		}catch (HttpException e) {
		}catch (IOException e) {
		}
		
		if(StringUtils.isBlank(content)){
			return newUrlValues;
		}
		
		
		Document doc = JsoupUtils.parse(content);
		Elements elements = doc.select(item_css);
		if (CollectionUtils.isNotEmpty(elements)) {
			newUrlValues = JsoupUtils.attrs(elements, ATTR_HREF);

			// 只有在newUrlValues不为空且regex不为空时,才改装newUrlValues
			if (CollectionUtils.isNotEmpty(newUrlValues)) {
				newUrlValues = this.reformStrings(newUrlValues,website_preffix);
			}
		}
		return newUrlValues;
	}

	private List<String> reformStrings(List<String> strings, String website_preffix) {
		List<String> result = new ArrayList<String>();
		DetailUrlCleaningTool instance = DetailUrlCleaningTool.getInstance();
		
		if (CollectionUtils.isNotEmpty(strings)) {
			for (String string : strings) {
				if(StringUtils.isBlank(string)){
					continue ;
				}
				String reformedString = StringUtils.EMPTY;
				if(!StringUtils.contains(string, "http")){
					reformedString = website_preffix.concat(string);
				}else{
					reformedString = string;
				}
				reformedString = instance.cleanDetailUrl(reformedString);
				result.add(reformedString);
			}
		}

		return result;
	}
	
	
	/***网站特有种子获取规则　start***/
	
	/***cosme-de***/
	/**
	 * cosme-de　获取种子链接
	 * @param item_css
	 * @param originalUrl
	 * @param website_preffix
	 * @param proxyRegionId 
	 * @return
	 */
	private List<String> cosmeDeCrawler(String item_css, String originalUrl, String website_preffix, String proxyRegionId) {
		List<String> newUrlValues = new ArrayList<String>();
		String content = StringUtils.EMPTY;
		try {
			content = crawler_result(proxyRegionId, originalUrl,null);
		} catch (ClientProtocolException e) {
		}catch (HttpException e) {
		}catch (IOException e) {
		}
		
		if(StringUtils.isBlank(content)){
			return newUrlValues;
		}
		
		
		Document doc = JsoupUtils.parse(content);
		Elements elements = doc.select(item_css);
		if (CollectionUtils.isNotEmpty(elements)) {
			newUrlValues = JsoupUtils.attrs(elements, ATTR_HREF);

			// 只有在newUrlValues不为空且regex不为空时,才改装newUrlValues
			if (CollectionUtils.isNotEmpty(newUrlValues)) {
				newUrlValues = this.cosmeDeReformStrings(newUrlValues,website_preffix);
			}
		}
		return newUrlValues;
	}

	private List<String> cosmeDeReformStrings(List<String> strings, String website_preffix) {
		List<String> result = new ArrayList<String>();
		DetailUrlCleaningTool instance = DetailUrlCleaningTool.getInstance();
		
		if (CollectionUtils.isNotEmpty(strings)) {
			for (String string : strings) {
				String reformedString = StringUtils.EMPTY;
				string = StringUtils.substringAfter(string,"..");
				if(!StringUtils.contains(string, "http")){
					reformedString = website_preffix.concat("/en").concat(string);
				}else{
					reformedString = string;
				}
				reformedString = instance.cleanDetailUrl(reformedString);
				result.add(reformedString);
			}
		}

		return result;
	}
	/***cosme-de***/
	
	/***网站特有种子获取规则　end***/
	
	
	
	/***任务是否使用代理ip**/
	/**
	 * 线上爬取
	 * @param proxyRegionId
	 * @param path
	 * @param headers 
	 * @return
	 * @throws ClientProtocolException
	 * @throws HttpException
	 * @throws IOException
	 */
	private String crawler_result(String proxyRegionId, String path, Map<String, Object> headers) throws ClientProtocolException, HttpException, IOException {
		String content = StringUtils.EMPTY;
		 if(StringUtils.isBlank(proxyRegionId)){
			 if(null != headers && !headers.isEmpty()){
				 
				 content = Crawler.create().timeOut(60000).url(path).header(headers).proxy(false).resultAsString();
			 }else{
				 
				 content = Crawler.create().timeOut(60000).url(path).proxy(false).resultAsString();
			 }
		 }else{
			 Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId,
					 true);
			 String proxyAddress=proxy.getIp();
			 int proxyPort=proxy.getPort();
			 if(null != headers && !headers.isEmpty()){
				 content = Crawler.create().timeOut(60000).url(path).header(headers).proxy(true).proxyAddress(proxyAddress)
						 .proxyPort(proxyPort).resultAsString();
			 }else{
				 content = Crawler.create().timeOut(60000).url(path).proxy(true).proxyAddress(proxyAddress)
						 .proxyPort(proxyPort).resultAsString();
			 }
		 }
		 return content;
	}
	
	/**headers*/
	private static Map<String,Object> getSaksfifthavenueHeaders(){
		final Map<String, Object> headers = new HashMap<String, Object>();
		headers.put("User-Agent", "Mozilla/5.0 (compatible; MSIE 10.0; Windows NT 6.1; WOW64; Trident/6.0; BIDUBrowser 2.x");
		 headers.put("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		 headers.put("Upgrade-Insecure-Requests", "1");
		 headers.put("Host", "www.saksfifthavenue.com");
		 headers.put("Cookie", "PSSALE=DEFAULT; s_vi=[CS]v1|2BBFC6A8852A03E2-4000010540081232[CE]; _qsst_s=1470110363764; cm=Typed%2FBookmarkedTyped%2FBookmarkedundefined; fsr.s=%7B%22v2%22%3A-2%2C%22v1%22%3A1%2C%22rid%22%3A%22de35430-94969414-c2bd-1614-d631a%22%2C%22cp%22%3A%7B%22LoggedIn%22%3A%220%22%2C%22Order_Number%22%3A%220%22%2C%22Loyalty%22%3A%220%22%2C%22PaymentMethod%22%3A%220%22%7D%2C%22to%22%3A10%2C%22c%22%3A%22http%3A%2F%2Fwww.saksfifthavenue.com%2Fmain%2FProductDetail.jsp%22%2C%22pv%22%3A2%2C%22lc%22%3A%7B%22d1%22%3A%7B%22v%22%3A2%2C%22s%22%3Afalse%7D%7D%2C%22cd%22%3A1%2C%22sd%22%3A1%7D; E4X_CURRENCY=USD; PSMS_EXP=2015-12-12 10:07:30; usy46gabsosd=sakscsa__186902665_1470110551648_1470109965637_7417; VIP_PRICE=OFF; sakscsauvt=7d556e63be644f0f85fe6d9767a1e8ab_1467977052492_186902665_1470109965637_2; __cmbTpvTm=383; E4X_COUNTRY=US; rr_rcs=eF5jYSlN9jA3TbG0SElK0TW1MDXSNTEzMtRNSjFO0U1NMTUxSU5OM0gxMeXKLSvJTOGxMNU11DUEAIKcDjg; qb_ss_status=BN7V:Ml&Orj; wlcme=true; PSSALE_EXP=2016-11-28T06:05:03; PS=DEFAULT; sakscsaDBID=14_null; _cavisit=1564961b34e|; _bcvm_vrid_4405020731204665754=717976135557928474T9404A6E759CD8F85A5D70717D9367DED0963C91B63DA6F2C08182266BB6D9A737E18CAE091E48A0A001B33E6B27780831451C7D3A84698A2856CD7092975524C; t013pv=2; skswlcme=true; sf_wdt_customer_id=3ej495i5w; _qubitTracker_s=1448865344691.625675; _bcvm_vid_4405020731204665754=717997464882030170TA07EAD9A28B90220FFCB907F55A43897B4EF0A3A3192C487D119C4DBB34E892B192948CAECE0AA5FE6E5349678CE2C69FC707928B343A12EFF11F220D06C6289; _qst_s=2; v55=%5B%5B%2730NOV2015%253A02%253A35%253A41%27%2C%271448865341925%27%5D%5D; TS54c0f7=ff3393595c1828a0415b2cb4f0a6da71baf55c5b562d702458381e1b55627810e5ef4f1860ac0ec5a4ec577a866d39d0b12f65515283d66affffffff2396c135d4d3511daf1a552cc2aced9040fda161d4d3511d63b6a6986ecb234d7d8b0044d4d3511d48de5bd5c2aced909cbcf6a071256871; c38=saks.com%3Aproduct%20detail%3Atory%20burch%3Athea%20woven%20satchel; _qubitTracker=1467977051015.766497; s_cc=true; s_eVar3=mhpmainm7s3l%7D; TLTSID=8606B122586410583E82C206E1EBBBF1; _qb_se=BN7V:Orj&VZJaGv3; v11=%5B%5B%27Google%27%2C%271448865341924%27%5D%5D; s_ppvl=saks.com%253Aproduct%2520detail%253Atory%2520burch%253Athea%2520woven%2520satchel%2C24%2C24%2C722%2C1280%2C702%2C1280%2C800%2C2%2CP; AKAI=3:1470114983; cache_buster_cookie=1470109963195; _caid=aee51edd-025e-4ed6-a52b-ab1beca70652; sf_siterefer=salesfloor; _ga=GA1.2.1115772862.1467977040; TLTHID=FA855854B30010B30031F091B7666880; sr_exp_rvo=0; v0=1; PS_EXP=2016-11-28T06:05:03; sessionID=1470109952452d9PAQ7T7Ln1TRRg5lEBDq8RcNkTuAVvi7vpUSrvgAwxAQQzKkCNkTDQH; saksBagNumberOfItems=0; _qst=%5B2%2C0%5D; EML1145A=TRUE; sakscsakey=f88749057d9d42b78939df0160696b3f; sr_locale=us; JSESSIONID=NtBLY4hf9lgxngMSTV3HYtSLj3Pw1vhJLLRB5SB1sD6LfcSL3G21!138045752; sf_change_page=true; __cmbDomTm=0; bc_pv_end=717997469065710654TD25459630FE587496A78487CFB4DB24BEA9080CBBC0F56C39801A3648D10EB9ED8C25C5223DADAD831B95E087240B10799A6266966A39CDD17210FECB656D0FD; t013-block=true; _sp_id.7a58=338f8db4405da4bb.1467977052.3.1470110542.1467978776; _sp_ses.7a58=*; ss_opts=BN7V:B&B|_g:VZJYeRl&VZJZ/Yc&C&C; SA74=35bC_M1lzZQyg2DkvwK8hHOaAKlyoWCCdZXvqHjG0aab2bAXlrjafHQ; _qPageNum_saks=1; PSMS=DEFAULT; s_ppv=saks.com%253Aproduct%2520detail%253Atory%2520burch%253Athea%2520woven%2520satchel%2C24%2C24%2C722%2C491%2C702%2C1280%2C800%2C2%2CL; qb_permanent=1467977051015.766497:4:1:4:4:0::0:1:0:BXf41d:BXf41d:::::27.115.111.22:shanghai:7391:china:CN:31.2307:121.473:unknown:156073:shanghai:10698:segments~::Orj; _qsst=1470110364074; saksBrowserWarnings=true; v50=%5B%5B%27natural%2520search%27%2C%271448865341926%27%5D%5D; sr_browser_id=93294629-9cc5-47ea-9d37-a205f5f68afc; qb_session=1:1:14:; PSC=null; mbox=PC#1467977025773-821861.24_2#1475294355|session#1470109947197-10314#1470112215|check#true#1470110415; s_fid=7CF176D3624632AC-3AF53877A17CCF5A; __cmbU=ABJeb19e3N49kewgbWX4yyElqZtnPYxsRBnI7tg5KBVAPUsiDT6-0MwB8npXzh3U5Oix8tQm90vcX3JWCpBpe6_J0OGfIzzIlQ; TS54c0f7=ff3393595c1828a0415b2cb4f0a6da71baf55c5b562d702458381e1b55627810e5ef4f1860ac0ec5a4ec577a866d39d0b12f65515283d66affffffff2396c135d4d3511daf1a552cc2aced9040fda161d4d3511d63b6a6986ecb234d7d8b0044d4d3511d48de5bd5c2aced909cbcf6a071256871; JSESSIONID=NtBLY4hf9lgxngMSTV3HYtSLj3Pw1vhJLLRB5SB1sD6LfcSL3G21!138045752; PSSALE=DEFAULT; PS=DEFAULT; PSMS_EXP=2015-12-12 10:07:30; TLTHID=FA855854B30010B30031F091B7666880; PSSALE_EXP=2016-11-28T06:05:03; PS_EXP=2016-11-28T06:05:03; saksBagNumberOfItems=0; PSMS=DEFAULT");
		return headers;
	}
	/**headers*/
}