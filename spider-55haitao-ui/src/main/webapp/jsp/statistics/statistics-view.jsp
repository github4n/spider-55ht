<%@ page contentType="text/html; charset=UTF-8" language="java" errorPage="" pageEncoding="utf-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%
	String basePath = request.getScheme()
			+ "://"
			+ request.getServerName() //
			+ ":" + request.getServerPort() + request.getContextPath()
			+ "/";
%>

<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />
		<link rel="stylesheet" type="text/css" href="<%=basePath%>css/ispider.css" />
		<link rel="stylesheet" type="text/css" href="<%=basePath%>css/comm.css" />
		<link rel="stylesheet" type="text/css" href="<%=basePath%>css/tipsy.css" />
		<link rel="stylesheet" type="text/css" href="<%=basePath%>css/flick/jquery-ui-1.8.16.custom.css" />

		<script type="text/javascript" src="<%=basePath%>js/jquery-1.4.4.min.js"></script>
		<script type="text/javascript" src="<%=basePath%>js/jquery-ui-1.8.16.custom.min.js"></script>
		<script type="text/javascript" src="<%=basePath%>js/ispider.js"></script>
		<script type="text/javascript" src="<%=basePath%>js/datapicker-fix.js"></script>
		<script type="text/javascript" src="<%=basePath%>js/tipsy.js"></script>
		<style>
			a:link{text-decoration:none ; color:#666 ;}
			a:visited {text-decoration:none ; color:#666 ;}
			a:hover {text-decoration:underline ; color:#000 ;}
			a:active {text-decoration:none ; color:#000 ;} 
			.submit-box {
				padding-left: 10px;
			}
			.title {
				width: 100px;
			}
		</style>
		
	</head>
	<body onload="javascript:highlightLeftMenuBarStatistics();">
		<div class="wrap">
			<div class="header">
				<div class="logo">
					<a href="#"><img src="<%=basePath%>images/logo.png" alt="cms"
						width="360" height="65" /></a>
				</div>
			</div>
			<div class="bar">
				<div class="location">位置&nbsp;&gt; 统计管理</div>
				<div class="search-wrap">
					<form name="searchByTaskNameForm" id="searchByTaskNameForm" action="searchByTaskName.action" method="post">
						<input type="hidden" name="currentPageId" id="currentPageId" value="${currentPageId}" /> 
						<input name="searchTaskName" id="searchTaskName" type="text" class="inputt" value="${searchTaskName}" />
						<input type="button" class="search-btn" onclick="javascript:searchByTaskName();" />
					</form>
				</div>
			</div>
			<div class="main">
				<div class="main-inner">
					<div class="inner-box">
						<div class="nav-sub-bar">
							<div class="nav-sub">
								<span class="current">查看统计详情</span>
							</div>
						</div>
						<div class="table-wrap">
							<div class="table-wrap-box">
									<h2>统计详情</h2>
									<table class="table-info" id="myTable">
											<tr>
												<td class="title">任务ID</td><td>${statisticsView.taskId}</td>
											</tr>
											<tr>
												<td class="title">任务名称</td><td>${statisticsView.taskName }</td>
											</tr>
											<tr>
												<td class="title">开始时间</td><td>${statisticsView.startTime }</td>
											</tr>
											<tr>
												<td class="title">结束时间</td><td>${statisticsView.endTime }</td>
											</tr>
											<tr>
												<td class="title">种子总量</td><td>${statisticsView.totalCount }</td>
											</tr>
											<tr>
												<td class="title">成功数量</td><td>${statisticsView.successCount }</td>
											</tr>
											<tr>
												<td class="title">失败数量</td><td>${statisticsView.failedCount }</td>
											</tr>
											<tr>
												<td class="title">下架数量</td><td>${statisticsView.offlineCount }</td>
											</tr>
									</table>
									<div class="submit-box">
	                            		<input name="button" id="back" type="button" class="defult-btn" value="返回" onclick="javascript:history.go(-1);"/>
	                           		 </div>
							</div>
						</div>
					</div>
				</div>
			</div>
			<jsp:include page="/jsp/leftMenu.jsp"></jsp:include>
			<div class="footer"></div>
		</div>
	</body>
</html>