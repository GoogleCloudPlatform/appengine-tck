/**
 * Angular JS application.
 * Copyright 2013 Google Inc. All Rights Reserved
 * @author <a href="mailto:kevin.pollet@serli.com">Kevin Pollet</a>
 */
var appEngineTckApp = angular.
    module('appEngineTckApp', ['ngRoute', 'googlechart', 'ui-rangeSlider']).
    filter('duration', function() {
        return function(input, unit) {
            var duration = moment.duration(parseInt(input), unit);
            return duration.hours() + 'h ' + duration.minutes() + 'm ' + duration.seconds() + 's';
        }
    }).
    config(['$routeProvider', function ($routeProvider) {
        $routeProvider.
            when('/', {
                templateUrl: 'partials/landingPage.html'
            }).
            when('/reports.html', {
                templateUrl: 'partials/appEngineTckReports.html',
                controller: 'ReportsCtrl'
            }).
            when('/coverage.html', {
                templateUrl: 'partials/appEngineTckCoverage.html'
            }).
            otherwise({ redirectTo: '/' });
    }]);
