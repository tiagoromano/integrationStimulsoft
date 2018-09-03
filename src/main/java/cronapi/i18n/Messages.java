package cronapi.i18n;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.PropertyResourceBundle;
import java.util.ResourceBundle;

public class Messages {

  public static final Locale DEFAUL_LOCALE = new Locale("pt", "BR");
  
  private static final String BUNDLE_NAME = "cronapi.i18n.Messages";
  
  private static final ResourceBundle DEFAULT_BUNDLE = ResourceBundle.getBundle(BUNDLE_NAME,DEFAUL_LOCALE,
          new UTF8Control());
  
  public static final ThreadLocal<ResourceBundle> RESOURCE_BUNDLE = new ThreadLocal<>();
  
  public static String getString(String key) {
    try {
      ResourceBundle bundle = RESOURCE_BUNDLE.get();
      if(bundle == null)
        return DEFAULT_BUNDLE.getString(key);
      else
        return RESOURCE_BUNDLE.get().getString(key);
    }
    catch(MissingResourceException e) {
      return '!' + key + '!';
    }
  }
  
  public static String format(String pattern, Object ... arguments) {
    // MessageFormat não aceita apostrofo simples diretamente.
    String fixedPattern = pattern.replace("'", "''");
    return MessageFormat.format(fixedPattern, arguments);
  }
  
  public static void set(Locale locale) {
    if(cronapi.util.Operations.IS_DEBUG) {
      ResourceBundle.clearCache();
    }
    RESOURCE_BUNDLE.set(ResourceBundle.getBundle(BUNDLE_NAME, locale, new UTF8Control()));
  }
  
  public static void remove() {
    RESOURCE_BUNDLE.set(null);
    RESOURCE_BUNDLE.remove();
  }
  
  public static ResourceBundle getBundle(Locale locale) {
    return ResourceBundle.getBundle(BUNDLE_NAME, locale, new UTF8Control());
  }
  
  public static Locale getLocale() {
    ResourceBundle bundle = RESOURCE_BUNDLE.get();
    if(bundle == null)
      bundle = DEFAULT_BUNDLE;
    
    return bundle.getLocale();
  }
  
  public static class UTF8Control extends ResourceBundle.Control {
    public ResourceBundle newBundle(String baseName, Locale locale, String format, ClassLoader loader, boolean reload)
            throws IllegalAccessException, InstantiationException, IOException {
      String bundleName = toBundleName(baseName, locale);
      String resourceName = toResourceName(bundleName, "properties");
      ResourceBundle bundle = null;
      InputStream stream = null;
      if(reload) {
        URL url = loader.getResource(resourceName);
        if(url != null) {
          URLConnection connection = url.openConnection();
          if(connection != null) {
            connection.setUseCaches(false);
            stream = connection.getInputStream();
          }
        }
      }
      else {
        stream = loader.getResourceAsStream(resourceName);
      }
      if(stream != null) {
        try {
          bundle = new PropertyResourceBundle(new InputStreamReader(stream, "UTF-8"));
        }
        finally {
          stream.close();
        }
      }
      return bundle;
    }
  }
}
