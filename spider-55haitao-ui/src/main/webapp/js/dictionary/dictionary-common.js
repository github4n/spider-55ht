function checkDictionaryFields() {
	
	var checkSuccess = true;// 默认校验都通过

	//type的校验
	var type = $("#type").val();
	if (type == "") {
		$("#div_type_err_info").html(
		"<span style='color: red'>类别不能为空！</span>");
		checkSuccess = false;
	}
	if (type.length >15) {
		$("#div_type_err_info").html(
		"<span style='color: red'>类别不能大于15个字符长度！</span>");
		checkSuccess = false;
	}
	
	//name的校验
	var name = $("#name").val();
	if (name == "") {
		$("#div_type_err_info").html(
		"<span style='color: red'>名称不能为空！</span>");
		checkSuccess = false;
	}
	if (name.length >15) {
		$("#div_name_err_info").html(
		"<span style='color: red'>名称不能大于30个字符长度！</span>");
		checkSuccess = false;
	}
	
	var key = $("#key").val();
	if (key == "") {
		$("#div_key_err_info").html(
		"<span style='color: red'>键值不能为空！</span>");
		checkSuccess = false;
	}
	if (key.length >20) {
		$("#div_key_err_info").html(
		"<span style='color: red'>键值不能大于20个字符长度！</span>");
		checkSuccess = false;
	}
	var value = $("#value").val();
	if (value == "") {
		$("#div_value_err_info").html(
		"<span style='color: red'>值不能为空！</span>");
		checkSuccess = false;
	}
	if (value.length >20) {
		$("#div_value_err_info").html(
		"<span style='color: red'>值不能大于20个字符长度！</span>");
		checkSuccess = false;
	}
	return checkSuccess;
}
function checkDictionaryFields2() {
	
	var checkSuccess = true;// 默认校验都通过
	
	//type的校验
	var type = $("#type").val();
	if (type == "") {
		$("#div_type_err_info").html(
		"<span style='color: red'>类别不能为空！</span>");
		checkSuccess = false;
	}
	if (type.length >15) {
		$("#div_type_err_info").html(
		"<span style='color: red'>类别不能大于15个字符长度！</span>");
		checkSuccess = false;
	}
	//name的校验
	var name = $("#name").val();
	if (name == "") {
		$("#div_type_err_info").html(
		"<span style='color: red'>名称不能为空！</span>");
		checkSuccess = false;
	}
	if (name.length >15) {
		$("#div_name_err_info").html(
		"<span style='color: red'>名称不能大于30个字符长度！</span>");
		checkSuccess = false;
	}
	return checkSuccess;
}

