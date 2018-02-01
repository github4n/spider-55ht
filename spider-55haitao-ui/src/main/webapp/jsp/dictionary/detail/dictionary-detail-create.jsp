<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="s" uri="/struts-tags"%>
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
<script type="text/javascript" src="<%=basePath%>js/dictionary/dictionary-common.js"></script>
<script type="text/javascript" src="<%=basePath%>js/dictionary/dictionary-create.js"></script>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" /></head>
<body>
<div class="wrap">
<div class="header">
	<div class="logo"><a href="#"><img src="<%=basePath%>images/logo.png" alt="cms" width="360" height="65" /></a></div>
</div>
<div class="bar">
	<div class="location">
   	位置 >数据字典详情
   	</div>
</div>
	<div class="main">
 		<div class="main-inner">
  			<div class="inner-box">
            		<div class="nav-sub-bar">
                		<div class="nav-sub">
                        	<span class="current">添加字典数据</span>
                         </div>
                	</div>
                	<div id="div">
           			</div>
  			  <div class="table-wrap">
               	  <div class="table-wrap-box">
               	 	<h2>添加字典数据</h2>
                    <form action="${pageContext.request.contextPath}/dictionary/insertDictionaryDetail.action" name="insertDictionaryForm" id="insertDictionaryForm"  method="post">
                          <div class="form-model">
                                <ul id="ul-outter" style="height: 1001px;">
                                   <li>
                                   	  <label><span style="color: red">*</span>类别 :</label>
                                   	  <input type="text" name="type" id="type" value="${dictionaryView.type }" readOnly="true" class="input" style="width: 500px;" />
                                   	  <span id="div_type_err_info" class="name-pop"></span>
                                   </li>
                                   <li>
                                   	  <label><span style="color: red">*</span>名称 :</label>
                                   	  <input type="text" name="name" id="name" value="${dictionaryView.name }" readOnly="true" class="input" style="width: 500px;" />
                                   	  <span id="div_name_err_info" class="name-pop"></span>
                                   </li>
                                   <li>
                                   	  <label><span style="color: red">*</span>键 :</label>
                                   	  <input type="text" name="key" id="key" class="input" style="width: 500px;" />
    							      <span id="div_key_err_info" class="name-pop"></span>
                                   </li>
                                   <li>
                                   	  <label><span style="color: red">*</span>值 :</label>
                                   	  <input type="text" name="value" id="value" class="input" style="width: 500px;" />
    							      <span id="div_value_err_info" class="name-pop"></span>
                                   </li>
                                 <div class="submit-box">
                            		<input name="button" id="button" type="button" class="defult-btn" value="确 定" onclick="javascrt:insertDictionary();"/>
                           		 </div>
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