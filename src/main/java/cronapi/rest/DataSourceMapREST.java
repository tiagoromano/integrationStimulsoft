package cronapi.rest;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import cronapi.AppConfig;
import cronapi.QueryManager;
import cronapi.odata.server.ODataConfiguration;
import org.eclipse.persistence.internal.jpa.deployment.PersistenceUnitProcessor;
import org.eclipse.persistence.internal.jpa.deployment.SEPersistenceUnitInfo;
import org.eclipse.persistence.jpa.Archive;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.lang.management.ManagementFactory;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

@RestController
@RequestMapping(value = "/js/dataSourceMap.js")
public class DataSourceMapREST {

  private static Map<String, DataSourceDetail> mapped;
  private static boolean isDebug = ManagementFactory.getRuntimeMXBean().getInputArguments().toString()
      .indexOf("-agentlib:jdwp") > 0;

  /**
   * Construtor
   **/
  public DataSourceMapREST() {
  }


  @RequestMapping(method = RequestMethod.GET)
  public void register(HttpServletRequest request, HttpServletResponse response) throws Exception {
    response.setContentType("application/javascript");
    PrintWriter out = response.getWriter();

    if (mapped == null) {
      synchronized (DataSourceMapREST.class) {
        if (mapped == null) {
          HashMap<String, DataSourceDetail> mappedAllDs = new HashMap<String, DataSourceDetail>();
          JsonObject customQuery = QueryManager.getJSON();
          for (Map.Entry<String, JsonElement> entry : customQuery.entrySet()) {
            String guid = entry.getKey();
            DataSourceDetail detail = this.getDetail(guid, entry.getValue().getAsJsonObject());
            if (detail.namespace.isEmpty()) {
              mappedAllDs.put(detail.customId, detail);
              mappedAllDs.put(guid, detail);
            } else {
              mappedAllDs.put(detail.namespace + "." + detail.customId, detail);
              mappedAllDs.put(detail.namespace + "." + guid, detail);
            }
          }

          if (AppConfig.exposeLocalEntities()) {
            Set<Archive> archives = PersistenceUnitProcessor.findPersistenceArchives();

            for (Archive archive : archives) {
              List<SEPersistenceUnitInfo> persistenceUnitInfos = PersistenceUnitProcessor.getPersistenceUnits(archive, Thread.currentThread().getContextClassLoader());
              for (SEPersistenceUnitInfo pui : persistenceUnitInfos) {

                String namespace = pui.getPersistenceUnitName();
                for (String clazz : pui.getManagedClassNames()) {
                  String clazzName = clazz.substring(clazz.lastIndexOf(".") + 1);
                  String serviceUrlODATA = String.format(ODataConfiguration.SERVICE_URL + "%s/%s", namespace, clazzName);
                  String serviceUrlApi = String.format("api/cronapi/crud/%s", clazz);

                  DataSourceDetail detail = new DataSourceDetail(namespace, clazz, serviceUrlApi, serviceUrlODATA, true);
                  mappedAllDs.put(clazz.replace(".entity.", "."), detail);
                }
              }
            }
          }


          if (!isDebug) {
            mapped = mappedAllDs;
          } else {
            write(out, mappedAllDs);
          }

        }
      }
    }

    if (mapped != null) {
      write(out, mapped);
    }

  }

  private DataSourceDetail getDetail(String guid, JsonObject json) {

    String customId = json.get("customId").getAsString();

    DataSourceDetail detail = null;

    String serviceUrl = json.get("serviceUrl").getAsString();
    serviceUrl = serviceUrl.replace(String.format("/%s/", guid), String.format("/%s/", customId));

    if ("entityFullName".equals(json.get("sourceType").getAsString())) {
      String entityFullName = json.get("entityFullName").getAsString();
      String namespace = entityFullName.substring(0, entityFullName.indexOf(".entity."));
      String serviceUrlODATA = String.format(ODataConfiguration.SERVICE_URL + "%s/%s", namespace, customId);
      detail = new DataSourceDetail(namespace, customId, serviceUrl, serviceUrlODATA, false);
    } else {
      detail = new DataSourceDetail("", customId, serviceUrl, "", false);
    }
    return detail;
  }

  private void write(PrintWriter out, Map<String, DataSourceDetail> mapped) {
    out.println("window.dataSourceMap = window.dataSourceMap || [];");

    mapped.forEach((k, v) -> {

      String curr = String.format("window.dataSourceMap[\"%s\"] = { customId: \"%s\", serviceUrl: \"%s\", serviceUrlODATA: \"%s\" };",
          k,
          v.customId,
          v.serviceUrl,
          v.serviceUrlODATA
      );

      out.println(curr);
    });
  }

  public class DataSourceDetail {

    public DataSourceDetail(String namespace, String customId, String serviceUrl, String serviceUrlODATA, boolean isEntity) {
      this.namespace = namespace;
      this.customId = customId;
      this.serviceUrl = serviceUrl;
      this.serviceUrlODATA = serviceUrlODATA;
      this.isEntity = isEntity;
    }

    public String namespace;
    public String customId;
    public String serviceUrl;
    public String serviceUrlODATA;
    public boolean isEntity;
  }
}
