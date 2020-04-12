/*
 * Copyright (C) 2015 Square, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package retrofit2;

import javax.annotation.Nullable;

import static retrofit2.Utils.checkNotNull;

/** An HTTP response. */
public abstract class Response<T> {

//  private final okhttp3.Response rawResponse;
  private final @Nullable T body;
//  private final @Nullable ResponseBody errorBody;

  protected Response(@Nullable T body) {
//    this.rawResponse = rawResponse;
    this.body = body;
//    this.errorBody = errorBody;
  }

//  /** The raw response from the HTTP client. */
//  public okhttp3.Response raw() {
//    return rawResponse;
//  }
//
//  /** HTTP status code. */
//  public int code() {
//    return rawResponse.code();
//  }
//
//  /** HTTP status message or null if unknown. */
//  public String message() {
//    return rawResponse.message();
//  }
//
//  /** HTTP headers. */
//  public Headers headers() {
//    return rawResponse.headers();
//  }
//
//  /** Returns true if {@link #code()} is in the range [200..300). */
//  public boolean isSuccessful() {
//    return rawResponse.isSuccessful();
//  }

  /** The deserialized response body of a {@linkplain #isSuccessful() successful} response. */
  public @Nullable T body() {
    return body;
  }

  /**
   * Returns true if {@link #code()} is in the range [200..300).
   */
  public abstract boolean isSuccessful();
  /** The raw response body of an {@linkplain #isSuccessful() unsuccessful} response. */
//  public @Nullable ResponseBody errorBody() {
//    return errorBody;
//  }

//  @Override public String toString() {
//    return rawResponse.toString();
//  }
}
