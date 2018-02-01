<%
	String basePath = request.getScheme()
			+ "://"
			+ request.getServerName() //
			+ ":" + request.getServerPort() + request.getContextPath()
			+ "/";
%>

<%@ page contentType="text/html; charset=utf-8" language="java" errorPage="" pageEncoding="utf-8"%>

<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<base href="<%=basePath%>" />
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<title>登录</title>
<link href="<%=basePath%>css/ispider.css" rel="stylesheet" type="text/css" />
<link href="<%=basePath%>css/comm.css" rel="stylesheet" type="text/css" />
<script type="text/javascript" src="<%=basePath%>js/login/login.js"></script>
</head>
<body class="login-bg" onload="loginFocus();" >
<form name="form" id="form" action="${pageContext.request.contextPath}/user/userLogin.action" method="post">
    <div class="login-wrap">
        <div class="login-top"></div>
        <div class="login-main">
<!--         <s:if test="loginErrorCode > 0"> -->
<!--         	<div class="alert-error"  > -->
<!--        		  <p><b> -->
<!--        		  	</b> -->
<!--        		  </p> -->
<!--         	</div> -->
<!--         </s:if> -->
        	<ul>
        		<li>
        			<div id="checkLogin" style="color: red; margin-left: 80px"></div>
        			<c:choose>
	        			<c:when test="${NO == 'privilegeError'}">
	        				<div style="color: red; margin-left: 80px">您没有权限，请联系管理员</div>
						</c:when>
						<c:when test="${NO == 'loginError'}">
							<div style="color: red; margin-left: 80px">用户名或密码错误</div>
						</c:when>
						<c:when test="${NO == 'System'}">
							<div style="color: red; margin-left: 80px">系统错误，请连续管理员</div>
						</c:when>
       		  		</c:choose>
        		<br />
        		  </li><li><label>用户名:</label><input type="text" id="uName" name="userName" class="inputLogin" value="" /> 
                  </li>
            	<li>
                   <label>密码:</label><input id="pwd" name="password" type="password" class="inputLogin" value=""/>
            	</li>
            </ul>
            <div class="form-submit">
              <input type="button" name="button" id="button" value="登录" class="confirm-btn" onclick="return checkLogin();"/>
            </div>

        </div>
        <div class="login-shadow"></div>
    </div>
</form>
</body>
</html>
