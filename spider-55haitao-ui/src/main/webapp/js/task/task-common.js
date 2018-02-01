function checkFields() {
	var checkSuccess = true;// 默认校验都通过

	// 任务名称的校验
	var taskName = $("#taskName").val();
	if (taskName == "") {
		$("#div_taskName_err_info").html(
				"<span style='color: red'>任务名称不能为空！</span>");
		checkSuccess = false;
	}
	if (taskName.length > 64) {
		$("#div_taskName_err_info").html(
				"<span style='color: red'>任务名称不能大于64个字符长度！</span>");
		checkSuccess = false;
	}

	// 网站域名的校验
	var taskDomain = $("#taskDomain").val();
	if (taskDomain == "") {
		$("#div_taskDomain_err_info").html(
				"<span style='color: red'>网站域名不能为空！</span>");
		checkSuccess = false;
	}
	if (taskDomain.length > 32) {
		$("#div_taskDomain_err_info").html(
				"<span style='color: red'>网站域名不能大于32个字符长度！</span>");
		checkSuccess = false;
	}

	// 初始URL的校验
	var taskInitUrl = $("#taskInitUrl").val();
	if (taskInitUrl == "") {
		$("#div_taskInitUrl_err_info").html(
				"<span style='color: red'>初始URL不能为空！</span>");
		checkSuccess = false;
	}
	if (taskInitUrl.length > 256) {
		$("#div_taskInitUrl_err_info").html(
				"<span style='color: red'>初始URL不能大于256个字符长度！</span>");
		checkSuccess = false;
	}

	// 执行速率的校验
	var taskRatio = $("#taskRatio").val();
	if (taskRatio == "") {
		$("#div_taskRatio_err_info").html(
				"<span style='color: red'>执行速率不能为空！</span>");
		checkSuccess = false;
	}
	if (taskRatio.length > 5) {
		$("#div_taskRatio_err_info").html(
				"<span style='color: red'>执行速率不能大于5个字符长度！</span>");
		checkSuccess = false;
	}
	var reg = new RegExp("^[1-9][0-9]*$");
	if (!reg.test(taskRatio)) {
		$("#div_taskRatio_err_info").html(
				"<span style='color: red'>执行速率只能是数字！</span>");
		checkSuccess = false;
	}

	// 任务权重的校验
	var taskWeight = $("#taskWeight").val();
	if (taskWeight.length > 5) {
		$("#div_taskWeight_err_info").html(
				"<span style='color: red'>任务权重不能大于5个字符长度！</span>");
		checkSuccess = false;
	}
	var regTaskWeight = new RegExp("^[1-9][0-9]*$");
	if (!regTaskWeight.test(taskWeight)) {
		$("#div_taskWeight_err_info").html(
				"<span style='color: red'>任务权重只能是数字！</span>");
		checkSuccess = false;
	}

	// 任务描述的校验
	var taskDescription = $("#taskDescription").val();
	if (taskDescription.length > 256) {
		$("#div_taskDescription_err_info").html(
				"<span style='color: red'>任务描述不能大于256个字符长度！</span>");
		checkSuccess = false;
	}

	// 负责人员的校验
	var taskMaster = $("#taskMaster").val();
	if (taskMaster.length > 32) {
		$("#div_taskMaster_err_info").html(
				"<span style='color: red'>负责人员不能大于32个字符长度！</span>");
		checkSuccess = false;
	}

	// 网站地区的校验
	var taskSiteRegion = $("#taskSiteRegion").val();
	if (taskSiteRegion.length > 32) {
		$("#div_taskSiteRegion_err_info").html(
				"<span style='color: red'>网站地区不能大于32个字符长度！</span>");
		checkSuccess = false;
	}

	// 代理地区的校验
	var taskProxyRegionId = $("#taskProxyRegionId").val();
	if (taskProxyRegionId.length > 32) {
		$("#div_taskProxyRegionId_err_info").html(
				"<span style='color: red'>代理地区不能大于32个字符长度！</span>");
		checkSuccess = false;
	}

	// 配置文件的校验
	var taskConfig = $("#taskConfig").val();
	if (taskConfig.length > 4096) {
		$("#div_taskConfig_err_info").html(
				"<span style='color: red'>配置文件不能大于4096个字符长度！</span>");
		checkSuccess = false;
	}

	return checkSuccess;
}

