<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%@taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>
<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml">
<head>
<base href="<%=basePath%>" />
<link href="<%=basePath%>css/ispider.css" rel="stylesheet" type="text/css" />
<link href="<%=basePath%>css/comm.css" rel="stylesheet" type="text/css" />
<link href="<%=basePath%>css/tipsy.css" rel="stylesheet" type="text/css" />
<link href="<%=basePath%>css/flick/jquery-ui-1.8.16.custom.css" rel="stylesheet" type="text/css" />
<script type="text/javascript" src="<%=basePath%>js/jquery-1.4.4.min.js"></script>
<script type="text/javascript" src="<%=basePath%>js/tipsy.js"></script>
<script type="text/javascript" src="<%=basePath%>js/jquery-ui-1.8.16.custom.min.js"></script>
<script type="text/javascript" src="<%=basePath%>js/datapicker-fix.js"></script>
<script type="text/javascript" src="<%=basePath%>js/user/user-home.js"></script>
<meta http-equiv="Content-Type" content="text/html; charset=utf-8" />
<script>
	
</script>
</head>

<body onload="javascript:checkUser();">
<s:hidden name="pageId" id="pageId"></s:hidden>
<div class="wrap">
<div class="header">
	<div class="logo"><a href="#"><img src="<%=basePath%>/images/logo.png" alt="cms" width="360" height="65" /></a></div>
</div>
<div class="bar">
	<div class="location">
   	位置 >管理员维护</div>
   <!--   <div class="search-wrap">
   	  <form ><input name="" type="text" class="search-input" /><input name="" type="button"  class="search-btn" value="  "/></form>
    </div>
    -->
</div>
	<div class="main">
 		<div class="main-inner">
  			<div class="inner-box">
     		 <div class="nav-sub-bar">
        		<div class="nav-sub">
                 <span  class="current"  >管理员维护</span>
                 <a href="getAllPrivilege.action" class="last"   >添加管理员</a>
                 </div>
        		</div>
		      <div class="table-wrap">
           	    <div class="table-wrap-box">
               	 	<h2>网站列表</h2> 
                  <table class="table-info" id="myTable">
                  	<thead>
                      <tr>
                        <th class="select-area">
                        	<input type="checkbox" name="chkMsgId" id="" onclick="allCheck(this)" />
                        </th>
                        <th class="name-area">用户名</th>
                        <th>权限</th>
                        <th  class="time-area">操作</th>
                      </tr>
                    </thead>
                  	<tbody>
                  	<c:forEach items="${userList}" var="list">
                  		<tr>
                        <td>
                          <input type="checkbox" name="chkMsgId23" id="chkMsgId23" value="${list.userId }" onclick="toChkSon(this.value);"/>
                        </td>
                        <td>${list.userName }</td>
                        <td>
                        	<c:forEach items=" ${list.descriptionList }" var="descPtion">
                        		<c:choose>
                        			<c:when test="${descPtion == ' [super' }">
                        				超级管理员
                        			</c:when>
                        			<c:when test="${descPtion == ' ispider]' }">
                        				爬虫管理员
                        			</c:when>
                        			<c:when test="${descPtion == ' [super]' }">
                        				超级管理员
                        			</c:when>
                        			<c:when test="${descPtion == ' [ispider]' }">
                        				爬虫管理员
                        			</c:when>
                        			<c:otherwise>
                        				无权限
                        			</c:otherwise>
                        		</c:choose>
                        	</c:forEach>
                        </td>
                        <td class="last">
                        	<a href="javascript:void(0)" class="edit-icon tips" title="编辑" onclick="editUser(${list.userId })"></a>
                        	<a href="javascript:void(0)" class="delete-icon tips" title="删除" onclick="deleteUser(${list.userId },'${list.userName }')"></a> 
                        	<a href="javascript:void(0)" class="read-icon tips" title="查看" onclick="viewUser(${list.userId })"></a> 
                        </td>
                      </tr>
                  	</c:forEach>
                    </tbody>
                    </table>
                    <div class="table-bottom">
                	<div class="selectall">
						<b>批量操作：  <a href="javascript:deleteUsers();">删除</a></b>
					</div>
					<div class="pag-no" id="pag-no">
                       <span>
						 共有${pageBean.count }条 共${pageBean.pages }页 第${pageBean.pageid }页 
				    	<c:choose>
				    		<c:when test="${pageBean.pages==0}">
				    			首页
				    		</c:when>
				    		<c:otherwise>
				    			<a href="getAllUser.action?pageId=0">首页 </a>
				    		</c:otherwise>
				    	</c:choose>
				    	<c:choose> 
				    		<c:when test="${pageBean.upPageYOrN=='Y'}">
				    			<a href="getAllUser.action?pageId=${pageBean.upPage}">上一页 </a>
				    		</c:when>
				    		<c:otherwise>
				    			<a>上一页 </a>
				    		</c:otherwise>
				    	</c:choose>
				    	<c:choose>
				    		<c:when test="${pageBean.nextPageYOrN=='Y'}">
				    			<a href="getAllUser.action?pageId=${pageBean.nextPage}">下一页</a>
				    		</c:when>
				    		<c:otherwise>
				    			<a>下一页</a>
				    		</c:otherwise>
				    	</c:choose>
				    	<c:choose>
				    		<c:when test="${pageBean.pages==0}">
				    			尾页
				    		</c:when>
				    		<c:otherwise>
				    			<a href="getAllUser.action?pageId=${pageBean.lastPage}">尾页</a>
				    		</c:otherwise>
				    	</c:choose>
				    	&nbsp;&nbsp;<input type="text" id="jump" size="2" value=""/>&nbsp;<a href="javascript:void(0)" onclick="spiltPage('${getActionName}',-1)">转到</a>
				    </span>
				    </div>
				    <input type="hidden" id="countPage"  value="${pageBean.pages }"/>
					<input type="hidden" id="pageNums"  value="${pageBean.pageNum }"/>
           	    </div>
                </div>
		      </div>
        	</div>
		</div>
	</div>
 	<jsp:include page="/jsp/leftMenu.jsp"></jsp:include>
<div class="footer"></div>
</div>



</body></html>