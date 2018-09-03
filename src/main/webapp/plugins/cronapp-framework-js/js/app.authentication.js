var cronappModules = [
  'ui.router',
  'ui.select',
  'ui-select-infinity',
  'ngResource',
  'ngSanitize',
  'custom.controllers',
  'custom.services',
  'datasourcejs',
  'chart.js',
  'ngJustGage',
  'pascalprecht.translate',
  'tmh.dynamicLocale',
  'ui-notification',
  'ui.bootstrap',
  'ngFileUpload',
  'report.services',
  'upload.services',
  'summernote',
  'ui.tinymce'
];

if (window.customModules) {
  cronappModules = cronappModules.concat(window.customModules);
}

var app = (function() {

  return angular.module('MyApp', cronappModules)
      .constant('LOCALES', {
        'locales': {
          'pt_br': 'Portugues (Brasil)',
          'en_us': 'English'
        },
        'preferredLocale': 'pt_br',
        'urlPrefix': ''
      })
      .config([
        '$httpProvider',
        function($httpProvider) {
          var interceptor = [
            '$q',
            '$rootScope',
            function($q, $rootScope) {
              var service = {
                'request': function(config) {
                  var _u = JSON.parse(localStorage.getItem('_u'));
                  if (_u && _u.token) {
                    config.headers['X-AUTH-TOKEN'] = _u.token;
                    window.uToken = _u.token;
                  }
                  return config;
                }
              };
              return service;
            }
          ];
          $httpProvider.interceptors.push(interceptor);
        }
      ])
      .config(function($stateProvider, $urlRouterProvider, NotificationProvider) {
        NotificationProvider.setOptions({
          delay: 5000,
          startTop: 20,
          startRight: 10,
          verticalSpacing: 20,
          horizontalSpacing: 20,
          positionX: 'right',
          positionY: 'top'
        });

        if (window.customStateProvider) {
          window.customStateProvider($stateProvider);
        }
        else {
          // Set up the states
          $stateProvider
              .state('login', {
                url: "",
                controller: 'LoginController',
                templateUrl: 'views/login.view.html'
              })

              .state('social', {
                url: "/connected",
                controller: 'SocialController',
                templateUrl: 'views/login.view.html'
              })

              .state('socialError', {
                url: "/notconnected",
                controller: 'SocialController',
                templateUrl: 'views/login.view.html'
              })

              .state('main', {
                url: "/",
                controller: 'LoginController',
                templateUrl: 'views/login.view.html'
              })

              .state('publicRoot', {
                url: "/public/{name:.*}",
                controller: 'PageController',
                templateUrl: function(urlattr) {
                  return 'views/public/' + urlattr.name + '.view.html';
                }
              })

              .state('public', {
                url: "/home/public",
                controller: 'PublicController',
                templateUrl: function(urlattr) {
                  return 'views/public/home.view.html';
                }
              })

              .state('public.pages', {
                url: "/{name:.*}",
                controller: 'PageController',
                templateUrl: function(urlattr) {
                  return 'views/public/' + urlattr.name + '.view.html';
                }
              })

              .state('home', {
                url: "/home",
                controller: 'HomeController',
                templateUrl: 'views/logged/home.view.html'
              })

              .state('home.pages', {
                url: "/{name:.*}",
                controller: 'PageController',
                templateUrl: function(urlattr) {
                  return 'views/' + urlattr.name + '.view.html';
                }
              })

              .state('404', {
                url: "/error/404",
                controller: 'PageController',
                templateUrl: function(urlattr) {
                  return 'views/error/404.view.html';
                }
              })

              .state('403', {
                url: "/error/403",
                controller: 'PageController',
                templateUrl: function(urlattr) {
                  return 'views/error/403.view.html';
                }
              });
        }

        // For any unmatched url, redirect to /state1
        $urlRouterProvider.otherwise("/error/404");
      })
      .factory('originPath', ['$location', function($location) {
        var originPath = {
          request: function(config) {
            config.headers['origin-path'] = $location.path();
            return config;
          }
        };
        return originPath;
      }])
      .config(['$httpProvider', function($httpProvider) {
        $httpProvider.interceptors.push('originPath');
      }])
      .config(function($translateProvider, tmhDynamicLocaleProvider) {

        $translateProvider.useMissingTranslationHandlerLog();

        $translateProvider.useStaticFilesLoader({
          files: [
            {
              prefix: 'i18n/locale_',
              suffix: '.json'
            },
            {
              prefix: 'plugins/cronapp-framework-js/i18n/locale_',
              suffix: '.json'
            }]
        });

        $translateProvider.registerAvailableLanguageKeys(
            ['pt_br', 'en_us'], {
              'en*': 'en_us',
              'pt*': 'pt_br',
              '*': 'pt_br'
            }
        );

        var locale = (window.navigator.userLanguage || window.navigator.language || 'pt_br').replace('-', '_');

        $translateProvider.use(locale.toLowerCase());
        $translateProvider.useSanitizeValueStrategy('escaped');

        tmhDynamicLocaleProvider.localeLocationPattern('plugins/angular-i18n/angular-locale_{{locale}}.js');

        if (moment)
          moment.locale(locale);
      })

      .directive('crnValue', ['$parse', function($parse) {
        return {
          restrict: 'A',
          require: '^ngModel',
          link: function(scope, element, attr, ngModel) {
            var evaluatedValue;
            if (attr.value) {
              evaluatedValue = attr.value;
            } else {
              evaluatedValue = $parse(attr.crnValue)(scope);
            }
            element.attr("data-evaluated", JSON.stringify(evaluatedValue));
            element.bind("click", function(event) {
              scope.$apply(function() {
                ngModel.$setViewValue(evaluatedValue);
              }.bind(element));
            });
          }
        };
      }])
      .decorator("$xhrFactory", [
        "$delegate", "$injector",
        function($delegate, $injector) {
          return function(method, url) {
            var xhr = $delegate(method, url);
            var $http = $injector.get("$http");
            var callConfig = $http.pendingRequests[$http.pendingRequests.length - 1];
            if (angular.isFunction(callConfig.onProgress))
              xhr.upload.addEventListener("progress",callConfig.onProgress);
            return xhr;
          };
        }
      ])
      // General controller
      .controller('PageController', function($controller, $scope, $stateParams, $location, $http, $rootScope, $translate) {
        // save state params into scope
        $scope.params = $stateParams;
        $scope.$http = $http;

        app.registerEventsCronapi($scope, $translate);


        // Query string params
        var queryStringParams = $location.search();
        for (var key in queryStringParams) {
          if (queryStringParams.hasOwnProperty(key)) {
            $scope.params[key] = queryStringParams[key];
          }
        }

        //Components personalization jquery
        $scope.registerComponentScripts = function() {
          //carousel slider
          $('.carousel-indicators li').on('click', function() {
            var currentCarousel = '#' + $(this).parent().parent().parent().attr('id');
            var index = $(currentCarousel + ' .carousel-indicators li').index(this);
            $(currentCarousel + ' #carousel-example-generic').carousel(index);
          });
        }

        $scope.registerComponentScripts();

        try {
          var contextAfterPageController = $controller('AfterPageController', { $scope: $scope });
          app.copyContext(contextAfterPageController, this, 'AfterPageController');
        } catch(e) {};
        try { if ($scope.blockly.events.afterPageRender) $scope.blockly.events.afterPageRender(); } catch(e) {};
      })

      .run(function($rootScope, $state) {
        $rootScope.$on('$stateChangeError', function() {
          if (arguments.length >= 6) {
            var requestObj = arguments[5];
            if (requestObj.status === 404 || requestObj.status === 403) {
              localStorage.removeItem('_u');
              $state.go('login');
            }
          } else {
            $state.go('404');
          }
        });
      });

}(window));

