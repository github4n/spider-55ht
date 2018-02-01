<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
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
<script type="text/javascript" src="<%=basePath%>js/task/task-common.js"></script>
<script type="text/javascript" src="<%=basePath%>js/task/task-edit.js"></script>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" /></head>
<body onload="javascript:highlightLeftMenuBarTask();">
<div class="wrap">
<div class="header">
	<div class="logo"><a href="#"><img src="<%=basePath%>images/logo.png" alt="cms" width="360" height="65" /></a></div>
</div>
<div class="bar">
	<div class="location">
   	位置 > 任务管理
   	</div>
</div>
	<div class="main">
 		<div class="main-inner">
  			<div class="inner-box">
            		<div class="nav-sub-bar">
                		<div class="nav-sub">
                        	<span class="current">查看任务</span>
                         </div>
                	</div>
                	<div id="div">
           			</div>
  			  <div class="table-wrap">
               	  <div class="table-wrap-box">
               	 	<h2>查看任务</h2>
 <%--                    <form action="${pageContext.request.contextPath}/task/editTask.action" name="editTaskForm" id="editTaskForm"  method="post">
                    		 <!-- 以下几个变量的值,只有在提交表单前的javascript中赋值,才合适 -->
                    		 <input type="hidden" id="taskId" name="id" value="${taskView.id}" />
                    		 <input type="hidden" id="taskStatus" name="status" value="${taskView.status}" />
                    		 <input type="hidden" id="taskUpdateOnly" name="updateOnly" value="${taskView.updateOnly}" />
			                 <input type="hidden" id="taskPeriod" name="period" value="${taskView.period}" />
			                 <input type="hidden" id="taskWinStart" name="winStart" value="${taskView.winStart}" />
			            	 <input type="hidden" id="taskWinEnd" name="winEnd" value="${taskView.winEnd}" /> --%>
                          <div class="form-model">
                                <ul id="ul-outter" style="height: 1001px;">
                                   <li>
                                	  <label><span style="color: red">*</span>任务名称 :</label>
                                  	  <input type="text" name="name" id="taskName" value="${taskView.name}" class="input" style="width: 500px;" disabled="disabled"/>
    								  <span id="div_taskName_err_info" class="name-pop"></span>  
                                   </li>
                                   <li>
                                   	  <label><span style="color: red">*</span>网站域名 :</label>
                                   	  <input type="text" name="domain" id="taskDomain" value="${taskView.domain}" class="input" style="width: 500px;" disabled="disabled"/>
                                   	  <span id="div_taskDomain_err_info" class="name-pop"></span>  
                                   </li>
                                   <li>
                                   	  <label><span style="color: red">*</span>初始URL :</label>
                                   	  <input type="text" name="initUrl" id="taskInitUrl" value="${taskView.initUrl}" class="input" style="width: 500px;" disabled="disabled"/>
                                   	  <span id="div_taskInitUrl_err_info" class="name-pop"></span>
                                   </li>
                                   <li>
                                   	  <label><span style="color: red">*</span>执行速率 :</label>
                                   	  <input type="text" name="ratio" id="taskRatio" value="${taskView.ratio}" class="input" style="width: 500px;" disabled="disabled"/>
                                   	  <label style="width: 25px;">/10"</label>
    							      <span id="div_taskRatio_err_info" class="name-pop"></span>
                                   </li>
                                   <li>
                                   	  <label>任务权重 :</label>
                                   	  <input type="text" name="weight" id="taskWeight" value="${taskView.weight}" class="input" style="width: 500px;" disabled="disabled"/>
                                   	  <span id="div_taskWeight_err_info" class="name-pop"></span>
                                   </li>
                                   <li>
                                	  <label>任务描述 :</label>
                                  	  <input type="text" name="description" id="taskDescription" value="${taskView.description}" class="input" style="width: 500px;" disabled="disabled"/>
    								  <span id="div_taskDescription_err_info" class="name-pop"></span>
                                   </li>
                                   <li>
                                   	  <label>负责人员 :</label>
                                   	  <input type="text" name="master" id="taskMaster" value="${taskView.master}" class="input" style="width: 500px;" disabled="disabled"/>
                                   	  <span id="div_taskMaster_err_info" class="name-pop"></span>
                                   </li>
                                   <li>
                                   	  <label>网站地区 :</label>
                                   	  <input type="text" name="siteRegion" id="taskSiteRegion" value="${taskView.siteRegion}" class="input" style="width: 500px;" disabled="disabled"/>
                                   	  <span id="div_taskSiteRegion_err_info" class="name-pop"></span>
                                   </li>
                                   <li>
                                   	  <label>代理地区 :</label>
                                   	  <select id="taskProxyRegionId" name="" class="input" style="width: 500px;" disabled="disabled">
                                   	  		<option value="" <c:if test="${taskView.proxyRegionId=='' }">selected</c:if>>不使用代理</option>
									      <c:forEach var="proxyRegion" items="${proxyRegionList}"> 
									        <option value="${proxyRegion.regionId}"<c:if test="${proxyRegion.regionId==taskView.proxyRegionId }">selected</c:if>>${proxyRegion.regionName}</option>
									      </c:forEach>
									  </select>
                                   	  <span id="div_taskProxyRegionId_err_info" class="name-pop"></span>
                                   </li>
                                   <li>
                                   	  <input type="hidden" id="taskTypeHiddenText" name="taskTypeHiddenText" value="${taskView.type}" />
                                   	  <label><span style="color: red">*</span>任务类型 :</label>
                                   	  <select name="type" id="taskType" style="height: 30px; width: 503px;" disabled="disabled">
	                                      <option value="M">Manual</option>
	                                      <option value="A">Automatic</option>
                                      </select>
                                   </li>
                                   <li style="height: 181px; display: none;" id="autom-task-time-div">
                                   		<div>
                                   			<ul>
			                                   <li>
			                                   	  <label><span style="color: red">*</span>任务周期 :</label>
			                                   	  <select id="autom-task-period-hour" name="autom-task-period-hour" style="height: 30px; width: 200px;" disabled="disabled">
	                                      			<c:forEach begin="0" end="24" step="1" var="ele">
	                                      				<c:if test="${taskView.periodView.hour == ele}">
	                                      					<option value="${ele}" selected="selected">${ele}</option>
	                                      				</c:if>
	                                      				<c:if test="${taskView.periodView.hour != ele}">
	                                      					<option value="${ele}">${ele}</option>
	                                      				</c:if>
													</c:forEach>
			                                   	  </select><label style="width: 40px;">小时</label>
			                                   	  <select id="autom-task-period-minute" name="autom-task-period-minute" style="height: 30px; width: 200px;" disabled="disabled">
			                                   	  	<c:forEach begin="0" end="59" step="1" var="ele">
			                                   	  		<c:if test="${taskView.periodView.minute == ele}">
	                                      					<option value="${ele}" selected="selected">${ele}</option>
	                                      				</c:if>
	                                      				<c:if test="${taskView.periodView.minute != ele}">
	                                      					<option value="${ele}">${ele}</option>
	                                      				</c:if>
													</c:forEach>
			                                   	  </select><label style="width: 40px;">分钟</label>
			                                   </li>
			                                   <li>
			                                   	  <label><span style="color: red">*</span>开始时间 :</label>
			                                   	  <select id="autom-task-win-start-year" name="autom-task-win-start-year" style="height: 30px; width: 100px;" disabled="disabled">
			                                   	  	<c:forEach begin="2016" end="2020" step="1" var="ele">
			                                   	  		<c:if test="${taskView.startTimeWindowView.year == ele}">
	                                      					<option value="${ele}" selected="selected">${ele}</option>
	                                      				</c:if>
	                                      				<c:if test="${taskView.startTimeWindowView.year != ele}">
	                                      					<option value="${ele}">${ele}</option>
	                                      				</c:if>
													</c:forEach>
			                                   	  </select><label style="width: 20px;">年</label>
			                                   	  <select id="autom-task-win-start-month" name="autom-task-win-start-month" style="height: 30px; width: 71px;" disabled="disabled">
			                                   	  	<c:forEach begin="1" end="12" step="1" var="ele">
			                                   	  		<c:if test="${taskView.startTimeWindowView.month == ele}">
	                                      					<option value="${ele}" selected="selected">${ele}</option>
	                                      				</c:if>
	                                      				<c:if test="${taskView.startTimeWindowView.month != ele}">
	                                      					<option value="${ele}">${ele}</option>
	                                      				</c:if>
													</c:forEach>
			                                   	  </select><label style="width: 20px;">月</label>
			                                   	  <select id="autom-task-win-start-day" name="autom-task-win-start-day" style="height: 30px; width: 71px;" disabled="disabled">
			                                   	  	<c:forEach begin="1" end="31" step="1" var="ele">
			                                   	  		<c:if test="${taskView.startTimeWindowView.day == ele}">
	                                      					<option value="${ele}" selected="selected">${ele}</option>
	                                      				</c:if>
	                                      				<c:if test="${taskView.startTimeWindowView.day != ele}">
	                                      					<option value="${ele}">${ele}</option>
	                                      				</c:if>
													</c:forEach>
			                                   	  </select><label style="width: 20px;">日</label>
			                                   	  <select id="autom-task-win-start-hour" name="autom-task-win-start-hour" style="height: 30px; width: 50px;" disabled="disabled">
			                                   	  	<c:forEach begin="0" end="23" step="1" var="ele">
			                                   	  		<c:if test="${taskView.startTimeWindowView.hour == ele}">
	                                      					<option value="${ele}" selected="selected">${ele}</option>
	                                      				</c:if>
	                                      				<c:if test="${taskView.startTimeWindowView.hour != ele}">
	                                      					<option value="${ele}">${ele}</option>
	                                      				</c:if>
													</c:forEach>
			                                   	  </select><label style="width: 20px;">时</label>
			                                   	  <select id="autom-task-win-start-minute" name="autom-task-win-start-minute" style="height: 30px; width: 50px;" disabled="disabled">
			                                   	  	<c:forEach begin="0" end="59" step="1" var="ele">
			                                   	  		<c:if test="${taskView.startTimeWindowView.minute == ele}">
	                                      					<option value="${ele}" selected="selected">${ele}</option>
	                                      				</c:if>
	                                      				<c:if test="${taskView.startTimeWindowView.minute != ele}">
	                                      					<option value="${ele}">${ele}</option>
	                                      				</c:if>
													</c:forEach>
			                                   	  </select><label style="width: 20px;">分</label>
			                                   </li>
			                                   <li>
			                                   	  <label><span style="color: red">*</span>结束时间 :</label>
			                                   	  <select id="autom-task-win-end-year" name="autom-task-win-end-year" style="height: 30px; width: 100px;" disabled="disabled">
			                                   	  	<c:forEach begin="2016" end="2020" step="1" var="ele">
			                                   	  		<c:if test="${taskView.endTimeWindowView.year == ele}">
	                                      					<option value="${ele}" selected="selected">${ele}</option>
	                                      				</c:if>
	                                      				<c:if test="${taskView.endTimeWindowView.year != ele}">
	                                      					<option value="${ele}">${ele}</option>
	                                      				</c:if>
													</c:forEach>
			                                   	  </select><label style="width: 20px;">年</label>
			                                   	  <select id="autom-task-win-end-month" name="autom-task-win-end-month" style="height: 30px; width: 71px;" disabled="disabled">
			                                   	  	<c:forEach begin="1" end="12" step="1" var="ele">
			                                   	  		<c:if test="${taskView.endTimeWindowView.month == ele}">
	                                      					<option value="${ele}" selected="selected">${ele}</option>
	                                      				</c:if>
	                                      				<c:if test="${taskView.endTimeWindowView.month != ele}">
	                                      					<option value="${ele}">${ele}</option>
	                                      				</c:if>
													</c:forEach>
			                                   	  </select><label style="width: 20px;">月</label>
			                                   	  <select id="autom-task-win-end-day" name="autom-task-win-end-day" style="height: 30px; width: 71px;" disabled="disabled">
			                                   	  	<c:forEach begin="1" end="31" step="1" var="ele">
			                                   	  		<c:if test="${taskView.endTimeWindowView.day == ele}">
	                                      					<option value="${ele}" selected="selected">${ele}</option>
	                                      				</c:if>
	                                      				<c:if test="${taskView.endTimeWindowView.day != ele}">
	                                      					<option value="${ele}">${ele}</option>
	                                      				</c:if>
													</c:forEach>
			                                   	  </select><label style="width: 20px;">日</label>
			                                   	  <select id="autom-task-win-end-hour" name="autom-task-win-end-hour" style="height: 30px; width: 50px;" disabled="disabled">
			                                   	  	<c:forEach begin="0" end="23" step="1" var="ele">
			                                   	  		<c:if test="${taskView.endTimeWindowView.hour == ele}">
	                                      					<option value="${ele}" selected="selected">${ele}</option>
	                                      				</c:if>
	                                      				<c:if test="${taskView.endTimeWindowView.hour != ele}">
	                                      					<option value="${ele}">${ele}</option>
	                                      				</c:if>
													</c:forEach>
			                                   	  </select><label style="width: 20px;">时</label>
			                                   	  <select id="autom-task-win-end-minute" name="autom-task-win-end-minute" style="height: 30px; width: 50px;" disabled="disabled">
			                                   	  	<c:forEach begin="0" end="59" step="1" var="ele">
			                                   	  		<c:if test="${taskView.endTimeWindowView.minute == ele}">
	                                      					<option value="${ele}" selected="selected">${ele}</option>
	                                      				</c:if>
	                                      				<c:if test="${taskView.endTimeWindowView.minute != ele}">
	                                      					<option value="${ele}">${ele}</option>
	                                      				</c:if>
													</c:forEach>
			                                   	  </select><label style="width: 20px;">分</label>
			                                   </li>
	                                   		</ul>
	                                   	</div>
	                               </li>
                                   <li>
                                   	  <label style="vertical-align: top;"><span style="color: red">*</span>配置文件:</label>
                                      <textarea name="config" id="taskConfig" rows="29" cols="99" disabled="disabled"><c:out value="${taskView.config}" escapeXml="true"/></textarea>
    							      <span id="div_taskConfig_err_info" class="name-pop"></span>
                                   </li>
                                </ul>
                                 <div class="submit-box">
                            		<input name="button" id="button" type="button" class="defult-btn" value="返回" onclick="javascript:history.go(-1);"/>
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