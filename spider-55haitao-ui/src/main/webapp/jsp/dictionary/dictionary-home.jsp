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
<%-- 		<script type="text/javascript" src="<%=basePath%>js/dictionary/dictionary-home.js"></script> --%>
		<style>
			a:link{text-decoration:none ; color:#666 ;}
			a:visited {text-decoration:none ; color:#666 ;}
			a:hover {text-decoration:underline ; color:#000 ;}
			a:active {text-decoration:none ; color:#000 ;} 
		</style>
		
		<script  type="text/javascript">
			function editDictionary(id) {
				window.location.href = "${pageContext.request.contextPath}/dictionary/gotoEditDictionaryPage.action?id=" + id;
// 				$.post("gotoEditdictionaryPage.action",{"dictionaryView.id":id},function(result){
// 				  });
			}
			function deleteDictionary(type){
				window.location.href = "${pageContext.request.contextPath}/dictionary/doDeleteDictionary.action?type=" + type;
			}
			function getDictionaryDetails(type){
				window.location.href = "${pageContext.request.contextPath}/dictionary/getDictionaryDetails.action?type=" + type;
			}
			
			function gotoSelectPage(pageSize){
				var page=$("#jump").val();
				if(page==''){
					page=1;
				}
				window.location.href = "${pageContext.request.contextPath}/dictionary/getDictionaries.action?page=" + page+"&rows="+pageSize;
				
			}
		</script>
	</head>
	<body onload="javascript:highlightLeftMenuBarDictionary();">
		<div class="wrap">
			<div class="header">
				<div class="logo">
					<a href="#"><img src="<%=basePath%>images/logo.png" alt="cms"
						width="360" height="65" /></a>
				</div>
			</div>
			<div class="bar">
				<div class="location">位置&nbsp;&gt; 数据字典管理</div>
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
								<span class="current"> 数据字典管理</span>
								<a href="gotoCreateDictionaryPage.action" class="last">添加</a>
							</div>
						</div>
						<c:if test="${page!=null}">
						<div class="table-wrap">
							<div class="table-wrap-box">
								<div id="taskListDiv">
									<table class="table-info" id="myTable">
										<thead>
											<tr>
												<!-- 
												<td class="select-area"><input type="checkbox" name="checkbo_all" id="checkbo" onclick="allCheck(this)" /></td>
												<th class="time-area">任务ID</th>
												 -->
												<th>类别</th>
												<th>名称</th>
												<th>字典项操作</th>
											</tr>
										</thead>
										<tbody>
											<c:forEach items="${pageInfo.list}" var="dictionaryView">
												<tr>
													<!-- 
													<td><input type="checkbox" name="checkboxname" id="checkboxname" value="${taskView.id }" /></td>
													<td>${taskView.id}</td>
													 -->
													<td>${dictionaryView.type}</td>
													<td>${dictionaryView.name}</td>
													<td class="last">
													<!-- 关点修改字典项名称到按钮, 针对系统配置项,代码获取时直接根据type获取数据 -->
														<a href="javascript:void(0)" class="edit-icon tips" title="编辑"
														onclick="return editDictionary(${dictionaryView.id })"></a>
														<a href="javascript:void(0)" class="delete-icon tips" title="删除"
														onclick="return deleteDictionary('${dictionaryView.type }')"></a>
														<a href="javascript:void(0);" class="read-icon tips" title="详情"
														onclick="return getDictionaryDetails('${dictionaryView.type }');"></a>
													</td>
												</tr>
											</c:forEach>
										</tbody>
									</table>
									<div class="table-bottom">
										<div class="pag-no" id="page_no">
										
											<div class="table-bottom">
												<div class="pag-no">
													<span> 共有${pageInfo.total }条 共${pageInfo.pages }页
														第${pageInfo.pageNum }页 <c:choose>
															<c:when test="${pageInfo.isFirstPage}">
							    			首页
							    		</c:when>
															<c:otherwise>
																<a href="${pageContext.request.contextPath}/dictionary/getAllDictionaries.action?page=${pageInfo.firstPage}&rows=${pageInfo.pageSize}">
															</c:otherwise>
														</c:choose> <c:choose>
															<c:when test="${pageInfo.hasPreviousPage}">
																<a href="${pageContext.request.contextPath}/dictionary/getAllDictionaries.action?page=${pageInfo.prePage}&rows=${pageInfo.pageSize}">上一页</a>
															</c:when>
															<c:otherwise>
																<a>上一页 </a>
															</c:otherwise>
														</c:choose> 
														
														<c:forEach items="${pageInfo.navigatepageNums}" var="nav">
									                        <c:if test="${nav == pageInfo.pageNum}">
									                            <td style="font-weight: bold;">${nav}</td>
									                        </c:if>
									                        <c:if test="${nav != pageInfo.pageNum}">
									                            <td>
									                                <a href="${pageContext.request.contextPath}/dictionary/getAllDictionaries.action?page=${nav}&rows=${pageInfo.pageSize}">${nav}</a>
									                            </td>
									                        </c:if>
								                 	   </c:forEach>
								                    
														<c:choose>
															<c:when test="${pageInfo.hasNextPage}">
																<a href="${pageContext.request.contextPath}/dictionary/getAllDictionaries.action?page=${pageInfo.nextPage}&rows=${pageInfo.pageSize}">下一页</a>
															</c:when>
															<c:otherwise>
																<a>下一页</a>
															</c:otherwise>
														</c:choose> <c:choose>
															<c:when test="${pageInfo.isLastPage}">
							    			尾页
							    		</c:when>
															<c:otherwise>
																<a href="${pageContext.request.contextPath}/dictionary/getAllDictionaries.action?page=${pageInfo.lastPage}&rows=${pageInfo.pageSize}">尾页</a>
															</c:otherwise>
														</c:choose> &nbsp;&nbsp;<input type="text" id="jump" size="2" value="" />&nbsp;<a
														href="javascript:void(0)" onclick="gotoSelectPage(${pageInfo.pageSize})"
														>转到</a>
													</span>
												</div>
											</div>
						          	  </div>
						            </div>
								</div>
							</div>
						</div>
						</c:if>
					</div>
				</div>
			</div>
			<jsp:include page="/jsp/leftMenu.jsp"></jsp:include>
			<div class="footer"></div>
		</div>
	</body>
</html>