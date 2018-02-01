<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
 <base href="<%=basePath%>" />
<link href="<%=basePath%>/css/ispider.css" rel="stylesheet" type="text/css" />
<link href="<%=basePath%>/css/comm.css" rel="stylesheet" type="text/css" />
<script type="text/javascript" src="<%=basePath%>/js/jquery-1.4.4.min.js"></script>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" /></head>


<body onload="javascript:checkUser();">
	<div class="inner-box">
	       		<div class="nav-sub-bar">
	           		<div class="nav-sub">
	           			<span class="current">查看用户</span>
	                   	 <%-- <a href="user/queryAll.action" >返回</a>--%>
	                   </div>
	           	</div>
	  		  <div class="table-wrap">
	          	  <div class="table-wrap-box">
	          	 	<h2>会员信息</h2>
	               <form>
	                 <div class="form-model form-view">
	                   <ul>
	                      	<li>
	                      	  <label>用户名 :</label>
	                      	  <span>${us.userName }</span>
	                         </li>
	                         <li>
	                      	  <label>权限:</label>
	                      	  <c:forEach items="${descprition }" var="descPtion">
	                      	  	<c:choose>
	                   			<c:when test="${descPtion == 'super' }">
	                   				超级管理员
	                   			</c:when>
	                   			<c:when test="${descPtion == 'ispider' }">
	                   				爬虫管理员
	                   			</c:when>
	                   			<c:otherwise>
	                   				无权限
	                   			</c:otherwise>
	                   		</c:choose>
	                      	  </c:forEach>
	                         </li>
	                       </ul>
	                     </div>
	                   
	               </form>
	          	  </div>
	           </div>
	           <div class="table-wrap"></div>
	   	</div>
</body></html>