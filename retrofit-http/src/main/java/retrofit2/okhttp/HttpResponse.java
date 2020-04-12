package retrofit2.okhttp;

import okhttp3.Headers;
import okhttp3.Protocol;
import okhttp3.Request;
import okhttp3.ResponseBody;
import retrofit2.OkHttpCall;
import retrofit2.Response;

import javax.annotation.Nullable;

import static retrofit2.okhttp.Utils.checkNotNull;

public class HttpResponse<T> extends Response<T> {
    /**
     * Create a synthetic successful response with {@code body} as the deserialized body.
     */
    public static <T> retrofit2.Response<T> success(@Nullable T body) {
        return success(body, new okhttp3.Response.Builder() //
                .code(200)
                .message("OK")
                .protocol(Protocol.HTTP_1_1)
                .request(new Request.Builder().url("http://localhost/").build())
                .build());
    }

    /**
     * Create a synthetic successful response with an HTTP status code of {@code code} and
     * {@code body} as the deserialized body.
     */
    public static <T> retrofit2.Response<T> success(int code, @Nullable T body) {
        if (code < 200 || code >= 300) {
            throw new IllegalArgumentException("code < 200 or >= 300: " + code);
        }
        return success(body, new okhttp3.Response.Builder() //
                .code(code)
                .message("Response.success()")
                .protocol(Protocol.HTTP_1_1)
                .request(new Request.Builder().url("http://localhost/").build())
                .build());
    }

    /**
     * Create a synthetic successful response using {@code headers} with {@code body} as the
     * deserialized body.
     */
    public static <T> retrofit2.Response<T> success(@Nullable T body, Headers headers) {
        checkNotNull(headers, "headers == null");
        return success(body, new okhttp3.Response.Builder() //
                .code(200)
                .message("OK")
                .protocol(Protocol.HTTP_1_1)
                .headers(headers)
                .request(new Request.Builder().url("http://localhost/").build())
                .build());
    }

    /**
     * Create a successful response from {@code rawResponse} with {@code body} as the deserialized
     * body.
     */
    public static <T> retrofit2.Response<T> success(@Nullable T body, okhttp3.Response rawResponse) {
        checkNotNull(rawResponse, "rawResponse == null");
        if (!rawResponse.isSuccessful()) {
            throw new IllegalArgumentException("rawResponse must be successful response");
        }
        return new retrofit2.okhttp.HttpResponse<>(rawResponse, body, null);
    }

    /**
     * Create a synthetic error response with an HTTP status code of {@code code} and {@code body}
     * as the error body.
     */
    public static <T> retrofit2.Response<T> error(int code, ResponseBody body) {
        checkNotNull(body, "body == null");
        if (code < 400) throw new IllegalArgumentException("code < 400: " + code);
        return error(body, new okhttp3.Response.Builder() //
                .body(new OkHttpCall.NoContentResponseBody(body.contentType(), body.contentLength()))
                .code(code)
                .message("Response.error()")
                .protocol(Protocol.HTTP_1_1)
                .request(new Request.Builder().url("http://localhost/").build())
                .build());
    }

    /**
     * Create an error response from {@code rawResponse} with {@code body} as the error body.
     */
    public static <T> retrofit2.Response<T> error(ResponseBody body, okhttp3.Response rawResponse) {
        checkNotNull(body, "body == null");
        checkNotNull(rawResponse, "rawResponse == null");
        if (rawResponse.isSuccessful()) {
            throw new IllegalArgumentException("rawResponse should not be successful response");
        }
        return new retrofit2.okhttp.HttpResponse<>(rawResponse, null, body);
    }

    private final okhttp3.Response rawResponse;
    private final @Nullable
    ResponseBody errorBody;

    private HttpResponse(okhttp3.Response rawResponse, @Nullable T body,
                         @Nullable ResponseBody errorBody) {
        super(body);
        this.rawResponse = rawResponse;
        this.errorBody = errorBody;
    }

    /**
     * The raw response from the HTTP client.
     */
    public okhttp3.Response raw() {
        return rawResponse;
    }

    /**
     * HTTP status code.
     */
    public int code() {
        return rawResponse.code();
    }

    /**
     * HTTP status message or null if unknown.
     */
    public String message() {
        return rawResponse.message();
    }

    /**
     * HTTP headers.
     */
    public Headers headers() {
        return rawResponse.headers();
    }

    /**
     * Returns true if {@link #code()} is in the range [200..300).
     */
    public boolean isSuccessful() {
        return rawResponse.isSuccessful();
    }

    /**
     * The raw response body of an {@linkplain #isSuccessful() unsuccessful} response.
     */
    public @Nullable
    ResponseBody errorBody() {
        return errorBody;
    }

    @Override
    public String toString() {
        return rawResponse.toString();
    }
}
