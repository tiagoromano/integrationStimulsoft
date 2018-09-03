package cronapi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(value = { ElementType.PARAMETER, ElementType.TYPE })
public @interface ParamMetaData {
  
  CronapiMetaData.ObjectType type() default CronapiMetaData.ObjectType.UNKNOWN;
  
  String defaultValue() default "";
  
  String blockType() default "";
  
  String description() default "";
  
  String[] keys() default "";
  String[] values() default "";
  
}
