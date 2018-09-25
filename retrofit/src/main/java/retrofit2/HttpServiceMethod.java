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

import okhttp3.ResponseBody;

/** Adapts an invocation of an interface method into an HTTP call. */
final class HttpServiceMethod<ResponseT, ReturnT> extends ServiceMethod<ReturnT> {

  private final HttpRequestFactory requestFactory;
  private final okhttp3.Call.Factory callFactory;
  private final CallAdapter<ResponseT, ReturnT> callAdapter;
  private final Converter<ResponseBody, ResponseT> responseConverter;

  HttpServiceMethod(HttpRequestFactory requestFactory, okhttp3.Call.Factory callFactory,
                    CallAdapter<ResponseT, ReturnT> callAdapter,
                    Converter<ResponseBody, ResponseT> responseConverter) {
    this.requestFactory = requestFactory;
    this.callFactory = callFactory;
    this.callAdapter = callAdapter;
    this.responseConverter = responseConverter;
  }

  @Override ReturnT invoke(Object[] args) {
    return callAdapter.adapt(
        new OkHttpCall<>(requestFactory, args, callFactory, responseConverter));
  }
}
