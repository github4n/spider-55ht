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
		<script type="text/javascript">
			$(function(){
				$("#search").click(function(){
					window.location.href = "<%=request.getContextPath() %>/httpproxy/search.action?$('#form').serialize()";
				});
			});
		</script>
	</head>
	<body onload="javascript:highlightLeftMenuBarHttpProxy();">
		<div class="wrap">
			<div class="header">
				<div class="logo">
					<a href="#"><img src="<%=basePath%>images/logo.png" alt="cms"
						width="360" height="65" /></a>
				</div>
			</div>
			<div class="bar">
				<div class="location">位置&nbsp;&gt; 阿里云代理测试</div>
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
								<span class="current">阿里云代理测试</span>
							</div>
						</div>
						<div class="table-wrap">
							<div class="table-wrap-box">
									<h2>获取数据</h2>
									<form id="form" action="<%=request.getContextPath() %>/httpproxy/get.action" method="post" target="_blank">
									<table class="table-info" id="myTable">
											<tr>
												<td class="title">待抓取URL:</td>
												<td><input type="text" name="url" class="input" style="width: 500px;"/></td>
											</tr>
											<tr>
												<td class="title">代理地区:</td>
												<td>
													<select name="proxyRegionId" class="input" style="width: 500px;">
			                                   	  		<option value="">不使用代理</option>
												      <c:forEach var="proxyRegion" items="${proxyRegionList}"> 
												        <option value="${proxyRegion.regionId}">${proxyRegion.regionName}</option>
												      </c:forEach>
												  </select>
												</td>
											</tr>
											<tr>
												<td class="title">超时时间(毫秒)</td>
												<td><input type="text" name="timeOut" class="input" style="width: 500px;"/></td>
											</tr>
											<tr>
												<td class="title">重试次数</td>
												<td><input type="text" name="retry" class="input" style="width: 500px;"/></td>
											</tr>
									</table>
									<div class="submit-box">
	                            		<input type="submit" class="defult-btn" value="获取数据"/>
	                           		 </div>
	                           		 </form>
							</div>
						</div>
						<div class="table-wrap">
							<div class="table-wrap-box">
									<h2>查找可用代理</h2>
									<form id="form" action="<%=request.getContextPath() %>/httpproxy/search.action" method="post" target="_blank">
									<table class="table-info" id="myTable">
											<tr>
												<td class="title">待抓取URL:</td>
												<td><input type="text" name="url" class="input" style="width: 500px;"/></td>
											</tr>
											<tr>
												<td class="title">代理地区:</td>
												<td>
													<select name="proxyRegionId" class="input" style="width: 500px;">
			                                   	  		<option value="">不使用代理</option>
												      <c:forEach var="proxyRegion" items="${proxyRegionList}"> 
												        <option value="${proxyRegion.regionId}">${proxyRegion.regionName}</option>
												      </c:forEach>
												  </select>
												</td>
											</tr>
											<tr>
												<td class="title">超时时间(毫秒)</td>
												<td><input type="text" name="timeOut" class="input" style="width: 500px;"/></td>
											</tr>
											<tr>
												<td class="title">重试次数</td>
												<td><input type="text" name="retry" class="input" style="width: 500px;"/></td>
											</tr>
									</table>
									<div class="submit-box">
	                            		<input type="submit" class="defult-btn" value="查找可用代理"/>
	                           		 </div>
	                           		 </form>
							</div>
						</div>
						<div class="table-wrap">
							<div class="table-wrap-box">
									<h2>具体代理IP测试</h2>
									<form action="<%=request.getContextPath() %>/httpproxy/comfirm.action" method="post" target="_blank">
									<table class="table-info">
											<tr>
												<td class="title">待抓取URL:</td>
												<td><input type="text" name="url" class="input" style="width: 500px;"/></td>
											</tr>
											<tr>
												<td class="title">代理IP:</td>
												<td><input type="text" name="ip" class="input" style="width: 500px;"/></td>
											</tr>
											<tr>
												<td class="title">端口:</td>
												<td><input type="text" name="port" class="input" style="width: 500px;"/></td>
											</tr>
											<tr>
												<td class="title">超时时间(毫秒)</td>
												<td><input type="text" name="timeOut" class="input" style="width: 500px;"/></td>
											</tr>
											<tr>
												<td class="title">重试次数</td>
												<td><input type="text" name="retry" class="input" style="width: 500px;"/></td>
											</tr>
									</table>
									<div class="submit-box">
	                            		<input type="submit" class="defult-btn" value="测试"/>
	                           		 </div>
	                           		 </form>
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