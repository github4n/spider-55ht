	 
//全选/反选	 
 function checkBox(){
		 var dription= document.getElementById("dription").value;
		 var value = ""+dription;
		 if("" != value){
			 var checkBox = document.getElementsByName("checkB");
			 if(value.indexOf(",")>0){
				 var values = value.split(","); 
				 for(var i=0;i<values.length;i++){
					for(var j=0; j<checkBox.length; j++){
						if(checkBox[j].value == values[i]){
							checkBox[j].checked=true;
						}
					}
				 }
			 }else{
				 for(var j=0; j<checkBox.length; j++){
						if(checkBox[j].value == value){
							checkBox[j].checked=true;
						}
					}
			 }
		 }
	}
	 //修改
	 function updateUser(){
		var password = pwd();
		var password1 = pwd1();
		if(password ==0 && password1 ==0){
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