<%@ page language="java" contentType="text/html; charset=UTF-8"
	pageEncoding="UTF-8"%>
<!DOCTYPE html PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">
<html>
<head>
<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
<!-- Meta, title, CSS, favicons, etc. -->
<meta charset="utf-8">
<meta http-equiv="X-UA-Compatible" content="IE=edge">
<meta name="viewport" content="width=device-width, initial-scale=1">

<title>统计图表</title>
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
<body class="nav-md">
	<div class="container body">
		<div class="main_container">
			<div class="col-md-3 left_col">
				<div class="left_col scroll-view">
					<div class="navbar nav_title" style="border: 0;">
						<a href="#" class="site_title"><i class="fa fa-paw"></i>
							<span>爬虫统计平台</span></a>
					</div>

					<div class="clearfix"></div>

					<!-- menu profile quick info -->
					<div class="profile clearfix">
						<div class="profile_pic">
							<img src="<%=request.getContextPath()%>/images/img.jpg" alt=""
								class="img-circle profile_img">
						</div>
						<div class="profile_info">
							<span>Welcome,</span>
							<h2>Tony</h2>
						</div>
					</div>
					<!-- /menu profile quick info -->

					<br />

					<!-- sidebar menu -->
					<div id="sidebar-menu"
						class="main_menu_side hidden-print main_menu">
						<div class="menu_section">
							<h3>General</h3>
							<ul class="nav side-menu">
								<li><a><i class="fa fa-home"></i> Home <span
										class="fa fa-chevron-down"></span></a>
									<ul class="nav child_menu">
										<li><a href="${pageContext.request.contextPath}/jsp/menu.jsp">Dashboard</a></li>
									</ul></li>
								<li><a><i class="fa fa-bar-chart-o"></i>统计图表<span
										class="fa fa-chevron-down"></span></a>
									<ul class="nav child_menu">
										<li><a href="#"
											onclick="changeUrl('${pageContext.request.contextPath}/jsp/allCount.jsp')">全量监控</a></li>
										<li><a href="#"
											onclick="changeUrl('${pageContext.request.contextPath}/jsp/realtimeCount.jsp')">核价耗时监控</a></li>
										<li><a href="#"
											onclick="changeUrl('${pageContext.request.contextPath}/jsp/realtimePerCount.jsp')">核价数分时监控</a></li>
										<li><a href="#"
											onclick="changeUrl('${pageContext.request.contextPath}/jsp/realtimeTotal.jsp')">核价数总量监控</a></li>
									</ul></li>
								<li><a><i class="fa fa-clone"></i>系统设置<span
										class="fa fa-chevron-down"></span></a>
									<ul class="nav child_menu">
										<li><a href="#">待定</a></li>
										<li><a href="#">待定</a></li>
									</ul></li>
							</ul>
						</div>

					</div>
					<!-- sidebar menu -->

					<!-- menu footer buttons -->
					<div class="sidebar-footer hidden-small">
						<a data-toggle="tooltip" data-placement="top" title="Settings">
							<span class="glyphicon glyphicon-cog" aria-hidden="true"></span>
						</a> <a data-toggle="tooltip" data-placement="top" title="FullScreen">
							<span class="glyphicon glyphicon-fullscreen" aria-hidden="true"></span>
						</a> <a data-toggle="tooltip" data-placement="top" title="Lock"> <span
							class="glyphicon glyphicon-eye-close" aria-hidden="true"></span>
						</a> <a data-toggle="tooltip" data-placement="top" title="Logout"
							href="login.html"> <span class="glyphicon glyphicon-off"
							aria-hidden="true"></span>
						</a>
					</div>
					<!-- menu footer buttons -->
				</div>
			</div>

			<!-- top navigation -->
			<div class="top_nav">
				<div class="nav_menu">
					<nav>
					<div class="nav toggle">
						<a id="menu_toggle"><i class="fa fa-bars"></i></a>
					</div>

					<ul class="nav navbar-nav navbar-right">
						<li class=""><a href="javascript:;"
							class="user-profile dropdown-toggle" data-toggle="dropdown"
							aria-expanded="false"> <img
								src="<%=request.getContextPath()%>/images/img.jpg" alt="">John
								Doe <span class=" fa fa-angle-down"></span>
						</a>
							<ul class="dropdown-menu dropdown-usermenu pull-right">
								<li><a href="javascript:;"> Profile</a></li>
								<li><a href="javascript:;"> <span
										class="badge bg-red pull-right">50%</span> <span>Settings</span>
								</a></li>
								<li><a href="javascript:;">Help</a></li>
								<li><a href="login.html"><i
										class="fa fa-sign-out pull-right"></i> Log Out</a></li>
							</ul></li>

						<li role="presentation" class="dropdown"><a
							href="javascript:;" class="dropdown-toggle info-number"
							data-toggle="dropdown" aria-expanded="false"> <i
								class="fa fa-envelope-o"></i> <span class="badge bg-green">6</span>
						</a>
							<ul id="menu1" class="dropdown-menu list-unstyled msg_list"
								role="menu">
								<li><a> <span class="image"><img
											src="<%=request.getContextPath()%>/images/img.jpg"
											alt="Profile Image" /></span> <span> <span>John Smith</span>
											<span class="time">3 mins ago</span>
									</span> <span class="message"> Film festivals used to be
											do-or-die moments for movie makers. They were where... </span>
								</a></li>
								<li><a> <span class="image"><img
											src="<%=request.getContextPath()%>/images/img.jpg"
											alt="Profile Image" /></span> <span> <span>Tony</span> <span
											class="time">3 mins ago</span>
									</span> <span class="message"> Film festivals used to be
											do-or-die moments for movie makers. They were where... </span>
								</a></li>
								<li><a> <span class="image"><img
											src="<%=request.getContextPath()%>/images/img.jpg"
											alt="Profile Image" /></span> <span> <span>John Smith</span>
											<span class="time">3 mins ago</span>
									</span> <span class="message"> Film festivals used to be
											do-or-die moments for movie makers. They were where... </span>
								</a></li>
								<li><a> <span class="image"><img
											src="<%=request.getContextPath()%>/images/img.jpg"
											alt="Profile Image" /></span> <span> <span>John Smith</span>
											<span class="time">3 mins ago</span>
									</span> <span class="message"> Film festivals used to be
											do-or-die moments for movie makers. They were where... </span>
								</a></li>
								<li>
									<div class="text-center">
										<a> <strong>See All Alerts</strong> <i
											class="fa fa-angle-right"></i>
										</a>
									</div>
								</li>
							</ul></li>
					</ul>
					</nav>
				</div>
			</div>
			<!-- top navigation -->

			<!-- page content -->
			<div class="right_col" role="main">
				<iframe id="content" src="${pageContext.request.contextPath}/jsp/totalView.jsp" width="100%"
					align="middle" frameborder="0" scrolling="no" marginheight="0" marginwidth="0" onLoad="iFrameHeight()" height="1000px;">
				</iframe>
			</div>
			<!-- page content -->

			<!-- footer content -->
			<footer>
			<div class="pull-right"></div>
			<div class="clearfix"></div>
			</footer>
			<!-- footer content -->
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
	function changeUrl(url) {
		$("iframe").attr("src", url);
	}
	function iFrameHeight() {
		var ifm = document.getElementById("content");
		var subWeb = document.frames ? document.frames["content"].document
				: ifm.contentDocument;
		if (ifm != null && subWeb != null) {
			ifm.height = subWeb.body.scrollHeight;
			ifm.width = subWeb.body.scrollWidth;
		}
	}
</script>
</html>