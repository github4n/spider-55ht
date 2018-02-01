var page = require("webpage").create();
page.viewportSize = { width: 1366, height: 600 };

var url="https://account.xiaomi.com/pass/serviceLogin";

page.open(url, function() {
    ret=page.evaluate(function() {
	    document.getElementById("username").value = "13681711786";
	    document.getElementById("pwd").value = "147258369lsz";
	    document.getElementById("login-button").click();
        });
    setTimeout("open_person_info()",5000);
});

function open_person_info(){
    page.render("login-xiaomi1.png");
    page.evaluate(function(){
    	document.querySelector("a[title='个人信息']").click();// 貌似在本页面可以打开并取到,但是新开页面(target="_blank")的就不行了
    });
    
    setTimeout("open_bind_auth()",5000);
}

function open_bind_auth(){
    page.render("login-xiaomi2.png");
    page.evaluate(function(){
    	document.querySelector("a[title='绑定授权']").click();// 貌似在本页面可以打开并取到,但是新开页面(target="_blank")的就不行了
    });
    
    setTimeout("open_xiaomi_service()",5000);
}

function open_xiaomi_service(){
    page.render("login-xiaomi3.png");
    page.evaluate(function(){
    	document.querySelector("a[title='小米服务']").click();// 貌似在本页面可以打开并取到,但是新开页面(target="_blank")的就不行了
    });
    
    setTimeout("login_out()",5000);
}

function login_out(){
    page.render("login-xiaomi4.png");
    page.evaluate(function(){
    	document.getElementById("logoutLink").click();// 貌似在本页面可以打开并取到,但是新开页面(target="_blank")的就不行了
    });
    
    setTimeout("login_again()",5000);
    
}

function login_again(){
    page.render("login-xiaomi5.png");
    page.evaluate(function(){
    	document.getElementById("username").value = "13681711786";
    	document.getElementById("pwd").value = "147258369lszhaha";
    	document.getElementById("login-button").click();// 貌似在本页面可以打开并取到,但是新开页面(target="_blank")的就不行了
    });
    
    setTimeout("print_login_again_error()",5000);
    
}

function print_login_again_error(){
    page.render("login-xiaomi6.png");

    var resultOutter = page.evaluate(function(){
    	var resultInner = document.querySelector("span[class='error-con']").innerText;
    	return resultInner;
    });

    console.log(resultOutter);

    phantom.exit();
}