app.userEvents = {};

//Configuration
app.config = {};
app.config.datasourceApiVersion = 2;

app.bindScope = function($scope, obj) {
  var newObj = {};

  for (var x in obj) {
    if (typeof obj[x] == 'string')
      newObj[x] = obj[x];
    else if (typeof obj[x] == 'function')
      newObj[x] = obj[x].bind($scope);
    else {
      newObj[x] = app.bindScope($scope, obj[x]);
    }
  }

  return newObj;
};

app.registerEventsCronapi = function($scope, $translate) {
  for (var x in app.userEvents)
    $scope[x] = app.userEvents[x].bind($scope);

  $scope.vars = {};

  try {
    if (cronapi) {
      $scope['cronapi'] = app.bindScope($scope, cronapi);
      $scope['cronapi'].$scope = $scope;
      $scope.safeApply = safeApply;
      if ($translate) {
        $scope['cronapi'].$translate = $translate;
      }
    }
  } catch (e) {
    console.info('Not loaded cronapi functions');
    console.info(e);
  }
  try {
    if (blockly)
      $scope['blockly'] = app.bindScope($scope, blockly);
  } catch (e) {
    console.info('Not loaded blockly functions');
    console.info(e);
  }
};

app.copyContext = function(fromContext, toContext, controllerName) {
  if (fromContext) {
    for (var item in fromContext) {
      if (!toContext[item])
        toContext[item] = fromContext[item];
      else
        toContext[item+controllerName] = fromContext[item];
    }
  }
};

window.safeApply = function(fn) {
  var phase = this.$root.$$phase;
  if (phase == '$apply' || phase == '$digest') {
    if (fn && (typeof(fn) === 'function')) {
      fn();
    }
  } else {
    this.$apply(fn);
  }
};