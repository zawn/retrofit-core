package retrofit2;

import javax.annotation.Nullable;

public class RequestWrapper<T> {

  private final @Nullable T body;

  public RequestWrapper(@Nullable T body) {
    this.body = body;
  }

  public @Nullable T body() {
    return body;
  }
}