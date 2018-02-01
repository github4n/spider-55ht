package com.haitao55.spider.image.server;

import java.io.InputStream;

import org.junit.Test;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import junit.framework.TestCase;

public class TestImageAction extends TestCase{
    
    
    @Test
    public void testGet() throws UnirestException{
        HttpResponse<InputStream> jsonResponse = Unirest.post("http://104.154.77.103:8080/spider-55haitao-image-server/img/get.action")
              .field("url", "").asBinary();
    }
    
}
