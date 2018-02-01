    	function userName(){
    		var mark =0;
    		var userName = $("#userName").val();
    		if("" == userName){
                return 1;//用户名不能为空
          	 }else if(/[@#$&]/.test(userName)){
        	   return 2;//用户名不能有特殊字符
           	 }else{
           		$.ajax({
    				url:"checkUserName.action",
    				data:{
    					userName:userName
    				},
    			    type: 'POST',
    			    dataType: 'json',
    			    timeout: 1000,
    			    cache: false,
    			    async: false,
    			    success: function(data){
    			   		if(data ==0){
    			   			//用户名不存在
    			   			mark = 0;	
    			   		}else{
    			   			mark = 3;
    			   		}
    			    },
    			    error: function(data){
    			    	alert('操作失败!');
    			    }
    			});
           		
                return mark;//填写正确
             }
    	}
		
		function pwd(){
    		var userPwd = $("#password").val();
    		if("" == userPwd){
          	 	return 1;//密码不能为空
          	 }else{
          	 	return 0;
          	 }
    	}
    	function pwd1(){
    		var userPwd = $("#password").val();
    		var userPwd1 = $("#password1").val();
    		if("" == userPwd1){
          	 	return 1;//确认密码不能为空
          	 }else if(userPwd != userPwd1){
          	 	return 2;//两次密码不一致
          	 }else{
          	 	return 0;
          	 }
    	}
    	 $(document).ready(function(){
    		 $("#userName").bind("blur",function(){
    			 $("#userName").bind("focus",function(){
 					$("#userName_err_info").hide();
 				});
    			 $("#userName_err_info").show();
    			var ret = userName();
      			if(ret == 1){
      				$("#userName_err_info").html("<span style='color: red'><img src='images/error.jpg'>用户名不能为空</span>");
      			}else if(ret == 0){
      				$("#userName_err_info").html("<span><img src='images/success.jpg'>填写正确</span>");
      			}else if(ret ==2){
      				$("#userName_err_info").html("<span style='color: red'><img src='images/error.jpg'>用户名不能有特殊字符</span>");
      			}else if(ret ==3){
      				$("#userName_err_info").html("<span style='color: red'><img src='images/error.jpg'>用户名已存在</span>");
      			}
      		});
    		 
     		$("#password").bind("blur",function(){
     			$("#password").bind("focus",function(){
 					$("#password_err_info").hide();
 				});
     			$("#password_err_info").show();
     			var ret = pwd();
     			if(ret == 1){
     				$("#password_err_info").html("<span style='color: red'><img src='images/error.jpg'>密码不能为空</span>");
     			}else if(ret == 0){
     				$("#password_err_info").html("<span><img src='images/success.jpg'>填写正确</span>");
     			}
     		});
     		$("#password1").bind("blur",function(){
     			$("#password1").bind("focus",function(){
 					$("#password1_err_info").hide();
 				});
     			$("#password1_err_info").show();
     			var ret = pwd1();
     			if(ret == 1){
     				$("#password1_err_info").html("<span style='color: red'><img src='images/error.jpg'>确认密码不能为空</span>");
     			}else if(ret == 2){
     				$("#password1_err_info").html("<span style='color: red'><img src='images/error.jpg'>两次密码不一致</span>");
     			}else if(ret == 0){
     				$("#password1_err_info").html("<span><img src='images/success.jpg'>填写正确</span>");
     			}
     		});
     	});
