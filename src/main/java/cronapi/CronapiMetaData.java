package cronapi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(value = { ElementType.METHOD, ElementType.TYPE })
public @interface CronapiMetaData {
  String type() default "function";
  
  String platform() default "";
  
  boolean external() default true;
  
  CategoryType category() default CategoryType.OTHER;
  
  String categoryName() default "";
  
  String[] categoryTags() default "";
  
  String name() default "";
  
  String[] nameTags() default "";
  
  String description() default "";
  
  String[] params() default "";
  
  String wizard() default "";

  String help() default "";

  String helpTemplate() default "";
  
  boolean displayInline() default false;
  
  boolean arbitraryParams() default false;
  
  ObjectType[] paramsType() default { ObjectType.UNKNOWN };
  
  ObjectType returnType() default ObjectType.VOID;
  
  public enum CategoryType {
    AUTHENTICATION, IO, DATABASE, FRONTEND, SCREEN, CONVERSION, DATETIME, XML, EMAIL, FTP, JSON, LOGIC, TEXT, LIST, MAP, COLOR, LOOP, MATH, GRID, PRINT, OTHER, OBJECT, SEMAPHORE, UTIL, JSONORMAP, TREE, CHART
  }
  
  public enum ObjectType {
    BOOLEAN, LONG, DOUBLE, DATETIME, STRING, LIST, MAP, DATASET, JSON, XML, OBJECT, UNKNOWN, BLOCK, STATEMENT, STATEMENTSENDER, VOID
  }
}
