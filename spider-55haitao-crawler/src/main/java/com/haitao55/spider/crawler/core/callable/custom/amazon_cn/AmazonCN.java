package com.haitao55.spider.crawler.core.callable.custom.amazon_cn;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.haitao55.spider.common.gson.bean.RetBody;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.callable.custom.amazon.api.AWSKey;
import com.haitao55.spider.crawler.core.model.Url;
import com.haitao55.spider.crawler.utils.Constants;

/**
 * 
 * 功能：现在将这个‘通过API抓取数据’的callable类作为实际使用的callable类
 * 
 * @time 2017年11月20日 下午3:11:13
 * @version 1.0
 */
public class AmazonCN extends AbstractSelect {
	private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
	private static final String defaultKeyId = "AKIAIYYR7H4HBSG56EBA";
	private static final String defaultSecretKey = "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";
	private static final String defaultAssociateTag = "55haitao";

	@Override
	public void invoke(Context context) throws Exception {
		RetBody retBody = null;
		for (int i = 0; i < 3; i++) {//  重试3次
			try {
				AWSKey key = new AWSKey(defaultKeyId, defaultSecretKey, defaultAssociateTag);
				retBody = AmazonCNUtils.getRetBody(context, key, 10000);
				if (retBody != null) {
					break;
				}
			} catch (Throwable e) {
				e.printStackTrace();
				TimeUnit.SECONDS.sleep(2);
			}
		}

		setOutput(context, retBody);
		logger.info("got result of amazon.cn by api, retBody: {}", retBody == null ? null : retBody.parseTo());
		System.out.println(retBody.parseTo());
	}

