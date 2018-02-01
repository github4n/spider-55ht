package com.haitao55.spider.crawling.service;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLEncoder;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import junit.framework.TestCase;
/**
  * @ClassName: TestUrlCheck
  * @Description: TODO
  * @author songsong.xu
  * @date 2017年4月10日 下午4:07:27
  *
 */
public class TestUrlCheck extends TestCase{
    
    @Test
    public void testCheckUrls() throws UnirestException{
        JsonArray arr = new JsonArray();
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(new FileInputStream("/data/linkhaitao-test")));
            String line = null;
            while((line = br.readLine()) != null) {
                if(StringUtils.isBlank(StringUtils.trim(line))){
                    continue;
                }
                String[]  urlArr = StringUtils.split(line, "    ");
                JsonObject obj = new JsonObject();
                String sourceUrl = urlArr[0] + URLEncoder.encode(urlArr[1],"UTF-8");
                obj.addProperty("sourceUrl", sourceUrl);
                obj.addProperty("destUrl", urlArr[2]);
                obj.addProperty("percent", 1.0);
                arr.add(obj);
            }
            System.out.println(arr.toString() ); 
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            if(br != null){
                IOUtils.closeQuietly(br);
            }
        }
       /* HttpResponse<String> jsonResponse = Unirest.post("http://104.154.77.103:8080/spider-55haitao-crawling-service/check/urls.action")
                .header("accept", "application/json").header("content-type", "application/json").body(arr.toString())
                .asString();
        System.out.println(jsonResponse.getBody());*/
    }
    
    @Test
    public void testGetUrls() throws UnirestException{
        JsonObject obj = new JsonObject();
        obj.addProperty("data", 1491805979388l);
        HttpResponse<String> jsonResponse = Unirest.post("http://104.154.77.103:8080/check/gets.action")
                .header("accept", "application/json").header("content-type", "application/json").body(obj.toString())
                .asString();
        System.out.println(jsonResponse.getBody());
    }
    
}
