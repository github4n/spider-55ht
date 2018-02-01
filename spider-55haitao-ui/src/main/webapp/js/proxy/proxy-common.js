function checkProxyFields() {
	
	var regex =  /^([0-9]|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.([0-9]|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.([0-9]|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])\.([0-9]|[1-9]\d|1\d\d|2[0-4]\d|25[0-5])$/;
	var checkSuccess = true;// 默认校验都通过

	// 区域的校验
	var regionId = $("#regionId").val();
	if (regionId == "") {
		$("#div_regionId_err_info").html(
				"<span style='color: red'>区域不能为空！</span>");
		checkSuccess = false;
	}
	if (regionId .length >64) {
		$("#div_regionId_err_info").html(
				"<span style='color: red'>区域不能大于64个字符长度！</span>");
		checkSuccess = false;
	}

	
	//代理ip的校验
	var ip = $("#ip").val();
	if (ip == "") {
		$("#div_ip_err_info").html(
		"<span style='color: red'>代理ip不能为空！</span>");
		checkSuccess = false;
	}
	/*正则校验**/
	if(ip != ""&&!regex.test(ip)){  
		$("#div_ip_err_info").html(
		"<span style='color: red'>代理ip不符合ip规则！</span>");
		checkSuccess = false;
	} 
	if (ip.length >64) {
		$("#div_ip_err_info").html(
		"<span style='color: red'>代理ip不能大于64个字符长度！</span>");
		checkSuccess = false;
	}
	/*键盘按下事件**/
	$("#ip").keydown(function(){
		$("#div_ip_err_info").html("");
	});
	//端口的校验
	var port = $("#port").val();
	if (port == "") {
		$("#div_port_err_info").html(
		"<span style='color: red'>端口不能为空！</span>");
		checkSuccess = false;
	}
	if (port.length >64) {
		$("#div_port_err_info").html(
		"<span style='color: red'>端口不能大于64个字符长度！</span>");
		checkSuccess = false;
	}
	$("#port").keydown(function(){
		$("#div_port_err_info").html("");
	});
	if (isNaN(port)) {     
		$("#div_port_err_info").html("<span style='color: red'>只能输入数字！</span>");
		checkSuccess = false;
  }     
	return checkSuccess;
}

$(function() {
    $("#ip").numeral("ip");  
    $("#port").numeral("port");  
});

//文本框只能输入数字，并屏蔽输入法和粘贴  
$.fn.numeral = function(flag) {     
   $(this).css("ime-mode", "disabled");     
   this.bind("keypress",function(e) {
   var code = (e.keyCode ? e.keyCode : e.which);  //兼容火狐 IE   
       if(!$.browser.msie&&(e.keyCode==0x8))  //火狐下不能使用退格键     
       {     
            return ;     
           }
       if(code<48||code>57){
    	   if("ip"==flag&&code==46){//ip输入框,允许.输入
    		   $("#div_"+flag+"_err_info").html("");
    	   }else{
    		   $("#div_"+flag+"_err_info").html("<span style='color: red'>只能输入数字！</span>");
    	   }
       }else{
    	   $("#div_"+flag+"_err_info").html("");
       }
           return (code >= 48 && code<= 57)||code==46;     
   });     
//   this.bind("blur", function() {     
//       if (this.value.lastIndexOf(".") == (this.value.length - 1)) {
//           this.value = this.value.substr(0, this.value.length - 1);     
//       } else if (isNaN(this.value)) {     
//           this.value = "";
//       }     
//   });     
   this.bind("paste", function() {     
       var s = clipboardData.getData('text');     
       if (!/\D/.test(s));     
       value = s.replace(/^0*/, '');  
       return false;     
   });     
   this.bind("dragenter", function() {     
       return false;     
   });     
   this.bind("keyup", function() {     
   if (/(^0+)/.test(this.value)) {     
       this.value = this.value.replace(/^0*/, '');   
       
       }     
   });     
};
