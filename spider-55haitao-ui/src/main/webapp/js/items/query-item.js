$(document).ready(function() {
	$("#docId").bind("focus", function() {
		$("#div_docId_or_url_err_info").html("");
	});
	
	$("#url").bind("focus", function() {
		$("#div_docId_or_url_err_info").html("");
	});
	
	// 根据div高度调整ul高度
	resizeQueryItemWindow();
});

function resizeQueryItemWindow(){
	var divHeight = $("div#div-item-query-result").height();
	var ulHeight = Number(divHeight) + 200;
	$("ul#ul-outter-queryItem").css("height", ulHeight + "px");
}

function gotoQueryItem(){
	var docId = $("#docId").val();
	var url = $("#url").val();
	
	if(docId == "" && url == ""){
		$("#div_docId_or_url_err_info").html("<span style='color: red'>DOCID和URL不能同时为空！</span>");
		return;
	}
	
	if(docId != "" && docId.length != 32){
		$("#div_docId_or_url_err_info").html("<span style='color: red'>DOCID应该是32个字符！</span>");
		return;
	}
	
	var form = $("#queryItemForm");
	form.submit();
}