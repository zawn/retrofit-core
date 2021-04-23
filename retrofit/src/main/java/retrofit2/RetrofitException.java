package retrofit2;


import javax.annotation.Nullable;

import static retrofit2.Utils.checkNotNull;

public class RetrofitException extends RuntimeException {
  private static String getMessage(ResponseWrapper<?> response) {
    checkNotNull(response, "response == null");
    return response.toString();
  }

  private final transient ResponseWrapper<?> response;

  public RetrofitException(ResponseWrapper<?> response) {
    super(getMessage(response));
    this.response = response;
  }

  /**
   * The full HTTP response. This may be null if the exception was serialized.
   */
  public @Nullable ResponseWrapper<?> response() {
    return response;
  }
}
