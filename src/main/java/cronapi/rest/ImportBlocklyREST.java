package cronapi.rest;

import java.io.File;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/js/blockly.js")
public class ImportBlocklyREST {

  private static List<String> imports;
  private static boolean isDebug = ManagementFactory.getRuntimeMXBean().getInputArguments().toString()
      .indexOf("-agentlib:jdwp") > 0;

  private void fill(String base, File folder, List<String> imports) {
    for(File file : folder.listFiles()) {
      if(file.isDirectory()) {
        fill(base, file, imports);
      }
      else {
        if(file.getName().endsWith(".blockly.js")) {
          String js = file.getAbsolutePath().replace(base, "");
          js = js.replace("\\", "/");
          if(js.startsWith("/")) {
            js = js.substring(1);
          }
          imports.add(js + "?" + file.lastModified());
        }
      }
    }
  }

  @RequestMapping(method = RequestMethod.GET)
  public void listBlockly(HttpServletRequest request, HttpServletResponse response) throws Exception {
    response.setContentType("application/javascript");
    PrintWriter out = response.getWriter();
    if(imports == null) {
      synchronized(ImportBlocklyREST.class) {
        if(imports == null) {
          List<String> fillImports = new ArrayList<>();
          File folder = new File(request.getServletContext().getRealPath("/"));
          fill(request.getServletContext().getRealPath("/"), folder, fillImports);
          if(!isDebug) {
            imports = fillImports;
          }
          else {
            write(out, fillImports);
          }
        }
      }
    }

    if(imports != null) {
      write(out, imports);
    }
  }

  private void write(PrintWriter out, List<String> imports) {
    out.println("window.blockly = window.blockly || {};");
    out.println("window.blockly.js = window.blockly.js || {};");
    out.println("window.blockly.js.blockly = window.blockly.js.blockly || {};");

    for(String js : imports) {
      out.println("document.write(\"<script src='" + js + "'></script>\")");
    }
  }

}