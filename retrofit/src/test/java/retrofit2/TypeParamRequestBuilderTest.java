/*
 * Copyright (C) 2013 Square, Inc.
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.lang.reflect.Method;
import okhttp3.MediaType;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okio.Buffer;
import org.junit.Test;
import retrofit2.helpers.ToStringConverterFactory;
import retrofit2.http.GET;
import retrofit2.http.ParamHeaders;
import retrofit2.http.ParamQuerys;
import retrofit2.http.ParamUrl;
import retrofit2.http.Query;

@SuppressWarnings({"UnusedParameters", "unused"}) // Parameters inspected reflectively.
public final class TypeParamRequestBuilderTest {
  private static final MediaType TEXT_PLAIN = MediaType.parse("text/plain");

  @Test
  public void noParamProvider() throws Exception {

    @ParamHeaders({"X-House365: {house365}", "User-Agent: {userAgent}"})
    @ParamQuerys({"userid={userid}", "city={city}", "v={verison}", "version={version}"})
    class Example {
      @GET("/foo") //
      Call<ResponseBody> method(@Query("bar") String thing) {
        return null;
      }
    }
    try {
      buildRequest(Example.class, null, "zhang");
      fail();
    } catch (IllegalArgumentException e) {
      assertThat(e).hasMessage("Use type parameters must be set ParamProvider");
    }
  }

  @Test
  public void paramProvider() throws Exception {

    @ParamHeaders({"X-House365: {house365}", "User-Agent: {userAgent}"})
    @ParamQuerys({"userid={userid}", "city={city}", "v={verison}", "version={version}"})
    class Example {
      @GET("/foo") //
      Call<ResponseBody> method(@Query("bar") String thing) {
        return null;
      }
    }
    final Request request = buildRequest(Example.class, paramProvider, "zhang");
    assertThat(request.headers().get("X-House365")).isEqualTo("formParam-house365");
    assertThat(request.headers().get("User-Agent")).isEqualTo("formParam-userAgent");
  }

  @Test
  public void errorMultipleHeaderParam() throws Exception {

    @ParamHeaders({"X-House365: {house365},{house366}", "User-Agent: {userAgent}"})
    @ParamQuerys({"userid={userid}", "city={city}", "v={verison}", "version={version}"})
    class Example {
      @GET("/foo") //
      Call<ResponseBody> method(@Query("bar") String thing) {
        return null;
      }
    }
    try {
      final Request request = buildRequest(Example.class, paramProvider, "zhang");
      assertThat(request.headers().get("X-House365")).isEqualTo("formParam-house365");
      fail();
    } catch (IllegalArgumentException e) {
      assertThat(e)
          .hasMessage(
              "@ParamHeader Configuration errors,at X-House365, You can only have a maximum of one "
                  + "parameter");
    }
  }

  @Test
  public void multipleHeaderParam() throws Exception {

    @ParamHeaders({
      "X-House365-Client: {deviceid=<deviceId>;phone=<phone>;uid=<userId>;"
          + "app_channel=<app_channel>}"
    })
    @ParamQuerys({"userid={userid}", "city={city}", "v={verison}", "version={version}"})
    class Example {
      @GET("/foo") //
      Call<ResponseBody> method(@Query("bar") String thing) {
        return null;
      }
    }
    try {
      final Request request = buildRequest(Example.class, paramProvider, "zhang");
      assertThat(request.headers().get("X-House365-Client"))
          .isEqualTo(
              "formParam-deviceid=%3CdeviceId%3E;"
                  + "phone=%3Cphone%3E;uid=%3CuserId%3E;app_channel=%3Capp_channel%3E");
    } catch (IllegalArgumentException e) {
      assertThat(e)
          .hasMessage(
              "@ParamHeader Configuration errors,at X-House365, You can only have a maximum of one "
                  + "parameter");
    }
  }

  @Test
  public void urlParam() throws Exception {

    //    @ParamQuerys({"userid={userid}", "city={city}", "v={verison}", "version={version}"})
    @ParamUrl("http://www.baidu.com")
    class Example {
      @GET("/foo") //
      Call<ResponseBody> method(@Query("bar") String thing) {
        return null;
      }
    }
    try {
      final Request request = buildRequest(Example.class, paramProvider, "zhang");
      assertThat(request.url().toString()).isEqualTo("http://www.baidu.com/foo?bar=zhang");
    } catch (IllegalArgumentException e) {
      assertThat(e)
          .hasMessage(
              "@ParamHeader Configuration errors,at X-House365, You can only have a maximum of one "
                  + "parameter");
    }
  }

  @Test
  public void url2Param() throws Exception {

    @ParamUrl("{base}")
    @ParamQuerys({"userid={userid}", "city={city}", "v={verison}", "version={version}"})
    class Example {
      @GET("/foo") //
      Call<ResponseBody> method(@Query("bar") String thing) {
        return null;
      }
    }
    try {
      final Request request = buildRequest(Example.class, paramProvider, "zhang");
      assertThat(request.url().toString())
          .isEqualTo(
              "http://www.baidu"
                  + ".com/foo?userid=formParam-userid&city=formParam-city&v=formParam-verison&version=formParam-version&bar=zhang");
    } catch (IllegalArgumentException e) {
      assertThat(e)
          .hasMessage(
              "@ParamHeader Configuration errors,at X-House365, You can only have a maximum of one "
                  + "parameter");
    }
  }

  @Test
  public void paramQuerys() throws Exception {

    @ParamQuerys({
      "userid={userid}",
      "city={city}",
      "v={verison}",
      "version={version}",
      "userName=zhangsan"
    })
    class Example {
      @GET("/foo") //
      Call<ResponseBody> method(@Query("bar") String thing) {
        return null;
      }
    }
    try {
      final Request request = buildRequest(Example.class, paramProvider, "zhang");
      assertTrue(request.url().toString().contains("userName=zhangsan"));
      assertThat(request.url().toString())
          .isEqualTo(
              "http://example"
                  + ".com/foo?userid=formParam-userid&city=formParam-city&v=formParam-verison&version=formParam-version&userName=zhangsan&bar=zhang");
    } catch (IllegalArgumentException e) {
      assertThat(e)
          .hasMessage(
              "@ParamHeader Configuration errors,at X-House365, You can only have a maximum of one "
                  + "parameter");
    }
  }

  private static void assertBody(RequestBody body, String expected) {
    assertThat(body).isNotNull();
    Buffer buffer = new Buffer();
    try {
      body.writeTo(buffer);
      assertThat(buffer.readUtf8()).isEqualTo(expected);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  private static ParamProvider paramProvider =
      new ParamProvider() {
        @Override
        public Object getHeaderParam(String paramName) {
          return "formParam-" + paramName;
        }

        @Override
        public Object getUrlParam(String paramName) {
          return "http://www.baidu.com";
        }

        @Override
        public Object getQueryParam(String paramName) {
          return "formParam-" + paramName;
        }
      };

  static <T> Request buildRequest(Class<T> cls, ParamProvider paramProvider, Object... args) {
    okhttp3.Call.Factory callFactory =
        new okhttp3.Call.Factory() {
          @Override
          public okhttp3.Call newCall(Request request) {
            throw new UnsupportedOperationException("Not implemented");
          }
        };

    Retrofit retrofit;
    if (paramProvider == null) {
      retrofit =
          new Retrofit.Builder()
              .baseUrl("http://example.com/")
              .addConverterFactory(new ToStringConverterFactory())
              .callFactory(callFactory)
              .build();

    } else {
      retrofit =
          new Retrofit.Builder()
              .baseUrl("http://example.com/")
              .addConverterFactory(new ToStringConverterFactory())
              .setParamProvider(paramProvider)
              .callFactory(callFactory)
              .build();
    }
    Method method = TestingUtils.onlyMethod(cls);
    try {
      return RequestFactory.parseAnnotations(retrofit, method).create(args);
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw new AssertionError(e);
    }
  }
}
