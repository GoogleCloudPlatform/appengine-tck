/**
 * Angular JS application.
 * Copyright 2013 Google Inc. All Rights Reserved
 * @author <a href="mailto:kevin.pollet@serli.com">Kevin Pollet</a>
 */
var appEngineTckApp = angular.
    module('appEngineTckApp', ['ngRoute', 'googlechart']).
    config(['$routeProvider', function ($routeProvider) {
        $routeProvider.
            when('/', {
                templateUrl: 'partials/landingPage.html'
            }).
            when('/reports.html', {
                templateUrl: 'partials/appEngineTckReports.html',
                controller: 'ReportsCtrl'
            }).
            otherwise({ redirectTo: '/' });
    }]);
