package cronapi.odata.server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import cronapi.AppConfig;
import cronapi.QueryManager;
import cronapi.Var;
import cronapi.database.DataSource;
import cronapi.util.Operations;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.olingo.odata2.api.ODataService;
import org.apache.olingo.odata2.api.commons.ODataHttpMethod;
import org.apache.olingo.odata2.api.processor.ODataRequest;
import org.apache.olingo.odata2.api.processor.ODataResponse;
import org.apache.olingo.odata2.api.uri.PathSegment;
import org.apache.olingo.odata2.core.ODataContextImpl;
import org.apache.olingo.odata2.core.ODataPathSegmentImpl;
import org.apache.olingo.odata2.core.ODataRequestHandler;
import org.apache.olingo.odata2.core.PathInfoImpl;
import org.apache.olingo.odata2.core.servlet.RestUtil;
import org.eclipse.persistence.internal.jpa.deployment.PersistenceUnitProcessor;
import org.eclipse.persistence.internal.jpa.deployment.SEPersistenceUnitInfo;
import org.eclipse.persistence.jpa.Archive;
import org.springframework.data.domain.PageRequest;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.net.URI;
import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Pattern;

public class ODataAgent {

  private static final String ERROR_TEMPLATE = "<?xml version=\"1.0\" ?><error xmlns=\"http://schemas.microsoft.com/ado/2007/08/dataservices/metadata\"><code></code><message xml:lang=\"en\">{0}</message></error>";

  private static final int START_RESULT = 0x1C;
  private static final int END_RESULT = 0x1D;

  private static final int DEFAULT_BUFFER_SIZE = 32768;
  private static final String DEFAULT_READ_CHARSET = "utf-8";

  private static void bind(File contextFile) throws Exception {
    System.setProperty(Context.INITIAL_CONTEXT_FACTORY, "cronapi.osjava.sj.memory.MemoryContextFactory");
    System.setProperty("org.osjava.sj.jndi.shared", "true");


    Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(contextFile);
    XPath xpath = XPathFactory.newInstance().newXPath();
    NodeList nodes = (NodeList) xpath.evaluate("//Resource", doc, XPathConstants.NODESET);

    HashSet added = new HashSet();
    for (int i = 0; i < nodes.getLength(); i++) {
      Node elem = nodes.item(i);
      String name = elem.getAttributes().getNamedItem("name").getTextContent();
      if (!added.contains(name)) {

        final BasicDataSource ds = new BasicDataSource();
        ds.setUrl(elem.getAttributes().getNamedItem("url").getTextContent());
        ds.setDriverClassName(elem.getAttributes().getNamedItem("driverClassName").getTextContent());
        ds.setUsername(elem.getAttributes().getNamedItem("username").getTextContent());
        ds.setPassword(elem.getAttributes().getNamedItem("password").getTextContent());

        final Context context = new InitialContext();
        context.bind("java:comp/env/" + name, ds);

        added.add(name);

      }

    }

  }

  private static String xmlEscapeText(String t) {
    StringBuilder sb = new StringBuilder();
    for(int i = 0; i < t.length(); i++){
      char c = t.charAt(i);
      switch(c){
        case '<': sb.append("&lt;"); break;
        case '>': sb.append("&gt;"); break;
        case '\"': sb.append("&quot;"); break;
        case '&': sb.append("&amp;"); break;
        case '\'': sb.append("&apos;"); break;
        default:
          if(c>0x7e) {
            sb.append("&#"+((int)c)+";");
          }else
            sb.append(c);
      }
    }
    return sb.toString();
  }

  public static void sendError(String msg) {
    System.out.println();
    System.out.write(START_RESULT);
    System.out.print(MessageFormat.format(ERROR_TEMPLATE, xmlEscapeText(msg)));
    System.out.write(END_RESULT);
    System.out.println();
  }

  public static EntityManagerFactory find(String pu) {
    Set<Archive> archives = PersistenceUnitProcessor.findPersistenceArchives();


    for (Archive archive : archives) {

      List<SEPersistenceUnitInfo> persistenceUnitInfos = PersistenceUnitProcessor.getPersistenceUnits(archive, Thread.currentThread().getContextClassLoader());

      for (SEPersistenceUnitInfo pui : persistenceUnitInfos) {

        String namespace = pui.getPersistenceUnitName();

        if (pu == null || namespace.equalsIgnoreCase(pu)) {
          Properties properties = pui.getProperties();
          properties.setProperty("eclipselink.ddl-generation", "none");

          return Persistence.createEntityManagerFactory(namespace, properties);
        }
      }
    }

    return null;
  }

