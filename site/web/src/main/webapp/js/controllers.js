/**
 * Angular JS controllers.
 * Copyright 2013 Google Inc. All Rights Reserved
 * @author <a href="mailto:kevin.pollet@serli.com">Kevin Pollet</a>
 */
function init() {
    window.initGoogleApis();
}

appEngineTckApp.controller('HeaderCtrl', function($scope, $location) {
    $scope.bool = true;

    $scope.isActive = function(path) {
        return $location.path() === path;
    };
});

//TODO should be configured externally!
appEngineTckApp.controller('ReportsCtrl', function($scope, $window) {
    $scope.buildTypes = [
        {
            id: 'AppEngineTck_Capedwarf',
            label: 'CapeDwarf'
        },
        {
            id: 'GaeJavaSdk',
            label: 'GAE Java SDK'
        }
    ];

    $window.initGoogleApis = function() {
        gapi.client.load('reports', 'v1', function() {
            $scope.$apply(function($scope) {
                $scope.isGoogleApisLoaded = true;
            });
        }, 'https://cloud-test-compatibility-kit.appspot.com/_ah/api');
    };
});

appEngineTckApp.controller('TestReportsCtrl', function($scope) {
    $scope.selectedTestReportChart = {
        type: 'PieChart',
        displayed: true,
        cssStyle: 'height: 500px;',
        data: {
            cols: [
                {
                    id: 'type-id',
                    label: 'Type',
                    type: 'string'
                },
                {
                    id: 'number-id',
                    label: 'Number',
                    type: 'number'
                }
            ],
            rows: [
            ]
        },
        options: {
            fill: 20,
            displayExactValues: true,
            chartArea: {
                width: '90%',
                height: '90%'
            }
        },
        drillDownLevel: 0
    };

    $scope.lastTestReportsChart = {
        type: 'AreaChart',
        displayed: true,
        cssStyle: 'height: 500px;',
        data: {
            cols: [
                {
                    id: 'build-id',
                    label: 'Build Id',
                    type: 'number'
                },
                {
                    id: 'passed-number-id',
                    label: 'Passed',
                    type: 'number'
                },
                {
                    id: 'failed-number-id',
                    label: 'Failed',
                    type: 'number'
                },
                {
                    id: 'ignored-number-id',
                    label: 'Ignored',
                    type: 'number'
                }
            ],
            rows: [
            ]
        },
        options: {
            fill: 20,
            hAxis: {
                title: 'Id of the build',
                viewWindowMode: 'maximized',
                gridlines: {
                    count: 5
                }
            },
            vAxis: {
                title: 'Number of tests'
            },
            pointSize: 8,
            displayExactValues: true,
            isStacked: true
        }
    };

    $scope.selectedTestReportChartSelect = function(selectedItem) {
        var drillDownLevel = $scope.selectedTestReportChart.drillDownLevel;

        if (drillDownLevel === 1) {
            $scope.selectedTestReportChart.data.rows = testReportToPieChartRows($scope.selectedTestReport);
            $scope.selectedTestReportChart.drillDownLevel--;
        }
        else if (drillDownLevel === 0 && selectedItem.row === 1) {
            $scope.selectedTestReportChart.data.rows = [];
            var failedTestsByClassName = _.countBy($scope.selectedTestReport.failedTests, function(failedTest){ return failedTest.className; });

            for (className in failedTestsByClassName) {
                $scope.selectedTestReportChart.data.rows.push(
                    {
                        c: [
                            { v: className },
                            { v: failedTestsByClassName[className] }
                        ]
                    }
                );
            }

            $scope.selectedTestReportChart.drillDownLevel++;
        }
    };

    $scope.lastTestReportsChartSelect = function(selectedItem) {
        $scope.selectedTestReport = $scope.testReports[selectedItem.row];
    };

    $scope.$watch('selectedTestReport', function(newValue, oldValue) {
        if (newValue !== oldValue) {
            $scope.selectedTestReportChart.data.rows = testReportToPieChartRows(newValue);
            $scope.selectedTestReportChart.drillDownLevel = 0;
        }
    });

    $scope.$watch('testReports', function(newValues, oldValues) {
        if (newValues !== oldValues) {
            var hAxis = $scope.lastTestReportsChart.options.hAxis;
            hAxis.gridlines.count = (newValues[0].buildId - newValues[newValues.length - 1].buildId) + 1;

            for(var i = 0; i < newValues.length; i++) {
                $scope.lastTestReportsChart.data.rows.push({
                    c: [
                        { v: newValues[i].buildId },
                        { v: newValues[i].numberOfPassedTests },
                        { v: newValues[i].numberOfFailedTests },
                        { v: newValues[i].numberOfIgnoredTests }
                    ]
                });
            }
        }
    });

    $scope.init = function(buildTypeId) {
        gapi.client.reports.tests.
            list({ buildTypeId: buildTypeId, limit: 10 }).
            execute(function(resp) {
                if (angular.isArray(resp.items) && resp.items.length > 0) {
                    $scope.$apply(function($scope) {
                        $scope.testReports = resp.items;
                        $scope.selectedTestReport = resp.items[0];
                    });
                }
            });
    };

    var testReportToPieChartRows = function(report) {
        return [
            {
                c: [
                    { v: 'Passed' },
                    { v: report.numberOfPassedTests }
                ]
            },
            {
                c: [
                    { v: 'Failed' },
                    { v: report.numberOfFailedTests }
                ]
            },
            {
                c: [
                    { v: 'Ignored' },
                    { v: report.numberOfIgnoredTests }
                ]
            }
        ];
    };
});