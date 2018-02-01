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
		<script type="text/javascript" src="<%=basePath%>js/task/task-home.js"></script>
		<style>
			a:link{text-decoration:none ; color:#666 ;}
			a:visited {text-decoration:none ; color:#666 ;}
			a:hover {text-decoration:underline ; color:#000 ;}
			a:active {text-decoration:none ; color:#000 ;} 
		</style>
		
		<script  type="text/javascript">
			function gotoSelectPage(pageSize){
				var page=$("#jump").val();
				if(page==''){
					page=1;
				}
				window.location.href = "${pageContext.request.contextPath}/task/getAllTasks.action?page=" + page+"&rows="+pageSize;
				
			}
		</script>
	</head>
	<body onload="javascript:highlightLeftMenuBarTask();">
		<div class="wrap">
			<div class="header">
				<div class="logo">
					<a href="#"><img src="<%=basePath%>images/logo.png" alt="cms"
						width="360" height="65" /></a>
				</div>
			</div>
			<div class="bar">
				<div class="location">位置&nbsp;&gt; 任务管理</div>
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
								<span class="current"> 任务管理</span>
								<c:if test="${login_user_name == 'admin' || login_user_name == 'operator'}">
						        	<a href="gotoCreateTaskPage.action" class="last">创建任务</a>
						        </c:if>
							</div>
						</div>
						<div class="table-wrap">
							<div class="table-wrap-box">
								<form action="searchByCreateDate.action" name="searchByCreateDateForm"
									id="searchByCreateDateForm" method="post">
									<input type="hidden" name="currentPageId" id="currentPageId"
										value="${currentPageId}" />
									<h2>
										任务列表 <span class="filter"> <label> 创建日期<input
												name="searchCreateDateStart" type="text" class="pick-date"
												id="searchCreateDateStart" value="${searchCreateDateStart}" />
										</label> <label> 到<input name="searchCreateDateEnd" type="text"
												class="pick-date" id="searchCreateDateEnd" value="${searchCreateDateEnd}" />
										</label> <label><input
												type="button" name="buttonFilter" id="buttonFilter" value="过滤"
												class="filter-btn" onclick="javascript:filterByCreateDate();" /></label>
										</span>
									</h2>
								</form>
								<div id="taskListDiv">
									<table class="table-info" id="myTable">
										<thead>
											<tr>
												<!-- 
												<td class="select-area"><input type="checkbox" name="checkbo_all" id="checkbo" onclick="allCheck(this)" /></td>
												<th class="time-area">任务ID</th>
												 -->
												<th class="sum-area">任务名称</th>
												<th>域名</th>
												<th>权重</th>
												<th>速率</th>
												<th>负责人</th>
												<th>类型</th>
												<th>状态</th>
												<c:if test="${login_user_name == 'admin' || login_user_name == 'operator'}">
										        	<th>任务控制</th>
										        </c:if>
												<th>任务管理</th>
											</tr>
										</thead>
										<tbody>
											<c:forEach items="${pageInfo.list}" var="taskView">
												<tr>
													<!-- 
													<td><input type="checkbox" name="checkboxname" id="checkboxname" value="${taskView.id }" /></td>
													<td>${taskView.id}</td>
													 -->
													<td>${taskView.name}</td>
													<td>${taskView.domain }</td>
													<td>${taskView.weight }</td>
													<td>${taskView.ratio }</td>
													<td>${taskView.master }</td>
													<td>
														<c:if test="${taskView.type=='M'}">手动</c:if>
														<c:if test="${taskView.type=='A'}">自动</c:if>
													</td>
													<td>
														<c:if test="${taskView.status=='I'}">初始化</c:if>
														<c:if test="${taskView.status=='S'}">启动中</c:if>
														<c:if test="${taskView.status=='A'}">运行中</c:if>
														<c:if test="${taskView.status=='P'}">暂停</c:if>
														<c:if test="${taskView.status=='H'}">挂起</c:if>
														<c:if test="${taskView.status=='F'}">完成</c:if>
														<c:if test="${taskView.status=='E'}">异常</c:if>
														<c:if test="${taskView.status=='D'}">丢弃中</c:if>
														<c:if test="${taskView.status=='V'}">已丢弃</c:if>
														<c:if test="${taskView.status=='M'}">休眠</c:if>
														<c:if test="${taskView.status=='W'}">休眠</c:if>
													</td>
													<c:if test="${login_user_name == 'admin' || login_user_name == 'operator'}">
													<td>
														<c:if test="${taskView.type=='M'}">
															<c:if test="${taskView.status=='I'}">
																<a href="javascript:startupTask('${taskView.id}');">启动</a>
															</c:if>
															<c:if test="${taskView.status=='A'}">
																<a href="javascript:pauseTask('${taskView.id}');">暂停</a>
															</c:if>
															<c:if test="${taskView.status=='P'}">
																<a href="javascript:recoverTask('${taskView.id}');">恢复</a>
															</c:if>
															<c:if test="${taskView.status=='F' || taskView.status=='E'}">
																<a href="javascript:restartTask('${taskView.id}');">重启</a>
															</c:if>
															<c:if test="${taskView.status=='A' || taskView.status=='P' || taskView.status=='F' || taskView.status=='E'}">
																<a href="javascript:discardTask('${taskView.id}');">丢弃</a>
															</c:if>
															<c:if test="${taskView.status=='V'}">
																<a href="javascript:startupTask('${taskView.id}');">启动</a>
															</c:if>
														</c:if>
														<c:if test="${taskView.type=='A'}">
															<c:if test="${taskView.status=='A'}">
																<a href="javascript:pauseTask('${taskView.id}');">暂停</a>
															</c:if>
															<c:if test="${taskView.status=='P'}">
																<a href="javascript:recoverTask('${taskView.id}');">恢复</a>
															</c:if>
														</c:if>
													</td>
										        	</c:if>
													<td class="last">
													<c:if test="${login_user_name == 'admin' || login_user_name == 'operator'}">
														<a href="javascript:void(0)" class="edit-icon tips" title="编辑"
														onclick="return editTask(${taskView.id })"></a>
														<a href="javascript:void(0)" class="delete-icon tips" title="删除"
														onclick="return deleteTask(${taskView.id })"></a>
														<a href="javascript:void(0)" class="import-icon tips" title="导入种子"
														onclick="return gotoImportSeeds(${taskView.id })"></a>
													</c:if>
														<a href="javascript:void(0)" class="read-icon tips" title="查看"
														onclick="return viewTask(${taskView.id })"></a>
													</td>
												</tr>
											</c:forEach>
										</tbody>
									</table>
									<div class="table-bottom">
										<c:if test="${login_user_name == 'admin' || login_user_name == 'operator'}">
											<div class="selectall">
												<b>批量操作：&nbsp;
													<a href="#" title="用于更新程序时，暂停所有正在运行和挂起的任务！" onclick="return runWait()">一键休眠</a>
													<a href="#" title="程序更新完成后，恢复休眠的任务" onclick="return rouse()">一键唤醒</a>
												</b>
											</div>
										</c:if>
										<div class="pag-no" id="page_no">
											<div class="table-bottom">
												<div class="pag-no">
													<span> 共有${pageInfo.total }条 共${pageInfo.pages }页
														第${pageInfo.pageNum }页 <c:choose>
															<c:when test="${pageInfo.isFirstPage}">
							    			首页
							    		</c:when>
															<c:otherwise>
																<a href="${pageContext.request.contextPath}/task/getAllTasks.action?page=${pageInfo.firstPage}&rows=${pageInfo.pageSize}">
															</c:otherwise>
														</c:choose> <c:choose>
															<c:when test="${pageInfo.hasPreviousPage}">
																<a href="${pageContext.request.contextPath}/task/getAllTasks.action?page=${pageInfo.prePage}&rows=${pageInfo.pageSize}">上一页</a>
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
									                                <a href="${pageContext.request.contextPath}/task/getAllTasks.action?page=${nav}&rows=${pageInfo.pageSize}">${nav}</a>
									                            </td>
									                        </c:if>
								                 	   </c:forEach>
								                    
														<c:choose>
															<c:when test="${pageInfo.hasNextPage}">
																<a href="${pageContext.request.contextPath}/task/getAllTasks.action?page=${pageInfo.nextPage}&rows=${pageInfo.pageSize}">下一页</a>
															</c:when>
															<c:otherwise>
																<a>下一页</a>
															</c:otherwise>
														</c:choose> <c:choose>
															<c:when test="${pageInfo.isLastPage}">
							    			尾页
							    		</c:when>
															<c:otherwise>
																<a href="${pageContext.request.contextPath}/task/getAllTasks.action?page=${pageInfo.lastPage}&rows=${pageInfo.pageSize}">尾页</a>
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
					</div>
				</div>
			</div>
			<jsp:include page="/jsp/leftMenu.jsp"></jsp:include>
			<div class="footer"></div>
		</div>
	</body>
</html>