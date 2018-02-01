package com.haitao55.spider.crawler.core.callable.custom.michaelkors;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.client.ClientProtocolException;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.haitao55.spider.common.http.Crawler;
import com.haitao55.spider.common.http.HttpException;
import com.haitao55.spider.crawler.common.cache.ProxyCache;
import com.haitao55.spider.crawler.core.callable.SelectPages;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelectUrls;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Proxy;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.core.model.UrlType;

/**
 * 
 * 功能：通过css选择器,专门用来提取页面上的urls超级链接
 * 
 * @author Arthur.Liu
 * @time 2016年8月19日 上午11:39:15
 * @version 1.0
 * @see Select
 * @see SelectPages
 */
public class SelectMichaelkorsUrls extends AbstractSelectUrls {
	private static final String DEFAULT_NEW_URLS_TYPE = UrlType.LINK.getValue();
	public int grade;

	// 迭代出来的newurls的类型,链接类型还是商品类型,默认是链接类型
	public String type = DEFAULT_NEW_URLS_TYPE;
	
	@Override
	public void invoke(Context context) throws Exception {
		String content = crawler_package(context);
		JSONObject dataJSONObject = JSONObject.parseObject(content);
		JSONObject resultJSONObject = dataJSONObject.getJSONObject("result");
		if(null!=resultJSONObject && !resultJSONObject.isEmpty()){
			List<String> newUrlValues = new ArrayList<String>();
			JSONArray productJSONArray = resultJSONObject.getJSONArray("productList");
			if(null != productJSONArray && productJSONArray.size()>0){
				for (Object object : productJSONArray) {
					JSONObject jsonObject = (JSONObject)object;
					String productSEOURL = jsonObject.getString("seoURL");
					String colorCode = jsonObject.getString("colorCode");
					String item_url = productSEOURL;
					if(StringUtils.isNotBlank(colorCode)){
						item_url = productSEOURL+"?color="+colorCode;
					}
					newUrlValues.add(item_url);
				}
			}
			
			Set<Url> newUrls = this.buildNewUrls(newUrlValues, context, type, grade);

			// 新迭代出来的urls,放置在Url对象的newUrls中；而不是放置在context中
			context.getUrl().getNewUrls().addAll(newUrls);
		}
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
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getGrade() {
		return grade;
	}
	public void setGrade(int grade) {
		this.grade = grade;
	}
}