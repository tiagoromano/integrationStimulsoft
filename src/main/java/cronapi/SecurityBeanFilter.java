package cronapi;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.ReflectionUtils;

import com.fasterxml.jackson.databind.ser.BeanPropertyWriter;
import com.fasterxml.jackson.databind.ser.PropertyWriter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;

import cronapi.rest.security.CronappSecurity;

public class SecurityBeanFilter extends SimpleBeanPropertyFilter {

  public static boolean includeProperty(Class clazzToCheck, String key, String method) {
    RestClient client = RestClient.getRestClient();
    if(client.getRequest() != null) {
      HttpServletRequest request = RestClient.getRestClient().getRequest();
      if(clazzToCheck != null) {
        if(request != null) {
          if(request.getAttribute("BeanPropertyFilter") != null) {
            HashSet<String> ignores = (HashSet<String>)request.getAttribute("BeanPropertyFilter");
            String name = clazzToCheck.getName() + "#" + key;
            if(ignores.contains(name))
              return false;
          }
        }
        
        Class clazz = clazzToCheck;
        if(clazz != null) {
          Field field = ReflectionUtils.findField(clazz, key);
          if(field != null) {
            Annotation security = field.getAnnotation(CronappSecurity.class);
            
            if(security instanceof CronappSecurity) {
              CronappSecurity cronappSecurity = (CronappSecurity)security;
              try {
                Method methodPermission = cronappSecurity.getClass().getMethod(method==null?client.getMethod().toLowerCase():method.toLowerCase());
                
                if(methodPermission != null) {
                  String value = (String)methodPermission.invoke(cronappSecurity);
                  
                  if(value != null && !value.isEmpty()) {
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
                      return false;
                    }
                  }
                }
                
              }
              catch(Exception e) {
                //
              }
            }
          }
        }
        
      }
    }
    return true;
  }
  
  @Override
  protected boolean include(BeanPropertyWriter writer) {
    return includeProperty(writer.getMember().getDeclaringClass(), writer.getName(), "GET");
  }

  @Override
  protected boolean include(PropertyWriter writer) {
    if(writer instanceof BeanPropertyWriter) {
      return includeProperty(((BeanPropertyWriter)writer).getMember().getDeclaringClass(), writer.getName(), "GET");
    }

    return true;
  }


}
