function editTask() {
	var checkSuccess = checkFields();// 检查有没有校验不通过的字段值
	if (!checkSuccess) {
		return;
	}

	handleAutomaticTaskTimeWindow();

	var editTaskForm = $("#editTaskForm");
	editTaskForm.submit();
}

$(document).ready(function() {// 在文档加载完毕时,执行一些初始化操作以及绑定一些组件的事件处理器
	// 高亮显示页面左边对应菜单项
	highlightLeftMenuBarTask();
	// 绑定"任务类型"下拉框的onChange事件处理器
	bindTaskTypeOnChangeEventHandler();
	// 绑定多个输入框的onFoucs事件处理器
	bindFieldsOnFoucsEventHandler();

	// 2.初始化"任务类型"下拉框控件值,以及对应需要显示/不显示的div区域
	var taskTypeHiddenText = $("#taskTypeHiddenText").val();
	if (taskTypeHiddenText == "M") {
		$("#taskType").find("option[value='M']").attr("selected", true);
		$("#autom-task-time-div").css("display", "none");
		$("#ul-outter").css("height", "1001px");
	} else if (taskTypeHiddenText == "A") {
		$("#taskType").find("option[value='A']").attr("selected", true);
		$("#autom-task-time-div").css("display", "block");
		$("#ul-outter").css("height", "1168px");
	}
});