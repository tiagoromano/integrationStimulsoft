package cronapi.rest.security;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import cronapi.i18n.Messages;
import org.springframework.security.core.GrantedAuthority;

import cronapi.RestClient;

public class BlocklySecurity {
  
  public static void checkSecurity(Class clazz, String method) throws Exception {
    
    if(clazz != null) {
      
      String value = null;
      for(Annotation annotation : clazz.getAnnotations()) {
        if(annotation.annotationType().getName().equals("cronapi.rest.security.CronappSecurity")) {
          Method type = annotation.annotationType().getMethod(method.toLowerCase());
          if(type != null) {
            value = (String)type.invoke(annotation);
          }
        }
      }
      
      if(value == null || value.isEmpty())
        value = "Authenticated";
      
      boolean authorized = false;
      
      String[] authorities = value.trim().split(";");
      
      for(String role : authorities) {
        if(role.equalsIgnoreCase("authenticated")) {
          authorized = RestClient.getRestClient().getUser() != null;
          if(authorized)
            break;
        }
        if(role.equalsIgnoreCase("permitAll") || role.equalsIgnoreCase("public")) {
          authorized = true;
          break;
        }
        for(GrantedAuthority authority : RestClient.getRestClient().getAuthorities()) {
          if(role.equalsIgnoreCase(authority.getAuthority())) {
            authorized = true;
            break;
          }
        }
        
        if(authorized)
          break;
      }
      
      if(!authorized) {
        throw new RuntimeException(Messages.getString("notAllowed"));
      }
      
    }
  }
}
