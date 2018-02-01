<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c"%>
<%
	String path = request.getContextPath();
	String basePath = request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort() + path + "/";
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
			<div class="logo">
				<a href="#">
					&nbsp;
					<!-- <img src="<%=basePath%>/images/logo.png" alt="cms" width="1288" height="101" /> -->
				</a>
			</div>
		</div>
		<div class="bar">
			<div class="location"></div>
			<div class="search-wrap"></div>
		</div>
		<div class="main">
			<div class="main-inner">
				<div class="inner-box">
					<div class="table-wrap">
						<div style="margin-left: 446px; margin-top: 200px; color: #3a2d38;">
							<h2>The realtime-crawler system is ok:)</h2>
						</div>
					</div>
				</div>
			</div>
		</div>
		<div class="footer"></div>
	</div>
</body>
</html>