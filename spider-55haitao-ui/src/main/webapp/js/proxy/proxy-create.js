function insertProxy() {
	var checkSuccess = checkProxyFields();// 检查有没有校验不通过的字段值
	if (!checkSuccess) {
		return;
	}

	var createProxyForm = $("#insertProxyForm");
	var text=$("#regionId").find("option:selected").text();
	$("#regionName").val(text);
	createProxyForm.submit();
}

$(document).ready(function() {// 在文档加载完毕时,执行一些初始化操作以及绑定一些组件的事件处理器
	// 高亮显示页面左边对应菜单项
	highlightLeftMenuBarProxy();
	// 绑定"任务类型"下拉框的onChange事件处理器
//	bindTaskTypeOnChangeEventHandler();
	// 绑定多个输入框的onFoucs事件处理器
//	bindFieldsOnFoucsEventHandler();
});