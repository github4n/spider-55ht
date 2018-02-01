function queryItemFromDB(){
	var targetUrl = $.trim($("#targetUrl").val());
	if(targetUrl == ""){
		$("#div_targetUrl_err_info").html("<span style='color: red'>目标URL不能为空！</span>");
		return;
	}
	
	$.post("items/queryItemForUI.action",$("#queryItemForm").serialize(),function(data){
		$("#div_targetUrl_err_info").html("");
		$("#div-query-item-result").html("");
		var height = data.length*3/10;
		if(height > 300)
			$("#ul-outter-one-key-crawle").css("height",height+"px");
		$("#div-query-item-result").html(data);
	})
}