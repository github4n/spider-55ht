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
<script type="text/javascript" src="<%=basePath%>js/items/query-item.js"></script>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" /></head>
<body onload="javascript:highlightLeftMenuBarItemsMgr();">
<div class="wrap">
<div class="header">
	<div class="logo"><a href="#"><img src="<%=basePath%>images/logo.png" alt="cms" width="360" height="65" /></a></div>
</div>
<div class="bar">
	<div class="location">
   	位置 > 商品管理
   	</div>
</div>
	<div class="main">
 		<div class="main-inner">
  			<div class="inner-box">
            		<div class="nav-sub-bar">
                		<div class="nav-sub">
                        	<span class="current">商品管理</span>
                         </div>
                	</div>
                	<div id="div">
           			</div>
  			  <div class="table-wrap">
               	  <div class="table-wrap-box">
               	 	<h2>商品查询</h2>
                    <form action="${pageContext.request.contextPath}/items/gotoQueryItem.action" name="queryItemForm" id="queryItemForm"  method="post">
                    	  <input type="hidden" name="taskId" id="taskId" value="${taskId}" />
                          <div class="form-model">
                                <ul id="ul-outter-queryItem" style="height: 101px;">
                                   <li>
                                	  <label><span style="color: red">*</span>DOCID :</label>
                                  	  <input type="text" name="docId" id="docId" value="${docId}" class="input" style="width: 500px;" />
    								  <span id="div_docId_or_url_err_info" class="name-pop"></span>  
                                   </li>
                                   <li>
                                	  <label><span style="color: red">*</span>URL :</label>
                                  	  <input type="text" name="url" id="url" value="${url}" class="input" style="width: 500px;" />
                                   </li>
                                   <li>
	                                   <div class="submit-box" style="padding-left: 292px;">
		                            	  <input name="button" id="button" type="button" class="defult-btn" value="编码" onclick="javascript:gotoQueryItem();"/>
		                           	   </div>
	                           	   </li>
                                   <li>
                                   	  <label><span style="color: red">*</span>查询结果 :</label>
                                   	  <div id="div-item-query-result" style="width: 800px; word-break: break-word; margin-left: 46px; min-height: 10px; overflow: auto;">${itemQueryResult}</div>
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