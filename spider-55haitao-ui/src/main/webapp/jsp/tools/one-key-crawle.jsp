<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%
	String basePath = request.getScheme()
			+ "://"
			+ request.getServerName() 
			+ ":" + request.getServerPort() + request.getContextPath()
			+ "/";
%>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<base href="<%=basePath%>" />
<link href="<%=basePath%>css/ispider.css" rel="stylesheet" type="text/css" />
<link href="<%=basePath%>css/comm.css" rel="stylesheet" type="text/css" />
<script type="text/javascript" src="<%=basePath%>js/jquery-1.4.4.min.js"></script>
<script type="text/javascript" src="<%=basePath%>js/tools/tools.js"></script>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" /></head>
<body onload="javascript:highlightLeftMenuBarOneKeyCrawle();">
<div class="wrap">
<div class="header">
	<div class="logo"><a href="#"><img src="<%=basePath%>images/logo.png" alt="cms" width="360" height="65" /></a></div>
</div>
<div class="bar">
	<div class="location">
   	位置 > 爬虫工具
   	</div>
</div>
	<div class="main">
 		<div class="main-inner">
  			<div class="inner-box">
            		<div class="nav-sub-bar">
                		<div class="nav-sub">
                        	<span class="current">一键抓取</span>
                         </div>
                	</div>
                	<div id="div">
           			</div>
  			  <div class="table-wrap">
               	  <div class="table-wrap-box">
               	 	<h2>一键抓取</h2>
                    <form action="${pageContext.request.contextPath}/tools/oneKeyCrawle.action" name="oneKeyCrawleForm" id="oneKeyCrawleForm"  method="post">
                          <div class="form-model">
                                <ul id="ul-outter-one-key-crawle" style="height: 101px;">
                                   <li>
                                	  <label><span style="color: red">*</span>目标URL :</label>
                                  	  <input type="text" name="targetUrl" id="targetUrl" value="" class="input" style="width: 500px;" />
    								  <span id="div_targetUrl_err_info" class="name-pop"></span>  
                                   </li>
                                   <li>
                                   	  <label><span style="color: red">*</span>是否上线 :</label>
                                      <input type="radio" name="isSendMessage" value="Y" />是&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;
    								  <input type="radio" name="isSendMessage" value="N" checked="checked" />否
                                   </li>
                                   <li>
	                                   <div class="submit-box" style="padding-left: 292px;">
		                            	  <input name="button" id="button" type="button" class="defult-btn" value="抓取" onclick="javascript:oneKeyCrawle();"/>
		                           	   </div>
	                           	   </li>
                                   <li>
                                   	  <label><span style="color: red">*</span>抓取结果 :</label>
                                   	  <div id="div-one-key-crawle-result" style="width: 800px; word-break: break-word; margin-left: 46px; min-height: 10px; overflow: auto;">${oneKeyResult}</div>
                                   </li>
                                </ul>
                          </div>
                    </form>
               	  </div>
                </div>
                <div class="table-wrap"></div>
        	</div>
		</div>
	</div>
 <jsp:include page="/jsp/leftMenu.jsp"></jsp:include>
<div class="footer"></div>
</div>
</body>
</html>