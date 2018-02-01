<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib prefix="s" uri="/struts-tags"%>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
  <base href="<%=basePath%>" />
<link href="<%=basePath%>/css/ispider.css" rel="stylesheet" type="text/css" />
<link href="<%=basePath%>/css/comm.css" rel="stylesheet" type="text/css" />
<script type="text/javascript" src="<%=basePath%>js/jquery-1.4.4.min.js"></script>
<script type="text/javascript" src="<%=basePath%>js/user/user-check.js"></script>
<script type="text/javascript" src="<%=basePath%>js/user/user-add.js"></script>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
</head>
<body onload="javascript:checkUser();">
<input type=hidden name="hidden" id="hidden" value="">
<div class="wrap">
<div class="header">
	<div class="logo"><a href="#"><img src="<%=basePath%>/images/logo.png" alt="cms" width="360" height="65" /></a></div>
</div>
<div class="bar">
	<div class="location">
   	位置 >管理员维护</div>
   	<!--  
    <div class="search-wrap">
   	  <form ><input name="" type="text" class="search-input" /><input name="" type="button"  class="search-btn" value="  "/></form>
    </div>
    -->
</div>
	<div class="main">
 		<div class="main-inner">
  			<div class="inner-box">
            		<div class="nav-sub-bar">
                		<div class="nav-sub">
                        	<span class="current">编辑用户</span>
                         </div>
                	</div>
       		  <div class="table-wrap">
               	  <div class="table-wrap-box">
               	 	<h2>新建</h2>
                    <form name="form1" id="form1" action="${pageContext.request.contextPath}/user/addUser.action" method="post">
                    	<input type="hidden" id="checkBoxHidden" name="user.description" value="">
                        <div class="form-model">
                              <ul>
                              	<li>
	                           		<label>用户名:</label><input name="userName" id="userName" type="text" class="input" />
	                           		 <span id="userName_rule"></span>
							 	  	 <span id="userName_err_info"></span>  
                              	</li>
                                  <li><label>密码:</label><input name="password" id="password" type="password"  class="input"/>
                                  	<span id="password_rule"></span>
  							 	  	<span id="password_err_info"></span>
                                  </li>
                                  <li><label>确认密码:</label><input name="password1" id="password1" type="password"  class="input" />
                                  	<span id="password1_rule"></span>
  							 	  	<span id="password1_err_info"></span>
  							 	  	<input type="hidden" id="cheHidden" name="privilegeIds" value=""/>
                                  </li>
                                  <li class="form-align">
									<label>
										权限:
									</label>	
                                  	<c:forEach items="${privilegeList}" var="desc">
                                  		<input type="checkbox" name="checkB" id="checkB" value="${desc.id}"/>${desc.description}
                                  	</c:forEach>
								</li>
                              </ul>
                               <div class="submit-box">
                          				<input name="button" type="button"  class="defult-btn" value="确 定" onclick="addUser();"/>
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

