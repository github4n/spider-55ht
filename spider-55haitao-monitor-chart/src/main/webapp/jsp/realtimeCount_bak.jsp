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
</head>
<body>
	<div class="right_col" role="main">
		<div class="">
			<div class="x_panel">
				<div class="x_title">
					<h2>
						时间区间内核价次数统计 <small>单位：秒</small>
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
					<canvas id="realtimeChart"></canvas>
				</div>
			</div>
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
<script type="text/javascript">
	$(function(){
		$.post("${pageContext.request.contextPath}/realtimeCount.action",function(data){
			if(data=="")
				alert("当前时间核价数据尚未入库！");
			var config = {
					type: 'line',
					data: {
						labels: ["1秒内", "2秒内", "3秒内", "4秒内", "5秒内", "6秒内", "7秒内", "8秒内", "9秒内", "10秒内", "超过10秒"],
					  datasets: [{
						label: '数量',
						backgroundColor: "#26B99A",
						data: data.x
					  }]
					},

					options: {
					  scales: {
						yAxes: [{
						  ticks: {
							beginAtZero: true
						  }
						}]
					  }
					}
				}
			var ctx = document.getElementById("realtimeChart");
			var mybarChart = new Chart(ctx, config);
		});
	});
</script>
</html>