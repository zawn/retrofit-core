package retrofit2.adapter.rxjava;

import retrofit2.Response;

/** @deprecated Use {@link retrofit2.ResponseException}. */
@Deprecated
public final class ResponseException extends retrofit2.ResponseException {
  public ResponseException(Response<?> response) {
    super(response);
  }
}
