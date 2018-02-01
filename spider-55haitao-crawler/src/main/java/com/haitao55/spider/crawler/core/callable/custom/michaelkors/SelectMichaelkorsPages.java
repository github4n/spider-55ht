package com.haitao55.spider.crawler.core.callable.custom.michaelkors;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.common.http.Crawler;
import com.haitao55.spider.common.http.HttpException;
import com.haitao55.spider.crawler.common.cache.ProxyCache;
import com.haitao55.spider.crawler.core.callable.SelectUrls;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelectUrls;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Proxy;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.core.model.UrlType;
import com.haitao55.spider.crawler.utils.Constants;
import com.haitao55.spider.crawler.utils.JsoupUtils;

/**
 * 
 * 功能：通过css选择器,提取和计算页面上的翻页urls超级链接
 * 
 * @author Arthur.Liu
 * @time 2016年8月22日 下午3:02:22
 * @version 1.0
 * @see Select
 * @see SelectUrls
 */
public class SelectMichaelkorsPages extends AbstractSelectUrls {
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private static final String PAGE_URL = "https://www.michaelkors.com/server/data/guidedSearch?stateIdentifier=";
	private static final String DEFAULT_ATTR_ABS_HREF = "abs:href";
	private static final String DEFAULT_ATTR_TEXT = "text";
	private static final String DEFAULT_START_PAGE_INDEX = "1";
	private static final String PLACEHOLDER = "\\{\\}";

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
	
	private int grade;
	
	//列表页　匹配规则
	private String pageFlag;

	@Override
	public void invoke(Context context) throws Exception {
		int totalPagesInt = 1;

		String content = crawler_package(context);
		
		if (StringUtils.isNotBlank(this.cssTotalPages)) {
			String totalPages = this.selectTotalPages(context);
			totalPages = this.handleTotalPages(totalPages);
			if (StringUtils.isNotBlank(totalPages)) {
				totalPagesInt = Integer.parseInt(totalPages);
			}
		}
		if (1 == totalPagesInt) {
			if (StringUtils.isNotBlank(this.cssTotalItems) && StringUtils.isNotBlank(this.itemsPerPage)) {
				String totalItems = this.selectTotalItems(content);
				if (StringUtils.isNotBlank(totalItems)) {
					totalPagesInt = (Integer.parseInt(totalItems) / Integer.parseInt(this.itemsPerPage)) + 1;
				}
			}
		}
		// 如果只有一页的情况
		if (totalPagesInt == 1) {
			return;
		}

		String param = StringUtils.EMPTY;
		if(StringUtils.isNotBlank(content)){
			param = StringUtils.substringBetween(content, "\"navStateId\":\"", "\"}");
		}
		String suffix = param+"?No={}&Nrpp="+itemsPerPage;
//		String templateUrl = PAGE_URL+temp;

		List<String> newUrlValues = new ArrayList<String>();
		for (int i = Integer.parseInt(startIndex); i <= totalPagesInt; i++) {
			
			String temp = suffix.replaceAll(PLACEHOLDER, String.valueOf((i-1)*Integer.parseInt(itemsPerPage)));
			String pageUrl = PAGE_URL + URLEncoder.encode(temp, "utf-8");
			newUrlValues.add(pageUrl);
		}

		// 翻页操作,最后取得的urls,肯定是link类型的url
		Set<Url> value = this.buildNewUrls(newUrlValues, context, UrlType.LINK.getValue(),grade);

		// 新迭代出来的urls,放置在Url对象的newUrls中；而不是放置在context中
		context.getUrl().getNewUrls().addAll(value);
	}

	private String selectTotalPages(Context context) {
		String totalPages = "";

		Document doc = super.getDocument(context);
		Elements elementsTotalPages = doc.select(this.cssTotalPages);
		if (CollectionUtils.isNotEmpty(elementsTotalPages)) {
			if (DEFAULT_ATTR_TEXT.equals(attrTotalPages)) {
				totalPages = elementsTotalPages.get(0).text();
			} else {
				totalPages = elementsTotalPages.get(0).attr(attrTotalPages);
			}
		} else {
			logger.warn("css got no elements,url:{} ,css:{}", context.getUrl().getValue(), this.cssTotalPages);
		}

		return totalPages;
	}

	private String selectTotalItems(String content) {
		String totalItems = "";

		Document doc = JsoupUtils.parse(content);
		Elements elementsTotalItems = doc.select(this.cssTotalItems);
		if (CollectionUtils.isNotEmpty(elementsTotalItems)) {
			if (DEFAULT_ATTR_TEXT.equals(attrTotalItems)) {
				totalItems = elementsTotalItems.get(0).text();
			} else {
				totalItems = elementsTotalItems.get(0).attr(attrTotalItems);
			}
		} 

		return totalItems;
	}
	
