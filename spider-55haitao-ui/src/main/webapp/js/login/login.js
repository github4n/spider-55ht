//登录
	function checkLogin(){
		var userName = document.getElementById("uName").value;
		var passWord = document.getElementById("pwd").value;
		if(userName == "" || passWord == ""){
			document.getElementById("checkLogin").innerHTML="用户名和密码不能为空";
		}else{
			document.getElementById("form").submit();
		}
	}
	//文本框获取焦点
	function loginFocus(){
		document.getElementById("uName").focus();
	}
	//按回车键触动按钮
	document.onkeydown=keyListener;
	function keyListener(e){
	    e = e ? e : event;// 兼容FF
	    if(e.keyCode == 13){
	    	checkLogin();
	    }
	}