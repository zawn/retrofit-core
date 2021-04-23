package retrofit2.adapter.rxjava;

import retrofit2.ResponseWrapper;

/** @deprecated Use {@link retrofit2.okhttp.HttpException}. */
@Deprecated
public final class HttpException extends retrofit2.okhttp.HttpException {
  public HttpException(ResponseWrapper<?> response) {
    super(response);
  }
}
