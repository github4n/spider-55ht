var page = require("webpage").create();
page.viewportSize = {
	width : 1366,
	height : 600
};

var system = require("system");

if (system.args.length !== 7) {
	console.log("There should be 7 arguments!!!");
	phantom.exit();
} else {
	var url = system.args[2];
	var username = system.args[4];
	var password = system.args[6];
	
	page.customHeaders = {
		"User-Agent":"Mozilla/5.0 (Macintosh; Intel Mac OS X 10.9; rv:36.0) Gecko/20100101 Firefox/36.0 WebKit"
	};

	page.open(url, function(status) {
	    page.evaluate(function(parameter) {
	    	document.getElementById("ap_email").value = parameter.split("#")[0];
	    	document.getElementById("ap_password").value = parameter.split("#")[1];
	    	document.getElementById("signInSubmit-input").click();
	    }, username + "#" + password);

	    setTimeout("clickMyAccountButton()",5000);
	});	
}

function clickMyAccountButton(){
	page.render("login_6pm_with_values11111.png");
	page.evaluate(function(){
	    	document.querySelector("a[title='Account']").click();
	});

	setTimeout("print_screen()",5000);
}

function print_screen(){
	page.render("login_6pm_with_values22222.png");
	console.log("Login OK!");
    phantom.exit();
}