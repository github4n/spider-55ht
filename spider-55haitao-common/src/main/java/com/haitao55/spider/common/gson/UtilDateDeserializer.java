package com.haitao55.spider.common.gson;


import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;

/**
 * 
  * @ClassName: UtilDateDeserializer
  * @Description: utildate的解序列化
  * @author songsong.xu
  * @date 2016年9月20日 下午2:34:22
  *
 */
public class UtilDateDeserializer implements JsonDeserializer<java.util.Date> {

	public java.util.Date deserialize(JsonElement json, Type typeOfT,
			JsonDeserializationContext context) throws JsonParseException {
		return new java.util.Date(json.getAsJsonPrimitive().getAsLong());
	}
}
