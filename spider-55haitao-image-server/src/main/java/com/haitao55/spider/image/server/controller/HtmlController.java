package com.haitao55.spider.image.server.controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.CapabilityType;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.haitao55.spider.common.gson.JsonUtils;
import com.haitao55.spider.common.gson.bean.Brand;
import com.haitao55.spider.common.gson.bean.LSelectionList;
import com.haitao55.spider.common.gson.bean.LStyleList;
import com.haitao55.spider.common.gson.bean.Picture;
import com.haitao55.spider.common.gson.bean.Price;
import com.haitao55.spider.common.gson.bean.ProdUrl;
import com.haitao55.spider.common.gson.bean.RetBody;
import com.haitao55.spider.common.gson.bean.Selection;
import com.haitao55.spider.common.gson.bean.Site;
import com.haitao55.spider.common.gson.bean.Sku;
import com.haitao55.spider.common.gson.bean.Stock;
import com.haitao55.spider.common.gson.bean.Title;
import com.haitao55.spider.common.utils.Currency;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.core.model.Image;
import com.haitao55.spider.crawler.utils.Constants;

@Controller
@RequestMapping("/mk")
public class HtmlController {
    
    
    private static final Logger logger = LoggerFactory.getLogger(Constants.LOGGER_NAME_CRAWLER);
    private static final String domain = "www.michaelkors.com";
    private String MICHAELKORS_API = "https://www.michaelkors.com/server/productinventory?productList=";
    private static final String IMAGE_PRIFER = "https://michaelkors.scene7.com/is/image/";
    private static final String IMAGE_SUFFER = "?wid=558&hei=748";
    private static final ExecutorService service = Executors.newCachedThreadPool();
    private GenericObjectPool pool;
    
    @PostConstruct
    public void init() {
      System.out.println("==================init start==================");
      HaiTaoPoolableObjectFactory factory = new HaiTaoPoolableObjectFactory();  
      pool = new GenericObjectPool(factory);
      pool.setMaxActive(6); // 能从池中借出的对象的最大数目  
      pool.setMaxIdle(6); // 池中可以空闲对象的最大数目  
      pool.setMaxWait(-1); // 对象池空时调用borrowObject方法，最多等待多少毫秒  
      pool.setTimeBetweenEvictionRunsMillis(600000);// 间隔每过多少毫秒进行一次后台对象清理的行动  
      pool.setNumTestsPerEvictionRun(-1);// －1表示清理时检查所有线程  
      pool.setMinEvictableIdleTimeMillis(600000);// 设定在进行后台对象清理时，休眠时间超过了3000毫秒的对象为过期
      pool.setTestOnBorrow(false);
      System.out.println("==================init end==================");
    }
    
    
    /*public ChromeDriver getDriver() {
        System.setProperty("webdriver.chrome.driver","C:\\55haitao\\chromedriver.exe");
        //System.setProperty("webdriver.chrome.driver","/usr/local/bin/chromedriver");
        ChromeOptions options = new ChromeOptions();
        options.addArguments("no-sandbox");
        options.addArguments("start-maximized");
        options.addArguments("--headless", "--disable-gpu", "--window-size=1920,1200","--ignore-certificate-errors");
        options.setBinary("C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe");
        //options.setBinary("/opt/google/chrome/chrome");
 
        //options.addExtensions(new File("/home/jerome/.config/google-chrome/Default/Extensions/padekgcemlokbadohgkifijomclgjgif/2.3.21_0.crx"));
        //options.addExtensions(new File(System.getProperty("user.dir")+"/config/block-image_1_1.crx"));
        DesiredCapabilities capabilities = new DesiredCapabilities();
        capabilities.setCapability(ChromeOptions.CAPABILITY, options);
        capabilities.setCapability(CapabilityType.ACCEPT_SSL_CERTS, true);
        ChromeDriver dr = new ChromeDriver(capabilities);
        dr.manage().window().maximize();
        return dr;
    }*/
    
    
    