	public static void main(String[] args) throws Exception {
		String url = "https://www.amazon.cn/dp/B01K0Y1H8O/ref?th=1&psc=1";
		url = "https://www.amazon.cn/SAM-Edelman-%E5%A5%B3%E5%BC%8F-ludlow-%E6%B3%B5-Cranberry-Multi-Metallic-Jacquard-9-5-B-US/dp/B01NBMN9PD/ref=sr_1_4?s=amazon-global-store&ie=UTF8&qid=1511168641&sr=1-4&dpID=41FVo1XYfgL&preST=_SX395_QL70_&dpSrc=srch";
		url = "https://www.amazon.cn/OLAY-%E7%8E%89%E5%85%B0%E6%B2%B9-%E8%B6%85%E6%B6%A6%E8%BA%AB%E4%BD%93%E4%B9%B3%E6%B6%B220-2%E7%9B%8E%E5%8F%B8/dp/B001G7PLZ0/ref=sr_1_1?s=amazon-global-store&ie=UTF8&qid=1511002385&sr=1-1&dpID=41ZNRpKqOhL&preST=_SY300_QL70_&dpSrc=srch&th=1";
		// url = "https://www.amazon.cn/Withings-Pulse-O2-%E8%BF%90%E5%8A%A8%E6%99%BA%E8%83%BD%E6%89%8B%E7%8E%AF-%E7%9D%A1%E7%9C%A0%E8%BF%BD%E8%B8%AA-%E5%BF%83%E7%8E%87%E8%AE%A1-%E8%AE%A1%E6%AD%A5%E5%99%A8-%E9%BB%91%E8%89%B2/dp/B00LF4BCFW/ref=lp_1323500071_1_1?s=music-players&ie=UTF8&qid=1511183536&sr=1-1";
		// url = "https://www.amazon.cn/Aerosoles-%E5%A5%B3%E5%BC%8F%E9%80%82%E7%94%A8%E4%BA%8E-SHORE-Black-Snake-5-5-B-US/dp/B01HC6QTCC/ref=sr_1_12?s=amazon-global-store&ie=UTF8&qid=1511230647&sr=1-12";
		
		// url = "https://www.amazon.cn/gp/product/B016R92B3Q?pf_rd_p=c6d17c3b-92ef-4aa7-920b-19155bc9b830&pf_rd_s=merchandised-search-7&pf_rd_t=101&pf_rd_i=1403206071&pf_rd_m=A1AJ19PSB66TGU&pf_rd_r=JW8TZWRMF065Y609NCQK&ref=cn_ags_floor_hotasin_1403206071_mobile-2&th=1";
		// url = "https://www.amazon.cn/gp/product/B000NY1VLU?pf_rd_p=c6d17c3b-92ef-4aa7-920b-19155bc9b830&pf_rd_s=merchandised-search-7&pf_rd_t=101&pf_rd_i=1403206071&pf_rd_m=A1AJ19PSB66TGU&pf_rd_r=YA6T3T5WX654FRRAJK2E&ref=cn_ags_floor_hotasin_1403206071_mobile-1";
		url = "https://www.amazon.cn/TAMARIS-%E5%A5%B3-%E4%B8%AD%E8%B7%9F%E9%9E%8B-1-1-22320-28-227-%E7%81%B0%E8%89%B2-39/dp/B01L7E33R4/ref=sr_1_23?s=amazon-global-store&ie=UTF8&qid=1511259348&sr=1-23&th=1";
		url = "https://www.amazon.cn/TAMARIS-%E5%A5%B3-%E4%B8%AD%E8%B7%9F%E9%9E%8B-1-1-22320-28-227-%E7%81%B0%E8%89%B2-39/dp/B01L7E311W/ref=sr_1_23?s=amazon-global-store&ie=UTF8&qid=1511259348&sr=1-23&th=1&psc=1";
		url = "https://www.amazon.cn/adidas-%E5%A9%B4%E5%84%BF%E7%94%B7%E5%AD%A9%E6%8B%89%E9%93%BE%E8%BF%9E%E5%B8%BD%E8%A1%AB%E5%92%8C%E8%A3%A4%E5%AD%90%E5%A5%97%E8%A3%85-Electric-Orange-9M/dp/B071NNY7XR/ref=sr_1_2?s=apparel&ie=UTF8&qid=1511260356&sr=1-2&nodeID=2153777051&psd=1&th=1&psc=1";
		url = "https://www.amazon.cn/GENTEN-%E6%B0%B4%E6%A1%B6%E5%9E%8B%E5%8C%85-%E3%82%AB%E3%83%86%E3%83%89%E3%83%A9%E3%83%AB%E3%82%A6%E3%82%A3%E3%83%B3%E3%83%89%E3%82%A6-41732-%E3%83%8C%E3%83%A1/dp/B06X41W81G/ref=sr_1_14?s=amazon-global-store&ie=UTF8&qid=1511260719&sr=1-14";
		url = "https://www.amazon.cn/%E7%BA%AA%E5%BF%B5%E5%93%81-THE-%E6%A0%87%E7%AD%BE%E5%A5%B3%E5%BC%8F-%E8%BF%B7%E4%BD%A0%E8%A3%99-%E9%BB%91%E8%89%B2-X-Large/dp/B06ZZ5426N/ref=sr_1_4?s=apparel&ie=UTF8&qid=1511260910&sr=1-4&nodeID=91622071&psd=1&dpID=41f4TQRhjXL&preST=_SX342_QL70_&dpSrc=srch";
		url = "https://www.amazon.cn/%E7%BA%AA%E5%BF%B5%E5%93%81-THE-%E6%A0%87%E7%AD%BE%E5%A5%B3%E5%BC%8F-%E8%BF%B7%E4%BD%A0%E8%A3%99-%E9%BB%91%E8%89%B2-X-Large/dp/B071DHLY78/ref=sr_1_4?s=apparel&ie=UTF8&qid=1511260910&sr=1-4&nodeID=91622071&psd=1&dpID=41f4TQRhjXL&preST=_SX342_QL70_&dpSrc=srch&th=1&psc=1";
		url = "https://www.amazon.cn/FRENCH-connection-%E5%A5%B3%E5%BC%8F-olitski-Ottoman-%E8%BF%9E%E8%A1%A3%E8%A3%99-Woodland-Green-6/dp/B01N9SCSQ5/ref=sr_1_8?s=apparel&ie=UTF8&qid=1511260910&sr=1-8&nodeID=91622071&psd=1&th=1&psc=1";
		
		url = "https://www.amazon.cn/gp/product/B0053N389E?ref_=plp_web_a_A3SFYC2CNO35QB_pc_1&me=A1AJ19PSB66TGU";
		// url = "https://www.amazon.cn/gp/product/B0722S1K8D/ref=br_prlt_grdDy_pdt-2?pf_rd_m=A1AJ19PSB66TGU&pf_rd_s=merchandised-search-4&pf_rd_r=4Z8MRHTPJSVH864D812S&pf_rd_r=4Z8MRHTPJSVH864D812S&pf_rd_t=101&pf_rd_p=b3690265-110f-4c70-ac9e-b2776d1620f9&pf_rd_p=b3690265-110f-4c70-ac9e-b2776d1620f9&pf_rd_i=1922716071";
		url = "https://www.amazon.cn/gp/product/B0728FR71R/ref=pd_sbs_194_3?ie=UTF8&psc=1&refRID=R9WVXZCBJY7HYS1M16N6";
		url = "https://www.amazon.cn/gp/product/B0777PK1N8/ref=pd_sim_194_82?ie=UTF8&refRID=P1731PVCCQDBYW66WAJF";
		url = "https://www.amazon.cn/gp/product/B00JFR1GTS";
		url = "https://www.amazon.cn/dp/B01MR1KZEJ/ref=twister_dp_update?_encoding=UTF8&psc=1";

		Context context = new Context();
		context.setUrl(new Url(url));
		context.setRunInRealTime(true);

		AmazonCN amazonCN = new AmazonCN();
		amazonCN.invoke(context);
	}
}