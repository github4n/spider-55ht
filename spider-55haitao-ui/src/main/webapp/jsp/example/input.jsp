
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
    <title>register</title>
    <meta http-equiv="pragma" content="no-cache">

  </head>
  
  <body>
     
      
      <s:text name="title" />

       <s:form action="i18n.action" method="post" name="frmPost" >
        <!--s:textfield name="name" label="姓名"/ -->
        <!-- s:textfield name="age" label="年龄" / -->
        <!-- s:submit name="submit" value=" 提交  " / -->
      
       
        <s:textfield name="name" key="name"></s:textfield><br/>
        <s:textfield name="age" key="age"></s:textfield><br/>
        <s:submit name="submit" key="submit"></s:submit>
    </s:form>
    

  </body>
</html>
