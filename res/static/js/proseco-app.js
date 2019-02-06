var moduleList = [
	'chart.js'
];

var ProsecoApp = angular.module('ProsecoApp', moduleList);

ProsecoApp.service('ProsecoService', ['$scope', '$http', '$interval', function($scope, $http, $interval) {
	var self = this;

	this.processID = null;
	this.processState = "domain";
	
	this.getProcessState = function() {
		return this.processState;
	}
	
	this.getProcessID = function() {
		if(processID === null) {
			this.pullProcessID();
		}
		return this.procesID;
	}
	
	this.pullProcessID = function() {
		$http({method: 'GET', url: '/api/process/id'}).then(function successCallback(response) {
			if(response.data.processID !== null) {
				self.processID = response.data.processID;
			} else {
				self.processID ="";
			}
		}, function errorCallback(response) {
			self.processID = "";
    	});
	}

}]);

ProsecoApp.controller('ProsecoAppController', ['$scope', '$http', '$timeout', '$interval', '$location', function($scope, $http, $timeout, $interval, $location){
	var self = this;
	
	this.debugTableFlag = false;
	this.prosecoStatus = "domain";
	this.processID = "NaN";
	
	this.showDebugTable = function() {
		return this.debugTableFlag;
	}
	
	this.setDebugTableFlag = function(newFlag) {
		this.debugTableFlag = newFlag;
	}
	
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
	}
	
	this.showInterview = function() {
		return this.prosecoStatus === "interview";
	}
	
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
	}
	
	this.statusClassForArrow = function(id) {
		order = ["interview","search","grounding","deployment","done"];
		index = order.indexOf(id);
		currentIndex = order.indexOf(this.prosecoStatus);
		if(index <= currentIndex) {
			return "statusbar-step-next-done";
		}
		return "";
	}
	
	this.initializeBenchmarkScore = function() {
		var initialValues = [100,67,54,34,32,32,31,30,29,28,27,26,25,24.4];
		this.benchmarkValues.push(initialValues);
		
	}
	
	this.addBenchmarkScore = function() {
		this.labels.push("a");
		this.data[0].push(parseFloat(this.newBenchmarkScore));
		this.benchmarkScore.push(parseFloat(this.newBenchmarkScore));
	}
	
    $timeout(function() {
		self.getProcessID(self.getProsecoStatus);
    });
    
    
    $interval(function() {
    	if(self.prosecoStatus !== "done") {
    		self.getProsecoStatus();
    	}
    }, 1000);
    
    
}]);


ProsecoApp.controller('StrategyChartController', function($scope, $http, $timeout, $interval){
	var self = this;
	
  	this.series = ['Strategy'];
  	this.labels = [0, 1, 2, 3, 4, 5, 6];
  	this.data = [[65, 59, 80, 81, 56, 55, 40]];
  	
  	this.options = {
  		elements: {
  			line: {
  				tension: 0
  			}
  		},
    	scales: {
      		yAxes: [
        		{
          			id: 'y-axis-1',
          			type: 'linear',
          			display: true,
			        position: 'left',
			        ticks: {
			        	min: 0,
			        	max: 100
			        }
        		}
      		]
    	},
    	animation: {
    		duration: 0
    	}
	};
	
  	this.onClick = function (points, evt) {
    	console.log(points, evt);
  	};
  	
  	/*
  	$interval(function() {
  		self.labels.push(self.labels.length);
  		self.data[0].push(Math.random() * 100);
  	}, 1000); */
});

ProsecoApp.directive('strategyChart', function() {
	function link(scope, element, attrs) {
		
	}

	return {
		link: link,
		templateUrl: "strategy/strategyChart.html"
	};
});