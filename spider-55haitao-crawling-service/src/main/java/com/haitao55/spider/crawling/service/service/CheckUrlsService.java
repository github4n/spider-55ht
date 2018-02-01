package com.haitao55.spider.crawling.service.service;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
/**
 * 
  * @ClassName: CheckUrlsService
  * @Description: 校验提转url的相关接口
  * @author songsong.xu
  * @date 2017年3月30日 下午6:13:37
  *
 */
public interface CheckUrlsService {
	/**
	  * @Title: check
	  * @Description: 添加url到队列中
	  * @param @param param
	  * @param @return    设定文件
	  * @return JsonObject    返回类型
	  * @throws
	 */
	public JsonObject checks(JsonArray param, String rootDir);
	/**
	  * @Title: gets
	  * @Description: 获取检验后的urls
	  * @param @param param
	  * @param @param rootDir
	  * @param @return    设定文件
	  * @return JsonArray    返回类型
	  * @throws
	 */
	public JsonArray gets(JsonObject param, String rootDir);

}
