package cronapi.odata.server;

import cronapi.ErrorResponse;
import cronapi.RestClient;
import cronapi.util.Operations;
import org.apache.olingo.odata2.api.ODataService;
import org.apache.olingo.odata2.api.edm.provider.EdmProvider;
import org.apache.olingo.odata2.api.processor.ODataSingleProcessor;
import org.apache.olingo.odata2.jpa.processor.api.ODataJPAContext;
import org.apache.olingo.odata2.jpa.processor.api.ODataJPAServiceFactory;
import org.apache.olingo.odata2.jpa.processor.api.exception.ODataJPARuntimeException;

import javax.persistence.EntityManagerFactory;

public class JPAODataServiceFactory extends ODataJPAServiceFactory {

  private final EntityManagerFactory entityManagerFactory;
  private final String namespace;

  public JPAODataServiceFactory(EntityManagerFactory entityManagerFactory, String namespace) {
    this.entityManagerFactory = entityManagerFactory;
    this.namespace = namespace;
  }

  @Override
  public ODataJPAContext initializeODataJPAContext() throws ODataJPARuntimeException {
    ODataJPAContext context = getODataJPAContext();
    context.setEntityManagerFactory(entityManagerFactory);
    context.setPersistenceUnitName(namespace);

    context.setJPAEdmExtension(new DatasourceExtension(context));
    context.setoDataJPAQueryExtensionEntityListener(new QueryExtensionEntityListener());

    return context;
  }

  @Override
  public ODataService createODataSingleProcessorService(EdmProvider provider, ODataSingleProcessor processor) {
    return super.createODataSingleProcessorService(provider, processor);
  }

  @Override
  public Exception handleException(Throwable throwable) {
    String msg = ErrorResponse.getExceptionMessage(throwable, RestClient.getRestClient() != null ? RestClient.getRestClient().getMethod() : "GET");
    return new RuntimeException(msg, throwable);
  }
}
