package retrofit2.http;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/** Created by ZhangZhenli on 2015/11/25. */
@Documented
@Target({TYPE})
@Retention(RUNTIME)
public @interface ParamQuerys {

  String[] value();

  /**
   * Specifies whether the parameter {@linkplain #value() name} and value are already URL encoded.
   */
  boolean encoded() default false;
}
