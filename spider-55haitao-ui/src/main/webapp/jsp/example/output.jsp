<%@ page contentType="text/html; charset=utf-8" language="java" errorPage="" pageEncoding="utf-8"%>
<%@ taglib prefix="s" uri="/struts-tags" %>
<%
	String path = request.getContextPath();
	String base = request.getScheme() + "://"
			+ request.getServerName() + ":" + request.getServerPort();
	String basePath = base + path + "/";
%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>    
    <title>My JSP 'output.jsp' starting page</title>
    <script language="javascript" src="jsp/example/example.js" type="text/javascript"></script>
  </head>
  
  <body>
     
      
    姓名:<s:property value="name"/><br/>
    年龄:<s:property value="age"/><br/>
  </body>
</html>
