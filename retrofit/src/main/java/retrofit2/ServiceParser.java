package retrofit2;

import java.lang.reflect.Method;

/**
 * @author zhangzhenli
 */
public abstract class ServiceParser {

  public abstract <T> ServiceMethod<T> parseAnnotations(Retrofit retrofit, Method method);
}
