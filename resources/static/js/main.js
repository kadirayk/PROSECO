$(document).ready(function() {
    checkResult();
	pollSys();
	timerSys = setTimeout(pollSys, 500);

});


// bind to Server-Sent Event Emitter to check process result 
function checkResult(){
	if(typeof(EventSource) !== "undefined") {
		currentUrl = window.location.href.split("/").pop();
	    var source = new EventSource("/api/result/" + currentUrl);
	    source.onmessage = function(event) {
	        document.getElementById("strategy-result").innerHTML = event.data;
	    };
	} else {
	    document.getElementById("strategy-result").innerHTML =
	                     "Your browser does not support server-sent events.";
	}
}

var pollButton = document.getElementById("btn-poll");

pollButton.addEventListener("click", Stop);

var pollStatus = "start";

// start console out
function Start() {
	console.log("start!!");
	pollButton.removeEventListener("click", Start);
	pollButton.addEventListener("click", Stop);
	pollButton.value = "Stop Console";
	pollSys();
	timerSys = setTimeout(pollSys, 500);
	pollStatus = "start";
}

// stop console out
function Stop() {
	console.log("stop!!");
	pollButton.removeEventListener("click", Stop);
	pollButton.addEventListener("click", Start);
	pollButton.value = "Start Console";
	if (timerSys) {
		clearTimeout(timerSys);
		timerSys = 0;
	}
	pollStatus = "stop";
}

// Stop System Log caller
function StopSys() {
	if (timerSys) {
		clearTimeout(timerSys);
		timerSys = 0;
	}
}

// Stop Created Prototype Service
function StopService() {
	currentUrl = window.location.href.split("/").pop();
	$
			.ajax({
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
}

// Poll System Logs
function pollSys() {
	currentUrl = window.location.href.split("/").pop();
	$
			.ajax({
				type : "GET",
				contentType : "application/json",
				url : "/api/log/" + currentUrl,
				cache : false,
				timeout : 60000,
				success : function(data) {

					var field = "<div class=\"container\">";
					for ( var logPair in data['logList']) {
						field = field
								+ "<div class=\"row\">"
								+ "<div class=\"col-xs-6\">"
								+ data['logList'][logPair]['strategyName']
								+ " System Out"
								+ "<div class=\"pre-scrollable\" style=\"min-height: 200px; max-height: 200px; max-width: 600px;\">"
								+ "<pre>"
								+ data['logList'][logPair]['systemOutLog']
								+ "</pre>"
								+ "</div> </div>"
								+ "<div class=\"row\">"
								+ "<div class=\"col-xs-6\">"
								+ data['logList'][logPair]['strategyName']
								+ " System Error"
								+ "<div class=\"pre-scrollable\" style=\"min-height: 200px; max-height: 200px; max-width: 600px;\">"
								+ "<pre>"
								+ data['logList'][logPair]['systemErrorLog']
								+ "</pre>" + "</div> </div> </div> </div>"

					}

					$('#feedback').html(field);


					// set scroll bar to the bottom
					$('.pre-scrollable').scrollTop(1E10);
				},
				error : function(e) {

					var json = "<h4>Ajax Response</h4><pre>" + e.responseText
							+ "</pre>";
					$('#feedback_sys').html(json);

					console.log("ERROR : ", e);

				}
			});
	if (pollStatus === "start") {
		timerSys = setTimeout(pollSys, 800);
	}
}
