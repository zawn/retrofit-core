package retrofit2.adapter.rxjava;

import retrofit2.Response;

/** @deprecated Use {@link retrofit2.okhttp.HttpException}. */
@Deprecated
public final class HttpException extends retrofit2.okhttp.HttpException {
  public HttpException(Response<?> response) {
    super(response);
  }
}
