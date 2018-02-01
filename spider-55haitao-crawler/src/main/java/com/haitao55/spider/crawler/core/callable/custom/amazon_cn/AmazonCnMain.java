package com.haitao55.spider.crawler.core.callable.custom.amazon_cn;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openqa.selenium.io.IOUtils;

import com.haitao55.spider.common.utils.HttpClientUtil;

/**
 * 
 * 功能：用来测试编码url并发送http请求
 * 
 * @author Arthur.Liu
 * @time 2017年11月17日 下午8:41:09
 * @version 1.0
 */
public class AmazonCnMain {
	public static void main(String... args) throws IOException, InterruptedException {
		String path = "http://114.55.10.105/spider-55haitao-realtime/realtime-crawler/pricing.action";

		List<String> urls = new ArrayList<String>();
		// urls.add(
		// "https://www.amazon.cn/Crocs-%E5%8D%A1%E9%AA%86%E9%A9%B0%E8%BF%90%E5%8A%A8%E9%9E%8B%E5%9F%8E%E5%B8%82%E8%8E%B1%E6%81%A9%E5%8D%A1%E5%9B%BE%E6%A1%88%E4%B8%80%E8%84%9A%E8%B9%AC%E5%A5%B3%E9%9E%8B204623-Floral-Cashmere-Rose-8-B-US/dp/B01MRYF2P3/ref=sr_1_3?s=amazon-global-store&ie=UTF8&qid=1510976213&sr=1-3&dpID=41-VpX8ABWL&preST=_SX395_QL70_&dpSrc=srch&th=1");
		// urls.add(
		// "https://www.amazon.cn/adrianna-papell-%E5%A5%B3%E5%A3%AB%E9%95%BF%E6%AC%BE%E4%B8%B2%E7%8F%A0%E8%BF%9E%E8%A1%A3%E8%A3%99%E7%A2%8E-SLV-Antique-Bronze-12/dp/B0735NCV9Y/ref=sr_1_1?s=apparel&ie=UTF8&qid=1510990682&sr=1-1&nodeID=91622071&psd=1&dpID=41DnX%252BOg3KL&preST=_SX342_QL70_&dpSrc=srch&th=1&psc=1");
		// urls.add(
		// "https://www.amazon.cn/Long-Sleeve-Dresses-for-Women-Casual-Loose-T-Shirt-Dress-Tunics-%E9%BB%91%E8%89%B2-2-Small/dp/B01N3605JT/ref=sr_1_4?s=apparel&ie=UTF8&qid=1510990682&sr=1-4&nodeID=91622071&psd=1&dpID=41Lw5EN6f1L&preST=_SY445_QL70_&dpSrc=srch&th=1");
		// urls.add(
		// "https://www.amazon.cn/Long-Sleeve-Dresses-for-Women-Casual-Loose-T-Shirt-Dress-Tunics-%E9%BB%91%E8%89%B2-2-Small/dp/B01N3605JT/ref=sr_1_4?s=apparel&ie=UTF8&qid=1510990682&sr=1-4&nodeID=91622071&psd=1&dpID=41Lw5EN6f1L&preST=_SY445_QL70_&dpSrc=srch&th=1&psc=1");
		// urls.add(
		// "https://www.amazon.cn/B-toys-%E5%9B%9E%E8%BD%AC%E9%81%A5%E6%8E%A7%E8%BD%A6-%E6%8B%89%E5%8A%9B%E8%BD%A6-1-4%E5%B2%81/dp/B00IWCQPVQ/ref=sr_1_1?s=toys-and-games&srs=1494170071&ie=UTF8&qid=1510993731&sr=1-1&dpID=41FPcxjvv6L&preST=_SY300_QL70_&dpSrc=srch&th=1");
		// urls.add(
		// "https://www.amazon.cn/%E8%BF%AA%E5%A3%AB%E5%B0%BC%E6%B1%BD%E8%BD%A6%E6%80%BB%E5%8A%A8%E5%91%98%E8%BF%AA%E5%A3%AB%E5%B0%BC-%E7%9A%AE%E5%85%8B%E6%96%AF%E6%B1%BD%E8%BD%A6%E6%80%BB%E5%8A%A8%E5%91%98%E7%89%B9%E6%8A%80%E7%8E%A9%E5%85%B7%E5%A5%97%E8%A3%85/dp/B073W3CCQL/ref=sr_1_2?s=toys-and-games&srs=1494170071&ie=UTF8&qid=1510993879&sr=1-2&dpID=51uKrkpw%252BML&preST=_SX300_QL70_&dpSrc=srch");
		// urls.add(
		// "https://www.amazon.cn/ASH-%E5%A5%B3%E5%BC%8F-as-heloise-%E4%B9%90%E7%A6%8F%E9%9E%8B-%E9%BB%91%E8%89%B2-38-M-EU-8-B-US/dp/B071D1C32Z/ref=sr_1_12?s=amazon-global-store&ie=UTF8&qid=1510994000&sr=1-12&th=1");
		// urls.add(
		// "https://www.amazon.cn/ASH-%E5%A5%B3%E5%BC%8F-as-heloise-%E4%B9%90%E7%A6%8F%E9%9E%8B-%E9%BB%91%E8%89%B2-38-M-EU-8-B-US/dp/B071D16GKQ/ref=sr_1_12?s=amazon-global-store&ie=UTF8&qid=1510994000&sr=1-12&th=1&psc=1");
		// urls.add(
		// "https://www.amazon.cn/ASH-%E5%A5%B3%E5%BC%8F-as-heloise-%E4%B9%90%E7%A6%8F%E9%9E%8B-%E9%BB%91%E8%89%B2-38-M-EU-8-B-US/dp/B071D1C32Z/ref=sr_1_12?s=amazon-global-store&ie=UTF8&qid=1510994000&sr=1-12&th=1&psc=1");
		// urls.add(
		// "https://www.amazon.cn/OLAY-%E7%8E%89%E5%85%B0%E6%B2%B9-%E8%B6%85%E6%B6%A6%E8%BA%AB%E4%BD%93%E4%B9%B3%E6%B6%B220-2%E7%9B%8E%E5%8F%B8/dp/B001G7PLZ0/ref=sr_1_1?s=amazon-global-store&ie=UTF8&qid=1511002385&sr=1-1&dpID=41ZNRpKqOhL&preST=_SY300_QL70_&dpSrc=srch&th=1");
		// urls.add(
		// "https://www.amazon.cn/Crocs-%E5%8D%A1%E9%AA%86%E9%A9%B0%E8%BF%90%E5%8A%A8%E9%9E%8B%E5%9F%8E%E5%B8%82%E8%8E%B1%E6%81%A9%E5%8D%A1%E5%9B%BE%E6%A1%88%E4%B8%80%E8%84%9A%E8%B9%AC%E5%A5%B3%E9%9E%8B204623-Floral-Cashmere-Rose-8-B-US/dp/B01MRYF2P3/ref=sr_1_3?s=amazon-global-store&ie=UTF8&qid=1510976213&sr=1-3&dpID=41-VpX8ABWL&preST=_SX395_QL70_&dpSrc=srch&th=1");
		// urls.add(
		// "https://www.amazon.cn/adrianna-papell-%E5%A5%B3%E5%A3%AB%E9%95%BF%E6%AC%BE%E4%B8%B2%E7%8F%A0%E8%BF%9E%E8%A1%A3%E8%A3%99%E7%A2%8E-SLV-Antique-Bronze-12/dp/B0735NCV9Y/ref=sr_1_1?s=apparel&ie=UTF8&qid=1510990682&sr=1-1&nodeID=91622071&psd=1&dpID=41DnX%252BOg3KL&preST=_SX342_QL70_&dpSrc=srch&th=1&psc=1");
		// urls.add(
		// "https://www.amazon.cn/Long-Sleeve-Dresses-for-Women-Casual-Loose-T-Shirt-Dress-Tunics-%E9%BB%91%E8%89%B2-2-Small/dp/B01N3605JT/ref=sr_1_4?s=apparel&ie=UTF8&qid=1510990682&sr=1-4&nodeID=91622071&psd=1&dpID=41Lw5EN6f1L&preST=_SY445_QL70_&dpSrc=srch&th=1");
		// urls.add(
		// "https://www.amazon.cn/Long-Sleeve-Dresses-for-Women-Casual-Loose-T-Shirt-Dress-Tunics-%E9%BB%91%E8%89%B2-2-Small/dp/B01N3605JT/ref=sr_1_4?s=apparel&ie=UTF8&qid=1510990682&sr=1-4&nodeID=91622071&psd=1&dpID=41Lw5EN6f1L&preST=_SY445_QL70_&dpSrc=srch&th=1&psc=1");
		// urls.add(
		// "https://www.amazon.cn/B-toys-%E5%9B%9E%E8%BD%AC%E9%81%A5%E6%8E%A7%E8%BD%A6-%E6%8B%89%E5%8A%9B%E8%BD%A6-1-4%E5%B2%81/dp/B00IWCQPVQ/ref=sr_1_1?s=toys-and-games&srs=1494170071&ie=UTF8&qid=1510993731&sr=1-1&dpID=41FPcxjvv6L&preST=_SY300_QL70_&dpSrc=srch&th=1");
		// urls.add(
		// "https://www.amazon.cn/%E8%BF%AA%E5%A3%AB%E5%B0%BC%E6%B1%BD%E8%BD%A6%E6%80%BB%E5%8A%A8%E5%91%98%E8%BF%AA%E5%A3%AB%E5%B0%BC-%E7%9A%AE%E5%85%8B%E6%96%AF%E6%B1%BD%E8%BD%A6%E6%80%BB%E5%8A%A8%E5%91%98%E7%89%B9%E6%8A%80%E7%8E%A9%E5%85%B7%E5%A5%97%E8%A3%85/dp/B073W3CCQL/ref=sr_1_2?s=toys-and-games&srs=1494170071&ie=UTF8&qid=1510993879&sr=1-2&dpID=51uKrkpw%252BML&preST=_SX300_QL70_&dpSrc=srch");
		// urls.add(
		// "https://www.amazon.cn/ASH-%E5%A5%B3%E5%BC%8F-as-heloise-%E4%B9%90%E7%A6%8F%E9%9E%8B-%E9%BB%91%E8%89%B2-38-M-EU-8-B-US/dp/B071D1C32Z/ref=sr_1_12?s=amazon-global-store&ie=UTF8&qid=1510994000&sr=1-12&th=1");
		// urls.add(
		// "https://www.amazon.cn/ASH-%E5%A5%B3%E5%BC%8F-as-heloise-%E4%B9%90%E7%A6%8F%E9%9E%8B-%E9%BB%91%E8%89%B2-38-M-EU-8-B-US/dp/B071D16GKQ/ref=sr_1_12?s=amazon-global-store&ie=UTF8&qid=1510994000&sr=1-12&th=1&psc=1");
		// urls.add(
		// "https://www.amazon.cn/ASH-%E5%A5%B3%E5%BC%8F-as-heloise-%E4%B9%90%E7%A6%8F%E9%9E%8B-%E9%BB%91%E8%89%B2-38-M-EU-8-B-US/dp/B071D1C32Z/ref=sr_1_12?s=amazon-global-store&ie=UTF8&qid=1510994000&sr=1-12&th=1&psc=1");
		// urls.add(
		// "https://www.amazon.cn/OLAY-%E7%8E%89%E5%85%B0%E6%B2%B9-%E8%B6%85%E6%B6%A6%E8%BA%AB%E4%BD%93%E4%B9%B3%E6%B6%B220-2%E7%9B%8E%E5%8F%B8/dp/B001G7PLZ0/ref=sr_1_1?s=amazon-global-store&ie=UTF8&qid=1511002385&sr=1-1&dpID=41ZNRpKqOhL&preST=_SY300_QL70_&dpSrc=srch&th=1");

		urls.add("https://www.amazon.cn/gp/product/B0053N389E?ref_=plp_web_a_A3SFYC2CNO35QB_pc_1&me=A1AJ19PSB66TGU");
		urls.add("https://www.amazon.cn/gp/product/B0722S1K8D/ref=br_prlt_grdDy_pdt-2?pf_rd_m=A1AJ19PSB66TGU&pf_rd_s=merchandised-search-4&pf_rd_r=4Z8MRHTPJSVH864D812S&pf_rd_r=4Z8MRHTPJSVH864D812S&pf_rd_t=101&pf_rd_p=b3690265-110f-4c70-ac9e-b2776d1620f9&pf_rd_p=b3690265-110f-4c70-ac9e-b2776d1620f9&pf_rd_i=1922716071");
		urls.add("https://www.amazon.cn/gp/product/B0728FR71R/ref=pd_sbs_194_3?ie=UTF8&psc=1&refRID=R9WVXZCBJY7HYS1M16N6");
		urls.add("https://www.amazon.cn/gp/product/B0777PK1N8/ref=pd_sim_194_82?ie=UTF8&refRID=P1731PVCCQDBYW66WAJF");
		urls.add("https://www.amazon.cn/gp/product/B074JF12PY/ref=pd_sim_194_4?ie=UTF8&refRID=MS8T4QC2ASE30SFBQCJR");
		urls.add("https://www.amazon.cn/gp/product/B00H90KAD6/ref=pd_sim_194_2?ie=UTF8&refRID=P1731PVCCQDBYW66WAJF&th=1");
		urls.add("https://www.amazon.cn/gp/product/B074J9QMSY/ref=pd_sim_194_8?ie=UTF8&refRID=77EVCJSZ6Y3T7X0PFSB4");
		urls.add("https://www.amazon.cn/gp/product/B01I7RLOOS/ref=cn_ags_s9_asin?pf_rd_p=33e63d50-addd-4d44-a917-c9479c457e1a&pf_rd_s=merchandised-search-3&pf_rd_t=101&pf_rd_i=1403206071&pf_rd_m=A1AJ19PSB66TGU&pf_rd_r=ZXWT0MM4Q7JF6T0MZ6EF&ref=cn_ags_s9_asin_1403206071_merchandised-search-3&th=1");

		BufferedWriter writer = createTestResultWriter();

		for (String url : urls) {
			Map<String, String> params = new HashMap<String, String>();
			params.put("url", url);

			long start = System.currentTimeMillis();
			String jsonResult = HttpClientUtil.post(path, params);
			long end = System.currentTimeMillis();
			long interval = (end - start) / 1000;

			writer.newLine();
			writer.write("spent time(s): " + interval);
			writer.newLine();
			writer.write(url);
			writer.newLine();
			writer.write(jsonResult);
			writer.newLine();

			Thread.sleep(3 * 1000);
		}

		writer.flush();
		IOUtils.closeQuietly(writer);
	}

	private static BufferedWriter createTestResultWriter() throws IOException {
		File file = new File("/home/arthur/temp/amazon_cn_test/test_result.data.1122.0952");
		if (file.exists()) {
			file.delete();
		}
		file.createNewFile();

		BufferedWriter br = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file, true)));

		return br;
	}
}