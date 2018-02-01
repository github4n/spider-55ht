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
<body onload="javascript:highlightLeftMenuBarMd5Encode();">
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
                        	<span class="current">编码工具</span>
                         </div>
                	</div>
                	<div id="div">
           			</div>
  			  <div class="table-wrap">
               	  <div class="table-wrap-box">
               	 	<h2>编码工具</h2>
                    <form action="${pageContext.request.contextPath}/tools/md5Encode.action" name="md5EncodeForm" id="md5EncodeForm"  method="post">
                          <div class="form-model">
                                <ul id="ul-outter-md5Encode" style="height: 101px;">
                                   <li>
                                	  <label><span style="color: red">*</span>原始字串 :</label>
                                  	  <input type="text" name="sourceString" id="sourceString" value="${sourceString}" class="input" style="width: 500px;" />
    								  <span id="div_sourceString_err_info" class="name-pop"></span>  
                                   </li>
                                   <li>
	                                   <div class="submit-box" style="padding-left: 292px;">
		                            	  <input name="button" id="button" type="button" class="defult-btn" value="编码" onclick="javascript:md5Encode();"/>
		                           	   </div>
	                           	   </li>
                                   <li>
                                   	  <label><span style="color: red">*</span>编码结果 :</label>
                                   	  <div id="div-md5-encode-result" style="width: 800px; word-break: break-word; margin-left: 46px; min-height: 10px; overflow: auto;">${md5EncodeResult}</div>
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