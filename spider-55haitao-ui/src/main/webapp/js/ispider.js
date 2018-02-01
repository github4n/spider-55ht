// JavaScript Document
$(function() {
	var clicknum = 0;
	$('a,input[type="button"],input[type="submit"]').bind('focus', function() {
		if (this.blur) {
			this.blur();
		}
		;
	});
	$('.nav ul li').click(function() {
		$(this).addClass('current').siblings().removeClass('current');
	});
	$('#startDate').datepicker();
	$('#endDate').datepicker();
	$('.tips').tipsy();
	$('.search-input')
			.keyup(
				function(event) {
					$(this)
							.parent()
							.append(
									"<div class=searchlist><ul><li>äº¬ä¸åå</li><li>äº¬ä¸åå</li><li>å½å½</li><li>æ·å®</li></ul></div>");
					$('.searchlist li').click(function() {
						var txt = $(this).text();
						$('.search-input').val(txt);
					});
					return false;
				});

	$('body').click(function() {
		$('.searchlist').remove();
	});

/*	$('#myTable').tablesorter({
		sortList : [ [ 0, 0 ], [ 1, 0 ], [ 5, 0 ] ],
		locale : 'de',
		widgets : [ 'zebra' ],
		useUI : true
	});*/
	$('#tasklist').hide();
	$('.table-info tr .tasknum').click(function() {
		if (clicknum == 0) {
			$("#tasklist").show(200);
			$(this).parent().parent().addClass('ative');
			clicknum = 1;
		} else {
			$("#tasklist").hide(200);
			$(this).parent().parent().removeClass('ative');
			clicknum = 0;
		}
	});
});

function selectDate(){
	datepicker();
}

//-->