package retrofit2;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * Creates {@link Converter} instances based on a type and target usage.
 */
public abstract class ConverterFactory<F, T> {
  /**
   * Returns a {@link Converter} for converting an HTTP response body to {@code type}, or null if
   * {@code type} cannot be handled by this factory. This is used to create converters for
   * response types such as {@code SimpleResponse} from a {@code Call<SimpleResponse>}
   * declaration.
   */
  public @Nullable
  Converter<T, ?> responseBodyConverter(Type type,
                                                   Annotation[] annotations, Retrofit retrofit) {
    return null;
  }

  /**
   * Returns a {@link Converter} for converting {@code type} to an HTTP request body, or null if
   * {@code type} cannot be handled by this factory. This is used to create converters for types
   * specified by {@link Body @Body}, {@link Part @Part}, and {@link PartMap @PartMap}
   * values.
   */
  public @Nullable Converter<?, F> requestBodyConverter(Type type,
                                                                  Annotation[] parameterAnnotations, Annotation[] methodAnnotations, Retrofit retrofit) {
    return null;
  }

  /**
   * Returns a {@link Converter} for converting {@code type} to a {@link String}, or null if
   * {@code type} cannot be handled by this factory. This is used to create converters for types
   * specified by {@link Field @Field}, {@link FieldMap @FieldMap} values,
   * {@link Header @Header}, {@link HeaderMap @HeaderMap}, {@link Path @Path},
   * {@link Query @Query}, and {@link QueryMap @QueryMap} values.
   */
  public @Nullable Converter<?, String> stringConverter(Type type, Annotation[] annotations,
      Retrofit retrofit) {
    return null;
  }

  /**
   * Extract the upper bound of the generic parameter at {@code index} from {@code type}. For
   * example, index 1 of {@code Map<String, ? extends Runnable>} returns {@code Runnable}.
   */
  protected static Type getParameterUpperBound(int index, ParameterizedType type) {
    return Utils.getParameterUpperBound(index, type);
  }

  /**
   * Extract the raw class type from {@code type}. For example, the type representing
   * {@code List<? extends Runnable>} returns {@code List.class}.
   */
  protected static Class<?> getRawType(Type type) {
    return Utils.getRawType(type);
  }
}
