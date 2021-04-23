package retrofit2.okhttp;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Converter;
import retrofit2.ParameterHandler;

import javax.annotation.Nullable;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;

import static retrofit2.Utils.checkNotNull;

public abstract class HttpParamHandler<T> extends ParameterHandler<RequestBuilder,T> {


    @Override
    public abstract void apply(RequestBuilder builder, @Nullable T value) throws IOException;

    static final class RelativeUrl extends HttpParamHandler<Object> {
        private final Method method;
        private final int p;

        RelativeUrl(Method method, int p) {
            this.method = method;
            this.p = p;
        }

        @Override public void apply(RequestBuilder builder, @Nullable Object value) {
            if (value == null) {
                throw retrofit2.Utils.parameterError(method, p, "@Url parameter is null.");
            }
            builder.setRelativeUrl(value);
        }
    }

    static final class Header<T> extends HttpParamHandler<T> {
        private final String name;
        private final Converter<T, String> valueConverter;

        Header(String name, Converter<T, String> valueConverter) {
            this.name = checkNotNull(name, "name == null");
            this.valueConverter = valueConverter;
        }

        @Override public void apply(RequestBuilder builder, @Nullable T value) throws IOException {
            if (value == null) return; // Skip null values.

            String headerValue = valueConverter.convert(value);
            if (headerValue == null) return; // Skip converted but null values.

            builder.addHeader(name, headerValue);
        }
    }

    static final class Path<T> extends HttpParamHandler<T> {
        private final Method method;
        private final int p;
        private final String name;
        private final Converter<T, String> valueConverter;
        private final boolean encoded;


        Path(Method method, int p, String name, Converter<T, String> valueConverter, boolean encoded) {
            this.method = method;
            this.p = p;
            this.name = checkNotNull(name, "name == null");
            this.valueConverter = valueConverter;
            this.encoded = encoded;
        }

        @Override public void apply(RequestBuilder builder, @Nullable T value) throws IOException {
            if (value == null) {
                throw retrofit2.Utils.parameterError(method, p,
                        "Path parameter \"" + name + "\" value must not be null.");
            }
            builder.addPathParam(name, valueConverter.convert(value), encoded);
        }
    }

    static final class Query<T> extends HttpParamHandler<T> {
        private final String name;
        private final Converter<T, String> valueConverter;
        private final boolean encoded;

        Query(String name, Converter<T, String> valueConverter, boolean encoded) {
            this.name = checkNotNull(name, "name == null");
            this.valueConverter = valueConverter;
            this.encoded = encoded;
        }

        @Override public void apply(RequestBuilder builder, @Nullable T value) throws IOException {
            if (value == null) return; // Skip null values.

            String queryValue = valueConverter.convert(value);
            if (queryValue == null) return; // Skip converted but null values

            builder.addQueryParam(name, queryValue, encoded);
        }
    }

    static final class QueryName<T> extends HttpParamHandler<T> {
        private final Converter<T, String> nameConverter;
        private final boolean encoded;

        QueryName(Converter<T, String> nameConverter, boolean encoded) {
            this.nameConverter = nameConverter;
            this.encoded = encoded;
        }

        @Override public void apply(RequestBuilder builder, @Nullable T value) throws IOException {
            if (value == null) return; // Skip null values.
            builder.addQueryParam(nameConverter.convert(value), null, encoded);
        }
    }

    static final class QueryMap<T> extends HttpParamHandler<Map<String, T>> {
        private final Method method;
        private final int p;
        private final Converter<T, String> valueConverter;
        private final boolean encoded;

        QueryMap(Method method, int p, Converter<T, String> valueConverter, boolean encoded) {
            this.method = method;
            this.p = p;
            this.valueConverter = valueConverter;
            this.encoded = encoded;
        }

