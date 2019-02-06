var moduleList = [
	'chart.js',
	'ngSanitize'
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