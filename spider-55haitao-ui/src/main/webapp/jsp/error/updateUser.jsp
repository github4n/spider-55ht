<%@ page language="java" contentType="text/html; charset=GB18030"
    pageEncoding="GB18030"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<%
	String basePath = request.getScheme()
			+ "://"
			+ request.getServerName() //
			+ ":" + request.getServerPort() + request.getContextPath()
			+ "/";
%>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=GB18030">
<title>error</title>
</head>
<script type="text/javascript">
	var i = 5 ;
	function show_secs(){
		if(i!=-1){
			document.getElementById("div").innerHTML="<h2>�޸ĳɹ�<span style='color: red'> "+i+" </span>���Ӻ��Զ����ص�¼ҳ��</h2><a href='javascript:void(0);'onclick='getAllweb();'>������ﷵ��</a>";
			init();
		}else{
			window.location.href="<%=basePath%>jsp/login.jsp";
		}
	}
	function init(){
	 	 window.setTimeout('show_secs()',1000);
	 	 i--;
	}
	function getAllweb(){
		window.location.href="<%=basePath%>jsp/login.jsp";
	}
</script>
<body onload="javascript:init();">
	<center>
		<div id="div">
			<h2>�޸ĳɹ�<span style="color: red"> 5 </span>���Ӻ��Զ����ص�¼ҳ��</h2><a href='javascript:void(0)' onclick="getAllweb();">������ﷵ��</a>
		</div>
	</center>
</body>
</html>