$(document).ready(function() {
	$("#targetUrl").bind("focus", function() {
		$("#div_targetUrl_err_info").html("");
	});
	
	// 调整一键抓取后的页面布局,根据div高度调整ul高度
	resizeOneKeyCrawleWindow();
});

function resizeOneKeyCrawleWindow(){
	var divHeight = $("div#div-one-key-crawle-result").height();
	var ulHeight = Number(divHeight) + 200;
	$("ul#ul-outter-one-key-crawle").css("height", ulHeight + "px");
}

function oneKeyCrawle(){
	var targetUrl = $("#targetUrl").val();
	if(targetUrl == ""){
		$("#div_targetUrl_err_info").html("<span style='color: red'>目标URL不能为空！</span>");
		return;
	}
	
	var form = $("#oneKeyCrawleForm");
	form.submit();
}

function md5Encode(){
	var sourceString = $("#sourceString").val();
	if(sourceString == ""){
		$("#div_sourceString_err_info").html("<span style='color: red'>原始字串不能为空！</span>");
		return;
	}
	
	var form = $("#md5EncodeForm");
	form.submit();
}