        @Override public void apply(RequestBuilder builder, @Nullable Map<String, T> value)
                throws IOException {
            if (value == null) {
                throw retrofit2.Utils.parameterError(method, p, "Query map was null");
            }

            for (Map.Entry<String, T> entry : value.entrySet()) {
                String entryKey = entry.getKey();
                if (entryKey == null) {
                    throw retrofit2.Utils.parameterError(method, p, "Query map contained null key.");
                }
                T entryValue = entry.getValue();
                if (entryValue == null) {
                    throw retrofit2.Utils.parameterError(method, p,
                            "Query map contained null value for key '" + entryKey + "'.");
                }

                String convertedEntryValue = valueConverter.convert(entryValue);
                if (convertedEntryValue == null) {
                    throw retrofit2.Utils.parameterError(method, p, "Query map value '"
                            + entryValue
                            + "' converted to null by "
                            + valueConverter.getClass().getName()
                            + " for key '"
                            + entryKey
                            + "'.");
                }

                builder.addQueryParam(entryKey, convertedEntryValue, encoded);
            }
        }
    }

    static final class HeaderMap<T> extends HttpParamHandler<Map<String, T>> {
        private final Method method;
        private final int p;
        private final Converter<T, String> valueConverter;

        HeaderMap(Method method, int p, Converter<T, String> valueConverter) {
            this.method = method;
            this.p = p;
            this.valueConverter = valueConverter;
        }

        @Override public void apply(RequestBuilder builder, @Nullable Map<String, T> value)
                throws IOException {
            if (value == null) {
                throw retrofit2.Utils.parameterError(method, p, "Header map was null.");
            }

            for (Map.Entry<String, T> entry : value.entrySet()) {
                String headerName = entry.getKey();
                if (headerName == null) {
                    throw retrofit2.Utils.parameterError(method, p, "Header map contained null key.");
                }
                T headerValue = entry.getValue();
                if (headerValue == null) {
                    throw retrofit2.Utils.parameterError(method, p,
                            "Header map contained null value for key '" + headerName + "'.");
                }
                builder.addHeader(headerName, valueConverter.convert(headerValue));
            }
        }
    }

    static final class Headers extends HttpParamHandler<okhttp3.Headers> {
        private final Method method;
        private final int p;

        Headers(Method method, int p) {
            this.method = method;
            this.p = p;
        }

        @Override public void apply(RequestBuilder builder, @Nullable okhttp3.Headers headers) {
            if (headers == null) {
                throw retrofit2.Utils.parameterError(method, p, "Headers parameter must not be null.");
            }
            builder.addHeaders(headers);
        }
    }

    static final class Field<T> extends HttpParamHandler<T> {
        private final String name;
        private final Converter<T, String> valueConverter;
        private final boolean encoded;

        Field(String name, Converter<T, String> valueConverter, boolean encoded) {
            this.name = checkNotNull(name, "name == null");
            this.valueConverter = valueConverter;
            this.encoded = encoded;
        }

        @Override public void apply(RequestBuilder builder, @Nullable T value) throws IOException {
            if (value == null) return; // Skip null values.

            String fieldValue = valueConverter.convert(value);
            if (fieldValue == null) return; // Skip null converted values

            builder.addFormField(name, fieldValue, encoded);
        }
    }

    static final class FieldMap<T> extends HttpParamHandler<Map<String, T>> {
        private final Method method;
        private final int p;
        private final Converter<T, String> valueConverter;
        private final boolean encoded;

        FieldMap(Method method, int p, Converter<T, String> valueConverter, boolean encoded) {
            this.method = method;
            this.p = p;
            this.valueConverter = valueConverter;
            this.encoded = encoded;
        }

        @Override public void apply(RequestBuilder builder, @Nullable Map<String, T> value)
                throws IOException {
            if (value == null) {
                throw retrofit2.Utils.parameterError(method, p, "Field map was null.");
            }

            for (Map.Entry<String, T> entry : value.entrySet()) {
                String entryKey = entry.getKey();
                if (entryKey == null) {
                    throw retrofit2.Utils.parameterError(method, p, "Field map contained null key.");
                }
                T entryValue = entry.getValue();
                if (entryValue == null) {
                    throw retrofit2.Utils.parameterError(method, p,
                            "Field map contained null value for key '" + entryKey + "'.");
                }

                String fieldEntry = valueConverter.convert(entryValue);
                if (fieldEntry == null) {
                    throw retrofit2.Utils.parameterError(method, p, "Field map value '"
                            + entryValue
                            + "' converted to null by "
                            + valueConverter.getClass().getName()
                            + " for key '"
                            + entryKey
                            + "'.");
                }

                builder.addFormField(entryKey, fieldEntry, encoded);
            }
        }
    }

