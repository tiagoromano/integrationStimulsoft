package cronapi;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Anotação para definição de segurança em serviços REST.
 *
 * @author romano
 * @since 25/09/17
 */
@Target(value = { ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface CronapiCloud {
  
  String type() default "";
  String value() default "";
}
