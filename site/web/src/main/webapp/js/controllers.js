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
        }, 'http://localhost:8989/_ah/api');
    };
});

appEngineTckApp.controller('TestReportsCtrl', function($scope) {

    $scope.showFullTreeMapTooltip = function(row, size) {
        var methodName = $scope.selectedTestReportChart.data.rows[row].c[0].v;
        var correlation = $scope.selectedTestReportChart.data.rows[row].c[3].v;

        var html = '<div style="background:#fd9; padding:10px; border-style:solid">' +
            '<span style="font-family:Courier"><b>' + methodName + '</b></span><br/>';

        if ( correlation !== 0 ) {
            var errorInfo = $scope.errorByClass[methodName];
            if ( errorInfo === undefined ) {
                html += 'Log unavailable';
            }
            else {
                html += 'Log : ' + errorInfo;
            }
        }
        else {
            html += 'Number of ' + $scope.selectedTestReportChart.testType + ' : ' + size;
        }
        html += '</div>';
        return html;
    };

    $scope.basicParamChart = {
        displayed: true,
        cssStyle: 'height: 500px;'
    };

    $scope.basicParamTreemapChart = {
        __proto__: $scope.basicParamChart,
        type: 'TreeMap',
        data: {
            cols: [
                {
                    id: "testedPackage",
                    label: "Tested Package",
                    type: "string"
                },
                {
                    id: "parent",
                    label: "Parent",
                    type: "string"
                },
                {
                    id: "nbFails",
                    label: "Nb fails",
                    type: "number"
                },
                {
                    id: "correlation",
                    label: "Correlation",
                    type: "number"
                }
            ],
            rows: [
            ]
        },
        drillDownLevel: 1
    };

    $scope.selectedTestReportTreemapChartFailedTests = {
        __proto__: $scope.basicParamTreemapChart,
        options: {
            fill: 20,
            displayExactValues: true,
            chartArea: {
                width: '90%',
                height: '90%'
            },
            minColor: '#FFCC99',
            midColor: '#FF6600',
            maxColor: '#FF0000',
            generateTooltip: $scope.showFullTreeMapTooltip
        },
        testType: 'failed'
    };

    $scope.selectedTestReportTreemapChartIgnoredTests = {
        __proto__: $scope.basicParamTreemapChart,
        options: {
            fill: 20,
            displayExactValues: true,
            chartArea: {
                width: '90%',
                height: '90%'
            },
            minColor: '#FFFF66',
            midColor: '#FFCC00',
            maxColor: '#FF9900',
            generateTooltip: $scope.showFullTreeMapTooltip
        },
        testType: 'ignored'
    };

    $scope.selectedTestReportPieChart = {
        __proto__: $scope.basicParamChart,
        type: 'PieChart',
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

    $scope.selectedTestReportChart = $scope.selectedTestReportPieChart;

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

        if (drillDownLevel === 1 && selectedItem.row === 0) {
            $scope.selectedTestReportChart = $scope.selectedTestReportPieChart;
            $scope.selectedTestReportChart.data.rows = testReportToPieChartRows($scope.selectedTestReport);
        }
        else if (drillDownLevel === 0 && selectedItem.row === 1 && $scope.selectedTestReport.failedTests !== undefined) {
            $scope.selectedTestReportChart = $scope.selectedTestReportTreemapChartFailedTests;
            $scope.selectedTestReportChart.data.rows = testReportToTreemapChartRows($scope.selectedTestReport.failedTests);
        }
        else if (drillDownLevel === 0 && selectedItem.row === 2 && $scope.selectedTestReport.ignoredTests !== undefined) {
            $scope.selectedTestReportChart = $scope.selectedTestReportTreemapChartIgnoredTests;
            $scope.selectedTestReportChart.data.rows = testReportToTreemapChartRows($scope.selectedTestReport.ignoredTests);
        }
    };

    $scope.lastTestReportsChartSelect = function(selectedItem) {
        $scope.selectedTestReport = $scope.testReports[selectedItem.row];
    };

    $scope.$watch('selectedTestReport', function(newValue, oldValue) {
        if (newValue !== oldValue) {
            $scope.selectedTestReportChart = $scope.selectedTestReportPieChart;
            $scope.selectedTestReportChart.data.rows = testReportToPieChartRows(newValue);
        }
    });

    $scope.$watch('testReports', function(newValues, oldValues) {
        if (newValues !== oldValues) {
            $scope.originalData = newValues;

            $scope.sliderValues = {
                from: 0,
                to: newValues.length - 1
            };

            $scope.buildMin = $scope.sliderValues.from;
            $scope.buildMax = $scope.sliderValues.to;

            $scope.displayFromTo( $scope.sliderValues );
        }
    });

    $scope.$watchCollection('sliderValues', function( newSliderValues ) {
        if (newSliderValues !== undefined) {
            $scope.displayFromTo( newSliderValues );
        }
    });

    $scope.displayFromTo = function(newSliderValues, oldSliderValues) {
        if ( newSliderValues !== oldSliderValues ) {
            var newRows = [];
            for (var i = $scope.originalData.length - newSliderValues.to - 1; i < $scope.originalData.length - newSliderValues.from; i++) {
                newRows.push({
                    c: [
                        { v: $scope.originalData[i].buildId },
                        { v: $scope.originalData[i].numberOfPassedTests },
                        { v: $scope.originalData[i].numberOfFailedTests },
                        { v: $scope.originalData[i].numberOfIgnoredTests }
                    ]
                });
            }
            $scope.lastTestReportsChart.data.rows = newRows;
            var hAxis = $scope.lastTestReportsChart.options.hAxis;
            hAxis.gridlines.count = ($scope.originalData[$scope.originalData.length - newSliderValues.to - 1].buildId - $scope.originalData[$scope.originalData.length - newSliderValues.from - 1].buildId) + 1;

            $scope.sliderValues = newSliderValues;
        }
    };

    $scope.init = function(buildTypeId) {
        $scope.toTestAvailable = true;
        $scope.loading = true;
        gapi.client.reports.tests.
            list({ buildTypeId: buildTypeId, limit: 10 }).
            execute(function(resp) {
                if (angular.isArray(resp.items) && resp.items.length > 0) {
                    $scope.$apply(function($scope) {
                        $scope.testReports = resp.items;
                        $scope.selectedTestReport = resp.items[0];
                    });
                    $scope.loading = false;
                }
                $scope.toTestAvailable = !$scope.loading;
                $scope.loading = false;
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

    var testReportToTreemapChartRows = function(report) {

        var addedPackages = [];
        var addedClasses = [];
        var addedMethods = [];

        var packagesRow = [];
        var classesRow = [];
        var methodsRow = [];

        var failedTestsByClassName = _.countBy(report, function(failedTest){ return failedTest.className; });
        $scope.errorByClass = [];

        report.forEach( function(test) {
            methodsRow.push(
                {
                    c: [
                        { v: ( $.inArray(test.methodName, addedMethods) === -1 ) ? test.methodName : test.methodName + ' (' + test.className + ')' },
                        { v: test.className },
                        { v: 1 },
                        { v: failedTestsByClassName[test.className] }
                    ]
                }
            );
            addedMethods.push( test.methodName );
            $scope.errorByClass[test.methodName] = test.error;

            if ( $.inArray(test.className, addedClasses) === -1 ) {
                classesRow.push(
                    {
                        c: [
                            { v: test.className },
                            { v: test.packageName },
                            { v: 0 },
                            { v: 0 }
                        ]
                    }
                );
                addedClasses.push( test.className );
            }

            if ( $.inArray(test.packageName, addedPackages) === -1 ) {
                packagesRow.push(
                    {
                        c: [
                            { v: test.packageName },
                            { v: 'Global' },
                            { v: 0 },
                            { v: 0 }
                        ]
                    }
                );
                addedPackages.push( test.packageName );
            }
        });

        var baseRow = [
            {
                c: [
                    { v: 'Global' },
                    { v: null },
                    { v: 0 },
                    { v: 0 }
                ]
            }
        ];

        return baseRow.concat( packagesRow, classesRow, methodsRow );
    };
});