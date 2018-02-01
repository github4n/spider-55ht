function editTask(taskId) {
	window.location.href = "gotoEditTaskPage.action?taskId=" + taskId;
}

function deleteTask(taskId) {
	if(confirm("确定要删除该任务吗?")){
		$.post("findTaskById.action",{taskId:taskId},function(data){
			if(data.status=='I' || data.status=='V'){
				window.location.href = "deleteTask.action?id=" + taskId;
			} else {
				alert("抱歉，该任务在当前状态下不可删除！");
			}
		});
	}
}

function viewTask(taskId) {
	window.location.href = "viewTask.action?id=" + taskId;
}

function gotoImportSeeds(taskId) {
	window.location.href = "gotoImportSeeds.action?taskId=" + taskId;
}

function startupTask(taskId) {
	window.location.href = "startupTask.action?taskId=" + taskId;
}

function pauseTask(taskId){
	window.location.href = "pauseTask.action?taskId=" + taskId;
}

function recoverTask(taskId){
	window.location.href = "recoverTask.action?taskId=" + taskId;
}

function restartTask(taskId){
	window.location.href = "restartTask.action?taskId=" + taskId;
}

function discardTask(taskId){
	window.location.href = "discardTask.action?taskId=" + taskId;
}

function runWait() {
	if(confirm("确定要暂停所有运行中的任务吗?")){
		window.location.href = "runWait.action";
	}
}

function rouse() {
	if(confirm("确定要恢复一键休眠暂停的任务吗?")){
		window.location.href = "rouse.action";
	}
}