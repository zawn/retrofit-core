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
import java.io.IOException;
import java.lang.reflect.Array;

public abstract class ParameterHandler<RequestBuilder, T> {
  public abstract void apply(RequestBuilder builder, @Nullable T value) throws IOException;

  public final ParameterHandler<RequestBuilder, Iterable<T>> iterable() {
    return new ParameterHandler<RequestBuilder, Iterable<T>>() {
      @Override public void apply(RequestBuilder builder, @Nullable Iterable<T> values)
          throws IOException {
        if (values == null) return; // Skip null values.

        for (T value : values) {
          ParameterHandler.this.apply(builder, value);
        }
      }
    };
  }

  public final ParameterHandler<RequestBuilder, Object> array() {
    return new ParameterHandler<RequestBuilder, Object>() {
      @Override public void apply(RequestBuilder builder, @Nullable Object values) throws IOException {
        if (values == null) return; // Skip null values.

        for (int i = 0, size = Array.getLength(values); i < size; i++) {
          //noinspection unchecked
          ParameterHandler.this.apply(builder, (T) Array.get(values, i));
        }
      }
    };
  }
}
