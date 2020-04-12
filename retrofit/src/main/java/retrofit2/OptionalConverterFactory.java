/*
 * Copyright (C) 2017 Square, Inc.
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

import org.codehaus.mojo.animal_sniffer.IgnoreJRERequirement;

import javax.annotation.Nullable;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;

@IgnoreJRERequirement // Only added when Optional is available (Java 8+ / Android API 24+).
final class OptionalConverterFactory<F, T> extends ConverterFactory<F, T> {
    static final ConverterFactory INSTANCE = new OptionalConverterFactory();

    @Override
    public @Nullable
    Converter<T, ?> responseBodyConverter(
            Type type, Annotation[] annotations, Retrofit retrofit) {
        if (getRawType(type) != Optional.class) {
            return null;
        }

        Type innerType = getParameterUpperBound(0, (ParameterizedType) type);
        Converter<T, Object> delegate =
                retrofit.responseBodyConverter(innerType, annotations);
        return new OptionalConverter<>(delegate);
    }

    @IgnoreJRERequirement
    static final class OptionalConverter<F, T> implements Converter<F, Optional<T>> {
        final Converter<F, T> delegate;

        OptionalConverter(Converter<F, T> delegate) {
            this.delegate = delegate;
        }

        @Override
        public Optional<T> convert(F value) throws IOException {
            return Optional.ofNullable(delegate.convert(value));
        }
    }
}
