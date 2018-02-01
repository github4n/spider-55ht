package com.haitao55.spider.crawler.core.callable.custom.taobao;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import com.haitao55.spider.common.gson.bean.taobao.TBRetBody;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.core.callable.base.AbstractSelect;
import com.haitao55.spider.crawler.core.callable.context.Context;
import com.haitao55.spider.crawler.core.model.Image;

/**
 * 淘宝全球购收录
 * date : 2017-3-8
 * @author denghuan
 *
 */
public class TaoBaoGlobal extends AbstractSelect{

	private static final String domain = "daigou.taobao.com";
	private static final String BASE_URL = "https://daigou.taobao.com";
	
	@Override
	public void invoke(Context context) throws Exception {
		String content = this.getInputString(context);
		TBRetBody tbRetBody = new TBRetBody();
		if (StringUtils.isNotBlank(content)) {
			Document doc = Jsoup.parse(content);
			String itemId = StringUtils.substringBetween(content, "itemId:", ",");
			String productUrl = StringUtils.substringBefore(context.getCurrentUrl(), "&cateName=");
			String category = StringUtils.substringAfter(context.getCurrentUrl(), "cateName=");
			String brand = StringUtils.substringBetween(content, "商品品牌：</label><span>", "</span>");
			String title = doc.select(".detail-info h1.title").text();
			if(StringUtils.isNotBlank(title)){
				title = title.replace("【直购】", "").trim();
			}
			String salePrice = doc.select("span.J-price").text();
			String origPrice = doc.select("span.J-usd-price").text();
			if(StringUtils.isNotBlank(origPrice) && 
					StringUtils.containsIgnoreCase(origPrice, "）")){
				origPrice = StringUtils.substringBetween(origPrice, "（", "）");
			}
			if(StringUtils.isBlank(brand) && 
					StringUtils.isBlank(salePrice) &&
					StringUtils.isBlank(title)){
				return;
			}
			
			String referencePrice = doc.select("span.J-ref-price").text();
			String directLink = doc.select("span.go-origin a").attr("href");
			if(StringUtils.isNotBlank(directLink)){
				directLink = getRedirectUrl(BASE_URL+directLink);//重定向URL
			}
			String weight = doc.select("span.J-weight").text();
			String internationalTransferFee = doc.select("span.J-tran-price").text();
			String overseasLogisticsFee = doc.select("span.J-tran-z-price").text();
			String logisticsSpeed = StringUtils.substringBetween(content, "付款后约 </span><span>", "</span>");
			String productFrom = doc.select(".b2c-info .hd a").text();
			String productMall = doc.select(".b2c-info p").text();
			String desc = doc.select(".product-desc-wrapper").text();
			
			List<Image> imgList = new ArrayList<>();
			Elements  es = doc.select(".nav-list-wrapper ul.nav-list a img");
			if(es != null && es.size() > 0){
				for(Element e : es){
					String image = e.attr("data-ks-imagezoom");
					if(StringUtils.isNotBlank(image)){
						imgList.add(new Image("http:"+image));
					}
				}
			}
			context.getUrl().getImages().put(itemId, imgList);
			
			tbRetBody.setDOCID(SpiderStringUtil.md5Encode(domain+itemId));
			tbRetBody.setProductUrl(productUrl);
			tbRetBody.setTitle(title);
			tbRetBody.setBrand(brand);
			tbRetBody.setSalePrice(salePrice);
			tbRetBody.setOrigPrice(origPrice);
			tbRetBody.setDirectLink(directLink);
			tbRetBody.setReferencePrice(referencePrice);
			tbRetBody.setInternationalTransferFee(internationalTransferFee);
			tbRetBody.setLogisticsSpeed(logisticsSpeed);
			tbRetBody.setOverseasLogisticsFee(overseasLogisticsFee);
			tbRetBody.setProductFrom(productFrom);
			tbRetBody.setWeight(weight);
			tbRetBody.setProductMall(productMall);
			tbRetBody.setDescription(desc);
			tbRetBody.setCategory(category);
			
			setOutput(context, tbRetBody.parseTo());
		}
		
	}
	private String getRedirectUrl(String path) throws Exception {  
        HttpURLConnection conn = (HttpURLConnection) new URL(path)  
                .openConnection();  
        conn.setInstanceFollowRedirects(false);  
        conn.setConnectTimeout(15000);  
        return conn.getHeaderField("Location");  
    }  
}
