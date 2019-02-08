/*

$(document).ready(function() {
	checkResult();
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

			if (false) {
				$("#btn-stop-service").css("display", "");
			}
				document.getElementById("strategy-result").innerHTML = event.data;
		};
	} else {
		document.getElementById("strategy-result").innerHTML = "Your browser does not support server-sent events.";
	}
}
*/

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

