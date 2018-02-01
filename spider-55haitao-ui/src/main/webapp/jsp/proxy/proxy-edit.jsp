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
<script type="text/javascript" src="<%=basePath%>js/proxy/proxy-common.js"></script>
<script type="text/javascript" src="<%=basePath%>js/proxy/proxy-edit.js"></script>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" /></head>
<body>
<div class="wrap">
<div class="header">
	<div class="logo"><a href="#"><img src="<%=basePath%>images/logo.png" alt="cms" width="360" height="65" /></a></div>
</div>
<div class="bar">
	<div class="location">
   	位置 > 代理ip管理
   	</div>
</div>
	<div class="main">
 		<div class="main-inner">
  			<div class="inner-box">
            		<div class="nav-sub-bar">
                		<div class="nav-sub">
                        	<span class="current">修改代理ip</span>
                         </div>
                	</div>
                	<div id="div">
           			</div>
  			  <div class="table-wrap">
               	  <div class="table-wrap-box">
               	 	<h2>修改代理ip</h2>
                    <form action="${pageContext.request.contextPath}/proxy/editProxy.action" name="editProxyForm" id="editProxyForm"  method="post">
                    		 <!-- 以下几个变量的值,只有在提交表单前的javascript中赋值,才合适 -->
                    		 <input type="hidden" id="proxy" name="id" value="${proxyView.id}" />
                    		 <input type="hidden" name="regionName" id="regionName" />
                          <div class="form-model">
                          	<ul id="ul-outter" style="height: 201px;">
                                <li>
                                	  <label><span style="color: red">*</span>区域 :</label>
                                	  <select id="regionId" name="regionId" class="input" style="width: 500px;">
									      <c:forEach var="proxyRegion" items="${proxyRegionList}"> 
									        <option value="${proxyRegion.key}"<c:if test="${proxyRegion.key==proxyView.regionId }">selected</c:if>>${proxyRegion.value}</option>
									      </c:forEach>
									  </select>
    								  <span id="div_regionId_err_info" class="name-pop"></span>  
                                   </li>
<!--                                    <li> -->
<%--                                    	  <label><span style="color: red">*</span>区域名 :</label> --%>
<%--                                    	  <input type="text" name="regionName" id="regionName" value="${proxyView.regionName}" class="input" style="width: 500px;" /> --%>
<%--                                    	  <span id="div_regionName_err_info" class="name-pop"></span>   --%>
<!--                                    </li> -->
                                   <li>
                                   	  <label><span style="color: red">*</span>代理ip :</label>
                                   	  <input type="text" name="ip" id="ip" value="${proxyView.ip}" class="input" style="width: 500px;" />
                                   	  <span id="div_ip_err_info" class="name-pop"></span>
                                   </li>
                                   <li>
                                   	  <label><span style="color: red">*</span>端口 :</label>
                                   	  <input type="text" name="port" id="port" value="${proxyView.port}" class="input" style="width: 500px;" />
    							      <span id="div_port_err_info" class="name-pop"></span>
                                   </li>
                                   </ul>
                                 <div class="submit-box">
                            		<input name="button" id="button" type="button" class="defult-btn" value="确 定" onclick="javascript:editProxy();"/>
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