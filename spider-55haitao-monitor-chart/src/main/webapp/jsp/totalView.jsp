<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<title>Insert title here</title>
<!-- Bootstrap -->
<link href="<%=request.getContextPath()%>/css/bootstrap.min.css"
	rel="stylesheet">
<!-- Font Awesome -->
<link href="<%=request.getContextPath()%>/css/font-awesome.min.css"
	rel="stylesheet">
<!-- NProgress -->
<link href="<%=request.getContextPath()%>/css/nprogress.css"
	rel="stylesheet">
<!-- Custom Theme Style -->
<link href="<%=request.getContextPath()%>/css/custom.min.css"
	rel="stylesheet">
<style type="text/css">
body {
	background-color: white;
}
</style>
</head>
<body>
	<div class="row">
		<div class="col-md-6 col-sm-6 col-xs-12">
			<div class="x_panel">
				<div class="x_title">
					<h2>
						全量监控<small>单位：个</small>
					</h2>
					<ul class="nav navbar-right panel_toolbox">
						<li><a class="collapse-link"><i class="fa fa-chevron-up"></i></a>
						</li>
						<li class="dropdown"><a href="#" class="dropdown-toggle"
							data-toggle="dropdown" role="button" aria-expanded="false"><i
								class="fa fa-wrench"></i></a>
							<ul class="dropdown-menu" role="menu">
								<li><a href="#">Settings 1</a></li>
								<li><a href="#">Settings 2</a></li>
							</ul></li>
						<li><a class="close-link"><i class="fa fa-close"></i></a></li>
					</ul>
					<div class="clearfix"></div>
				</div>
				<div class="x_content">
					<div id="fulldata"></div>
				</div>
			</div>
		</div>

		<div class="col-md-6 col-sm-6 col-xs-12">
			<div class="x_panel">
				<div class="x_title">
					<h2>
						核价耗时监控 <small>单位：秒</small>
					</h2>
					<ul class="nav navbar-right panel_toolbox">
						<li><a class="collapse-link"><i class="fa fa-chevron-up"></i></a>
						</li>
						<li class="dropdown"><a href="#" class="dropdown-toggle"
							data-toggle="dropdown" role="button" aria-expanded="false"><i
								class="fa fa-wrench"></i></a>
							<ul class="dropdown-menu" role="menu">
								<li><a href="#">Settings 1</a></li>
								<li><a href="#">Settings 2</a></li>
							</ul></li>
						<li><a class="close-link"><i class="fa fa-close"></i></a></li>
					</ul>
					<div class="clearfix"></div>
				</div>
				<div class="x_content">
					<div id="container"></div>
				</div>
			</div>
		</div>
	</div>
	<div class="clearfix"></div>
	<div class="row">
		<div class="col-md-6 col-sm-6 col-xs-12">
			<div class="x_panel">
				<div class="x_title">
					<h2>
						每天实时核价数量趋势 <small>每分钟刷新一次</small>
					</h2>
					<ul class="nav navbar-right panel_toolbox">
						<li><a class="collapse-link"><i class="fa fa-chevron-up"></i></a>
						</li>
						<li class="dropdown"><a href="#" class="dropdown-toggle"
							data-toggle="dropdown" role="button" aria-expanded="false"><i
								class="fa fa-wrench"></i></a>
							<ul class="dropdown-menu" role="menu">
								<li><a href="#">Settings 1</a></li>
								<li><a href="#">Settings 2</a></li>
							</ul></li>
						<li><a class="close-link"><i class="fa fa-close"></i></a></li>
					</ul>
					<div class="clearfix"></div>
				</div>
				<div class="x_content" style="padding: 10px;">
					<div id="realTimePerCount"></div>
				</div>
			</div>
		</div>

		<div class="col-md-6 col-sm-6 col-xs-12">
			<div class="x_panel">
				<div class="x_title">
					<h2>
						实时核价总量走势 <small>每分钟刷新一次</small>
					</h2>
					<ul class="nav navbar-right panel_toolbox">
						<li><a class="collapse-link"><i class="fa fa-chevron-up"></i></a>
						</li>
						<li class="dropdown"><a href="#" class="dropdown-toggle"
							data-toggle="dropdown" role="button" aria-expanded="false"><i
								class="fa fa-wrench"></i></a>
							<ul class="dropdown-menu" role="menu">
								<li><a href="#">Settings 1</a></li>
								<li><a href="#">Settings 2</a></li>
							</ul></li>
						<li><a class="close-link"><i class="fa fa-close"></i></a></li>
					</ul>
					<div class="clearfix"></div>
				</div>
				<div class="x_content">
					<div id="realtimeTotal"></div>
				</div>
			</div>
		</div>
	</div>
	<div class="clearfix"></div>