  public static synchronized void odata(String strPath) {
    try {
      String queryString = null;

      if (strPath.contains("?")) {
        String[] urlParts = strPath.split("\\?");
        queryString = urlParts[1];
        strPath = urlParts[0];
      }

      String[] parts = strPath.split("/");
      String pu = parts[0];

      Set<Archive> archives = PersistenceUnitProcessor.findPersistenceArchives();

      boolean found = false;

      for (Archive archive : archives) {

        List<SEPersistenceUnitInfo> persistenceUnitInfos = PersistenceUnitProcessor.getPersistenceUnits(archive, Thread.currentThread().getContextClassLoader());

        for (SEPersistenceUnitInfo pui : persistenceUnitInfos) {

          String namespace = pui.getPersistenceUnitName();

          if (pu == null || namespace.equalsIgnoreCase(pu)) {
            found = true;
            Properties properties = pui.getProperties();
            properties.setProperty("eclipselink.ddl-generation", "none");

            EntityManagerFactory emf = Persistence.createEntityManagerFactory(namespace, properties);
            JPAODataServiceFactory serviceFactory = new JPAODataServiceFactory(emf, namespace);

            List<PathSegment> odataPathSegment = new LinkedList<>();
            for (int i = 1; i < parts.length; i++) {
              odataPathSegment.add(new ODataPathSegmentImpl(parts[i], new LinkedHashMap<>()));
            }
            PathInfoImpl path = new PathInfoImpl();
            path.setODataPathSegment(odataPathSegment);
            path.setServiceRoot(new URI("file:///local/"));
            path.setRequestUri(new URI("file:///local/" + strPath));

            InputStream ip = new ByteArrayInputStream(new byte[0]);

            ODataRequest odataRequest = ODataRequest.method(ODataHttpMethod.GET)
                .httpMethod("GET")
                .contentType(RestUtil.extractRequestContentType(null).toContentTypeString())
                .acceptHeaders(RestUtil.extractAcceptHeaders("text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8"))
                .acceptableLanguages(RestUtil.extractAcceptableLanguage("en-US"))
                .pathInfo(path)
                .allQueryParameters(RestUtil.extractAllQueryParameters(queryString, null))
                .requestHeaders(new HashMap<>())
                .body(ip)
                .build();


            ODataContextImpl context = new ODataContextImpl(odataRequest, serviceFactory);

            ODataService service = serviceFactory.createService(context);
            context.setService(service);
            service.getProcessor().setContext(context);

            ODataRequestHandler requestHandler = new ODataRequestHandler(serviceFactory, service, context);
            final ODataResponse odataResponse = requestHandler.handle(odataRequest);

            Object entity = odataResponse.getEntity();
            System.out.println();
            System.out.write(START_RESULT);

            if (entity != null) {
              if (entity instanceof InputStream) {
                handleStream((InputStream) entity, System.out);
              } else if (entity instanceof String) {
                String body = (String) entity;
                final byte[] entityBytes = body.getBytes(DEFAULT_READ_CHARSET);
                System.out.write(entityBytes);
              } else {
                System.out.print("Illegal entity object in ODataResponse of type '" + entity.getClass() + "'");
              }

            }
            System.out.write(END_RESULT);
            System.out.println();
          }
        }
      }

      if (!found) {
        sendError("No persistence provided found!");
      }

    } catch (Exception e) {
      sendError(e.getMessage());
    }
  }

  private static int handleStream(InputStream stream, OutputStream out) throws IOException {
    int contentLength = 0;
    byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];

    try {
      int len;
      while ((len = stream.read(buffer)) != -1) {
        contentLength += len;
        out.write(buffer, 0, len);
      }
    } finally {
      stream.close();
    }
    return contentLength;
  }

  public static synchronized void jpql(String strJson) {
    try {

      Gson gson = new Gson();
      JsonObject json = gson.fromJson(strJson, JsonObject.class);

      EntityManagerFactory factory = find(json.get("persistenceUnit").getAsString());
      EntityManager entityManager = factory.createEntityManager();

      String jpql = json.get("sql").getAsString();

      Set<Var> params = new HashSet<Var>();
      if (json.get("parameters") != null && !json.get("parameters").isJsonNull()) {
        json.get("parameters").getAsJsonArray().forEach(p -> {
          params.add(Var.valueOf(p.getAsString()));
        });
      }

      PageRequest p = new PageRequest(0, json.get("rows").getAsInt());

      DataSource ds = new DataSource(json.get("entity").getAsString(), entityManager);
      ds.disableMultiTenant();

      if (params.size() > 0) {
        ds.filter(jpql, p, params.toArray(new Var[0]));

      } else {
        ds.filter(jpql, p);
      }

      ds.fetch();

      System.out.println();
      System.out.write(START_RESULT);

      System.out.print(gson.toJson(ds.getPage().getContent()));

      System.out.write(END_RESULT);
      System.out.println();
    } catch (Exception e) {
      sendError(e.getMessage());
    }
  }

  public static void main(String[] args) throws Exception {
    QueryManager.DISABLE_AUTH = true;
    AppConfig.FORCE_METADATA = true;
    AppConfig.FORCE_LOCAL_ENTITIES = true;
    Operations.IS_DEBUG = true;

    try {
      Class.forName("SpringBootMain");
    } catch (ClassNotFoundException exception) {
      TimeZone.setDefault(TimeZone.getTimeZone("UTC"));
    }

    System.out.println("Starting CronApp Data Agent");
    bind(new File(args[0]));

    if (args.length > 1) {
      QueryManager.loadJSONFromFile(new File(args[1]));
    }

    Scanner scanner = new Scanner(System.in);
    scanner.useDelimiter(Pattern.compile("[\\n]+"));

    System.out.println("Waiting for commands...");
    while (true) {
      String input = scanner.next();
      if (input.startsWith("odata ")) {
        odata(input.substring(6).trim());
      } else if (input.startsWith("jpql ")) {
        jpql(input.substring(5).trim());
      } else {
        sendError("Command not found!");
      }
    }
  }

}