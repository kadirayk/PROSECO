class StrategyLog {
	
	constructor(strategyName, prototypeName, sysOut, sysErr, sysAll, showLog) {
		this.strategyName = strategyName;
		this.prototypeName = prototypeName;
		this.sysOut = sysOut;
		this.sysErr = sysErr.replace(/\$\_\(/g, '<span style="color:red">').replace(/\)\_\$/g, "</span>");
		this.sysAll = sysAll.replace(/\$\_\(/g, '<span style="color:red">').replace(/\)\_\$/g, "</span>");
		this.showLog = showLog;
		this.collapseStrategy = false;
	}

}

ProsecoApp.controller('ProsecoAppController', ['$scope', '$http', '$timeout', '$interval', '$location', function($scope, $http, $timeout, $interval, $location){
	var self = this;
	
	this.debugTableFlag = false;
	this.prosecoStatus = "domain";
	this.processID = "NaN";
	this.logList = [];
	this.result = {
			"remainingTime": null,
			"serviceHandle": null,
			"isComplete": false
	};
	this.autoScroll = true;
	this.showConsoles = true;
	
	this.getShowConsoles = function() {
		return this.showConsoles;
	}
	
	this.toggleShowConsoles = function() {
		this.showConsoles = !this.showConsoles;
	}

	
	this.isRemainingTimeNull = function() {
		return this.result.remainingTime == null;
	}
	
	this.isRemainingTimePositive = function() {
		return this.result.remainingTime > 0;
	}
	
	this.getRemainingTime = function() {
		return this.result.remainingTime;
	}
	
	this.getServiceHandle = function() {
		return this.result.serviceHandle;
	}
	
	this.getIsComplete = function() {
		return this.result.isComplete;
	}
	
	this.showDebugTable = function() {
		return this.debugTableFlag;
	};
	
	this.setDebugTableFlag = function(newFlag) {
		this.debugTableFlag = newFlag;
	};
	
	this.getLogList = function() {
		return this.logList;
	};
	
	this.getCurrentMessage = function() {
		return this.currentMessage;
	}
	
	this.collapseStrategy = function(strategyName) {
		for(let x in this.logList) {
			if(this.logList[x].strategyName === strategyName) {
				this.logList[x].collapseStrategy = !this.logList[x].collapseStrategy;
			}
		}
	}
	
	this.toggleAutoScroll = function() {
		this.autoScroll = !this.autoScroll;
	}
	
	this.showStrategyLog = function(strategyName, logName) {
		for(let x in this.logList) {
			if(this.logList[x].strategyName === strategyName) {
				this.logList[x].showLog = logName;
			}
		}
		
    	if(self.autoScroll) {
    		$timeout(function() {
        		self.doAutoScroll();
    		},250);
    	}
	};
	
	this.currentOutput = function(strategyName) {
		for(let x in this.logList) {
			if(this.logList[x].strategyName === strategyName) {
				let outputMode = this.logList[x].showLog;
				
				if(outputMode === "all") {
					return "Console: All <span class=\"caret\"></span>";
				} else if (outputMode === "out") {
					return "Console: System.out <span class=\"caret\"></span>";
				} else {
					return "Console: System.err <span class=\"caret\"></span>";
				}
			}
		}
	}
	
	this.getShowLog = function(strategyName) {
		for(let x in this.logList) {
			if(this.logList[x].strategyName === strategyName) {
				return this.logList[x].showLog;
			}
		}
	};
	
	this.showLog = function(expected, actual) {
		return expected === actual;
	};
	
	this.getProsecoStatus = function(cb = 'NaN') {
		var urlToCall = '/api/process/'+self.processID+'/status';
		$http({method: 'GET', url: urlToCall}).then(function successCallback(response) {
			if(response.data.status !== null) {
				self.prosecoStatus = response.data.status;
			} else {
				self.prosecoStatus ="domain";
			}
			
			if(cb !== "NaN") {
				cb();
			}
		}, function errorCallback(response) {
			self.prosecoStatus = "domain";
			if(cb !== "NaN") {
				cb();
			}
    	});
	};
	
	this.getProcessID = function(cb = 'NaN') {
		var url = $location.$$absUrl;
		if(url.split("/").length == 5) {
			self.processID = url.split("/")[4];
		}
		if(cb !== "NaN") {
			cb();
		}
	};
	
	this.showChart = function() {
		return this.prosecoStatus === "search";
	};
	
	this.showInterview = function() {
		return this.prosecoStatus === "interview";
	};
	
	this.statusClassFor = function(id) {
		order = ["domain", "interview","search","grounding","deployment","done"];
		index = order.indexOf(id);
		currentIndex = order.indexOf(this.prosecoStatus);
		if(index < currentIndex) {
			return "statusbar-step-done";
		}
		if(index === currentIndex) {
			return "statusbar-step-active";
		}
		
		return "statusbar-step-todo";
	};
	
	this.statusClassForArrow = function(id) {
		order = ["interview","search","grounding","deployment","done"];
		index = order.indexOf(id);
		currentIndex = order.indexOf(this.prosecoStatus);
		if(index <= currentIndex) {
			return "statusbar-step-next-done";
		}
		return "";
	};
	
	this.pullResult = function() {
		var urlToCall = '/api/result/'+self.processID;
		$http({method: 'GET', url: urlToCall}).then(function successCallback(response) {
			self.result.isComplete = response.data.isComplete == "true";
			self.result.serviceHandle = response.data.serviceHandle;
			self.result.remainingTime = parseInt(response.data.remainingTime);
			
			if(self.result.isComplete) {
				self.showConsoles = false;
			}
		}, function errorCallback(response) {
			console.log("Could not pull result from server");
    	});
	}

	this.getLogs = function() {
		var urlToCall = '/api/strategyLogs/'+self.processID;
		$http({method: 'GET', url: urlToCall}).then(function successCallback(response) {
			let data = response.data;
			if(data.logList !== null) {
				for(let x in data.logList) {
					let logData = data.logList[x];
					let strategyLog = new StrategyLog(logData.strategyName, logData.prototypeName, logData.systemOutLog, logData.systemErrorLog, logData.systemAllLog, "all");
					let updated = false;

					for(let logIx in self.logList) {
						if(self.logList[logIx].strategyName === strategyLog.strategyName) {
							self.logList[logIx].sysOut = strategyLog.sysOut;
							self.logList[logIx].sysErr = strategyLog.sysErr;
							self.logList[logIx].sysAll = strategyLog.sysAll;
							updated = true;
						}
					}
					if(!updated) {
						self.logList.push(strategyLog);
					}
				}
				
			} else {
				console.log("Could not pull log list from server");
				self.logList = [];
			}
		}, function errorCallback(response) {
			console.log("Could not pull log list from server");
			self.logList = [];
    	});
	};
	
	this.getAutoScroll = function() {
		return this.autoScroll;
	}
	
	this.doAutoScroll = function() {
		let logModes = ["all", "out", "err"];
		for(x in this.logList) {
			let strategyName = this.logList[x].strategyName;
			
			for(y in logModes) {
				let logName = logModes[y];
				let activeLogID = "#" + strategyName + "-" + logName;
				$(activeLogID).scrollTop($(activeLogID)[0].scrollHeight);		
			}
			
		}
	}
	
    $timeout(function() {
		self.getProcessID(self.getProsecoStatus);
    });
    
    
    $interval(function() {
    	if(self.prosecoStatus !== "done") {
    		self.getProsecoStatus();
    	}
    	if(self.prosecoStatus === "search") {
    		self.getLogs();
    	}
    	if(self.autoScroll) {
    		$timeout(function() {
        		self.doAutoScroll();
    		},250);
    	}
    }, 3000);
    
    $interval(function() {
    	if(!self.result.isComplete && (self.prosecoStatus === "search" || self.prosecoStatus === "grounding" || self.prosecoStatus === "deployment" || self.prosecoStatus === "done")) {
    		self.pullResult();
    	}
    }, 1000);
}]);