package cronapi.report;

import java.io.File;

import cronapi.CronapiMetaData;
import cronapi.CronapiMetaData.CategoryType;
import cronapi.CronapiMetaData.ObjectType;
import cronapi.ParamMetaData;
import cronapi.Var;
import cronapp.reports.ReportExport;
import cronapp.reports.commons.Parameter;
import cronapp.reports.commons.ParameterType;
import cronapp.reports.commons.ReportFront;

/**
 * Classe que representa ...
 * 
 * @author Usuário de Teste
 * @version 1.0
 * @since 2017-12-04
 *
 */

@CronapiMetaData(category = CategoryType.UTIL, categoryTags = { "Report", "Relatório" })
public class Operations {
  
  /**
   * Construtor
   **/
  public Operations() {
    
  }
  
  @CronapiMetaData(type = "function", name = "{{generateReport}}", nameTags = { "generateReport",
      "GerarRelatorio" }, description = "{{generateReportDescription}}", returnType = ObjectType.OBJECT, wizard = "procedures_generatereport_callreturn")
  public static final Var generateReport(@ParamMetaData(blockType = "util_report_list", type = ObjectType.STRING, description = "{{report}}") Var reportName,
                                         @ParamMetaData(type = ObjectType.STRING, description = "{{path}}") Var path) {
    return cronapi.report.Operations.generateReport(reportName, path, Var.VAR_NULL);
  }
  
  public static final Var generateReport(Var reportName, Var path, Var params) {
    File file = null;
    if(!reportName.isNull() || !path.isNull()) {
      ReportService service = new ReportService();
      ReportFront reportFront = service.getReport(reportName.getObjectAsString());
      if(params != Var.VAR_NULL && params.size() > 0) {
        for(Var param : params.getObjectAsList()) {
          Parameter parameter = new Parameter();
          parameter.setName(param.getId());
          parameter.setType(ParameterType.toType(param.getObject().getClass()));
          parameter.setValue(param.getObjectAsString());
          reportFront.addParameter(parameter);
        }
      }
      file = new File(path.getObjectAsString());
      ReportExport export = service.getReportExport(reportFront, file);
      if(export != null)
        export.exportReportToPdfFile();
      else
        throw new RuntimeException("Error while exporting report [" + reportName.getObjectAsString() + "]");
    }
    else {
      throw new RuntimeException("Error without parameters");
    }
    return Var.valueOf(file);
  }
  
}