function handleAutomaticTaskTimeWindow() {
	// 校验周期性自动任务的时间合法性,并将时间处理成Long类型以传递给后台struts的action代码
	var taskType = $("#taskType").children("option:selected").val();
	if (taskType == "A") {// 周期性自动任务,需要获取并处理时间
		var periodHour = $("#autom-task-period-hour").children(
				"option:selected").val();
		var periodMinute = $("#autom-task-period-minute").children(
				"option:selected").val();
		var period = (periodHour * 60 * 60 + periodMinute * 60) * 1000;
		$("#taskPeriod").val(period);// 为表单中的变量赋值,以传递到后台struts的action中去

		var winStartYear = $("#autom-task-win-start-year").children(
				"option:selected").val();
		var winStartMonth = $("#autom-task-win-start-month").children(
				"option:selected").val();
		var winStartDay = $("#autom-task-win-start-day").children(
				"option:selected").val();
		var winStartHour = $("#autom-task-win-start-hour").children(
				"option:selected").val();
		var winStartMinute = $("#autom-task-win-start-minute").children(
				"option:selected").val();
		var winStart = winStartYear + "-" + correctTimeField(winStartMonth)
				+ "-" + correctTimeField(winStartDay) + " "
				+ correctTimeField(winStartHour) + ":"
				+ correctTimeField(winStartMinute) + ":00";
		var winStartTimestamp = Date.parse(new Date(winStart));
		$("#taskWinStart").val(winStartTimestamp);// 为表单中的变量赋值,以传递到后台struts的action中去

		var winEndYear = $("#autom-task-win-end-year").children(
				"option:selected").val();
		var winEndMonth = $("#autom-task-win-end-month").children(
				"option:selected").val();
		var winEndDay = $("#autom-task-win-end-day")
				.children("option:selected").val();
		var winEndHour = $("#autom-task-win-end-hour").children(
				"option:selected").val();
		var winEndMinute = $("#autom-task-win-end-minute").children(
				"option:selected").val();
		var winEnd = winEndYear + "-" + correctTimeField(winEndMonth) + "-"
				+ correctTimeField(winEndDay) + " "
				+ correctTimeField(winEndHour) + ":"
				+ correctTimeField(winEndMinute) + ":00";
		var winEndTimestamp = Date.parse(new Date(winEnd));
		$("#taskWinEnd").val(winEndTimestamp);// 为表单中的变量赋值,以传递到后台struts的action中去
	}
}

function correctTimeField(field) {
	if (field.length == 1) {
		return "0" + field;
	} else {
		return field;
	}
}

function bindTaskTypeOnChangeEventHandler() {
	// 绑定"任务类型"下拉选择框的onChange事件
	$("#taskType").change(function() {
		var selectedTaskType = $(this).children("option:selected").val();

		if (selectedTaskType == "M") {
			$("#autom-task-time-div").css("display", "none");
			$("#ul-outter").css("height", "1001px");
		} else if (selectedTaskType == "A") {
			$("#autom-task-time-div").css("display", "block");
			$("#ul-outter").css("height", "1168px");
		}
	});
}

function bindFieldsOnFoucsEventHandler() {
	// 任务名称错误信息的清空
	$("#taskName").bind("focus", function() {
		$("#div_taskName_err_info").html("");
	});

	// 网站域名错误信息的清空
	$("#taskDomain").bind("focus", function() {
		$("#div_taskDomain_err_info").html("");
	});

	// 初始URL错误信息的清空
	$("#taskInitUrl").bind("focus", function() {
		$("#div_taskInitUrl_err_info").html("");
	});

	// 执行速率错误信息的清空
	$("#taskRatio").bind("focus", function() {
		$("#div_taskRatio_err_info").html("");
	});

	// 任务权重错误信息的清空
	$("#taskWeight").bind("focus", function() {
		$("#div_taskWeight_err_info").html("");
	});

	// 任务描述错误信息的清空
	$("#taskDescription").bind("focus", function() {
		$("#div_taskDescription_err_info").html("");
	});

	// 负责人员错误信息的清空
	$("#taskMaster").bind("focus", function() {
		$("#div_taskMaster_err_info").html("");
	});

	// 网站地区错误信息的清空
	$("#taskSiteRegion").bind("focus", function() {
		$("#div_taskSiteRegion_err_info").html("");
	});

	// 代理地区错误信息的清空
	$("#taskProxyRegionId").bind("focus", function() {
		$("#div_taskProxyRegionId_err_info").html("");
	});

	// 配置文件错误信息的清空
	$("#taskConfig").bind("focus", function() {
		$("#div_taskConfig_err_info").html("");
	});
}