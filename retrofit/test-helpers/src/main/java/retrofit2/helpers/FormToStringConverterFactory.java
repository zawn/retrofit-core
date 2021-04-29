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
package retrofit2.helpers;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;
import okhttp3.*;
import okio.Buffer;
import retrofit2.Converter;
import retrofit2.Retrofit;

public class FormToStringConverterFactory extends Converter.Factory {
  static final MediaType MEDIA_TYPE = MediaType.get("text/plain");

  @Override
  public @Nullable Converter<ResponseBody, String> responseBodyConverter(
      Type type, Annotation[] annotations, Retrofit retrofit) {
    if (String.class.equals(type)) {
      return ResponseBody::string;
    }
    return null;
  }

  @Override
  public @Nullable Converter<?, RequestBody> requestBodyConverter(
      Type type,
      Annotation[] parameterAnnotations,
      Annotation[] methodAnnotations,
      Retrofit retrofit) {
    if (FormBody.class.equals(type)) {
      return new FormBodyConverter();
    }
    if (MultipartBody.class.equals(type)) {
      return new MultipartBodyConverter();
    }
    return null;
  }

  private static class FormBodyConverter implements Converter<FormBody, RequestBody> {
    @Nullable
    @Override
    public RequestBody convert(FormBody formBody) throws IOException {
      StringBuilder stringBuilder = new StringBuilder();
      int size = formBody.size();
      for (int i = 0; i < size; i++) {
        stringBuilder.append(formBody.name(i)).append("=").append(formBody.value(i)).append(";");
      }
      return RequestBody.create(MEDIA_TYPE, stringBuilder.toString());
    }
  }

  private static class MultipartBodyConverter implements Converter<MultipartBody, RequestBody> {
    @Nullable
    @Override
    public RequestBody convert(MultipartBody multipartBody) throws IOException {
      StringBuilder stringBuilder = new StringBuilder();
      List<MultipartBody.Part> parts = multipartBody.parts();
      for (MultipartBody.Part part : parts) {
        MediaType mediaType = part.body().contentType();
        if ("text".equals(mediaType.type())) {
          Map<String, String> map = parseContentDisposition(part.headers());
          String name = map.get("name");
          stringBuilder.append(name).append("=");
          Buffer buffer = new Buffer();
          part.body().writeTo(buffer);
          String bodyString = buffer.readUtf8();
          stringBuilder.append(bodyString).append(";");
        } else {
          throw new UnsupportedOperationException(mediaType.toString());
        }
      }
      return RequestBody.create(MEDIA_TYPE, stringBuilder.toString());
    }

    private Map<String, String> parseContentDisposition(Headers headers) {
      Map<String, String> map = new LinkedHashMap<>();
      String headerValue = headers.get("Content-Disposition");
      if (headerValue != null) {
        String[] split = headerValue.split(";");
        for (String nameValue : split) {
          String name = nameValue;
          String value = null;
          int i = nameValue.indexOf("=");
          if (i > -1) {
            String[] strings = nameValue.split("=");
            name = strings[0].trim();
            String regex = "^" + "\"" + "*|" + "\"" + "*$";
            value = strings[1].replaceAll(regex, "");
          }
          if (name != null) {
            name = name.trim();
          }
          map.put(name, value);
        }
      }
      return map;
    }
  }
}
