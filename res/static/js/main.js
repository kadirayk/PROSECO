$(document).ready(function() {
	checkResult();
	listenStrategyLogs();
	/* SendResolution(); */
});

var strategyLogSource;
var resultSource;

var timeoutAsked;

// bind to Server-Sent Event Emitter to check process result
function checkResult() {
	if (typeof (EventSource) !== "undefined") {
		currentUrl = window.location.href.split("/").pop();
		resultSource = new EventSource("/api/result/" + currentUrl);
		resultSource.onmessage = function(event) {
//			if (!timeoutAsked && event.data.includes('.')) {
//				if (!confirm("Time out reached do you want to continue?")) {
//					document.getElementById("strategy-result").innerHTML = "Canceled";
//					StopService();
//					resultSource.close();
//					strategyLogSource.close();
//				} else {
//					timeoutAsked = true;
//					document.getElementById("strategy-result").innerHTML = event.data;
//				}
//			} else {
			console.log(event.data);
			if (false) {
				$("#btn-stop-service").css("display", "");
			}
				document.getElementById("strategy-result").innerHTML = event.data;
//			}
		};
	} else {
		document.getElementById("strategy-result").innerHTML = "Your browser does not support server-sent events.";
	}
}

function listenStrategyLogs() {
	if (typeof (EventSource) !== "undefined") {
		currentUrl = window.location.href.split("/").pop();
		strategyLogSource = new EventSource("/api/strategyLogs/" + currentUrl);
		strategyLogSource.onmessage = function(event) {
			
			/* when new log information arrives, reset respective DIV */
			var htmlContent = "";
			const data = JSON.parse(event.data);
			
			/* now create the HTML for the console boxes */
			for ( var logPair in data['logList']) {
				var strategyName = data['logList'][logPair]['strategyName'];
				
				/* memorize checked log-boxes */
				var checkedBox;
				$("#strategy-log-" + strategyName + " input:checked").each(function() {
					checkedBox = $(this).attr("id");
				});
				
				/* derive the (formatted) string for the joint log and the error log respectively */
				var allLog = data['logList'][logPair]['systemAllLog'];
				allLog = allLog.replace(/\$\_\(/g, '<span style=\"color:red\">');
				allLog = allLog.replace(/\)\_\$/g, '</span>');
				var errLog = data['logList'][logPair]['systemErrorLog'];
				errLog = errLog.replace(/\$\_\(/g, '<span style=\"color:red\">');
				errLog = errLog.replace(/\)\_\$/g, '</span>');
				
				/* define HTML for the console box */
				var thisBox = $(
					"<div><div id=\"strategy-log-" + strategyName + "\" class=\"container strategy-log\">" +
					"  <h2>" + strategyName + "</h2>" + 
					"  <div class=\"tabs\">" +
					"    <input name=\"tabs-" + strategyName + "-strategy\" type=\"radio\" id=\"tab-" + strategyName + "-strategy-merged\" class=\"input\" />" + 
					"    <label for=\"tab-" + strategyName + "-strategy-merged\" class=\"label\">Merged</label>" + 
					"    <div class=\"panel console-box\">" +
					allLog +
					"    </div>" +
					"    <input name=\"tabs-" + strategyName + "-strategy\" type=\"radio\" id=\"tab-" + strategyName + "-strategy-out\" class=\"input\" />" +
					"    <label for=\"tab-" + strategyName + "-strategy-out\" class=\"label\">System.out</label>" +
					"    <div class=\"panel console-box\">" +
					data['logList'][logPair]['systemOutLog'] +
					"    </div>" +
					"    <input name=\"tabs-" + strategyName + "-strategy\" type=\"radio\" id=\"tab-" + strategyName + "-strategy-error\" class=\"input\" />" +
					"    <label for=\"tab-" + strategyName + "-strategy-error\" class=\"label\">System.err</label>" +
					"    <div class=\"panel console-box\">" +
					errLog +
					"    </div>" +
					"  </div>" +
					"  <br style=\"clear: left;\" />" +
					"</div></div>");
				
				/* automatically check the currently active box */
				if (checkedBox == undefined)
					$("input:first", thisBox).attr("checked", "checked");
				else
					$("#" + checkedBox, thisBox).attr("checked", "checked");
				
				/* append html */
				htmlContent += thisBox.html();
			}
			
			/* inject the HTML into the corresponding div-tag */
			$('#strategy-logs').html(htmlContent);
			/*for (var i = 0, len = tabArray.length; i < len; i++) {
				$('.nav-tabs a[href="' + tabArray[i] + '"]').tab('show');
			}
			*/

			// this only scrolls the active ones!
			$('.console-box').scrollTop(1E10);
		};
	} else {
		document.getElementById("strategy-result").innerHTML = "Your browser does not support server-sent events.";
	}
}

var strategyLogListenerButton = document
		.getElementById("btn-strategy-log-listener");

strategyLogListenerButton.addEventListener("click", stopListeningStrategyLogs);

// start console out
function startListeningStrategyLogs() {
	strategyLogListenerButton.removeEventListener("click",
			startListeningStrategyLogs);
	strategyLogListenerButton.addEventListener("click",
			stopListeningStrategyLogs);
	strategyLogListenerButton.value = "Stop Console";
	listenStrategyLogs();
}

// stop console out
function stopListeningStrategyLogs(event) {
	strategyLogListenerButton.removeEventListener("click",
			stopListeningStrategyLogs);
	strategyLogListenerButton.addEventListener("click",
			startListeningStrategyLogs);
	strategyLogListenerButton.value = "Start Console";
	strategyLogSource.close();
}

// Stop Created Prototype Service
function StopService() {
	currentUrl = window.location.href.split("/").pop();
	$.ajax({
		type : "GET",
		contentType : "application/json",
		url : "/api/stopService/" + currentUrl,
		cache : false,
		timeout : 60000,
		success : function(data) {
			console.log(data);
		},
		error : function(e) {
			console.log("ERROR : ", e);
		}
	});
	document.getElementById("strategy-result").innerHTML = "Canceled";
	resultSource.close();
	strategyLogSource.close();
}

function SendResolution() {
	var screenHeight = window.screen.height;
	var screenWidth = window.screen.width;

	currentUrl = window.location.href.split("/").pop();
	$.ajax({
		type : "GET",
		contentType : "application/json",
		url : "/api/sendResolution/" + currentUrl + "?height=" + screenHeight
				+ "&width=" + screenWidth,
		cache : false,
		timeout : 60000,
		success : function(data) {
			console.log(data);
		},
		error : function(e) {
			console.log("ERROR : ", e);
		}
	});
}

var tabArray = []

$(document).on('shown.bs.tab', function(e) {
	console.log(e.target.hash);
	tabArray.push(e.target.hash);
});