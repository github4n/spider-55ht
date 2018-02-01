<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<link rel="icon"
	href="https://static.jianshukeji.com/highcharts/images/favicon.ico">
<meta name="viewport" content="width=device-width, initial-scale=1">
<meta name="description" content="">
<title>全量统计</title>
<link href="<%=request.getContextPath()%>/css/bootstrap.min.css" rel="stylesheet">
<link href="<%=request.getContextPath()%>/css/bootstrap-datetimepicker.css" rel="stylesheet">
<script src="https://img.hcharts.cn/jquery/jquery-1.8.3.min.js"></script>
<script src="http://code.jquery.com/ui/1.10.2/jquery-ui.js "></script>
<script src="https://img.hcharts.cn/highcharts/highcharts.js"></script>
<script src="https://img.hcharts.cn/highcharts/modules/exporting.js"></script>
<script src="https://img.hcharts.cn/highcharts-plugins/highcharts-zh_CN.js"></script>
<script src="${pageContext.request.contextPath}/js/bootstrap.min.js"></script>
<script src="${pageContext.request.contextPath}/js/bootstrap-datetimepicker.js"></script>
<script src="${pageContext.request.contextPath}/js/utils.js" type="text/javascript"></script>
<script src="${pageContext.request.contextPath}/js/bootstrap-datetimepicker.zh-CN.js"></script>
</head>
<body>
	<!-- <div class="form-group">
         <div class="input-group date form_date col-md-3" data-date="" data-date-format="yyyy-mm-dd" data-link-field="dtp_input2" data-link-format="yyyy-mm-dd" style="float: right;">
             <input class="form-control" size="16" type="text" value="" readonly onchange="init();" id="time">
             <span class="input-group-addon"><span class="glyphicon glyphicon-remove"></span></span>
			 <span class="input-group-addon"><span class="glyphicon glyphicon-calendar"></span></span>
         </div>
		<input type="hidden" id="dtp_input2" value="" /><br/>
    </div> -->
	<div id="container" style="min-width: 100%; height: 500px;"></div>
	<br>
	<form action="" class="form-horizontal"  role="form" id="form">
		<div class="form-group" style="float: right;">
			 <label for="dtp_input1" class="col-md-1 control-label">开始时间</label>
	         <div class="input-group date col-md-3" data-date="" data-date-format="yyyy-mm-dd hh:ii" data-link-field="start" data-link-format="yyyy-mm-dd hh:ii" id="new_start_time">
	             <input class="form-control" size="16" name="startTime" type="text" value="" readonly>
	             <span class="input-group-addon"><span class="glyphicon glyphicon-remove"></span></span>
				 <span class="input-group-addon"><span class="glyphicon glyphicon-calendar"></span></span>
	         </div>
	         <br>
	         <label for="dtp_input1" class="col-md-1 control-label">结束时间</label>
	         <div class="input-group date col-md-3" data-date="" data-date-format="yyyy-mm-dd hh:ii" data-link-field="start" data-link-format="yyyy-mm-dd hh:ii" id="new_end_time">
	             <input class="form-control" size="16" name="endTime" type="text" value="" readonly>
	             <span class="input-group-addon"><span class="glyphicon glyphicon-remove"></span></span>
				 <span class="input-group-addon"><span class="glyphicon glyphicon-calendar"></span></span>
	         </div>
	         <br>
	         <label for="dtp_input1" class="col-md-1 control-label"></label>
	         <input type="button" value="查&nbsp;&nbsp;&nbsp;&nbsp;询" width="20px;" onclick="init();">
	    </div>
    </form>
</body>
<script>
	$(function() {
		init();
	});
	function init() {
		$.post(
				"${pageContext.request.contextPath}/realtimeCount.action", $("#form").serialize(), 
				function(data) {
					if (data == "")
						alert("当前时间核价数据尚未入库！");
					$('#container')
							.highcharts(
									{
										chart : {
											type : 'line'
										},
										title : {
											text : '实时核价耗时监控'
										},
										subtitle : {
											text : '数据来源: 爬虫系统日志	(统计时间：'
													+ new Date()
															.Format("yyyy-MM-dd hh:mm:ss")
													+ ')'
										},
										xAxis : {
											categories : [ '1秒内',
													'2秒内', '3秒内',
													'4秒内', '5秒内',
													'6秒内', '7秒内',
													'8秒内', '9秒内',
													'10秒内', '超过10秒' ]
										},
										yAxis : {
											title : {
												text : '核价次数'
											}
										},
										plotOptions : {
											line : {
												dataLabels : {
													enabled : true
												// 开启数据标签
												},
												enableMouseTracking : true
											// 开启鼠标跟踪，对应的提示框、点击事件会失效
											}
										},
										series : [ {
											name : '选定时间内核价情况（默认当前分钟）',
											data : data.x
										} ]
									});
				});
	}
	new Date().picker($("#new_start_time"), $("#new_end_time"));
	setInterval(function() {
		init();
	}, 60000);
</script>
</html>