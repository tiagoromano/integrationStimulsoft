package cronapi.rest.security;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotação para definição de segurança em serviços REST.
 *
 * @author arthemus
 * @since 01/08/17
 */
@Target(value = { ElementType.METHOD, ElementType.TYPE, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface CronappSecurity {
  
  String get() default "";
  
  String post() default "";
  
  String put() default "";
  
  String delete() default "";

  String filter() default "";

  String execute() default "";
  
}
