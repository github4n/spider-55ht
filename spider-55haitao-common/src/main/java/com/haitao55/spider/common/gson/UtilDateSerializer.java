package com.haitao55.spider.common.gson;


import java.lang.reflect.Type;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * 
  * @ClassName: UtilDateSerializer
  * @Description: utildate的序列化
  * @author songsong.xu
  * @date 2016年9月20日 下午2:34:38
  *
 */
public class UtilDateSerializer implements JsonSerializer<java.util.Date> {

	public JsonElement serialize(java.util.Date src, Type typeOfSrc,
			JsonSerializationContext context) {
		return new JsonPrimitive(src.getTime());
	}
}