    @RequestMapping(path = "/saks", method = RequestMethod.GET,produces = MediaType.TEXT_HTML_VALUE)
    public @ResponseBody String  saks(String url) {
        logger.info("saks params url {}",url ); 
        long start = System.currentTimeMillis();
        ChromeDriver dr = null;
        try{
            dr = (ChromeDriver) pool.borrowObject();
            //RetBody rebody = Saksfifthavenue.invoke(url, dr);
            dr.get(url);
            String content = dr.getPageSource();
            long end = System.currentTimeMillis();
            logger.info("saks consume time : {}",(end-start));
            //String result = JsonUtils.bean2json(rebody);
            //logger.info("saks result : {}", result); 
            return content;
        }catch(Exception e){
            e.printStackTrace();
        }finally {
          try {
            pool.returnObject(dr);
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
        return null;
    }
    
    
    @RequestMapping(path = "/get", method = RequestMethod.GET,produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public @ResponseBody RetBody  get(String url) {
        RetBody retBody = new RetBody();
        long start = System.currentTimeMillis();
        logger.info("param url {}" , url);
        if(StringUtils.isBlank(url) || !StringUtils.contains(url, "michaelkors.com")){
            return retBody;
        }
        try {
            String itemId = StringUtils.EMPTY;
            if(StringUtils.contains(url, "?")){
                itemId = StringUtils.substringBetween(url, "R-", "?");
            } else {
                itemId = StringUtils.substringAfter(url, "R-");
            }
            String productId = itemId;
            Future<String> f = service.submit(new Callable<String>(){
                @Override
                public String call() throws Exception {
                    //productId = productJSONObject.getString("identifier");
                    ChromeDriver dr = null;
                    try{
                        dr = (ChromeDriver) pool.borrowObject();
                        dr.get(MICHAELKORS_API + productId);
                        String content = dr.getPageSource();
                        return content;
                    }catch(Exception e){
                        e.printStackTrace();
                    }finally {
                        pool.returnObject(dr);
                    }
                    return null;
                    
                }
            });
            
            Future<String> res = service.submit(new Callable<String>(){

                    @Override
                    public String call() throws Exception {
                        ChromeDriver dr = null;
                        try{
                            dr = (ChromeDriver) pool.borrowObject();
                            dr.get(url);
                            String content = dr.getPageSource();
                            return content;
                        }catch(Exception e){
                            e.printStackTrace();
                        }finally {
                            pool.returnObject(dr);
                        }
                        return null;
                    }
              });
           String content = res.get();
          //String content = crawler_package(context, url);
          //Document doc = JsoupUtils.parse(content);
          if (StringUtils.isNotBlank(content)) {
            Sku sku = new Sku();

            // productId
            
            // spu stock status
            int spu_stock_status = 0;
            // default color code
            String default_color_code = StringUtils.substringAfter(url, "color=");
            // title
            String title = StringUtils.EMPTY;
            // brand
            String brand = StringUtils.EMPTY;
            // desc
            String desc = StringUtils.EMPTY;
            // gender
            String gender = StringUtils.EMPTY;
            // unit
            String unit = Currency.USD.name();

            String parentCategoryName = StringUtils.EMPTY;
            Map<String, String> hashMap = new HashMap<>();

            // product data
            String data = StringUtils.substringBetween(content, "rawJson\":", ",\"schemaOrg\":");
            //JSONObject parseObject = JSONObject.parseObject(data + "}}");
            //JSONObject pdpJSONObject = parseObject.getJSONObject("pdp");
            if (null != data) {
              JSONObject productJSONObject = JSONObject.parseObject(data);
              //JSONObject productJSONObject = pdpJSONObject.getJSONObject("rawJson");

              // sku
              List<LSelectionList> l_selection_list = new ArrayList<LSelectionList>();
              List<LStyleList> l_style_list = new ArrayList<LStyleList>();
              // style
              Set<String> styleSet = new HashSet<String>();
              // color image jsonobject key:styleNumber_colorcode
              // value:color
              if (null != productJSONObject && !productJSONObject.isEmpty()) {
                //productId = productJSONObject.getString("identifier");
                //dr.get(MICHAELKORS_API + productId);
                String rsSkus = f.get();
                if (StringUtils.isNotBlank(rsSkus)) {
                  String skus = StringUtils.substringBetween(rsSkus, "\"product\":", "}</pre>");
                  if (StringUtils.isNotBlank(skus)) {
                    JSONObject productJsonObj = JSONObject.parseObject(skus);
                    JSONArray jsonArray = productJsonObj.getJSONArray("SKUs");
                    for (int i = 0; i < jsonArray.size(); i++) {
                      JSONObject instockJson = jsonArray.getJSONObject(i);
                      String pId = instockJson.getString("identifier");
                      String stockNumber =
                          StringUtils.substringBetween(instockJson.toString(), "stockLevel\":", ",");
                      if (StringUtils.isNotBlank(pId) && StringUtils.isNotBlank(stockNumber)) {
                        hashMap.put(pId, stockNumber);
                      }
                    }
                  }
                }

                brand = productJSONObject.getString("brand");
                desc = productJSONObject.getString("description");
                parentCategoryName = productJSONObject.getString("parentCategoryName");
                title = productJSONObject.getString("displayName");
                gender = productJSONObject.getString("guideType");
                System.out.println(
                    "test michk : url : "
                        + url
                        + " ,  brand : "
                        + brand
                        + " , parentCategoryName :"
                        + parentCategoryName);
                JSONArray skuJSONArray = productJSONObject.getJSONArray("SKUs");
                if (null != skuJSONArray && skuJSONArray.size() > 0) {
                  for (Object object : skuJSONArray) {
                    JSONObject skuJSONObject = (JSONObject) object;
                    // selectlist
                    LSelectionList lselectlist = new LSelectionList();
                    String skuId = skuJSONObject.getString("identifier");
                    //String styleNumber = skuJSONObject.getString("styleNumber");
                    String color = StringUtils.EMPTY;
                    String color_code = StringUtils.EMPTY;
                    String size = StringUtils.EMPTY;
                    String swicth_img = StringUtils.EMPTY;
                    JSONObject forSizeAnsColorJSONObject =
                        skuJSONObject.getJSONObject("variant_values");
                    if (null != forSizeAnsColorJSONObject && !forSizeAnsColorJSONObject.isEmpty()) {
                      JSONObject sizeJSONObject = forSizeAnsColorJSONObject.getJSONObject("size");
                      JSONObject colorJSONObject = forSizeAnsColorJSONObject.getJSONObject("color");
                      size = sizeJSONObject.getString("name");
                      color = colorJSONObject.getString("name");
                      color_code = colorJSONObject.getString("colorCode");
                      swicth_img = colorJSONObject.getString("swatchImageUrl");
                    }
                    // stock
                    int stock_status = 0;
                    String stockNum = hashMap.get(skuId);
                    if (StringUtils.isNotBlank(stockNum) && Integer.parseInt(stockNum) > 0) {
                      stock_status = 1;
                    }
                    List<Image> imageList = new ArrayList<>();
                    JSONObject mediaJSONObject = skuJSONObject.getJSONObject("media");
                    JSONArray jsonArray = mediaJSONObject.getJSONArray("images");
                    List<Picture> style_images = new ArrayList<>();
                    for (int i = 0; i < jsonArray.size(); i++) {
                      String imageKey = jsonArray.getString(i);
                      if (StringUtils.isNotBlank(imageKey)) {
                        String src = IMAGE_PRIFER + imageKey + IMAGE_SUFFER;
                        imageList.add(new Image(src));
                        style_images.add(new Picture(src,""));
                      }
                    }
                    //context.getUrl().getImages().put(skuId, imageList);
                    // price
                    float sale_price = 0;
                    float orign_price = 0;
                    JSONObject priceJSONObject = skuJSONObject.getJSONObject("prices");
                    if (null != priceJSONObject && !priceJSONObject.isEmpty()) {
                      orign_price = priceJSONObject.getFloatValue("listPrice");
                      sale_price = priceJSONObject.getFloatValue("salePrice");
                      if (orign_price < sale_price) {
                        orign_price = sale_price;
                      }
                    }

                    if (StringUtils.equals(default_color_code, color_code)
                        || StringUtils.isBlank(default_color_code)) {
                      int save = Math.round((1 - sale_price / orign_price) * 100);
                      retBody.setPrice(new Price(orign_price, save, sale_price, unit));
                    }

                    // selections
                    List<Selection> selections = new ArrayList<Selection>();
                    if (StringUtils.isNotBlank(size)) {
                      Selection selection = new Selection();
                      selection.setSelect_id(0);
                      selection.setSelect_name("Size");
                      selection.setSelect_value(size);
                      selections.add(selection);
                    }

                    // lselectlist
                    lselectlist.setGoods_id(skuId);
                    lselectlist.setOrig_price(orign_price);
                    lselectlist.setSale_price(sale_price);
                    lselectlist.setPrice_unit(unit);
                    lselectlist.setStock_status(stock_status);
                    lselectlist.setStyle_id(color);
                    lselectlist.setSelections(selections);

                    // l_selection_list
                    l_selection_list.add(lselectlist);
                    if (!styleSet.contains(color)) {
                      // stylelist
                      LStyleList lStyleList = new LStyleList();
                      if (StringUtils.equals(default_color_code, color_code)
                          || StringUtils.isBlank(default_color_code)) {
                        lStyleList.setDisplay(true);
                      }
                      // stylelist
                      lStyleList.setGood_id(skuId);
                      lStyleList.setStyle_switch_img(swicth_img);
                      lStyleList.setStyle_cate_id(0);
                      lStyleList.setStyle_id(color);
                      lStyleList.setStyle_cate_name("Color");
                      lStyleList.setStyle_name(color);
                      lStyleList.setStyle_images(style_images);
                      l_style_list.add(lStyleList);
                      styleSet.add(color);
                    }
                  }
                }
              }

              sku.setL_selection_list(l_selection_list);
              sku.setL_style_list(l_style_list);

              retBody.setSku(sku);

              // stock
              retBody.setStock(new Stock(spu_stock_status));

              // brand
              retBody.setBrand(new Brand(brand, "", "", ""));

              retBody.setTitle(new Title(title, "", "", ""));

              // full doc info
              String docid = SpiderStringUtil.md5Encode(domain + productId);
              String url_no = SpiderStringUtil.md5Encode(url);
              retBody.setDOCID(docid);
              retBody.setSite(new Site(domain));
              retBody.setProdUrl(new ProdUrl(url, System.currentTimeMillis(), url_no));

              // category breadcrumb
              // category_package(doc, retBody, brand, productId,context);
              List<String> cats = new ArrayList<String>();
              List<String> breads = new ArrayList<String>();

              if (StringUtils.isNotBlank(parentCategoryName)) {
                cats.add(parentCategoryName);
                cats.add(title);
                breads.add(parentCategoryName);
                breads.add(title);
              } else {
                cats.add(title);
                breads.add(title);
              }
              retBody.setCategory(cats);
              retBody.setBreadCrumb(breads);

              // description
              Map<String, Object> featureMap = new HashMap<String, Object>();
              Map<String, Object> descMap = new HashMap<String, Object>();
              featureMap.put("feature-1", desc);
              retBody.setFeatureList(featureMap);
              descMap.put("en", desc);
              retBody.setDescription(descMap);

              // properties
              Map<String, Object> propMap = new HashMap<String, Object>();
              propMap.put("s_gender", gender);
              retBody.setProperties(propMap);
            }
          }
        } catch (Exception e) {
          System.out.println("michaelkors Execption :::::: " + e.getMessage());
        }
        String result = JsonUtils.bean2json(retBody);
        long end = System.currentTimeMillis();
        logger.info("url {} ,consume time {}",url,(end-start));
        logger.info("url {} ,response json {}",url,result);
        return retBody;
    }
    
}
