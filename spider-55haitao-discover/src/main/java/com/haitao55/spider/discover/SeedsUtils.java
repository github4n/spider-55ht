package com.haitao55.spider.discover;

import java.io.ByteArrayInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.gargoylesoftware.htmlunit.html.HtmlPage;
import com.gargoylesoftware.htmlunit.util.UrlUtils;

/**
 * 
 * 功能：Seeds模块范围的静态工具类
 * 
 * @author arthur
 * @time 2016年2月17日 下午2:31:36
 * @version 1.0
 */
public class SeedsUtils {
	private static final Logger logger = LoggerFactory.getLogger("system");

	@SuppressWarnings("unused")
	private static final String RULE_CONTENT_ELE_KEY = "rule_content";
	@SuppressWarnings("unused")
	private static final String META_VALUE_PREFIX = "<![CDATA[";
	@SuppressWarnings("unused")
	private static final String META_VALUE_SUFFIX = "]]>";

	/**
	 * 
	 * 功能：由字符串格式的配置文件内容，构建出
	 * 
	 * @param ruleContent
	 * @return
	 */
	public static SeedsRule buildSeedsRuleFromRuleContent(String ruleContent) {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		DocumentBuilder db;
		try {
			db = dbf.newDocumentBuilder();
		} catch (ParserConfigurationException e1) {
			logger.error("构建DocumentBuilder失败！");
			return null;
		}

		Document doc;
		ByteArrayInputStream bais = null;
		try {
			bais = new ByteArrayInputStream(ruleContent.getBytes("utf-8"));
			doc = db.parse(new InputSource(bais));
		} catch (Exception e) {// 这里很可能会抛异常，因为手工配置的配置文件千奇百怪
			logger.error("由rule content字符串解析Document文档时出错e：{}", e);
			return null;
		} finally {
			IOUtils.closeQuietly(bais);
		}

		SeedsRule seedsRule = new SeedsRule();

		Element rootEle = doc.getDocumentElement();
		Element seedsRuleElement = (Element) rootEle.getElementsByTagName("seedsRule").item(0);
		String timeOutInSecond = seedsRuleElement.getAttribute("timeOutInSecond");
		String hopCount = seedsRuleElement.getAttribute("hopCount");
		String baseUrl = seedsRuleElement.getAttribute("baseUrl");
		String baseCss = seedsRuleElement.getAttribute("baseCss");
		String firstPageOnly = seedsRuleElement.getAttribute("firstPageOnly");
		String secondaryCss = seedsRuleElement.getAttribute("secondaryCss");
		String tertiaryCss = seedsRuleElement.getAttribute("tertiaryCss");
		String nextCss = seedsRuleElement.getAttribute("nextCss");
		String totalCountCss = seedsRuleElement.getAttribute("totalCountCss");
		String totalProductCountCss = seedsRuleElement.getAttribute("totalProductCountCss");
		String productCountPerPage = seedsRuleElement.getAttribute("productCountPerPage");
		seedsRule.setTimeOutInSecond(Integer.parseInt(timeOutInSecond));
		seedsRule.setHopCount(Integer.parseInt(hopCount));
		seedsRule.setBaseUrl(baseUrl);
		seedsRule.setBaseCss(baseCss);
		seedsRule.setFirstPageOnly(
				StringUtils.isNotBlank(firstPageOnly) && (StringUtils.equalsIgnoreCase(firstPageOnly, "yes")
						|| StringUtils.equalsIgnoreCase(firstPageOnly, "true")));
		seedsRule.setSecondaryCss(secondaryCss);
		seedsRule.setTertiaryCss(tertiaryCss);
		seedsRule.setNextCss(nextCss);
		seedsRule.setTotalCountCss(totalCountCss);
		seedsRule.setTotalProductCountCss(totalProductCountCss);
		try {
			seedsRule.setProductCountPerPage(Integer.parseInt(productCountPerPage));
		} catch (Exception e) {
			// Ignore
		}
		seedsRule.setReplacement(seedsRule.new Replacement());
		seedsRule.setControl(seedsRule.new Control());

		Element replacementElement = (Element) seedsRuleElement.getElementsByTagName("replacement").item(0);
		String nextUrlRegex = replacementElement.getAttribute("nextUrlRegex");
		String replaceStartIndex = replacementElement.getAttribute("replaceStartIndex");
		String replaceRegex = replacementElement.getAttribute("replaceRegex");
		String replaceFormat = replacementElement.getAttribute("replaceFormat");
		seedsRule.getReplacement().setNextUrlRegex(nextUrlRegex);
		seedsRule.getReplacement().setReplaceStartIndex(Integer.parseInt(replaceStartIndex));
		seedsRule.getReplacement().setReplaceRegex(replaceRegex);
		seedsRule.getReplacement().setReplaceFormat(replaceFormat);

		Element controlElement = (Element) seedsRuleElement.getElementsByTagName("control").item(0);
		String mergedb = controlElement.getAttribute("mergedb");
		seedsRule.getControl().setMergedb(StringUtils.isNotBlank(mergedb)
				&& (StringUtils.equalsIgnoreCase(mergedb, "yes") || StringUtils.equalsIgnoreCase(mergedb, "true")));
		String threadsCount = controlElement.getAttribute("threadsCount");
		try {
			seedsRule.getControl().setThreadsCount(Integer.parseInt(threadsCount));
		} catch (Exception e) {
			seedsRule.getControl().setThreadsCount(1);// 如果配置文件中没有配置这个属性，或者配置错误（比如不是数字），则默认启动一个线程来执行抓取
		}
		String enableJs = controlElement.getAttribute("enableJs");
		seedsRule.getControl().setEnableJs(StringUtils.isNotBlank(enableJs)
				&& (StringUtils.equalsIgnoreCase(enableJs, "yes") || StringUtils.equalsIgnoreCase(enableJs, "true")));

		return seedsRule;
	}

