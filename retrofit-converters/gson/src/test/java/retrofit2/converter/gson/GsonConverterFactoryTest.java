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
package retrofit2.converter.gson;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonIOException;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import okhttp3.*;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import okio.Buffer;
import org.assertj.core.annotations.Nullable;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import retrofit2.Call;
import retrofit2.Converter;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.http.*;
import retrofit2.http.Headers;

public final class GsonConverterFactoryTest {
  interface AnInterface {
    String getName();
  }

  static class AnImplementation implements AnInterface {
    private final String theName;

    AnImplementation(String name) {
      theName = name;
    }

    @Override
    public String getName() {
      return theName;
    }
  }

  static final class Value {
    static final TypeAdapter<Value> BROKEN_ADAPTER =
        new TypeAdapter<Value>() {
          @Override
          public void write(JsonWriter out, Value value) {
            throw new AssertionError();
          }

          @Override
          public Value read(JsonReader reader) throws IOException {
            reader.beginObject();
            reader.nextName();
            String theName = reader.nextString();
            return new Value(theName);
          }
        };

    final String theName;

    Value(String theName) {
      this.theName = theName;
    }
  }

  static class AnInterfaceAdapter extends TypeAdapter<AnInterface> {
    @Override
    public void write(JsonWriter jsonWriter, AnInterface anInterface) throws IOException {
      jsonWriter.beginObject();
      jsonWriter.name("name").value(anInterface.getName());
      jsonWriter.endObject();
    }

    @Override
    public AnInterface read(JsonReader jsonReader) throws IOException {
      jsonReader.beginObject();

      String name = null;
      while (jsonReader.peek() != JsonToken.END_OBJECT) {
        switch (jsonReader.nextName()) {
          case "name":
            name = jsonReader.nextString();
            break;
        }
      }

      jsonReader.endObject();
      return new AnImplementation(name);
    }
  }

  static class FormBodyAdapter extends TypeAdapter<FormBody> {
    @Override
    public void write(JsonWriter out, FormBody formBody) throws IOException {
      out.beginObject();
      int size = formBody.size();
      for (int i = 0; i < size; i++) {
        out.name(formBody.name(i)).value(formBody.value(i));
      }
      out.endObject();
    }

    @Override
    public FormBody read(JsonReader in) throws IOException {
      throw new UnsupportedOperationException("Not implemented");
    }
  }

  static class MultipartBodyAdapter extends TypeAdapter<MultipartBody> {

    @Override
    public void write(JsonWriter out, MultipartBody multipartBody) throws IOException {
      out.beginObject();
      List<MultipartBody.Part> parts = multipartBody.parts();
      for (MultipartBody.Part part : parts) {
        MediaType mediaType = part.body().contentType();
        if ("text".equals(mediaType.type())) {
          Map<String, String> map = parseContentDisposition(part.headers());
          String name = map.get("name");
          out.name(name);
          Buffer buffer = new Buffer();
          part.body().writeTo(buffer);
          String bodyString = buffer.readUtf8();
          out.value(bodyString);
        } else {
          throw new UnsupportedOperationException(mediaType.toString());
        }
      }
      out.endObject();
    }

    private Map<String, String> parseContentDisposition(okhttp3.Headers headers) {
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

    @Override
    public MultipartBody read(JsonReader in) throws IOException {
      throw new UnsupportedOperationException("Not implemented");
    }
  }

  interface Service {
    @POST("/")
    Call<AnImplementation> anImplementation(@Body AnImplementation impl);

    @POST("/")
    Call<AnInterface> anInterface(@Body AnInterface impl);

    @GET("/")
    Call<Value> value();

    @FormUrlEncoded //
    @POST("/") //
    @Headers("Content-Type: application/json")
    Call<ResponseBody> method(
        @Field("firstName") String firstName, @Field("lastName") String lastName);

    @Multipart //
    @POST("/") //
    @Headers("Content-Type: application/json")
    Call<ResponseBody> multipart(
        @Part("firstName") String firstName, @Part("lastName") String lastName);
  }

  @Rule public final MockWebServer server = new MockWebServer();

  private Service service;

  @Before
  public void setUp() {
    Gson gson =
        new GsonBuilder()
            .registerTypeAdapter(AnInterface.class, new AnInterfaceAdapter())
            .registerTypeAdapter(Value.class, Value.BROKEN_ADAPTER)
            .registerTypeAdapter(FormBody.class, new FormBodyAdapter())
            .registerTypeAdapter(MultipartBody.class, new MultipartBodyAdapter())
            .setLenient()
            .create();
    Retrofit retrofit =
        new Retrofit.Builder()
            .baseUrl(server.url("/"))
            .addConverterFactory(new ToStringConverterFactory())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build();
    service = retrofit.create(Service.class);
  }

