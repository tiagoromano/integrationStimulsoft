package cronapi.rest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import cronapi.ErrorResponse;
import cronapi.report.ReportService;
import cronapp.reports.commons.ReportFront;


@RestController
@RequestMapping("/api/rest")
public class ReportREST {
  
  private static final Logger log = LoggerFactory.getLogger(ReportREST.class);
  
  @Autowired
  private ReportService reportService;
  
  @RequestMapping(value = "/report", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<ReportFront> getReport(@RequestBody ReportFront reportFront) {
    if(reportFront == null)
      return ResponseEntity.badRequest().header("Error", "Report is null").body(new ReportFront());
    log.debug("Get report [" + reportFront + "].");
    ReportFront reportResult = reportService.getReport(reportFront.getReportName());
    return ResponseEntity.ok().body(reportResult);
  }
  
  @RequestMapping(value = "/report/contentasstring", method = RequestMethod.POST, produces = MediaType.TEXT_PLAIN_VALUE)
  public ResponseEntity<String> getContentAsString(@RequestBody ReportFront reportFront, HttpServletResponse response) {
    if(reportFront == null)
      return ResponseEntity.badRequest().header("Error", "Report is null").body("Error read content file");
    String reportName = reportFront.getReportName();
    log.debug("Print report [" + reportName + "]");
    response.setHeader("Content-Disposition", "inline; filename=" + reportName);
    response.setContentType("application/plain");
    String reportResult = reportService.getContentReport(reportName);
    return ResponseEntity.ok().body(reportResult);
  }
  
  
  @RequestMapping(value = "/report/pdf", method = RequestMethod.POST, produces = MediaType.TEXT_PLAIN_VALUE)
  public ResponseEntity<byte[]> getPDF(@RequestBody ReportFront reportFront, HttpServletResponse response) {
    if(reportFront == null)
      return ResponseEntity.badRequest().header("Error", "Report is null").body(new byte[0]);
    String reportName = reportFront.getReportName();
    log.debug("Print report [" + reportName + "]");
    response.setHeader("Content-Disposition", "inline; filename=" + reportName + ".pdf");
    response.setContentType("application/pdf");
    byte[] reportResult = reportService.getPDF(reportFront);
    return ResponseEntity.ok().body(reportResult);
  }
  
  @RequestMapping(value = "/report/pdfasfile", method = RequestMethod.POST, produces = MediaType.TEXT_PLAIN_VALUE)
  public ResponseEntity<String> getPDFAsFile(@RequestBody ReportFront reportFront, HttpServletResponse response) {
    if(reportFront == null)
      return ResponseEntity.badRequest().header("Error", "Report is null").body("");
    String reportName = reportFront.getReportName();
    log.debug("Print report [" + reportName + "]");
    response.setHeader("Content-Disposition", "inline; filename=" + reportName + ".pdf");
    response.setContentType("application/plain");
    String reportResult = reportService.getPDFAsFile(reportFront);
    return ResponseEntity.ok().body(reportResult);
  }
  
  @ExceptionHandler(Throwable.class)
  @ResponseBody
  ResponseEntity<ErrorResponse> handleControllerException(HttpServletRequest req, Throwable ex) {
    ex.printStackTrace();
    ErrorResponse errorResponse = new ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), ex, req.getMethod());
    return new ResponseEntity<ErrorResponse>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
  }
  
}
