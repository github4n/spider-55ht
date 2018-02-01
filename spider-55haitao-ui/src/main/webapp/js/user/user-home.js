// 编辑用户
	function editUser(userId){
		var pageId = $("#pageId").val();
		window.location.href="getUserByUserId.action?userId="+userId+"&name=edit&&pageId="+pageId;
	}
	
	// 删除用户 
	function deleteUser(userId,userName){
		var mark = 0;
		if(confirm("您确定要删除用户 "+userName+" 吗？")){
			var url = "checkWebByUserId.action";
			$.ajax({
				url:url,
				data:{
			    	userIds:userId
			    	},
			    type: 'POST',
			    dataType: 'json',
			    timeout: 1000,
			    cache: false,
			    async: false,
			    success: function(data){
			   		if(data ==0){
			   			mark = 0;	
			   		}else{
			   			mark = 1;
			   		}
			    },
			    error: function(data){
			    	alert('操作失败!');
			    }
			});
			if(mark == 0){
				var pageId = $("#pageId").val();
				window.location.href="deleteUser.action?userIds="+userId+"&&pageId="+pageId;
			}else{
				alert("用户 "+userName+" 里面还有任务，请先把任务删除后才能删除此用户！");
				return;
			}
			
		}
	}
	
	// 批量删除
	function deleteUsers(){
		var chkInputs=new Array(); 
		var checkbox = document.getElementsByName("chkMsgId23");
		for(var i=0;i<checkbox.length;i++){
			if(checkbox[i].checked){
				chkInputs[i]=checkbox[i].value;
			}
		} 
		var userIds = chkInputs;
		var mark = 0;
		if(confirm("您确定要删除所选用户吗？")){
			var url = "checkWebByUserId.action?userIds="+userIds;
			$.ajax({
				url:url,
			    type: 'POST',
			    dataType: 'json',
			    timeout: 1000,
			    cache: false,
			    async: false,
			    success: function(data){
			   		if(data ==0){
			   			mark = 0;	
			   		}else{
			   			mark = 1;
			   		}
			    },
			    error: function(data){
			    	alert('操作失败!');
			    }
			});
			if(mark == 0){
				window.location.href="deleteUser.action?userIds="+userIds;
			}else{
				alert("所选用户里面还有任务，请先把任务删除后才能删除此用户！");
				return;
			}
		}
	}
	
	
	// 查看用户 
	function viewUser(userId){
		var top = (window.screen.availHeight - 700) / 2;
    	var left = (window.screen.availWidth - 565) / 2;
    	var url = "getUserByUserId.action?userId="+userId;
    	window.open (url,"_blank","newwindow", "height=300, width=500, toolbar=no, menubar=no, scrollbars=no,left="+left+",top="+top+",resizable=no, location=no, status=no");
	}
	
	// 全选或反选复选框
	function allCheck(check){
		var checkbox = document.getElementsByName("chkMsgId23");
		if(check.checked){
			for(var i=0;i<checkbox.length;i++){
				checkbox[i].checked="checked";
			}    	
		}else{
			for(var i=0;i<checkbox.length;i++){
				checkbox[i].checked="";
			} 
		}
	}
	//分页跳转
	function spiltPage(){
		var jump = $("#jump").val();
		var pages = $("#countPage").val();
		var pageNum = $("#pageNums").val();
		if("" == jump){
			alert("请输入页数！");
			return;
		}else if(/[^\d|]/g.test(jump)){
			alert("只能输入数字！");
			return;
			}else if(parseInt(jump)>parseInt(pages)){
			alert("您输入的页数过大，请正确输入！");
			return;
		}else{
			var pageid = jump*pageNum-pageNum;
			window.location.href="getAllUser.action?pageId="+pageid;
		}
	}