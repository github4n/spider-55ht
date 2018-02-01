package com.haitao55.spider.common.gson;


import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

/**
 * 
  * @ClassName: SQLDateDeserializer
  * @Description: sqldate的解序列化
  * @author songsong.xu
  * @date 2016年9月20日 下午2:33:22
  *
 */
public class SQLDateDeserializer implements JsonDeserializer<java.sql.Date> {

	public java.sql.Date deserialize(JsonElement json, Type typeOfT,
			JsonDeserializationContext context) throws JsonParseException {
		return new java.sql.Date(json.getAsJsonPrimitive().getAsLong());
	}
}
