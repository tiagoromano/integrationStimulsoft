package cronapi.clazz;

import java.io.File;
import java.io.FileInputStream;
import java.util.Hashtable;

import org.apache.commons.io.IOUtils;

import cronapi.util.Operations;

public class CronapiClassLoader extends ClassLoader {
  
  private Hashtable<String, Class<?>> classes = new Hashtable<>();

  public void addClass(String className, byte[] classData) {
    Class<?> clazz = defineClass(className, classData, 0, classData.length);
    classes.put(className, clazz);
  }
  
  public CronapiClassLoader() {
    super(CronapiClassLoader.class.getClassLoader()); // calls the parent class loader's constructor
  }
  
  @Override
  public Class<?> loadClass(String className) throws ClassNotFoundException {
    return findClass(className);
  }
  
  @Override
  public Class<?> findClass(String className) throws ClassNotFoundException {
    Class<?> clazz = classes.get(className);
    
    if(clazz != null)
      return clazz;
    
    byte classData[];
    try {
      String path = className.replace('.', File.separatorChar);
      File classFile = new File(classFolder(Class.forName(className)), path + ".class");
      
      if(classFile.exists()) {
        
        if(className.startsWith("cronapi.")) {
          return Class.forName(className);
        }
        
        try (FileInputStream fi = new FileInputStream(classFile)) {
          classData = IOUtils.toByteArray(fi);
          clazz = defineClass(className, classData, 0, classData.length);
          classes.put(className, clazz);
          return clazz;
        }
      }
      else {
        return Class.forName(className);
      }
    }
    catch(Exception e) {
      throw new ClassNotFoundException(e.getMessage(), e);
    }
  }
  
  private static String classFolder(Class<?> clazz) {
    String classFolder = "";
    
    try {
      if(clazz.getProtectionDomain() != null && clazz.getProtectionDomain().getCodeSource() != null) {
        classFolder = new File(clazz.getProtectionDomain().getCodeSource().getLocation().toURI()).getAbsolutePath();
        
        String windowsSlash = (Operations.IS_WINDOWS ? "/?" : "");
        classFolder = classFolder.replaceAll("file:" + windowsSlash + "|vfs:" + windowsSlash, "");

        String path = null;
        
        if (clazz.getCanonicalName() != null) {
          path = clazz.getCanonicalName().replace(".", File.separator) + ".class";
        } else {
          path = clazz.getName().replace(".", File.separator) + ".class";
        }
        if(classFolder.endsWith(path)) {
          classFolder = classFolder.substring(0, classFolder.length() - path.length());
        }
      }
    }
    catch(Exception e) {
      classFolder = "";
    }
    
    return fix(classFolder);
  }
  
  private static String fix(String path) {
    return path.replace('\\', File.separatorChar).replace('/', File.separatorChar);
  }
  
}
