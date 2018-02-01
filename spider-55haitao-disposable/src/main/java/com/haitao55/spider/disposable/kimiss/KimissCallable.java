package com.haitao55.spider.disposable.kimiss;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.ExecutionContext;
import org.apache.http.protocol.HttpContext;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.haitao55.spider.common.gson.JsonUtils;
import com.haitao55.spider.common.http.Crawler;
import com.haitao55.spider.common.http.HttpMethod;
import com.haitao55.spider.common.utils.SpiderStringUtil;
import com.haitao55.spider.crawler.utils.HttpUtils;
import com.haitao55.spider.crawler.utils.JsoupUtils;
import com.haitao55.spider.crawler.writer.OutputTools;

import main.java.com.UpYun;

class KimissCallable implements Callable<List<KimissUser>> {
	private static final String UPYUN_DIR = "/prodimage/";
	private static final String upyunAddress="http://spider-prerelease.b0.upaiyun.com";
	private static final String ITEMS_ROOT_PATH = "/output";
	private static final String filePrefixName = "55HT";
	private static final String fileSuffixName = ".SCD";
	private String url;
	private UpYun upyun;

	public KimissCallable(String url,UpYun upyun) {
		this.url = url;
		this.upyun = upyun;
	}

	public List<KimissUser> call() {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}
		// 根据url 爬取，　封装KimissUser
		List<KimissUser> list = new ArrayList<KimissUser>();
		String content = HttpUtils.get(url);
		Document doc = JsoupUtils.parse(content);
		Elements elements = doc.select("ul.buddy.cl li");
		if(CollectionUtils.isNotEmpty(elements)){
			for (Element element : elements) {
				KimissUser kimissUser =new KimissUser();
				String src = element.select("div.avt>a>img").get(0).attr("src");
				src = StringUtils.replacePattern(src, "small", "big");
				//获取图片重定向后到地址
				src=getRedirectInfo(src);
				//排除不符合要求到帐号
				if(StringUtils.contains(src, ".gif") || StringUtils.contains(src, "闺蜜网小助手")){
					continue;
				}
				
				String name = element.select("h4>a").get(0).text();
				kimissUser.setName(name);
				kimissUser.setSrc(src);
				handleImages(kimissUser,upyun);
				
				//写文件形式　将json　数据写出去
				write(kimissUser);
			}
		}
		return list;
	}
	
	private static void handleImages(KimissUser kimissUser,UpYun upyun) {
		createImageRepertoryUrl(kimissUser);// 为了向image对象中设置完整的图片cdn地址链接字符串
		boolean boo = existInRepertory(kimissUser,upyun);
		if (!boo) {// 图片在图片库中不存在,才执行实际的下载和上传过程
			byte[] imageData = null;
			for (int i = 0; i < 3; i++) {
			    imageData = downloadImageData(kimissUser);// 下载图片的逻辑都是一样的
				if (StringUtils.isNotBlank(kimissUser.getSrc()) && ArrayUtils.isNotEmpty(imageData)) {
					break;
				}
			}
			uploadImage(kimissUser,imageData,upyun);
		}
	}
	
	public static void createImageRepertoryUrl(KimissUser kimissUser) {
		String repertoryImageUrl = (new StringBuilder())
				.append(getRepertoryImageAddressPrefix())
				.append(UPYUN_DIR)
				.append(SpiderStringUtil.upYunFileName(kimissUser.getSrc()))
				.append(getRepertoryImageAddressSuffix()).toString();
		kimissUser.setCdn(repertoryImageUrl);
	}
	
	private static String getRepertoryImageAddressPrefix() {
		return upyunAddress;
	}
	private static String getRepertoryImageAddressSuffix() {
		return ".jpg";
	}
	
	public static boolean existInRepertory(KimissUser kimissUser,UpYun upyun) {
		String path=StringUtils.substring(kimissUser.getCdn(), StringUtils.indexOf(kimissUser.getCdn(), UPYUN_DIR));
		Map<String, String> fileInfo = upyun.getFileInfo(path);
		if(null==fileInfo||fileInfo.isEmpty()){
			return false;//不存在
		}
		return true;//存在
	}
	
	private static byte[] downloadImageData(KimissUser kimissUser) {
		try {
			byte[] imageData = null;

			imageData = Crawler.create().retry(3).timeOut(10000).url(kimissUser.getSrc())
					.method(HttpMethod.GET.getValue()).resultAsBytes();
			
			return imageData;
		} catch (Exception e) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
		}
		return null;
	}
	
	public static void uploadImage(KimissUser kimissUser, byte[] imageData, UpYun upyun) {
		upyun.setTimeout(30);
		upyun.setApiDomain(UpYun.ED_AUTO);
		String path=StringUtils.substring(kimissUser.getCdn(), StringUtils.indexOf(kimissUser.getCdn(), UPYUN_DIR));
		boolean  result = false;
		for(int i =0; i < 5; i++){
			try{
				result = upyun.writeFile(path, imageData);
			}catch(Throwable e){
				e.printStackTrace();
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
			}
			if(result){
				break;
			}
		}

	}
	
	
	/**
	 * 
	 */
	public static  void write(KimissUser kimissUser)  {
		try{
			OutputTools tools = OutputTools.getInstance();
			String website = tools.getWebsite("http://home.kimiss.com/");
			BufferedWriter bw =  tools.getBufferWriter(website);
			if(bw == null){
				String localIP = InetAddress.getLocalHost().getHostAddress().toString();
				String filePath = getFilePath(website, localIP, "kimiss",ITEMS_ROOT_PATH,fileSuffixName);
				bw = OutputTools.getInstance().getBw(filePath, website);
			}
			synchronized (bw) {
				bw.write(JsonUtils.bean2json(kimissUser)+"\n");
				bw.flush();
			}
		}catch(Throwable e){
		}
	}
	
	private static String getFilePath(String website,String ip,String taskId,String path,String suffixName) throws IOException {
		String rootDir = getOutPutDir(website,path);
		String lastIp = StringUtils.substringAfterLast(ip, ".");
		String itemDateRand = (new StringBuilder((new SimpleDateFormat("yyyyMMddHHmmss")).format(new Date())))
				.append("-").append(String.valueOf(Math.round(Math.random() * 10000))).append("-").toString();
		String fileName = filePrefixName+"-"+taskId + "-" + itemDateRand + lastIp + suffixName;
		File itemFile = new File(rootDir + "/" + fileName);
		if (!itemFile.exists()) {
			itemFile.createNewFile();
		}
		return itemFile.getAbsolutePath();
	}

	private static String getOutPutDir(String website,String path) {
		URL url = KimissCallable.class.getProtectionDomain().getCodeSource().getLocation();
		File jarFile = new File(url.getFile());
		String parentPath = jarFile.getParent();
		String itemsRootPath = parentPath + path;
		File itemsRootDir = new File(itemsRootPath);
		if (!itemsRootDir.exists()) {// 根路径
			itemsRootDir.mkdir();
		}
		File websiteDir = new File(itemsRootPath+"/"+website);
		if (!websiteDir.exists()) {// 根路径
			websiteDir.mkdir();
		}
		return websiteDir.getAbsolutePath();
	}
	
	
	/**
	 * 获取跳转后的图片地址
	 * @param url
	 */
	public static String getRedirectInfo(String url) {

		HttpClient httpClient = new DefaultHttpClient();

		HttpContext httpContext = new BasicHttpContext();

		HttpGet httpGet = new HttpGet(url);

		try {

			// 将HttpContext对象作为参数传给execute()方法,则HttpClient会把请求响应交互过程中的状态信息存储在HttpContext中

			HttpResponse response = httpClient.execute(httpGet, httpContext);

			// 获取重定向之后的主机地址信息,即"http://127.0.0.1:8088"

			HttpHost targetHost = (HttpHost) httpContext.getAttribute(ExecutionContext.HTTP_TARGET_HOST);

			// 获取实际的请求对象的URI,即重定向之后的"/blog/admin/login.jsp"

			HttpUriRequest realRequest = (HttpUriRequest) httpContext.getAttribute(ExecutionContext.HTTP_REQUEST);
			
			return targetHost.toString().concat(realRequest.getURI().toString());

//			System.out.println("主机地址:" + targetHost);
//
//			System.out.println("URI信息:" + realRequest.getURI());

//			HttpEntity entity = response.getEntity();
//			if (null != entity) {
//
//				System.out.println("响应内容:" + EntityUtils.toString(entity, ContentType.getOrDefault(entity).getCharset()));
//
//				EntityUtils.consume(entity);
//
//			}

		} catch (Exception e) {

			e.printStackTrace();

		} finally {

			httpClient.getConnectionManager().shutdown();

		}
		return StringUtils.EMPTY;

	}
}
