package cronapi.database;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.eclipse.persistence.descriptors.DescriptorQueryManager;

import javassist.util.proxy.MethodHandler;
import javassist.util.proxy.ProxyFactory;
import javassist.util.proxy.ProxyObject;

public class CronappDescriptorQueryManager {
  
  private static ThreadLocal<Boolean> DISABLED = new ThreadLocal<>();
  
  public static boolean isDisabled() {
    return DISABLED.get() != null && DISABLED.get();
  }
  
  public static void disableMultitenant() {
    DISABLED.set(true);
  }
  
  public static void enableMultitenant() {
    DISABLED.remove();
  }
  
  public static boolean needProxy(Object obj) {
    if(obj == null)
      return false;
    
    return !obj.getClass().getName().contains("jvst");
  }
  
  public static DescriptorQueryManager build(final DescriptorQueryManager toWrap)
          throws InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {
    
    ProxyFactory factory = new ProxyFactory();
    factory.setSuperclass(DescriptorQueryManager.class);
    Class clazz = factory.createClass();
    MethodHandler handler = new MethodHandler() {
      
      @Override
      public Object invoke(Object self, Method overridden, Method forwarder, Object[] args) throws Throwable {
        if(overridden.getName().equals("getAdditionalJoinExpression")) {
          if(DISABLED.get() != null && DISABLED.get()) {
            return null;
          }
        }
        
        if(overridden.getName().equals("hasReadObjectQuery")) {
          if(DISABLED.get() != null && DISABLED.get()) {
            return false;
          }
        }
        return overridden.invoke(toWrap, args);
      }
    };
    Object instance = clazz.newInstance();
    ((ProxyObject)instance).setHandler(handler);
    return (DescriptorQueryManager)instance;
  }
}