</body>
<!-- jQuery -->
<script src="<%=request.getContextPath()%>/js/jquery.min.js"></script>
<!-- Bootstrap -->
<script src="<%=request.getContextPath()%>/js/bootstrap.min.js"></script>
<!-- FastClick -->
<script src="<%=request.getContextPath()%>/js/fastclick.js"></script>
<!-- NProgress -->
<script src="<%=request.getContextPath()%>/js/nprogress.js"></script>
<!-- Chart.js -->
<script src="<%=request.getContextPath()%>/js/Chart.min.js"></script>
<!-- Custom Theme Scripts -->
<script src="<%=request.getContextPath()%>/js/custom.js"></script>
<script src="https://img.hcharts.cn/highcharts/highcharts.js"></script>
<script src="https://img.hcharts.cn/highcharts/modules/exporting.js"></script>
<script
	src="https://img.hcharts.cn/highcharts-plugins/highcharts-zh_CN.js"></script>
<script src="${pageContext.request.contextPath}/js/utils.js" type="text/javascript"></script>
<script>
	var times = 0;
	var size = 20;
	var fullData;
	$(function() {
		Highcharts.setOptions({ global: { useUTC: false } });
		initRealTime();
	});
	function initRealTime(){
		times = 0;
		$.post("${pageContext.request.contextPath}/viewAll.action",function(data) {
			fullData = data.fullData;
			setFullDataCharts(fullData);
			initRealtimeChart(data.realtime);
			initRealTimePerCountCharts($.parseJSON(data.realtimePerCount));
			initRealTimeTotalCountCharts($.parseJSON(data.realtimeTotalCount));
		});
	}
	function setFullDataCharts(data){
		if(data=="")
			alert("今日全量数据尚未入库！");
		var length = data.y.length;
		if(times<length/size){
			initFullDataCharts(data.total, data.y.slice(times*size,(times+1)*size), data.x.slice(times*size,(times+1)*size));
			times++;
		}
	}
	function initFullDataCharts(total, ydata, xdata){
		var chart = new Highcharts.Chart(
				{
					chart : {
						renderTo : 'fulldata',
						type : 'bar'//折线图：spline  //指定图表类型，柱状图、折线图等
					},
					title : {
						text : '爬虫收录商品按商家监控'
					},
					subtitle : {
						text : '数据来源: 爬虫系统日志('+ new Date().Format("yyyy-MM-dd")+')'
					},
					xAxis : {
						categories : null,
						title : {
							text : '商家'
						}
					},
					yAxis : {
						min : 0,
						title : {
							text : '商品总数 (个)',
							align : 'high'
						},
						labels : {
							overflow : 'justify'
						}
					},
					tooltip : {
						formatter : function() {
							var num = (this.y * 100 / total);
							num = num.toFixed(2);
							return '占比：' + num + '%'
						}

					},
					plotOptions : {
						bar : {
							dataLabels : {
								enabled : true,
								allowOverlap : true
							}
						}
					},
					legend : {
						layout : 'vertical',
						align : 'right',
						verticalAlign : 'top',
						x : -40,
						y : 100,
						floating : true,
						borderWidth : 1,
						backgroundColor : ((Highcharts.theme && Highcharts.theme.legendBackgroundColor) || '#FFFFFF'),
						shadow : true,
						enabled : false 
					},
					credits : {
						enabled : false
					},
					series : [ {
						name : '数量',
						data : null
					} ]
				});
		chart.xAxis[0].setCategories(ydata);
		chart.series[0].setData(xdata);
	}
	function initRealtimeChart(data){
		if (data == "")
			alert("当前时间核价数据尚未入库！");
		$('#container').highcharts({
			chart : {
				type : 'line'
			},
			title : {
				text : '实时核价耗时监控'
			},
			subtitle : {
				text : '数据来源: 爬虫系统日志	(统计时间：'+ new Date().Format("yyyy-MM-dd hh:mm:ss")+')'
			},
			xAxis : {
				categories : [ '1秒内', '2秒内', '3秒内', '4秒内',
						'5秒内', '6秒内', '7秒内', '8秒内', '9秒内',
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
			series : [{
						name : '当前分钟内统计情况',
						data : data.x
					}]
		});
	}
	
	function initRealTimePerCountCharts(data){
		$('#realTimePerCount').highcharts({
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
	}
	
	function initRealTimeTotalCountCharts(data){
		$('#realtimeTotal').highcharts({
            chart: {
                zoomType: 'x'
            },
            title: {
                text: '实时核价数总量监控'
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
	}
	
	setInterval(function(){initRealTime();},60000);
	setInterval(function(){setFullDataCharts(fullData);},11000);
</script>
</html>