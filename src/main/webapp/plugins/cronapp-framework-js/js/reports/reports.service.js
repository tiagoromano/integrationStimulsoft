(function($app) {
  angular.module('report.services', []).service('ReportService', function($http, $compile, $modal, $translate) {

    var body = $('body');
    var scope = angular.element(body.get(0)).scope();

    // data
    this.getReport = function(reportName) {
      debugger;
      var req = {
        url : 'api/rest/report',
        method : 'POST',
        data : angular.toJson({
          'reportName' : reportName
        })
      };
      return $http(req);
    };

    // bytes[]
    this.getPDF = function(report) {
      var req = {
        url : 'api/rest/report/pdf',
        method : 'POST',
        responseType : 'arraybuffer',
        data : angular.toJson(report)
      };
      return $http(req);
    };

    // file
    this.getPDFAsFile = function(report) {
      var req = {
        url : 'api/rest/report/pdfasfile',
        method : 'POST',
        data : angular.toJson(report)
      };
      return $http(req);
    };
    
    this.getContentAsString = function(report) {
      var req = {
        url : 'api/rest/report/contentasstring',
        method : 'POST',
        data : angular.toJson(report)
      };
      return $http(req);
    };

    // open report
    this.openURLContent = function(url) {
      debugger;
      // Retrocompatibilidade
      var context = $('#reportViewContext');

      if(!context.get(0)) {
        console.log('include[#reportViewContext]');
        body.append('<div id="reportViewContext" ng-include="\'plugins/cronapp-framework-js/components/reports/reports.view.html\'"></div>');
        $compile(body)(scope);
      }

      var include = function() {
        var frame = $('<iframe/>');
        frame.attr('frameborder', 0);
        var h = parseInt($(window).height());

        frame.attr('height', h - 200);
        frame.attr('width', '100%');
        frame.attr('src', url + "?download=false");
        var m = $('#reportView .modal-body');
        if(m.get(0)) {
          m.html(frame);
          $('#reportViewContext .modal-dialog').css('width', '95%');
          setTimeout(function() {
            console.log('open[#reportViewContext]');
            $('body').append(context);
            $('#reportView').modal();
          }, 100);
        }
        else {
          console.log('wait[#reportViewContext]');
          setTimeout(include, 200);
        }
      }

      setTimeout(include, 200);
    };

    this.initializeStimulsoft = function(language, json) {
      if (!Stimulsoft.Base.StiLicense.Key) {
        var localization = stimulsoftHelper.getLocalization(language);
        Stimulsoft.Base.Localization.StiLocalization.loadLocalization(localization.xml);
        Stimulsoft.Base.Localization.StiLocalization.cultureName = localization.cultureName;
        Stimulsoft.Base.StiLicense.Key = stimulsoftHelper.getKey();
      }
    }

    this.openStimulsoftReport = function(json) {
      var context = $('#reportViewContext');
      if(!context.get(0)) {
        console.log('include[#reportViewContext]');
        body.append('<div id="reportViewContext" ng-include="\'plugins/cronapp-framework-js/components/reports/reports.view.html\'"></div>');
        $compile(body)(scope);
      }
      
      this.initializeStimulsoft($translate.use(), json);
      var options = null;
      var viewer = new Stimulsoft.Viewer.StiViewer(options, "StiViewer", false);
      var report = new Stimulsoft.Report.StiReport();
      report.load(json);
      viewer.report = report;
      
      var include = setInterval(function() {
        var div = $('<div/>');
        div.attr('id',"contentReport");
        div.attr('width', '100%');
        var m = $('#reportView .modal-body');
        if(m.get(0)) {
          m.html(div);
          $('#reportViewContext .modal-dialog').css('width', '95%');
          setTimeout(function() {
            console.log('open[#reportViewContext]');
            $('body').append(context);
            $('#reportView').modal();
            viewer.renderHtml("contentReport"); 
          }, 100);
          
          clearInterval(include);
        }
      }, 200);

    };


    this.showParameters = function(report) {
      var parameters = report.parameters;
      var htmlParameters = [];
      var index = 0;
      var escapeRegExp = function(str) {
        return str.replace(/([.*+?^=!:()|\[\]\/\\])/g, "\\$1");
      };
      var replaceAll = function(str, find, replace) {
        return str.replace(new RegExp(escapeRegExp(find), 'g'), replace);
      };

      var next = function() {
        if(index < parameters.length) {
          var parameter = parameters[index++];
          $.get("plugins/cronapp-framework-js/components/reports/" + parameter.type + ".parameter.html").done(function(result) {
            htmlParameters.push(replaceAll(result, "_field_", parameter.name));
            next();
          });
        }
        else if(htmlParameters.length > 0) {
          $modal.open({
            templateUrl : 'plugins/cronapp-framework-js/components/reports/reports.parameters.html',
            controller : 'ParameterController',
            resolve : {
              report : function() {
                return JSON.parse(JSON.stringify(report));
              },
              htmlParameters : function() {
                return JSON.parse(JSON.stringify(htmlParameters));
              }
            }
          });
        }
      }.bind(this);
      next();
    };
    
    this.mergeParam = function(parameters, params) {
      var getValue = function(key, json) {
        for (var i in Object.keys(json)) {
           var k = Object.keys(json[i])[0];
           if (key == k)
            return Object.values(json[i])[0];
        }
      };
      for (var i in Object.keys(parameters)) { 
        var k = parameters[i].name;
        var v = parameters[i].value;
        var valueParam = getValue(k, params);
        if (valueParam) {
          parameters[i].value = valueParam;
        }
      }
      return parameters;
    };
    
    this.hasParameterWithOutValue = function(parameters) {
      var hasWithOutValue = false;
      for (var i in Object.keys(parameters)) { 
        if (!parameters[i].value) {
          return true;
        }
      }
      return hasWithOutValue;
    };

    this.openReport = function(reportName, params) {
      this.getReport(reportName).then(function(result) {
        if(result && result.data) {
          // Abrir direto o relatorio , caso não haja parametros
          if(result.data.parameters.length == 0 || (result.data.parameters.length == 1 && result.data.parameters[0].name == 'DATA_LIMIT')) {
            
            if (result.data.reportName.endsWith('.report')) {
              this.getContentAsString(result.data).then(
                function(content) {
                  debugger;
                  this.openStimulsoftReport(content.data);
                }.bind(this),
                function(data) {
                  var message = cronapi.internal.getErrorMessage(data, data.statusText);
                  scope.Notification.error(message);
                }.bind(this)
              );
            }
            else {
              this.getPDFAsFile(result.data.reportName).then(function(obj) {
                this.openURLContent(obj.data);
              }.bind(this), function(data) {
                var message = cronapi.internal.getErrorMessage(data, data.statusText);
                scope.Notification.error(message);
              }.bind(this));
            }
            
            
          }
          else {
            if (params)
              result.data.parameters = this.mergeParam(result.data.parameters, params);
            if (this.hasParameterWithOutValue(result.data.parameters)) {
              this.showParameters(JSON.parse(JSON.stringify(result.data)));
            } else {
              this.getPDFAsFile(result.data).then(function(obj) {
                this.openURLContent(obj.data);
              }.bind(this));
            }
          }
        }
      }.bind(this));
    };

  });
}(app));