	/**
	 * 
	 * 功能：由数据库存储格式的配置文件内容，得到真实干净的配置文件内容
	 * 
	 * @param rule
	 * @return
	 */
	public static String getRuleContentFromWrappedRuleContent(/*
																 * SeedsRuleDO
																 * rule
																 */) {
		// if (rule != null) {
		// String wrappedRuleContent = rule.getRuleContent();
		// String temp = StringUtils.substringBetween(wrappedRuleContent,
		// RULE_CONTENT_ELE_KEY, RULE_CONTENT_ELE_KEY);
		// return StringUtils.substringBetween(temp, META_VALUE_PREFIX,
		// META_VALUE_SUFFIX);
		// }

		// if (rule != null) {// 因为现在config是以干净的值存储在mongoDB数据库中的，所以直接返回使用就可以了
		// String ruleContent = rule.getRuleContent();
		// return ruleContent;
		// }

		return "";
	}

	/**
	 * 
	 * 功能：通过使用Htmlunit工具来抓取一个url所代表的html页面
	 */
	public static String crawlViaHtmlunit(String url, int timeOut, boolean enableJs) {
		if (StringUtils.isBlank(url)) {
			return null;
		}

		WebClient webClient = new WebClient(CrawlBrowserVersion.CHROME.getBrowserVersion());
		webClient.getOptions().setThrowExceptionOnScriptError(false);
		webClient.getOptions().setThrowExceptionOnFailingStatusCode(false);
		webClient.getOptions().setPrintContentOnFailingStatusCode(false);
		webClient.getOptions().setUseInsecureSSL(false);
		webClient.getOptions().setRedirectEnabled(true);
		webClient.getOptions().setCssEnabled(true);
		webClient.getOptions().setTimeout(timeOut);
		webClient.getOptions().setJavaScriptEnabled(enableJs);

		HtmlPage htmlPage = null;
		try {
			WebRequest webRequest = new WebRequest(UrlUtils.toUrlUnsafe(url));
			htmlPage = webClient.getPage(webRequest);
			if (null != htmlPage) {
				return htmlPage.asXml();
			}
		} catch (Exception e) {
			logger.error("Error occured while crawl via htmlunit::url:{};e:{}", url, e);
		} finally {
			if (null != htmlPage) {
				htmlPage.cleanUp();
			}
			if (null != webClient) {
				webClient.closeAllWindows();
			}
		}

		return "";
	}

	/**
	 * 
	 * 功能：处理url字符串中的一些特殊字符
	 * 
	 * @param url
	 *            待处理的url字符串
	 * @return 处理后的url字符串
	 */
	public static String handleSpecialCharInUrl(String url) {
		if (url != null && url.trim().length() > 0 && url.indexOf("|") > 0) {
			return url.replaceAll("\\|", "%7c");
		}

		return url;
	}
}