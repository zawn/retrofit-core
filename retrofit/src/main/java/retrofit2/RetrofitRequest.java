package retrofit2;

import javax.annotation.Nullable;

public class RetrofitRequest<T> {

  private final @Nullable T body;

  public RetrofitRequest(@Nullable T body) {
    this.body = body;
  }

  public @Nullable T body() {
    return body;
  }
}