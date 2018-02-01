function addUser(){
	var username = userName();
	var password = pwd();
	var password1 = pwd1();
	if(password ==0 && password1==0 && username ==0){
		var j=0;
		var checkInputs=new Array(); 
		var checkBox = document.getElementsByName("checkB");
		for(var i=0; i<checkBox.length; i++){
			if(checkBox[i].checked){
				checkInputs[j] = checkBox[i].value; 
				j++;
			}
		}
		document.getElementById("checkBoxHidden").value=checkInputs;
		document.getElementById("form1").submit();
	}else{
		if(username == 1){
			$("#userName_err_info").html("<span style='color: red'><img src='images/error.jpg'>用户名不能为空</span>");
		}else if(username == 0){
			$("#userName_err_info").html("<span><img src='images/success.jpg'>填写正确</span>");
		}else if(username ==2){
			$("#userName_err_info").html("<span style='color: red'><img src='images/error.jpg'>用户名不能有特殊字符</span>");
		}else if(username ==3){
			$("#userName_err_info").html("<span style='color: red'><img src='images/error.jpg'>用户名已存在</span>");
		}
		if(password ==1){
			$("#password_err_info").html("<span style='color: red'><img src='images/error.jpg'>密码不能为空</span>");
		}else if(password ==0){
			$("#password_err_info").html("<span><img src='images/success.jpg'>填写正确</span>");
		}
		if(password1 == 1){
			$("#password1_err_info").html("<span style='color: red'><img src='images/error.jpg'>确认密码不能为空</span>");
		}else if(password1 == 2){
			$("#password1_err_info").html("<span style='color: red'><img src='images/error.jpg'>两次密码不一致</span>");
		}else if(password1 == 0){
			$("#password1_err_info").html("<span><img src='images/success.jpg'>填写正确</span>");
		}
		alert("抱歉，填写不正确，请正确填写后再提交！");
	}
}