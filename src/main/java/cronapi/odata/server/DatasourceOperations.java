package cronapi.odata.server;

import com.sun.security.auth.UserPrincipal;
import org.apache.olingo.odata2.api.annotation.edm.EdmFacets;
import org.apache.olingo.odata2.api.annotation.edm.EdmFunctionImport;
import org.apache.olingo.odata2.api.annotation.edm.EdmFunctionImportParameter;
import org.apache.olingo.odata2.api.edm.EdmSimpleTypeKind;
import org.apache.olingo.odata2.api.edm.provider.ComplexType;
import org.apache.olingo.odata2.api.edm.provider.Property;
import org.apache.olingo.odata2.api.edm.provider.SimpleProperty;
import org.springframework.security.core.userdetails.User;

import javax.persistence.EntityManager;
import javax.persistence.Persistence;
import javax.persistence.Query;
import java.util.ArrayList;
import java.util.List;

public class DatasourceOperations {

  private EntityManager em;

  public DatasourceOperations() {
    em = Persistence.createEntityManagerFactory("app").createEntityManager();
  }

 // @SuppressWarnings("unchecked")
  //@EdmFunctionImport(name = "FindAllSalesOrders", returnType = @EdmFunctionImport.ReturnType(type = EdmFunctionImport.ReturnType.Type.ENTITY, isCollection = false))
  public int findAllSalesOrders() {


    return 0;
  }

}