    static final class Part<T> extends HttpParamHandler<T> {
        private final Method method;
        private final int p;
        private final okhttp3.Headers headers;
        private final Converter<T, RequestBody> converter;

        Part(Method method, int p, okhttp3.Headers headers, Converter<T, RequestBody> converter) {
            this.method = method;
            this.p = p;
            this.headers = headers;
            this.converter = converter;
        }

        @Override public void apply(RequestBuilder builder, @Nullable T value) {
            if (value == null) return; // Skip null values.

            RequestBody body;
            try {
                body = converter.convert(value);
            } catch (IOException e) {
                throw retrofit2.Utils.parameterError(method, p, "Unable to convert " + value + " to RequestBody", e);
            }
            builder.addPart(headers, body);
        }
    }

    static final class RawPart extends HttpParamHandler<MultipartBody.Part> {
        static final RawPart INSTANCE = new RawPart();

        private RawPart() {
        }

        @Override public void apply(RequestBuilder builder, @Nullable MultipartBody.Part value) {
            if (value != null) { // Skip null values.
                builder.addPart(value);
            }
        }
    }

    static final class PartMap<T> extends HttpParamHandler<Map<String, T>> {
        private final Method method;
        private final int p;
        private final Converter<T, RequestBody> valueConverter;
        private final String transferEncoding;

        PartMap(Method method, int p,
                Converter<T, RequestBody> valueConverter, String transferEncoding) {
            this.method = method;
            this.p = p;
            this.valueConverter = valueConverter;
            this.transferEncoding = transferEncoding;
        }

        @Override public void apply(RequestBuilder builder, @Nullable Map<String, T> value)
                throws IOException {
            if (value == null) {
                throw retrofit2.Utils.parameterError(method, p, "Part map was null.");
            }

            for (Map.Entry<String, T> entry : value.entrySet()) {
                String entryKey = entry.getKey();
                if (entryKey == null) {
                    throw retrofit2.Utils.parameterError(method, p, "Part map contained null key.");
                }
                T entryValue = entry.getValue();
                if (entryValue == null) {
                    throw retrofit2.Utils.parameterError(method, p,
                            "Part map contained null value for key '" + entryKey + "'.");
                }

                okhttp3.Headers headers = okhttp3.Headers.of(
                        "Content-Disposition", "form-data; name=\"" + entryKey + "\"",
                        "Content-Transfer-Encoding", transferEncoding);

                builder.addPart(headers, valueConverter.convert(entryValue));
            }
        }
    }

    static final class Body<T> extends HttpParamHandler<T> {
        private final Method method;
        private final int p;
        private final Converter<T, RequestBody> converter;

        Body(Method method, int p, Converter<T, RequestBody> converter) {
            this.method = method;
            this.p = p;
            this.converter = converter;
        }

        @Override public void apply(RequestBuilder builder, @Nullable T value) {
            if (value == null) {
                throw retrofit2.Utils.parameterError(method, p, "Body parameter value must not be null.");
            }
            RequestBody body;
            try {
                body = converter.convert(value);
            } catch (IOException e) {
                throw retrofit2.Utils.parameterError(method, e, p, "Unable to convert " + value + " to RequestBody");
            }
            builder.setBody(body);
        }
    }

    static final class Tag<T> extends HttpParamHandler<T> {
        final Class<T> cls;

        Tag(Class<T> cls) {
            this.cls = cls;
        }

        @Override public void apply(RequestBuilder builder, @Nullable T value) {
            builder.addTag(cls, value);
        }
    }
}
