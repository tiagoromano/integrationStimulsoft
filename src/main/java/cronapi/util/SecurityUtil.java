package cronapi.util;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.springframework.util.ReflectionUtils;

public class SecurityUtil {
  
  public static class SecurityGroup {
    public String id;
    public String name;
  }
  
  private static Class forName(String clazz) {
    try {
      return Class.forName(clazz);
    }
    catch(Exception e) {
      //
    }
    return null;
  }
  
  public static List<SecurityGroup> getRoles() {
    Class clazz = forName("auth.permission.SecurityPermission");
    if(clazz != null) {
      final List<SecurityGroup> groups = new LinkedList<>();
      ReflectionUtils.doWithLocalFields(clazz, new ReflectionUtils.FieldCallback() {
        @Override
        public void doWith(Field field) throws IllegalArgumentException, IllegalAccessException {
          Object v = field.get(null);
          
          SecurityGroup group = new SecurityGroup();
          group.name = v.toString();
          group.id = v.toString();
          
          groups.add(group);
        }
      });
      return groups;
    }
    
    return Collections.EMPTY_LIST;
  }
}
