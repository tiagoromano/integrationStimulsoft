package cronapi.database;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Service;

@Service
@Scope(proxyMode = ScopedProxyMode.TARGET_CLASS, value = "session")
public class TenantService {
  
  private final Map<String, Object> contextIds;
  
  public TenantService() {
    this.contextIds = new ConcurrentHashMap<>();
  }
  
  public Object getId(String contextKey) {
    return contextIds.get(contextKey);
  }
  
  public void setId(String contextKey, Object id) {
    contextIds.put(contextKey, id);
  }
  
  public Map<String, Object> getContextIds() {
    return contextIds;
  }
}
