package retrofit2.http;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

/** Created by ZhangZhenli on 2015/11/27. */
@Documented
@Target({TYPE})
@Retention(RUNTIME)
public @interface ParamUrl {
  String value();
}
