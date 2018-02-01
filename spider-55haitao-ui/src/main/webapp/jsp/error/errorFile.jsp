<%@ page language="java" contentType="text/html; charset=GB18030"
    pageEncoding="GB18030"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=GB18030">
<title>error</title>
</head>
<script type="text/javascript">
	var i = 5 ;
	function show_secs(){
		if(i!=-1){
			document.getElementById("div").innerHTML="<h2>文件上传失败<span style='color: red'> "+i+" </span>秒钟后自动返回</h2><a href='javascript:void(0);'onclick='getAllweb();'>点击这里返回</a>";
			init();
		}else{
			window.location.href="getAllWebsite.action";
		}
	}
	function init(){
	 	 window.setTimeout('show_secs()',1000);
	 	 i--;
	}
	function getAllweb(){
		window.location.href="getAllWebsite.action";
	}
</script>
<body onload="javascript:init();">
	<center>
		<div id="div">
			<h2>文件上传失败<span style="color: red"> 5 </span>秒钟后自动返回</h2><a href='javascript:void(0)' onclick="getAllweb();">点击这里返回</a>
		</div>
	</center>
</body>
</html>