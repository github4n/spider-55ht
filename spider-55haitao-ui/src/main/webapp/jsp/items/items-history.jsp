<%@ page contentType="text/html; charset=UTF-8" language="java" errorPage="" pageEncoding="utf-8"%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="s" uri="/struts-tags"%>
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
		<script type="text/javascript" src="<%=basePath%>js/proxy/proxy-home.js"></script>
		<style>
			a:link{text-decoration:none ; color:#666 ;}
			a:visited {text-decoration:none ; color:#666 ;}
			a:hover {text-decoration:underline ; color:#000 ;}
			a:active {text-decoration:none ; color:#000 ;} 
		</style>
		
		<script  type="text/javascript">
		function searchItemHistory(){
			var url = $("#url").val();
			if(url == ''){
				alert("url is empty!!!");
				return;
			}
			$("#searchItemHistoryForm").submit();
		}
		</script>
	</head>
	<body onload="javascript:highlightLeftMenuBarItemsQueryHistory();">
		<div class="wrap">
			<div class="header">
				<div class="logo">
					<a href="#"><img src="<%=basePath%>images/logo.png" alt="cms"
						width="360" height="65" /></a>
				</div>
			</div>
			<div class="bar">
				<div class="location">位置&nbsp;&gt; 商品历史价格管理</div>
			</div>
			<div class="main">
				<div class="main-inner">
					<div class="inner-box">
					<div class="nav-sub-bar">
							<div class="nav-sub">
								<span class="current"> 商品历史价格管理</span>
							</div>
						</div>
						<div class="table-wrap">
							<div class="table-wrap-box">
							<form action="${pageContext.request.contextPath}/items/searchItemHistory.action" name="searchItemHistoryForm"
									id="searchItemHistoryForm" method="post">
									<input type="hidden" name="currentPageId" id="currentPageId"
										value="${currentPageId}" />
									<h2>
										商品历史查询列表 <span class="filter"> <label> URL : <input
												name="url" type="text"
												id="url" />
												<input
												name="pageNos" type="hidden"
												id="pageNos"/>
										<input
												type="button" name="buttonFilter" id="buttonFilter" value="查询"
												class="filter-btn" onclick="javascript:searchItemHistory();" /></label>
										</span>
									</h2>
								</form>
								<table class="table-info" id="myTable">
										<thead>
											<tr>
												<th class="sum-area">docId</th>
												<th>原url</th>
												<th>清洗后url</th>
											</tr>
										</thead>
										<tbody>
												<tr>
													<td>${docId }</td>
													<td>${origUrl }</td>
													<td>${cleaningUrl}</td>
												</tr>
										</tbody>
									</table>
								<div id="taskListDiv">
									<table class="table-info" id="myTable">
										<thead>
											<tr>
												<th>原价</th>
												<th class="sum-area">售价</th>
												<th>skus</th>
												<th>创建时间</th>
											</tr>
										</thead>
										<tbody>
											<c:forEach items="${itemHistoryList}" var="itemHistoryView">
												<tr>
													<td>${itemHistoryView.origPrice }</td>
													<td>${itemHistoryView.salePrice }</td>
													<td>${itemHistoryView.skus}</td>
													<td>${itemHistoryView.createTime }</td>
												</tr>
											</c:forEach>
										</tbody>
									</table>
									</br></br>
									<div class="table-bottom">
										<div class="pag-no" id="page_no">
											<div class="table-bottom">
												<div class="pag-no">
													<span> 共有${total}条 共${pages}页  第${pageNo}页
													 <c:if test="${pageNo > 1}">
													 	<a href="${pageContext.request.contextPath}/items/searchItemHistory.action?pageNos=1&url=${origUrl}">首页</a>
													 	<a href="${pageContext.request.contextPath}/items/searchItemHistory.action?pageNos=${pageNo-1 }&url=${origUrl}">上一页</a>
													 </c:if>
													<c:if test="${pageNo < pages }">
														<a href="${pageContext.request.contextPath}/items/searchItemHistory.action?pageNos=${pageNo+1 }&url=${origUrl}">下一页</a>
														<a href="${pageContext.request.contextPath}/items/searchItemHistory.action?pageNos=${pages }&url=${origUrl}">末页</a>
													</c:if>
													</span>
												</div>
											</div>
						          	  </div>
						            </div>
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