<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<base href="<%=basePath%>" />
<link href="<%=basePath%>css/ispider.css" rel="stylesheet" type="text/css" />
<link href="<%=basePath%>css/comm.css" rel="stylesheet" type="text/css" />
<link href="<%=basePath%>css/tipsy.css" rel="stylesheet" type="text/css" />
<script type="text/javascript" src="js/jquery-1.4.4.min.js"></script>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
</head>

<body>
<div>
<div class="header">
	<div class="logo"><a href="#"><img src="<%=basePath%>/images/logo.png" alt="cms" width="360" height="65" /></a></div>
</div>
<div class="bar">
	<div class="location">
   	位置  主页</div>
    <div class="search-wrap">
    </div>
</div>
	<div class="main">
 		<div class="main-inner">
  			<div class="inner-box">
		      <div class="table-wrap">
		    <div style="margin-left: 250px; margin-top: 70%px; color: red"><h2>欢迎来到ISpider管理系统</h2></div>
		      </div>
        	</div>
		</div>
	</div>
	<jsp:include page="/jsp/leftMenu.jsp"></jsp:include>
	<div class="footer"></div>
</div>



</body></html>