  public static class ToStringConverterFactory extends Converter.Factory {
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
    public @Nullable Converter<String, RequestBody> requestBodyConverter(
        Type type,
        Annotation[] parameterAnnotations,
        Annotation[] methodAnnotations,
        Retrofit retrofit) {
      if (String.class.equals(type)) {
        return value -> RequestBody.create(MEDIA_TYPE, value);
      }
      return null;
    }
  }

  @Test
  public void anInterface() throws IOException, InterruptedException {
    server.enqueue(new MockResponse().setBody("{\"name\":\"value\"}"));

    Call<AnInterface> call = service.anInterface(new AnImplementation("value"));
    Response<AnInterface> response = call.execute();
    AnInterface body = response.body();
    assertThat(body.getName()).isEqualTo("value");

    RecordedRequest request = server.takeRequest();
    assertThat(request.getBody().readUtf8()).isEqualTo("{\"name\":\"value\"}");
    assertThat(request.getHeader("Content-Type")).isEqualTo("application/json; charset=UTF-8");
  }

  @Test
  public void anImplementation() throws IOException, InterruptedException {
    server.enqueue(new MockResponse().setBody("{\"theName\":\"value\"}"));

    Call<AnImplementation> call = service.anImplementation(new AnImplementation("value"));
    Response<AnImplementation> response = call.execute();
    AnImplementation body = response.body();
    assertThat(body.theName).isEqualTo("value");

    RecordedRequest request = server.takeRequest();
    assertThat(request.getBody().readUtf8()).isEqualTo("{\"theName\":\"value\"}");
    assertThat(request.getHeader("Content-Type")).isEqualTo("application/json; charset=UTF-8");
  }

  @Test
  public void serializeUsesConfiguration() throws IOException, InterruptedException {
    server.enqueue(new MockResponse().setBody("{}"));

    service.anImplementation(new AnImplementation(null)).execute();

    RecordedRequest request = server.takeRequest();
    assertThat(request.getBody().readUtf8()).isEqualTo("{}"); // Null value was not serialized.
    assertThat(request.getHeader("Content-Type")).isEqualTo("application/json; charset=UTF-8");
  }

  @Test
  public void deserializeUsesConfiguration() throws IOException, InterruptedException {
    server.enqueue(new MockResponse().setBody("{/* a comment! */}"));

    Response<AnImplementation> response =
        service.anImplementation(new AnImplementation("value")).execute();
    assertThat(response.body().getName()).isNull();
  }

  @Test
  public void requireFullResponseDocumentConsumption() throws Exception {
    server.enqueue(new MockResponse().setBody("{\"theName\":\"value\"}"));

    Call<Value> call = service.value();
    try {
      call.execute();
      fail();
    } catch (JsonIOException e) {
      assertThat(e).hasMessage("JSON document was not fully consumed.");
    }
  }

  @Test
  public void testContentTypeOverridesFormEncoded() throws IOException {
    server.enqueue(new MockResponse().setBody("{\"theName\":\"value\"}"));

    Call<ResponseBody> call = service.method("zhang", "san");
    try {
      Request request = call.request();
      RequestBody body = request.body();
      Buffer buffer = new Buffer();
      try {
        body.writeTo(buffer);
        String actual = buffer.readUtf8();
        assertThat(actual).isEqualTo("{\"firstName\":\"zhang\",\"lastName\":\"san\"}");
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      assertThat(request.body().contentType().toString()).isEqualTo("application/json");
    } catch (JsonIOException e) {
      assertThat(e).hasMessage("JSON document was not fully consumed.");
    }
  }

  @Test
  public void testContentTypeOverridesMultipart() throws IOException {
    server.enqueue(new MockResponse().setBody("{\"theName\":\"value\"}"));

    Call<ResponseBody> call = service.multipart("zhang", "san");
    try {
      Request request = call.request();
      RequestBody body = request.body();
      Buffer buffer = new Buffer();
      try {
        body.writeTo(buffer);
        String actual = buffer.readUtf8();
        assertThat(actual).isEqualTo("{\"firstName\":\"zhang\",\"lastName\":\"san\"}");
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
      assertThat(request.body().contentType().toString()).isEqualTo("application/json");
    } catch (JsonIOException e) {
      assertThat(e).hasMessage("JSON document was not fully consumed.");
    }
  }
}
