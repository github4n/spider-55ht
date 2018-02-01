package com.haitao55.spider.common.gson;

import java.lang.reflect.Type;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

/**
 * 
  * @ClassName: SQLDateSerializer
  * @Description: sqlDate的序列化
  * @author songsong.xu
  * @date 2016年9月20日 下午2:33:42
  *
 */
public class SQLDateSerializer implements JsonSerializer<java.sql.Date> {

	public JsonElement serialize(java.sql.Date src, Type typeOfSrc,
			JsonSerializationContext context) {
		return new JsonPrimitive(src.getTime());
	}
}
