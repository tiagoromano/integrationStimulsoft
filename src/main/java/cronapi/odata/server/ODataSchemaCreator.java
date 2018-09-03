package cronapi.odata.server;

import cronapi.AppConfig;
import cronapi.QueryManager;
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

import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import java.io.*;
import java.net.URI;
import java.util.*;

public class ODataSchemaCreator {

  private static final int DEFAULT_BUFFER_SIZE = 32768;
  private static final String DEFAULT_READ_CHARSET = "utf-8";

  public static void create(String pu, String fromFile, String file) throws Exception {

    if (fromFile != null) {
      QueryManager.loadJSONFromFile(new File(fromFile));
    }

    Set<Archive> archives = PersistenceUnitProcessor.findPersistenceArchives();

    for (Archive archive : archives) {

      List<SEPersistenceUnitInfo> persistenceUnitInfos = PersistenceUnitProcessor.getPersistenceUnits(archive, Thread.currentThread().getContextClassLoader());

      for (SEPersistenceUnitInfo pui : persistenceUnitInfos) {

        String namespace = pui.getPersistenceUnitName();

        if (pu == null || namespace.equalsIgnoreCase(pu)) {
          Properties properties = pui.getProperties();
          properties.setProperty("javax.persistence.jdbc.driver", "org.h2.Driver");
          properties.setProperty("javax.persistence.jdbc.url", "jdbc:h2:mem:test");
          properties.setProperty("javax.persistence.jdbc.user", "root");
          properties.setProperty("javax.persistence.jdbc.password", "root");
          properties.setProperty("javax.persistence.nonJtaDataSource", "");
          properties.setProperty("javax.persistence.jtaDataSource", "");
          properties.setProperty("eclipselink.ddl-generation", "none");
          EntityManagerFactory emf = Persistence.createEntityManagerFactory(namespace, properties);
          JPAODataServiceFactory serviceFactory = new JPAODataServiceFactory(emf, namespace);

          List<PathSegment> odataPathSegment = new LinkedList<>();
          odataPathSegment.add(new ODataPathSegmentImpl("$metadata", new LinkedHashMap<>()));
          PathInfoImpl path = new PathInfoImpl();
          path.setODataPathSegment(odataPathSegment);
          path.setServiceRoot(new URI("file:///local/"));
          path.setRequestUri(new URI("file:///local/$metadata"));

          InputStream ip = new ByteArrayInputStream(new byte[0]);
          OutputStream out;

          if (file == null) {
            out = new ByteArrayOutputStream();
          } else {
            File oFile = new File(file);
            if (oFile.exists()) {
              oFile.delete();
            }
            out = new FileOutputStream(oFile);
          }

          ODataRequest odataRequest = ODataRequest.method(ODataHttpMethod.GET)
              .httpMethod("GET")
              .contentType(RestUtil.extractRequestContentType(null).toContentTypeString())
              .acceptHeaders(RestUtil.extractAcceptHeaders("text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8"))
              .acceptableLanguages(RestUtil.extractAcceptableLanguage("en-US"))
              .pathInfo(path)
              .allQueryParameters(RestUtil.extractAllQueryParameters(null, null))
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
          if (entity != null) {

            if (entity instanceof InputStream) {
              handleStream((InputStream) entity, out);
            } else if (entity instanceof String) {
              String body = (String) entity;
              final byte[] entityBytes = body.getBytes(DEFAULT_READ_CHARSET);
              out.write(entityBytes);
            } else {
              throw new IOException("Illegal entity object in ODataResponse of type '" + entity.getClass() + "'.");
            }

            out.flush();
            out.close();

          }

          if (out instanceof ByteArrayOutputStream) {
            System.out.println();
            System.out.print("[" + namespace + " = ");
            System.out.print(new String(((ByteArrayOutputStream) out).toByteArray()));
            System.out.println("]");
          }
        }
      }
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

  public static void main(String[] args) throws Exception {
    AppConfig.FORCE_METADATA = true;
    if (args.length == 0) {
      create(null, null, null);
    } else {
      create(null, args[0], null);
    }
  }

  public static void test() throws Exception {
    create(null, null, null);
  }

}