function insertDictionary() {
	var checkSuccess = checkDictionaryFields();// 检查有没有校验不通过的字段值
	if (!checkSuccess) {
		return;
	}

	var createDictionaryForm = $("#insertDictionaryForm");
	createDictionaryForm.submit();
}

$(document).ready(function() {// 在文档加载完毕时,执行一些初始化操作以及绑定一些组件的事件处理器
	// 高亮显示页面左边对应菜单项
	highlightLeftMenuBarDictionary();
	// 绑定"任务类型"下拉框的onChange事件处理器
//	bindTaskTypeOnChangeEventHandler();
	// 绑定多个输入框的onFoucs事件处理器
//	bindFieldsOnFoucsEventHandler();
});