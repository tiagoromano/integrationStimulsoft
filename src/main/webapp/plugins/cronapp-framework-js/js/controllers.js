(function($app) {
  angular.module('custom.controllers', []);

  app.controller('HomeController', function($controller, $scope, $http, $rootScope, $state, $translate, Notification, ReportService, UploadService) {

    $rootScope.http = $http;
    $rootScope.Notification = Notification;
    $rootScope.UploadService = UploadService;

    $rootScope.getReport = function(reportName, params) {
      ReportService.openReport(reportName, params);
    }

    app.registerEventsCronapi($scope, $translate);

    for( var x in app.userEvents)
      $scope[x] = app.userEvents[x].bind($scope);

    $scope.message = {};

    try { $controller('AfterHomeController', { $scope: $scope }); } catch(e) {};
    try { if ($scope.blockly.events.afterHomeRender) $scope.blockly.events.afterHomeRender(); } catch(e) {};
  });
}(app));