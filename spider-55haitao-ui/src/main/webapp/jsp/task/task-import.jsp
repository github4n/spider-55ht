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
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<script type="text/javascript">
	function controlInputMode(){
		if($("li>select#mode").val()=="0"){
			$("li>input[name='file']").val("");
			$("li#fileInput").hide();
			$("li#textInput").show();
			$("li.result").hide();
		} else if($("li>select#mode").val()=="1"){
			$("#urls").val("");
			$("li#fileInput").show();
			$("li#textInput").hide();
			$("li.result").hide();
		}
	}
	window.onload = function initPage(){
		controlInputMode();
		if(${map.successCount}!="NULL"){
			$("li.result").show();
			$("#successCount").html(${map.successCount});
			$("#failCount").html(${map.errorCount});
			$("#urls").val($("#errorUrls").val());
		}
	};
</script>
</head>
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
                        	<span class="current">导入种子</span>
                         </div>
                	</div>
                	<div id="div">
           			</div>
  			  <div class="table-wrap">
               	  <div resultclass="table-wrap-box">
               	 	<h2>导入种子</h2>
                    <form id="form" action="${pageContext.request.contextPath}/task/importSeeds.action" enctype="multipart/form-data" method="post">
                    	  <input type="hidden" name="taskId" id="taskId" value="${taskView.id}"/>
                    	  <input id="errorUrls" type="hidden" value="${map.errorUrls}"/>
                          <div class="form-model">
                                <ul id="ul-outter" style="height: 700px;">
                                   <li>
                                	  <label><span style="color: red">*</span>任务名称 :</label>
                                  	  <input type="text" value="${taskView.name}" class="input" style="width: 500px;" readonly="readonly"/>
                                   </li>
                                   <li>
                                   	  <label><span style="color: red">*</span>URL级别 :</label>
                                   	  <select id="grade" name="grade" style="width: 500px;" class="input">
                                   	  		<c:forEach begin="0" end="10" step="1" var="grade">
                                   	  			<option value="${grade }" <c:if test="${map.grade==grade }">selected</c:if>>${grade }</option>
                                   	  		</c:forEach>
									  </select>
                                   </li>
                                   <li>
                                   	  <label><span style="color: red">*</span>URL类型 :</label>
                                   	  <select id="urlType" name="urlType" style="width: 500px;" class="input">
                                   	  		<option value="LINK" <c:if test="${map.urlType=='LINK' }">selected</c:if>>LINK</option>
                                   	  		<option value="ITEM" <c:if test="${map.urlType=='ITEM' }">selected</c:if>>ITEM</option>
									  </select>
                                   </li>
                                   <li>
                                   	  <label><span style="color: red">*</span>输入方式 :</label>
                                   	  <select id="mode" name="mode" style="width: 500px;" onchange="controlInputMode()" class="input">
                                   	  		<option value="0" <c:if test="${map.mode=='0' }">selected</c:if>>文本输入</option>
                                   	  		<option value="1" <c:if test="${map.mode=='1' }">selected</c:if>>文件上传</option>
									  </select>
                                   </li>
                                   <li class="result" style="display: none;">
                                   	  <label style="vertical-align: top;">导入结果:</label>
                                      <span id="msg" style="color: red">去除重复后导入成功<span id="successCount"></span>个；失败<span id="failCount"></span>个，若有失败，请仔细检查URL：</span>
                                   </li>
                                   <li id="fileInput">
                                   		<label style="vertical-align: top;"><span style="color: red">*</span>种子文件:</label>
                                   		<input type="file" name="file" title="选择文件"/>
                                   </li>
                                   <li id="textInput">
                                   	  <label style="vertical-align: top;"><span style="color: red">*</span>种子链接:</label>
                                      <textarea id="urls" name="urls" rows="29" cols="99"></textarea>
                                   </li>
                                </ul>
                                 <div class="submit-box">
                            		<input name="button" id="button" type="submit" class="defult-btn" value="导入"/>
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