	private String crawler_package(Context context) throws ClientProtocolException, HttpException, IOException {
		Map<String,Object> headers = new HashMap<String,Object>();
		headers.put("Accept", "application/json, text/plain, */*");
		headers.put("Accept-Encoding", "gzip, deflate, sdch, br");
		headers.put("Accept-Language", "zh-CN,zh;q=0.8");
		headers.put("Connection", "keep-alive");
		headers.put("Content-Type", "application/json;charset=utf-8");
//		headers.put("Cookie", "acceptance=; _ga=GA1.1.1281525637.1481867704; _sdsat_session_count=4; _sdsat_lt_pages_viewed=352; WLS_ROUTE=.www.c; AMCVS_3D6068F454E7858C0A4C98A6%40AdobeOrg=1; BVImplmain_site=19826; JSESSIONID=XhzVo9r4LOrxEskzTzZQisBpe2cxvNT8zT_rBlJjBiHSNhdMSeVI!379461691; ATG_SESSION_ID=XhzVo9r4LOrxEskzTzZQisBpe2cxvNT8zT_rBlJjBiHSNhdMSeVI!379461691!1489642969849; sessionTimeoutRedirect=pageRefreshed; lightBox=lightBoxCookie; AMCV_3D6068F454E7858C0A4C98A6%40AdobeOrg=2121618341%7CMCIDTS%7C17242%7CMCMID%7C19559687538777690422262280029028408631%7CMCAAMLH-1490242001%7C9%7CMCAAMB-1490258109%7CNRX38WO0n5BH8Th-nqAG_A%7CMCOPTOUT-1489660509s%7CNONE%7CMCAID%7CNONE%7CMCSYNCSOP%7C411-17249; _ga=GA1.2.1281525637.1481867704; userPrefLanguage=en_US; cookieLanguage=en_US; rr_rcs=eF4FwbsRgCAUBMCEyF5uhuN96cA2EAwMzNT63S3b_T3XqmQ4qNndPOjpAifA8s59tePsJgE1ClSZmCMJ6eZ1WEgb9gNrrxDb; s_cc=true; gpv_pn=Home%20%3E%20SHOES%20%3E%20VIEW%20ALL%20SHOES; gpv_purl=%2Fshoes%2Fview-all-shoes%2F_%2FN-28ba; gpv_ptyp=SubCategory; productMerchNum=12; dtm_pageviews=29; s_sq=%5B%5BB%5D%5D; xyz_cr_356_et_100==NaN&cr=356&et=100&ap=; s_nr=1489657189247-Repeat; s_vs=1; mt.v=2.109331950.1481867698629; RT=\"sl=11&ss=1489656331153&tt=58294&obo=0&sh=1489657191518%3D11%3A0%3A58294%2C1489657126826%3D10%3A0%3A55484%2C1489657027448%3D9%3A0%3A44193%2C1489656823885%3D8%3A0%3A42470%2C1489656728455%3D7%3A0%3A40867&dm=michaelkors.com&si=4a89ea9d-7820-4976-9563-95ee3817260a&bcn=%2F%2F36f1f340.mpstat.us%2F&ld=1489657191519&r=https%3A%2F%2Fwww.michaelkors.com%2Fribbed-lace-up-dress%2F_%2FR-US_MS78WSN66K&ul=1489656732203&hd=1489656732399\"; tp=11143; s_ppv=Home%2520%253E%2520SHOES%2520%253E%2520VIEW%2520ALL%2520SHOES%2C100%2C10%2C11158");
//		headers.put("User-Agent", "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko) Ubuntu Chromium/56.0.2924.76 Chrome/56.0.2924.76 Safari/537.36");
		String content = StringUtils.EMPTY;
		String proxyRegionId = context.getUrl().getTask().getProxyRegionId();
		if (StringUtils.isBlank(proxyRegionId)) {
			content = Crawler.create().timeOut(30000).url(context.getCurrentUrl().toString()).header(headers).proxy(false)
					.resultAsString();
		} else {
			Proxy proxy = ProxyCache.getInstance().pickup(proxyRegionId, true);
			String proxyAddress = proxy.getIp();
			int proxyPort = proxy.getPort();
			content = Crawler.create().timeOut(30000).url(context.getCurrentUrl().toString())
					.header(headers).proxy(true).proxyAddress(proxyAddress).proxyPort(proxyPort).resultAsString();
		}
		return content;
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

	public String getPageFlag() {
		return pageFlag;
	}

	public void setPageFlag(String pageFlag) {
		this.pageFlag = pageFlag;
	}
	
	
}