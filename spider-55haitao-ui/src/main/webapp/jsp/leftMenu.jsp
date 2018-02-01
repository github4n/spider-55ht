<%@ page contentType="text/html; charset=utf-8" language="java" errorPage="" pageEncoding="utf-8"%>
<%-- <%@ taglib prefix="s" uri="/struts-tags"%> --%>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%
	String basePath = request.getScheme()
			+ "://"
			+ request.getServerName() //
			+ ":" + request.getServerPort() + request.getContextPath()
			+ "/";
%>
<script type="text/javascript" src="<%=basePath%>js/task/task_Splice.js"></script>
<script>
function showCurrentDate(){
	var today,hour,second,minute,year,month,date;
	var strDate ;
	today=new Date();
	var n_day = today.getDay();
	switch (n_day)
	{
	    case 0:{
	      strDate = "星期日"
	    }break;
	    case 1:{
	      strDate = "星期一"
	    }break;
	    case 2:{
	      strDate ="星期二"
	    }break;
	    case 3:{
	      strDate = "星期三"
	    }break;
	    case 4:{
	      strDate = "星期四"
	    }break;
	    case 5:{
	      strDate = "星期五"
	    }break;
	    case 6:{
	      strDate = "星期六"
	    }break;
	    case 7:{
	      strDate = "星期日"
	    }break;
	}
	year = today.getYear()+1900;
	month = today.getMonth()+1;
	if(month<10){
		month="0"+month;
	}
	date = today.getDate();
	if(date<10){
		date="0"+date;
	}
	hour = today.getHours();
	if(hour<10){
		hour="0"+hour;
	}
	minute =today.getMinutes();
	if(minute<10){
		minute="0"+minute;
	}
	second = today.getSeconds();
	if(second<10){
		second="0"+second;
	}
	document.getElementById('currentDate').innerHTML = year + ":" + month + ":" + date + " " + strDate +" " + hour + ":" + minute + ":" + second; //显示时间
    window.setTimeout('showCurrentDate()',1000);
}
function initCurrentDate(){
 	 window.setTimeout('showCurrentDate()',1000);
}
$(document).ready(function(){
	showCurrentDate();
});
</script>
<div class="fixed">
<div style="line-height: 20px;">
			<span style="color: red;margin-left: 20px;">系统当前时间：</span><span id="currentDate"></span>
		</div>
<div class="user-info">
	<div class="user-info-box">
		<img src="<%=basePath %>images/user-img.png" width="78" height="78" alt="用户头像" />
		<div class="user-status">
			<h2>
				用户名：
				<c:if test="${login_user_name == 'admin'}">
		        	管理员
		        </c:if>
		        <c:if test="${login_user_name == 'operator'}">
		        	操作员
		        </c:if>
		        <c:if test="${login_user_name == 'visitor'}">
		        	访客
		        </c:if>
			</h2>
			<a href="${pageContext.request.contextPath}/user/userLoginOut.action" class="status">退出登录</a>
		</div>
	</div>
</div>
<script type="text/javascript">
	function highlightLeftMenuBarTask(){
		document.getElementById("taskMgr").className="current";
	}
	function highlightLeftMenuBarUser(){
		 document.getElementById("user").className="current";
	}
	function highlightLeftMenuBarProxy(){
		 document.getElementById("proxyMgr").className="current";
	}
	function highlightLeftMenuBarDictionary(){
		 document.getElementById("dictionaryMgr").className="current";
	}
	function highlightLeftMenuBarStatistics(){
		 document.getElementById("statisticsMgr").className="current";
	}
	function highlightLeftMenuBarHeartbeat(){
		 document.getElementById("heartbeatMgr").className="current";
	}
	function highlightLeftMenuBarHttpProxy(){
		 document.getElementById("httpProxy").className="current";
	}
	function highlightLeftMenuBarOneKeyCrawle(){
		 document.getElementById("oneKeyCrawle").className="current";
	}
	function highlightLeftMenuBarMd5Encode(){
		 document.getElementById("md5Encode").className="current";
	}
	function highlightLeftMenuBarItemsMgr(){
		 document.getElementById("itemsMgr").className="current";
	}
	function highlightLeftMenuBarItemsQuery(){
		 document.getElementById("itemsQuery").className="current";
	}
	function highlightLeftMenuBarItemsQueryHistory(){
		 document.getElementById("itemsHistory").className="current";
	}
</script>
<div class="nav">
	<ul>
		<li id="taskMgr" class=""><a href="${pageContext.request.contextPath}/task/getAllTasks.action" class="home-icon">任务管理</a></li>
		<c:if test="${login_user_name == 'admin' || login_user_name == 'operator'}">
		<li id="proxyMgr" class=""><a href="${pageContext.request.contextPath}/proxy/getAllProxies.action" class="home-icon">代理管理</a></li>
		<li id="dictionaryMgr" class=""><a href="${pageContext.request.contextPath}/dictionary/getDictionaries.action" class="home-icon">字典管理</a></li>
		<%-- <li id="httpProxy" class=""> <a href="${pageContext.request.contextPath }/httpproxy/gotoConfigPage.action" class="home-icon">代理测试</a></li> --%>
		</c:if>
		<li id="heartbeatMgr" class=""> <a href="${pageContext.request.contextPath }/heartbeat/getAllLatestHeartbeat.action" class="home-icon">爬虫管理</a></li>
		<li id="statisticsMgr" class=""><a href="${pageContext.request.contextPath }/statistics/getLastUpdateStatistics.action" class="home-icon">抓取统计</a></li>
		<li id="oneKeyCrawle" class=""><a href="${pageContext.request.contextPath }/tools/gotoOneKeyCrawle.action" class="home-icon">一键抓取</a></li>
		<li id="md5Encode" class=""><a href="${pageContext.request.contextPath }/tools/gotoMd5Encode.action" class="home-icon">编码工具</a></li>
		<li id="itemsMgr" class=""><a href="${pageContext.request.contextPath }/items/gotoItemsMgrHome.action?page=1&rows=20" class="home-icon">商品统计</a></li>
		<li id="itemsQuery" class=""><a href="${pageContext.request.contextPath }/items/gotoItemsQueryHome.action" class="home-icon">商品查询</a></li>
		<li id="itemsHistory" class=""><a href="${pageContext.request.contextPath }/items/getItemsHistoryHome.action" class="home-icon">历史商品</a></li>
	</ul>
</div>
</div>
<script type="text/javascript">
	$('#funcLab${param.currFunName}').addClass('current');
</script>