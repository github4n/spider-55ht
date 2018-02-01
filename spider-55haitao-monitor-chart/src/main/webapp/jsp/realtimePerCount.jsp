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
<script src="${pageContext.request.contextPath}/js/jquery-3.2.1.min.js"></script>
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
	<div id="container" style="width:100%; height: 500px;"></div><br>
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
		$(function () {
			init();
		});
		function init(){
			Highcharts.setOptions({ global: { useUTC: false } });
			$.getJSON("${pageContext.request.contextPath}/realtimePerCount.action", $("#form").serialize(),  function (data) {
		        $('#container').highcharts({
		            chart: {
		                zoomType: 'x'
		            },
		            title: {
		                text: '实时核价数分时监控'
		            },
		            subtitle: {
		                text: document.ontouchstart === undefined ?
		                '鼠标拖动可以进行缩放' : '手势操作进行缩放'
		            },
		            xAxis: {
		                type: 'datetime',
		                dateTimeLabelFormats: {
		                    millisecond: '%H:%M:%S.%L',
		                    second: '%H:%M:%S',
		                    minute: '%H:%M',
		                    hour: '%H:%M',
		                    day: '%m-%d',
		                    week: '%m-%d',
		                    month: '%Y-%m',
		                    year: '%Y'
		                }
		            },
		            tooltip: {
		                dateTimeLabelFormats: {
		                    millisecond: '%H:%M:%S.%L',
		                    second: '%H:%M:%S',
		                    minute: '%H:%M',
		                    hour: '%H:%M',
		                    day: '%Y-%m-%d',
		                    week: '%m-%d',
		                    month: '%Y-%m',
		                    year: '%Y'
		                }
		            },
		            yAxis: {
		            	min : 0,
		                title: {
		                    text: '次数'
		                }
		            },
		            legend: {
		                enabled: false
		            },
		            plotOptions: {
		                area: {
		                    fillColor: {
		                        linearGradient: {
		                            x1: 0,
		                            y1: 0,
		                            x2: 0,
		                            y2: 1
		                        },
		                        stops: [
		                            [0, Highcharts.getOptions().colors[0]],
		                            [1, Highcharts.Color(Highcharts.getOptions().colors[0]).setOpacity(0).get('rgba')]
		                        ]
		                    },
		                    marker: {
		                        radius: 2
		                    },
		                    lineWidth: 1,
		                    states: {
		                        hover: {
		                            lineWidth: 1
		                        }
		                    },
		                    threshold: null
		                }
		            },
		            series: [{
		                type: 'area',
		                name: '实时核价次数',
		                data: data
		            }]
		        });
		    });
		}
		new Date().picker($("#new_start_time"), $("#new_end_time"));
		setInterval(function(){init();},60000);
</script>
</html>