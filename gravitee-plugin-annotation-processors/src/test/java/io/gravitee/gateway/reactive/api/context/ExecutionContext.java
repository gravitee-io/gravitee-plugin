/*
 * Copyright © 2015 The Gravitee team (http://gravitee.io)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.gravitee.gateway.reactive.api.context;

import io.gravitee.gateway.reactive.api.ExecutionFailure;
import io.reactivex.rxjava3.core.Completable;
import io.reactivex.rxjava3.core.Maybe;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Getter;

/**
 * @author Remi Baptiste (remi.baptiste at graviteesource.com)
 * @author GraviteeSource Team
 */
public class ExecutionContext {

    @Getter
    private final TemplateEngine templateEngine;

    private final Map<String, Object> attributes;

    @Getter
    private final Map<String, Object> internalAttributes;

    public ExecutionContext() {
        this(new TemplateEngine(), new HashMap<>());
    }

    public ExecutionContext(TemplateEngine templateEngine) {
        this(templateEngine, new HashMap<>());
    }

    public ExecutionContext(Map<String, Object> attributes) {
        this(new TemplateEngine(), attributes);
    }

    public ExecutionContext(TemplateEngine templateEngine, Map<String, Object> attributes) {
        this.templateEngine = templateEngine;
        this.attributes = attributes;
        this.internalAttributes = new HashMap<>();
    }

    public <T> T getAttribute(String attribute) {
        return (T) attributes.get(attribute);
    }

    public <T> List<T> getAttributeAsList(String name) {
        var value = this.attributes.get(name);

        if (value == null) {
            return null;
        }
        if (value instanceof List) {
            return (List<T>) value;
        }
        if (value instanceof String) {
            return (List<T>) Arrays.stream(((String) value).split(",")).toList();
        }

        return List.of((T) value);
    }

    public void setInternalAttribute(String var1, Object var2) {
        internalAttributes.put(var1, var2);
    }

    public <T> T getInternalAttribute(String attribute) {
        return (T) internalAttributes.get(attribute);
    }

    public Completable interruptWith(final ExecutionFailure failure) {
        return Completable.defer(() -> Completable.error(new IllegalStateException((failure.message()))));
    }

    public static class TemplateEngine {

        public <T> Maybe<T> eval(String expression, Class<T> clazz) {
            return Maybe.just((T) expression);
        }
    }
}
