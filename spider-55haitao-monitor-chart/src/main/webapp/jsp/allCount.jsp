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
<script src="https://img.hcharts.cn/jquery/jquery-1.8.3.min.js"></script>
<script src="https://img.hcharts.cn/highcharts/highcharts.js"></script>
<script src="https://img.hcharts.cn/highcharts/modules/exporting.js"></script>
<script
	src="https://img.hcharts.cn/highcharts-plugins/highcharts-zh_CN.js"></script>
</head>
<script src="${pageContext.request.contextPath}/js/utils.js" type="text/javascript"></script>
<body>
	<div id="container" style="min-width: 100%; height: 4000px"></div>
</body>
<script>
	$(function() {
		init();
	});
	function init(){
		$.post("${pageContext.request.contextPath}/allcount.action",function(data){
			if(data=="")
				alert("今日全量数据尚未入库！");
			var total = data.total;
			var chart = new Highcharts.Chart(
					{
						chart : {
							renderTo : 'container',
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
			chart.xAxis[0].setCategories(data.y);
			chart.series[0].setData(data.x);
		});
	}
	setInterval(function(){init();},1000*60*60*24);/* 每天执行一次 */
</script>
</html>